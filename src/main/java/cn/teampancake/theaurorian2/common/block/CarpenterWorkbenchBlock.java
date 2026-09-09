package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Two-cell decorative workbench. Crafting behavior is intentionally not assigned yet. */
public final class CarpenterWorkbenchBlock extends PairedFurnitureBlock {
    public static final MapCodec<CarpenterWorkbenchBlock> CODEC = simpleCodec(CarpenterWorkbenchBlock::new);
    // Original model bounds, translated +16 X; rotated tools use their enclosing boxes.
    private static final VoxelShape BODY = Shapes.or(
            box(28, 0, 1, 31, 14, 4), box(28, 0, 12, 31, 14, 15),
            box(1, 0, 12, 4, 14, 15), box(1, 0, 1, 4, 14, 4),
            box(0, 14, 11, 32, 16, 16), box(0, 14, 5, 32, 16, 10), box(0, 14, 0, 32, 16, 4),
            box(23, 12, -1, 28, 14, 17), box(4, 12, -1, 9, 14, 17), box(13.5, 12, -1, 18.5, 14, 17),
            box(16, 16, 1, 19, 19, 14),
            box(27.606494, 16, 5.056632, 30.434921, 20, 7.885059),
            box(24.778067, 16, 7.885059, 27.606494, 20, 10.713487),
            box(23.363853, 16, 3.642419, 29.020707, 20, 9.299273),
            box(19.833524, 17, 0.190507, 26.197485, 19, 6.554468),
            box(24.913945, 17, 8.936430, 32.146468, 19, 13.462973),
            box(20.070376, 16, 8.843576, 26.827362, 20, 17.765346));
    private static final Map<Direction, VoxelShape> FIRST = Shapes.rotateHorizontal(
            Shapes.join(BODY, box(-16, 0, -16, 16, 32, 32), BooleanOp.AND));
    private static final Map<Direction, VoxelShape> SECOND_SHAPES = Shapes.rotateHorizontal(
            Shapes.join(BODY, box(16, 0, -16, 48, 32, 32), BooleanOp.AND).move(-1, 0, 0));

    public CarpenterWorkbenchBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected Direction placementFacing(BlockPlaceContext context) { return context.getHorizontalDirection(); }

    @Override
    protected Direction extensionDirection(BlockState state) { return state.getValue(FACING).getClockWise(); }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        return mirror == Mirror.NONE ? mirrored : mirrored.cycle(SECOND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(SECOND) ? SECOND_SHAPES : FIRST).get(state.getValue(FACING));
    }
}
