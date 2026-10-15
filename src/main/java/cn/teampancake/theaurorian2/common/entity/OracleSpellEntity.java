package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.OracleDisplacedTerrainBlock;
import cn.teampancake.theaurorian2.common.block.entity.OracleDisplacedTerrainBlockEntity;
import cn.teampancake.theaurorian2.common.network.OracleGravityMotionPayload;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

/** One synchronized start time drives the entire presentation; gameplay remains on the server. */
public final class OracleSpellEntity extends Entity {
    public static final int LIGHTNING = 1, GRAVITY = 2, LIFTED_BLOCK = 3;
    public static final int LIGHTNING_IMPACT = 20;
    public static final int LIFT_START = 14, LIFT_END = 32, SLAM_START = 42, IMPACT = 60;
    public static final double FIELD_HALF_WIDTH = 1.5, LIFT_HEIGHT = 15;
    public static final double EMPOWERED_LIFT_HEIGHT = 20;
    public static final int MAX_LIFTED_BLOCKS = 14;
    public static final int EMPOWERED_MAX_LIFTED_BLOCKS = 30;
    private static final ResourceKey<DamageType> LIGHTNING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_lightning"));
    private static final ResourceKey<DamageType> SLAM_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,TheAurorian2.id("oracle_gravity_slam"));
    private static final Identifier WEIGHTLESS = TheAurorian2.id("oracle_weightless");
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<BlockState> BLOCK = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> EMPOWERED = SynchedEntityData.defineId(OracleSpellEntity.class, EntityDataSerializers.BOOLEAN);
    private RisingClockOracleEntity owner;
    private static final class LiftedTarget {
        final LivingEntity entity;
        final double baseY;
        double peakY;
        LiftedTarget(LivingEntity entity) { this.entity=entity; baseY=entity.getY(); peakY=baseY; }
    }
    private final List<LiftedTarget> captured = new ArrayList<>();
    private boolean hit;
    private boolean thunderSound = true;
    private final List<BlockPos> reservations = new ArrayList<>();
    private final Map<BlockPos,BlockState> lockedGround = new LinkedHashMap<>();
    private final List<OracleSpellEntity> pieces = new ArrayList<>();

    public OracleSpellEntity(EntityType<? extends OracleSpellEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(KIND, LIGHTNING); data.define(DELAY, 0); data.define(SEED, 0); data.define(START, 0L);
        data.define(BLOCK, Blocks.AIR.defaultBlockState());
        data.define(EMPOWERED,false);
    }
    public int kind() { return entityData.get(KIND); }
    public int seed() { return entityData.get(SEED); }
    public int delay() { return entityData.get(DELAY); }
    public float age(float partial) { return Math.max(0, level().getGameTime() - entityData.get(START)) + partial; }
    public BlockState blockState() { return entityData.get(BLOCK); }
    public boolean empowered() { return entityData.get(EMPOWERED); }
    public double liftHeight() { return empowered() ? EMPOWERED_LIFT_HEIGHT : LIFT_HEIGHT; }
    void setThunderSound(boolean enabled) { thunderSound = enabled; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 128 * 128; }
    public static double liftOffset(float age) {
        return liftOffset(age,false);
    }
    public static double liftOffset(float age, boolean enhanced) {
        double height = enhanced ? EMPOWERED_LIFT_HEIGHT : LIFT_HEIGHT;
        if (age < LIFT_START || age >= IMPACT) return 0;
        if (age < LIFT_END) { double u = (age - LIFT_START) / (LIFT_END - LIFT_START); return height * u * u * (3 - 2 * u); }
        if (age < SLAM_START) return height;
        double u = (age - SLAM_START) / (IMPACT - SLAM_START);
        return height * (1 - u * u);
    }
    public static OracleSpellEntity create(ServerLevel level, RisingClockOracleEntity owner, int kind, BlockPos floor, int delay) {
        return create(level,owner,kind,floor,delay,false);
    }
    public static OracleSpellEntity create(ServerLevel level, RisingClockOracleEntity owner, int kind, BlockPos floor, int delay, boolean enhanced) {
        OracleSpellEntity spell = new OracleSpellEntity(ModEntities.ORACLE_SPELL.get(), level);
        spell.owner = owner;
        spell.entityData.set(KIND, kind); spell.entityData.set(DELAY, delay);
        spell.entityData.set(EMPOWERED,enhanced);
        spell.entityData.set(START, level.getGameTime()); spell.entityData.set(SEED, level.getRandom().nextInt());
        spell.setPos(floor.getX() + .5, floor.getY() + 1.025, floor.getZ() + .5);
        if (!level.addFreshEntity(spell)) return null;
        if (kind == GRAVITY) spell.lockTerrain(level,floor);
        return spell;
    }
    /** Bounded, loaded-chunk-only search: a cast never generates terrain. */
    public static BlockPos findSurface(ServerLevel level, BlockPos start, int depth) {
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 0; i <= depth && pos.getY() >= level.getMinY(); i++, pos.move(0, -1, 0)) {
            if (!level.hasChunkAt(pos)) return null;
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) return pos.immutable();
        }
        return null;
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (owner == null || !owner.isAlive() || owner.isRemoved() || owner.isNoAi()) { cancel(); return; }
        int age = (int) age(0);
        if (kind() == LIGHTNING) {
            if (age >= LIGHTNING_IMPACT + delay() && !hit) {
                hit = true;
                float damage = switch (server.getDifficulty()) { case PEACEFUL -> 0; case EASY -> empowered() ? 15 : 12; case NORMAL -> empowered() ? 19 : 16; case HARD -> empowered() ? 23 : 20; };
                DamageSource source = spellDamage(server,LIGHTNING_DAMAGE);
                // A vanilla-looking vertical bolt can also strike airborne targets. Never hurt its caster.
                for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(getX()-1.25,getY()-.3,getZ()-1.25,getX()+1.25,getY()+129,getZ()+1.25), owner::validTarget)) {
                    if (victim != owner && damage > 0) victim.hurtServer(server, source, damage);
                }
                sound(server, SoundEvents.LIGHTNING_BOLT_IMPACT, .65F, .85F);
                if (thunderSound && delay() == 0) sound(server, SoundEvents.LIGHTNING_BOLT_THUNDER, .7F, 1.25F);
            }
            if (age >= LIGHTNING_IMPACT + 19 + delay()) discard();
        } else if (kind() == GRAVITY) {
            if (age == LIFT_START) {
                capture(server);
                for (BlockPos pos : reservations) {
                    BlockState state = server.getBlockState(pos);
                    if (state.is(ModBlocks.ORACLE_DISPLACED_TERRAIN.get())) server.setBlock(pos,state.setValue(OracleDisplacedTerrainBlock.LIFTED,true),Block.UPDATE_ALL);
                }
                sound(server, SoundEvents.BEACON_ACTIVATE, .9F, .6F);
            }
            if (age >= IMPACT && !hit) {
                hit = true; restoreTerrain(server);
                sound(server, SoundEvents.GENERIC_EXPLODE.value(), .8F, .65F);
                sound(server, SoundEvents.AMETHYST_BLOCK_BREAK, 1.3F, .6F);
            }
            moveCaptured(server, age);
            if (age >= IMPACT+20) {
                // A lagging player or a target over a hole can finish falling normally after the cast.
                for (LiftedTarget lifted : captured) finishSlam(server,lifted,false);
                captured.clear(); discard();
            }
        } else if (age >= IMPACT) discard();
    }
    private void capture(ServerLevel server) {
        // Only feet still on marked ground at the end of the warning are caught.
        double halfWidth = empowered() ? 2.5 : FIELD_HALF_WIDTH;
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(),position()).inflate(halfWidth,5,halfWidth), owner::validTarget)) {
            captureVictim(server, victim);
        }
    }
    private void captureVictim(ServerLevel server, LivingEntity victim) {
        BlockPos support = victim.getOnPos();
        if (!owner.validTarget(victim) || victim.isPassenger() || !victim.onGround() || !lockedGround.containsKey(support)
                || victim.distanceToSqr(owner) > RisingClockOracleEntity.AGGRO_RANGE * RisingClockOracleEntity.AGGRO_RANGE) return;
        BlockState supportState = server.getBlockState(support);
        if (!supportState.is(ModBlocks.ORACLE_DISPLACED_TERRAIN.get()) && !supportState.equals(lockedGround.get(support))) return;
        var gravity = victim.getAttribute(Attributes.GRAVITY);
        if (gravity == null || gravity.hasModifier(WEIGHTLESS)) return;
        captured.add(new LiftedTarget(victim));
        owner.controlApplied(IMPACT-LIFT_START+(empowered()?20:0));
        gravity.addTransientModifier(new AttributeModifier(WEIGHTLESS, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        victim.fallDistance = 0;
        if (victim instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player,
                new OracleGravityMotionPayload(getId(),entityData.get(START),victim.getY(),true,empowered()));
    }
    private void moveCaptured(ServerLevel server, int age) {
        for (var iterator = captured.iterator(); iterator.hasNext();) {
            LiftedTarget lifted = iterator.next();
            LivingEntity victim = lifted.entity;
            if (victim.isRemoved() || victim.level() != server || !owner.validTarget(victim)
                    || victim.distanceToSqr(this) > 40*40 || victim.isPassenger()) {
                releaseVictim(victim,true); iterator.remove(); continue;
            }
            lifted.peakY = Math.max(lifted.peakY,victim.getY());
            // Resolve the vanilla fall first, then the spell's separate base damage. Its data-driven
            // cooldown bypass prevents either hit swallowing the other without changing entity i-frames.
            if (age >= SLAM_START && victim.onGround() && (lifted.peakY > lifted.baseY+.5 || age >= IMPACT)) {
                finishSlam(server,lifted,true); iterator.remove();
                continue;
            }
            victim.fallDistance = 0;
            double nextY = lifted.baseY + (age >= IMPACT ? -.5 : liftOffset(age + 1,empowered()));
            Vec3 velocity = victim.getDeltaMovement();
            victim.setDeltaMovement(velocity.x * .65, Mth.clamp(nextY - victim.getY(), empowered() ? -3.5 : -2.5, empowered() ? 2 : 1.5), velocity.z * .65);
            // Players predict the same trajectory locally from a single capture message.
            // Repeated velocity packets fight their delayed movement reports and cause oscillation.
            if (!(victim instanceof ServerPlayer) && age % 4 == 0) victim.needsSync = true;
        }
    }
    private void lockTerrain(ServerLevel server, BlockPos center) {
        boolean moveTerrain = EventHooks.canEntityGrief(server, owner);
        int radius = empowered() ? 2 : 1;
        List<BlockPos> tops = new ArrayList<>((radius*2+1)*(radius*2+1));
        // One footprint at the locked target: 3 x 3 normally, 5 x 5 when empowered.
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            BlockPos pos = findSurface(server, center.offset(dx, 1, dz), 4);
            if (pos != null) { lockBlock(server,pos,moveTerrain); if (lockedGround.containsKey(pos)) tops.add(pos); }
        }
        // Carry a small, irregular underside with the surface, using real blocks directly below it.
        // Each extra block stays attached to a selected top block; never widen the footprint.
        int remaining = 3 + random.nextInt(3);
        while (remaining > 0 && !tops.isEmpty()) {
            BlockPos below = tops.remove(random.nextInt(tops.size())).below();
            int before = lockedGround.size();
            lockBlock(server,below,moveTerrain);
            if (lockedGround.size() > before) remaining--;
        }
    }
    private void lockBlock(ServerLevel server, BlockPos pos, boolean moveTerrain) {
        if (lockedGround.size() >= (empowered() ? EMPOWERED_MAX_LIFTED_BLOCKS : MAX_LIFTED_BLOCKS)
                || lockedGround.containsKey(pos) || !server.hasChunkAt(pos)) return;
        BlockState state = server.getBlockState(pos);
        // No inventories, fluids or protected blocks. Covered blocks may move only beneath our own selection.
        if (state.hasBlockEntity() || state.getDestroySpeed(server, pos) < 0 || !state.getFluidState().isEmpty()
                || !state.isCollisionShapeFullBlock(server, pos)
                || state.getPistonPushReaction() == PushReaction.BLOCK
                || !server.isEmptyBlock(pos.above()) && !lockedGround.containsKey(pos.above())) return;
        lockedGround.put(pos,state);
        if (!moveTerrain) return;
        if (!server.setBlock(pos, ModBlocks.ORACLE_DISPLACED_TERRAIN.get().defaultBlockState().setValue(OracleDisplacedTerrainBlock.LIFTED,false), Block.UPDATE_ALL)) { lockedGround.remove(pos); return; }
        if (!(server.getBlockEntity(pos) instanceof OracleDisplacedTerrainBlockEntity reservation)) {
            server.setBlock(pos, state, Block.UPDATE_ALL); lockedGround.remove(pos); return;
        }
        reservation.initialize(state); reservations.add(pos);
        OracleSpellEntity piece = create(server, owner, LIFTED_BLOCK, pos, 0,empowered());
        if (piece != null) {
            piece.entityData.set(START, entityData.get(START)); piece.entityData.set(BLOCK, state);
            piece.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5); pieces.add(piece);
        } else {
            reservation.restore(server); reservations.remove(pos); lockedGround.remove(pos);
        }
    }
    private DamageSource spellDamage(ServerLevel server, ResourceKey<DamageType> type) {
        return new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type),this,owner);
    }
    private void finishSlam(ServerLevel server, LiftedTarget lifted, boolean landed) {
        LivingEntity victim = lifted.entity;
        double distance = Math.max(0,lifted.peakY-victim.getY());
        boolean cushioned = victim.isInWater() || victim.isInLava() || victim.hasEffect(MobEffects.SLOW_FALLING);
        if (landed && !cushioned) {
            BlockPos pos = victim.getOnPos();
            BlockState state = server.getBlockState(pos);
            boolean aliveBeforeFall = victim.isAlive();
            state.getBlock().fallOn(server,state,pos,victim,distance);
            // A lethal fall prevents the following spell damage event from firing.
            // Credit that hit here; surviving targets are counted by the spell event once.
            if (aliveBeforeFall && !victim.isAlive()) owner.recordSpellHit(victim);
        }
        victim.hurtServer(server,spellDamage(server,SLAM_DAMAGE),empowered() ? 18 : 13);
        releaseVictim(victim,false);
        if (empowered() && landed && victim.isAlive())
            victim.addEffect(new MobEffectInstance(ModMobEffects.STUN,20,0,false,false,true),owner);
        if (landed) owner.controlApplied(empowered()?20:0);
        if (!landed && !cushioned) victim.fallDistance = distance;
    }
    private void release(boolean interrupted) {
        for (LiftedTarget lifted : captured) releaseVictim(lifted.entity,interrupted);
        captured.clear();
    }
    private void releaseVictim(LivingEntity victim, boolean interrupted) {
        var attribute = victim.getAttribute(Attributes.GRAVITY);
        if (attribute != null) attribute.removeModifier(WEIGHTLESS);
        victim.fallDistance = 0;
        if (interrupted && victim.isAlive()) victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 60, 0, false, false));
        if (victim instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player,new OracleGravityMotionPayload(getId(),0,0,false));
            player.connection.send(new ClientboundSetEntityMotionPacket(victim));
        }
    }
    private void restoreTerrain(ServerLevel server) {
        for (BlockPos pos : reservations) {
            if (server.hasChunkAt(pos) && server.getBlockEntity(pos) instanceof OracleDisplacedTerrainBlockEntity reservation) reservation.restore(server);
        }
        reservations.clear();
        lockedGround.clear();
        for (OracleSpellEntity piece : pieces) if (!piece.isRemoved()) piece.discard();
        pieces.clear();
    }
    public void cancel() { release(true); if (level() instanceof ServerLevel server) restoreTerrain(server); discard(); }
    @Override public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel server) { release(true); restoreTerrain(server); }
        super.remove(reason);
    }
    private void sound(ServerLevel level, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        level.playSound(null, getX(),getY(),getZ(),sound,SoundSource.HOSTILE,volume,pitch);
    }
    @Override public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) { return false; }
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
    @Override protected void readAdditionalSaveData(ValueInput input) {}
}
