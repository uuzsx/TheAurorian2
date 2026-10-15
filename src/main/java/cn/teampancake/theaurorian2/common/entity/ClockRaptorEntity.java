package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** A desert sky bird with long patrols and brief, infrequent rests. All flight is server-authoritative. */
public class ClockRaptorEntity extends PathfinderMob implements GeoEntity {
    private static final int MIN_FLIGHT_TICKS = 8 * 60 * 20;
    private static final int MAX_FLIGHT_TICKS = 15 * 60 * 20;
    public static final TagKey<Block> SPAWNABLE_ON = TagKey.create(Registries.BLOCK,
            TheAurorian2.id("clock_raptor_spawnable_on"));
    public enum FlightPhase { GROUNDED, TAKEOFF, HOVER, HOVER_TO_FLY, FLY, FLY_TO_HOVER, LAND }
    private static final FlightPhase[] PHASES = FlightPhase.values();
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ClockRaptorEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation[] FLIGHT = {
            IDLE, RawAnimation.begin().thenPlayAndHold("takeoff"), RawAnimation.begin().thenLoop("hover"),
            RawAnimation.begin().thenPlayAndHold("hover_to_fly"), RawAnimation.begin().thenLoop("fly"),
            RawAnimation.begin().thenPlayAndHold("fly_to_hover"), RawAnimation.begin().thenPlayAndHold("land")};
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final PathNavigation groundNavigation;
    private final FlyingPathNavigation airNavigation;
    private final MoveControl groundMoveControl;
    private final MoveControl airMoveControl;
    private int flightCooldown, phaseTicks, flightTicksRemaining, frightenedTicks;
    private boolean resumeFlight, redirectFlight;
    private BlockPos flightHome;
    private Vec3 frightOrigin;
    private double walkTime, previousWalkTime;

    public ClockRaptorEntity(EntityType<? extends ClockRaptorEntity> type, Level level) {
        super(type, level);
        groundNavigation = navigation;
        groundMoveControl = moveControl;
        airNavigation = new FlyingPathNavigation(this, level);
        airNavigation.setCanFloat(true);
        airMoveControl = new FlightMoveControl();
        flightCooldown = 20 + random.nextInt(20);
        xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ARMOR, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.18).add(Attributes.FLYING_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 32).add(Attributes.STEP_HEIGHT, 1);
    }

    public static boolean checkSpawnRules(EntityType<ClockRaptorEntity> type, LevelAccessor level,
                                         EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // Eggs/commands remain useful outside the habitat; only natural generation is restricted.
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return true;
        return level instanceof ServerLevelAccessor server && server.getLevel().dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                && level.getBiome(pos).is(TheAurorian2.id("aurorian_desert"))
                && level.getBlockState(pos.below()).is(SPAWNABLE_ON)
                && pos.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())
                && level.getFluidState(pos).isEmpty() && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, FlightPhase.GROUNDED.ordinal());
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FlightGoal());
        goalSelector.addGoal(2, new PanicGoal(this, 1.4));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 7));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public FlightPhase flightPhase() { return PHASES[entityData.get(PHASE)]; }

    private void setPhase(FlightPhase phase) {
        entityData.set(PHASE, phase.ordinal()); // Only state transitions generate entity data packets.
        phaseTicks = 0;
    }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (flightCooldown > 0) flightCooldown--;
            if (frightenedTicks > 0 && --frightenedTicks == 0) frightOrigin = null;
            if ((!isAlive() || isNoAi()) && flightPhase() != FlightPhase.GROUNDED) finishFlight();
        }
        // Safe descent when flight is disabled, obstructed, or interrupted by a leash/passenger.
        if (isAlive() && !onGround() && !isInLiquid() && flightPhase() == FlightPhase.GROUNDED
                && getDeltaMovement().y < -0.12) {
            setDeltaMovement(getDeltaMovement().multiply(1, 0, 1).add(0, -0.12, 0));
        }
    }

    @Override public void travel(Vec3 input) {
        if (isNoGravity() && flightPhase() != FlightPhase.GROUNDED && !isInLiquid()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91));
        } else {
            super.travel(input);
        }
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && isAlive()) {
            frightenedTicks = 100;
            frightOrigin = source.getSourcePosition();
            flightCooldown = 0;
            redirectFlight = true;
            if (flightPhase() != FlightPhase.GROUNDED) flightTicksRemaining = Math.max(flightTicksRemaining, 600);
        }
        return hurt;
    }

    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }
    @Override public int getMaxSpawnClusterSize() { return 2; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PARROT_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 0.7F; }

    private void finishFlight() {
        navigation.stop();
        moveControl.setWait();
        navigation = groundNavigation;
        moveControl = groundMoveControl;
        moveControl.setWait();
        setNoGravity(false);
        setPhase(FlightPhase.GROUNDED);
        flightCooldown = frightenedTicks > 0 ? 0 : 40 + random.nextInt(41);
        flightTicksRemaining = 0;
        resumeFlight = false;
        redirectFlight = false;
        frightOrigin = null;
    }

    private int newFlightDuration() { return MIN_FLIGHT_TICKS + random.nextInt(MAX_FLIGHT_TICKS - MIN_FLIGHT_TICKS + 1); }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("FlightCooldown", flightCooldown);
        output.putInt("FlightTicksRemaining", flightTicksRemaining);
        output.putBoolean("Flying", resumeFlight || flightPhase() != FlightPhase.GROUNDED);
        if (flightHome != null) output.store("FlightHome", BlockPos.CODEC, flightHome);
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Old saves stored NoGravity but no flight timer. Rebuild the route instead of saving paths.
        boolean wasFlying = input.getBooleanOr("Flying", isNoGravity());
        finishFlight();
        flightCooldown = Math.clamp(input.getIntOr("FlightCooldown", 20), 0, 80);
        flightTicksRemaining = Math.clamp(input.getIntOr("FlightTicksRemaining", newFlightDuration()), 0, MAX_FLIGHT_TICKS);
        flightHome = input.read("FlightHome", BlockPos.CODEC).orElse(blockPosition());
        resumeFlight = wasFlying && !isNoAi();
    }

    @Override protected void updateWalkAnimation(float distance) {
        super.updateWalkAnimation(distance);
        previousWalkTime = walkTime;
        if (onGround() && flightPhase() == FlightPhase.GROUNDED && distance <= 1.5F) {
            // Authored contact foot travels 4.9 model pixels in 0.58 seconds per stride.
            walkTime += distance / (4.9 / 16.0 / 0.58 * getScale());
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ClockRaptorEntity>("locomotion", 3, state -> {
            FlightPhase phase = flightPhase();
            if (phase != FlightPhase.GROUNDED) return state.setAndContinue(FLIGHT[phase.ordinal()]);
            if (!onGround() && getDeltaMovement().y < -0.04) return state.setAndContinue(FLIGHT[FlightPhase.HOVER.ordinal()]);
            if (state.isMoving()) {
                state.controller().setAnimationTime(Mth.lerp(state.renderState().getPartialTick(), previousWalkTime, walkTime) % 1.0);
                return state.setAndContinue(WALK);
            }
            return state.setAndContinue(IDLE);
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private final class FlightMoveControl extends MoveControl {
        FlightMoveControl() { super(ClockRaptorEntity.this); }
        @Override public void tick() {
            if (operation != Operation.MOVE_TO) {
                setDeltaMovement(getDeltaMovement().scale(0.8));
                return;
            }
            operation = Operation.WAIT;
            Vec3 delta = new Vec3(wantedX - getX(), wantedY - getY(), wantedZ - getZ());
            double distance = delta.length();
            double speed = Math.min(speedModifier * getAttributeValue(Attributes.FLYING_SPEED), distance * 0.4);
            Vec3 velocity = distance > 0.001 ? delta.scale(speed / distance) : Vec3.ZERO;
            setDeltaMovement(getDeltaMovement().lerp(velocity, 0.3));
            if (delta.horizontalDistanceSqr() > 0.04) {
                float yaw = (float) (Mth.atan2(delta.z, delta.x) * 180 / Math.PI) - 90;
                setYRot(rotlerp(getYRot(), yaw, 8));
                setYHeadRot(getYRot());
                yBodyRot = getYRot();
            }
            setXRot(0); // Pitch belongs to the authored torso animation, not the whole collision body.
        }
    }

    private final class FlightGoal extends Goal {
        private Vec3 departure, landing, cruise;
        private int legTicks, landingRetryTicks;
        private boolean approachingLanding;
        FlightGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)); }

        @Override public boolean canUse() {
            if ((!resumeFlight && flightCooldown > 0) || isInLiquid() || isPassenger() || isLeashed()) return false;
            flightCooldown = 40; // Bounded retry, including obstructed or unloaded surroundings.
            if (getY() < level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getBlockX(), getBlockZ())
                    || !clearAt(position().add(0, onGround() ? 3 : 0, 0))) return false;
            if (flightHome == null || horizontalDistanceSqr(position(), Vec3.atBottomCenterOf(flightHome)) > 64 * 64) {
                flightHome = blockPosition();
            }
            departure = position().add(0, onGround() ? 3 : 0, 0);
            return chooseCruiseTarget();
        }

        private double horizontalDistanceSqr(Vec3 a, Vec3 b) {
            double x = a.x - b.x, z = a.z - b.z;
            return x * x + z * z;
        }

        private boolean clearAt(Vec3 pos) {
            var box = getBoundingBox().move(pos.subtract(position()));
            // Collision queries must never pull in an unloaded neighbor at a chunk edge.
            BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
            BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
            return level().hasChunksAt(min, max) && level().noCollision(ClockRaptorEntity.this, box)
                    && !level().containsAnyLiquid(box);
        }

        private boolean safeGround(BlockPos pos) {
            return level().getBlockState(pos.below()).isFaceSturdy(level(), pos.below(), net.minecraft.core.Direction.UP)
                    && level().getFluidState(pos.below()).isEmpty() && clearAt(Vec3.atBottomCenterOf(pos));
        }

        private boolean chooseCruiseTarget() {
            Vec3 home = Vec3.atBottomCenterOf(flightHome);
            double heading = Math.toRadians(getYRot() + 90);
            if (horizontalDistanceSqr(position(), home) > 28 * 28) {
                heading = Mth.atan2(home.z - getZ(), home.x - getX());
            } else if (frightenedTicks > 0 && frightOrigin != null) {
                heading = Mth.atan2(getZ() - frightOrigin.z, getX() - frightOrigin.x);
            }
            for (int i = 0; i < 8; i++) {
                // Prefer a forward arc, then widen the search if terrain blocks that arc.
                double angle = heading + (random.nextDouble() - 0.5) * (i < 4 ? 1.8 : Math.PI * 2);
                double radius = 16 + random.nextInt(11);
                double x = getX() + Math.cos(angle) * radius, z = getZ() + Math.sin(angle) * radius;
                BlockPos column = BlockPos.containing(x, getY(), z);
                if (!level().hasChunkAt(column) || horizontalDistanceSqr(Vec3.atBottomCenterOf(column), home) > 48 * 48) continue;
                int surface = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
                double y = Mth.clamp(surface + 12 + random.nextInt(9), getY() - 6, getY() + 8);
                y = Mth.clamp(y, level().getMinY() + 2, level().getMaxY() - getBbHeight() - 2);
                Vec3 target = new Vec3(x, y, z);
                if (!clearAt(target)) continue;
                cruise = target;
                legTicks = 0;
                approachingLanding = false;
                redirectFlight = false;
                return true;
            }
            return false;
        }

        private boolean chooseLandingTarget() {
            // Only called when the multi-minute flight expires; failed searches wait ten seconds.
            landingRetryTicks = 200;
            for (int i = 0; i < 8; i++) {
                BlockPos column = i == 0 ? blockPosition() : blockPosition().offset(random.nextInt(25) - 12, 0, random.nextInt(25) - 12);
                if (!level().hasChunkAt(column)) continue;
                BlockPos ground = level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
                if (ground.getY() > getY() || getY() - ground.getY() > 32 || !safeGround(ground)) continue;
                landing = Vec3.atBottomCenterOf(ground);
                cruise = landing.add(0, 1.6, 0);
                if (!clearAt(cruise)) continue;
                legTicks = 0;
                approachingLanding = true;
                return true;
            }
            return false;
        }

        @Override public void start() {
            navigation.stop();
            groundMoveControl.setWait();
            setZza(0);
            navigation = airNavigation;
            moveControl = airMoveControl;
            if (!resumeFlight) flightTicksRemaining = newFlightDuration();
            resumeFlight = false;
            landingRetryTicks = 0;
            setNoGravity(!onGround());
            setPhase(onGround() ? FlightPhase.TAKEOFF : FlightPhase.HOVER);
        }

        @Override public boolean canContinueToUse() {
            return flightPhase() != FlightPhase.GROUNDED && isAlive() && !isInLiquid()
                    && !isPassenger() && !isLeashed();
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() {
            // Do not erase a new hurt response between landing and removal of this completed goal.
            if (flightPhase() != FlightPhase.GROUNDED) finishFlight();
        }

        private void aim(Vec3 target, double speed) {
            navigation.stop();
            moveControl.setWantedPosition(target.x, target.y, target.z, speed);
        }

        private void hoverHere() {
            navigation.stop();
            moveControl.setWait();
            departure = position();
            approachingLanding = false;
            setPhase(FlightPhase.HOVER);
        }

        private void navigateCruise() {
            navigation.moveTo(cruise.x, cruise.y, cruise.z, frightenedTicks > 0 ? 1.25 : 1);
        }

        @Override public void tick() {
            phaseTicks++;
            if (flightTicksRemaining > 0) flightTicksRemaining--;
            if (landingRetryTicks > 0) landingRetryTicks--;
            switch (flightPhase()) {
                case TAKEOFF -> {
                    if (phaseTicks >= 6) {
                        setNoGravity(true);
                        aim(departure, frightenedTicks > 0 ? 1.2 : 1);
                    }
                    if (phaseTicks >= 24) setPhase(FlightPhase.HOVER);
                }
                case HOVER -> {
                    aim(departure, 0.8);
                    if (phaseTicks >= 32) {
                        if (chooseCruiseTarget()) setPhase(FlightPhase.HOVER_TO_FLY);
                        else phaseTicks = 0;
                    }
                }
                case HOVER_TO_FLY, FLY -> {
                    legTicks++;
                    if (redirectFlight && phaseTicks % 20 == 0) {
                        if (!chooseCruiseTarget()) { hoverHere(); return; }
                    }
                    if (flightTicksRemaining == 0 && !approachingLanding && landingRetryTicks == 0) chooseLandingTarget();
                    boolean arrived = position().distanceToSqr(cruise) < 1.5;
                    if (flightPhase() == FlightPhase.FLY && arrived && approachingLanding) {
                        setPhase(FlightPhase.FLY_TO_HOVER);
                        aim(landing.add(0, 1.6, 0), 0.9);
                        return;
                    }
                    if (arrived || legTicks >= 200) {
                        if (!chooseCruiseTarget()) { hoverHere(); return; }
                    }
                    if (phaseTicks == 1 || phaseTicks % 20 == 0 || legTicks == 0) navigateCruise();
                    if (flightPhase() == FlightPhase.HOVER_TO_FLY && phaseTicks >= 16) setPhase(FlightPhase.FLY);
                }
                case FLY_TO_HOVER -> {
                    if (redirectFlight) { hoverHere(); return; }
                    aim(landing.add(0, 1.6, 0), 0.9);
                    if (phaseTicks >= 16) setPhase(FlightPhase.LAND);
                }
                case LAND -> {
                    if (redirectFlight || !safeGround(BlockPos.containing(landing))) { hoverHere(); return; }
                    aim(landing.add(0, -0.05, 0), 0.8);
                    if (onGround()) setDeltaMovement(Vec3.ZERO);
                    if (phaseTicks >= 28 && onGround()) finishFlight();
                    else if (phaseTicks >= 65) hoverHere();
                }
                default -> { }
            }
        }
    }
}
