package cn.teampancake.theaurorian2.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianWaterSurfacePlantBlock extends LilyPadBlock {

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    private final VoxelShape shape;

    public AurorianWaterSurfacePlantBlock(VoxelShape shape, Properties properties) {
        super(properties);
        this.shape = shape;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}
