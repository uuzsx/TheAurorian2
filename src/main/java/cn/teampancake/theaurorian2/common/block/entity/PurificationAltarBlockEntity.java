package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.block.PurificationAltarBlock;
import cn.teampancake.theaurorian2.common.block.PurificationAltarBaseBlock;
import cn.teampancake.theaurorian2.common.block.PurificationAltarUpperBlock;
import cn.teampancake.theaurorian2.common.entity.PurificationRiftEntity;
import cn.teampancake.theaurorian2.common.entity.PurificationRitualZombieEntity;
import cn.teampancake.theaurorian2.common.network.PurificationRitualMusicPayload;
import cn.teampancake.theaurorian2.common.network.PurificationRitualPromptPayload;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.world.MoonShieldSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/** Server-authoritative state machine for the permanent purification ritual. */
public final class PurificationAltarBlockEntity extends BlockEntity {

    public static final int CHANNEL_TICKS = 4 * 60 * 20 + 20 * 20;
    public static final long PROGRESS_EVENT_ID_PREFIX = 0x5055524946590000L;
    private static final int PROMPT_TICKS = 5 * 60 * 20;
    private static final int PROGRESS_STEPS = 176;
    private static final double CONFIRM_MAX_DISTANCE_SQUARED = 20.25D;
    private static final int INITIAL_SHIELDS = 4;
    private static final int ALTAR_HIT_COOLDOWN_TICKS = 15;
    private static final int MAX_RIFTS = 2;
    private static final int MAX_RITUAL_ZOMBIES = 6;
    private static final int RIFT_OPEN_DELAY_TICKS = 8 * 20;
    private static final int RIFT_SPAWN_STOP_TICKS = CHANNEL_TICKS - 12 * 20;
    private static final int RIFT_MIN_DISTANCE = 4;
    private static final double RIFT_MIN_RADIUS = 6.5D;
    private static final double RIFT_MAX_RADIUS = 10.5D;

    private @Nullable UUID pendingPlayer;
    private long promptExpiresAt;
    private @Nullable UUID ritualPlayer;
    private long ritualStartedAt;
    private long ritualId;
    private long nextRiftAt;
    private float lastHealth;
    private int altarHitCooldown;
    private boolean baseLightSynchronized;
    private boolean upperPartSynchronized;
    private @Nullable ServerBossEvent progressEvent;
    private int lastProgressStep = -1;

    // Client-side book animation state, matching the vanilla enchanting table.
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource BOOK_RANDOM = RandomSource.create();

    public PurificationAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PURIFICATION_ALTAR.get(), pos, state);
        this.shieldFade = state.getValue(PurificationAltarBlock.RITUAL_ACTIVE) ? 1.0F : 0.0F;
    }

    public void requestRitual(ServerPlayer player) {
        if (isRitualActive()) {
            player.sendSystemMessage(Component.translatable(
                    ritualPlayer != null && ritualPlayer.equals(player.getUUID())
                            ? "message.theaurorian2.purification.in_progress"
                            : "message.theaurorian2.purification.busy"));
            return;
        }

        pendingPlayer = player.getUUID();
        promptExpiresAt = level == null ? 0L : level.getGameTime() + PROMPT_TICKS;
        PacketDistributor.sendToPlayer(player, new PurificationRitualPromptPayload(worldPosition));
    }

    public void confirmRitual(ServerPlayer player) {
        if (level == null || level.isClientSide()
                || pendingPlayer == null
                || !pendingPlayer.equals(player.getUUID())
                || level.getGameTime() > promptExpiresAt
                || MoonShieldSystem.isPurified(player)
                || player.level() != level
                || player.distanceToSqr(worldPosition.getCenter()) > CONFIRM_MAX_DISTANCE_SQUARED
                || isRitualActive()) {
            return;
        }

        pendingPlayer = null;
        ritualPlayer = player.getUUID();
        ritualStartedAt = level.getGameTime();
        ritualId++;
        nextRiftAt = ritualStartedAt + RIFT_OPEN_DELAY_TICKS;
        lastHealth = player.getHealth();
        altarHitCooldown = 0;
        setShieldCount(INITIAL_SHIELDS);
        setRitualActive(true);
        PacketDistributor.sendToPlayersInDimension(
                (ServerLevel) level, new PurificationRitualMusicPayload(true));
        updateProgressEvent(player, 0L);
        level.playSound(
                null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 1.0F, 0.75F);
    }

    public boolean isRitualActive() {
        return getBlockState().getValue(PurificationAltarBlock.RITUAL_ACTIVE);
    }

    public int getShieldCount() {
        return getBlockState().getValue(PurificationAltarBlock.SHIELD_COUNT);
    }

    public float getShieldFade() {
        return this.shieldFade;
    }

    public long getRitualId() {
        return ritualId;
    }

    public boolean belongsToRitual(long id) {
        return ritualId == id;
    }

    /** Client-side fade amount for the remaining shield sprites. */
    public float shieldFade = 1.0F;

    public void tryDamageShield(PurificationRitualZombieEntity attacker) {
        if (!(level instanceof ServerLevel serverLevel)
                || !isRitualActive()
                || altarHitCooldown > 0
                || getShieldCount() <= 0
                || attacker.level() != level
                || attacker.distanceToSqr(worldPosition.getCenter()) > 3.0D) {
            return;
        }

        altarHitCooldown = ALTAR_HIT_COOLDOWN_TICKS;
        setShieldCount(getShieldCount() - 1);
        serverLevel.playSound(
                null, worldPosition, SoundEvents.GLASS_BREAK,
                SoundSource.BLOCKS, 0.75F, 1.15F);
        if (getShieldCount() <= 0) {
            cancelRitual("message.theaurorian2.purification.altar_destroyed", false);
        }
    }

    public void spawnRitualZombies(PurificationRiftEntity rift, int count) {
        if (!(level instanceof ServerLevel serverLevel) || !isRitualActive()) {
            return;
        }
        int existing = countRitualZombies(serverLevel);
        for (int index = 0; index < count && existing < MAX_RITUAL_ZOMBIES; index++) {
            PurificationRitualZombieEntity zombie = ModEntities.PURIFICATION_RITUAL_ZOMBIE
                    .get().create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (zombie == null) {
                continue;
            }
            double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 0.35D + serverLevel.getRandom().nextDouble() * 0.55D;
            zombie.configureRitual(worldPosition, ritualId);
            zombie.setPos(
                    rift.getX() + Math.cos(angle) * radius,
                    rift.getY(),
                    rift.getZ() + Math.sin(angle) * radius);
            zombie.setYRot(serverLevel.getRandom().nextFloat() * 360.0F);
            serverLevel.addFreshEntity(zombie);
            existing++;
        }
    }

    private int countRitualZombies(ServerLevel serverLevel) {
        AABB searchArea = new AABB(worldPosition).inflate(18.0D);
        return serverLevel.getEntitiesOfClass(
                PurificationRitualZombieEntity.class,
                searchArea,
                zombie -> zombie.belongsTo(worldPosition, ritualId) && zombie.isAlive()).size();
    }

    private int countRifts(ServerLevel serverLevel) {
        AABB searchArea = new AABB(worldPosition).inflate(18.0D);
        return serverLevel.getEntitiesOfClass(
                PurificationRiftEntity.class,
                searchArea,
                rift -> rift.belongsTo(worldPosition, ritualId) && rift.isAlive()).size();
    }

    private void spawnRift(ServerLevel serverLevel, long elapsed) {
        BlockPos spawnPos = findRiftPosition(serverLevel);
        if (spawnPos == null) {
            nextRiftAt = serverLevel.getGameTime() + 40L;
            return;
        }

        int spawnCount = elapsed >= 95L * 20L ? 3 : 2;
        PurificationRiftEntity rift = PurificationRiftEntity.create(
                serverLevel,
                worldPosition,
                ritualId,
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                spawnCount);
        if (rift != null) {
            serverLevel.addFreshEntity(rift);
        }
        nextRiftAt = serverLevel.getGameTime() + nextRiftInterval(elapsed);
    }

    private long nextRiftInterval(long elapsed) {
        int min;
        int max;
        if (elapsed < 45L * 20L) {
            min = 15 * 20;
            max = 18 * 20;
        } else if (elapsed < 95L * 20L) {
            min = 12 * 20;
            max = 15 * 20;
        } else {
            min = 9 * 20;
            max = 12 * 20;
        }
        return min + getLevel().getRandom().nextInt(max - min + 1);
    }

    private @Nullable BlockPos findRiftPosition(ServerLevel serverLevel) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = RIFT_MIN_RADIUS
                    + serverLevel.getRandom().nextDouble() * (RIFT_MAX_RADIUS - RIFT_MIN_RADIUS);
            int x = Mth.floor(worldPosition.getX() + 0.5D + Math.cos(angle) * radius);
            int z = Mth.floor(worldPosition.getZ() + 0.5D + Math.sin(angle) * radius);
            for (int yOffset = 4; yOffset >= -4; yOffset--) {
                BlockPos candidate = new BlockPos(x, worldPosition.getY() + yOffset, z);
                if (!serverLevel.getBlockState(candidate.below())
                            .isFaceSturdy(serverLevel, candidate.below(), Direction.UP)
                        || !serverLevel.getBlockState(candidate).isAir()
                        || !serverLevel.getBlockState(candidate.above()).isAir()
                        || !serverLevel.getBlockState(candidate.above(2)).isAir()
                        || !serverLevel.getBlockState(candidate.above(3)).isAir()) {
                    continue;
                }
                boolean tooClose = serverLevel.getEntitiesOfClass(
                        PurificationRiftEntity.class,
                        new AABB(candidate).inflate(RIFT_MIN_DISTANCE),
                        rift -> rift.belongsTo(worldPosition, ritualId) && rift.isAlive()).size() > 0;
                if (!tooClose) {
                    // Leave one full air block between the ground and the rift.
                    return candidate.above(1);
                }
            }
        }
        return null;
    }

    /** Matches the vanilla enchanting table's client-side book animation state machine. */
    public static void bookAnimationTick(
            Level level, BlockPos pos, BlockState state, PurificationAltarBlockEntity altar) {
        altar.oOpen = altar.open;
        altar.oRot = altar.rot;
        net.minecraft.world.entity.player.Player player = level.getNearestPlayer(
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 3.0D, false);
        if (player != null) {
            double x = player.getX() - (pos.getX() + 0.5D);
            double z = player.getZ() - (pos.getZ() + 0.5D);
            altar.tRot = (float) Mth.atan2(z, x);
            altar.open += 0.1F;
            if (altar.open < 0.5F || BOOK_RANDOM.nextInt(40) == 0) {
                float oldFlipTarget = altar.flipT;
                do {
                    altar.flipT += BOOK_RANDOM.nextInt(4) - BOOK_RANDOM.nextInt(4);
                } while (oldFlipTarget == altar.flipT);
            }
        } else {
            altar.tRot += 0.02F;
            altar.open -= 0.1F;
        }

        while (altar.rot >= (float) Math.PI) {
            altar.rot -= (float) (Math.PI * 2.0D);
        }
        while (altar.rot < (float) -Math.PI) {
            altar.rot += (float) (Math.PI * 2.0D);
        }
        while (altar.tRot >= (float) Math.PI) {
            altar.tRot -= (float) (Math.PI * 2.0D);
        }
        while (altar.tRot < (float) -Math.PI) {
            altar.tRot += (float) (Math.PI * 2.0D);
        }

        float rotationDelta = altar.tRot - altar.rot;
        while (rotationDelta >= (float) Math.PI) {
            rotationDelta -= (float) (Math.PI * 2.0D);
        }
        while (rotationDelta < (float) -Math.PI) {
            rotationDelta += (float) (Math.PI * 2.0D);
        }
        altar.rot += rotationDelta * 0.4F;
        altar.open = Mth.clamp(altar.open, 0.0F, 1.0F);
        altar.time++;
        altar.oFlip = altar.flip;
        float flipDelta = Mth.clamp((altar.flipT - altar.flip) * 0.4F, -0.2F, 0.2F);
        altar.flipA += (flipDelta - altar.flipA) * 0.9F;
        altar.flip += altar.flipA;
        if (altar.isRitualActive() && altar.getShieldCount() > 0) {
            altar.shieldFade = Math.min(1.0F, altar.shieldFade + 0.2F);
        } else {
            altar.shieldFade = Math.max(0.0F, altar.shieldFade - 0.1F);
        }
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, PurificationAltarBlockEntity altar) {
        if (!altar.upperPartSynchronized) {
            altar.syncUpperPart();
        }
        if (!state.getValue(PurificationAltarBlock.RITUAL_ACTIVE)) {
            return;
        }
        if (!altar.baseLightSynchronized) {
            altar.syncBaseLight(true);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (altar.altarHitCooldown > 0) {
            altar.altarHitCooldown--;
        }

        ServerPlayer player = altar.ritualPlayer == null
                ? null
                : serverLevel.getServer().getPlayerList().getPlayer(altar.ritualPlayer);
        if (player == null || !player.isAlive() || player.level() != level) {
            altar.cancelRitual("message.theaurorian2.purification.player_dead", false);
            return;
        }

        long elapsed = Math.max(0L, level.getGameTime() - altar.ritualStartedAt);
        altar.updateProgressEvent(player, elapsed);

        if (elapsed < RIFT_SPAWN_STOP_TICKS
                && level.getGameTime() >= altar.nextRiftAt
                && altar.countRifts(serverLevel) < MAX_RIFTS) {
            altar.spawnRift(serverLevel, elapsed);
        } else if (elapsed < RIFT_SPAWN_STOP_TICKS
                && level.getGameTime() >= altar.nextRiftAt) {
            altar.nextRiftAt = level.getGameTime() + 20L;
        }

        if (elapsed >= CHANNEL_TICKS) {
            if (altar.getShieldCount() > 0
                    && MoonShieldSystem.purify(player)) {
                altar.cleanupRitualEntities(serverLevel);
                altar.clearProgressEvent();
                altar.setRitualMusicPlaying(serverLevel, false);
                altar.setRitualActive(false);
                altar.ritualPlayer = null;
                player.sendSystemMessage(Component.translatable(
                        "message.theaurorian2.purification.completed"));
                level.playSound(
                        null, pos, SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS, 1.2F, 1.15F);
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D,
                        72, 2.5D, 0.15D, 2.5D, 0.05D);
            } else {
                altar.cancelRitual("message.theaurorian2.purification.already_completed", false);
            }
            return;
        }

        if (elapsed % 10L == 0L) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.08D, pos.getZ() + 0.5D,
                    4, 1.8D, 0.05D, 1.8D, 0.01D);
        }
    }

    private void cancelRitual(String reasonKey) {
        cancelRitual(reasonKey, true);
    }

    private void cancelRitual(String reasonKey, boolean playFailureEffects) {
        UUID owner = ritualPlayer;
        clearProgressEvent();
        if (level instanceof ServerLevel serverLevel) {
            cleanupRitualEntities(serverLevel);
            setRitualMusicPlaying(serverLevel, false);
        }
        setRitualActive(false);
        ritualPlayer = null;
        pendingPlayer = null;
        if (level instanceof ServerLevel serverLevel) {
            if (playFailureEffects) {
                serverLevel.playSound(
                        null, worldPosition, SoundEvents.GLASS_BREAK,
                        SoundSource.BLOCKS, 0.7F, 0.8F);
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.1D, worldPosition.getZ() + 0.5D,
                        12, 0.8D, 0.05D, 0.8D, 0.02D);
            }
            if (owner != null
                    && serverLevel.getServer().getPlayerList().getPlayer(owner) instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable(reasonKey));
            }
        }
    }

    private void cleanupRitualEntities(ServerLevel serverLevel) {
        AABB searchArea = new AABB(worldPosition).inflate(24.0D);
        serverLevel.getEntitiesOfClass(
                PurificationRiftEntity.class,
                searchArea,
                rift -> rift.belongsTo(worldPosition, ritualId))
                .forEach(PurificationRiftEntity::beginClosing);
        serverLevel.getEntitiesOfClass(
                PurificationRitualZombieEntity.class,
                searchArea,
                zombie -> zombie.belongsTo(worldPosition, ritualId))
                .forEach(Entity::discard);
    }

    private void setRitualMusicPlaying(ServerLevel serverLevel, boolean playing) {
        PacketDistributor.sendToPlayersInDimension(
                serverLevel, new PurificationRitualMusicPayload(playing));
    }

    private void setRitualActive(boolean active) {
        if (level == null || getBlockState().getValue(PurificationAltarBlock.RITUAL_ACTIVE) == active) {
            return;
        }
        level.setBlock(
                worldPosition,
                getBlockState().setValue(PurificationAltarBlock.RITUAL_ACTIVE, active),
                Block.UPDATE_ALL);
        syncUpperPart();
        syncBaseLight(active);
        setChanged();
    }

    private void syncUpperPart() {
        if (level == null) {
            return;
        }
        BlockPos upperPos = worldPosition.above();
        BlockState upperState = level.getBlockState(upperPos);
        boolean active = getBlockState().getValue(PurificationAltarBlock.RITUAL_ACTIVE);
        if (upperState.is(ModBlocks.PURIFICATION_ALTAR_UPPER.get())) {
            if (upperState.getValue(PurificationAltarUpperBlock.RITUAL_ACTIVE) != active) {
                level.setBlock(
                        upperPos,
                        upperState.setValue(PurificationAltarUpperBlock.RITUAL_ACTIVE, active),
                        Block.UPDATE_ALL);
            }
        } else if (upperState.canBeReplaced()) {
            level.setBlock(
                    upperPos,
                    ModBlocks.PURIFICATION_ALTAR_UPPER.get().defaultBlockState()
                            .setValue(PurificationAltarUpperBlock.RITUAL_ACTIVE, active),
                    Block.UPDATE_ALL);
        }
        upperPartSynchronized = true;
    }

    private void syncBaseLight(boolean active) {
        if (level == null) {
            return;
        }
        PurificationAltarBaseBlock.setRitualActive(level, worldPosition.below(), active);
        baseLightSynchronized = true;
    }

    private void setShieldCount(int count) {
        if (level == null) {
            return;
        }
        int clamped = Mth.clamp(count, 0, INITIAL_SHIELDS);
        if (getShieldCount() == clamped) {
            return;
        }
        level.setBlock(
                worldPosition,
                getBlockState().setValue(PurificationAltarBlock.SHIELD_COUNT, clamped),
                Block.UPDATE_ALL);
        setChanged();
    }

    private void updateProgressEvent(ServerPlayer player, long elapsed) {
        if (progressEvent == null) {
            long dimensionBits = (long) level.dimension().identifier().hashCode() << 32;
            UUID eventId = new UUID(
                    PROGRESS_EVENT_ID_PREFIX,
                    player.getUUID().getLeastSignificantBits() ^ worldPosition.asLong() ^ dimensionBits);
            progressEvent = new ServerBossEvent(
                    eventId,
                    Component.translatable("gui.theaurorian2.purification.title"),
                    BossEvent.BossBarColor.WHITE,
                    BossEvent.BossBarOverlay.PROGRESS);
            lastProgressStep = -1;
        }

        int progressStep = Mth.clamp(
                (int) Math.ceil(Math.min(elapsed, CHANNEL_TICKS)
                        * PROGRESS_STEPS / (double) CHANNEL_TICKS),
                0,
                PROGRESS_STEPS);
        if (progressStep != lastProgressStep) {
            progressEvent.setProgress(progressStep / (float) PROGRESS_STEPS);
            lastProgressStep = progressStep;
        }
        if (!progressEvent.getPlayers().contains(player)) {
            progressEvent.addPlayer(player);
        }
    }

    private void clearProgressEvent() {
        if (progressEvent != null) {
            progressEvent.removeAllPlayers();
            progressEvent = null;
            lastProgressStep = -1;
        }
    }

    public static boolean isProgressEvent(UUID eventId) {
        return eventId.getMostSignificantBits() == PROGRESS_EVENT_ID_PREFIX;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (ritualPlayer != null) {
            output.putString("ritual_player", ritualPlayer.toString());
            output.putLong("ritual_started_at", ritualStartedAt);
            output.putLong("ritual_id", ritualId);
            output.putLong("next_rift_at", nextRiftAt);
            output.putFloat("last_health", lastHealth);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ritualPlayer = input.getString("ritual_player")
                .flatMap(PurificationAltarBlockEntity::parseUuid)
                .orElse(null);
        ritualStartedAt = input.getLongOr("ritual_started_at", 0L);
        ritualId = input.getLongOr("ritual_id", 0L);
        nextRiftAt = input.getLongOr("next_rift_at", 0L);
        lastHealth = input.getFloatOr("last_health", 0.0F);
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void setRemoved() {
        clearProgressEvent();
        if (level instanceof ServerLevel serverLevel) {
            cleanupRitualEntities(serverLevel);
        }
        super.setRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
