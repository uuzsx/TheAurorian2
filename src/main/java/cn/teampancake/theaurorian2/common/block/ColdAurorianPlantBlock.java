package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class ColdAurorianPlantBlock extends BushBlock {

    public ColdAurorianPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.AURORIAN_GRASS_BLOCK.get()) || state.is(Blocks.SNOW_BLOCK);
    }
}
