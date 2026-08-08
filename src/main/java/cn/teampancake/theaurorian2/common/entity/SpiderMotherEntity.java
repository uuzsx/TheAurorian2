package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlockTags;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SpiderMotherEntity extends Monster implements GeoEntity {

    public static final int MAX_SPIDERLINGS = 6;
    private static final int EGGS_PER_HATCH = 3;
    private static final int REGULAR_HATCH_COOLDOWN = 18 * 20;
    private static final int EIDOLON_COOLDOWN = 12 * 20;
    private static final int LOOSE_TRAP_BREAK_TICKS = 10;
    private static final int HARD_TRAP_ESCAPE_TICKS = 3 * 20;
    private static final int ESCAPE_SEARCH_RADIUS = 6;
    private static final double HIGH_GROUND_HEIGHT = 4.5;
    private static final double HIGH_GROUND_RANGE = 24.0;
    private static final int SILK_TRIGGER_TICKS = 2 * 20;
    private static final int WALL_REINFORCEMENT_TICKS = 6 * 20;
    private static final int NEST_QUAKE_TICKS = 10 * 20;
    private static final int SILK_BIND_TICKS = 2 * 20;
    private static final int VENOM_POOL_DURATION_TICKS = 6 * 20;
    private static final double VENOM_POOL_RADIUS = 3.0;
    private static final int MAX_VENOM_POOLS = 3;
    private static final int RAGE_DAMAGE_WINDOW_TICKS = 3 * 20;
    private static final float RAGE_DAMAGE_THRESHOLD = 60.0F;
    private static final int RAGE_WINDUP_TICKS = 20;
    private static final int RAGE_DURATION_TICKS = 10 * 20;
    private static final int HALF_HEALTH_RAGE_DURATION_TICKS = 60 * 20;
    private static final int RAGE_COOLDOWN_TICKS = 20 * 20;
    private static final int RAGE_FATIGUE_TICKS = 2 * 20;
    private static final double NORMAL_MOVEMENT_SPEED = 0.45;
    private static final double RAGE_MOVEMENT_SPEED = 0.52;
    private static final double FATIGUE_MOVEMENT_SPEED = 0.30;
    private static final float RAGE_DAMAGE_MULTIPLIER = 1.2F;
    private static final float RAGE_DAMAGE_TAKEN_MULTIPLIER = 0.8F;
    private static final float RAGE_COOLDOWN_MULTIPLIER = 0.65F;
    private static final int OUT_OF_COMBAT_HEAL_DELAY_TICKS = 8 * 20;
    private static final int OUT_OF_COMBAT_HEAL_INTERVAL_TICKS = 20;
    private static final float OUT_OF_COMBAT_HEAL_PERCENT = 0.05F;
    private static final double HUNTING_CHARGE_MIN_RANGE = 6.0;
    private static final double HUNTING_CHARGE_MAX_RANGE = 12.0;
    private static final int HUNTING_CHARGE_DURATION_TICKS = 20;
    private static final int HUNTING_CHARGE_COOLDOWN_TICKS = 8 * 20;
    private static final double HUNTING_CHARGE_MOVEMENT_SPEED = 0.90;
    private static final DustParticleOptions RAGE_PARTICLE = new DustParticleOptions(0x58F5B4, 1.5F);
    private static final EntityDataAccessor<Byte> ACTION =
            SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> RAGE_PHASE =
            SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.BYTE);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation SLASH = RawAnimation.begin().thenPlay("attack.slash");
    private static final RawAnimation SMASH = RawAnimation.begin().thenPlay("attack.smash");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("attack.spit");
    private static final RawAnimation HATCH_BEGIN = RawAnimation.begin().thenPlay("misc.hatch_begin");
    private static final RawAnimation HATCH_HOLD = RawAnimation.begin().thenLoop("misc.hatch_hold");
    private static final RawAnimation HATCH_END = RawAnimation.begin().thenPlay("misc.hatch_end");
    private static final RawAnimation EIDOLON = RawAnimation.begin().thenPlay("attack.eidolon");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("misc.death");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private ServerBossEvent bossEvent;
    private final Set<UUID> smashBeamHits = new HashSet<>();
    private final Map<UUID, Integer> silkBindings = new HashMap<>();
    private final Deque<VenomPool> venomPools = new ArrayDeque<>();
    private final Deque<RecentDamage> recentDamage = new ArrayDeque<>();
    private int actionTicks;
    private int combatCooldown;
    private int hatchCooldown = 8 * 20;
    private int eidolonCooldown = EIDOLON_COOLDOWN;
    private int looseTrapTicks;
    private int hardTrapTicks;
    private int highGroundTicks;
    private int blockedApproachTicks;
    private int approachSampleTicks;
    private int silkTimeout;
    private int elevatedAggressorTicks;
    private int rageTicks;
    private int rageCooldown;
    private int outOfCombatTicks;
    private int huntingChargeCooldown;
    private int huntingChargeTicks;
    private double lastApproachDistance = Double.MAX_VALUE;
    private @Nullable UUID trackedHighTarget;
    private @Nullable UUID elevatedAggressor;
    private @Nullable Vec3 nestQuakePosition;
    private boolean silkAttempted;
    private boolean silkInFlight;
    private boolean pendingVenomPool;
    private boolean wallReinforcementSent;
    private boolean nestQuakeSent;
    private boolean forcedHatchTriggered;
    private SpitMode spitMode = SpitMode.NORMAL;
    private SmashMode smashMode = SmashMode.NORMAL;

    public SpiderMotherEntity(EntityType<? extends SpiderMotherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
        this.setPersistenceRequired();
        this.bossEvent = this.createBossEvent();
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        this.setPathfindingMalus(PathType.FIRE, 0.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, NORMAL_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SpiderMotherCombatGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(
                this, LivingEntity.class, true, (target, level) -> this.isCombatTarget(target)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ACTION, Action.IDLE.id);
        entityData.define(RAGE_PHASE, RagePhase.NORMAL.id);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setName(this.getDisplayName());
        this.tickTrapEscape(level);
        this.tickSilkBindings(level);
        this.tickVenomPools(level);
        this.tickRage(level);
        this.triggerHalfHealthPhase();
        this.tickOutOfCombatHealing(level);
        if (this.elevatedAggressorTicks > 0) {
            this.elevatedAggressorTicks--;
        } else {
            this.elevatedAggressor = null;
        }
        if (this.silkTimeout > 0 && --this.silkTimeout == 0 && this.silkInFlight) {
            this.onSilkResolved(false);
        }
        if (this.tickCount % 20 == 0) {
            this.preferReachableGroundTarget(level);
        }
        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.hatchCooldown > 0) {
            this.hatchCooldown--;
        }
        if (this.eidolonCooldown > 0) {
            this.eidolonCooldown--;
        }
        if (this.huntingChargeCooldown > 0) {
            this.huntingChargeCooldown--;
        }
        if (this.huntingChargeTicks > 0 && --this.huntingChargeTicks == 0) {
            this.finishHuntingChargeWithSmash();
        }
        if (this.getAction() != Action.IDLE) {
            this.tickAction(level);
        }
    }

    private void tickAction(ServerLevel level) {
        this.actionTicks++;
        switch (this.getAction()) {
            case SLASH -> {
                if (this.actionTicks == 8) {
                    this.performSlash(level);
                }
                if (this.actionTicks >= 20) {
                    this.finishRegularAttack(10);
                }
            }
            case SMASH -> {
                if (this.smashMode == SmashMode.NEST_QUAKE) {
                    this.tickNestQuake(level);
                    if (this.actionTicks >= 20) {
                        this.performNestQuake(level);
                        this.finishAction(55);
                    }
                } else {
                    if (this.actionTicks == 10) {
                        this.performSmash(level);
                    } else if (this.actionTicks == 12) {
                        this.performSmashBeam(level, -1.6);
                    } else if (this.actionTicks == 17) {
                        this.performSmashBeam(level, 1.6);
                    }
                    if (this.actionTicks >= 20) {
                        this.finishRegularAttack(10);
                    }
                }
            }
            case SPIT -> {
                if (this.actionTicks == 8) {
                    LivingEntity target = this.getTarget();
                    if (target != null && target.isAlive()) {
                        switch (this.spitMode) {
                            case NORMAL -> SpiderVenomProjectileEntity.shootWebbing(level, this, target, 6.0F);
                            case SILK -> SpiderSilkProjectileEntity.shoot(level, this, target);
                            case VENOM_POOL -> {
                                SpiderVenomProjectileEntity.shootPool(level, this, target);
                                this.pendingVenomPool = false;
                            }
                        }
                    } else if (this.spitMode == SpitMode.SILK) {
                        this.onSilkResolved(false);
                    }
                }
                if (this.actionTicks >= 14) {
                    if (this.spitMode == SpitMode.NORMAL) {
                        this.finishRegularAttack(16);
                    } else {
                        this.finishAction(32);
                    }
                }
            }
            case HATCH_BEGIN -> {
                if (this.actionTicks >= 6) {
                    this.setAction(Action.HATCH_HOLD);
                }
            }
            case HATCH_HOLD -> {
                if (this.actionTicks >= 30) {
                    this.spawnEggs(level);
                    this.setAction(Action.HATCH_END);
                }
            }
            case HATCH_END -> {
                if (this.actionTicks >= 17) {
                    this.hatchCooldown = REGULAR_HATCH_COOLDOWN;
                    this.finishAction(30);
                }
            }
            case EIDOLON -> {
                if (this.actionTicks == 12) {
                    this.performEidolonPulse(level);
                }
                if (this.actionTicks >= 30) {
                    this.eidolonCooldown = EIDOLON_COOLDOWN;
                    this.finishAction(35);
                }
            }
            case RAGE_ROAR -> {
                this.setDeltaMovement(Vec3.ZERO);
                if (this.actionTicks % 2 == 0) {
                    level.sendParticles(
                            RAGE_PARTICLE,
                            this.getX(), this.getY() + 2.2, this.getZ(),
                            8, 1.2, 0.8, 1.2, 0.03);
                }
                if (this.actionTicks >= RAGE_WINDUP_TICKS) {
                    this.beginRage();
                }
            }
            case IDLE -> {
            }
        }
    }

    private void startHuntingCharge(LivingEntity target) {
        this.huntingChargeCooldown = HUNTING_CHARGE_COOLDOWN_TICKS;
        this.huntingChargeTicks = HUNTING_CHARGE_DURATION_TICKS;
        this.updateMovementSpeed(HUNTING_CHARGE_MOVEMENT_SPEED);
        this.getNavigation().moveTo(target, 1.0);
    }

    private boolean isHuntingChargeActive() {
        return this.huntingChargeTicks > 0;
    }

    private void stopHuntingCharge() {
        if (this.huntingChargeTicks > 0) {
            this.huntingChargeTicks = 0;
            this.updateMovementSpeedForState();
        }
    }

    private void finishHuntingChargeWithSmash() {
        this.huntingChargeTicks = 0;
        this.updateMovementSpeedForState();
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.isCombatTarget(target)) {
            this.getLookControl().setLookAt(target, 35.0F, 30.0F);
            this.startAction(Action.SMASH);
        }
    }

    private void updateMovementSpeedForState() {
        double speed = switch (this.getRagePhase()) {
            case ENRAGED -> RAGE_MOVEMENT_SPEED;
            case FATIGUED -> FATIGUE_MOVEMENT_SPEED;
            case NORMAL, WINDUP -> NORMAL_MOVEMENT_SPEED;
        };
        this.updateMovementSpeed(speed);
    }

    private void tickRage(ServerLevel level) {
        this.pruneRecentDamage();
        if (this.rageCooldown > 0) {
            this.rageCooldown--;
        }

        RagePhase phase = this.getRagePhase();
        if (phase == RagePhase.ENRAGED) {
            if (this.tickCount % 5 == 0) {
                level.sendParticles(
                        RAGE_PARTICLE,
                        this.getX(), this.getY() + 1.5, this.getZ(),
                        5, 1.0, 0.6, 1.0, 0.02);
            }
            if (--this.rageTicks <= 0) {
                this.setRagePhase(RagePhase.FATIGUED);
                this.rageTicks = RAGE_FATIGUE_TICKS;
                this.rageCooldown = RAGE_COOLDOWN_TICKS;
                this.combatCooldown = Math.max(this.combatCooldown, RAGE_FATIGUE_TICKS);
                this.updateMovementSpeed(FATIGUE_MOVEMENT_SPEED);
                this.getNavigation().stop();
            }
        } else if (phase == RagePhase.FATIGUED && --this.rageTicks <= 0) {
            this.setRagePhase(RagePhase.NORMAL);
            this.updateMovementSpeed(NORMAL_MOVEMENT_SPEED);
        }
    }

    private void tickOutOfCombatHealing(ServerLevel level) {
        LivingEntity target = this.getTarget();
        boolean hasLiveTarget = target != null && target.isAlive() && this.isCombatTarget(target);
        if (this.getRagePhase() != RagePhase.NORMAL || hasLiveTarget) {
            this.outOfCombatTicks = 0;
            return;
        }

        this.outOfCombatTicks++;
        if (this.outOfCombatTicks < OUT_OF_COMBAT_HEAL_DELAY_TICKS
                || (this.outOfCombatTicks - OUT_OF_COMBAT_HEAL_DELAY_TICKS)
                        % OUT_OF_COMBAT_HEAL_INTERVAL_TICKS != 0
                || this.getHealth() >= this.getMaxHealth()) {
            return;
        }

        this.heal(this.getMaxHealth() * OUT_OF_COMBAT_HEAL_PERCENT);
        level.sendParticles(
                RAGE_PARTICLE,
                this.getX(), this.getY() + 1.5, this.getZ(),
                8, 1.1, 0.7, 1.1, 0.02);
    }

    private void recordRecentDamage(float damage, @Nullable Entity attacker) {
        UUID attackerId = attacker instanceof LivingEntity living && this.isCombatTarget(living)
                ? attacker.getUUID()
                : null;
        this.recentDamage.addLast(new RecentDamage(this.tickCount, damage, attackerId));
        this.pruneRecentDamage();
        if (this.getRagePhase() != RagePhase.NORMAL || this.rageCooldown > 0 || !this.isAlive()) {
            return;
        }

        float totalDamage = 0.0F;
        Map<UUID, Float> damageByAttacker = new HashMap<>();
        for (RecentDamage recent : this.recentDamage) {
            totalDamage += recent.damage;
            if (recent.attackerId != null) {
                damageByAttacker.merge(recent.attackerId, recent.damage, Float::sum);
            }
        }
        if (totalDamage < RAGE_DAMAGE_THRESHOLD) {
            return;
        }

        UUID priorityTarget = null;
        float priorityDamage = 0.0F;
        for (Map.Entry<UUID, Float> entry : damageByAttacker.entrySet()) {
            if (entry.getValue() > priorityDamage) {
                priorityTarget = entry.getKey();
                priorityDamage = entry.getValue();
            }
        }
        if (priorityTarget != null && this.level() instanceof ServerLevel level) {
            Entity entity = level.getEntity(priorityTarget);
            if (entity instanceof LivingEntity living && living.isAlive() && this.isCombatTarget(living)) {
                this.setTarget(living);
            }
        }
        this.startRageWindup();
    }

    private void pruneRecentDamage() {
        int earliestTick = this.tickCount - RAGE_DAMAGE_WINDOW_TICKS;
        while (!this.recentDamage.isEmpty() && this.recentDamage.peekFirst().tick < earliestTick) {
            this.recentDamage.removeFirst();
        }
    }

    private void startRageWindup() {
        this.recentDamage.clear();
        this.setRagePhase(RagePhase.WINDUP);
        this.spitMode = SpitMode.NORMAL;
        this.smashMode = SmashMode.NORMAL;
        this.nestQuakePosition = null;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.6F, 0.65F);
        this.startAction(Action.RAGE_ROAR);
    }

    private void beginRage() {
        this.setRagePhase(RagePhase.ENRAGED);
        this.rageTicks = RAGE_DURATION_TICKS;
        this.updateMovementSpeed(RAGE_MOVEMENT_SPEED);
        this.setAction(Action.IDLE);
        this.combatCooldown = 0;
    }

    private void triggerHalfHealthPhase() {
        if (this.forcedHatchTriggered || this.getHealth() > this.getMaxHealth() * 0.5F) {
            return;
        }

        this.forcedHatchTriggered = true;
        this.recentDamage.clear();
        this.rageCooldown = 0;
        this.rageTicks = HALF_HEALTH_RAGE_DURATION_TICKS;
        this.setRagePhase(RagePhase.ENRAGED);
        this.updateMovementSpeed(RAGE_MOVEMENT_SPEED);
        this.combatCooldown = 0;
        this.spitMode = SpitMode.NORMAL;
        this.smashMode = SmashMode.NORMAL;
        this.nestQuakePosition = null;
        this.startAction(Action.HATCH_BEGIN);
    }

    private void updateMovementSpeed(double speed) {
        AttributeInstance movementSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(speed);
        }
    }

    private void tickTrapEscape(ServerLevel level) {
        AABB body = this.getBoundingBox().deflate(0.05);
        boolean hasLooseTrap = false;
        int minX = Mth.floor(body.minX + 1.0E-4);
        int minY = Mth.floor(body.minY + 1.0E-4);
        int minZ = Mth.floor(body.minZ + 1.0E-4);
        int maxX = Mth.floor(body.maxX - 1.0E-4);
        int maxY = Mth.floor(body.maxY - 1.0E-4);
        int maxZ = Mth.floor(body.maxZ - 1.0E-4);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(ModBlockTags.SPIDER_MOTHER_BREAKABLE_TRAPS)) {
                        hasLooseTrap = true;
                    }
                }
            }
        }

        this.looseTrapTicks = hasLooseTrap ? this.looseTrapTicks + 1 : 0;
        if (this.looseTrapTicks >= LOOSE_TRAP_BREAK_TICKS
                && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            boolean brokeTrap = false;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        cursor.set(x, y, z);
                        if (level.getBlockState(cursor).is(ModBlockTags.SPIDER_MOTHER_BREAKABLE_TRAPS)) {
                            brokeTrap |= level.destroyBlock(cursor.immutable(), true, this, 64);
                        }
                    }
                }
            }
            this.looseTrapTicks = 0;
            if (brokeTrap) {
                level.sendParticles(
                        ParticleTypes.POOF,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        18, 1.4, 0.8, 1.4, 0.04);
            }
        }

        if (level.noBlockCollision(this, body)) {
            this.hardTrapTicks = 0;
            return;
        }
        if (++this.hardTrapTicks >= HARD_TRAP_ESCAPE_TICKS) {
            this.escapeHardTrap(level);
            this.hardTrapTicks = 0;
        }
    }

    private void escapeHardTrap(ServerLevel level) {
        Vec3 oldPosition = this.position();
        Vec3 escapePosition = this.findEscapePosition(level);
        if (escapePosition == null) {
            return;
        }
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.teleportTo(escapePosition.x, escapePosition.y, escapePosition.z);
        level.sendParticles(
                ParticleTypes.POOF,
                oldPosition.x, oldPosition.y + 1.0, oldPosition.z,
                20, 1.2, 0.8, 1.2, 0.05);
        level.sendParticles(
                ParticleTypes.PORTAL,
                escapePosition.x, escapePosition.y + 1.0, escapePosition.z,
                24, 1.2, 0.8, 1.2, 0.08);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.7F);
    }

    private @Nullable Vec3 findEscapePosition(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        int[] verticalOffsets = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6};
        for (int radius = 1; radius <= ESCAPE_SEARCH_RADIUS; radius++) {
            for (int dy : verticalOffsets) {
                if (Math.abs(dy) > radius) {
                    continue;
                }
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.max(Math.abs(dx), Math.abs(dz)), Math.abs(dy)) != radius) {
                            continue;
                        }
                        BlockPos candidate = origin.offset(dx, dy, dz);
                        if (!level.hasChunkAt(candidate)) {
                            continue;
                        }
                        Vec3 position = new Vec3(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
                        if (this.isSafeEscapePosition(level, position)) {
                            return position;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeEscapePosition(ServerLevel level, Vec3 position) {
        AABB candidateBox = this.getBoundingBox()
                .move(position.x - this.getX(), position.y - this.getY(), position.z - this.getZ())
                .deflate(0.05);
        if (!level.noCollision(this, candidateBox)) {
            return false;
        }
        BlockPos feet = BlockPos.containing(position.x, position.y, position.z);
        BlockPos floor = BlockPos.containing(position.x, position.y - 0.05, position.z);
        BlockState floorState = level.getBlockState(floor);
        return !floorState.getCollisionShape(level, floor).isEmpty() || !level.getFluidState(feet).isEmpty();
    }

    public void bindWithSilk(LivingEntity target) {
        if (!this.isCombatTarget(target)) {
            return;
        }
        this.silkBindings.put(target.getUUID(), SILK_BIND_TICKS);
        Vec3 pull = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        if (pull.lengthSqr() > 0.001) {
            pull = pull.normalize().scale(0.75);
        }
        target.setDeltaMovement(pull.x, Math.min(-0.35, target.getDeltaMovement().y), pull.z);
        target.push(Vec3.ZERO);
    }

    public void onSilkResolved(boolean hitTarget) {
        if (!this.silkInFlight) {
            return;
        }
        this.silkInFlight = false;
        this.silkTimeout = 0;
        if (!hitTarget) {
            this.pendingVenomPool = true;
        }
    }

    public void addVenomPool(Vec3 position) {
        while (this.venomPools.size() >= MAX_VENOM_POOLS) {
            this.venomPools.removeFirst();
        }
        this.venomPools.addLast(new VenomPool(position, VENOM_POOL_DURATION_TICKS));
    }

    private void tickSilkBindings(ServerLevel level) {
        Iterator<Map.Entry<UUID, Integer>> iterator = this.silkBindings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity target) || !target.isAlive() || !this.isCombatTarget(target)) {
                iterator.remove();
                continue;
            }

            Vec3 movement = target.getDeltaMovement();
            Vec3 pull = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            if (pull.lengthSqr() > 0.001) {
                pull = pull.normalize().scale(0.09);
            }
            target.setDeltaMovement(
                    movement.x * 0.35 + pull.x,
                    Math.min(movement.y, target.onGround() ? 0.0 : -0.08),
                    movement.z * 0.35 + pull.z);
            target.push(Vec3.ZERO);
            if (this.tickCount % 4 == 0) {
                Vec3 line = this.getEyePosition().subtract(target.getEyePosition());
                for (int i = 1; i <= 4; i++) {
                    Vec3 point = target.getEyePosition().add(line.scale(i / 5.0));
                    level.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
                }
            }

            int remainingTicks = entry.getValue() - 1;
            if (remainingTicks <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remainingTicks);
            }
        }
    }

    private void tickVenomPools(ServerLevel level) {
        Iterator<VenomPool> iterator = this.venomPools.iterator();
        while (iterator.hasNext()) {
            VenomPool pool = iterator.next();
            pool.remainingTicks--;
            if (pool.remainingTicks <= 0) {
                iterator.remove();
                continue;
            }
            if (pool.remainingTicks % 5 == 0) {
                level.sendParticles(
                        ParticleTypes.WITCH,
                        pool.center.x, pool.center.y + 0.15, pool.center.z,
                        10, 1.8, 0.08, 1.8, 0.02);
            }
            if (pool.remainingTicks % 20 != 0) {
                continue;
            }

            AABB area = new AABB(
                    pool.center.x - VENOM_POOL_RADIUS,
                    pool.center.y - 1.0,
                    pool.center.z - VENOM_POOL_RADIUS,
                    pool.center.x + VENOM_POOL_RADIUS,
                    pool.center.y + 2.0,
                    pool.center.z + VENOM_POOL_RADIUS);
            for (LivingEntity target : this.combatTargets(level, area)) {
                double dx = target.getX() - pool.center.x;
                double dz = target.getZ() - pool.center.z;
                if (dx * dx + dz * dz <= VENOM_POOL_RADIUS * VENOM_POOL_RADIUS
                        && target.hurtServer(level, this.damageSources().mobAttack(this), 2.0F)) {
                    target.addEffect(new MobEffectInstance(ModMobEffects.EIDOLON_POISON, 3 * 20), this);
                }
            }
        }
    }

    private void tickHighGroundState(ServerLevel level, LivingEntity target) {
        if (!this.isHighGroundTarget(target)) {
            this.resetHighGroundState();
            return;
        }
        if (!target.getUUID().equals(this.trackedHighTarget)) {
            this.resetHighGroundState();
            this.trackedHighTarget = target.getUUID();
            this.lastApproachDistance = this.horizontalDistanceTo(target);
        }

        this.highGroundTicks++;
        if (++this.approachSampleTicks >= 10) {
            double distance = this.horizontalDistanceTo(target);
            PathNavigation navigation = this.getNavigation();
            boolean madeProgress = distance < this.lastApproachDistance - 0.35;
            if (madeProgress && !navigation.isStuck()) {
                this.blockedApproachTicks = 0;
            } else {
                this.blockedApproachTicks += this.approachSampleTicks;
            }
            this.lastApproachDistance = distance;
            this.approachSampleTicks = 0;
        }

        if (!this.wallReinforcementSent
                && this.highGroundTicks >= WALL_REINFORCEMENT_TICKS
                && this.highGroundTicks % 20 == 0) {
            this.wallReinforcementSent = this.spawnWallReinforcement(level, target);
        }
    }

    private void resetHighGroundState() {
        this.trackedHighTarget = null;
        this.highGroundTicks = 0;
        this.blockedApproachTicks = 0;
        this.approachSampleTicks = 0;
        this.lastApproachDistance = Double.MAX_VALUE;
        this.silkAttempted = false;
        this.wallReinforcementSent = false;
        this.nestQuakeSent = false;
    }

    private boolean isHighGroundTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return this.isCombatTarget(target)
                && target.getY() - this.getY() >= HIGH_GROUND_HEIGHT
                && dx * dx + dz * dz <= HIGH_GROUND_RANGE * HIGH_GROUND_RANGE;
    }

    private double horizontalDistanceTo(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private boolean spawnWallReinforcement(ServerLevel level, LivingEntity target) {
        if (this.countOwnedUnits(level) >= MAX_SPIDERLINGS) {
            return false;
        }
        WallClimberSpiderlingEntity spiderling =
                ModEntities.SPIDERLING_WALL_CLIMBER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (spiderling == null) {
            return false;
        }
        spiderling.setMother(this.getUUID());
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        spiderling.snapTo(
                target.getX() + Math.cos(angle) * 2.0,
                target.getY(),
                target.getZ() + Math.sin(angle) * 2.0,
                (float)Math.toDegrees(angle),
                0.0F);
        level.addFreshEntity(spiderling);
        level.sendParticles(
                ParticleTypes.POOF,
                spiderling.getX(), spiderling.getY(), spiderling.getZ(),
                14, 0.35, 0.25, 0.35, 0.04);
        return true;
    }

    private void preferReachableGroundTarget(ServerLevel level) {
        LivingEntity currentTarget = this.getTarget();
        if (!(currentTarget instanceof Player)
                || !this.isHighGroundTarget(currentTarget)
                || this.isActiveElevatedAggressor(currentTarget)) {
            return;
        }

        List<Player> candidates = level.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE)),
                player -> !player.isCreative()
                        && !player.isSpectator()
                        && player.isAlive()
                        && player.getY() - this.getY() < HIGH_GROUND_HEIGHT);
        candidates.sort((left, right) -> Double.compare(this.distanceToSqr(left), this.distanceToSqr(right)));
        for (int i = 0; i < Math.min(4, candidates.size()); i++) {
            Player candidate = candidates.get(i);
            Path path = this.getNavigation().createPath(candidate, 1);
            if (path != null && path.canReach()) {
                this.setTarget(candidate);
                this.resetHighGroundState();
                this.getNavigation().moveTo(path, 0.9);
                return;
            }
        }
    }

    private boolean isActiveElevatedAggressor(LivingEntity target) {
        return this.elevatedAggressorTicks > 0
                && this.elevatedAggressor != null
                && this.elevatedAggressor.equals(target.getUUID());
    }

    private void tickNestQuake(ServerLevel level) {
        if (this.nestQuakePosition == null || this.actionTicks % 2 != 0) {
            return;
        }
        double height = 0.5 + this.actionTicks * 0.25;
        level.sendParticles(
                ParticleTypes.END_ROD,
                this.nestQuakePosition.x,
                this.nestQuakePosition.y + height,
                this.nestQuakePosition.z,
                5, 0.12, 0.45, 0.12, 0.01);
    }

    private void performNestQuake(ServerLevel level) {
        if (this.nestQuakePosition == null) {
            return;
        }
        Vec3 center = this.nestQuakePosition;
        AABB hitArea = new AABB(
                center.x - 2.5, center.y - 1.0, center.z - 2.5,
                center.x + 2.5, center.y + 6.0, center.z + 2.5);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;
            if (dx * dx + dz * dz > 2.5 * 2.5) {
                continue;
            }
            if (target.hurtServer(level, this.damageSources().mobAttack(this), this.rageDamage(10.0F))) {
                Vec3 knockback = new Vec3(dx, 0.0, dz);
                if (knockback.lengthSqr() < 0.01) {
                    knockback = this.horizontalLook();
                } else {
                    knockback = knockback.normalize();
                }
                target.push(knockback.x * 1.2, 0.45, knockback.z * 1.2);
            }
        }
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                center.x, center.y + 0.2, center.z,
                6, 1.3, 0.2, 1.3, 0.0);
        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y + 3.0, center.z,
                42, 0.35, 2.6, 0.35, 0.03);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.3F, 0.6F);
    }

    private void performSlash(ServerLevel level) {
        Vec3 forward = this.horizontalLook();
        AABB hitArea = this.getBoundingBox()
                .expandTowards(forward.scale(4.5))
                .inflate(1.25, 0.75, 1.25);
        float damage = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 13.0F;
            case NORMAL -> 16.0F;
            case HARD -> 19.0F;
        };
        damage = this.rageDamage(damage);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            Vec3 toTarget = target.getBoundingBox().getCenter()
                    .subtract(this.getBoundingBox().getCenter())
                    .multiply(1.0, 0.0, 1.0);
            if (toTarget.lengthSqr() <= 2.5 * 2.5 || toTarget.normalize().dot(forward) >= 0.0) {
                target.hurtServer(level, this.damageSources().mobAttack(this), damage);
            }
        }
    }

    private void performSmash(ServerLevel level) {
        Vec3 forward = this.horizontalLook();
        Vec3 center = this.position().add(forward.scale(2.6));
        AABB hitArea = this.getBoundingBox()
                .expandTowards(forward.scale(4.8))
                .inflate(1.75, 1.0, 1.75);
        float damage = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 17.0F;
            case NORMAL -> 21.0F;
            case HARD -> 25.0F;
        };
        damage = this.rageDamage(damage);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            target.hurtServer(level, this.damageSources().mobAttack(this), damage);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.2, center.z, 4, 1.0, 0.2, 1.0, 0.0);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.75F);
    }

    private void performSmashBeam(ServerLevel level, double sideOffset) {
        Vec3 forward = this.horizontalLook();
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 center = this.position().add(forward.scale(2.6)).add(side.scale(sideOffset));
        AABB hitArea = new AABB(center, center).inflate(1.25, 3.75, 1.25).move(0.0, 3.0, 0.0);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            if (this.smashBeamHits.add(target.getUUID())) {
                target.hurtServer(level, this.damageSources().mobAttack(this), this.rageDamage(15.0F));
            }
        }
    }

    private void performEidolonPulse(ServerLevel level) {
        AABB pulseArea = this.getBoundingBox().inflate(7.0);
        for (LivingEntity target : this.combatTargets(level, pulseArea)) {
            if (target.hurtServer(level, this.damageSources().mobAttack(this), 8.0F)) {
                target.addEffect(new MobEffectInstance(ModMobEffects.EIDOLON_POISON, 8 * 20), this);
            }
        }
        for (AbstractSpiderlingEntity spiderling : level.getEntitiesOfClass(
                AbstractSpiderlingEntity.class,
                this.getBoundingBox().inflate(12.0),
                candidate -> candidate.belongsTo(this))) {
            spiderling.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 12 * 20, 0), this);
            spiderling.addEffect(new MobEffectInstance(MobEffects.SPEED, 12 * 20, 0), this);
        }
        level.sendParticles(
                ParticleTypes.ENCHANT,
                this.getX(), this.getY() + 3.0, this.getZ(),
                48, 3.5, 1.0, 3.5, 0.08);
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.1F, 0.75F);
    }

    private void spawnEggs(ServerLevel level) {
        int room = MAX_SPIDERLINGS - this.countOwnedUnits(level);
        int eggCount = Math.min(EGGS_PER_HATCH, Math.max(0, room));
        double baseAngle = this.random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < eggCount; i++) {
            SpiderEggEntity egg = ModEntities.SPIDER_EGG.get().create(level, EntitySpawnReason.TRIGGERED);
            if (egg == null) {
                continue;
            }
            double angle = baseAngle + i * Math.PI * 2.0 / Math.max(1, eggCount);
            egg.setMother(this.getUUID());
            egg.snapTo(
                    this.getX() + Math.cos(angle) * 2.8,
                    this.getY() + 0.2,
                    this.getZ() + Math.sin(angle) * 2.8,
                    (float)Math.toDegrees(angle),
                    0.0F);
            level.addFreshEntity(egg);
        }
        level.sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + 1.0, this.getZ(),
                24, 2.0, 0.7, 2.0, 0.04);
    }

    private int countOwnedUnits(ServerLevel level) {
        AABB searchArea = this.getBoundingBox().inflate(32.0);
        int spiderlings = level.getEntitiesOfClass(
                AbstractSpiderlingEntity.class,
                searchArea,
                spiderling -> spiderling.belongsTo(this)).size();
        int eggs = level.getEntitiesOfClass(
                SpiderEggEntity.class,
                searchArea,
                egg -> egg.belongsTo(this)).size();
        return spiderlings + eggs;
    }

    private boolean hasOwnedSpiderlings(ServerLevel level) {
        return !level.getEntitiesOfClass(
                AbstractSpiderlingEntity.class,
                this.getBoundingBox().inflate(12.0),
                spiderling -> spiderling.belongsTo(this)).isEmpty();
    }

    private List<LivingEntity> combatTargets(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::isCombatTarget);
    }

    private boolean isCombatTarget(LivingEntity target) {
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !BuiltInRegistries.ENTITY_TYPE
                .getKey(target.getType())
                .getNamespace()
                .equals(TheAurorian2.MOD_ID);
    }

    private Vec3 horizontalLook() {
        Vec3 look = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        return look.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
    }

    private void startAction(Action action) {
        if (action != Action.IDLE) {
            this.stopHuntingCharge();
        }
        this.getNavigation().stop();
        this.smashBeamHits.clear();
        this.setAction(action);
    }

    private void startSpit(SpitMode mode) {
        this.spitMode = mode;
        if (mode == SpitMode.SILK) {
            this.silkAttempted = true;
            this.silkInFlight = true;
            this.silkTimeout = 4 * 20;
        }
        this.startAction(Action.SPIT);
    }

    private void startNestQuake(LivingEntity target) {
        this.smashMode = SmashMode.NEST_QUAKE;
        this.nestQuakePosition = target.position();
        this.nestQuakeSent = true;
        this.startAction(Action.SMASH);
    }

    private void finishAction(int cooldown) {
        this.setAction(Action.IDLE);
        int adjustedCooldown = this.isEnraged()
                ? Mth.ceil(cooldown * RAGE_COOLDOWN_MULTIPLIER)
                : cooldown;
        if (this.getRagePhase() == RagePhase.FATIGUED) {
            adjustedCooldown = Math.max(adjustedCooldown, this.rageTicks);
        }
        this.combatCooldown = adjustedCooldown;
        this.spitMode = SpitMode.NORMAL;
        this.smashMode = SmashMode.NORMAL;
        this.nestQuakePosition = null;
    }

    private void finishRegularAttack(int normalCooldown) {
        this.finishAction(this.isEnraged() ? 0 : normalCooldown);
    }

    private void setAction(Action action) {
        this.entityData.set(ACTION, action.id);
        this.actionTicks = 0;
    }

    private ServerBossEvent createBossEvent() {
        return new ServerBossEvent(
                this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    }

    private Action getAction() {
        return Action.byId(this.entityData.get(ACTION));
    }

    public boolean isRageActive() {
        RagePhase phase = this.getRagePhase();
        return phase == RagePhase.WINDUP || phase == RagePhase.ENRAGED;
    }

    private boolean isEnraged() {
        return this.getRagePhase() == RagePhase.ENRAGED;
    }

    private RagePhase getRagePhase() {
        return RagePhase.byId(this.entityData.get(RAGE_PHASE));
    }

    private void setRagePhase(RagePhase phase) {
        this.entityData.set(RAGE_PHASE, phase.id);
    }

    private float rageDamage(float damage) {
        return this.isEnraged() ? damage * RAGE_DAMAGE_MULTIPLIER : damage;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void knockback(double power, double x, double z) {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 speedMultiplier) {
        if (!blockState.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(blockState, speedMultiplier);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.CRAMMING)) {
            return false;
        }
        float healthBefore = this.getHealth();
        float adjustedDamage = this.isEnraged() ? damage * RAGE_DAMAGE_TAKEN_MULTIPLIER : damage;
        boolean hurt = super.hurtServer(level, source, adjustedDamage);
        if (hurt) {
            float actualDamage = Math.max(0.0F, healthBefore - this.getHealth());
            if (actualDamage > 0.0F) {
                if (source.getEntity() instanceof LivingEntity attacker && this.isCombatTarget(attacker)) {
                    this.outOfCombatTicks = 0;
                }
                this.recordRecentDamage(actualDamage, source.getEntity());
            }
        }
        if (hurt && source.getEntity() instanceof Player player && this.isHighGroundTarget(player)) {
            this.elevatedAggressor = player.getUUID();
            this.elevatedAggressorTicks = 4 * 20;
            this.setTarget(player);
        }
        return hurt;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim, DamageSource source) {
        boolean killed = super.killedEntity(level, victim, source);
        if (killed) {
            float healPercent = victim instanceof Player ? 0.20F : 0.05F;
            this.heal(this.getMaxHealth() * healPercent);
            level.sendParticles(
                    RAGE_PARTICLE,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    12, 1.1, 0.7, 1.1, 0.03);
        }
        return killed;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isCombatTarget(target) && super.canAttack(target);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CombatCooldown", this.combatCooldown);
        output.putInt("HatchCooldown", this.hatchCooldown);
        output.putInt("EidolonCooldown", this.eidolonCooldown);
        output.putInt("RagePhase", this.getRagePhase().id);
        output.putInt("RageTicks", this.rageTicks);
        output.putInt("RageCooldown", this.rageCooldown);
        output.putInt("OutOfCombatTicks", this.outOfCombatTicks);
        output.putInt("HuntingChargeCooldown", this.huntingChargeCooldown);
        output.putBoolean("ForcedHatchTriggered", this.forcedHatchTriggered);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.bossEvent = this.createBossEvent();
        this.combatCooldown = input.getIntOr("CombatCooldown", 0);
        this.hatchCooldown = input.getIntOr("HatchCooldown", 8 * 20);
        this.eidolonCooldown = input.getIntOr("EidolonCooldown", EIDOLON_COOLDOWN);
        RagePhase savedRagePhase = RagePhase.byId((byte)input.getIntOr("RagePhase", RagePhase.NORMAL.id));
        this.rageTicks = input.getIntOr("RageTicks", 0);
        this.rageCooldown = input.getIntOr("RageCooldown", 0);
        this.outOfCombatTicks = Math.max(0, input.getIntOr("OutOfCombatTicks", 0));
        this.huntingChargeCooldown = Math.max(0, input.getIntOr("HuntingChargeCooldown", 0));
        this.huntingChargeTicks = 0;
        this.forcedHatchTriggered = input.getBooleanOr("ForcedHatchTriggered", false);
        this.resetHighGroundState();
        this.silkBindings.clear();
        this.venomPools.clear();
        this.recentDamage.clear();
        this.silkInFlight = false;
        this.pendingVenomPool = false;
        this.spitMode = SpitMode.NORMAL;
        this.smashMode = SmashMode.NORMAL;
        this.nestQuakePosition = null;
        this.setRagePhase(savedRagePhase);
        if (savedRagePhase == RagePhase.WINDUP) {
            this.setAction(Action.RAGE_ROAR);
            this.updateMovementSpeed(NORMAL_MOVEMENT_SPEED);
        } else {
            this.setAction(Action.IDLE);
            this.updateMovementSpeed(savedRagePhase == RagePhase.ENRAGED
                    ? RAGE_MOVEMENT_SPEED
                    : savedRagePhase == RagePhase.FATIGUED ? FATIGUE_MOVEMENT_SPEED : NORMAL_MOVEMENT_SPEED);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SpiderMotherEntity>("main", 2, state -> {
            state.setControllerSpeed(this.isHuntingChargeActive() ? 2.0F : 1.0F);
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return switch (this.getAction()) {
                case SLASH -> state.setAndContinue(SLASH);
                case SMASH -> state.setAndContinue(SMASH);
                case SPIT -> state.setAndContinue(SPIT);
                case HATCH_BEGIN -> state.setAndContinue(HATCH_BEGIN);
                case HATCH_HOLD -> state.setAndContinue(HATCH_HOLD);
                case HATCH_END -> state.setAndContinue(HATCH_END);
                case EIDOLON -> state.setAndContinue(EIDOLON);
                case RAGE_ROAR -> state.setAndContinue(EIDOLON);
                case IDLE -> state.setAndContinue(state.isMoving() ? WALK : IDLE);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private final class SpiderMotherCombatGoal extends Goal {

        private SpiderMotherCombatGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = SpiderMotherEntity.this.getTarget();
            return target != null && target.isAlive() && SpiderMotherEntity.this.canAttack(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SpiderMotherEntity mother = SpiderMotherEntity.this;
            LivingEntity target = mother.getTarget();
            if (target == null) {
                mother.stopHuntingCharge();
                return;
            }
            mother.getLookControl().setLookAt(target, 35.0F, 30.0F);
            ServerLevel level = (ServerLevel)mother.level();
            mother.tickHighGroundState(level, target);
            if (mother.getAction() != Action.IDLE) {
                mother.getNavigation().stop();
                return;
            }
            if (mother.getRagePhase() == RagePhase.FATIGUED) {
                mother.stopHuntingCharge();
                mother.getNavigation().stop();
                return;
            }
            double distanceSqr = mother.distanceToSqr(target);
            if (mother.isHuntingChargeActive()) {
                if (distanceSqr > 4.5 * 4.5) {
                    mother.getNavigation().moveTo(target, 1.0);
                    return;
                }
                mother.finishHuntingChargeWithSmash();
                return;
            }
            if (mother.isEnraged()) {
                this.tickEnragedCombat(mother, target);
                return;
            }

            if (mother.hatchCooldown <= 0 && mother.countOwnedUnits(level) < MAX_SPIDERLINGS) {
                mother.startAction(Action.HATCH_BEGIN);
                return;
            }
            if (mother.eidolonCooldown <= 0 && mother.hasOwnedSpiderlings(level)) {
                mother.startAction(Action.EIDOLON);
                return;
            }
            if (mother.pendingVenomPool) {
                mother.getNavigation().stop();
                mother.startSpit(SpitMode.VENOM_POOL);
                return;
            }
            if (!mother.silkAttempted
                    && !mother.silkInFlight
                    && mother.blockedApproachTicks >= SILK_TRIGGER_TICKS) {
                mother.getNavigation().stop();
                mother.startSpit(SpitMode.SILK);
                return;
            }
            if (!mother.nestQuakeSent && mother.highGroundTicks >= NEST_QUAKE_TICKS) {
                mother.getNavigation().stop();
                mother.startNestQuake(target);
                return;
            }

            if (mother.combatCooldown > 0) {
                if (distanceSqr > 3.5 * 3.5 && mother.tickCount % 10 == 0) {
                    mother.getNavigation().moveTo(target, 0.9);
                }
                return;
            }
            if (this.canStartHuntingCharge(mother, target, distanceSqr)) {
                mother.startHuntingCharge(target);
                return;
            }
            if (distanceSqr <= 4.5 * 4.5) {
                mother.getNavigation().stop();
                mother.startAction(mother.random.nextInt(3) == 0 ? Action.SMASH : Action.SLASH);
            } else if (distanceSqr <= 14.0 * 14.0 && mother.getSensing().hasLineOfSight(target)) {
                mother.getNavigation().stop();
                mother.startSpit(SpitMode.NORMAL);
            } else if (mother.tickCount % 10 == 0) {
                mother.getNavigation().moveTo(target, 0.9);
            }
        }

        private void tickEnragedCombat(SpiderMotherEntity mother, LivingEntity target) {
            double distanceSqr = mother.distanceToSqr(target);
            if (mother.combatCooldown > 0) {
                if (distanceSqr > 3.5 * 3.5 && mother.tickCount % 5 == 0) {
                    mother.getNavigation().moveTo(target, 1.0);
                }
                return;
            }
            if (this.canStartHuntingCharge(mother, target, distanceSqr)) {
                mother.startHuntingCharge(target);
                return;
            }
            if (distanceSqr <= 4.5 * 4.5) {
                mother.getNavigation().stop();
                mother.startAction(mother.random.nextInt(3) == 0 ? Action.SMASH : Action.SLASH);
            } else if (mother.tickCount % 5 == 0) {
                mother.getNavigation().moveTo(target, 1.0);
            }
        }

        private boolean canStartHuntingCharge(
                SpiderMotherEntity mother, LivingEntity target, double distanceSqr) {
            return mother.huntingChargeCooldown <= 0
                    && mother.onGround()
                    && distanceSqr >= HUNTING_CHARGE_MIN_RANGE * HUNTING_CHARGE_MIN_RANGE
                    && distanceSqr <= HUNTING_CHARGE_MAX_RANGE * HUNTING_CHARGE_MAX_RANGE
                    && mother.getSensing().hasLineOfSight(target);
        }
    }

    private enum Action {
        IDLE(0),
        SLASH(1),
        SMASH(2),
        SPIT(3),
        HATCH_BEGIN(4),
        HATCH_HOLD(5),
        HATCH_END(6),
        EIDOLON(7),
        RAGE_ROAR(8);

        private final byte id;

        Action(int id) {
            this.id = (byte)id;
        }

        private static Action byId(byte id) {
            for (Action action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return IDLE;
        }
    }

    private enum SpitMode {
        NORMAL,
        SILK,
        VENOM_POOL
    }

    private enum SmashMode {
        NORMAL,
        NEST_QUAKE
    }

    private enum RagePhase {
        NORMAL(0),
        WINDUP(1),
        ENRAGED(2),
        FATIGUED(3);

        private final byte id;

        RagePhase(int id) {
            this.id = (byte)id;
        }

        private static RagePhase byId(byte id) {
            for (RagePhase phase : values()) {
                if (phase.id == id) {
                    return phase;
                }
            }
            return NORMAL;
        }
    }

    private static final class RecentDamage {

        private final int tick;
        private final float damage;
        private final @Nullable UUID attackerId;

        private RecentDamage(int tick, float damage, @Nullable UUID attackerId) {
            this.tick = tick;
            this.damage = damage;
            this.attackerId = attackerId;
        }
    }

    private static final class VenomPool {

        private final Vec3 center;
        private int remainingTicks;

        private VenomPool(Vec3 center, int remainingTicks) {
            this.center = center;
            this.remainingTicks = remainingTicks;
        }
    }
}
