package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

final class AncientTreeFeatureSupport {

    private static final int PLACEMENT_SEARCH_RADIUS = 1;
    private static final int PLACEMENT_SEARCH_ATTEMPTS = 8;
    private static final int MAX_PLACEMENT_CANDIDATES = 4;
    private static final int LEAF_LITTER_UPDATE_FLAGS = 19;
    private static final int OUTER_LEAF_LITTER_RADIUS = 7;
    private static final int OUTER_LEAF_LITTER_HEIGHT = 3;
    private static final int OUTER_LEAF_LITTER_TRIES = 200;
    private static final int INNER_LEAF_LITTER_RADIUS = 4;
    private static final int INNER_LEAF_LITTER_HEIGHT = 2;
    private static final int INNER_LEAF_LITTER_TRIES = 280;

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

    /** Places the denser two-ring ground litter used beneath the 3x3 ancient trees. */
    static void placeLeafLitterUnderAncientTree(
            WorldGenLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> logs,
            RandomSource random) {
        if (logs.isEmpty()) {
            return;
        }

        int baseY = logs.keySet().stream().mapToInt(BlockPos::getY).min().orElse(origin.getY());
        int minX = origin.getX();
        int maxX = origin.getX();
        int minZ = origin.getZ();
        int maxZ = origin.getZ();
        for (BlockPos log : logs.keySet()) {
            if (log.getY() != baseY) {
                continue;
            }
            minX = Math.min(minX, log.getX());
            maxX = Math.max(maxX, log.getX());
            minZ = Math.min(minZ, log.getZ());
            maxZ = Math.max(maxZ, log.getZ());
        }

        placeLeafLitterPass(
                level, random, minX, maxX, minZ, maxZ, baseY,
                OUTER_LEAF_LITTER_RADIUS, OUTER_LEAF_LITTER_HEIGHT,
                OUTER_LEAF_LITTER_TRIES, 3);
        placeLeafLitterPass(
                level, random, minX, maxX, minZ, maxZ, baseY,
                INNER_LEAF_LITTER_RADIUS, INNER_LEAF_LITTER_HEIGHT,
                INNER_LEAF_LITTER_TRIES, 4);
    }

    private static void placeLeafLitterPass(
            WorldGenLevel level,
            RandomSource random,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int radius,
            int height,
            int tries,
            int maxSegmentAmount) {
        int minCandidateX = minX - radius;
        int maxCandidateX = maxX + radius;
        int minCandidateZ = minZ - radius;
        int maxCandidateZ = maxZ + radius;
        BlockState litter = ModBlocks.SILENT_TREE_LEAF_LITTER.get().defaultBlockState();

        for (int attempt = 0; attempt < tries; attempt++) {
            int x = minCandidateX + random.nextInt(maxCandidateX - minCandidateX + 1);
            int y = baseY - height + random.nextInt(height * 2 + 1);
            int z = minCandidateZ + random.nextInt(maxCandidateZ - minCandidateZ + 1);
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos litterPos = groundPos.above();
            if (litterPos.getY() >= level.getMaxY()) {
                continue;
            }

            BlockState aboveState = level.getBlockState(litterPos);
            if (!aboveState.isAir() && !aboveState.is(Blocks.VINE)) {
                continue;
            }
            if (!level.getBlockState(groundPos).isSolidRender()
                    || level.getHeightmapPos(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, groundPos).getY()
                    > litterPos.getY()) {
                continue;
            }

            BlockState selected = litter
                    .setValue(LeafLitterBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(BlockStateProperties.SEGMENT_AMOUNT, 1 + random.nextInt(maxSegmentAmount));
            level.setBlock(litterPos, selected, LEAF_LITTER_UPDATE_FLAGS);
        }
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
