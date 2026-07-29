package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class AurorianWaterGrassBlock extends SeagrassBlock {

    public AurorianWaterGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState lowerState = ModBlocks.TALL_AURORIAN_WATER_GRASS.get().defaultBlockState();
        BlockState upperState = lowerState.setValue(TallAurorianWaterGrassBlock.HALF, DoubleBlockHalf.UPPER);
        level.setBlock(pos, lowerState, 2);
        level.setBlock(pos.above(), upperState, 2);
    }
}
