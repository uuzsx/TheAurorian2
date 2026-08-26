package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A short-lived, non-colliding rift that belongs to one purification ritual. */
public final class PurificationRiftEntity extends Entity {

    public static final int LIFETIME_TICKS = 16 * 20;
    public static final int CLOSE_ANIMATION_TICKS = 4 * 2;
    private static final int FIRST_SPAWN_TICK = 2 * 20;
    private static final int SECOND_SPAWN_TICK = 8 * 20;
    private static final int THIRD_SPAWN_TICK = 13 * 20;
    private static final EntityDataAccessor<Boolean> CLOSING =
            SynchedEntityData.defineId(PurificationRiftEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CLOSING_TICKS =
            SynchedEntityData.defineId(PurificationRiftEntity.class, EntityDataSerializers.INT);

    private BlockPos altarPos = BlockPos.ZERO;
    private long ritualId;
    private int spawnCount = 2;
    private boolean firstSpawned;
    private boolean secondSpawned;
    private boolean thirdSpawned;
    private int closingTicks = -1;

    public PurificationRiftEntity(EntityType<? extends PurificationRiftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static PurificationRiftEntity create(
            ServerLevel level, BlockPos altarPos, long ritualId, double x, double y, double z,
            int spawnCount) {
        PurificationRiftEntity rift = cn.teampancake.theaurorian2.common.registry.ModEntities.PURIFICATION_RIFT
                .get().create(level, EntitySpawnReason.TRIGGERED);
        if (rift == null) {
            return null;
        }
        rift.altarPos = altarPos.immutable();
        rift.ritualId = ritualId;
        rift.spawnCount = Math.clamp(spawnCount, 2, 3);
        rift.setPos(x, y, z);
        rift.setNoGravity(true);
        return rift;
    }

    public boolean belongsTo(BlockPos pos, long id) {
        return this.ritualId == id && this.altarPos.equals(pos);
    }

    public BlockPos getAltarPos() {
        return this.altarPos;
    }

    /** Rift visuals remain visible well beyond the tiny non-colliding entity hitbox. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0D * 64.0D;
    }

    public boolean isClosing() {
        return this.entityData.get(CLOSING);
    }

    public float getClosingTicks(float partialTick) {
        return this.entityData.get(CLOSING_TICKS) + partialTick;
    }

    public void beginClosing() {
        if (this.isClosing()) {
            return;
        }
        this.entityData.set(CLOSING, true);
        this.closingTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isClosing()) {
            if (this.closingTicks < 0) {
                this.closingTicks = 0;
            } else {
                this.closingTicks++;
            }
            this.entityData.set(CLOSING_TICKS, Math.max(0, this.closingTicks));
            if (this.level() instanceof ServerLevel && this.closingTicks >= CLOSE_ANIMATION_TICKS) {
                this.discard();
            }
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (this.tickCount >= LIFETIME_TICKS) {
            this.beginClosing();
            return;
        }

        if (!(level.getBlockEntity(this.altarPos) instanceof PurificationAltarBlockEntity altar)
                || !altar.isRitualActive()
                || !altar.belongsToRitual(this.ritualId)) {
            this.beginClosing();
            return;
        }

        if (!this.firstSpawned && this.tickCount >= FIRST_SPAWN_TICK) {
            this.firstSpawned = true;
            altar.spawnRitualZombies(this, 1);
        }
        if (!this.secondSpawned && this.tickCount >= SECOND_SPAWN_TICK) {
            this.secondSpawned = true;
            altar.spawnRitualZombies(this, 1);
        }
        if (this.spawnCount >= 3 && !this.thirdSpawned && this.tickCount >= THIRD_SPAWN_TICK) {
            this.thirdSpawned = true;
            altar.spawnRitualZombies(this, 1);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CLOSING, false);
        builder.define(CLOSING_TICKS, 0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Ritual rifts are intentionally transient and are never saved.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Ritual rifts are intentionally transient and are never saved.
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }
}
