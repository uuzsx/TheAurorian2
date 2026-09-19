package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlockTags;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import cn.teampancake.theaurorian2.common.world.SpiderBroodData;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
    private static final int ENTRANCE_SPIDERLINGS = 3;
    // misc.death lasts 20 ticks, followed by 20 ticks holding its final pose.
    private static final int DEATH_DURATION_TICKS = 40;
    private static final int EGGS_PER_HATCH = 2;
    private static final int REGULAR_HATCH_COOLDOWN = 30 * 20;
    private static final int EIDOLON_COOLDOWN = 24 * 20;
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
    private static final int RAGE_WINDUP_TICKS = 32;
    private static final int RAGE_DURATION_TICKS = 14 * 20;
    private static final int HALF_HEALTH_RAGE_TICKS = 45 * 20;
    private static final int RAGE_COOLDOWN_TICKS = 12 * 20;
    private static final int RAGE_FATIGUE_TICKS = 58;
    private static final double NORMAL_MOVEMENT_SPEED = 0.45;
    private static final double RAGE_MOVEMENT_SPEED = 0.52;
    private static final double FATIGUE_MOVEMENT_SPEED = 0.30;
    private static final float RAGE_DAMAGE_MULTIPLIER = 1.5F;
    private static final float RAGE_DAMAGE_TAKEN_MULTIPLIER = 0.8F;
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
    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWUP =
            SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation SLASH_COMBO = RawAnimation.begin().thenPlay("attack.slash_combo");
    private static final RawAnimation THRUST_COMBO = RawAnimation.begin().thenPlay("attack.thrust_combo");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation SLASH = RawAnimation.begin().thenPlay("attack.slash");
    private static final RawAnimation SMASH = RawAnimation.begin().thenPlay("attack.smash");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("attack.spit");
    private static final RawAnimation HATCH_BEGIN = RawAnimation.begin().thenPlay("misc.hatch_begin");
    private static final RawAnimation HATCH_HOLD = RawAnimation.begin().thenLoop("misc.hatch_hold");
    private static final RawAnimation HATCH_END = RawAnimation.begin().thenPlay("misc.hatch_end");
    private static final RawAnimation EIDOLON = RawAnimation.begin().thenPlay("attack.eidolon");
    private static final RawAnimation THRUST = RawAnimation.begin().thenPlay("attack.thrust");
    private static final RawAnimation SWEEP = RawAnimation.begin().thenPlay("attack.turning_sweep");
    private static final RawAnimation PIN_CUT = RawAnimation.begin().thenPlay("attack.pin_cut");
    private static final RawAnimation POUNCE_WINDUP = RawAnimation.begin().thenPlayAndHold("attack.pounce_windup");
    private static final RawAnimation POUNCE_AIR = RawAnimation.begin().thenPlayAndHold("attack.pounce_air");
    private static final RawAnimation POUNCE_LAND = RawAnimation.begin().thenPlay("attack.pounce_land");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private static final RawAnimation GUARD = RawAnimation.begin().thenLoop("move.guard");
    private static final RawAnimation EXHAUSTED = RawAnimation.begin().thenPlayAndHold("misc.exhausted");
    private static final RawAnimation STAGGER = RawAnimation.begin().thenPlay("misc.stagger");
    private static final RawAnimation RECOVER = RawAnimation.begin().thenPlayAndHold("misc.recover");
    private static final RawAnimation ROAR = RawAnimation.begin().thenPlay("misc.rage_roar");
    private static final RawAnimation CHARGE_WINDUP = RawAnimation.begin().thenPlay("misc.charge_windup");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("misc.death");
    private static final RawAnimation LOOK_AROUND = RawAnimation.begin().thenPlay("misc.look_around");
    private static final RawAnimation GROOM_CLAWS = RawAnimation.begin().thenPlay("misc.groom_claws");
    private static final RawAnimation STRETCH = RawAnimation.begin().thenPlay("misc.stretch");
    private static final RawAnimation INSPECT_GROUND = RawAnimation.begin().thenPlay("misc.inspect_ground");
    private static final RawAnimation SHAKE_LEG = RawAnimation.begin().thenPlay("misc.shake_leg");
    private static final RawAnimation LOOSEN_HINDLEG = RawAnimation.begin().thenPlay("misc.loosen_hindleg");
    private static final RawAnimation FLEX_CLAWS = RawAnimation.begin().thenPlay("misc.flex_claws");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private ServerBossEvent bossEvent;
    private final Set<UUID> smashBeamHits = new HashSet<>();
    private final Map<UUID, Integer> silkBindings = new HashMap<>();
    private final Deque<VenomPool> venomPools = new ArrayDeque<>();
    private final Deque<RecentDamage> recentDamage = new ArrayDeque<>();
    private int actionTicks;
    private float actionFrameRemainder;
    private int combatCooldown;
    private int hatchCooldown = 8 * 20;
    private int entranceSpiderlingsRemaining = ENTRANCE_SPIDERLINGS;
    private int entranceRetryTicks;
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
    private int pounceCooldown;
    private int pursuitCooldown;
    private int sweepCooldown;
    private int specialCooldown;
    // Support skills must be separated by completed main-body attack rounds.
    private int roundsSinceSupport;
    private int guardCooldown;
    private int ambientCooldown = 120;
    private int lastAmbientVariant = -1;
    private int rearPressureTicks;
    private int recoveryTicks;
    private int comboDepth;
    private boolean halfHealthRage;
    private int guardTicks;
    private int guardDirection = 1;
    private boolean pendingRage;
    private boolean pendingPhaseTransition;
    private boolean phaseHatchPending;
    private boolean attackConnected;
    private boolean attackLocked;
    private float attackYaw;
    private Action lastAttack = Action.IDLE;
    private Action queuedFollowup = Action.IDLE;
    private Vec3 lockedAim = Vec3.ZERO;
    private Vec3 pounceDestination = Vec3.ZERO;
    private @Nullable UUID attackTarget;
    private final Set<UUID> meleeHits = new HashSet<>();
    private Vec3 pounceVelocity = Vec3.ZERO;


    private double lastApproachDistance = Double.MAX_VALUE;
    private @Nullable UUID trackedHighTarget;
    private @Nullable UUID elevatedAggressor;
    private @Nullable Vec3 nestQuakePosition;
    private @Nullable BlockPos arenaBarrier;
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
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SpiderMotherCombatGoal());
        this.goalSelector.addGoal(4, new SpiderMotherAmbientGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(
                this, LivingEntity.class, true, (target, level) -> this.isCombatTarget(target)).setUnseenMemoryTicks(200));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ACTION, Action.IDLE.id);
        entityData.define(RAGE_PHASE, RagePhase.NORMAL.id);
        entityData.define(CHARGING, false);
        entityData.define(FOLLOWUP, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.entranceSpiderlingsRemaining > 0 && this.isAlive()
                && this.level() instanceof ServerLevel level && level.getDifficulty() != Difficulty.PEACEFUL
                && this.entranceRetryTicks-- <= 0) {
            this.spawnEntranceSpiderlings(level);
            this.entranceRetryTicks = 20;
        }
    }

    private void spawnEntranceSpiderlings(ServerLevel level) {
        double baseAngle = this.random.nextDouble() * Math.PI * 2.0;
        int attempts = this.entranceSpiderlingsRemaining;
        for (int i = 0; i < attempts && this.countOwnedUnits(level) < MAX_SPIDERLINGS; i++) {
            EntityType<? extends AbstractSpiderlingEntity> type = this.random.nextBoolean()
                    ? ModEntities.SPIDERLING.get() : ModEntities.SPIDERLING_CRYSTAL_SHELL.get();
            AbstractSpiderlingEntity spiderling = type.create(level, EntitySpawnReason.TRIGGERED);
            if (spiderling == null) continue;
            double angle = baseAngle + i * Math.PI * 2.0 / ENTRANCE_SPIDERLINGS;
            Vec3 spawn = SpiderSummonPlacement.find(level, spiderling,
                    this.position().add(Math.cos(angle) * 3.5, 0.0, Math.sin(angle) * 3.5));
            if (spawn == null) continue;
            spiderling.snapTo(spawn.x, spawn.y, spawn.z, this.getYRot(), 0.0F);
            spiderling.setMother(this.getUUID());
            spiderling.finalizeSpawn(level, level.getCurrentDifficultyAt(spiderling.blockPosition()),
                    EntitySpawnReason.TRIGGERED, null);
            if (level.addFreshEntity(spiderling)) this.entranceSpiderlingsRemaining--;
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (this.arenaBarrier == null) {
                this.discard();
            } else {
                this.setTarget(null);
                this.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setName(this.getDisplayName());
        this.tickTrapEscape(level);
        this.tickSilkBindings(level);
        this.tickVenomPools(level);
        boolean wasEnraged = this.isEnraged();
        this.tickRage(level);
        // The expiry tick starts fatigue; its full 58 ticks start on the next tick.
        if (wasEnraged && this.getRagePhase() == RagePhase.FATIGUED) return;
        this.triggerHalfHealthPhase();
        this.tickOutOfCombatHealing(level);
        LivingEntity combatTarget = this.getTarget();
        if (combatTarget != null && combatTarget.isAlive()) this.ambientCooldown = Math.max(this.ambientCooldown, 120);
        else if (this.ambientCooldown > 0) this.ambientCooldown--;
        if (combatTarget != null && combatTarget.isAlive()) this.tickHighGroundState(level, combatTarget);
        else this.resetHighGroundState();
        if (this.isHuntingChargeActive()) this.updateAttackFacing();
        if (combatTarget != null && this.distanceToSqr(combatTarget) < 36 && !this.isInFront(combatTarget, -.15)) {
            this.rearPressureTicks = Math.min(60, this.rearPressureTicks + 1);
        } else this.rearPressureTicks = Math.max(0, this.rearPressureTicks - 2);
        if (this.elevatedAggressorTicks > 0) {
            this.elevatedAggressorTicks--;
        } else {
            this.elevatedAggressor = null;
        }
        if (this.silkTimeout > 0 && --this.silkTimeout == 0 && this.silkInFlight) {
            this.onSilkResolved(false);
        }
        if (this.tickCount % 20 == 0 && this.getAction() == Action.IDLE && !this.isHuntingChargeActive()) {
            this.preferReachableGroundTarget(level);
        }
        if (this.pounceCooldown > 0) this.pounceCooldown--;
        if (this.pursuitCooldown > 0) this.pursuitCooldown--;
        if (this.sweepCooldown > 0) this.sweepCooldown--;
        if (this.specialCooldown > 0) this.specialCooldown--;
        if (this.guardCooldown > 0) this.guardCooldown--;
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

    private float actionPlaybackSpeed() {
        if (this.isDeadOrDying()) return 1.0F;
        return switch (this.getAction()) {
            case SLASH, SMASH, THRUST, SWEEP, SPIT -> this.isEnraged() ? 1.75F : 1.25F;
            // Keep the delayed pin-cut's two hits outside vanilla hurt immunity.
            // Air movement, recovery and punish windows stay on real game ticks.
            default -> 1.0F;
        };
    }

    private void tickAction(ServerLevel level) {
        Action initialAction = this.getAction();
        this.actionFrameRemainder += this.actionPlaybackSpeed();
        int frames = (int)this.actionFrameRemainder;
        this.actionFrameRemainder -= frames;
        // Advance every crossed animation frame so faster playback never skips hits.
        for (int frame = 0; frame < frames; frame++) {
            this.tickActionFrame(level);
            if (this.getAction() != initialAction) break;
        }
    }

    private void tickActionFrame(ServerLevel level) {
        this.actionTicks++;
        this.updateAttackFacing();
        this.tickAttackWarning(level);
        if (this.getAction() != Action.POUNCE_AIR && this.getAction() != Action.GUARD) {
            this.setSpeed(0.0F);
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        switch (this.getAction()) {
            case SLASH -> {
                if (this.actionTicks == 8) {
                    this.performSlash(level);
                }
                if (this.actionTicks >= 20) {
                    this.finishRegularAttack(4);
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
                        this.finishRegularAttack(4);
                    }
                }
            }
            case SPIT -> {
                if (this.actionTicks == 8) {
                    LivingEntity target = this.getTarget();
                    if (target != null && target.isAlive()) {
                        switch (this.spitMode) {
                            case NORMAL -> SpiderVenomProjectileEntity.shootWebbing(level, this, this.lockedAim, 6.0F);
                            case SILK -> SpiderSilkProjectileEntity.shoot(level, this, this.lockedAim);
                            case VENOM_POOL -> {
                                SpiderVenomProjectileEntity.shootPool(level, this, this.lockedAim);
                                this.pendingVenomPool = false;
                            }
                        }
                    } else if (this.spitMode == SpitMode.SILK) {
                        this.onSilkResolved(false);
                    }
                }
                if (this.actionTicks >= 14) {
                    if (this.spitMode == SpitMode.NORMAL) {
                        this.finishRegularAttack(6);
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
                    this.finishAction(6);
                }
            }
            case EIDOLON -> {
                if (this.actionTicks == 20) {
                    this.performEidolonPulse(level);
                }
                if (this.actionTicks >= 50) {
                    this.eidolonCooldown = EIDOLON_COOLDOWN;
                    this.finishAction(6);
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
            case THRUST -> {
                if (this.actionTicks == 12) this.hitDirected(level, 0.7, 4.0, 26.0F, 0.0, false);
                if (this.actionTicks >= 29) this.finishRegularAttack(6);
            }
            case SWEEP -> {
                if (this.actionTicks >= 15 && this.actionTicks <= 21) {
                    this.hitDirected(level, 1.1, 3.8, 24.0F, -55.0 + (this.actionTicks - 15) * 25.0, true);
                }
                if (this.actionTicks >= 38) this.finishHeavyAttack(10);
            }
            case PIN_CUT -> {
                if (this.actionTicks == 12) this.hitDirected(level, 0.75, 3.3, 18.0F, -25.0, false);
                if (this.actionTicks == 22) this.meleeHits.clear();
                if (this.actionTicks >= 24 && this.actionTicks <= 29) {
                    this.hitDirected(level, 1.0, 3.5, 26.0F, -40.0 + (this.actionTicks - 24) * 18.0, true);
                }
                if (this.actionTicks >= 46) this.finishHeavyAttack(10);
            }
            case CHARGE_WINDUP -> {
                if (this.actionTicks >= 10) {
                    LivingEntity target = this.committedTarget();
                    if (target != null && this.hasLineOfSight(target)) this.beginCharge();
                    else this.beginRecovery(20);
                }
            }
            case POUNCE_WINDUP -> {
                if (this.actionTicks == 10 && !this.preparePounce(level)) this.beginRecovery(20);
                else if (this.actionTicks >= 16) this.launchPounce();
            }
            case POUNCE_AIR -> {
                if (!this.horizontalCollision && !this.onGround()) {
                    this.setDeltaMovement(this.pounceVelocity.x, this.getDeltaMovement().y, this.pounceVelocity.z);
                }
                if ((this.actionTicks > 3 && this.onGround()) || this.actionTicks >= 50) {
                    this.setAction(Action.POUNCE_LAND);
                    if (this.onGround()) this.performPounceLanding(level);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.15, 1.0, 0.15));
                }
            }
            case POUNCE_LAND -> {
                if (this.actionTicks >= 13) this.finishHeavyAttack(10);
            }
            case RECOVER -> {
                this.getNavigation().stop();
                if (this.actionTicks >= this.recoveryTicks) {
                    this.setAction(Action.IDLE);
                    this.combatCooldown = 0;
                }
            }
            case STAGGER -> {
                this.getNavigation().stop();
                if (this.actionTicks >= 35) this.beginRecovery(8);
            }
            case EXHAUSTED -> {
                this.getNavigation().stop();
                if (this.actionTicks >= RAGE_FATIGUE_TICKS) {
                    this.setRagePhase(RagePhase.NORMAL);
                    this.updateMovementSpeedForState();
                    this.setAction(Action.IDLE);
                    this.combatCooldown = 0;
                }
            }
            case GUARD -> {
                if (this.actionTicks >= this.guardTicks) {
                    this.getNavigation().stop();
                    this.setAction(Action.IDLE);
                }
            }
            case LOOK_AROUND, GROOM_CLAWS, STRETCH, INSPECT_GROUND, SHAKE_LEG, LOOSEN_HINDLEG, FLEX_CLAWS -> {
                int duration = switch (this.getAction()) {
                    case LOOK_AROUND -> 80;
                    case GROOM_CLAWS -> 92;
                    case INSPECT_GROUND -> 116;
                    case SHAKE_LEG -> 104;
                    case LOOSEN_HINDLEG -> 120;
                    case FLEX_CLAWS -> 128;
                    default -> 84;
                };
                if (this.actionTicks >= duration) this.setAction(Action.IDLE);
            }
            case IDLE -> { }

        }
    }

    private void startHuntingCharge(LivingEntity target) {
        this.huntingChargeCooldown = HUNTING_CHARGE_COOLDOWN_TICKS;
        this.pursuitCooldown = 80;
        this.startAction(Action.CHARGE_WINDUP);
    }

    private void beginCharge() {
        this.setAction(Action.IDLE);
        this.huntingChargeTicks = HUNTING_CHARGE_DURATION_TICKS;
        this.entityData.set(CHARGING, true);
        this.updateMovementSpeed(HUNTING_CHARGE_MOVEMENT_SPEED);
        this.attackLocked = true;
        Vec3 forward = this.attackForward();
        this.getNavigation().moveTo(this.getX() + forward.x * 9.0, this.getY(), this.getZ() + forward.z * 9.0, 1.0);
    }

    private boolean isHuntingChargeActive() { return this.entityData.get(CHARGING); }

    private void stopHuntingCharge() {
        this.huntingChargeTicks = 0;
        this.entityData.set(CHARGING, false);
        this.updateMovementSpeedForState();
    }

    private void finishHuntingChargeWithSmash() {
        this.stopHuntingCharge();
        // Keep the committed direction so a successful sidestep stays successful.
        float heading = this.attackYaw;
        this.startAction(Action.SMASH);
        this.attackYaw = heading;
        this.attackLocked = true;
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
        if (this.rageCooldown > 0) this.rageCooldown--;
        if (this.isEnraged()) {
            if (this.tickCount % 8 == 0) level.sendParticles(RAGE_PARTICLE, this.getX(), this.getY() + 2,
                    this.getZ(), 3, 0.8, 0.5, 0.8, 0.01);
            if (this.rageTicks > 0) this.rageTicks--;
            if (this.rageTicks == 0) this.enterFatigue();
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
        // Earned recovery is never cancelled by the player's damage burst.
        this.pendingRage = true;
    }

    private void pruneRecentDamage() {
        int earliestTick = this.tickCount - RAGE_DAMAGE_WINDOW_TICKS;
        while (!this.recentDamage.isEmpty() && this.recentDamage.peekFirst().tick < earliestTick) {
            this.recentDamage.removeFirst();
        }
    }

    private void startRageWindup() {
        this.recentDamage.clear();
        this.pendingRage = false;
        this.halfHealthRage = this.pendingPhaseTransition;
        this.pendingPhaseTransition = false;
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
        this.rageTicks = this.halfHealthRage ? HALF_HEALTH_RAGE_TICKS : RAGE_DURATION_TICKS;
        this.updateMovementSpeed(RAGE_MOVEMENT_SPEED);
        this.beginRecovery(10);
    }

    private void triggerHalfHealthPhase() {
        if (this.forcedHatchTriggered || this.getHealth() > this.getMaxHealth() * 0.5F) return;
        // Reuse the existing saved flag as permanent phase two; never interrupt a move.
        this.forcedHatchTriggered = true;
        this.pendingPhaseTransition = true;
        this.phaseHatchPending = true;
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
        this.specialCooldown = Math.max(this.specialCooldown, SILK_BIND_TICKS + 30);
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
            this.specialCooldown = Math.max(this.specialCooldown, 30);
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
        this.pendingVenomPool = false;
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
        Vec3 spawn = SpiderSummonPlacement.find(level, spiderling,
                target.position().add(Math.cos(angle) * 2.0, 0.0, Math.sin(angle) * 2.0));
        if (spawn == null) {
            spawn = SpiderSummonPlacement.find(level, spiderling,
                    this.position().add(Math.cos(angle) * 2.8, 0.0, Math.sin(angle) * 2.8));
        }
        if (spawn == null) {
            return false;
        }
        spiderling.snapTo(spawn.x, spawn.y, spawn.z, (float)Math.toDegrees(angle), 0.0F);
        if (!level.addFreshEntity(spiderling)) {
            return false;
        }
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
        Vec3 forward = this.attackForward();
        AABB hitArea = this.getBoundingBox()
                .expandTowards(forward.scale(4.5))
                .inflate(1.25, 0.75, 1.25);
        float damage = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 22.0F;
            case NORMAL -> 28.0F;
            case HARD -> 32.0F;
        };
        damage = this.rageDamage(damage);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            Vec3 toTarget = target.getBoundingBox().getCenter()
                    .subtract(this.getBoundingBox().getCenter())
                    .multiply(1.0, 0.0, 1.0);
            if (toTarget.dot(forward) >= 0.0
                    && toTarget.lengthSqr() <= Mth.square(3.4 + target.getBbWidth() * 0.5)
                    && this.hasLineOfSight(target)) {
                this.attackConnected = true;
                if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) this.smashBeamHits.add(target.getUUID());
            }
        }
    }

    private void performSmash(ServerLevel level) {
        Vec3 forward = this.attackForward();
        Vec3 center = this.position().add(forward.scale(2.6));
        AABB hitArea = this.getBoundingBox()
                .expandTowards(forward.scale(4.8))
                .inflate(1.75, 1.0, 1.75);
        float damage = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 30.0F;
            case NORMAL -> 38.0F;
            case HARD -> 44.0F;
        };
        damage = this.rageDamage(damage);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            Vec3 offset = target.position().subtract(this.position());
            double along = offset.dot(forward);
            double across = Math.abs(offset.x * forward.z - offset.z * forward.x);
            double margin = target.getBbWidth() * 0.5;
            if (along >= 0.0 && along <= 3.8 + margin && across <= 1.75 + margin
                    && this.hasLineOfSight(target)) {
                this.attackConnected = true;
                if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) this.smashBeamHits.add(target.getUUID());
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.2, center.z, 4, 1.0, 0.2, 1.0, 0.0);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.75F);
    }

    private void performSmashBeam(ServerLevel level, double sideOffset) {
        Vec3 forward = this.attackForward();
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 center = this.position().add(forward.scale(2.6)).add(side.scale(sideOffset));
        AABB hitArea = new AABB(center, center).inflate(1.25, 3.75, 1.25).move(0.0, 3.0, 0.0);
        for (LivingEntity target : this.combatTargets(level, hitArea)) {
            if (!this.smashBeamHits.contains(target.getUUID()) && this.hasLineOfSight(target)
                    && target.hurtServer(level, this.damageSources().mobAttack(this), this.rageDamage(30.0F))) {
                this.attackConnected = true;
                this.smashBeamHits.add(target.getUUID());
            }
        }
    }

    private void performEidolonPulse(ServerLevel level) {
        AABB pulseArea = this.getBoundingBox().inflate(7.0);
        for (LivingEntity target : this.combatTargets(level, pulseArea)) {
            if (target.distanceToSqr(this) <= 7.0 * 7.0 && this.hasLineOfSight(target)
                    && target.hurtServer(level, this.damageSources().mobAttack(this), 8.0F)) {
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
        int eggCount = Math.min(this.phaseHatchPending ? 3 : EGGS_PER_HATCH, Math.max(0, room));
        this.phaseHatchPending = false;
        this.specialCooldown = Math.max(this.specialCooldown, 120);
        this.eidolonCooldown = Math.max(this.eidolonCooldown, 120);
        double baseAngle = this.random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < eggCount; i++) {
            SpiderEggEntity egg = ModEntities.SPIDER_EGG.get().create(level, EntitySpawnReason.TRIGGERED);
            if (egg == null) {
                continue;
            }
            double angle = baseAngle + i * Math.PI * 2.0 / Math.max(1, eggCount);
            egg.setMother(this.getUUID());
            Vec3 spawn = SpiderSummonPlacement.find(level, egg,
                    this.position().add(Math.cos(angle) * 2.8, 0.0, Math.sin(angle) * 2.8));
            if (spawn != null) {
                egg.snapTo(spawn.x, spawn.y, spawn.z, (float)Math.toDegrees(angle), 0.0F);
                level.addFreshEntity(egg);
            }
        }
        level.sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + 1.0, this.getZ(),
                24, 2.0, 0.7, 2.0, 0.04);
    }

    private int countOwnedUnits(ServerLevel level) {
        return SpiderBroodData.get(level).count(this.getUUID());
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
        this.meleeHits.clear();
        this.attackConnected = false;
        this.attackLocked = false;
        this.entityData.set(FOLLOWUP, this.queuedFollowup != Action.IDLE);
        LivingEntity target = this.getTarget();
        this.attackTarget = target == null ? null : target.getUUID();
        this.attackYaw = this.getYRot();
        this.lockedAim = target == null ? this.position().add(this.horizontalLook().scale(5)) : target.getBoundingBox().getCenter();
        this.setAction(action);
        this.playSound(SoundEvents.SPIDER_AMBIENT, 0.8F, 0.7F);
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
        this.queuedFollowup = Action.IDLE;
        this.comboDepth = 0;
        this.specialCooldown = Math.max(this.specialCooldown, 60);
        this.beginRecovery(cooldown);
    }

    private void finishRegularAttack(int normalCooldown) {
        if (this.getAction() == Action.SMASH) {
            this.finishHeavyAttack(10);
            return;
        }
        LivingEntity target = this.committedTarget();
        if (this.queuedFollowup != Action.IDLE && target != null && this.silkBindings.isEmpty()
                && this.distanceToSqr(target) < 4.2 * 4.2 && this.isInFront(target, 0.0)
                && this.hasLineOfSight(target)) {
            Action next = this.queuedFollowup;
            this.queuedFollowup = this.isEnraged() && this.comboDepth == 0 && next == Action.THRUST
                    ? Action.SMASH : Action.IDLE;
            this.comboDepth++;
            this.lastAttack = next;
            this.startAction(next);
        } else this.finishRound(normalCooldown, false);
    }

    private void finishHeavyAttack(int recovery) {
        // Ordinary missed swings already have an animated follow-through. Only a
        // committed leap or pinning attack warrants the extra stumble opening.
        boolean overextended = this.getAction() == Action.POUNCE_LAND || this.getAction() == Action.PIN_CUT;
        this.finishRound(recovery, overextended && !this.attackConnected && this.onGround());
    }

    private void finishRound(int recovery, boolean whiffed) {
        this.queuedFollowup = Action.IDLE;
        this.comboDepth = 0;
        this.roundsSinceSupport = Math.min(3, this.roundsSinceSupport + 1);
        if (whiffed) {
            this.stopHuntingCharge();
            this.setAction(Action.STAGGER);
            this.combatCooldown = 35;
        } else this.beginRecovery(recovery);
    }

    private void beginRecovery(int ticks) {
        this.stopHuntingCharge();
        this.getNavigation().stop();
        this.queuedFollowup = Action.IDLE;
        this.recoveryTicks = ticks;
        this.entityData.set(FOLLOWUP, false);
        this.combatCooldown = ticks;
        this.spitMode = SpitMode.NORMAL;
        this.smashMode = SmashMode.NORMAL;
        this.nestQuakePosition = null;
        this.setAction(Action.RECOVER);
    }


    private @Nullable LivingEntity committedTarget() {
        if (this.attackTarget == null || !(this.level() instanceof ServerLevel level)) return null;
        Entity entity = level.getEntity(this.attackTarget);
        return entity instanceof LivingEntity living && living.isAlive() && this.isCombatTarget(living) ? living : null;
    }

    private Vec3 attackForward() {
        double radians = Math.toRadians(this.attackYaw);
        return new Vec3(-Math.sin(radians), 0, Math.cos(radians));
    }

    private boolean isInFront(LivingEntity target, double cosine) {
        Vec3 delta = target.position().subtract(this.position()).multiply(1, 0, 1);
        return delta.lengthSqr() < 0.01 || this.horizontalLook().dot(delta.normalize()) >= cosine;
    }

    private void updateAttackFacing() {
        Action action = this.getAction();
        if (this.isAmbientAction()) return;
        if (action == Action.IDLE && !this.isHuntingChargeActive() || action == Action.GUARD
                || action == Action.RECOVER || action == Action.STAGGER || action == Action.EXHAUSTED) return;
        int lockTick = switch (action) {
            case SLASH -> 4;
            case SMASH, SPIT -> 5;
            case THRUST, PIN_CUT -> 7;
            case SWEEP -> 10;
            case CHARGE_WINDUP -> 6;
            case POUNCE_WINDUP -> 10;
            default -> 1;
        };
        if (!this.attackLocked) {
            LivingEntity target = this.committedTarget();
            if (target != null) {
                Vec3 delta = target.position().subtract(this.position());
                float desired = (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
                this.attackYaw = Mth.approachDegrees(this.attackYaw, desired, 12.0F);
                this.lockedAim = target.getBoundingBox().getCenter();
            }
            if (this.actionTicks >= lockTick) this.attackLocked = true;
        }
        this.setYRot(this.attackYaw);
        this.yBodyRot = this.attackYaw;
        this.setYHeadRot(this.attackYaw);
        this.setXRot(0);
    }

    private void hitDirected(ServerLevel level, double width, double reach, float damage, double angle, boolean sweep) {
        double yaw = Math.toRadians(this.attackYaw + angle);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        AABB area = this.getBoundingBox().inflate(reach, 0.5, reach);
        for (LivingEntity target : this.combatTargets(level, area)) {
            Vec3 d = target.position().subtract(this.position());
            double along = d.dot(forward);
            double across = Math.abs(d.x * forward.z - d.z * forward.x);
            if (along < 0 || along > reach + target.getBbWidth() * 0.5
                    || across > width + target.getBbWidth() * 0.5 || !this.hasLineOfSight(target)
                    || this.meleeHits.contains(target.getUUID())) continue;
            this.attackConnected = true;
            // A blocked hit also consumes this swing; subsequent frames cannot slip past a shield.
            this.meleeHits.add(target.getUUID());
            if (target.hurtServer(level, this.damageSources().mobAttack(this), this.rageDamage(damage)) && sweep) {
                target.push(forward.x * 0.22, 0.08, forward.z * 0.22);
            }
        }
        if (!sweep || this.actionTicks % 3 == 0) {
            Vec3 point = this.position().add(forward.scale(reach * 0.65));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y + 0.8, point.z, 1, 0, 0, 0, 0);
        }
        if (!sweep || this.actionTicks % 5 == 0) this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.65F);
    }

    private void tickAttackWarning(ServerLevel level) {
        Action action = this.getAction();
        if (this.actionTicks % 4 != 0) return;
        if (action == Action.EIDOLON && this.actionTicks < 20) {
            this.warningRing(level, this.position(), 7.0);
        } else if (action == Action.PIN_CUT && this.actionTicks >= 16 && this.actionTicks <= 24) {
            Vec3 p = this.position().add(this.attackForward().scale(2.5));
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y + 0.3, p.z, 3, 0.4, 0.1, 0.4, 0);
            if (this.actionTicks == 20) this.playSound(SoundEvents.SPIDER_AMBIENT, 1.2F, 1.3F);
        }
    }

    private void warningRing(ServerLevel level, Vec3 center, double radius) {
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            level.sendParticles(ParticleTypes.END_ROD, center.x + Math.cos(angle) * radius, center.y + 0.12,
                    center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }

    private boolean preparePounce(ServerLevel level) {
        LivingEntity target = this.committedTarget();
        if (target == null || !this.onGround() || !this.hasLineOfSight(target)) return false;
        Vec3 desired = target.position();
        // Bounded floor check and a swept body volume, without loading new chunks.
        for (int dy = 1; dy >= -3; dy--) {
            BlockPos feet = BlockPos.containing(desired.x, desired.y + dy, desired.z);
            if (!level.hasChunkAt(feet) || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)) continue;
            Vec3 candidate = new Vec3(desired.x, feet.getY(), desired.z);
            if (Math.abs(candidate.y - this.getY()) > 2.0 || !this.pouncePathClear(level, candidate)) continue;
            this.pounceDestination = candidate;
            return true;
        }
        return false;
    }

    private boolean pouncePathClear(ServerLevel level, Vec3 candidate) {
        Vec3 delta = candidate.subtract(this.position());
        for (int i = 1; i <= 24; i++) {
            double t = i / 24.0;
            Vec3 pos = this.position().add(delta.scale(t)).add(0, 5.2 * 4 * t * (1 - t), 0);
            BlockPos feet = BlockPos.containing(pos);
            if (!level.hasChunkAt(feet) || !level.getWorldBorder().isWithinBounds(feet)
                    || !level.getFluidState(feet).isEmpty()
                    || !level.noBlockCollision(this, this.getBoundingBox().move(pos.subtract(this.position())))) return false;
        }
        return true;
    }

    private void launchPounce() {
        this.getNavigation().stop();
        this.setAction(Action.POUNCE_AIR);
        this.attackLocked = true;
        Vec3 delta = this.pounceDestination.subtract(this.position());
        this.pounceVelocity = new Vec3(delta.x / 23.0, 0, delta.z / 23.0);
        this.setDeltaMovement(this.pounceVelocity.x, 0.92, this.pounceVelocity.z);
        this.push(Vec3.ZERO);
        this.playSound(SoundEvents.SPIDER_AMBIENT, 1.3F, 0.5F);
    }

    private void performPounceLanding(ServerLevel level) {
        for (LivingEntity target : this.combatTargets(level, this.getBoundingBox().inflate(2.5, 0.5, 2.5))) {
            Vec3 delta = target.position().subtract(this.position()).multiply(1, 0, 1);
            if (delta.lengthSqr() > Math.pow(2.5 + target.getBbWidth() * 0.5, 2) || !this.hasLineOfSight(target)) continue;
            this.attackConnected = true;
            if (target.hurtServer(level, this.damageSources().mobAttack(this), this.rageDamage(32.0F))) {
                Vec3 knock = delta.lengthSqr() < .01 ? this.attackForward() : delta.normalize();
                target.push(knock.x * .35, .12, knock.z * .35);
            }
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.1F, .75F);
    }

    private void enterFatigue() {
        this.pendingRage = false;
        this.rageCooldown = RAGE_COOLDOWN_TICKS;
        this.rageTicks = 0;
        this.setRagePhase(RagePhase.FATIGUED);
        this.startAction(Action.EXHAUSTED);
        this.updateMovementSpeedForState();
        this.combatCooldown = RAGE_FATIGUE_TICKS;
        this.playSound(SoundEvents.SPIDER_AMBIENT, 1.0F, .45F);
    }

    private void guardAround(ServerLevel level, LivingEntity target) {
        this.getLookControl().setLookAt(target, 10, 10);
        if (this.actionTicks % 10 != 0) return;
        Vec3 toward = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-toward.z, 0, toward.x).scale(this.guardDirection * 2.0);
        Vec3 next = this.position().add(side);
        BlockPos floor = BlockPos.containing(next).below();
        if (!level.hasChunkAt(floor) || !level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)
                || !level.noBlockCollision(this, this.getBoundingBox().move(side))) {
            this.guardDirection = -this.guardDirection;
            this.getNavigation().stop();
        } else this.getNavigation().moveTo(next.x, next.y, next.z, 0.4);
    }

    private void setAction(Action action) {
        this.entityData.set(ACTION, action.id);
        this.actionTicks = 0;
        this.actionFrameRemainder = 0;
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

    public void setArenaBarrier(@Nullable BlockPos arenaBarrier) {
        this.arenaBarrier = arenaBarrier == null ? null : arenaBarrier.immutable();
    }

    @Override
    public void die(DamageSource source) {
        this.stopHuntingCharge();
        this.getNavigation().stop();
        if (this.arenaBarrier != null && this.level() instanceof ServerLevel level) {
            this.releaseArenaBarrier(level);
        }
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        if (this.deathTime < DEATH_DURATION_TICKS - 1) {
            this.deathTime++;
        } else {
            // Retain vanilla server removal and death-particle synchronization.
            super.tickDeath();
        }
    }

    private void releaseArenaBarrier(ServerLevel level) {
        if (this.arenaBarrier == null) {
            return;
        }
        BlockPos center = this.arenaBarrier;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getBlockState(cursor).is(
                            cn.teampancake.theaurorian2.common.registry.ModStructureBlocks.SPIDER_MOTHER_BARRIER.get())) {
                        level.removeBlock(cursor, false);
                    }
                }
            }
        }
        this.arenaBarrier = null;
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
        output.putInt("EntranceSpiderlingsRemaining", this.entranceSpiderlingsRemaining);
        output.putInt("EidolonCooldown", this.eidolonCooldown);
        output.putInt("RagePhase", this.getRagePhase().id);
        output.putInt("RageTicks", this.rageTicks);
        output.putBoolean("HalfHealthRage", this.halfHealthRage);
        output.putBoolean("FollowRangeUpdated", true);
        output.putInt("RageCooldown", this.rageCooldown);
        output.putInt("OutOfCombatTicks", this.outOfCombatTicks);
        output.putInt("HuntingChargeCooldown", this.huntingChargeCooldown);
        output.putBoolean("ForcedHatchTriggered", this.forcedHatchTriggered);
        output.putBoolean("StepHeightUpdated", true);
        output.putInt("CombatVersion", 2);
        output.putInt("PounceCooldown", this.pounceCooldown);
        output.putInt("PursuitCooldown", this.pursuitCooldown);
        output.putInt("SpecialCooldown", this.specialCooldown);
        output.putBoolean("PendingPhaseTransition", this.pendingPhaseTransition);
        output.putBoolean("PhaseHatchPending", this.phaseHatchPending);
        output.putBoolean("PendingRage", this.pendingRage);

        if (this.arenaBarrier != null) {
            output.putInt("ArenaBarrierX", this.arenaBarrier.getX());
            output.putInt("ArenaBarrierY", this.arenaBarrier.getY());
            output.putInt("ArenaBarrierZ", this.arenaBarrier.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Older saves may explicitly contain the former vanilla 0.6-block base value.
        AttributeInstance stepHeight = this.getAttribute(Attributes.STEP_HEIGHT);
        if (!input.getBooleanOr("StepHeightUpdated", false)
                && stepHeight != null && stepHeight.getBaseValue() == 0.6) {
            stepHeight.setBaseValue(1.0);
        }
        AttributeInstance followRange = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (!input.getBooleanOr("FollowRangeUpdated", false)
                && followRange != null && followRange.getBaseValue() == 32.0) {
            followRange.setBaseValue(64.0);
        }
        this.bossEvent = this.createBossEvent();
        this.combatCooldown = input.getIntOr("CombatCooldown", 0);
        this.hatchCooldown = input.getIntOr("HatchCooldown", 8 * 20);
        // Old saved bosses have already made their entrance; never add a new escort on load.
        this.entranceSpiderlingsRemaining = Mth.clamp(input.getIntOr("EntranceSpiderlingsRemaining", 0), 0, ENTRANCE_SPIDERLINGS);
        this.entranceRetryTicks = 0;
        this.eidolonCooldown = input.getIntOr("EidolonCooldown", EIDOLON_COOLDOWN);
        RagePhase savedRagePhase = RagePhase.byId((byte)input.getIntOr("RagePhase", RagePhase.NORMAL.id));
        this.rageTicks = input.getIntOr("RageTicks", 0);
        this.rageCooldown = input.getIntOr("RageCooldown", 0);
        this.outOfCombatTicks = Math.max(0, input.getIntOr("OutOfCombatTicks", 0));
        this.huntingChargeCooldown = Math.max(0, input.getIntOr("HuntingChargeCooldown", 0));
        this.huntingChargeTicks = 0;
        this.entityData.set(CHARGING, false);
        this.pounceCooldown = Math.max(0, input.getIntOr("PounceCooldown", 0));
        this.pursuitCooldown = Math.max(40, input.getIntOr("PursuitCooldown", 0));
        this.specialCooldown = Math.max(40, input.getIntOr("SpecialCooldown", 0));
        this.pendingPhaseTransition = input.getBooleanOr("PendingPhaseTransition", false);
        this.phaseHatchPending = input.getBooleanOr("PhaseHatchPending", false);
        this.pendingRage = input.getBooleanOr("PendingRage", false);
        this.queuedFollowup = Action.IDLE;
        this.comboDepth = 0;
        this.halfHealthRage = input.getBooleanOr("HalfHealthRage", false);
        this.rageTicks = Mth.clamp(this.rageTicks, 0, this.halfHealthRage ? HALF_HEALTH_RAGE_TICKS : RAGE_DURATION_TICKS);
        this.forcedHatchTriggered = input.getBooleanOr("ForcedHatchTriggered", false);
        if (input.getInt("ArenaBarrierX").isPresent()
                && input.getInt("ArenaBarrierY").isPresent()
                && input.getInt("ArenaBarrierZ").isPresent()) {
            this.arenaBarrier = new BlockPos(
                    input.getIntOr("ArenaBarrierX", 0),
                    input.getIntOr("ArenaBarrierY", 0),
                    input.getIntOr("ArenaBarrierZ", 0));
        } else {
            this.arenaBarrier = null;
        }
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
        } else if (savedRagePhase == RagePhase.FATIGUED) {
            this.setAction(Action.EXHAUSTED);
        } else {
            // Active attacks and airborne trajectories are not resumed after unloading.
            this.beginRecovery(Math.max(20, Math.min(60, this.combatCooldown)));
        }
        this.updateMovementSpeedForState();
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SpiderMotherEntity>("main", 2, state -> {
            state.setControllerSpeed(this.actionPlaybackSpeed());
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return switch (this.getAction()) {
                case SLASH -> state.setAndContinue(this.entityData.get(FOLLOWUP) ? SLASH_COMBO : SLASH);
                case SMASH -> state.setAndContinue(SMASH);
                case SPIT -> state.setAndContinue(SPIT);
                case HATCH_BEGIN -> state.setAndContinue(HATCH_BEGIN);
                case HATCH_HOLD -> state.setAndContinue(HATCH_HOLD);
                case HATCH_END -> state.setAndContinue(HATCH_END);
                case EIDOLON -> state.setAndContinue(EIDOLON);
                case THRUST -> state.setAndContinue(this.entityData.get(FOLLOWUP) ? THRUST_COMBO : THRUST);
                case SWEEP -> state.setAndContinue(SWEEP);
                case PIN_CUT -> state.setAndContinue(PIN_CUT);
                case POUNCE_WINDUP -> state.setAndContinue(POUNCE_WINDUP);
                case POUNCE_AIR -> state.setAndContinue(POUNCE_AIR);
                case POUNCE_LAND -> state.setAndContinue(POUNCE_LAND);
                case GUARD -> state.setAndContinue(GUARD);
                case EXHAUSTED -> state.setAndContinue(EXHAUSTED);
                case STAGGER -> state.setAndContinue(STAGGER);
                case RECOVER -> state.setAndContinue(RECOVER);
                case RAGE_ROAR -> state.setAndContinue(ROAR);
                case CHARGE_WINDUP -> state.setAndContinue(CHARGE_WINDUP);
                case LOOK_AROUND -> state.setAndContinue(LOOK_AROUND);
                case GROOM_CLAWS -> state.setAndContinue(GROOM_CLAWS);
                case STRETCH -> state.setAndContinue(STRETCH);
                case INSPECT_GROUND -> state.setAndContinue(INSPECT_GROUND);
                case SHAKE_LEG -> state.setAndContinue(SHAKE_LEG);
                case LOOSEN_HINDLEG -> state.setAndContinue(LOOSEN_HINDLEG);
                case FLEX_CLAWS -> state.setAndContinue(FLEX_CLAWS);
                case IDLE -> state.setAndContinue(this.isHuntingChargeActive() ? RUN : state.isMoving() ? WALK : IDLE);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private boolean isAmbientAction() {
        return this.getAction() == Action.LOOK_AROUND || this.getAction() == Action.GROOM_CLAWS
                || this.getAction() == Action.STRETCH || this.getAction() == Action.INSPECT_GROUND
                || this.getAction() == Action.SHAKE_LEG || this.getAction() == Action.LOOSEN_HINDLEG
                || this.getAction() == Action.FLEX_CLAWS;
    }

    private final class SpiderMotherAmbientGoal extends Goal {
        private SpiderMotherAmbientGoal() { this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override public boolean canUse() {
            LivingEntity target = SpiderMotherEntity.this.getTarget();
            return SpiderMotherEntity.this.isAlive() && (target == null || !target.isAlive())
                    && SpiderMotherEntity.this.getAction() == Action.IDLE
                    && SpiderMotherEntity.this.getRagePhase() == RagePhase.NORMAL
                    && SpiderMotherEntity.this.combatCooldown == 0 && SpiderMotherEntity.this.ambientCooldown == 0
                    && SpiderMotherEntity.this.onGround() && !SpiderMotherEntity.this.isInWater()
                    && !SpiderMotherEntity.this.isInLava() && SpiderMotherEntity.this.getNavigation().isDone()
                    && SpiderMotherEntity.this.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
        }

        @Override public boolean canContinueToUse() {
            LivingEntity target = SpiderMotherEntity.this.getTarget();
            return SpiderMotherEntity.this.isAlive() && SpiderMotherEntity.this.isAmbientAction()
                    && (target == null || !target.isAlive()) && SpiderMotherEntity.this.onGround()
                    && !SpiderMotherEntity.this.isInWater() && !SpiderMotherEntity.this.isInLava();
        }

        @Override public void start() {
            int last = SpiderMotherEntity.this.lastAmbientVariant;
            int variant = SpiderMotherEntity.this.random.nextInt(last < 0 ? 7 : 6);
            if (last >= 0 && variant >= last) variant++;
            SpiderMotherEntity.this.lastAmbientVariant = variant;
            SpiderMotherEntity.this.getNavigation().stop();
            SpiderMotherEntity.this.setAction(switch (variant) {
                case 0 -> Action.LOOK_AROUND;
                case 1 -> Action.GROOM_CLAWS;
                case 2 -> Action.STRETCH;
                case 3 -> Action.INSPECT_GROUND;
                case 4 -> Action.SHAKE_LEG;
                case 5 -> Action.LOOSEN_HINDLEG;
                default -> Action.FLEX_CLAWS;
            });
        }

        @Override public void stop() {
            if (SpiderMotherEntity.this.isAmbientAction()) SpiderMotherEntity.this.setAction(Action.IDLE);
            SpiderMotherEntity.this.ambientCooldown = 160 + SpiderMotherEntity.this.random.nextInt(201);
        }
    }

    private final class SpiderMotherCombatGoal extends Goal {
        private SpiderMotherCombatGoal() { this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        private Action nearAttack(int index) {
            return switch (index) { case 0 -> Action.SLASH; case 1 -> Action.THRUST;
                case 2 -> Action.SMASH; default -> Action.PIN_CUT; };
        }

        @Override public boolean canUse() {
            LivingEntity target = SpiderMotherEntity.this.getTarget();
            if (SpiderMotherEntity.this.isAmbientAction()) {
                return target != null && target.isAlive() && SpiderMotherEntity.this.canAttack(target);
            }
            return SpiderMotherEntity.this.getAction() != Action.IDLE || SpiderMotherEntity.this.isHuntingChargeActive()
                    || target != null && target.isAlive() && SpiderMotherEntity.this.canAttack(target);
        }
        @Override public boolean canContinueToUse() { return this.canUse(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override public void tick() {
            SpiderMotherEntity m = SpiderMotherEntity.this;
            LivingEntity target = m.getTarget();
            ServerLevel level = (ServerLevel)m.level();
            // No secondary skill, retarget, or rage transition may skip a recovery action.
            if (m.getAction() != Action.IDLE) {
                if (m.getAction() == Action.GUARD && target != null && target.isAlive()) m.guardAround(level, target);
                else m.getNavigation().stop();
                return;
            }
            if (m.isHuntingChargeActive()) {
                m.updateAttackFacing();
                if (target == null || !target.isAlive() || m.horizontalCollision
                        || m.getNavigation().isDone() || m.isInFront(target, .6) && m.distanceToSqr(target) <= 3.7 * 3.7) {
                    m.finishHuntingChargeWithSmash();
                }
                return;
            }
            if (m.combatCooldown > 0) { m.getNavigation().stop(); return; }
            if (m.getRagePhase() == RagePhase.FATIGUED) { m.enterFatigue(); return; }
            if (target == null || !target.isAlive() || !m.isCombatTarget(target)) {
                m.pendingRage = false;
                return;
            }
            if (m.pendingPhaseTransition || m.pendingRage && m.rageCooldown == 0 && !m.isEnraged()) {
                m.startRageWindup();
                return;
            }
            // Keep the victim mobile before any new attack after a silk hit.
            if (!m.silkBindings.isEmpty()) { m.beginRecovery(10); return; }
            double distance = m.distanceToSqr(target);
            if (m.specialCooldown == 0 && !m.isEnraged() && m.roundsSinceSupport >= 3 && m.entranceSpiderlingsRemaining == 0) {
                if ((m.phaseHatchPending || m.hatchCooldown == 0) && m.countOwnedUnits(level) < MAX_SPIDERLINGS) {
                    m.roundsSinceSupport = 0;
                    m.startAction(Action.HATCH_BEGIN); return;
                }
                if (m.eidolonCooldown == 0 && m.hasOwnedSpiderlings(level)) {
                    m.roundsSinceSupport = 0;
                    m.startAction(Action.EIDOLON); return;
                }
            }
            if (m.specialCooldown == 0) {
                if (!m.wallReinforcementSent && m.highGroundTicks >= WALL_REINFORCEMENT_TICKS
                        && m.isHighGroundTarget(target) && m.spawnWallReinforcement(level, target)) {
                    m.wallReinforcementSent = true;
                    m.specialCooldown = 80;
                    m.beginRecovery(20);
                    return;
                }
                if (m.pendingVenomPool && m.isHighGroundTarget(target)) { m.startSpit(SpitMode.VENOM_POOL); return; }
                if (!m.silkAttempted && !m.silkInFlight && m.blockedApproachTicks >= SILK_TRIGGER_TICKS) {
                    m.startSpit(SpitMode.SILK); return;
                }
                if (!m.nestQuakeSent && m.highGroundTicks >= NEST_QUAKE_TICKS) { m.startNestQuake(target); return; }
            }
            if (m.rearPressureTicks >= 40 && m.sweepCooldown == 0) {
                m.sweepCooldown = 160;
                m.rearPressureTicks = 0;
                m.startCombatRound(Action.SWEEP, false);
                return;
            }
            m.getLookControl().setLookAt(target, 10, 10);
            if (!m.isInFront(target, .3)) {
                // Reposition visibly instead of snapping a damaging hit through the player.
                if (m.tickCount % 10 == 0) m.getNavigation().moveTo(target, .85);
                return;
            }
            if (m.pursuitCooldown == 0 && m.onGround() && m.hasLineOfSight(target)) {
                if (distance >= 10 * 10 && distance <= 18 * 18 && m.pounceCooldown == 0) {
                    m.pounceCooldown = 240;
                    m.pursuitCooldown = 80;
                    m.startCombatRound(Action.POUNCE_WINDUP, false);
                    return;
                }
                if (distance >= HUNTING_CHARGE_MIN_RANGE * HUNTING_CHARGE_MIN_RANGE
                        && distance <= HUNTING_CHARGE_MAX_RANGE * HUNTING_CHARGE_MAX_RANGE
                        && m.huntingChargeCooldown == 0) {
                    Path path = m.getNavigation().createPath(target, 1);
                    if (path != null && path.canReach()) { m.startHuntingCharge(target); return; }
                }
            }
            if (distance <= 4.2 * 4.2) {
                if (!m.isEnraged() && m.guardCooldown == 0 && m.random.nextInt(10) == 0) {
                    m.guardTicks = 16;
                    m.guardCooldown = 8 * 20;
                    m.guardDirection = m.random.nextBoolean() ? 1 : -1;
                    m.startAction(Action.GUARD);
                    return;
                }
                Action choice;
                if (distance > 3.4 * 3.4) choice = Action.THRUST;
                else {
                    int count = m.forcedHatchTriggered ? 4 : 3;
                    int index = m.isEnraged() && m.random.nextFloat() < .65F ? 0 : m.random.nextInt(count);
                    choice = this.nearAttack(index);
                    if (choice == m.lastAttack) choice = this.nearAttack((index + 1) % count);
                }
                m.startCombatRound(choice, choice == Action.SLASH && m.random.nextFloat() < (m.isEnraged() ? 1.0F : m.forcedHatchTriggered ? .85F : .65F));
            } else if (distance <= 6 * 6 && m.tickCount % 10 == 0) {
                m.getNavigation().moveTo(target, 1.0);
            } else if (distance > 6 * 6 && distance <= 14 * 14 && m.hasLineOfSight(target) && !m.isEnraged()) {
                m.queuedFollowup = Action.IDLE;
                m.startSpit(SpitMode.NORMAL);
            } else if (m.tickCount % 10 == 0) m.getNavigation().moveTo(target, m.isEnraged() ? 1.15 : 1.0);
        }
    }

    private void startCombatRound(Action action, boolean combo) {
        this.comboDepth = 0;
        this.lastAttack = action;
        this.queuedFollowup = combo ? (this.isEnraged() ? Action.THRUST : Action.SMASH) : Action.IDLE;
        this.startAction(action);
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
        RAGE_ROAR(8), THRUST(9), SWEEP(10), PIN_CUT(11), CHARGE_WINDUP(12),
        POUNCE_WINDUP(13), POUNCE_AIR(14), POUNCE_LAND(15), RECOVER(16),
        STAGGER(17), EXHAUSTED(18), GUARD(19), LOOK_AROUND(20), GROOM_CLAWS(21), STRETCH(22),
        INSPECT_GROUND(23), SHAKE_LEG(24), LOOSEN_HINDLEG(25), FLEX_CLAWS(26);

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
