package cn.teampancake.theaurorian2.common.entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WallClimberSpiderlingEntity extends AbstractSpiderlingEntity {

    private static final EntityDataAccessor<Byte> CLIMBING =
            SynchedEntityData.defineId(WallClimberSpiderlingEntity.class, EntityDataSerializers.BYTE);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final int SHOT_COOLDOWN_TICKS = 50;
    private int shotCooldown = 20;

    public WallClimberSpiderlingEntity(EntityType<? extends WallClimberSpiderlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 3;
        this.updateAttackDamage(level.getDifficulty(), 8.0, 10.0, 12.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSpiderlingAttributes(18.0, 4.0, 8.0, 0.35);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(CLIMBING, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.updateAttackDamage(level.getDifficulty(), 8.0, 10.0, 12.0);
        super.customServerAiStep(level);
        this.tickRangedAttack(level);
    }

    private void tickRangedAttack(ServerLevel level) {
        if (this.shotCooldown > 0) {
            this.shotCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (this.shotCooldown <= 0
                && target != null
                && target.isAlive()
                && this.distanceToSqr(target) <= 16.0 * 16.0
                && this.getSensing().hasLineOfSight(target)) {
            SpiderVenomProjectileEntity.shootWebbing(
                    level, this, target, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            this.shotCooldown = SHOT_COOLDOWN_TICKS;
        }
    }

    public boolean isClimbing() {
        return (this.entityData.get(CLIMBING) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte flags = this.entityData.get(CLIMBING);
        if (climbing) {
            flags = (byte)(flags | 1);
        } else {
            flags = (byte)(flags & -2);
        }
        this.entityData.set(CLIMBING, flags);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ShotCooldown", this.shotCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.shotCooldown = input.getIntOr("ShotCooldown", 20);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<WallClimberSpiderlingEntity>(
                "idle", state -> state.setAndContinue(IDLE)));
    }
}
