package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import cn.teampancake.theaurorian2.common.item.ArcanePrismItem;
import cn.teampancake.theaurorian2.common.item.ArcanePrismChannel;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/** Bounded, transient casts. Imported models are presentation; hits and debuffs belong to the server. */
public final class OracleArcaneEntity extends Entity implements GeoEntity {
    public static final int SHIELD = 0, BLAST = 1, TRAIL = 2, HIT = 3, CLOCK = 4,
            FLAME = 5, FIRE_PATCH = 6, BURN = 7, REPULSOR = 8, WARD_PUSH = 9;
    public static final double REPULSOR_RANGE = 64;
    private static final double REPULSOR_RADIUS = .65, REPULSOR_IMPACT_RADIUS = 3;
    public static final int REPULSOR_CHANNEL_TICKS = 40, REPULSOR_FADE_TICKS = 4;
    private static final int BEAM_MIN_VISIBLE_TICKS = 6;
    public static final int EMPOWERED_BEAM_TICKS = 70;
    public static final float EMPOWERED_BLAST_SCALE = 5.5F, REGIONAL_CLOCK_SCALE = 128F/(82.36F/16);
    public static final double EMPOWERED_BLAST_RADIUS = 8;
    private static final ResourceKey<DamageType> EMPOWERED_BLAST_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_empowered_blast"));
    public static final double FLAME_RANGE = 16;
    public static final int FLAME_DURATION = 60, FIRE_PATCH_DURATION = 200, FIRE_PULSE_TICKS = 10;
    private static final ResourceKey<DamageType> FIRE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_arcane_fire"));
    private static final int BEAM_PULSE_TICKS = 2;
    private static final ResourceKey<DamageType> BEAM_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_repulsor"));
    public static final float BLAST_SCALE = 4;
    public static final double BLAST_RADIUS = 6;
    public static final int CLOCK_CONTROL_START = 56, CLOCK_CONTROL_DURATION = 80;
    private static final ResourceKey<DamageType> CLOCK_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_chrono"));
    // The outer authored core is 8.3 pixels wide; its collision follows the enlarged core.
    private static final double BLAST_CORE_RADIUS = 8.3 / 32 * BLAST_SCALE;
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> BEAM_LENGTH = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BEAM_STOP = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> EMPOWERED = SynchedEntityData.defineId(OracleArcaneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation BEAM = RawAnimation.begin().thenPlayAndHold("channel");
    private static final RawAnimation[] SKILLS = {RawAnimation.begin().thenPlayAndHold("skill"),
            RawAnimation.begin().thenPlayAndHold("skill2"), RawAnimation.begin().thenPlayAndHold("skill3")};
    private static final int[] LIFETIMES = {28, 35, 22, 25, CLOCK_CONTROL_START + CLOCK_CONTROL_DURATION, FLAME_DURATION, FIRE_PATCH_DURATION, 80, REPULSOR_CHANNEL_TICKS + REPULSOR_FADE_TICKS, 11};
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private RisingClockOracleEntity caster;
    private ServerPlayer prismCaster;
    private boolean prismReleased;
    private LivingEntity slowed;
    private Identifier slowId;
    private int slowStage = -1;
    private Set<LivingEntity> beamHit, beamSlowed, beamContact;
    private Identifier beamSlowId;
    private DamageSource beamDamage;
    private DamageSource fireDamage;
    private int lastFirePulse = -1;
    private OracleFlamePath flamePath;
    private List<LivingEntity> clockTargets = List.of();
    private record WardPush(LivingEntity entity, Vec3 start, Vec3 direction) {}
    private List<WardPush> pushed = List.of();

    public OracleArcaneEntity(EntityType<? extends OracleArcaneEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(KIND, SHIELD); data.define(OWNER, -1); data.define(TARGET, -1);
        data.define(VARIANT, 0); data.define(START, 0L);
        data.define(BEAM_LENGTH, 0F);
        data.define(BEAM_STOP, -1);
        data.define(EMPOWERED,false);
    }
    public int kind() { return entityData.get(KIND); }
    public int targetId() { return entityData.get(TARGET); }
    public boolean empowered() { return entityData.get(EMPOWERED); }
    public int beamTicks() { return empowered() ? EMPOWERED_BEAM_TICKS : REPULSOR_CHANNEL_TICKS; }
    public double beamRadius() { return REPULSOR_RADIUS*(empowered() ? 2 : 1); }
    // The handheld prism retains empowered damage while keeping its shaft clear of the player's view.
    public float beamVisualScale() { return level().getEntity(entityData.get(OWNER)) instanceof Player ? .125F : 1; }
    public float blastScale() { return empowered() ? EMPOWERED_BLAST_SCALE : BLAST_SCALE; }
    public double blastRadius() { return empowered() ? EMPOWERED_BLAST_RADIUS : BLAST_RADIUS; }
    public float beamLength() { return entityData.get(BEAM_LENGTH); }
    private boolean beamStopped() { return entityData.get(BEAM_STOP) >= 0; }
    private int beamFadeStart() { return Math.max(BEAM_MIN_VISIBLE_TICKS,entityData.get(BEAM_STOP)); }
    public float beamVisualFade(float partialTick) {
        return beamStopped() ? Math.clamp((beamFadeStart()+REPULSOR_FADE_TICKS-age(partialTick))/REPULSOR_FADE_TICKS,0,1) : 1;
    }
    void stopChannel() {
        if (kind()!=REPULSOR) { discard(); return; }
        if (isRemoved() || beamStopped()) return;
        // Release can kill before the next entity tick. Stop gameplay immediately, but
        // leave the already-fired shaft long enough to replicate and visibly dissipate.
        entityData.set(BEAM_STOP,(int)age(0));
        clearBeamSlow();
    }
    public Vec3 beamEnd() { return position().add(getLookAngle().scale(beamLength())); }
    public float age(float partial) { return Math.max(0, level().getGameTime() - entityData.get(START)) + partial; }
    public boolean isWarmingUp() { return level().getGameTime() < entityData.get(START); }
    public OracleFlamePath flamePath() {
        if (flamePath == null) flamePath = new OracleFlamePath();
        flamePath.update(this);
        return flamePath;
    }

    public static OracleArcaneEntity createPrismBeam(ServerLevel level, ServerPlayer player) {
        OracleArcaneEntity effect = new OracleArcaneEntity(ModEntities.ORACLE_ARCANE.get(), level);
        effect.prismCaster = player;
        effect.entityData.set(KIND, REPULSOR); effect.entityData.set(OWNER, player.getId());
        effect.entityData.set(EMPOWERED, true);
        effect.entityData.set(START, level.getGameTime() + RisingClockOracleEntity.BEAM_LOCK_TICKS);
        effect.setPos(player.getEyePosition());
        effect.initializeBeam(level, player);
        if (!level.addFreshEntity(effect)) return null;
        player.setData(ModAttachments.ARCANE_PRISM_CHANNEL, new ArcanePrismChannel(effect.getId(), player.position()));
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        return effect;
    }

    private void initializeBeam(ServerLevel level, LivingEntity owner) {
        beamHit = new HashSet<>(); beamSlowed = new HashSet<>(); beamContact = new HashSet<>();
        beamSlowId = TheAurorian2.id("oracle_beam/" + getUUID());
        beamDamage = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(BEAM_DAMAGE), this, owner);
    }
    public static OracleArcaneEntity create(ServerLevel level, RisingClockOracleEntity owner, int kind, Vec3 pos, LivingEntity target) {
        return create(level,owner,kind,pos,target,false);
    }
    public static OracleArcaneEntity create(ServerLevel level, RisingClockOracleEntity owner, int kind, Vec3 pos, LivingEntity target, boolean enhanced) {
        OracleArcaneEntity effect = new OracleArcaneEntity(ModEntities.ORACLE_ARCANE.get(), level);
        effect.caster = owner; effect.entityData.set(KIND, kind); effect.entityData.set(OWNER, owner.getId());
        effect.entityData.set(EMPOWERED,enhanced);
        effect.entityData.set(TARGET, target == null ? -1 : target.getId());
        effect.entityData.set(VARIANT, level.getRandom().nextInt(3)); effect.entityData.set(START, level.getGameTime());
        effect.setPos(pos);
        if (kind == BLAST && target != null) {
            // The enlarged core must clear the floor while approaching a standing target.
            Vec3 aim = enhanced ? target.getEyePosition() : target.getBoundingBox().getCenter();
            effect.setDeltaMovement(aim.subtract(pos).normalize().scale(.9));
        }
        if (kind == FLAME || kind == FIRE_PATCH) {
            effect.fireDamage = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(FIRE_DAMAGE),effect,owner);
            if (kind == FLAME && target != null) effect.aimAt(target);
        }
        if (kind == REPULSOR && target != null) {
            effect.initializeBeam(level, owner);
            Vec3 aim = target.getBoundingBox().getCenter().subtract(pos);
            effect.setYRot((float)(Math.atan2(aim.z,aim.x)*180/Math.PI)-90);
            effect.setXRot((float)(-Math.atan2(aim.y,aim.horizontalDistance())*180/Math.PI));
            effect.yRotO = effect.getYRot(); effect.xRotO = effect.getXRot();
            effect.updateBeamLength(level);
        }
        if (!level.addFreshEntity(effect)) return null;
        owner.trackArcane(effect);
        // The ward protects on the casting frame; its expanding visual is not a second windup.
        if (kind == SHIELD) owner.grantWard(enhanced);
        // Start the first 0.8-second slow on release, rather than one entity tick later.
        if (kind == CLOCK && target != null) effect.tickClock(level,target,0);
        if (kind == FLAME || kind == FIRE_PATCH) effect.tickFire(level,0);
        if (kind == REPULSOR && target != null) {
            effect.tickRepulsor(level,0);
            effect.repulsorImpact(level);
        }
        return effect;
    }
    public static OracleArcaneEntity createRegionalClock(ServerLevel level, RisingClockOracleEntity owner, List<LivingEntity> targets) {
        OracleArcaneEntity clock = create(level,owner,CLOCK,owner.position().add(0,20,0),null,true);
        if (clock != null) { clock.clockTargets = List.copyOf(targets); clock.tickClock(level,null,0); }
        return clock;
    }
    public static void createWardPush(ServerLevel level, RisingClockOracleEntity owner, List<LivingEntity> targets) {
        OracleArcaneEntity effect = create(level,owner,WARD_PUSH,owner.position(),null,true);
        if (effect != null) {
            effect.pushed = targets.stream().map(v -> {
                Vec3 direction = v.position().subtract(owner.position()).multiply(1,0,1);
                if (direction.lengthSqr()<.001) direction = new Vec3(1,0,0);
                return new WardPush(v,v.position(),direction.normalize());
            }).toList();
            effect.sound(level,SoundEvents.AMETHYST_BLOCK_BREAK,1.3F,.8F);
        }
    }
    private void tickWardPush(int age) {
        for (WardPush push : pushed) {
            LivingEntity victim = push.entity;
            if (!victim.isAlive() || victim.isRemoved() || victim.level()!=level() || victim.isPassenger()) continue;
            Vec3 destination = push.start.add(push.direction.scale(Math.min(3,age*.3)));
            // Exact three-block travel in open space, using collision handling and bypassing
            // knockback resistance. Do not teleport through walls or modify attributes.
            victim.move(MoverType.SELF,new Vec3(destination.x-victim.getX(),0,destination.z-victim.getZ()));
            victim.setDeltaMovement(0,victim.getDeltaMovement().y,0);
            victim.needsSync = true;
            if (victim instanceof ServerPlayer player)
                player.connection.teleport(player.getX(),player.getY(),player.getZ(),player.getYRot(),player.getXRot());
        }
    }
    public void refreshBurn() { entityData.set(START, level().getGameTime()); }
    private void aimAt(LivingEntity target) {
        Vec3 aim = target.getBoundingBox().getCenter().subtract(position());
        setYRot((float)(Math.atan2(aim.z,aim.x)*180/Math.PI)-90);
        setXRot((float)(-Math.atan2(aim.y,aim.horizontalDistance())*180/Math.PI));
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerLevel server = (ServerLevel) level();
        if (kind()==REPULSOR && beamStopped()) {
            if (age(0)>=beamFadeStart()+REPULSOR_FADE_TICKS) discard();
            return;
        }
        if (prismCaster != null) { tickPrismBeam(server); return; }
        if (caster == null || !caster.isAlive() || caster.isRemoved() || caster.isNoAi()) { discard(); return; }
        int age = (int) age(0);
        if (age >= (kind()==REPULSOR ? beamTicks()+REPULSOR_FADE_TICKS : LIFETIMES[kind()])) {
            if (kind() == BLAST) impact(server);
            discard(); return;
        }
        LivingEntity target = server.getEntity(targetId()) instanceof LivingEntity living ? living : null;
        switch (kind()) {
            case SHIELD -> {
                setPos(caster.position().add(0,.35,0));
            }
            case BLAST -> tickBlast(server, age);
            case REPULSOR -> {
                if (beamHit == null) { discard(); return; }
                if (!caster.canContinueChannel(RisingClockOracleEntity.REPULSOR,target)) { stopChannel(); return; }
                // Direction stays fixed after the lock; newly placed cover still intercepts the shot.
                if (age <= beamTicks() && age % BEAM_PULSE_TICKS == 0) {
                    if (empowered() && target != null && caster.validTarget(target)) aimAt(target);
                    updateBeamLength(server);
                    tickRepulsor(server,age);
                    if (age == beamTicks()) clearBeamSlow();
                }
            }
            case CLOCK -> tickClock(server,target,age);
            case WARD_PUSH -> tickWardPush(age);
            case FLAME -> {
                if (!caster.canContinueChannel(RisingClockOracleEntity.FLAME,target)) { discard(); return; }
                setPos(caster.spellOrigin());
                aimAt(target);
                tickFire(server,age);
            }
            case FIRE_PATCH -> tickFire(server,age);
            case BURN -> {
                if (target == null || !caster.validTarget(target)) { discard(); return; }
                setPos(target.getBoundingBox().getCenter());
                if (age > 0 && age % 20 == 0) target.hurtServer(server,damageSources().indirectMagic(this,caster),1.5F);
            }
            default -> { }
        }
    }

    private void tickPrismBeam(ServerLevel server) {
        if (!prismCaster.isAlive() || prismCaster.isRemoved() || prismCaster.isSpectator()
                || prismCaster.isPassenger() || prismCaster.level() != server || !(prismCaster.getMainHandItem().getItem() instanceof ArcanePrismItem)) {
            discard(); return;
        }
        var channel = prismCaster.getData(ModAttachments.ARCANE_PRISM_CHANNEL);
        // External long-distance teleports cancel the cast instead of dragging a player back.
        if (channel.spellId() != getId() || prismCaster.position().distanceToSqr(channel.anchor()) > 64) { discard(); return; }
        prismCaster.setDeltaMovement(Vec3.ZERO);
        prismCaster.resetFallDistance();
        if (prismCaster.position().distanceToSqr(channel.anchor()) > 1.0E-6)
            prismCaster.connection.teleport(channel.anchor().x, channel.anchor().y, channel.anchor().z,
                    prismCaster.getYRot(), prismCaster.getXRot());
        if (isWarmingUp()) { setPos(prismCaster.getEyePosition()); return; }
        int age = (int)age(0);
        if (age >= beamTicks() + REPULSOR_FADE_TICKS) { discard(); return; }
        if (!prismReleased) {
            prismReleased = true;
            // Place the muzzle ahead/below the camera without letting it cross a wall.
            Vec3 eye = prismCaster.getEyePosition(), muzzle = eye.add(prismCaster.getLookAngle().scale(1.6)).add(0, -.35, 0);
            var hit = server.clip(new ClipContext(eye, muzzle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, prismCaster));
            setPos(hit.getType() == HitResult.Type.MISS ? muzzle : hit.getLocation().subtract(muzzle.subtract(eye).normalize().scale(.05)));
            aimPrismBeam(server);
            yRotO = getYRot(); xRotO = getXRot();
            tickRepulsor(server, 0); repulsorImpact(server);
        } else if (age <= beamTicks() && age % BEAM_PULSE_TICKS == 0) {
            aimPrismBeam(server);
            tickRepulsor(server, age);
            if (age == beamTicks()) clearBeamSlow();
        }
    }

    private void aimPrismBeam(ServerLevel server) {
        Vec3 eye = prismCaster.getEyePosition(), look = prismCaster.getLookAngle();
        double range = loadedRange(server, eye, look, REPULSOR_RANGE);
        Vec3 aimPoint = server.clip(new ClipContext(eye, eye.add(look.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, prismCaster)).getLocation();
        Vec3 aim = aimPoint.subtract(position());
        if (aim.lengthSqr() > 1.0E-8) {
            setYRot((float)(Math.atan2(aim.z, aim.x) * 180 / Math.PI) - 90);
            setXRot((float)(-Math.atan2(aim.y, aim.horizontalDistance()) * 180 / Math.PI));
        }
        updateBeamLength(server);
    }

    private boolean validBeamTarget(LivingEntity target) {
        if (caster != null) return caster.validTarget(target);
        if (prismCaster == null || target == prismCaster || !target.isAlive() || target.isSpectator()
                || prismCaster.isAlliedTo(target) || target.isAlliedTo(prismCaster)) return false;
        return !(target instanceof Player player) || (!player.getAbilities().instabuild && prismCaster.canHarmPlayer(player));
    }
    private void tickFire(ServerLevel server, int age) {
        if (age%FIRE_PULSE_TICKS!=0 || lastFirePulse==age) return;
        lastFirePulse = age;
        boolean flame = kind()==FLAME;
        OracleFlamePath path = flame ? flamePath() : null;
        AABB area = flame ? path.bounds() : getBoundingBox().inflate(.75,1,.75);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,area,caster::validTarget)) {
            if (flame && !path.touches(victim.getBoundingBox())) continue;
            // Cover is tested from the fire itself, even after the caster retreats behind a wall.
            if (!unobstructed(victim) || (!flame && !caster.canHitFirePatch(victim))) continue;
            victim.hurtServer(server,fireDamage,flame ? 6 : 4);
            caster.burn(victim);
        }
        if (flame) sound(server,SoundEvents.BLAZE_SHOOT,.5F,1.25F);
    }
    private void tickClock(ServerLevel server, LivingEntity target, int age) {
        // Released clocks follow their victim independently of the caster's next action or position.
        if (!empowered()) {
            if (target == null || !caster.validTarget(target)) { discard(); return; }
            setPos(target.getX(), target.getBbHeight()+target.getY()+.25, target.getZ());
        } else setPos(caster.getX(),caster.getY()+20,caster.getZ());
        int stage = age < 16 ? 0 : age < 36 ? 1 : age < CLOCK_CONTROL_START ? 2 : 3;
        if (stage == slowStage) return;
        if (slowId == null) slowId = TheAurorian2.id("oracle_clock/"+getUUID());
        if (empowered()) {
            for (LivingEntity victim : clockTargets) if (caster.validTarget(victim) && victim.level()==server) applyClockStage(server,victim,stage);
        } else { applyClockStage(server,target,stage); slowed = target; }
        slowStage = stage;
        sound(server, SoundEvents.ILLUSIONER_MIRROR_MOVE, .65F, 1F-stage*.22F);
    }
    private void applyClockStage(ServerLevel server, LivingEntity target, int stage) {
        var speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(slowId);
            if (stage < 3) speed.addTransientModifier(new AttributeModifier(slowId,
                    stage == 0 ? -.15 : stage == 1 ? -.4 : -.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (stage == 3) {
            // Apply once. Preserve immunity and stronger/longer effects from other sources.
            target.addEffect(new MobEffectInstance(ModMobEffects.STUN, CLOCK_CONTROL_DURATION, 0, false, false, true), caster);
            target.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, CLOCK_CONTROL_DURATION, 0, false, false, true), caster);
            if (target.hasEffect(ModMobEffects.STUN) || target.hasEffect(ModMobEffects.PARALYSIS))
                caster.controlApplied(CLOCK_CONTROL_DURATION);
            float damage = switch (server.getDifficulty()) { case PEACEFUL -> 0; case EASY -> 6; case NORMAL -> 8; case HARD -> 10; };
            if (damage > 0) target.hurtServer(server,new DamageSource(
                    server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CLOCK_DAMAGE),this,caster),damage);
            if (target.hasEffect(ModMobEffects.PARALYSIS)) { target.setDeltaMovement(Vec3.ZERO); target.needsSync = true; }
        }
    }
    private void tickBlast(ServerLevel server, int age) {
        Vec3 start = position(), next = start.add(getDeltaMovement());
        if (!server.hasChunkAt(BlockPos.containing(next))) { discard(); return; }
        double coreRadius = BLAST_CORE_RADIUS*blastScale()/BLAST_SCALE;
        AABB swept = new AABB(start,next).inflate(coreRadius);
        Vec3 collision = null;
        double nearest = Double.MAX_VALUE;
        // Sweep the whole core, so its visible edge cannot pass through walls or targets.
        for (var shape : server.getBlockCollisions(this,swept)) {
            for (AABB box : shape.toAabbs()) {
                AABB expanded = box.inflate(coreRadius);
                Vec3 point = expanded.contains(start) ? start : expanded.clip(start,next).orElse(null);
                if (point != null && start.distanceToSqr(point) < nearest) {
                    nearest = start.distanceToSqr(point); collision = point;
                }
            }
        }
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,swept,caster::validTarget)) {
            AABB expanded = victim.getBoundingBox().inflate(coreRadius);
            Vec3 point = expanded.contains(start) ? start : expanded.clip(start,next).orElse(null);
            if (point != null && start.distanceToSqr(point) < nearest) {
                nearest = start.distanceToSqr(point); collision = point;
            }
        }
        if (collision != null) { setPos(collision); impact(server); discard(); return; }
        setPos(next);
        if (age % 3 == 0) create(server,caster,TRAIL,position(),null,empowered());
    }
    private void updateBeamLength(ServerLevel server) {
        Vec3 direction = getLookAngle();
        double range = REPULSOR_RANGE;
        if (empowered() && server.getEntity(targetId()) instanceof LivingEntity target)
            range = Math.min(range,position().distanceTo(target.getBoundingBox().getCenter()));
        range = loadedRange(server, position(), direction, range);
        Vec3 end = server.clip(new ClipContext(position(),position().add(direction.scale(range)),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getLocation();
        entityData.set(BEAM_LENGTH,(float)position().distanceTo(end));
    }
    private static double loadedRange(ServerLevel server, Vec3 origin, Vec3 direction, double range) {
        for (int distance = 0; distance <= range; distance += 4)
            if (!server.hasChunkAt(BlockPos.containing(origin.add(direction.scale(distance))))) return Math.max(0, distance - 4);
        return range;
    }
    private void tickRepulsor(ServerLevel server, int age) {
        Vec3 start = position(), end = beamEnd();
        beamContact.clear();
        if (beamLength() >= .05F) {
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(beamRadius()),this::validBeamTarget)) {
                // Clip the axial interval so the endpoint blast remains separate from the shaft.
                double projection = victim.getBoundingBox().getCenter().subtract(start).dot(getLookAngle());
                AABB expanded = victim.getBoundingBox().inflate(beamRadius());
                if (projection < 0 || projection > beamLength()+.001
                        || (!expanded.contains(start) && expanded.clip(start,end).isEmpty())
                        || !unobstructed(victim)) continue;
                beamContact.add(victim);
                if (beamHit.add(victim)) victim.hurtServer(server,beamDamage,empowered() ? 20 : 18);
                else if (age > 0 && beamSlowed.contains(victim)) victim.hurtServer(server,beamDamage,
                        (10F+(empowered() ? victim.getMaxHealth()*.05F : 0))*BEAM_PULSE_TICKS/20);
                if (!beamSlowed.contains(victim)) {
                    var speed = victim.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speed != null) speed.addTransientModifier(new AttributeModifier(beamSlowId,-.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
        for (LivingEntity victim : beamSlowed) if (!beamContact.contains(victim)) removeBeamSlow(victim);
        var previous = beamSlowed; beamSlowed = beamContact; beamContact = previous;
    }
    private void repulsorImpact(ServerLevel server) {
        Vec3 end = beamEnd();
        Vec3 impact = end.subtract(getLookAngle().scale(.05));
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,new AABB(impact,impact).inflate(REPULSOR_IMPACT_RADIUS),this::validBeamTarget)) {
            if (!beamHit.contains(victim) && victim.getBoundingBox().distanceToSqr(impact) <= REPULSOR_IMPACT_RADIUS * REPULSOR_IMPACT_RADIUS
                    && server.clip(new ClipContext(impact,victim.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS) {
                victim.hurtServer(server,beamDamage,empowered() ? 20 : 12);
            }
        }
        sound(server,SoundEvents.WARDEN_SONIC_BOOM,1.1F,.7F);
        server.playSound(null,end.x,end.y,end.z,SoundEvents.GENERIC_EXPLODE.value(),prismCaster == null ? SoundSource.HOSTILE : SoundSource.PLAYERS,.85F,.75F);
    }
    private void removeBeamSlow(LivingEntity victim) {
        var speed = victim.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(beamSlowId);
    }
    private void clearBeamSlow() {
        if (beamSlowed != null) { for (LivingEntity victim : beamSlowed) removeBeamSlow(victim); beamSlowed.clear(); }
    }
    private void impact(ServerLevel server) {
        create(server,caster,HIT,position(),null,empowered());
        float damage = empowered() ? switch(server.getDifficulty()) { case PEACEFUL -> 0; case EASY -> 11; case NORMAL -> 15; case HARD -> 20; } : 10;
        DamageSource source = empowered() ? new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(EMPOWERED_BLAST_DAMAGE),this,caster) : damageSources().indirectMagic(this,caster);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,new AABB(position(),position()).inflate(blastRadius()),caster::validTarget)) {
            if (victim.getBoundingBox().getCenter().distanceToSqr(position()) <= blastRadius() * blastRadius() && unobstructed(victim)) {
                if (damage > 0) victim.hurtServer(server,source,damage);
            }
        }
        sound(server,SoundEvents.GENERIC_EXPLODE.value(),1F,.8F);
    }
    private boolean unobstructed(LivingEntity target) {
        return level().clip(new ClipContext(position(),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;
    }
    private void sound(ServerLevel server, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        server.playSound(null,getX(),getY(),getZ(),sound,prismCaster == null ? SoundSource.HOSTILE : SoundSource.PLAYERS,volume,pitch);
    }
    @Override public void remove(RemovalReason reason) {
        if (prismCaster != null && prismCaster.getData(ModAttachments.ARCANE_PRISM_CHANNEL).spellId() == getId()) {
            prismCaster.setData(ModAttachments.ARCANE_PRISM_CHANNEL, ArcanePrismChannel.IDLE);
            prismCaster.setDeltaMovement(Vec3.ZERO);
            prismCaster.resetFallDistance();
        }
        clearBeamSlow();
        if (slowId != null) for (LivingEntity victim : clockTargets) {
            var speed = victim.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(slowId);
        }
        clockTargets = List.of(); pushed = List.of();
        if (slowed != null && slowId != null) {
            var speed=slowed.getAttribute(Attributes.MOVEMENT_SPEED); if(speed!=null) speed.removeModifier(slowId);
            slowed=null;
        }
        super.remove(reason);
    }
    @Override public boolean hurtServer(ServerLevel server, DamageSource source, float damage) { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance<(empowered() && kind()==CLOCK ? 192*192 : 96*96); }
    @Override protected void addAdditionalSaveData(ValueOutput output) { }
    @Override protected void readAdditionalSaveData(ValueInput input) { }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<OracleArcaneEntity>("arcane",0,state -> {
            int variant=(kind()==TRAIL || kind()==HIT) ? entityData.get(VARIANT) : 0;
            var result=state.setAndContinue(kind()==BLAST ? IDLE : kind()==REPULSOR ? BEAM : SKILLS[variant]);
            double time = age(state.renderState().getPartialTick())/20.0;
            // Retain the authored clock's countdown and stretch only the final held control phase.
            if (kind()==CLOCK && time>2.8) time=2.8+(time-2.8)*.75;
            if (kind()==REPULSOR && empowered()) time*=REPULSOR_CHANNEL_TICKS/(double)EMPOWERED_BEAM_TICKS;
            // An interrupted beam fades through its render width, not by waiting for
            // the full channel animation or replaying the firing frame.
            if (kind()==REPULSOR && beamStopped()) time=Math.min(time,REPULSOR_CHANNEL_TICKS/20.0);
            // Explicit seeking clamps at the end; wrap the blast's 2/3-second loop ourselves.
            state.controller().setAnimationTime(kind()==BLAST ? time % (2.0/3.0) : time);
            return result;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
