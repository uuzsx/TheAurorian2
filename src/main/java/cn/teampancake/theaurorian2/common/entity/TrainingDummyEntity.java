package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModItems;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

public final class TrainingDummyEntity extends LivingEntity implements GeoEntity {

    public static final String HIT_CONTROLLER = "hit_controller";
    public static final String HIT_ANIMATION = "hit";
    private static final int STAT_RESET_TICKS = 5 * 20;
    private static final long NO_COMBAT = -1L;
    private static final EntityDataAccessor<Float> TOTAL_DAMAGE =
            SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAST_HIT_DAMAGE =
            SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> COMBAT_START_TICK =
            SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> LAST_HIT_TICK =
            SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.LONG);
    private static final RawAnimation HIT = RawAnimation.begin().thenPlay(HIT_ANIMATION);
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public TrainingDummyEntity(EntityType<? extends TrainingDummyEntity> type, Level level) {
        super(type, level);
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1024.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(TOTAL_DAMAGE, 0.0F);
        entityData.define(LAST_HIT_DAMAGE, 0.0F);
        entityData.define(COMBAT_START_TICK, NO_COMBAT);
        entityData.define(LAST_HIT_TICK, NO_COMBAT);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            long lastHitTick = this.entityData.get(LAST_HIT_TICK);
            if (lastHitTick != NO_COMBAT && this.level().getGameTime() - lastHitTick >= STAT_RESET_TICKS) {
                this.resetDamageStats();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.invulnerableTime = 0;
        this.lastHurt = 0.0F;
        boolean damaged = super.hurtServer(level, source, damage);
        this.invulnerableTime = 0;
        this.lastHurt = 0.0F;
        return damaged;
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        super.actuallyHurt(level, source, damage);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void onDamageTaken(DamageContainer damageContainer) {
        float damage = damageContainer.getNewDamage();
        if (damage <= 0.0F || !(this.level() instanceof ServerLevel level)) {
            return;
        }

        this.recordDamage(level, damage);
        this.triggerAnim(HIT_CONTROLLER, HIT_ANIMATION);
        DamageNumberEntity number = ModEntities.DAMAGE_NUMBER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (number == null) {
            return;
        }

        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double radius = 0.08 + this.random.nextDouble() * 0.18;
        number.snapTo(
                this.getX() + Math.cos(angle) * radius,
                this.getY() + this.getBbHeight() * 0.78 + this.random.nextDouble() * 0.12,
                this.getZ() + Math.sin(angle) * radius);
        number.setDamage(damage);
        level.addFreshEntity(number);
    }

    public float getTotalDamage() {
        return this.entityData.get(TOTAL_DAMAGE);
    }

    public float getLastHitDamage() {
        return this.entityData.get(LAST_HIT_DAMAGE);
    }

    public float getDamagePerSecond() {
        long startTick = this.entityData.get(COMBAT_START_TICK);
        long lastHitTick = this.entityData.get(LAST_HIT_TICK);
        if (startTick == NO_COMBAT || lastHitTick == NO_COMBAT) {
            return 0.0F;
        }

        long elapsedTicks = Math.max(20L, lastHitTick - startTick);
        return this.getTotalDamage() * 20.0F / elapsedTicks;
    }

    private void recordDamage(ServerLevel level, float damage) {
        long now = level.getGameTime();
        long lastHitTick = this.entityData.get(LAST_HIT_TICK);
        if (lastHitTick == NO_COMBAT || now - lastHitTick >= STAT_RESET_TICKS) {
            this.resetDamageStats();
            this.entityData.set(COMBAT_START_TICK, now);
        }

        this.entityData.set(TOTAL_DAMAGE, this.getTotalDamage() + damage);
        this.entityData.set(LAST_HIT_DAMAGE, damage);
        this.entityData.set(LAST_HIT_TICK, now);
    }

    private void resetDamageStats() {
        this.entityData.set(TOTAL_DAMAGE, 0.0F);
        this.entityData.set(LAST_HIT_DAMAGE, 0.0F);
        this.entityData.set(COMBAT_START_TICK, NO_COMBAT);
        this.entityData.set(LAST_HIT_TICK, NO_COMBAT);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS_SERVER.withoutItem();
        }

        if (!player.getAbilities().instabuild) {
            ItemStack dummyItem = new ItemStack(ModItems.TRAINING_DUMMY.get());
            if (!player.addItem(dummyItem)) {
                player.drop(dummyItem, false);
            }
        }

        this.level().playSound(
                null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, SoundSource.BLOCKS, 0.75F, 1.0F);
        this.gameEvent(GameEvent.ENTITY_INTERACT, player);
        this.discard();
        return InteractionResult.SUCCESS_SERVER.withoutItem();
    }

    @Override
    public void travel(Vec3 input) {
        double fixedX = this.getX();
        double fixedZ = this.getZ();
        super.travel(Vec3.ZERO);
        this.setPos(fixedX, this.getY(), fixedZ);
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
    }

    @Override
    public void knockback(double power, double x, double z) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TrainingDummyEntity>(HIT_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(HIT_ANIMATION, HIT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
