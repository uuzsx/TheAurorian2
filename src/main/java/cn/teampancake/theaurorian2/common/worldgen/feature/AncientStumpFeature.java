package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.block.GroundMushroomBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public final class AncientStumpFeature extends Feature<BlockStateConfiguration> {

    private static final int UPDATE_FLAGS = 19;

    public AncientStumpFeature() {
        super(BlockStateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!canPlaceStump(level, origin)) {
            return false;
        }

        BlockState log = context.config().state
                .trySetValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(origin.offset(x, 0, z), log, UPDATE_FLAGS);
            }
        }

        placeMushrooms(level, origin, context.random());
        return true;
    }

    private static boolean canPlaceStump(WorldGenLevel level, BlockPos origin) {
        if (origin.getY() <= level.getMinY() || origin.getY() + 1 >= level.getMaxY()) {
            return false;
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos logPos = origin.offset(x, 0, z);
                if (!level.getBlockState(logPos.below()).is(BlockTags.DIRT)
                        || !canReplace(level.getBlockState(logPos))
                        || !canReplace(level.getBlockState(logPos.above()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void placeMushrooms(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int mushroomCount = 2 + random.nextInt(3);
        Set<BlockPos> positions = new HashSet<>();
        while (positions.size() < mushroomCount) {
            positions.add(origin.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1));
        }

        for (BlockPos pos : positions) {
            Block mushroom = random.nextBoolean()
                    ? ModBlocks.WHITE_GROUND_MUSHROOM.get()
                    : ModBlocks.BLUE_GROUND_MUSHROOM.get();
            BlockState state = mushroom.defaultBlockState()
                    .setValue(GroundMushroomBlock.FACING,
                            Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(GroundMushroomBlock.VARIANT, random.nextInt(3));
            level.setBlock(pos, state, UPDATE_FLAGS);
        }
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES);
    }
}
