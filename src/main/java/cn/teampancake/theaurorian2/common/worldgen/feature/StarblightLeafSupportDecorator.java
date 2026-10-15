package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModTreeDecorators;
import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluids;

/** Connects random mangrove foliage during generation, without disabling normal leaf decay. */
public final class StarblightLeafSupportDecorator extends TreeDecorator {

    public static final MapCodec<StarblightLeafSupportDecorator> CODEC =
            MapCodec.unit(StarblightLeafSupportDecorator::new);
    private static final int MAX_DISTANCE = 6;

    @Override
    public void place(Context context) {
        var leaves = new HashSet<>(context.leaves());
        var distances = new HashMap<BlockPos, Integer>();
        var queue = new ArrayDeque<BlockPos>();
        for (BlockPos log : context.logs()) {
            distances.put(log, 0);
            queue.add(log);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            int distance = distances.get(pos) + 1;
            if (distance > MAX_DISTANCE) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (leaves.contains(next) && !distances.containsKey(next)) {
                    distances.put(next, distance);
                    queue.addLast(next);
                }
            }
        }
        // The native random spread placer occasionally leaves isolated pixels in the canopy.
        // Search at most six steps from each unsupported leaf, within this tree's canopy bounds.
        var bounds = net.minecraft.world.level.levelgen.structure.BoundingBox
                .encapsulatingPositions(context.leaves()).orElse(null);
        for (BlockPos leaf : context.leaves()) {
            if (!distances.containsKey(leaf) && bounds != null) {
                connect(context, leaf, bounds, distances, leaves);
            }
        }
        for (BlockPos leaf : leaves) {
            var state = context.level().getBlockState(leaf);
            Integer distance = distances.get(leaf);
            if (distance == null) {
                // Obstructed foliage cannot be supported without replacing terrain. Omit it
                // before the tree is exposed to random ticks, rather than spawning item drops.
                context.setBlock(leaf, state.getValue(LeavesBlock.WATERLOGGED)
                        ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            } else {
                // Decorator positions are excluded from TreeFeature's later distance update.
                context.setBlock(leaf, state.setValue(LeavesBlock.DISTANCE, distance));
            }
        }
    }

    private static void connect(Context context, BlockPos origin,
            net.minecraft.world.level.levelgen.structure.BoundingBox bounds,
            Map<BlockPos, Integer> distances, HashSet<BlockPos> leaves) {
        var queue = new ArrayDeque<BlockPos>();
        var steps = new HashMap<BlockPos, Integer>();
        var parents = new HashMap<BlockPos, BlockPos>();
        queue.add(origin);
        steps.put(origin, 0);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            int step = steps.get(pos);
            Integer support = distances.get(pos);
            if (support != null && step + support <= MAX_DISTANCE) {
                int distance = support;
                for (BlockPos path = parents.get(pos); path != null; path = parents.get(path)) {
                    distances.put(path, ++distance);
                    leaves.add(path);
                    if (!context.level().getBlockState(path).is(ModBlocks.STARBLIGHT_LEAVES.get())) {
                        context.setBlock(path, ModBlocks.STARBLIGHT_LEAVES.get().defaultBlockState()
                                .setValue(LeavesBlock.WATERLOGGED, context.level().getFluidState(path)
                                        .isSourceOfType(Fluids.WATER)));
                    }
                }
                return;
            }
            if (step == MAX_DISTANCE) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (steps.containsKey(next)) {
                    continue;
                }
                var state = context.level().getBlockState(next);
                if (distances.containsKey(next) || bounds.isInside(next)
                        && (leaves.contains(next) || state.isAir() || state.is(Blocks.WATER))) {
                    steps.put(next, step + 1);
                    parents.put(next, pos);
                    queue.addLast(next);
                }
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.STARBLIGHT_LEAF_SUPPORT.get();
    }
}
