package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Farmland-state placeholder retaining legacy moisture values without enabling cultivation yet. */
public final class LegacyMoistureBlock extends Block {
    public static final MapCodec<LegacyMoistureBlock> CODEC = simpleCodec(LegacyMoistureBlock::new);
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;

    public LegacyMoistureBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }
}
