package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class AurorianGrassRockFeature extends Feature<NoneFeatureConfiguration> {

    private static final int FOOTPRINT_SIZE = 2;
    private static final int CLEARANCE_HEIGHT = 2;

    public AurorianGrassRockFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!hasCompleteGrassPlatform(level, origin) || !hasClearableFootprint(level, origin)) {
            return false;
        }

        clearFootprint(level, origin);
        level.setBlock(origin, ModBlocks.AURORIAN_GRASS_ROCK.get().defaultBlockState(), 3);
        return true;
    }

    private static boolean hasCompleteGrassPlatform(WorldGenLevel level, BlockPos origin) {
        for (int x = 0; x < FOOTPRINT_SIZE; x++) {
            for (int z = 0; z < FOOTPRINT_SIZE; z++) {
                BlockPos supportPos = origin.offset(x, -1, z);
                BlockState support = level.getBlockState(supportPos);
                if (!support.is(ModBlocks.AURORIAN_GRASS_BLOCK)
                        || !support.isFaceSturdy(level, supportPos, Direction.UP)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasClearableFootprint(WorldGenLevel level, BlockPos origin) {
        for (int y = 0; y < CLEARANCE_HEIGHT; y++) {
            for (int x = 0; x < FOOTPRINT_SIZE; x++) {
                for (int z = 0; z < FOOTPRINT_SIZE; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void clearFootprint(WorldGenLevel level, BlockPos origin) {
        for (int y = CLEARANCE_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < FOOTPRINT_SIZE; x++) {
                for (int z = 0; z < FOOTPRINT_SIZE; z++) {
                    BlockPos target = origin.offset(x, y, z);
                    if (!level.getBlockState(target).isAir()) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
