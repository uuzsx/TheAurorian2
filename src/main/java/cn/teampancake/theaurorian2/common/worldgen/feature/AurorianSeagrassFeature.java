package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.block.TallAurorianWaterGrassBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public final class AurorianSeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {

    public AurorianSeagrassFeature() {
        super(ProbabilityFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int x = origin.getX() + random.nextInt(8) - random.nextInt(8);
        int z = origin.getZ() + random.nextInt(8) - random.nextInt(8);
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        BlockPos grassPos = new BlockPos(x, y, z);

        if (!level.getBlockState(grassPos).is(Blocks.WATER)) {
            return false;
        }

        boolean tall = random.nextDouble() < context.config().probability;
        BlockState state = tall
                ? ModBlocks.TALL_AURORIAN_WATER_GRASS.get().defaultBlockState()
                : ModBlocks.AURORIAN_WATER_GRASS.get().defaultBlockState();
        if (!state.canSurvive(level, grassPos)) {
            return false;
        }

        if (tall) {
            BlockPos above = grassPos.above();
            if (!level.getBlockState(above).is(Blocks.WATER)) {
                return false;
            }
            level.setBlock(grassPos, state, 2);
            level.setBlock(above, state.setValue(TallAurorianWaterGrassBlock.HALF, DoubleBlockHalf.UPPER), 2);
        } else {
            level.setBlock(grassPos, state, 2);
        }
        return true;
    }
}
