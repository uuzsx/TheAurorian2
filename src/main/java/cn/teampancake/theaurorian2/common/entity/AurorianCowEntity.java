package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class AurorianCowEntity extends Cow {
    private static final EntityDataAccessor<Boolean> PANICKING = SynchedEntityData.defineId(AurorianCowEntity.class, EntityDataSerializers.BOOLEAN);
    private @Nullable PanicGoal panicGoal;
    // Transient visual state: no save data or per-frame entity lookups.
    private float panicBlend, previousPanicBlend, idleBlend, previousIdleBlend;
    private int idleVariant, idleStartTick, nextIdleTick, startleTick = -1000, previousHurtTime;
    private boolean previousPanic;
    private boolean warblerCompanionPending;
    public AurorianCowEntity(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof BreedGoal || goal instanceof TemptGoal);
        for (var wrapped : this.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof PanicGoal goal) this.panicGoal = goal;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PANICKING, false);
    }

    public boolean isPanicking() { return this.entityData.get(PANICKING); }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // SynchedEntityData only marks a changed value dirty; retain vanilla goals intact.
            this.entityData.set(PANICKING, this.panicGoal != null && this.panicGoal.isRunning());
            if (warblerCompanionPending) {
                // Spawn only after this cow has joined the world, never during chunk generation.
                warblerCompanionPending = false;
                WarblerCowInteraction.spawnOnCow(this, null, null, EntitySpawnReason.NATURAL);
            }
            return;
        }
        boolean panic = isPanicking();
        if (this.isAlive() && (this.hurtTime > previousHurtTime || (panic && !previousPanic && tickCount - startleTick > 11))) startleTick = tickCount;
        previousHurtTime = this.hurtTime;
        previousPanic = panic;
        previousPanicBlend = panicBlend;
        panicBlend = Mth.approach(panicBlend, panic && isAlive() ? 1.0F : 0.0F, 0.2F);
        previousIdleBlend = idleBlend;
        boolean resting = isAlive() && !panic && walkAnimation.speed() < 0.025F && onGround() && !isInWater() && tickCount - startleTick > 11;
        int duration = idleVariant == 1 ? 140 : idleVariant == 2 ? 120 : 72;
        boolean active = idleVariant != 0 && tickCount - idleStartTick < duration;
        idleBlend = Mth.approach(idleBlend, resting && active ? 1.0F : 0.0F, 0.15F);
        if (idleVariant != 0 && (!resting || !active) && idleBlend == 0) {
            idleVariant = 0;
            nextIdleTick = tickCount + 100 + random.nextInt(141);
        }
        if (nextIdleTick == 0) nextIdleTick = tickCount + 60 + random.nextInt(141);
        if (resting && idleVariant == 0 && tickCount >= nextIdleTick) {
            idleVariant = 1 + random.nextInt(3);
            idleStartTick = tickCount;
        }
    }

    public float panicBlend(float partialTick) { return Mth.lerp(partialTick, previousPanicBlend, panicBlend); }
    public float idleBlend(float partialTick) { return Mth.lerp(partialTick, previousIdleBlend, idleBlend); }
    public int idleVariant() { return idleVariant; }
    public float idleTime(float partialTick) { return (tickCount - idleStartTick + partialTick) / 20.0F; }
    public float startleTime(float partialTick) { return (tickCount - startleTick + partialTick) / 20.0F; }

    public boolean isSafeForWarbler() {
        return isAlive() && !isPassenger() && !isPanicking() && hurtTime == 0
                && getLastDamageSource() == null && !isOnFire() && !isInWater();
    }

    public boolean canAcceptWarbler() {
        return isSafeForWarbler() && getPassengers().isEmpty();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof AzureWarblerEntity ? canAcceptWarbler() : super.canAddPassenger(passenger);
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        if (!(passenger instanceof AzureWarblerEntity)) return super.getPassengerRidingPosition(passenger);
        // Model back: body pivot Y=6.5, top Y=-4, standing feet Y=24.
        float scale = getScale() * getAgeScale();
        return position().add(new Vec3(0, 21.5 / 16.0 * scale, -0.12 * scale)
                .yRot(-yBodyRot * Mth.DEG_TO_RAD));
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return passenger instanceof AzureWarblerEntity
                ? getPassengerRidingPosition(passenger).add(0, 0.15, 0)
                : super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.is(ModItems.AZURE_WARBLER_SPAWN_EGG.get())
                && SpawnEggItem.spawnsEntity(stack, ModEntities.AZURE_WARBLER.get())) {
            if (level() instanceof ServerLevel) {
                if (!WarblerCowInteraction.spawnOnCow(this, stack, player, EntitySpawnReason.SPAWN_ITEM_USE)) {
                    return InteractionResult.FAIL;
                }
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, @Nullable SpawnGroupData group) {
        var result = super.finalizeSpawn(level, difficulty, reason, group);
        warblerCompanionPending = (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION)
                && random.nextInt(5) == 0;
        return result;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (warblerCompanionPending) output.putBoolean("WarblerCompanionPending", true);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        warblerCompanionPending = input.getBooleanOr("WarblerCompanionPending", false);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
