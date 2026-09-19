package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.world.SpiderBroodData;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractSpiderlingEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICKS =
            SynchedEntityData.defineId(AbstractSpiderlingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> AMBIENT_ACTION =
            SynchedEntityData.defineId(AbstractSpiderlingEntity.class, EntityDataSerializers.BYTE);
    private static final RawAnimation LOOK = RawAnimation.begin().thenPlay("misc.curious_look");
    private static final RawAnimation CLEAN = RawAnimation.begin().thenPlay("misc.clean_front_leg");
    private static final RawAnimation STRETCH = RawAnimation.begin().thenPlay("misc.stretch_settle");
    private int ambientCooldown = 100;
    private int ambientTicks;
    private int lastAmbientAction;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("attack.bite");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable UUID motherId;
    private Difficulty appliedAttackDamageDifficulty;

    protected AbstractSpiderlingEntity(EntityType<? extends AbstractSpiderlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    protected static AttributeSupplier.Builder createSpiderlingAttributes(
            double health, double armor, double damage, double speed) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    protected final void updateAttackDamage(
            Difficulty difficulty, double easyDamage, double normalDamage, double hardDamage) {
        if (this.appliedAttackDamageDifficulty == difficulty) {
            return;
        }

        var attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        attackDamage.setBaseValue(switch (difficulty) {
            case NORMAL -> normalDamage;
            case HARD -> hardDamage;
            case PEACEFUL, EASY -> easyDamage;
        });
        this.appliedAttackDamageDifficulty = difficulty;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.addGoal(5, new AmbientGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new MotherTargetGoal());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ATTACK_ANIMATION_TICKS, 0);
        entityData.define(AMBIENT_ACTION, (byte)0);
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level && level.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.ambientCooldown > 0) this.ambientCooldown--;
            int attackTicks = this.entityData.get(ATTACK_ANIMATION_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(ATTACK_ANIMATION_TICKS, attackTicks - 1);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            this.entityData.set(ATTACK_ANIMATION_TICKS, 10);
        }
        return hurt;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof SpiderMotherEntity)
                && !(target instanceof AbstractSpiderlingEntity)
                && !(target instanceof SpiderEggEntity)
                && super.canAttack(target);
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 speedMultiplier) {
        if (!blockState.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(blockState, speedMultiplier);
        }
    }

    public void setMother(@Nullable UUID motherId) {
        this.motherId = motherId;
        if (this.isAddedToLevel() && this.level() instanceof ServerLevel level) {
            if (motherId == null) {
                SpiderBroodData.get(level).remove(this.getUUID());
            } else {
                SpiderBroodData.get(level).add(this.getUUID(), motherId);
            }
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // Also imports offspring from saves created before the brood ledger existed.
        if (this.motherId != null && this.level() instanceof ServerLevel level) {
            SpiderBroodData.get(level).add(this.getUUID(), this.motherId);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        if (this.level() instanceof ServerLevel level
                && this.getRemovalReason() != null && this.getRemovalReason().shouldDestroy()) {
            SpiderBroodData.get(level).remove(this.getUUID());
        }
        super.onRemovedFromLevel();
    }

    public @Nullable UUID getMotherId() {
        return this.motherId;
    }

    public boolean belongsTo(SpiderMotherEntity mother) {
        return this.motherId != null && this.motherId.equals(mother.getUUID());
    }

    private @Nullable SpiderMotherEntity getMother() {
        if (this.motherId == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.getEntity(this.motherId);
        return entity instanceof SpiderMotherEntity mother && mother.isAlive() ? mother : null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.motherId != null) {
            output.putString("Mother", this.motherId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String encodedMother = input.getStringOr("Mother", "");
        if (!encodedMother.isEmpty()) {
            try {
                this.motherId = UUID.fromString(encodedMother);
            } catch (IllegalArgumentException ignored) {
                this.motherId = null;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AbstractSpiderlingEntity>("movement", 2, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(IDLE);
            }
            if (this.entityData.get(ATTACK_ANIMATION_TICKS) > 0) {
                return state.setAndContinue(BITE);
            }
            if (this.entityData.get(AMBIENT_ACTION) != 0 && !state.isMoving()) {
                return state.setAndContinue(this.ambientAnimation());
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    protected final RawAnimation ambientAnimation() {
        if (!this.isAlive() || this.onClimbable()) return IDLE;
        return switch (this.entityData.get(AMBIENT_ACTION)) {
            case 1 -> LOOK;
            case 2 -> CLEAN;
            case 3 -> STRETCH;
            default -> IDLE;
        };
    }

    protected double ambientDurationScale() { return 1.0; }

    private boolean canPerformAmbient() {
        LivingEntity target = this.getTarget();
        return this.isAlive() && (target == null || !target.isAlive())
                && this.hurtTime == 0 && this.entityData.get(ATTACK_ANIMATION_TICKS) == 0
                && this.onGround() && !this.onClimbable() && !this.horizontalCollision
                && !this.isInWater() && !this.isInLava() && this.getNavigation().isDone()
                && this.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
    }

    private final class AmbientGoal extends Goal {
        private AmbientGoal() { this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            return ambientCooldown == 0 && canPerformAmbient();
        }
        @Override public boolean canContinueToUse() {
            return ambientTicks > 0 && canPerformAmbient();
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void start() {
            int choice = random.nextInt(lastAmbientAction == 0 ? 3 : 2) + 1;
            if (lastAmbientAction != 0 && choice >= lastAmbientAction) choice++;
            lastAmbientAction = choice;
            ambientTicks = (int)Math.ceil((choice == 1 ? 88 : choice == 2 ? 96 : 92) * ambientDurationScale());
            getNavigation().stop();
            entityData.set(AMBIENT_ACTION, (byte)choice);
        }
        @Override public void tick() { ambientTicks--; }
        @Override public void stop() {
            entityData.set(AMBIENT_ACTION, (byte)0);
            ambientTicks = 0;
            ambientCooldown = 120 + random.nextInt(161);
        }
    }

    private final class MotherTargetGoal extends TargetGoal {

        private MotherTargetGoal() {
            super(AbstractSpiderlingEntity.this, false);
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            SpiderMotherEntity mother = AbstractSpiderlingEntity.this.getMother();
            LivingEntity target = mother == null ? null : mother.getTarget();
            if (target == null || !target.isAlive() || !AbstractSpiderlingEntity.this.canAttack(target)) {
                return false;
            }
            this.targetMob = target;
            return true;
        }

        @Override
        public void start() {
            super.start();
            AbstractSpiderlingEntity.this.setTarget(this.targetMob);
        }

        @Override
        public boolean canContinueToUse() {
            SpiderMotherEntity mother = AbstractSpiderlingEntity.this.getMother();
            return mother != null
                    && this.targetMob != null
                    && this.targetMob.isAlive()
                    && mother.getTarget() == this.targetMob
                    && AbstractSpiderlingEntity.this.canAttack(this.targetMob);
        }
    }
}
