package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class StarblightPlantBlock extends BushBlock {

    public static final MapCodec<BushBlock> CODEC = simpleCodec(StarblightPlantBlock::new);

    public StarblightPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BushBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.is(ModBlocks.STARBLIGHT_FLOWER.get())) {
            // Keep vanilla's particles, sound, timing, light and exposure checks together.
            Blocks.FIREFLY_BUSH.animateTick(state, level, pos, random);
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.STARBLIGHT_SOIL.get()) || state.is(ModBlocks.AURORIAN_DIRT.get());
    }
}
