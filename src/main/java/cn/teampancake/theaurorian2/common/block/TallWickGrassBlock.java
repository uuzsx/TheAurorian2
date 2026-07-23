package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class TallWickGrassBlock extends DoublePlantBlock {

    public static final MapCodec<TallWickGrassBlock> CODEC = simpleCodec(TallWickGrassBlock::new);
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    public TallWickGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 15));
    }

    @Override
    public MapCodec<TallWickGrassBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.AURORIAN_GRASS_BLOCK.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int lightLevel = state.getValue(LEVEL);
        if (lightLevel == 0 || random.nextFloat() >= lightLevel / 30.0F) {
            return;
        }

        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(
                ModParticles.WICK.get(), x, y, z,
                random.nextGaussian() * 0.02,
                random.nextGaussian() * 0.02,
                random.nextGaussian() * 0.02);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }
}
