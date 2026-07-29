package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

final class CustomTreeFeatureSupport {

    private static final int UPDATE_FLAGS = 19;

    private CustomTreeFeatureSupport() {
    }

    static boolean place(
            WorldGenLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> logs,
            Set<BlockPos> plannedLeaves,
            Block leavesBlock,
            Block saplingBlock) {
        return place(level, origin, logs, plannedLeaves, leavesBlock, saplingBlock, null, 0.0F, null);
    }

    static boolean place(
            WorldGenLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> logs,
            Set<BlockPos> plannedLeaves,
            Block leavesBlock,
            Block saplingBlock,
            Block fruitingLeavesBlock,
            float fruitingLeafChance,
            RandomSource random) {
        if (origin.getY() <= level.getMinY()) {
            return false;
        }
        int trunkBaseY = logs.keySet().stream().mapToInt(BlockPos::getY).min().orElse(origin.getY());
        Set<BlockPos> trunkBases = logs.keySet().stream()
                .filter(pos -> pos.getY() == trunkBaseY)
                .collect(Collectors.toSet());
        if (trunkBases.isEmpty()
                || trunkBases.stream().anyMatch(pos -> !level.getBlockState(pos.below()).is(BlockTags.DIRT))) {
            return false;
        }
        for (BlockPos pos : logs.keySet()) {
            if (!isInsideLevel(level, pos) || !canReplace(level.getBlockState(pos), saplingBlock)) {
                return false;
            }
        }
        for (BlockPos pos : plannedLeaves) {
            if (!isInsideLevel(level, pos)) {
                return false;
            }
        }

        trunkBases.forEach(pos -> level.setBlock(
                pos.below(), ModBlocks.AURORIAN_DIRT.get().defaultBlockState(), UPDATE_FLAGS));
        logs.forEach((pos, state) -> level.setBlock(pos, state, UPDATE_FLAGS));

        Map<BlockPos, Integer> leafDistances = calculateLeafDistances(logs.keySet(), plannedLeaves);
        BlockState leaves = leavesBlock.defaultBlockState();
        leafDistances.forEach((pos, distance) -> {
            if (!logs.containsKey(pos) && canReplace(level.getBlockState(pos), saplingBlock)) {
                BlockState selectedLeaves = fruitingLeavesBlock != null
                        && random != null
                        && random.nextFloat() < fruitingLeafChance
                        ? fruitingLeavesBlock.defaultBlockState()
                        : leaves;
                level.setBlock(pos, selectedLeaves.setValue(LeavesBlock.DISTANCE, distance), UPDATE_FLAGS);
            }
        });
        return !leafDistances.isEmpty();
    }

    private static Map<BlockPos, Integer> calculateLeafDistances(Set<BlockPos> logs, Set<BlockPos> leaves) {
        Map<BlockPos, Integer> distances = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        logs.forEach(pos -> {
            distances.put(pos, 0);
            queue.add(pos);
        });

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            int distance = distances.get(current);
            if (distance >= LeavesBlock.DECAY_DISTANCE - 1) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (leaves.contains(neighbor) && !distances.containsKey(neighbor)) {
                    distances.put(neighbor, distance + 1);
                    queue.addLast(neighbor);
                }
            }
        }

        distances.keySet().removeAll(logs);
        return distances;
    }

    private static boolean isInsideLevel(WorldGenLevel level, BlockPos pos) {
        return pos.getY() >= level.getMinY() && pos.getY() < level.getMaxY();
    }

    private static boolean canReplace(BlockState state, Block saplingBlock) {
        return state.isAir()
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(BlockTags.LEAVES)
                || state.is(saplingBlock);
    }
}
