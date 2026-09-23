package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModEntityTypeTags;
import cn.teampancake.theaurorian2.common.registry.ModItemTags;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

/** Neutral pack hunter and bone-tamed companion. Natural spawning is intentionally not registered. */
public final class BlueTailWolfEntity extends TamableAnimal implements NeutralMob, GeoEntity {
    // Physical running speed also drives the distance-based animation clock.
    private static final double RUN_SPEED_MODIFIER = 2.1;
    private static final double BASE_ATTACK_DAMAGE = 7;
    private static final int BITE_COOLDOWN_TICKS = 14;
    private static final EntityDataAccessor<Long> ANGER_END = SynchedEntityData.defineId(BlueTailWolfEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> GAIT = SynchedEntityData.defineId(BlueTailWolfEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(BlueTailWolfEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("misc.sit");
    private static final RawAnimation LOOK = RawAnimation.begin().thenPlay("misc.idle_look");
    private static final RawAnimation SNIFF = RawAnimation.begin().thenPlay("misc.idle_sniff");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("attack.bite");
    private static final RawAnimation HOWL = RawAnimation.begin().thenPlay("misc.howl");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final BlueTailWolfBlend poseBlend = new BlueTailWolfBlend();
    private final BlueTailWolfStride stride = new BlueTailWolfStride();
    private final AnimalVisualState idleVisuals = new AnimalVisualState(120, 120);
    private @Nullable EntityReference<LivingEntity> angerTarget;
    private @Nullable LivingEntity actionTarget;
    private int actionTicks, actionStartedTick, biteCooldown, howlCooldown, nextPackCheck;

    public BlueTailWolfEntity(EntityType<? extends BlueTailWolfEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 35)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER_END, -1L);
        builder.define(GAIT, 0);
        builder.define(ACTION, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(1, new TamableAnimalPanicGoal(RUN_SPEED_MODIFIER, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(5, new PursueGoal());
        goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10, 2));
        goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override protected void alertOther(Mob other, LivingEntity enemy) {
                if (other instanceof TamableAnimal companion && canRecruit(companion, enemy)) other.setTarget(enemy);
            }
        }.setAlertOthers());
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false,
                (target, level) -> target.is(ModEntityTypeTags.BLUE_TAIL_WOLF_PREY)));
        targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public int action() { return entityData.get(ACTION); }
    public int actionTicks() { return actionTicks; }
    public double poseWeight(int channel, float partialTick) {
        double time = (tickCount + partialTick) / 20.0;
        if (channel < 3) return poseBlend.value(channel, time);
        return Math.max(0, 1 - poseBlend.value(0, time) - poseBlend.value(1, time) - poseBlend.value(2, time));
    }
    public float idleGestureWeight(float partialTick) { return idleVisuals.idleBlend(partialTick); }

    public double gaitAnimationTime(boolean running, float partialTick) { return stride.animationTime(running, partialTick); }

    @Override protected AABB getAttackBoundingBox(double horizontalExpansion) {
        // The muzzle extends beyond the body box. Keep vanilla vertical overlap and item reach rules.
        return super.getAttackBoundingBox(horizontalExpansion + 0.5 * getScale());
    }

    @Override protected void updateWalkAnimation(float distance) {
        super.updateWalkAnimation(distance);
        if (level().isClientSide()) {
            // Vanilla supplies actual interpolated horizontal displacement here, after collision/network movement.
            stride.advance(distance, getScale() * getAgeScale(), onGround() && !isInWater()
                    && !isInSittingPose() && action() == 0 && isAlive());
        }
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GAIT.equals(key) && poseBlend != null) poseBlend.setGait(entityData.get(GAIT), tickCount / 20.0);
    }

    @Override public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            updatePersistentAnger(server, true);
            if (biteCooldown > 0) biteCooldown--;
            if (howlCooldown > 0) howlCooldown--;
            if (action() != 0) {
                if (!isAlive() || isNoAi() || isOrderedToSit() || isPanicking() || actionTarget == null
                        || !actionTarget.isAlive() || getTarget() != actionTarget || !canAttack(actionTarget)) cancelAction();
                else {
                    if (tickCount > actionStartedTick) actionTicks++;
                    if (action() == 1 && actionTicks == 4 && isWithinMeleeAttackRange(actionTarget)
                            && getSensing().hasLineOfSight(actionTarget) && super.doHurtTarget(server, actionTarget)
                            && random.nextFloat() < 0.1F) {
                        actionTarget.addEffect(new MobEffectInstance(ModMobEffects.LACERATION, 60), this);
                    }
                    if (action() == 2 && actionTicks == 40) {
                        playSound(wolfSounds().whineSound().value(), 1.0F, 0.7F);
                        for (TamableAnimal ally : packCandidates(actionTarget)) ally.setTarget(actionTarget);
                    }
                    if (actionTicks >= (action() == 1 ? 10 : 70)) finishAction();
                }
            }
            int gait = isInSittingPose() ? 3 : action() == 0 && isAlive() && !isNoAi()
                    && getDeltaMovement().horizontalDistanceSqr() > 0.0004
                    ? (getSpeed() > 0.22F ? 2 : 1) : 0;
            entityData.set(GAIT, gait);
        } else {
            if (isPassenger() || !isAlive()) stride.advance(0, 1, false);
            idleVisuals.tick(this, action() != 0 || entityData.get(GAIT) != 0 || isAngry() || isInSittingPose());
        }
    }

    private void beginAction(int action, LivingEntity target) {
        if (action == 2) navigation.stop();
        actionTarget = target;
        actionTicks = 0;
        actionStartedTick = tickCount;
        entityData.set(ACTION, action);
        triggerAnim("action", action == 1 ? "bite" : "howl");
    }

    private void finishAction() {
        if (action() == 1) biteCooldown = BITE_COOLDOWN_TICKS;
        if (action() == 2) howlCooldown = 200;
        entityData.set(ACTION, 0);
        actionTarget = null;
        actionTicks = 0;
    }

    private void cancelAction() {
        if (action() == 0) return;
        stopTriggeredAnim("action", action() == 1 ? "bite" : "howl");
        finishAction();
    }

    private boolean sameOwner(TamableAnimal other) {
        return getOwnerReference() != null && other.getOwnerReference() != null
                && getOwnerReference().getUUID().equals(other.getOwnerReference().getUUID());
    }

    private boolean canRecruit(TamableAnimal other, LivingEntity enemy) {
        return other != this && (other instanceof BlueTailWolfEntity || other instanceof Wolf)
                && other.isAlive() && !other.isBaby() && !other.isNoAi() && !other.isOrderedToSit()
                && other.getTarget() == null && (isTame() ? other.isTame() && sameOwner(other) : !other.isTame())
                && other.canAttack(enemy) && (other.getOwner() == null || other.wantsToAttack(enemy, other.getOwner()));
    }

    private java.util.List<TamableAnimal> packCandidates(LivingEntity enemy) {
        // Only two concrete entity classes in loaded sections, at most once per second while ready to howl.
        var allies = new java.util.ArrayList<TamableAnimal>();
        var bounds = getBoundingBox().inflate(50);
        allies.addAll(level().getEntitiesOfClass(BlueTailWolfEntity.class, bounds, other -> canRecruit(other, enemy)));
        allies.addAll(level().getEntitiesOfClass(Wolf.class, bounds, other -> canRecruit(other, enemy)));
        return allies;
    }

    @Override public boolean canAttack(LivingEntity target) {
        if (target instanceof TamableAnimal pet && isTame() && sameOwner(pet)) return false;
        if (getOwnerReference() != null && getOwnerReference().getUUID().equals(target.getUUID())) return false;
        return super.canAttack(target);
    }

    @Override public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || target instanceof Ghast || target instanceof ArmorStand || !canAttack(target)) return false;
        if (target instanceof TamableAnimal pet) return !pet.isTame();
        if (target instanceof Player player && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(player)) return false;
        return !(target instanceof AbstractHorse horse) || !horse.isTamed();
    }

    @Override public boolean isFood(ItemStack stack) { return stack.is(ModItemTags.BLUE_TAIL_WOLF_FOOD); }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame() && stack.is(Items.BONE) && !isAngry()) {
            if (!level().isClientSide()) {
                stack.consume(1, player);
                if (random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
                    tame(player);
                    heal(getMaxHealth());
                    navigation.stop();
                    setTarget(null);
                    cancelAction();
                    setOrderedToSit(true);
                    level().broadcastEntityEvent(this, (byte)7);
                } else level().broadcastEntityEvent(this, (byte)6);
            }
            return InteractionResult.SUCCESS;
        }
        if (isFood(stack)) {
            if (!isTame()) return InteractionResult.PASS;
            if (getHealth() < getMaxHealth()) {
                if (!level().isClientSide()) {
                    var food = stack.get(DataComponents.FOOD);
                    usePlayerItem(player, hand, stack);
                    heal(food == null ? 2 : 2 * food.nutrition());
                    playEatingSound();
                    gameEvent(GameEvent.EAT);
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
        InteractionResult result = super.mobInteract(player, hand);
        if (!result.consumesAction() && isOwnedBy(player)) {
            if (!level().isClientSide()) {
                setOrderedToSit(!isOrderedToSit());
                navigation.stop();
                setTarget(null);
                cancelAction();
            }
            return InteractionResult.SUCCESS;
        }
        return result;
    }

    @Override public boolean canMate(Animal partner) {
        return partner != this && partner instanceof BlueTailWolfEntity wolf && isTame() && wolf.isTame()
                && !isOrderedToSit() && !wolf.isOrderedToSit() && isInLove() && wolf.isInLove();
    }

    @Override public @Nullable BlueTailWolfEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        BlueTailWolfEntity child = ModEntities.BLUE_TAIL_WOLF.get().create(level, EntitySpawnReason.BREEDING);
        if (child != null && isTame()) { child.setOwnerReference(getOwnerReference()); child.setTame(true, true); }
        return child;
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt) setOrderedToSit(false);
        return hurt;
    }

    @Override public boolean canBeLeashed() { return !isAngry() && super.canBeLeashed(); }
    private WolfSoundVariant.WolfSoundSet wolfSounds() {
        var sounds = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC);
        return isBaby() ? sounds.babySounds() : sounds.adultSounds();
    }
    @Override protected @Nullable SoundEvent getAmbientSound() { return action() == 2 ? null : (isAngry() ? wolfSounds().growlSound() : wolfSounds().ambientSound()).value(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return wolfSounds().hurtSound().value(); }
    @Override protected SoundEvent getDeathSound() { return wolfSounds().deathSound().value(); }
    @Override protected void playEatingSound() { playSound(SoundEvents.GENERIC_EAT.value(), 1, 1); }
    @Override public long getPersistentAngerEndTime() { return entityData.get(ANGER_END); }
    @Override public void setPersistentAngerEndTime(long time) { entityData.set(ANGER_END, time); }
    @Override public void startPersistentAngerTimer() { setTimeToRemainAngry(400 + random.nextInt(400)); }
    @Override public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() { return angerTarget; }
    @Override public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) { angerTarget = target; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        addPersistentAngerSaveData(output);
        output.putInt("AttackBalanceVersion", 1);
        output.putInt("BiteCooldown", biteCooldown);
        output.putInt("HowlCooldown", howlCooldown);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        readPersistentAngerSaveData(level(), input);
        // Upgrade the old saved default once, preserving custom bases and all attribute modifiers.
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (input.getIntOr("AttackBalanceVersion", 0) < 1 && attack != null && attack.getBaseValue() == 6) {
            attack.setBaseValue(BASE_ATTACK_DAMAGE);
        }
        biteCooldown = Math.clamp(input.getIntOr("BiteCooldown", 0), 0, BITE_COOLDOWN_TICKS);
        howlCooldown = Math.clamp(input.getIntOr("HowlCooldown", 0), 0, 200);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Gait clocks follow distance, including partial-tick interpolation; other actions retain their timing.
        controllers.add(new AnimationController<BlueTailWolfEntity>("idle", 0, state -> state.setAndContinue(IDLE)));
        controllers.add(new AnimationController<BlueTailWolfEntity>("walk", 0, state -> {
            state.controller().setAnimationTime(gaitAnimationTime(false, state.renderState().getPartialTick()));
            return state.setAndContinue(WALK);
        }).additiveAnimations());
        controllers.add(new AnimationController<BlueTailWolfEntity>("run", 0, state -> {
            state.controller().setAnimationTime(gaitAnimationTime(true, state.renderState().getPartialTick()));
            return state.setAndContinue(RUN);
        }).additiveAnimations());
        controllers.add(new AnimationController<BlueTailWolfEntity>("sit", 0, state -> state.setAndContinue(SIT)).additiveAnimations());
        controllers.add(new AnimationController<BlueTailWolfEntity>("gesture", 0, state -> idleVisuals.idleVariant() == 0
                ? PlayState.STOP : state.setAndContinue(idleVisuals.idleVariant() == 1 ? LOOK : SNIFF)).additiveAnimations());
        controllers.add(new AnimationController<BlueTailWolfEntity>("action", 0, state -> PlayState.STOP)
                .additiveAnimations().triggerableAnim("bite", BITE).triggerableAnim("howl", HOWL));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private final class PursueGoal extends MeleeAttackGoal {
        private final LeapAtTargetGoal leap = new LeapAtTargetGoal(BlueTailWolfEntity.this, 0.4F);
        private boolean leaping;
        private double approachGroundY;
        PursueGoal() {
            super(BlueTailWolfEntity.this, RUN_SPEED_MODIFIER, true);
            setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }
        @Override public boolean canUse() { return !isBaby() && !isOrderedToSit() && super.canUse(); }
        @Override public boolean canContinueToUse() { return !isOrderedToSit() && super.canContinueToUse(); }
        @Override public void start() { super.start(); approachGroundY = getY(); }
        @Override public void stop() { super.stop(); leaping = false; leap.stop(); cancelAction(); }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null || !canAttack(target)) { setTarget(null); return; }
            lookControl.setLookAt(target, 30, 30);
            if (onGround()) approachGroundY = getY();
            boolean inReach = isWithinMeleeAttackRange(target) && getSensing().hasLineOfSight(target);
            // Keep a terrain jump moving until landing on the higher ground. Merely reaching
            // the target's height mid-jump does not mean the step has been cleared.
            boolean climbingToTarget = target.getY() > approachGroundY + 0.01;
            if (action() == 2 && inReach) cancelAction();
            if (action() != 0) {
                if (action() == 1 && climbingToTarget) super.tick();
                else navigation.stop();
                return;
            }
            // Bite immediately on approach, including during a leap or ordinary hurt knockback.
            // Damage still checks reach and visibility at the animation's contact tick.
            if (inReach) {
                if (climbingToTarget) super.tick();
                else navigation.stop();
                if (biteCooldown == 0) beginAction(1, target);
                return;
            }
            if (leaping) {
                if (leap.canContinueToUse()) { navigation.stop(); return; }
                leaping = false;
                leap.stop();
            }
            if (howlCooldown == 0 && tickCount >= nextPackCheck && distanceToSqr(target) > 16) {
                nextPackCheck = tickCount + 20;
                if (onGround() && !packCandidates(target).isEmpty()) { beginAction(2, target); return; }
            }
            // Keep vanilla leap physics inside pursuit so jumping cannot preempt the bite scheduler.
            // Pounce only with a ready bite and enough approach distance; otherwise chase on foot.
            if (!climbingToTarget && biteCooldown == 0 && distanceToSqr(target) > 9
                    && getSensing().hasLineOfSight(target) && leap.canUse()) {
                navigation.stop();
                leap.start();
                leaping = true;
            } else super.tick();
        }
        @Override protected void checkAndPerformAttack(LivingEntity target) { /* Contact is handled once at authored tick 4. */ }
    }
}
