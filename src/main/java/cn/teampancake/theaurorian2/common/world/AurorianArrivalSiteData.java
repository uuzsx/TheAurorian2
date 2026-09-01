package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/** Stores and creates the single shared receiving site used by all summoned players. */
public final class AurorianArrivalSiteData extends SavedData {

    public static final Codec<AurorianArrivalSiteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.optionalFieldOf("arrival_pos")
                            .forGetter(data -> Optional.ofNullable(data.arrivalPos)),
                    Codec.LONG.optionalFieldOf("first_arrival_day")
                            .forGetter(data -> Optional.ofNullable(data.firstArrivalDay)))
            .apply(instance, AurorianArrivalSiteData::new));
    public static final SavedDataType<AurorianArrivalSiteData> TYPE = new SavedDataType<>(
            TheAurorian2.id("aurorian_arrival_site"), AurorianArrivalSiteData::new, CODEC);

    private @Nullable BlockPos arrivalPos;
    private @Nullable Long firstArrivalDay;

    public AurorianArrivalSiteData() {
    }

    private AurorianArrivalSiteData(Optional<BlockPos> arrivalPos, Optional<Long> firstArrivalDay) {
        this.arrivalPos = arrivalPos.orElse(null);
        this.firstArrivalDay = firstArrivalDay.orElse(null);
    }

    public static BlockPos getOrCreate(
            ServerLevel level, ServerPlayer player, long currentOverworldDay) {
        AurorianArrivalSiteData data = level.getDataStorage().computeIfAbsent(TYPE);
        if (data.arrivalPos == null) {
            BlockPos suggested = level.getRespawnData().pos();
            data.arrivalPos = player.adjustSpawnLocation(level, suggested).immutable();
            data.firstArrivalDay = currentOverworldDay;
            buildReceivingPlatform(level, data.arrivalPos);
            data.setDirty();
        } else if (data.firstArrivalDay == null) {
            // Existing saves keep their absolute-day cycle instead of resetting on upgrade.
            data.firstArrivalDay = 0L;
            data.setDirty();
        }
        return data.arrivalPos;
    }

    public static long relativeBlessingDay(ServerLevel level, long currentOverworldDay) {
        AurorianArrivalSiteData data = level.getDataStorage().computeIfAbsent(TYPE);
        return data.firstArrivalDay == null
                ? currentOverworldDay
                : currentOverworldDay - data.firstArrivalDay;
    }

    private static void buildReceivingPlatform(ServerLevel level, BlockPos center) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                int distanceSquared = x * x + z * z;
                if (distanceSquared > 16) {
                    continue;
                }

                BlockPos floorPos = center.offset(x, -1, z);
                level.setBlock(floorPos, platformState(x, z, distanceSquared), Block.UPDATE_ALL);
                fillSupport(level, floorPos.below());
                for (int y = 0; y <= 2; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static BlockState platformState(int x, int z, int distanceSquared) {
        if (x == 0 && z == 0) {
            return ModStructureBlocks.CHISELED_MOON_TEMPLE_BRICKS.get().defaultBlockState();
        }
        if (distanceSquared == 9 && (x == 0 || z == 0)) {
            return ModStructureBlocks.MOON_TEMPLE_LAMP.get().defaultBlockState();
        }
        if (distanceSquared >= 12) {
            return ModStructureBlocks.SMOOTH_RUNE_STONE.get().defaultBlockState();
        }
        if (x == 0 || z == 0 || Math.abs(x) == Math.abs(z)) {
            return ModStructureBlocks.MOON_TEMPLE_BRICKS.get().defaultBlockState();
        }
        return ModStructureBlocks.SMOOTH_MOON_TEMPLE_BRICKS.get().defaultBlockState();
    }

    private static void fillSupport(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int depth = 0; depth < 4; depth++) {
            BlockState state = level.getBlockState(cursor);
            if (!state.getCollisionShape(level, cursor).isEmpty() && state.getFluidState().isEmpty()) {
                break;
            }
            level.setBlock(
                    cursor,
                    ModStructureBlocks.SMOOTH_MOON_TEMPLE_BRICKS.get().defaultBlockState(),
                    Block.UPDATE_ALL);
            cursor.move(0, -1, 0);
        }
    }
}
