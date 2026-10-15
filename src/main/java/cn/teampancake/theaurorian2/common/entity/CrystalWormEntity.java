package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.world.CrystalWormSpawning;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

/** A rock-swimming cave predator. Travel ignores terrain without modifying it. */
public final class CrystalWormEntity extends Monster implements GeoEntity {
    public enum Phase { IDLE, SENSE, START_MOVE, MOVE, ACCELERATE, FAST, DECELERATE, STOP_MOVE,
        BURROW_IN, HIDDEN, BURROW_OUT, ATTACK, HURT }
    private static final Phase[] PHASES = Phase.values();
    private static final double BASE_MOVEMENT_SPEED = .32;
    private static final int HURT_TICKS = 4, HURT_RECOVERY_TICKS = 24, ACCELERATE_TICKS = 5;
    private static final float BURROW_PLAYBACK_SPEED = 1.25F;
    // One undulation covers about 2.1 blocks at the original adult movement speeds.
    private static final double LOCOMOTION_CYCLE_DISTANCE = 2.1;
    public static final TagKey<DamageType> MAGIC = TagKey.create(Registries.DAMAGE_TYPE, TheAurorian2.id("crystal_worm_magic"));
    public static final TagKey<Item> MINING_TOOLS = TagKey.create(Registries.ITEM, TheAurorian2.id("crystal_worm_mining_tools"));
    private static final EntityDataAccessor<Byte> PHASE = SynchedEntityData.defineId(CrystalWormEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(CrystalWormEntity.class, EntityDataSerializers.LONG);
    private static final RawAnimation[] ANIMATIONS = java.util.Arrays.stream(new String[]{"idle", "idle_sense", "start_move",
            "move", "accelerate", "move_fast", "decelerate", "stop_move", "burrow_in", "move_fast", "burrow_out", "attack", "hurt"})
            .map(n -> RawAnimation.begin().thenPlayAndHold(n)).toArray(RawAnimation[]::new);
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation ATTITUDE = RawAnimation.begin().thenLoop("motion_attitude");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Vec3 waypoint, burrowAnchor, burrowDirection, lastSurface, exitStage, exitDirection;
    private LivingEntity strikeTarget;
    private int exposedTicks = 200, attackCooldown, routeRetry, hurtRecovery, ambientCooldown = 100;
    private double locomotionDistance, previousLocomotionDistance;
    private boolean strikeApplied;

    public CrystalWormEntity(EntityType<? extends CrystalWormEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        blocksBuilding = false;
        setNoGravity(true);
        xpReward = 6;
        entityData.set(START, level.getGameTime());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED).add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.KNOCKBACK_RESISTANCE, .75);
    }

    public static boolean checkNaturalSpawn(EntityType<CrystalWormEntity> type, LevelAccessor accessor,
                                           EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return true;
        return accessor instanceof ServerLevelAccessor a && a.getLevel().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && CrystalWormSpawning.habitat(a.getLevel(), pos)
                && random.nextFloat() < CrystalWormSpawning.naturalChance(CrystalWormSpawning.depth(a.getLevel(), pos));
    }

    @Override public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        return reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION || level instanceof ServerLevelAccessor a
                && CrystalWormSpawning.habitat(a.getLevel(), blockPosition());
    }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.getFluidState(blockPosition()).isEmpty(); }
    @Override public int getMaxSpawnClusterSize() { return 1; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean canDrownInFluidType(FluidType type) { return false; }
    @Override public boolean isAffectedByFluids() { return false; }

    @Override protected AABB makeBoundingBox(Vec3 p) {
        // Cover the authored long body rather than leaving only its tiny root hittable.
        Vec3 forward = Vec3.directionFromRotation(getXRot(), getYRot());
        double scale = getBbWidth() / .7;
        Vec3 nose = p.add(forward.scale(.9 * scale)), tail = p.subtract(forward.scale(2.35 * scale));
        return new AABB(Math.min(nose.x, tail.x) - .36 * scale, Math.min(p.y, Math.min(nose.y, tail.y)),
                Math.min(nose.z, tail.z) - .36 * scale, Math.max(nose.x, tail.x) + .36 * scale,
                Math.max(p.y, Math.max(nose.y, tail.y)) + getBbHeight(), Math.max(nose.z, tail.z) + .36 * scale);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, (byte) Phase.IDLE.ordinal());
        builder.define(START, 0L);
    }
    @Override protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, null));
    }
    public Phase phase() { return PHASES[entityData.get(PHASE)]; }
    public float phaseTicks(float partialTick) { return Math.max(0, level().getGameTime() - entityData.get(START) + partialTick); }
    private void change(Phase phase) {
        entityData.set(PHASE, (byte) phase.ordinal());
        entityData.set(START, level().getGameTime());
    }

    @Override public void tick() {
        noPhysics = true;
        setNoGravity(true);
        super.tick();
        setBoundingBox(makeBoundingBox());
    }

    private boolean validTarget(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && target.level() == level() && canAttack(target)
                && distanceToSqr(target) < 48 * 48 && !(target instanceof Player p && (p.isCreative() || p.isSpectator()));
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!isAlive()) { setDeltaMovement(Vec3.ZERO); return; }
        if (attackCooldown > 0) attackCooldown--;
        if (routeRetry > 0) routeRetry--;
        if (hurtRecovery > 0) hurtRecovery--;
        if (--ambientCooldown <= 0) {
            ambientCooldown = 120 + random.nextInt(121);
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME, .15F, .7F);
        }
        LivingEntity target = getTarget();
        if (!validTarget(target)) { setTarget(null); target = null; }
        int age = (int) phaseTicks(0);
        switch (phase()) {
            case BURROW_IN -> {
                driveBurrow(level, true, age);
                if (age * BURROW_PLAYBACK_SPEED >= CrystalWormBurrowMotion.ENTRY_TICKS) { change(Phase.HIDDEN); exitStage = null; }
            }
            case BURROW_OUT -> {
                driveBurrow(level, false, age);
                if (age * BURROW_PLAYBACK_SPEED >= CrystalWormBurrowMotion.EXIT_TICKS) {
                    lastSurface = position(); exposedTicks = 100; attackCooldown = 8;
                    waypoint = null; change(Phase.FAST);
                }
            }
            case HIDDEN -> hidden(level, target, age);
            case ATTACK -> attack(level, age);
            case HURT -> {
                setDeltaMovement(getDeltaMovement().scale(.5));
                if (age >= HURT_TICKS) change(target == null ? Phase.IDLE : Phase.FAST);
            }
            case SENSE -> { setDeltaMovement(Vec3.ZERO); if (target != null || age >= 64) change(Phase.IDLE); }
            case START_MOVE -> { advance(waypoint, .5); if (age >= 12) change(Phase.MOVE); }
            case ACCELERATE -> { advance(waypoint, .8); if (age >= ACCELERATE_TICKS) change(Phase.FAST); }
            case DECELERATE -> { advance(waypoint, .5); if (age >= 8) change(Phase.MOVE); }
            case STOP_MOVE -> { setDeltaMovement(getDeltaMovement().scale(.6)); if (age >= 12) change(Phase.IDLE); }
            case IDLE, MOVE, FAST -> exposed(level, target, age);
        }
    }

    private void exposed(ServerLevel level, LivingEntity target, int age) {
        exposedTicks--;
        if (target != null) {
            waypoint = floorNear(level, target.position(), 5);
            if (waypoint == null) waypoint = target.position();
            if (attackCooldown == 0 && canStrike(target)) {
                strikeTarget = target; strikeApplied = false; setDeltaMovement(Vec3.ZERO); face(target.position(), 12); change(Phase.ATTACK); return;
            }
            if (exposedTicks <= 0 && attackCooldown < 40 && beginDive(level)) return;
            if (phase() == Phase.IDLE || phase() == Phase.MOVE) { change(Phase.ACCELERATE); return; }
            // After emergence, remain available for several seconds before diving again.
            if (distanceToSqr(target) < 3.5 && attackCooldown > 0) setDeltaMovement(getDeltaMovement().scale(.65));
            else advance(waypoint, .95);
        } else {
            if (exposedTicks <= 0 && beginDive(level)) return;
            if (phase() == Phase.FAST) { change(Phase.DECELERATE); return; }
            if (phase() == Phase.IDLE) {
                setDeltaMovement(Vec3.ZERO);
                setXRot(Mth.approachDegrees(getXRot(), 0, 4));
                if (age > 30 + getId() % 40 && routeRetry == 0) {
                    if (random.nextInt(4) == 0) change(Phase.SENSE);
                    else if (chooseWander(level)) change(Phase.START_MOVE);
                }
            } else if (waypoint == null || position().distanceToSqr(waypoint) < .3 || age > 120) {
                waypoint = null; change(Phase.STOP_MOVE);
            } else advance(waypoint, .5);
        }
    }

    private boolean canStrike(LivingEntity target) {
        Vec3 head = position().add(Vec3.directionFromRotation(0, getYRot()).scale(.65)).add(0, .35, 0);
        return AABB.ofSize(head, 2.1, 1.6, 2.1).intersects(target.getBoundingBox()) && hasLineOfSight(target)
                && level().getBlockState(BlockPos.containing(head)).isAir();
    }
    private void attack(ServerLevel level, int age) {
        if (!validTarget(strikeTarget)) { strikeTarget = null; setDeltaMovement(Vec3.ZERO); change(Phase.IDLE); return; }
        if (age < 5) face(strikeTarget.position(), 10);
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        setDeltaMovement(forward.scale(age >= 5 && age <= 8 ? .10 : age > 8 && age < 12 ? -.015 : 0));
        if (!strikeApplied && age >= 8) {
            strikeApplied = true;
            if (canStrike(strikeTarget)) doHurtTarget(level, strikeTarget);
            playSound(SoundEvents.STONE_BREAK, .5F, .85F);
        }
        if (age >= 24) {
            attackCooldown = 90; exposedTicks = 60; strikeTarget = null;
            change(Phase.IDLE);
        }
    }

    private boolean beginDive(ServerLevel level) {
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        Vec3 end = transform(position(), forward, CrystalWormBurrowMotion.sample(true, CrystalWormBurrowMotion.ENTRY_TICKS));
        if (!loadedRoute(level, position(), end)) { routeRetry = 40; exposedTicks = 40; return false; }
        lastSurface = position(); burrowAnchor = position(); burrowDirection = forward;
        waypoint = null; setDeltaMovement(Vec3.ZERO); change(Phase.BURROW_IN);
        playSound(SoundEvents.STONE_BREAK, .45F, .65F);
        return true;
    }
    private static Vec3 transform(Vec3 anchor, Vec3 direction, Vec3 offset) {
        return anchor.add(direction.scale(offset.z)).add(0, offset.y, 0);
    }
    private static boolean loadedRoute(ServerLevel level, Vec3 start, Vec3 end) {
        return end.y > level.getMinY() + 3 && end.y < level.getMaxY() - 3
                && level.hasChunksAt(BlockPos.containing(Math.min(start.x, end.x) - 3, Math.min(start.y, end.y) - 1, Math.min(start.z, end.z) - 3),
                BlockPos.containing(Math.max(start.x, end.x) + 3, Math.max(start.y, end.y) + 2, Math.max(start.z, end.z) + 3));
    }
    private void driveBurrow(ServerLevel level, boolean entry, int age) {
        if (burrowAnchor == null || burrowDirection == null) { change(Phase.HIDDEN); return; }
        Vec3 next = transform(burrowAnchor, burrowDirection, CrystalWormBurrowMotion.sample(entry, age * BURROW_PLAYBACK_SPEED));
        if (!loadedRoute(level, position(), next)) { setDeltaMovement(Vec3.ZERO); change(Phase.HIDDEN); routeRetry = 40; return; }
        // Ordinary entity motion synchronizes the actual position; no separate per-tick packets.
        setDeltaMovement(next.subtract(position()));
        setXRot(0);
    }

    private void hidden(ServerLevel level, LivingEntity target, int age) {
        if (exitStage == null && routeRetry == 0 || target != null && age % 30 == 0 && exitStage != null
                && target.position().distanceToSqr(exitStage) > 20 * 20) {
            prepareExit(level, target);
        }
        if (exitStage == null) { setDeltaMovement(getDeltaMovement().scale(.7)); return; }
        if (position().distanceToSqr(exitStage) <= .2 && age >= 20) {
            Vec3 end = transform(position(), exitDirection, CrystalWormBurrowMotion.sample(false, CrystalWormBurrowMotion.EXIT_TICKS));
            if (!loadedRoute(level, position(), end)) { exitStage = null; routeRetry = 40; return; }
            burrowAnchor = position(); burrowDirection = exitDirection;
            float yaw = (float)(Mth.atan2(exitDirection.z, exitDirection.x) * 180 / Math.PI) - 90;
            setYRot(yaw); setYHeadRot(yaw); yBodyRot = yaw; setXRot(0);
            setDeltaMovement(Vec3.ZERO); change(Phase.BURROW_OUT);
            playSound(SoundEvents.STONE_BREAK, .5F, .7F);
        } else advance(exitStage, 1.15);
    }

    private void prepareExit(ServerLevel level, LivingEntity target) {
        routeRetry = 30;
        Vec3 near = target == null ? lastSurface : target.position();
        if (near == null || !level.hasChunkAt(BlockPos.containing(near))) return;
        Vec3 horizontal = new Vec3(near.x - getX(), 0, near.z - getZ());
        Vec3 direction = horizontal.lengthSqr() < .1 ? Vec3.directionFromRotation(0, getYRot()) : horizontal.normalize();
        Vec3 end = floorNear(level, target == null ? near : near.subtract(direction.scale(2.4)), 6);
        if (end == null) return;
        Vec3 exitMotion = CrystalWormBurrowMotion.sample(false, CrystalWormBurrowMotion.EXIT_TICKS);
        Vec3 stage = end.subtract(direction.scale(exitMotion.z)).add(0, -exitMotion.y, 0);
        if (!loadedRoute(level, position(), stage) || !loadedRoute(level, stage, end)) return;
        exitStage = stage; exitDirection = direction;
    }

    private @Nullable Vec3 floorNear(ServerLevel level, Vec3 near, int range) {
        BlockPos top = BlockPos.containing(near.x, near.y + 2, near.z);
        if (!level.hasChunkAt(top)) return null;
        for (int i = 0; i < range; i++) {
            BlockPos p = top.below(i);
            if (p.getY() <= level.getMinY() + 4 || !level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()
                    || !level.getFluidState(p.below()).isEmpty()
                    || level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) continue;
            return Vec3.atBottomCenterOf(p).add(0, .05, 0);
        }
        return null;
    }
    private boolean chooseWander(ServerLevel level) {
        routeRetry = 40;
        for (int i = 0; i < 8; i++) {
            Vec3 near = position().add(random.nextInt(15) - 7, random.nextInt(5) - 2, random.nextInt(15) - 7);
            Vec3 floor = floorNear(level, near, 6);
            if (floor == null || position().distanceToSqr(floor) < 4 || !loadedRoute(level, position(), floor)) continue;
            waypoint = floor; return true;
        }
        return false;
    }
    private void face(Vec3 point, float maxTurn) {
        Vec3 delta = point.subtract(position());
        if (delta.horizontalDistanceSqr() < .001) return;
        float yaw = (float)(Mth.atan2(delta.z, delta.x) * 180 / Math.PI) - 90;
        setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(yaw - getYRot()), -maxTurn, maxTurn));
        setYHeadRot(getYRot()); yBodyRot = getYRot();
    }
    private void advance(@Nullable Vec3 point, double speedFactor) {
        if (point == null) { setDeltaMovement(getDeltaMovement().scale(.65)); return; }
        Vec3 delta = point.subtract(position());
        if (!level().hasChunkAt(BlockPos.containing(position().add(delta.normalize().scale(2))))) {
            setDeltaMovement(Vec3.ZERO); routeRetry = 40; return;
        }
        face(point, 11);
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        Vec3 toward = delta.lengthSqr() < .001 ? Vec3.ZERO : delta.normalize();
        double speed = Math.min(getAttributeValue(Attributes.MOVEMENT_SPEED) * speedFactor, delta.length() * .4);
        double alignment = Math.max(0, forward.x * toward.x + forward.z * toward.z);
        Vec3 desired = forward.scale(speed * toward.horizontalDistance() * alignment).add(0, toward.y * speed, 0);
        setDeltaMovement(getDeltaMovement().lerp(desired, .28));
        float pitch = (float)(-Mth.atan2(getDeltaMovement().y, Math.max(.04, getDeltaMovement().horizontalDistance())) * 180 / Math.PI);
        setXRot(getXRot() + Mth.clamp(Mth.wrapDegrees(Mth.clamp(pitch, -48, 48) - getXRot()), -4, 4));
    }
    @Override public void travel(Vec3 input) {
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(.88));
    }

    @Override protected void updateWalkAnimation(float distance) {
        super.updateWalkAnimation(distance);
        if (!level().isClientSide()) return;
        previousLocomotionDistance = locomotionDistance;
        Phase phase = phase();
        double travelled = Mth.length(getX() - xo, getY() - yo, getZ() - zo);
        // Follow actual interpolated movement. Teleport corrections must not race the body through cycles.
        if ((phase == Phase.MOVE || phase == Phase.FAST || phase == Phase.HIDDEN)
                && Double.isFinite(travelled) && travelled <= 1.5 && getScale() > 0) {
            locomotionDistance += travelled / getScale();
        }
    }

    public float locomotionAnimationTime(Phase phase, float partialTick) {
        double distance = Mth.lerp(Mth.clamp(partialTick, 0, 1), previousLocomotionDistance, locomotionDistance);
        double cycles = distance / LOCOMOTION_CYCLE_DISTANCE;
        return (float)((cycles - Math.floor(cycles)) * (phase == Phase.MOVE ? 1.6 : .8));
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypeTags.IS_FALL)) return false;
        boolean magical = source.is(MAGIC) || source.is(Tags.DamageTypes.IS_MAGIC);
        var weapon = source.getWeaponItem();
        boolean pickaxe = weapon != null && (weapon.is(ItemTags.PICKAXES) || weapon.is(MINING_TOOLS));
        boolean attack = source.getEntity() instanceof LivingEntity || source.is(DamageTypeTags.IS_PROJECTILE);
        if (attack && !magical && !pickaxe && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) amount *= .25F;
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && isAlive() && phase() != Phase.BURROW_IN && phase() != Phase.BURROW_OUT && phase() != Phase.HIDDEN) {
            // Repeated hits still deal normal damage but cannot continually reset hard stun or cancel a strike.
            if (phase() != Phase.ATTACK && phase() != Phase.HURT && hurtRecovery == 0) {
                hurtRecovery = HURT_RECOVERY_TICKS;
                change(Phase.HURT);
            }
            exposedTicks = Math.max(exposedTicks, 40);
        }
        return hurt;
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.AMETHYST_BLOCK_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.AMETHYST_BLOCK_BREAK; }
    @Override protected void tickDeath() {
        setDeltaMovement(Vec3.ZERO);
        // Vanilla's 20-tick removal would truncate the authored 2.2-second death.
        deathTime++;
        if (deathTime >= 44 && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60); remove(RemovalReason.KILLED);
        }
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<CrystalWormEntity>("attitude", 0, state -> state.setAndContinue(ATTITUDE)));
        controllers.add(new AnimationController<CrystalWormEntity>("worm", 4, state -> {
            Phase phase = phase();
            float time = phaseTicks(state.renderState().getPartialTick()) / 20F;
            RawAnimation animation;
            if (!isAlive()) { animation = DEATH; time = (deathTime + state.renderState().getPartialTick()) / 20F; }
            else {
                animation = ANIMATIONS[phase.ordinal()];
                time = switch (phase) {
                    case IDLE -> time % 3.2F;
                    case MOVE, FAST, HIDDEN -> locomotionAnimationTime(phase, state.renderState().getPartialTick());
                    case HURT -> time * (12F / HURT_TICKS);
                    case ACCELERATE -> time * (8F / ACCELERATE_TICKS);
                    case BURROW_IN, BURROW_OUT -> time * BURROW_PLAYBACK_SPEED;
                    default -> time;
                };
            }
            state.setAnimation(animation);
            state.controller().setAnimationTime(time);
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Burrowed", phase() == Phase.HIDDEN || phase() == Phase.BURROW_IN || phase() == Phase.BURROW_OUT);
        output.putInt("AttackCooldown", attackCooldown);
        output.putInt("ExposedTicks", exposedTicks);
        if (lastSurface != null) output.store("LastSurface", Vec3.CODEC, lastSurface);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        noPhysics = true; blocksBuilding = false; setNoGravity(true);
        var movement = getAttribute(Attributes.MOVEMENT_SPEED);
        // Upgrade only the previously published default; preserve player/mod-authored attribute values.
        if (movement != null && Math.abs(movement.getBaseValue() - .18) < 1e-9) movement.setBaseValue(BASE_MOVEMENT_SPEED);
        lastSurface = input.read("LastSurface", Vec3.CODEC).orElse(position());
        attackCooldown = Math.clamp(input.getIntOr("AttackCooldown", 0), 0, 100);
        exposedTicks = Math.clamp(input.getIntOr("ExposedTicks", 120), 20, 200);
        waypoint = null; burrowAnchor = null; exitStage = null; strikeTarget = null; hurtRecovery = 0;
        change(input.getBooleanOr("Burrowed", false) ? Phase.HIDDEN : Phase.IDLE);
    }
}
