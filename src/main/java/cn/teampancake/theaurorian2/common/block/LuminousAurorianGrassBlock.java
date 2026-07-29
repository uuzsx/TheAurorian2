package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class LuminousAurorianGrassBlock extends TallGrassBlock {

    public LuminousAurorianGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return ModBlocks.TALL_AURORIAN_GRASS_LIGHT.get().defaultBlockState().canSurvive(level, pos)
                && level.isEmptyBlock(pos.above())
                && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        DoublePlantBlock.placeAt(
                level, ModBlocks.TALL_AURORIAN_GRASS_LIGHT.get().defaultBlockState(), pos, 2);
    }
}
