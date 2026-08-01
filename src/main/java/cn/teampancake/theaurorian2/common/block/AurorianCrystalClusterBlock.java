package cn.teampancake.theaurorian2.common.block;

import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class AurorianCrystalClusterBlock extends AmethystClusterBlock {

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    public AurorianCrystalClusterBlock(int height, int offset, Properties properties) {
        super(height, offset, properties.lightLevel(state -> state.getValue(LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }
}
