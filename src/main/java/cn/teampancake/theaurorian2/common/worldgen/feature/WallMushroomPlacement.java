package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.block.WallMushroomBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

final class WallMushroomPlacement {

    private static final int UPDATE_FLAGS = 19;
    private static final float ANCIENT_TREE_CHANCE = 0.40F;

    private WallMushroomPlacement() {
    }

    static Block randomMushroom(RandomSource random) {
        return switch (random.nextInt(3)) {
            case 0 -> ModBlocks.BROWN_MUSHROOM.get();
            case 1 -> ModBlocks.DARK_BROWN_MUSHROOM.get();
            default -> ModBlocks.RED_MUSHROOM.get();
        };
    }

    static BlockState stateFor(Block mushroom, Direction facing, RandomSource random) {
        return mushroom.defaultBlockState()
                .setValue(WallMushroomBlock.FACING, facing)
                .setValue(WallMushroomBlock.VARIANT, random.nextInt(5));
    }

    static void placeOnAncientTree(
            WorldGenLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> logs,
            RandomSource random) {
        if (random.nextFloat() >= ANCIENT_TREE_CHANCE || logs.isEmpty()) {
            return;
        }

        int span = 3;
        int highestLogY = logs.keySet().stream().mapToInt(BlockPos::getY).max().orElse(origin.getY());
        int startY = origin.getY();
        if (highestLogY < startY + span - 1) {
            return;
        }
        int endY = startY + span - 1;

        List<Attachment> attachments = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : logs.entrySet()) {
            BlockPos logPos = entry.getKey();
            BlockState logState = entry.getValue();
            if (logPos.getY() < startY || logPos.getY() > endY
                    || !logState.hasProperty(RotatedPillarBlock.AXIS)
                    || logState.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) {
                continue;
            }
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos mushroomPos = logPos.relative(facing);
                if (!logs.containsKey(mushroomPos) && level.getBlockState(mushroomPos).isAir()) {
                    attachments.add(new Attachment(mushroomPos, facing));
                }
            }
        }

        Block mushroom = randomMushroom(random);
        int targetCount = Math.min(attachments.size(), 14 + random.nextInt(7));
        for (int placed = 0; placed < targetCount; placed++) {
            Attachment attachment = attachments.remove(random.nextInt(attachments.size()));
            level.setBlock(
                    attachment.pos(), stateFor(mushroom, attachment.facing(), random), UPDATE_FLAGS);
        }
    }

    private record Attachment(BlockPos pos, Direction facing) {
    }
}
