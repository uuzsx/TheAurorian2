package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class AurorianKelpFeature extends Feature<NoneFeatureConfiguration> {

    public AurorianKelpFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int placedBlocks = 0;
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int oceanFloorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, origin.getX(), origin.getZ());
        BlockPos kelpPos = new BlockPos(origin.getX(), oceanFloorY, origin.getZ());

        if (!level.getBlockState(kelpPos).is(Blocks.WATER)) {
            return false;
        }

        BlockState kelpState = ModBlocks.AURORIAN_KELP.get().defaultBlockState();
        BlockState kelpPlantState = ModBlocks.AURORIAN_KELP_PLANT.get().defaultBlockState();
        int height = 1 + random.nextInt(10);

        for (int index = 0; index <= height; index++) {
            if (level.getBlockState(kelpPos).is(Blocks.WATER)
                    && level.getBlockState(kelpPos.above()).is(Blocks.WATER)
                    && kelpPlantState.canSurvive(level, kelpPos)) {
                if (index == height) {
                    level.setBlock(
                            kelpPos,
                            kelpState.setValue(KelpBlock.AGE, 20 + random.nextInt(4)),
                            2);
                } else {
                    level.setBlock(kelpPos, kelpPlantState, 2);
                }
                placedBlocks++;
            } else if (index > 0) {
                BlockPos below = kelpPos.below();
                if (kelpState.canSurvive(level, below)
                        && !level.getBlockState(below.below()).is(ModBlocks.AURORIAN_KELP.get())) {
                    level.setBlock(
                            below,
                            kelpState.setValue(KelpBlock.AGE, 20 + random.nextInt(4)),
                            2);
                    placedBlocks++;
                }
                break;
            }
            kelpPos = kelpPos.above();
        }

        return placedBlocks > 0;
    }
}
