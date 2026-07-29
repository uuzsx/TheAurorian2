package cn.teampancake.theaurorian2.common.worldgen.feature;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class AncientTreeFeatureSupport {

    private static final int PLACEMENT_SEARCH_RADIUS = 1;
    private static final int PLACEMENT_SEARCH_ATTEMPTS = 8;
    private static final int MAX_PLACEMENT_CANDIDATES = 4;

    private AncientTreeFeatureSupport() {
    }

    static Set<BlockPos> findPlacementCandidates(
            WorldGenLevel level, BlockPos requestedOrigin, RandomSource random) {
        Set<BlockPos> candidates = new LinkedHashSet<>();
        ChunkPos chunk = new ChunkPos(requestedOrigin.getX() >> 4, requestedOrigin.getZ() >> 4);
        int centerX = chunk.getMiddleBlockX();
        int centerZ = chunk.getMiddleBlockZ();
        BlockPos requestedCandidate = findTerrainAlignedOrigin(level, centerX, centerZ);
        if (requestedCandidate != null) {
            candidates.add(requestedCandidate);
        }

        for (int attempt = 0;
                attempt < PLACEMENT_SEARCH_ATTEMPTS && candidates.size() < MAX_PLACEMENT_CANDIDATES;
                attempt++) {
            int x = centerX
                    + random.nextInt(PLACEMENT_SEARCH_RADIUS * 2 + 1) - PLACEMENT_SEARCH_RADIUS;
            int z = centerZ
                    + random.nextInt(PLACEMENT_SEARCH_RADIUS * 2 + 1) - PLACEMENT_SEARCH_RADIUS;
            BlockPos candidate = findTerrainAlignedOrigin(level, x, z);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    static boolean extendTrunkToTerrain(
            WorldGenLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> logs,
            BlockState verticalLog) {
        int extendedColumns = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos support = origin.offset(x, -1, z);
                if (level.getBlockState(support).is(BlockTags.DIRT)) {
                    continue;
                }
                BlockState gapState = level.getBlockState(support);
                if (++extendedColumns > 2
                        || (!gapState.isAir() && !gapState.is(BlockTags.REPLACEABLE_BY_TREES))
                        || !level.getBlockState(support.below()).is(BlockTags.DIRT)) {
                    return false;
                }
                logs.put(support, verticalLog);
            }
        }
        return true;
    }

    private static BlockPos findTerrainAlignedOrigin(WorldGenLevel level, int centerX, int centerZ) {
        int[][] surfaceHeights = new int[3][3];
        int highestSurface = level.getMinY();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                int surfaceY = level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG, centerX + x, centerZ + z);
                surfaceHeights[x + 1][z + 1] = surfaceY;
                highestSurface = Math.max(highestSurface, surfaceY);
            }
        }

        int directSupports = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                int surfaceY = surfaceHeights[x + 1][z + 1];
                int drop = highestSurface - surfaceY;
                if (drop > 1) {
                    return null;
                }
                BlockPos ground = new BlockPos(centerX + x, surfaceY - 1, centerZ + z);
                if (!level.getBlockState(ground).is(BlockTags.DIRT)) {
                    return null;
                }
                if (drop == 0) {
                    directSupports++;
                }
            }
        }
        return directSupports >= 7 ? new BlockPos(centerX, highestSurface, centerZ) : null;
    }
}
