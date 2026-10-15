package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative floating encounter. Deliberately egg-only during encounter testing. */
public final class RisingClockOracleEntity extends Monster implements GeoEntity {
    public static final int IDLE = 0, LIGHTNING = 1, GRAVITY = 2, AWAKEN = 3;
    public static final int SHIELD = 4, BLAST = 5, FLAME = 6, GLIDE = 7, CHRONO = 8, REPULSOR = 9;
    public static final int COFFIN = 10;
    public static final int CHANNEL_WINDUP = 5, BOLT_WINDUP = 6, BEAM_LOCK_TICKS = 10;
    public static final int CHANNEL_RECOVERY = 4, SHORT_RECOVERY = 6, SPELL_GAP = 3, DEATH_DURATION = 50;
    public static final double AGGRO_RANGE = 64;
    public static final float MIRROR_HEALTH = 125;
    public static final int MIRROR_VANISH_TICKS = 10;
    public static final int MAX_RAGE = 8;
    public static final int EMPOWERED_BLAST_COUNT = 5, BLAST_INTERVAL = 10;
    public static final double GLIDE_TRIGGER_RANGE = 6;
    public static final int GLIDE_WINDUP = 10, GLIDE_MOVEMENT_TICKS = 30, GLIDE_COOLDOWN = 160;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> MIRROR = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> VANISHING = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RAGE = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> EMPOWERED = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FINAL_CAST = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WARD_MASK = SynchedEntityData.defineId(RisingClockOracleEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle_hover");
    private static final RawAnimation[] IDLE_GESTURES = {
            RawAnimation.begin().thenPlayAndHold("idle_survey"),
            RawAnimation.begin().thenPlayAndHold("idle_staff_inspect"),
            RawAnimation.begin().thenPlayAndHold("idle_wing_stretch"),
            RawAnimation.begin().thenPlayAndHold("idle_contemplate")
    };
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly_glide");
    private static final RawAnimation THUNDER = RawAnimation.begin().thenPlayAndHold("cast_skyfall");
    private static final RawAnimation LIFT = RawAnimation.begin().thenPlayAndHold("cast_gravity");
    private static final RawAnimation BLACK_COFFIN = RawAnimation.begin().thenPlayAndHold("cast_black_coffin");
    private static final RawAnimation ENTRANCE = RawAnimation.begin().thenPlayAndHold("boss_awaken");
    private static final RawAnimation DEFEAT = RawAnimation.begin().thenPlayAndHold("boss_death");
    private static final RawAnimation WARD = RawAnimation.begin().thenPlayAndHold("cast_ward");
    private static final RawAnimation BOLT = RawAnimation.begin().thenPlayAndHold("cast_bolt");
    private static final RawAnimation CHANNEL_START = RawAnimation.begin().thenPlayAndHold("channel_start");
    private static final RawAnimation CHANNEL_LOOP = RawAnimation.begin().thenLoop("channel_loop");
    private static final RawAnimation CHANNEL_END = RawAnimation.begin().thenPlayAndHold("channel_end");
    private static final RawAnimation TO_FLY = RawAnimation.begin().thenPlayAndHold("hover_to_fly");
    private static final RawAnimation TO_HOVER = RawAnimation.begin().thenPlayAndHold("fly_to_hover");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final List<OracleSpellEntity> spells = new ArrayList<>();
    private final List<OracleArcaneEntity> arcana = new ArrayList<>();
    private final OracleCombatBrain combat = new OracleCombatBrain(this);
    private ServerBossEvent bossEvent;
    private int cooldown = SPELL_GAP;
    private boolean awakened;
    private double hoverY;
    private int hoverRefresh;
    private int shieldCooldown;
    private OracleCoffinEntity coffin;
    private boolean finalCastUsed, restoreFinalCast;
    private UUID finalKillerId, finalPlayerCredit;
    private Vec3 finalCoffinOrigin, finalVictimAnchor;
    private int savedFinalAge;
    private DamageSource finalDamageSource;
    private ResourceKey<DamageType> finalDamageType = DamageTypes.GENERIC;
    private boolean castReleased, castRecovering;
    private int blastsReleased;
    private LivingEntity castTarget;
    private Vec3 glideDirection = Vec3.ZERO;
    private int glideCooldown;
    private BlockPos lastGlideFire;
    private final Map<Integer, Long> firePatchHits = new HashMap<>();
    private boolean mirrorPhaseUsed;
    private float remainingMirrorHealth;
    private long mirrorReturnAt;
    private int mirrorRetryDelay;
    private RisingClockOracleEntity mirror, mirrorOwner;
    // Presentation only: start a fresh quiet period after movement, damage or casting.
    // All gestures share idle_hover's 96-tick period and its endpoint pose/velocity.
    private long idleAnimationStart = -1;

    public RisingClockOracleEntity(EntityType<? extends RisingClockOracleEntity> type, Level level) {
        super(type, level); setNoGravity(true); setPersistenceRequired(); xpReward = 150; bossEvent = createBossEvent();
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500).add(Attributes.ARMOR, 6)
                .add(Attributes.ARMOR_TOUGHNESS, 2).add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, .26).add(Attributes.FOLLOW_RANGE, AGGRO_RANGE).add(Attributes.ATTACK_DAMAGE, 12)
                .add(Attributes.MAX_ABSORPTION, 50);
    }
    @Override protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(
                this, Player.class, false, (target, level) -> validTarget(target)) {
            { targetConditions.ignoreLineOfSight().ignoreInvisibilityTesting(); }
        });
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<LivingEntity>(
                this, LivingEntity.class, false, (target, level) -> validTarget(target)) {
            { targetConditions.ignoreLineOfSight().ignoreInvisibilityTesting(); }
        });
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data); data.define(ACTION, IDLE); data.define(ACTION_START, 0L);
        data.define(MIRROR,false); data.define(VANISHING,false);
        data.define(RAGE,0);
        data.define(EMPOWERED,false);
        data.define(FINAL_CAST,false);
        data.define(WARD_MASK,0);
    }
    public boolean isFinalCasting() { return entityData.get(FINAL_CAST); }
    // Actual health remains zero. Delay vanilla's dying state until the last spell settles.
    @Override public boolean isAlive() { return !isRemoved() && (isFinalCasting() || super.isAlive()); }
    @Override public boolean isDeadOrDying() { return !isFinalCasting() && super.isDeadOrDying(); }
    @Override public void heal(float amount) { if (!isFinalCasting()) super.heal(amount); }
    public int rage() { return entityData.get(RAGE); }
    public boolean empowered() { return entityData.get(EMPOWERED); }
    private void beginEmpowerment(int action) {
        boolean enhanced = !isMirror() && rage() == MAX_RAGE && action != FLAME && action != GLIDE && action != COFFIN;
        entityData.set(EMPOWERED,enhanced);
        if (enhanced) entityData.set(RAGE,0);
    }
    void recordSpellHit(LivingEntity victim) {
        // Damage Post runs after death. getTarget() filters out a dead target, so use
        // the stored lock to credit lethal hits without counting unrelated victims.
        if (!level().isClientSide() && !isMirror() && !isFinalCasting() && isAlive() && !isRemoved()
                && victim == getTargetUnchecked() && rage() < MAX_RAGE) {
            int charge = rage() + 1;
            entityData.set(RAGE, charge);
            // One clear ascending chime per newly lit wing; loading or spending rage is silent.
            float pitch = switch (charge) {
                case 1 -> .75F; case 2 -> .841F; case 3 -> .945F; case 4 -> 1F;
                case 5 -> 1.122F; case 6 -> 1.26F; case 7 -> 1.414F; default -> 1.5F;
            };
            playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), charge == MAX_RAGE ? .8F : .6F, pitch);
        }
    }
    public boolean isMirror() { return entityData.get(MIRROR); }
    public boolean isVanishing() { return entityData.get(VANISHING); }
    @Override public boolean isInvisible() { return isVanishing() || super.isInvisible(); }
    @Override public boolean isPickable() { return !isVanishing() && super.isPickable(); }
    @Override public boolean isPushable() { return !isVanishing() && super.isPushable(); }
    @Override public boolean isCurrentlyGlowing() { return !isVanishing() && (isMirror() || super.isCurrentlyGlowing()); }
    @Override public LivingEntity getTarget() {
        if (isFinalCasting()) return castTarget;
        return isMirror() ? mirrorOwner != null && mirrorOwner.isAlive() ? mirrorOwner.getTarget() : null : super.getTarget();
    }
    @Override public void setTarget(LivingEntity target) {
        if (isFinalCasting()) return;
        if (isMirror()) target = getTarget();
        else if (action()!=IDLE && castTarget!=null && castTarget!=target && !castTarget.isRemoved()
                && castTarget.level()==level() && validTarget(castTarget) && distanceToSqr(castTarget)<=AGGRO_RANGE*AGGRO_RANGE)
            return;
        super.setTarget(target);
        if (!isMirror() && mirror != null && !mirror.isRemoved()) mirror.syncMirrorTarget();
    }
    public int action() { return entityData.get(ACTION); }
    OracleCombatBrain combatBrain() { return combat; }
    RisingClockOracleEntity combatLeader() { return isMirror() && mirrorOwner!=null ? mirrorOwner : this; }
    RisingClockOracleEntity combatPartner() { return isMirror() ? mirrorOwner : mirror!=null && !mirror.isRemoved() ? mirror : null; }
    void controlApplied(int remainingTicks) { combat.controlApplied(remainingTicks); }
    public float actionAge(float partialTick) { return Math.max(0, level().getGameTime() - entityData.get(ACTION_START)) + partialTick; }
    private void setAction(int action) {
        entityData.set(ACTION_START, level().getGameTime()); entityData.set(ACTION, action);
        castRecovering = false;
        if (action == IDLE) entityData.set(EMPOWERED,false);
    }
    List<LivingEntity> nearbyEnemies(double radius) {
        return level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius),
                e -> validTarget(e) && distanceToSqr(e) <= radius*radius);
    }
    public int wardMask() { return entityData.get(WARD_MASK); }
    public int wardCount() { return Integer.bitCount(wardMask()); }
    void grantWard(boolean enhanced) {
        if (!level().isClientSide() && isAlive() && !isFinalCasting()) {
            // Fixed orbit slots: two opposite shields, or all four. Losing one never
            // makes the surviving shields jump to new positions around the caster.
            entityData.set(WARD_MASK,enhanced ? 15 : 5);
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.8F,1.2F);
        }
    }
    boolean absorbWardHit(DamageSource source, float damage) {
        int mask = wardMask();
        if (mask == 0 || damage <= 0 || !isAlive() || isFinalCasting() || !(level() instanceof ServerLevel server)) return false;
        int remaining = mask & (mask - 1);
        entityData.set(WARD_MASK,remaining);
        if (source.getEntity() instanceof LivingEntity) combat.hurt(damage);
        if (remaining == 0) OracleArcaneEntity.createWardPush(server,this,nearbyEnemies(6));
        else playSound(SoundEvents.AMETHYST_BLOCK_BREAK,.8F,1.2F);
        return true;
    }
    private ServerBossEvent createBossEvent() { return new ServerBossEvent(getUUID(), getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS); }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (!isMirror()) { bossEvent.setProgress(getHealth() / getMaxHealth()); bossEvent.setName(getDisplayName()); }
        if (isFinalCasting()) {
            getNavigation().stop(); setDeltaMovement(Vec3.ZERO);
            if (getHealth()!=0) setHealth(0);
            if (restoreFinalCast) restoreFinalCoffin(server);
            // Entity/player loading order can leave the saved killer absent on the first tick.
            if (castTarget==null && finalKillerId!=null && tickCount%5==0 && coffin!=null && coffin.channeling()
                    && server.getEntity(finalKillerId) instanceof LivingEntity killer && killer.isAlive()) {
                castTarget=killer;
                finalDamageSource=new DamageSource(finalDamageSource.typeHolder(),killer);
                coffin.resumeVictim(killer);
            }
            // A removed/cancelled effect or a failed spawn must never leave an immortal boss.
            if (isFinalCasting() && (coffin==null || coffin.isRemoved() || coffin.dissolveAt()<OracleCoffinEntity.FINAL_STRIKE || actionAge(0)>OracleCoffinEntity.FINAL_STRIKE+5)) finishFinalCoffin();
            return;
        }
        if (isMirror()) {
            if (mirrorOwner == null || mirrorOwner.isRemoved() || !mirrorOwner.isAlive() || mirrorOwner.level() != server) { discard(); return; }
            syncMirrorTarget();
            if (mirrorOwner.isNoAi()) { cancelSpells(); setAction(IDLE); setDeltaMovement(Vec3.ZERO); return; }
        } else if (isAlive()) {
            if (!isNoAi() && !mirrorPhaseUsed && getHealth() < getMaxHealth()*.5F) beginMirrorPhase();
            if (isVanishing()) {
                setDeltaMovement(Vec3.ZERO);
                if (!isNoAi() && server.getGameTime() >= mirrorReturnAt) {
                    entityData.set(VANISHING,false); cooldown = SPELL_GAP;
                    ensureMirror(server);
                    playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE,1.1F,.9F);
                } else return;
            } else if (!isNoAi()) ensureMirror(server);
        }
        // Weather is shared by the save in 26.1. Use the same public API as /weather;
        // renew only when needed, and leave normal weather cycling in charge after the encounter.
        if (!isMirror() && isAlive() && (tickCount == 1 || tickCount % 20 == 0)) {
            var weather = server.getWeatherData();
            if (!weather.isRaining() || !weather.isThundering() || weather.getClearWeatherTime() > 0
                    || weather.getRainTime() < 200 || weather.getThunderTime() < 200)
                server.getServer().setWeatherParameters(0, 600, true, true);
        }
        spells.removeIf(OracleSpellEntity::isRemoved);
        arcana.removeIf(OracleArcaneEntity::isRemoved);
        if (shieldCooldown > 0) shieldCooldown--;
        if (glideCooldown > 0) glideCooldown--;
        if (coffin != null && coffin.isRemoved()) coffin = null;
        if (!firePatchHits.isEmpty() && tickCount%20 == 0) firePatchHits.values().removeIf(time -> server.getGameTime()-time >= 10);
        if (!isAlive() || isNoAi()) { cancelSpells(); setDeltaMovement(Vec3.ZERO); if (action() != IDLE) setAction(IDLE); return; }
        setNoGravity(true);
        if (!awakened) { awakened = true; setAction(AWAKEN); playSound(SoundEvents.BEACON_ACTIVATE, 1.1F, .7F); }
        LivingEntity target = getTarget();
        if (!isMirror() && target != null && (!validTarget(target) || distanceToSqr(target) > AGGRO_RANGE * AGGRO_RANGE)) { setTarget(null); target = null; }
        if (--hoverRefresh <= 0) {
            BlockPos ground = OracleSpellEntity.findSurface(server, blockPosition(), 12);
            hoverY = ground == null ? getY() : ground.getY() + 1.65;
            hoverRefresh = 10;
        }
        if (target != null) {
            Vec3 delta = target.position().subtract(position());
            float yaw = (float) (Mth.atan2(delta.z, delta.x) * 180 / Math.PI) - 90;
            if (action()!=REPULSOR || actionAge(0)<BEAM_LOCK_TICKS) setYRot(Mth.rotLerp(.16F, getYRot(), yaw));
            setYHeadRot(getYRot()); yBodyRot = getYRot();
        }
        if (target != null && action()!=AWAKEN && action()!=GLIDE && action()!=COFFIN && glideCooldown==0
                && combat.canStart(server.getGameTime()) && validTarget(target)
                && distanceToSqr(target)<=GLIDE_TRIGGER_RANGE*GLIDE_TRIGGER_RANGE && hasLineOfSight(target)) {
            // Interrupt channeled magic; already released clocks, missiles and ground fire keep running.
            for (OracleSpellEntity spell : spells) if (spell.kind()==OracleSpellEntity.GRAVITY && !spell.isRemoved()) spell.cancel();
            for (OracleArcaneEntity spell : arcana) if (spell.kind()==OracleArcaneEntity.FLAME || spell.kind()==OracleArcaneEntity.REPULSOR) spell.stopChannel();
            beginArcaneCast(server,target,GLIDE);
        }
        if (action() != IDLE) {
            if (action() >= SHIELD) {
                recoverIfTargetLost();
                tickArcaneCast(server);
                // Initial beam/flame damage can kill on the very frame the effect is created.
                recoverIfTargetLost();
            }
            else setDeltaMovement(getDeltaMovement().scale(.7));
            if (actionAge(0) >= castDuration()) {
                setAction(IDLE); castTarget = null; cooldown = SPELL_GAP;
            }
            return;
        }
        Vec3 desired = new Vec3(0, Mth.clamp((hoverY - getY()) * .08, -.11, .11), 0);
        if (isMirror() && target == null) {
            Vec3 home = mirrorOwner.position().subtract(position());
            if (home.lengthSqr() > 6*6) desired = home.normalize().scale(.16);
        }
        if (target != null) {
            Vec3 toward = target.position().subtract(position()).multiply(1,0,1);
            double distance = toward.length();
            if (distance > .01) {
                Vec3 forward = toward.scale(1 / distance);
                desired = desired.add(forward.scale(distance > 12 ? .14 : distance < 6 ? -.11 : 0));
                if (distance >= 6 && distance <= 15) desired = desired.add(forward.z * .055, 0, -forward.x * .055);
            }
            if (cooldown > 0) cooldown--;
            if (cooldown == 0 && distanceToSqr(target) <= AGGRO_RANGE * AGGRO_RANGE) {
                int chosen = combat.choose(server,target,shieldCooldown==0);
                if (chosen == IDLE) cooldown = SPELL_GAP;
                else if (chosen == LIGHTNING || chosen == GRAVITY) beginCast(server,target,chosen == GRAVITY);
                else beginArcaneCast(server,target,chosen);
            }
        }
        if (horizontalCollision) desired = desired.add(0, .08, 0);
        setDeltaMovement(getDeltaMovement().lerp(desired, .18));
    }
    private void beginMirrorPhase() {
        if (isMirror() || mirrorPhaseUsed || !isAlive()) return;
        mirrorPhaseUsed = true; remainingMirrorHealth = MIRROR_HEALTH;
        mirrorReturnAt = level().getGameTime()+MIRROR_VANISH_TICKS;
        entityData.set(VANISHING,true); awakened = true;
        cancelSpells(); setAction(IDLE); getNavigation().stop(); setDeltaMovement(Vec3.ZERO);
        playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE,1.1F,1.25F);
    }
    private void syncMirrorTarget() {
        LivingEntity target = getTarget();
        if (super.getTargetUnchecked() != target) super.setTarget(target);
        // The same cast recovery handles both bodies. Released missiles, clocks and
        // ground spells have their own lifetimes even when the owner's lock changes.
    }
    private void ensureMirror(ServerLevel server) {
        if (!mirrorPhaseUsed || remainingMirrorHealth <= 0) return;
        if (mirror != null && !mirror.isRemoved()) {
            remainingMirrorHealth = mirror.getHealth();
            return;
        }
        if (mirrorRetryDelay > 0) { mirrorRetryDelay--; return; }
        RisingClockOracleEntity copy = ModEntities.RISING_CLOCK_ORACLE.get().create(server,EntitySpawnReason.TRIGGERED);
        if (copy == null) { mirrorRetryDelay = 20; return; }
        copy.entityData.set(MIRROR,true); copy.mirrorOwner = this; copy.awakened = true;
        copy.mirrorPhaseUsed = true; copy.targetSelector.removeAllGoals(goal -> true);
        copy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MIRROR_HEALTH);
        copy.setHealth(remainingMirrorHealth); copy.setGlowingTag(true); copy.xpReward = 0;
        copy.setYRot(getYRot()); copy.yBodyRot = getYRot(); copy.setYHeadRot(getYRot());
        copy.cooldown = SPELL_GAP+OracleCombatBrain.PARTNER_STAGGER;
        copy.combat.delayStart(OracleCombatBrain.PARTNER_STAGGER);
        copy.syncMirrorTarget();
        Vec3 spawn = mirrorPosition(server,copy);
        copy.setPos(spawn);
        if (server.addFreshEntity(copy)) mirror = copy;
        else { copy.discard(); mirrorRetryDelay = 20; }
    }
    private Vec3 mirrorPosition(ServerLevel server, RisingClockOracleEntity copy) {
        // A bounded search in already loaded space; no ground scans, terrain edits or forced chunks.
        double yaw = getYRot()*Math.PI/180;
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = yaw+(attempt%8)*Math.PI/4;
            double radius = attempt < 8 ? 4 : 2;
            Vec3 candidate = position().add(Math.cos(angle)*radius,attempt < 8 ? 0 : 2,Math.sin(angle)*radius);
            copy.setPos(candidate);
            if (server.hasChunkAt(copy.blockPosition()) && server.noCollision(copy)) return candidate;
        }
        // The caster already occupies valid space. A cramped room must not erase the phase.
        return position();
    }
    private void removeMirror() {
        if (mirror != null) {
            remainingMirrorHealth = mirror.getHealth();
            if (!mirror.isRemoved()) mirror.discard();
            mirror = null;
        }
    }
    @Override public boolean hurtServer(ServerLevel server, DamageSource source, float damage) {
        if (isFinalCasting()) return false;
        // Environmental protection only: poison, wither and combat effects keep their normal behavior.
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_DROWNING)) return false;
        if (isVanishing() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        float before = getHealth()+getAbsorptionAmount();
        boolean hit = super.hurtServer(server,source,damage);
        if (hit && source.getEntity() instanceof LivingEntity)
            combat.hurt(Math.max(0,before-getHealth()-getAbsorptionAmount()));
        if (isMirror()) {
            if (mirrorOwner != null) mirrorOwner.remainingMirrorHealth = getHealth();
        } else if (hit && !isFinalCasting() && isAlive() && !isNoAi() && getHealth() < getMaxHealth()*.5F) beginMirrorPhase();
        return hit;
    }
    public static int castWindup(int action) {
        return action==REPULSOR ? BEAM_LOCK_TICKS : action==BLAST ? BOLT_WINDUP : CHANNEL_WINDUP;
    }
    public static int castDuration(int action) {
        return castDuration(action,false);
    }
    public int castDuration() { return castDuration(action(),empowered()); }
    public static int castDuration(int action, boolean enhanced) {
        return switch(action) {
            case COFFIN -> OracleCoffinEntity.DURATION;
            // Nine bolts finish at tick36. Keep every telegraph/impact, then recover in six ticks.
            case LIGHTNING -> OracleSpellEntity.LIGHTNING_IMPACT + (enhanced ? 11 : 8)*2 + SHORT_RECOVERY;
            case GRAVITY -> OracleSpellEntity.IMPACT; case AWAKEN -> 60;
            case SHIELD -> CHANNEL_WINDUP + SHORT_RECOVERY;
            case BLAST -> BOLT_WINDUP + (enhanced ? (EMPOWERED_BLAST_COUNT-1)*BLAST_INTERVAL : 0) + SHORT_RECOVERY;
            case FLAME -> CHANNEL_WINDUP + OracleArcaneEntity.FLAME_DURATION + CHANNEL_RECOVERY;
            case GLIDE -> GLIDE_WINDUP + GLIDE_MOVEMENT_TICKS + CHANNEL_RECOVERY;
            case CHRONO -> CHANNEL_WINDUP + CHANNEL_RECOVERY;
            case REPULSOR -> BEAM_LOCK_TICKS + (enhanced ? OracleArcaneEntity.EMPOWERED_BEAM_TICKS : OracleArcaneEntity.REPULSOR_CHANNEL_TICKS) + CHANNEL_RECOVERY; default -> 80;
        };
    }
    private void beginArcaneCast(ServerLevel server, LivingEntity target, int action) {
        setAction(action); beginEmpowerment(action); castTarget = target; castReleased = false; blastsReleased = 0; setDeltaMovement(Vec3.ZERO);
        combat.started(action,target,empowered());
        if (action == SHIELD) shieldCooldown = 240;
        if (action == GLIDE) {
            Vec3 away = position().subtract(target.position()).multiply(1,0,1);
            if (away.horizontalDistanceSqr()<.001) away = new Vec3(Math.sin(getYRot()*Math.PI/180),0,-Math.cos(getYRot()*Math.PI/180));
            glideDirection = away.normalize().scale(.48);
            glideCooldown = GLIDE_COOLDOWN; lastGlideFire = null;
        }
        playSound(SoundEvents.BEACON_POWER_SELECT,.7F,action == CHRONO ? .75F : 1.3F);
    }
    private boolean beginFinalCoffin(ServerLevel server, DamageSource source) {
        LivingEntity killer=source.getEntity() instanceof LivingEntity living ? living : getKillCredit();
        if (killer==null || killer==this || killer.isRemoved() || killer.level()!=server) return false;
        cancelSpells(); removeMirror(); remainingMirrorHealth=0; mirrorPhaseUsed=true;
        finalCastUsed=true; finalDamageSource=source; finalDamageType=source.typeHolder().unwrapKey().orElse(DamageTypes.GENERIC);
        finalKillerId=killer.getUUID(); finalVictimAnchor=killer.position();
        var credited=getLastHurtByPlayer(); finalPlayerCredit=credited==null?null:credited.getUUID();
        BlockPos floor=OracleSpellEntity.findSurface(server,killer.blockPosition(),32);
        double y=floor==null?killer.getY()-.015:floor.getY()+server.getBlockState(floor).getCollisionShape(server,floor).max(net.minecraft.core.Direction.Axis.Y)+.015;
        if (killer.getBoundingBox().maxY>=y+OracleCoffinEntity.HEIGHT) y=killer.getY()-1;
        finalCoffinOrigin=new Vec3(killer.getX(),y,killer.getZ());
        entityData.set(VANISHING,false); entityData.set(FINAL_CAST,true); entityData.set(EMPOWERED,false);
        setAction(COFFIN); setHealth(0); castTarget=killer;
        getNavigation().stop(); setDeltaMovement(Vec3.ZERO);
        Vec3 delta=killer.position().subtract(position());
        setYRot((float)(Mth.atan2(delta.z,delta.x)*180/Math.PI)-90); yBodyRot=getYRot(); setYHeadRot(getYRot());
        coffin=OracleCoffinEntity.create(server,this,finalCoffinOrigin,killer,finalVictimAnchor,0);
        if (coffin==null) { entityData.set(FINAL_CAST,false); return false; }
        var chant=Component.translatable("message.theaurorian2.oracle_final_coffin").withColor(0xB995EA);
        for(ServerPlayer player:server.players()) if(player==killer || player.distanceToSqr(this)<=96*96 || player.distanceToSqr(killer)<=64*64) player.sendSystemMessage(chant);
        return true;
    }
    private void restoreFinalCoffin(ServerLevel server) {
        restoreFinalCast=false;
        LivingEntity killer=finalKillerId!=null && server.getEntity(finalKillerId) instanceof LivingEntity living?living:null;
        castTarget=killer;
        var damageTypes=server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
        finalDamageSource=new DamageSource(damageTypes.get(finalDamageType).orElseGet(()->damageTypes.getOrThrow(DamageTypes.GENERIC)),killer);
        if (finalPlayerCredit!=null) setLastHurtByPlayer(finalPlayerCredit,100);
        entityData.set(ACTION_START,server.getGameTime()-savedFinalAge);
        if (finalCoffinOrigin!=null && finalVictimAnchor!=null && savedFinalAge<OracleCoffinEntity.FINAL_STRIKE)
            coffin=OracleCoffinEntity.create(server,this,finalCoffinOrigin,killer,finalVictimAnchor,savedFinalAge);
    }
    void finishFinalCoffin() {
        if (!isFinalCasting() || !(level() instanceof ServerLevel)) return;
        // Detach the already-broken cage before ordinary death cleanup, leaving its shards visible.
        OracleCoffinEntity ending=coffin; coffin=null;
        if (ending!=null) ending.releaseVictim();
        entityData.set(FINAL_CAST,false); setHealth(0);
        DamageSource source=finalDamageSource==null?damageSources().generic():finalDamageSource;
        die(source);
    }
    /** Server-space casting origin at the staff side, ahead of the caster's collision box. */
    public Vec3 spellOrigin() {
        double yaw = getYRot()*Math.PI/180;
        return position().add(-Math.sin(yaw)*1.1-Math.cos(yaw)*.75,1.9,Math.cos(yaw)*1.1-Math.sin(yaw)*.75);
    }
    private Vec3 repulsorOrigin() {
        // Staff-head ring in channel_loop, measured from the authored rig at the renderer's .8 scale.
        double yaw = getYRot()*Math.PI/180;
        return position().add(-Math.sin(yaw)*2.13-Math.cos(yaw)*.59,2.54,Math.cos(yaw)*2.13-Math.sin(yaw)*.59);
    }
    private boolean hasCastTarget() {
        return castTarget != null && !castTarget.isRemoved() && castTarget.level() == level()
                && validTarget(castTarget) && getTarget() == castTarget
                && distanceToSqr(castTarget) <= AGGRO_RANGE*AGGRO_RANGE;
    }
    boolean canContinueChannel(int action, LivingEntity target) {
        return action() == action && !castRecovering && castTarget == target && hasCastTarget();
    }
    private void recoverIfTargetLost() {
        boolean needsTarget = switch (action()) {
            case FLAME, REPULSOR -> true;
            case BLAST -> empowered() ? blastsReleased < EMPOWERED_BLAST_COUNT : !castReleased;
            case CHRONO -> !empowered() && !castReleased;
            default -> false;
        };
        if (!castRecovering && needsTarget && !hasCastTarget()) recoverCast();
    }
    private void recoverCast() {
        if (castRecovering) return;
        castRecovering = true;
        // Seek the existing recovery pose on both clients and server, without a new
        // action ID or replaying release frames. Only cancel effects still being channeled.
        int recovery = action() == BLAST ? SHORT_RECOVERY : CHANNEL_RECOVERY;
        int age = Math.max((int)actionAge(0),castDuration()-recovery);
        entityData.set(ACTION_START,level().getGameTime()-age);
        for (OracleArcaneEntity spell : arcana)
            if (!spell.isRemoved() && (spell.kind()==OracleArcaneEntity.FLAME || spell.kind()==OracleArcaneEntity.REPULSOR)) spell.stopChannel();
    }
    private void tickArcaneCast(ServerLevel server) {
        int age = (int)actionAge(0);
        setDeltaMovement(getDeltaMovement().scale(.65));
        if (castRecovering) return;
        if (action() == GLIDE) {
            if (age >= GLIDE_WINDUP && age < GLIDE_WINDUP+GLIDE_MOVEMENT_TICKS) {
                // move() performs ordinary collision handling; never teleport through terrain.
                setDeltaMovement(glideDirection.add(0,Mth.clamp((hoverY-getY())*.08,-.08,.12),0));
                if (age%2 == 0) {
                    BlockPos floor = OracleSpellEntity.findSurface(server,blockPosition(),5);
                    if (floor != null && !floor.equals(lastGlideFire)) {
                        OracleArcaneEntity.create(server,this,OracleArcaneEntity.FIRE_PATCH,Vec3.atBottomCenterOf(floor.above()).add(0,.015,0),null);
                        lastGlideFire = floor;
                    }
                }
            }
            return;
        }
        int release = castWindup(action());
        if (action()==BLAST && empowered()) {
            if (age >= release + blastsReleased*BLAST_INTERVAL && blastsReleased < EMPOWERED_BLAST_COUNT) {
                blastsReleased++;
                if (castTarget != null && validTarget(castTarget) && hasLineOfSight(castTarget)) {
                    OracleArcaneEntity.create(server,this,OracleArcaneEntity.BLAST,spellOrigin(),castTarget,true);
                    playSound(SoundEvents.BLAZE_SHOOT,.8F,.8F);
                } else recoverCast();
            }
            return;
        }
        if (castReleased || age < release) return;
        castReleased = true;
        if (action() == SHIELD) {
            OracleArcaneEntity.create(server,this,OracleArcaneEntity.SHIELD,position().add(0,.35,0),null,empowered()); return;
        }
        if (action()==CHRONO && empowered()) {
            OracleArcaneEntity.createRegionalClock(server,this,nearbyEnemies(AGGRO_RANGE)); return;
        }
        if (!hasCastTarget() || !hasLineOfSight(castTarget)) { recoverCast(); return; }
        if (action() == BLAST) {
            OracleArcaneEntity.create(server,this,OracleArcaneEntity.BLAST,spellOrigin(),castTarget);
            playSound(SoundEvents.BLAZE_SHOOT,.8F,.8F);
        } else if (action() == REPULSOR) {
            OracleArcaneEntity.create(server,this,OracleArcaneEntity.REPULSOR,repulsorOrigin(),castTarget,empowered());
            playSound(SoundEvents.BEACON_ACTIVATE,1.1F,.8F);
        } else if (action() == FLAME) {
            OracleArcaneEntity.create(server,this,OracleArcaneEntity.FLAME,spellOrigin(),castTarget);
        } else if (action() == CHRONO) {
            List<LivingEntity> targets = new ArrayList<>(); targets.add(castTarget);
            server.getEntitiesOfClass(LivingEntity.class,castTarget.getBoundingBox().inflate(7),
                    e -> e != castTarget && validTarget(e) && castTarget.distanceToSqr(e) <= 7*7 && hasLineOfSight(e)).stream()
                    .sorted(Comparator.comparingDouble(castTarget::distanceToSqr)).limit(3).forEach(targets::add);
            for (LivingEntity victim : targets) OracleArcaneEntity.create(server,this,OracleArcaneEntity.CLOCK,
                    victim.position().add(0,victim.getBbHeight()+.25,0),victim);
        }
    }
    void trackArcane(OracleArcaneEntity spell) { arcana.add(spell); }
    boolean canHitFirePatch(LivingEntity victim) {
        long now = level().getGameTime();
        Long last = firePatchHits.get(victim.getId());
        if (last != null && now-last < OracleArcaneEntity.FIRE_PULSE_TICKS) return false;
        firePatchHits.put(victim.getId(),now); return true;
    }
    void burn(LivingEntity victim) {
        for (OracleArcaneEntity spell : arcana) {
            if (!spell.isRemoved() && spell.kind()==OracleArcaneEntity.BURN && spell.targetId()==victim.getId()) { spell.refreshBurn(); return; }
        }
        OracleArcaneEntity.create((ServerLevel)level(),this,OracleArcaneEntity.BURN,victim.getBoundingBox().getCenter(),victim);
    }
    private void beginCast(ServerLevel server, LivingEntity target, boolean gravity) {
        boolean enhanced = !isMirror() && rage() == MAX_RAGE;
        BlockPos floor = OracleSpellEntity.findSurface(server, target.blockPosition(), 64);
        if (floor == null && !gravity) floor = target.blockPosition().below();
        if (floor == null) { cooldown = 20; return; }
        setAction(gravity ? GRAVITY : LIGHTNING); beginEmpowerment(action()); castTarget = target;
        combat.started(action(),target,enhanced);
        setDeltaMovement(Vec3.ZERO);
        if (gravity) {
            OracleSpellEntity field = OracleSpellEntity.create(server, this, OracleSpellEntity.GRAVITY, floor, 0, enhanced);
            if (field != null) spells.add(field);
        } else {
            List<LivingEntity> targets = isMirror() ? new ArrayList<>(List.of(target)) : nearbyEnemies(AGGRO_RANGE);
            targets.sort(Comparator.<LivingEntity>comparingInt(e -> e instanceof Player ? 0 : 1)
                    .thenComparingDouble(this::distanceToSqr).thenComparingInt(LivingEntity::getId));
            List<BlockPos> anchors = new ArrayList<>();
            for (LivingEntity victim : targets) {
                BlockPos anchor = OracleSpellEntity.findSurface(server, victim.blockPosition(), 64);
                if (anchor == null) anchor = victim.blockPosition().below();
                anchors.add(anchor);
                if (!enhanced && anchors.size() == 3) break;
            }
            if (enhanced) {
                var used = new HashSet<BlockPos>();
                boolean first = true;
                for (BlockPos anchor : anchors) {
                    List<BlockPos> points = lightningPoints(server,List.of(anchor),12);
                    for (int i=0;i<points.size();i++) if (used.add(points.get(i))) {
                        OracleSpellEntity strike = OracleSpellEntity.create(server,this,OracleSpellEntity.LIGHTNING,points.get(i),i*2,true);
                        if (strike != null) { strike.setThunderSound(first); spells.add(strike); }
                    }
                    first = false;
                }
                playSound(SoundEvents.BEACON_POWER_SELECT,1.1F,1.1F); return;
            }
            List<BlockPos> points = lightningPoints(server,anchors,9);
            for (int i = 0; i < points.size(); i++) {
                OracleSpellEntity strike = OracleSpellEntity.create(server, this, OracleSpellEntity.LIGHTNING, points.get(i), i*2);
                if (strike != null) spells.add(strike);
            }
        }
        playSound(SoundEvents.BEACON_POWER_SELECT, 1.1F, gravity ? .6F : 1.1F);
    }
    private List<BlockPos> lightningPoints(ServerLevel server, List<BlockPos> anchors, int count) {
            List<BlockPos> points = new ArrayList<>();
            for (BlockPos anchor : anchors) if (!points.contains(anchor)) points.add(anchor);
            for (int tries = 0; tries < 128 && !anchors.isEmpty() && points.size() < count; tries++) {
                BlockPos anchor = anchors.get(tries % anchors.size());
                int dx = random.nextInt(13)-6, dz = random.nextInt(13)-6;
                if (dx*dx+dz*dz < 2 || dx*dx+dz*dz > 36) continue;
                BlockPos candidate = OracleSpellEntity.findSurface(server, anchor.offset(dx, 2, dz), 8);
                if (candidate != null && points.stream().noneMatch(p -> p.distSqr(candidate) < 2)) points.add(candidate);
            }
            return points;
    }
    public boolean validTarget(LivingEntity target) {
        if (isMirror() && mirrorOwner != null) return mirrorOwner.validTarget(target);
        if (!target.isAlive() || target.isSpectator() || isAlliedTo(target)) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        // Spider Mother and her spiderlings are enemies for acquisition, retaliation and spell damage.
        return target instanceof SpiderMotherEntity || target instanceof AbstractSpiderlingEntity
                || !BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getNamespace().equals(TheAurorian2.MOD_ID);
    }
    @Override public boolean canAttack(LivingEntity target) { return validTarget(target) && super.canAttack(target); }
    private void cancelSpells() {
        combat.clearPlan();
        if (coffin!=null) { coffin.cancel(); coffin=null; }
        for (OracleSpellEntity spell : List.copyOf(spells)) if (!spell.isRemoved()) spell.cancel(); spells.clear();
        for (OracleArcaneEntity spell : arcana) if (!spell.isRemoved()) spell.discard(); arcana.clear(); castTarget = null;
    }
    @Override public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            cancelSpells();
            if (isMirror()) {
                if (mirrorOwner != null && mirrorOwner.mirror == this) {
                    mirrorOwner.remainingMirrorHealth = getHealth(); mirrorOwner.mirror = null;
                }
            } else removeMirror();
        }
        super.remove(reason);
    }
    @Override public void die(DamageSource source) {
        if (isFinalCasting()) return;
        if (!level().isClientSide()) entityData.set(WARD_MASK,0);
        if (!isMirror() && !finalCastUsed && level() instanceof ServerLevel server && beginFinalCoffin(server,source)) return;
        cancelSpells(); setDeltaMovement(Vec3.ZERO); getNavigation().stop();
        if (!level().isClientSide()) {
            entityData.set(VANISHING,false); setAction(IDLE);
            if (isMirror()) { if (mirrorOwner != null) mirrorOwner.remainingMirrorHealth = 0; }
            else removeMirror();
        }
        super.die(source);
    }
    @Override protected void tickDeath() {
        // Two seconds for the authored collapse, then half a second holding its final pose.
        // Let vanilla perform the one-time removal and death event after that presentation.
        if (deathTime < DEATH_DURATION - 1) deathTime++;
        else super.tickDeath();
    }
    @Override public void travel(Vec3 input) { if (isAlive() && !isVanishing()) move(MoverType.SELF, getDeltaMovement()); }
    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public void knockback(double strength, double x, double z) {}
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); if (!isMirror()) bossEvent.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossEvent.removePlayer(player); }
    @Override protected boolean shouldDropLoot(ServerLevel server) { return !isMirror() && super.shouldDropLoot(server); }
    @Override public boolean shouldDropExperience() { return !isMirror() && super.shouldDropExperience(); }
    // Store the subordinate's remaining life on its owner, never two independently saved bosses.
    @Override public boolean shouldBeSaved() { return !isMirror() && super.shouldBeSaved(); }
    @Override public boolean saveAsPassenger(ValueOutput output) { return !isMirror() && super.saveAsPassenger(output); }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output); output.putBoolean("Awakened", awakened);
        output.putInt("ArcaneShieldCooldown", shieldCooldown);
        combat.save(output.child("OracleCombat"));
        output.putInt("GlideCooldown",glideCooldown);
        output.putBoolean("FinalCoffinUsed",finalCastUsed);
        output.putBoolean("FinalCoffinPending",isFinalCasting());
        if (isFinalCasting()) {
            output.putInt("FinalCoffinAge",(int)Math.min(actionAge(0),OracleCoffinEntity.FINAL_STRIKE));
            if(finalKillerId!=null) output.store("FinalCoffinKiller",UUIDUtil.CODEC,finalKillerId);
            if(finalPlayerCredit!=null) output.store("FinalCoffinPlayer",UUIDUtil.CODEC,finalPlayerCredit);
            if(finalCoffinOrigin!=null) output.store("FinalCoffinOrigin",Vec3.CODEC,finalCoffinOrigin);
            if(finalVictimAnchor!=null) output.store("FinalCoffinAnchor",Vec3.CODEC,finalVictimAnchor);
            output.putString("FinalCoffinDamage",finalDamageType.identifier().toString());
        }
        output.putInt("OracleRage",rage());
        output.putInt("OracleWardMask",wardMask());
        output.putBoolean("MirrorPhaseUsed",mirrorPhaseUsed);
        output.putFloat("MirrorHealth",mirror != null && !mirror.isRemoved() ? mirror.getHealth() : remainingMirrorHealth);
        output.putInt("MirrorReturnDelay",isVanishing() ? (int)Math.clamp(mirrorReturnAt-level().getGameTime(),0,10) : 0);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); awakened = input.getBooleanOr("Awakened", false);
        // Old saves simply start fresh tactical memory; health, rage, mirror and final-cast state retain their keys.
        input.child("OracleCombat").ifPresent(combat::load);
        shieldCooldown = Mth.clamp(input.getIntOr("ArcaneShieldCooldown",0),0,240);
        glideCooldown = Mth.clamp(input.getIntOr("GlideCooldown",0),0,GLIDE_COOLDOWN);
        entityData.set(RAGE,Mth.clamp(input.getIntOr("OracleRage",0),0,MAX_RAGE));
        int savedWard = input.getIntOr("OracleWardMask",-1);
        if (savedWard == -1) {
            // Migrate old absorption wards once. Modern potion absorption remains
            // separate from these charges and is never erased by granting a ward.
            savedWard = getAbsorptionAmount() > 0 ? input.getBooleanOr("EmpoweredWard",false) ? 15 : 5 : 0;
            setAbsorptionAmount(0);
        }
        entityData.set(WARD_MASK,Mth.clamp(savedWard,0,15));
        mirrorPhaseUsed = input.getBooleanOr("MirrorPhaseUsed",false);
        remainingMirrorHealth = mirrorPhaseUsed ? Mth.clamp(input.getFloatOr("MirrorHealth",0),0,MIRROR_HEALTH) : 0;
        int returnDelay = Mth.clamp(input.getIntOr("MirrorReturnDelay",0),0,MIRROR_VANISH_TICKS);
        entityData.set(VANISHING,mirrorPhaseUsed && returnDelay > 0);
        mirrorReturnAt = level().getGameTime()+returnDelay;
        // Pre-arcana bosses may have saved the old vanilla zero absorption limit.
        var absorption = getAttribute(Attributes.MAX_ABSORPTION);
        if (absorption != null && absorption.getBaseValue() < 50) absorption.setBaseValue(50);
        var followRange = getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && followRange.getBaseValue() == 36) followRange.setBaseValue(AGGRO_RANGE);
        bossEvent = createBossEvent(); setAction(IDLE); cooldown = SPELL_GAP; setNoGravity(true);
        finalCastUsed=input.getBooleanOr("FinalCoffinUsed",false);
        if(input.getBooleanOr("FinalCoffinPending",false)) {
            finalCastUsed=true; restoreFinalCast=true;
            finalKillerId=input.read("FinalCoffinKiller",UUIDUtil.CODEC).orElse(null);
            finalPlayerCredit=input.read("FinalCoffinPlayer",UUIDUtil.CODEC).orElse(null);
            finalCoffinOrigin=input.read("FinalCoffinOrigin",Vec3.CODEC).orElse(null);
            finalVictimAnchor=input.read("FinalCoffinAnchor",Vec3.CODEC).orElse(null);
            var damageId=Identifier.tryParse(input.getStringOr("FinalCoffinDamage","minecraft:generic"));
            if(damageId!=null) finalDamageType=ResourceKey.create(Registries.DAMAGE_TYPE,damageId);
            savedFinalAge=Mth.clamp(input.getIntOr("FinalCoffinAge",0),0,OracleCoffinEntity.FINAL_STRIKE);
            entityData.set(FINAL_CAST,true); entityData.set(VANISHING,false); entityData.set(EMPOWERED,false);
            entityData.set(WARD_MASK,0);
            setAction(COFFIN); entityData.set(ACTION_START,level().getGameTime()-savedFinalAge); setHealth(0);
        }
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RisingClockOracleEntity>("oracle", 2, state -> {
            boolean resting = action() == IDLE && !isDeadOrDying() && !isVanishing()
                    && hurtTime == 0 && getDeltaMovement().horizontalDistanceSqr() <= .001;
            long idleNow = level().getGameTime();
            if (!resting) idleAnimationStart = -1;
            // A client time correction can move the clock behind the recorded
            // idle start. Restart that quiet period instead of seeking negative time.
            else if (idleAnimationStart < 0 || idleAnimationStart > idleNow) idleAnimationStart = idleNow;
            double idleTicks = resting ? Math.max(0,idleNow - idleAnimationStart + state.renderState().getPartialTick()) : 0;
            // Gentle idle/movement blends; combat retains its responsive two-tick transition.
            state.controller().setTransitionTicks(action() == IDLE && !isDeadOrDying() ? 6 : 2);
            float age = actionAge(state.renderState().getPartialTick());
            int windup = castWindup(action());
            double animationTime = age/20.0;
            RawAnimation animation = isDeadOrDying() ? DEFEAT : switch(action()) {
                case LIGHTNING -> THUNDER; case GRAVITY -> LIFT; case AWAKEN -> ENTRANCE;
                case COFFIN -> BLACK_COFFIN;
                case SHIELD -> WARD; case BLAST -> empowered() ? age < BOLT_WINDUP ? CHANNEL_START : age < castDuration()-SHORT_RECOVERY ? CHANNEL_LOOP : CHANNEL_END : BOLT;
                case CHRONO -> age < windup ? CHANNEL_START : CHANNEL_END;
                case FLAME, REPULSOR -> age < windup ? CHANNEL_START : age < castDuration()-CHANNEL_RECOVERY ? CHANNEL_LOOP : CHANNEL_END;
                case GLIDE -> age < GLIDE_WINDUP ? TO_FLY : age < GLIDE_WINDUP+GLIDE_MOVEMENT_TICKS ? FLY : TO_HOVER;
                default -> getDeltaMovement().horizontalDistanceSqr() > .001 ? FLY : resting ? idleAnimation(idleTicks) : IDLE_ANIMATION;
            };
            // Retime the approved poses, including the release frame; never detach the staff
            // or uniformly accelerate the sustained spell effects and gravity choreography.
            if (action()==BLAST && empowered()) animationTime = age < BOLT_WINDUP ? age*1.2/BOLT_WINDUP : age < castDuration()-SHORT_RECOVERY ? ((age-BOLT_WINDUP)/20.0)%2.4 : (age-(castDuration()-SHORT_RECOVERY))*1.2/SHORT_RECOVERY;
            else if (isDeadOrDying()) animationTime = Math.min(1, (deathTime + state.renderState().getPartialTick()) / 40.0);
            // Stretch only the authored sealed hold; growth and the final release retain their poses.
            else if(action()==COFFIN && age>OracleCoffinEntity.SEAL) animationTime=2+(age-OracleCoffinEntity.SEAL)/OracleCoffinEntity.SEALED_HOLD;
            else if (action()==FLAME || action()==CHRONO || action()==REPULSOR) {
                if (age>=castDuration()-CHANNEL_RECOVERY) animationTime=(age-(castDuration()-CHANNEL_RECOVERY))*1.2/CHANNEL_RECOVERY;
                else if(age>=windup) animationTime=((age-windup)/20.0)%2.4;
                // The beam reaches its casting pose quickly, while its promised0.5s aim window remains.
                else animationTime=Math.min(age/CHANNEL_WINDUP,1)*1.2;
            } else if(action()==BLAST) animationTime=age<=BOLT_WINDUP ? age*1.1/BOLT_WINDUP : 1.1+(age-BOLT_WINDUP)*1.3/SHORT_RECOVERY;
            else if(action()==SHIELD) animationTime=age<=CHANNEL_WINDUP ? age*1.25/CHANNEL_WINDUP : 1.25+(age-CHANNEL_WINDUP)*1.75/SHORT_RECOVERY;
            else if(action()==LIGHTNING) animationTime=age<=CHANNEL_WINDUP ? age*1.95/CHANNEL_WINDUP : age<=20 ? 1.95+(age-CHANNEL_WINDUP)*.25/(20-CHANNEL_WINDUP) : 2.2+(age-20)*1.4/(castDuration()-20);
            else if(action()==GRAVITY) animationTime=age<=CHANNEL_WINDUP ? age*1.35/CHANNEL_WINDUP : age<=14 ? 1.35+(age-CHANNEL_WINDUP)*.15/(14-CHANNEL_WINDUP) : age<=42 ? 1.5+(age-14)*2.3/28 : age<=50 ? 3.8+(age-42)*.3/8 : 4.1+(age-50)*1.7/10;
            else if(action()==GLIDE) animationTime=age<GLIDE_WINDUP ? Math.min(age/CHANNEL_WINDUP,1) : age<GLIDE_WINDUP+GLIDE_MOVEMENT_TICKS ? (age-GLIDE_WINDUP)/20.0 : (age-GLIDE_WINDUP-GLIDE_MOVEMENT_TICKS)/CHANNEL_RECOVERY;
            var result = state.setAndContinue(animation);
            if (action() != IDLE || isDeadOrDying()) state.controller().setAnimationTime(animationTime);
            else if (resting) state.controller().setAnimationTime((idleTicks % 96) / 20.0);
            return result;
        }));
    }
    private RawAnimation idleAnimation(double ticks) {
        // One, two, one, then three hover cycles between gestures. Stable per-entity
        // ordering keeps nearby priests from performing the same gesture together,
        // without gameplay RNG, saved state, scans or extra synchronization packets.
        int gesture = switch ((int)((long)(ticks / 96) % 11)) {
            case 1 -> 0; case 4 -> 1; case 6 -> 2; case 10 -> 3;
            default -> -1;
        };
        if (gesture < 0) return IDLE_ANIMATION;
        int seed = getUUID().hashCode();
        return IDLE_GESTURES[Math.floorMod((seed & 3) + gesture * ((seed & 4) == 0 ? 1 : -1), 4)];
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }
}
