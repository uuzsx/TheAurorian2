package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.item.WorldScrollItem;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.world.AurorianTravel;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Transient, server-timed VFX; never saves or changes the player's travel record. */
public final class WorldScrollTeleportEntity extends Entity implements GeoEntity {
    public static final int ARRIVAL_TICKS = 20;
    // Allow the loading screen's 30-second client neighborhood fallback to finish
    // before the missing-ack safeguard starts the arrival animation.
    public static final int VIEW_READY_TIMEOUT_TICKS = 800;
    private static final byte CHARGING = 0, DEPARTED = 1, ARRIVING = 2, CANCELLED = 3, WAITING_FOR_VIEW = 4;
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Byte> PHASE = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ARRIVAL = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> TRANSITION = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> CANCEL_TIME = SynchedEntityData.defineId(
            WorldScrollTeleportEntity.class, EntityDataSerializers.FLOAT);
    private static final RawAnimation ORIGIN = RawAnimation.begin().thenPlayAndHold("teleport_charge_origin");
    private static final RawAnimation DESTINATION = RawAnimation.begin().thenPlayAndHold("teleport_charge_destination");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private boolean soundStarted;
    private boolean readySent;

    public WorldScrollTeleportEntity(EntityType<? extends WorldScrollTeleportEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public static @Nullable WorldScrollTeleportEntity active(Player player) {
        int id = player.getExistingData(ModAttachments.WORLD_SCROLL_EFFECT).orElse(-1);
        return id >= 0 && player.level().getEntity(id) instanceof WorldScrollTeleportEntity effect
                && !effect.isRemoved() && effect.entityData.get(OWNER) == player.getId() ? effect : null;
    }

    public static void begin(ServerPlayer player, boolean arrival) {
        WorldScrollTeleportEntity old = active(player);
        if (old != null) old.cancel();
        ServerLevel level = player.level();
        WorldScrollTeleportEntity effect = ModEntities.WORLD_SCROLL_TELEPORT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (effect == null) return;
        effect.setPos(player.position());
        effect.entityData.set(OWNER, player.getId());
        effect.entityData.set(START, level.getGameTime());
        effect.entityData.set(ARRIVAL, arrival);
        effect.entityData.set(PHASE, arrival ? WAITING_FOR_VIEW : CHARGING);
        if (level.addFreshEntity(effect)) player.setData(ModAttachments.WORLD_SCROLL_EFFECT, effect.getId());
    }

    public void completeDeparture() {
        if (entityData.get(PHASE) != CHARGING) return;
        entityData.set(TRANSITION, level().getGameTime());
        entityData.set(PHASE, DEPARTED);
        releaseOwner();
    }

    public void cancel() {
        if (entityData.get(PHASE) == CANCELLED) return;
        entityData.set(CANCEL_TIME, animationSeconds(0));
        entityData.set(TRANSITION, level().getGameTime());
        entityData.set(PHASE, CANCELLED);
        releaseOwner();
    }

    public boolean isArrival() { return entityData.get(ARRIVAL); }
    public boolean isWaitingForView() { return entityData.get(PHASE) == WAITING_FOR_VIEW; }
    public boolean markReadySent() {
        if (readySent) return false;
        readySent = true;
        return true;
    }
    public void startArrival() {
        if (!isWaitingForView()) return;
        entityData.set(START, level().getGameTime());
        entityData.set(PHASE, ARRIVING);
    }
    public boolean isCancelled() { return entityData.get(PHASE) == CANCELLED; }
    public boolean isInitialized() { return entityData.get(START) >= 0; }
    public float elapsedTicks(float partialTick) {
        return Math.max(0, level().getGameTime() - entityData.get(START) + partialTick);
    }
    public boolean isMaterializing() {
        return isWaitingForView() || entityData.get(PHASE) == ARRIVING && elapsedTicks(0) < ARRIVAL_TICKS;
    }
    public boolean startSoundOnce() {
        if (soundStarted || !isInitialized() || isWaitingForView()) return false;
        soundStarted = true;
        return !isCancelled() && elapsedTicks(0) < 8;
    }

    public float animationSeconds(float partialTick) {
        float age = elapsedTicks(partialTick);
        return switch (entityData.get(PHASE)) {
            case WAITING_FOR_VIEW -> 0;
            case DEPARTED -> Math.min(5, 2.65F + (level().getGameTime() - entityData.get(TRANSITION) + partialTick) / 20F);
            case ARRIVING -> Math.min(5, age <= ARRIVAL_TICKS ? age * 3F / ARRIVAL_TICKS : 3 + (age - ARRIVAL_TICKS) / 20F);
            case CANCELLED -> entityData.get(CANCEL_TIME);
            default -> Math.min(2.65F, age * 2.65F / WorldScrollItem.USE_DURATION);
        };
    }

    public float opacity(float partialTick) {
        if (isWaitingForView()) return 0;
        return isCancelled() ? Mth.clamp(1 - (level().getGameTime() - entityData.get(TRANSITION) + partialTick) / 6F, 0, 1) : 1;
    }

    /** Authored player X/Z collapse, applied to the actual avatar and equipment. */
    public float playerWidth(float partialTick) {
        float time = animationSeconds(partialTick);
        if (isArrival()) return Mth.clamp((time - 2.95F) / 0.05F, 0, 1);
        if (entityData.get(PHASE) == CHARGING) return Mth.clamp((2.65F - time) / 0.05F, 0, 1);
        return 1;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        byte phase = entityData.get(PHASE);
        if (phase == CANCELLED) {
            if (level().getGameTime() - entityData.get(TRANSITION) >= 6) discard();
            return;
        }
        if (!isWaitingForView() && (animationSeconds(0) >= 5 || elapsedTicks(0) > 120)) {
            releaseOwner();
            discard();
            return;
        }
        if (phase == DEPARTED) return;
        Entity owner = level().getEntity(entityData.get(OWNER));
        if (!(owner instanceof ServerPlayer player) || !player.isAlive()) {
            cancel();
            return;
        }
        if (phase == CHARGING) {
            if (active(player) != this || !player.isUsingItem()
                    || !(player.getUseItem().getItem() instanceof WorldScrollItem)
                    || !AurorianTravel.canBegin(player, false)) {
                cancel();
                return;
            }
            setPos(player.position());
        } else if (isMaterializing()) {
            // Wait for the owner's loading screen to close. A bounded fallback
            // prevents a missing client acknowledgement from holding them forever.
            if (isWaitingForView() && elapsedTicks(0) >= VIEW_READY_TIMEOUT_TICKS) startArrival();
            // A one-second arrival hold; only correct actual movement, never send
            // a repeated position packet to an already stationary player.
            player.setDeltaMovement(Vec3.ZERO);
            player.resetFallDistance();
            if (player.distanceToSqr(this) > 0.0001) {
                player.connection.teleport(getX(), getY(), getZ(), player.getYRot(), player.getXRot());
            }
        } else {
            releaseOwner();
        }
    }

    private void releaseOwner() {
        if (level().getEntity(entityData.get(OWNER)) instanceof Player player
                && player.getExistingData(ModAttachments.WORLD_SCROLL_EFFECT).orElse(-1) == getId()) {
            player.setData(ModAttachments.WORLD_SCROLL_EFFECT, -1);
        }
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, -1);
        builder.define(START, -1L);
        builder.define(PHASE, CHARGING);
        builder.define(ARRIVAL, false);
        builder.define(TRANSITION, 0L);
        builder.define(CANCEL_TIME, 0F);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) { }
    @Override protected void addAdditionalSaveData(ValueOutput output) { }
    @Override public boolean isPickable() { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float damage) { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 96 * 96; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<WorldScrollTeleportEntity>("teleport", 0, state -> {
            state.setAnimation(isArrival() ? DESTINATION : ORIGIN);
            state.controller().setAnimationTime(animationSeconds(state.renderState().getPartialTick()));
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }
}
