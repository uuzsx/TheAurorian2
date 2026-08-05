package cn.teampancake.theaurorian2.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Four-stage legacy crop with vanilla-style random growth. */
public final class LegacyAgeThreeCropBlock extends BushBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public LegacyAgeThreeCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9
                || !net.neoforged.neoforge.common.CommonHooks.canCropGrow(
                        level, pos, state, random.nextInt(5) == 0)) {
            return;
        }
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
        net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }
}
