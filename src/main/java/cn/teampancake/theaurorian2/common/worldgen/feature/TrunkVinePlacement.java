package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

final class TrunkVinePlacement {

    private static final int UPDATE_FLAGS = 19;

    private TrunkVinePlacement() {
    }

    static void placeOnAncientTree(
            WorldGenLevel level,
            Map<BlockPos, BlockState> logs,
            RandomSource random) {
        if (logs.isEmpty()) {
            return;
        }

        int minY = logs.keySet().stream().mapToInt(BlockPos::getY).min().orElseThrow();
        int maxY = logs.keySet().stream().mapToInt(BlockPos::getY).max().orElseThrow();
        int denseCanopyStart = minY + Math.max(8, (maxY - minY) * 2 / 3);
        boolean placed = false;

        for (BlockPos logPos : logs.keySet().stream()
                .sorted(Comparator.comparingInt(BlockPos::getY))
                .toList()) {
            float density = logPos.getY() < denseCanopyStart ? 0.72F : 0.48F;
            for (Direction outward : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (random.nextFloat() < density && placeAt(level, logPos, outward)) {
                    placed = true;
                }
            }
        }

        if (!placed) {
            for (BlockPos logPos : logs.keySet()) {
                for (Direction outward : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                    if (placeAt(level, logPos, outward)) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean placeAt(WorldGenLevel level, BlockPos logPos, Direction outward) {
        BlockPos vinePos = logPos.relative(outward);
        if (!level.isEmptyBlock(vinePos)) {
            return false;
        }
        level.setBlock(
                vinePos,
                ModBlocks.AURORIAN_VINE.get().defaultBlockState()
                        .setValue(VineBlock.getPropertyForFace(outward.getOpposite()), true),
                UPDATE_FLAGS);
        return true;
    }
}
