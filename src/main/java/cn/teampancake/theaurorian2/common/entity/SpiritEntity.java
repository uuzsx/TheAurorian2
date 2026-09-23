package cn.teampancake.theaurorian2.common.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Egg-only preview encounter: hovering pursuit and a telegraphed, server-authoritative claw strike. */
public final class SpiritEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> GLIDING =
            SynchedEntityData.defineId(SpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation LOCOMOTION = RawAnimation.begin().thenLoop("misc.locomotion");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack.swing");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final SpiritGlideBlend glideBlend = new SpiritGlideBlend();
    private int attackTicks, attackCooldown;
    private LivingEntity strikeTarget;

    public SpiritEntity(EntityType<? extends SpiritEntity> type, Level level) {
        super(type, level);
        moveControl = new GlideMoveControl();
        setNoGravity(true);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.FOLLOW_RANGE, 35);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GLIDING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new PursueGoal());
        goalSelector.addGoal(6, new DriftGoal());
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isGliding() { return entityData.get(GLIDING); }

    public double glideWeight(int group, float partialTick) {
        return glideBlend.value(group, (tickCount + partialTick) / 20.0);
    }

    private void setGliding(boolean moving) {
        entityData.set(GLIDING, moving); // SynchedEntityData sends only changes, never a per-tick blend value.
        glideBlend.setMoving(moving, tickCount / 20.0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GLIDING.equals(key) && glideBlend != null) glideBlend.setMoving(isGliding(), tickCount / 20.0);
    }

    @Override
    public void tick() {
        // Preserve the legacy spirit's phasing flight, using the same scope as vanilla Vex.
        setNoGravity(true);
        noPhysics = true;
        super.tick();
        noPhysics = false;
        if (level() instanceof ServerLevel server) {
            if (attackCooldown > 0) attackCooldown--;
            if (attackTicks > 0) {
                attackTicks--;
                if (attackTicks == 21 && isAlive() && !isNoAi() && strikeTarget != null
                        && strikeTarget.isAlive() && canAttack(strikeTarget)
                        && distanceToSqr(strikeTarget) <= 4 && getSensing().hasLineOfSight(strikeTarget)) {
                    super.doHurtTarget(server, strikeTarget); // One hit at the authored 0.45-second contact pose.
                }
                if (attackTicks == 0) strikeTarget = null;
            }
            if (isNoAi() || !isAlive()) {
                setGliding(false);
                setDeltaMovement(getDeltaMovement().scale(0.8));
            }
        }
    }

    private void beginStrike(LivingEntity target) {
        attackTicks = 30;
        attackCooldown = 44;
        strikeTarget = target;
        triggerAnim("attack", "swing");
        playSound(SoundEvents.VEX_CHARGE, 0.7F, 0.8F);
    }

    private void hover() {
        ((GlideMoveControl) moveControl).stop();
        navigation.stop();
        setGliding(false);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) { }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VEX_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WITHER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.BLAZE_DEATH; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // One uninterrupted clock for both loops. Changing a target never resets either animation phase.
        controllers.add(new AnimationController<SpiritEntity>("locomotion", 0,
                state -> state.setAndContinue(LOCOMOTION)));
        // The attack resource includes each bone's bind rotation: GeckoLib 5.5.2
        // subtracts it during additive blending, even though snapshots are offsets.
        controllers.add(new AnimationController<SpiritEntity>("attack", 0, state -> PlayState.STOP)
                .additiveAnimations().triggerableAnim("swing", ATTACK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private final class PursueGoal extends Goal {
        PursueGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return getTarget() != null && getTarget().isAlive() && canAttack(getTarget()); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() { hover(); setAggressive(false); }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            setAggressive(true);
            lookControl.setLookAt(target, 20, 20);
            double distance = distanceToSqr(target);
            if (attackTicks > 0 || distance <= (isGliding() ? 2.56 : 4)) {
                hover();
                if (attackTicks == 0 && attackCooldown == 0 && glideWeight(SpiritGlideBlend.BODY, 0) < 0.25
                        && getSensing().hasLineOfSight(target)) beginStrike(target);
            } else {
                moveControl.setWantedPosition(target.getX(), target.getY() + 0.2, target.getZ(), 1);
                setGliding(true);
            }
        }
    }

    private final class DriftGoal extends Goal {
        private int remaining, nextDrift = 80;
        DriftGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            return getTarget() == null && tickCount >= nextDrift && attackTicks == 0;
        }
        @Override public boolean canContinueToUse() { return getTarget() == null && remaining > 0 && moveControl.hasWanted(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void start() {
            // At most four nearby candidates; no ground scans, chunk loading or pathfinder expansion.
            remaining = 60;
            for (int i = 0; i < 4; i++) {
                double x = getX() + (random.nextDouble() - 0.5) * 6;
                double y = Mth.clamp(getY() + (random.nextDouble() - 0.5) * 0.6,
                        level().getMinY() + 2, level().getMaxY() - 3);
                double z = getZ() + (random.nextDouble() - 0.5) * 6;
                BlockPos pos = BlockPos.containing(x, y, z);
                if (level().hasChunkAt(pos) && level().isEmptyBlock(pos) && level().isEmptyBlock(pos.above())) {
                    moveControl.setWantedPosition(x, y, z, 0.45);
                    setGliding(true);
                    return;
                }
            }
            remaining = 0;
        }
        @Override public void tick() { remaining--; }
        @Override public void stop() { hover(); nextDrift = tickCount + 100 + random.nextInt(100); }
    }

    private final class GlideMoveControl extends MoveControl {
        GlideMoveControl() { super(SpiritEntity.this); }
        void stop() { operation = Operation.WAIT; }
        @Override public void tick() {
            Vec3 desired = Vec3.ZERO;
            if (operation == Operation.MOVE_TO) {
                double dx = wantedX - getX(), dy = wantedY - getY(), dz = wantedZ - getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance < 0.3) {
                    stop();
                    setGliding(false);
                } else {
                    double speed = Math.min(getAttributeValue(Attributes.MOVEMENT_SPEED) * speedModifier, distance * 0.12)
                            * glideWeight(SpiritGlideBlend.BODY, 0) / distance;
                    desired = new Vec3(dx * speed, dy * speed, dz * speed);
                    float yaw = (float) (Mth.atan2(dz, dx) * 180 / Math.PI) - 90;
                    setYRot(rotlerp(getYRot(), yaw, 6));
                    yBodyRot = getYRot();
                }
            }
            setDeltaMovement(getDeltaMovement().lerp(desired, 0.18));
        }
    }
}
