package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;

final class AurorianDripstoneFeatureSupport {

    private AurorianDripstoneFeatureSupport() {
    }

    static void replaceInBox(WorldGenLevel level, BlockPos origin, int horizontalRadius, int verticalRadius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY(), origin.getY() - verticalRadius);
        int maxY = Math.min(level.getMaxY() - 1, origin.getY() + verticalRadius);
        for (int x = origin.getX() - horizontalRadius; x <= origin.getX() + horizontalRadius; x++) {
            for (int z = origin.getZ() - horizontalRadius; z <= origin.getZ() + horizontalRadius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    replaceAt(level, pos.set(x, y, z));
                }
            }
        }
    }

    static void replaceLargeDripstone(WorldGenLevel level, BlockPos origin) {
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY(), origin.getY() - 48);
        int maxY = Math.min(level.getMaxY() - 1, origin.getY() + 48);

        for (int x = origin.getX() - 3; x <= origin.getX() + 3; x++) {
            for (int z = origin.getZ() - 3; z <= origin.getZ() + 3; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(Blocks.DRIPSTONE_BLOCK)) {
                        open.add(cursor.immutable());
                    }
                }
            }
        }

        while (!open.isEmpty() && visited.size() < 100_000) {
            BlockPos pos = open.removeFirst();
            if (!visited.add(pos)
                    || Math.abs(pos.getX() - origin.getX()) > 32
                    || Math.abs(pos.getZ() - origin.getZ()) > 32
                    || pos.getY() < minY
                    || pos.getY() > maxY
                    || !level.getBlockState(pos).is(Blocks.DRIPSTONE_BLOCK)) {
                continue;
            }
            level.setBlock(pos, ModBlocks.AURORIAN_DRIPSTONE_BLOCK.get().defaultBlockState(), 2);
            for (Direction direction : Direction.values()) {
                open.addLast(pos.relative(direction));
            }
        }
    }

    private static void replaceAt(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.DRIPSTONE_BLOCK)) {
            level.setBlock(pos, ModBlocks.AURORIAN_DRIPSTONE_BLOCK.get().defaultBlockState(), 2);
        } else if (state.is(Blocks.POINTED_DRIPSTONE)) {
            BlockState replacement = ModBlocks.AURORIAN_POINTED_DRIPSTONE.get().defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, state.getValue(PointedDripstoneBlock.TIP_DIRECTION))
                    .setValue(PointedDripstoneBlock.THICKNESS, state.getValue(PointedDripstoneBlock.THICKNESS))
                    .setValue(PointedDripstoneBlock.WATERLOGGED, state.getValue(PointedDripstoneBlock.WATERLOGGED));
            level.setBlock(pos, replacement, 2);
        }
    }
}
