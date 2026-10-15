package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Small amphibious sky flocks. Only a leader searches for food or chooses a patrol. */
public final class SeaDrifterEntity extends PathfinderMob implements GeoEntity {
    public static final TagKey<Biome> HABITAT = TagKey.create(Registries.BIOME, TheAurorian2.id("has_sea_drifter"));
    public static final TagKey<EntityType<?>> PREY = TagKey.create(Registries.ENTITY_TYPE, TheAurorian2.id("sea_drifter_prey"));
    public enum Activity { CRUISE, HOVER, DIVE, RETURN, DEFEND }
    private static final EntityDataAccessor<Integer> MOTION = SynchedEntityData.defineId(SeaDrifterEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation HOVER = RawAnimation.begin().thenPlay("fly_to_hover").thenLoop("idle_hover");
    private static final RawAnimation FLY = RawAnimation.begin().thenPlay("hover_to_fly").thenLoop("fly");
    private static final RawAnimation GLIDE = RawAnimation.begin().thenPlay("fly_to_glide").thenLoop("glide");
    private static final RawAnimation CURIOUS = RawAnimation.begin().thenPlay("idle_curious").thenLoop("idle_hover");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("hurt");
    private static final RawAnimation ATTITUDE = RawAnimation.begin().thenLoop("flight_attitude");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private UUID flockId, leaderId, angerId;
    private boolean leader, spawnCompanions;
    private int slot, orphanTicks, angerTicks, biteCooldown, activityTicks, routeTicks, flockRefresh, ascentRetry;
    private int huntCooldown = 600 + random.nextInt(601), hoverCooldown = 400 + random.nextInt(301);
    private Activity activity = Activity.CRUISE;
    private Vec3 home, destination, ascentTarget;
    private LivingEntity prey;
    private List<SeaDrifterEntity> flock = List.of();
    private double orbitAngle;

    public SeaDrifterEntity(EntityType<? extends SeaDrifterEntity> type, Level level) {
        super(type, level);
        moveControl = new SeaFlightControl();
        setNoGravity(true);
        xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.MOVEMENT_SPEED, .22).add(Attributes.FLYING_SPEED, .25)
                .add(Attributes.FOLLOW_RANGE, 48).add(Attributes.SCALE, .55);
    }

    @Override protected void registerGoals() { }
    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOTION, 0);
    }
    public boolean isFlockLeader() { return leader; }
    public UUID flockId() { return flockId; }
    public UUID leaderId() { return leaderId; }
    public Activity activity() { return activity; }

    public static boolean checkSpawnRules(EntityType<SeaDrifterEntity> type, LevelAccessor level,
                                         EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return true;
        if (!(level instanceof ServerLevelAccessor server) || !server.getLevel().dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                || !level.getBiome(pos).is(HABITAT)) return false;
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return pos.getY() >= surface - 2 && pos.getY() <= surface + 1
                && level.getFluidState(new BlockPos(pos.getX(), surface - 1, pos.getZ())).is(FluidTags.WATER)
                && level.getBlockState(new BlockPos(pos.getX(), surface, pos.getZ())).isAir();
    }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public int getMaxSpawnClusterSize() { return 4; }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public boolean causeFallDamage(double distance, float multiplier, DamageSource source) { return false; }

    private static final class FlockSpawnData implements SpawnGroupData {
        final UUID id;
        final boolean allowed;
        int count = 1;
        FlockSpawnData(UUID id, boolean allowed) { this.id = id; this.allowed = allowed; }
    }
    @EventBusSubscriber(modid = TheAurorian2.MOD_ID)
    public static final class SpawnEvents {
        private SpawnEvents() { }
        @SubscribeEvent
        public static void spaceNaturalFlocks(FinalizeSpawnEvent event) {
            if (!(event.getEntity() instanceof SeaDrifterEntity ray) || event.isSpawnCancelled()) return;
            EntitySpawnReason reason = event.getSpawnType();
            if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return;
            FlockSpawnData data;
            if (event.getSpawnData() instanceof FlockSpawnData existing) {
                data = existing;
            } else {
                // Decide once per flock, so density checks never reject its own followers.
                // Generation runs off-thread with unloaded entities: thin whole packs there,
                // and query only loaded entities for subsequent natural replenishment.
                boolean allowed = reason == EntitySpawnReason.CHUNK_GENERATION
                        ? event.getLevel().getRandom().nextInt(4) == 0
                        : event.getLevel().getEntitiesOfClass(SeaDrifterEntity.class,
                                ray.getBoundingBox().inflate(96, 48, 96), other -> other.isAlive()
                                        && other.position().subtract(ray.position()).horizontalDistanceSqr() < 96*96).isEmpty();
                data = new FlockSpawnData(ray.getUUID(), allowed);
                event.setSpawnData(data);
            }
            if (!data.allowed) event.setSpawnCancelled(true);
        }
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason reason, SpawnGroupData group) {
        super.finalizeSpawn(level, difficulty, reason, group);
        if (group instanceof FlockSpawnData data && !data.id.equals(getUUID())) {
            flockId = data.id;
            leaderId = data.id;
            slot = Math.clamp(data.count++, 1, 3);
        } else {
            initializeLeader();
            if (!(group instanceof FlockSpawnData)) group = new FlockSpawnData(getUUID(), true);
            spawnCompanions = reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION;
        }
        return group;
    }
    private void initializeLeader() {
        flockId = getUUID();
        leaderId = getUUID();
        setLeader(true);
    }
    private void setLeader(boolean value) {
        leader = value;
        float fraction = getHealth() / getMaxHealth();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(value ? 30 : 20);
        getAttribute(Attributes.SCALE).setBaseValue(value ? .6875 : .55);
        setHealth(getMaxHealth() * fraction);
    }

    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        setNoGravity(true);
        if (isPassenger() || isLeashed()) { setDeltaMovement(getDeltaMovement().scale(.8)); return; }
        if (flockId == null) { initializeLeader(); spawnCompanions = true; }
        if (home == null) {
            home = position();
            Vec3 forward = Vec3.directionFromRotation(0, getYRot());
            orbitAngle = Math.atan2(forward.z, forward.x) - .6;
        }
        if (spawnCompanions) { spawnCompanions = false; createCompanions(level); }
        if (biteCooldown > 0) biteCooldown--;
        if (huntCooldown > 0) huntCooldown--;
        activityTicks++;
        SeaDrifterEntity guide = leader ? this : resolveLeader(level);
        if (leader && --flockRefresh <= 0) { reconcileFlock(level); flockRefresh = 60; }
        if (angerTicks > 0) {
            angerTicks--;
            if (getTarget() == null && angerId != null && level.getEntity(angerId) instanceof LivingEntity target) setTarget(target);
            if (validEnemy(getTarget()) && distanceToSqr(getTarget()) < 56*56) {
                changeActivity(Activity.DEFEND);
                attackTarget(level, getTarget(), slot);
                return;
            }
        }
        if (getTarget() != null || angerId != null) {
            setTarget(null); angerId = null; angerTicks = 0;
            changeActivity(Activity.RETURN); huntCooldown = Math.max(huntCooldown, 1200);
        }
        if (!leader && guide != null) { followGuide(level, guide); return; }
        if (!leader) { aim(position().add(0, isInWater() || onGround() ? 2 : .1, 0), .5, 0); return; }
        switch (activity) {
            case DIVE -> hunt(level);
            case RETURN -> ascend();
            case HOVER -> {
                aim(destination == null ? position() : destination, .45, activityTicks < 96 ? 3 : 0);
                if (activityTicks > 100) changeActivity(Activity.CRUISE);
            }
            default -> patrol(level);
        }
    }

    private void createCompanions(ServerLevel level) {
        int count = 2 + random.nextInt(2);
        for (int i=1; i<=count; i++) {
            SeaDrifterEntity member = ModEntities.SEA_DRIFTER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (member == null) continue;
            member.flockId = flockId; member.leaderId = getUUID(); member.slot = i;
            member.home = home;
            Vec3 spot = position().add(formationOffset(i, getYRot())).add(0, .8, 0);
            if (!clearAt(spot)) spot = position().add(0, 1.5+i*.6, 0);
            if (!clearAt(spot)) continue;
            member.snapTo(spot.x, spot.y, spot.z, getYRot(), getXRot());
            member.setYHeadRot(getYRot()); member.yBodyRot = member.yBodyRotO = getYRot();
            member.setDeltaMovement(getDeltaMovement());
            if (isPersistenceRequired()) member.setPersistenceRequired();
            level.addFreshEntity(member);
        }
        flockRefresh = 0;
    }
    private List<SeaDrifterEntity> nearbyFlock(ServerLevel level) {
        return level.getEntitiesOfClass(SeaDrifterEntity.class, getBoundingBox().inflate(64),
                other -> other.isAlive() && flockId.equals(other.flockId));
    }
    private SeaDrifterEntity resolveLeader(ServerLevel level) {
        if (leaderId != null && level.getEntity(leaderId) instanceof SeaDrifterEntity guide
                && guide.isAlive() && flockId.equals(guide.flockId)) { orphanTicks = 0; return guide; }
        if (++orphanTicks >= 200 && orphanTicks % 40 == 0) {
            // Wait for chunk reloads before election. Never load a missing leader's chunk.
            List<SeaDrifterEntity> present = nearbyFlock(level);
            present.stream().min(Comparator.comparing(Entity::getUUID)).ifPresent(next -> next.reconcileFlock(level));
        }
        return null;
    }
    private void reconcileFlock(ServerLevel level) {
        List<SeaDrifterEntity> present = new ArrayList<>(nearbyFlock(level));
        present.sort(Comparator.comparing(Entity::getUUID));
        SeaDrifterEntity elected = present.stream().filter(e -> e.leader).findFirst().orElse(this);
        boolean[] occupied = new boolean[4];
        occupied[0] = true;
        for (SeaDrifterEntity member : present) {
            member.leaderId = elected.getUUID();
            if (member.leader != (member == elected)) member.setLeader(member == elected);
            if (member == elected) member.slot = 0;
            else if (member.slot > 0 && member.slot < 4 && !occupied[member.slot]) occupied[member.slot] = true;
            else member.slot = 0;
            if (member != elected) member.home = elected.home;
        }
        // Keep established left/right positions. UUID sorting must not swap a new
        // follower to the opposite side immediately after it has been spawned.
        for (SeaDrifterEntity member : present) if (member != elected && member.slot == 0) {
            int index = 1;
            while (index < 3 && occupied[index]) index++;
            member.slot = index; occupied[index] = true;
        }
        elected.flock = present;
    }

    private void patrol(ServerLevel level) {
        if (isInWater()) { changeActivity(Activity.RETURN); ascend(); return; }
        if (huntCooldown == 0) {
            huntCooldown = 200 + random.nextInt(201); // Empty water is retried only every 10–20 seconds.
            AABB search = getBoundingBox().inflate(32, 36, 32);
            prey = level.getEntitiesOfClass(LivingEntity.class, search,
                    fish -> fish.isAlive() && fish.getType().builtInRegistryHolder().is(PREY) && fish.isInWater()
                            && waterSurface(fish.getX(), fish.getZ()) != null)
                    .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (prey != null) { changeActivity(Activity.DIVE); hunt(level); return; }
        }
        if (--hoverCooldown <= 0) {
            hoverCooldown = 600 + random.nextInt(601);
            destination = position(); changeActivity(Activity.HOVER); return;
        }
        if (--routeTicks <= 0 || destination == null || position().distanceToSqr(destination) < 9
                || horizontalCollision || verticalCollision) {
            routeTicks = 120;
            choosePatrol();
        }
        aim(destination, .85, activity == Activity.HOVER ? 0 : (tickCount / 100) % 3 == 1 ? 2 : 1);
    }
    private void choosePatrol() {
        orbitAngle += .45 + random.nextDouble()*.35;
        Vec3 landRoute = null;
        for (int i=0; i<6; i++) {
            double angle = orbitAngle + i*.7;
            double radius = 12 + random.nextDouble()*10;
            double x = home.x + Math.cos(angle)*radius, z = home.z + Math.sin(angle)*radius;
            Vec3 surface = waterSurface(x, z);
            if (surface == null) {
                BlockPos column = BlockPos.containing(x, getY(), z);
                if (landRoute == null && level().hasChunkAt(column)) {
                    int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
                    Vec3 point = new Vec3(x, Math.max(home.y, ground + 8 + 2*Math.sin(angle*.6)), z);
                    if (clearRoute(point)) landRoute = point;
                }
                continue;
            }
            // A low sea beyond a cliff/raised platform must not pull the flock
            // straight down into its current ground. Descend in shallow legs.
            Vec3 point = new Vec3(x, Math.max(surface.y + 10 + 3*Math.sin(angle*.6), getY()-4), z);
            if (clearRoute(point)) { destination = point; return; }
        }
        // Land-spawned flocks need real horizontal patrol legs too, not a single
        // stationary point over home while the animation keeps saying "fly".
        destination = landRoute;
        if (destination == null) {
            destination = position();
            changeActivity(Activity.HOVER);
        }
    }
    private Vec3 waterSurface(double x, double z) {
        BlockPos column = BlockPos.containing(x, getY(), z);
        if (!level().hasChunkAt(column)) return null;
        int y = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos below = new BlockPos(column.getX(), y-1, column.getZ());
        if (!level().getFluidState(below).is(FluidTags.WATER) || !level().getBlockState(below.above()).isAir()) return null;
        return new Vec3(x, y, z);
    }
    private void hunt(ServerLevel level) {
        if (prey == null || !prey.isAlive() || !prey.isInWater() || distanceToSqr(prey) > 56*56 || activityTicks > 500) {
            finishHunt(); ascend(); return;
        }
        Vec3 surface = waterSurface(prey.getX(), prey.getZ());
        if (surface == null) { finishHunt(); return; }
        Vec3 point = prey.position().add(0, prey.getBbHeight()*.5, 0);
        double dx = getX()-prey.getX(), dz = getZ()-prey.getZ();
        if (!isInWater() && dx*dx+dz*dz > 12) point = surface.add(0, 2, 0);
        aim(point, isInWater() ? 1.05 : 1.25, 1);
        bite(level, prey);
        if (!prey.isAlive()) finishHunt();
    }
    private void finishHunt() {
        prey = null;
        huntCooldown = 1800 + random.nextInt(2401);
        changeActivity(Activity.RETURN);
        // One fish is a flock meal; followers never independently hunt the remaining shoal.
        for (SeaDrifterEntity member : flock) {
            member.huntCooldown = huntCooldown;
            member.prey = null;
        }
    }
    private void ascend() {
        Vec3 surface = waterSurface(getX(), getZ());
        if (surface == null && home != null) {
            surface = waterSurface(home.x, home.z);
            if (isInWater() && surface != null) {
                // Return horizontally to open water before climbing past ice or an overhang.
                ascentTarget = null;
                aim(new Vec3(surface.x, Math.min(getY(), surface.y-1), surface.z), 1.2, 1);
                return;
            }
        }
        if (surface == null) {
            int y = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getBlockX(), getBlockZ());
            surface = new Vec3(getX(), y, getZ());
        }
        if (!isInWater() && getY() >= surface.y+9) {
            home = surface;
            Vec3 forward = flightHeading();
            // The first patrol leg continues the climb's heading rather than reversing into a circle.
            orbitAngle = Math.atan2(forward.z, forward.x) - .6;
            routeTicks = 0; changeActivity(Activity.CRUISE);
            patrol((ServerLevel)level());
            return;
        }
        if (ascentRetry > 0) ascentRetry--;
        if (ascentRetry == 0 && (ascentTarget == null || horizontalCollision || verticalCollision
                || position().subtract(ascentTarget).horizontalDistanceSqr() < 4)) {
            ascentTarget = chooseAscentTarget(surface);
            ascentRetry = 20;
        }
        // A fixed point ahead preserves forward travel through the waterline. Do not
        // continually target the current X/Z: that creates an elevator-like takeoff.
        Vec3 point = ascentTarget == null ? position().add(flightHeading().scale(6)).add(0, 3, 0) : ascentTarget;
        aim(point, 1.2, 1);
    }
    private Vec3 flightHeading() {
        Vec3 velocity = getDeltaMovement().multiply(1, 0, 1);
        return velocity.lengthSqr() > .0025 ? velocity.normalize() : Vec3.directionFromRotation(0, getYRot());
    }
    private Vec3 chooseAscentTarget(Vec3 surface) {
        Vec3 forward = flightHeading();
        double distance = Mth.clamp((surface.y+11-getY())/.6, 18, 36);
        for (int attempt=0; attempt<16; attempt++) {
            int turn = attempt%8;
            double angle = turn == 7 ? Math.PI : ((turn+1)/2)*.6*(turn%2 == 1 ? 1 : -1);
            Vec3 direction = forward.yRot((float)angle);
            Vec3 ahead = position().add(direction.scale(attempt < 8 ? distance : distance*.5));
            Vec3 water = waterSurface(ahead.x, ahead.z);
            // Over land (e.g. a spawn egg test), the same inclined flight is still valid.
            if (water == null && isInWater()) continue;
            Vec3 point = new Vec3(ahead.x, Math.max(surface.y, water == null ? surface.y : water.y)+11, ahead.z);
            if (clearRoute(point)) return point;
        }
        return null;
    }
    private void followGuide(ServerLevel level, SeaDrifterEntity guide) {
        changeActivity(guide.activity);
        double side = slot == 1 ? -2.2 : slot == 2 ? 2.2 : .4;
        double back = slot == 3 ? 4.4 : 2.3;
        Vec3 point = guide.position().add(formationOffset(slot, guide.getYRot()))
                .add(0, .3 + .2*Math.sin((tickCount+slot*23)*.04), 0);
        // Follow the moving formation rather than repeatedly braking at the
        // leader's old position. Corrections remain local to the follower.
        if (guide.activity != Activity.HOVER) point = point.add(guide.getDeltaMovement().scale(6));
        if (guide.activity == Activity.DIVE && guide.prey != null && guide.prey.isAlive()) {
            if (isInWater() && guide.isInWater()) {
                point = guide.prey.position().add(side*.18, .1, back*.12);
                bite(level, guide.prey);
                if (!guide.prey.isAlive()) guide.finishHunt();
            } else if (guide.isInWater()) {
                // Follow through the same open entry column instead of cutting through a bank.
                point = guide.position().add(side*.2, .15, back*.2);
            }
        }
        double distance = position().distanceTo(point);
        aim(point, Math.min(1.6, .6+distance*.14), guide.entityData.get(MOTION));
        if (guide.activity != Activity.DIVE && distance < 2.5)
            ((SeaFlightControl)moveControl).alignWithGuide(guide.getYRot());
    }
    private static Vec3 formationOffset(int slot, float yaw) {
        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        double side = slot == 1 ? -2.2 : slot == 2 ? 2.2 : .4;
        double back = slot == 3 ? 4.4 : 2.3;
        return right.scale(side).subtract(forward.scale(back));
    }
    private boolean validEnemy(LivingEntity target) {
        return target != null && target.isAlive() && !target.isSpectator() && !isAlliedTo(target) && canAttack(target)
                && !(target instanceof Player player && player.getAbilities().instabuild)
                && !(target instanceof SeaDrifterEntity other && flockId != null && flockId.equals(other.flockId));
    }
    private void attackTarget(ServerLevel level, LivingEntity target, int index) {
        Vec3 center = target.position().add(0, Math.min(target.getBbHeight()*.6, 1.2), 0);
        // Stagger attacks so a defending flock circles between modest bites.
        if (biteCooldown > 8) {
            double a = (tickCount+index*35)*.055;
            aim(center.add(Math.cos(a)*2.3, .8, Math.sin(a)*2.3), 1.25, 1);
        } else {
            aim(center, 1.45, 1);
            bite(level, target);
        }
    }
    private void bite(ServerLevel level, LivingEntity target) {
        if (biteCooldown > 0 || !target.isAlive() || !getBoundingBox().inflate(.65).intersects(target.getBoundingBox())
                || !hasLineOfSight(target)) return;
        if (doHurtTarget(level, target)) {
            biteCooldown = 28 + random.nextInt(9);
            playSound(SoundEvents.DOLPHIN_ATTACK, .25F, 1.4F);
        } else biteCooldown = 8;
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && amount > 0 && source.getEntity() instanceof LivingEntity attacker && validEnemy(attacker)) {
            List<SeaDrifterEntity> allies = flockId == null ? List.of(this) : nearbyFlock(level);
            // Broadcast even when this hit kills the original victim.
            for (SeaDrifterEntity member : allies) {
                member.angerId = attacker.getUUID(); member.angerTicks = 600;
                member.setTarget(attacker); member.prey = null; member.changeActivity(Activity.DEFEND);
            }
        }
        return hurt;
    }
    private void changeActivity(Activity next) {
        if (activity != next) { activity = next; activityTicks = 0; ascentTarget = null; ascentRetry = 0; }
    }
    private void aim(Vec3 point, double speed, int motion) {
        ((SeaFlightControl)moveControl).aligningWithGuide = false;
        double y = Mth.clamp(point.y, level().getMinY()+2, level().getMaxY()-4);
        moveControl.setWantedPosition(point.x, y, point.z, speed);
        entityData.set(MOTION, motion);
    }
    private boolean clearAt(Vec3 point) {
        AABB box = getBoundingBox().move(point.subtract(position()));
        return level().hasChunksAt(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))
                && level().noBlockCollision(this, box);
    }
    private boolean clearRoute(Vec3 point) {
        for (int step=1; step<=4; step++) {
            if (!clearAt(position().lerp(point, step*.25))) return false;
        }
        return true;
    }
    private final class SeaFlightControl extends MoveControl {
        private Vec3 avoidance = Vec3.ZERO;
        private int avoidanceTicks;
        private boolean aligningWithGuide;
        private float guideYaw;
        SeaFlightControl() { super(SeaDrifterEntity.this); }
        void alignWithGuide(float yaw) { aligningWithGuide = true; guideYaw = yaw; }
        @Override public void tick() {
            if (operation != Operation.MOVE_TO) { setDeltaMovement(getDeltaMovement().scale(.8)); return; }
            operation = Operation.WAIT;
            Vec3 delta = new Vec3(wantedX-getX(), wantedY-getY(), wantedZ-getZ());
            double distance = delta.length();
            double speed = Math.min(getAttributeValue(Attributes.FLYING_SPEED)*speedModifier, distance*.3);
            Vec3 velocity = distance < .02 ? Vec3.ZERO : delta.scale(speed/distance);
            if (avoidanceTicks > 0) { velocity = avoidance; avoidanceTicks--; }
            if ((tickCount+getId())%5 == 0 || horizontalCollision || verticalCollision) {
                Vec3 lookAhead = position().add(velocity.scale(6));
                if (!clearAt(lookAhead)) {
                    // Bounded local avoidance in both water and air, with no forced chunk loads.
                    Vec3[] alternatives = {new Vec3(0, .2, 0), new Vec3(-velocity.z, .1, velocity.x),
                            new Vec3(velocity.z, .1, -velocity.x), new Vec3(-velocity.x, .15, -velocity.z)};
                    velocity = Vec3.ZERO;
                    for (Vec3 option : alternatives) if (clearAt(position().add(option.scale(6)))) { velocity = option; break; }
                    avoidance = velocity; avoidanceTicks = 8;
                }
            }
            if (onGround() && velocity.y < .15) velocity = new Vec3(velocity.x, .18, velocity.z);
            setDeltaMovement(getDeltaMovement().lerp(velocity, .18));
            if (aligningWithGuide || getDeltaMovement().horizontalDistanceSqr() > .001) {
                float yaw = aligningWithGuide ? guideYaw
                        : (float)(Mth.atan2(getDeltaMovement().z, getDeltaMovement().x)*180/Math.PI)-90;
                setYRot(rotlerp(getYRot(), yaw, 6)); setYHeadRot(getYRot()); yBodyRot = getYRot();
            }
            float pitch = (float)(-Mth.atan2(getDeltaMovement().y, Math.max(.04, getDeltaMovement().horizontalDistance()))*180/Math.PI);
            setXRot(rotlerp(getXRot(), Mth.clamp(pitch, -48, 48), 4));
        }
    }
    @Override public void travel(Vec3 input) {
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(.91));
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.DOLPHIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.DOLPHIN_DEATH; }
    @Override protected float getSoundVolume() { return .35F; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SeaDrifterEntity>("attitude", 0, state -> state.setAndContinue(ATTITUDE)));
        controllers.add(new AnimationController<SeaDrifterEntity>("flight", 5, state -> {
            if (hurtTime > 0) return state.setAndContinue(HURT);
            return state.setAndContinue(switch(entityData.get(MOTION)) { case 1 -> FLY; case 2 -> GLIDE; case 3 -> CURIOUS; default -> HOVER; });
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (flockId != null) output.store("Flock", UUIDUtil.CODEC, flockId);
        if (leaderId != null) output.store("Leader", UUIDUtil.CODEC, leaderId);
        if (angerId != null) output.store("AngerTarget", UUIDUtil.CODEC, angerId);
        output.putBoolean("FlockLeader", leader); output.putInt("FlockSlot", slot);
        output.putBoolean("SpawnCompanions", spawnCompanions);
        output.putInt("HuntCooldown", huntCooldown); output.putInt("AngerTicks", angerTicks);
        if (home != null) output.store("SeaHome", Vec3.CODEC, home);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        flockId = input.read("Flock", UUIDUtil.CODEC).orElse(null);
        leaderId = input.read("Leader", UUIDUtil.CODEC).orElse(null);
        angerId = input.read("AngerTarget", UUIDUtil.CODEC).orElse(null);
        setLeader(input.getBooleanOr("FlockLeader", false));
        slot = Math.clamp(input.getIntOr("FlockSlot", 1), 0, 3);
        spawnCompanions = input.getBooleanOr("SpawnCompanions", false);
        huntCooldown = Math.clamp(input.getIntOr("HuntCooldown", 1200), 0, 4200);
        angerTicks = Math.clamp(input.getIntOr("AngerTicks", 0), 0, 600);
        home = input.read("SeaHome", Vec3.CODEC).orElse(null);
        // Food entity references/routes are ephemeral; reload safely returns a submerged flock to air.
        activity = Activity.RETURN; destination = null; ascentTarget = null; ascentRetry = 0; prey = null;
        setNoGravity(true);
    }
}
