package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianLongTableBlock extends PairedFurnitureBlock {
    public static final MapCodec<AurorianLongTableBlock> CODEC = simpleCodec(AurorianLongTableBlock::new);
    private static final Map<Direction, VoxelShape> FIRST_SHAPES = shapes(1, 3);
    private static final Map<Direction, VoxelShape> SECOND_SHAPES = shapes(13, 15);

    public AurorianLongTableBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected Direction placementFacing(BlockPlaceContext context) {
        // Clockwise from the player's facing is their right; keep saved pair directions unchanged.
        return context.getHorizontalDirection();
    }

    @Override
    protected Direction extensionDirection(BlockState state) { return state.getValue(FACING).getClockWise(); }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        return mirror == Mirror.NONE ? mirrored : mirrored.cycle(SECOND);
    }

    private static Map<Direction, VoxelShape> shapes(int legMin, int legMax) {
        return Shapes.rotateHorizontal(Shapes.or(box(0, 14, 0, 16, 16, 16),
                box(legMin, 0, 1.5, legMax, 14, 14.5)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(SECOND) ? SECOND_SHAPES : FIRST_SHAPES).get(state.getValue(FACING));
    }
}
