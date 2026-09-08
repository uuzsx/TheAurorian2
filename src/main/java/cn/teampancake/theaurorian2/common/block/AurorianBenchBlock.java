package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Two independent seats on the original two-block medieval bench. */
public final class AurorianBenchBlock extends PairedFurnitureBlock {
    public static final MapCodec<AurorianBenchBlock> CODEC = simpleCodec(AurorianBenchBlock::new);
    public static final double SEAT_HEIGHT = 7.0 / 16.0;
    private static final Map<Direction, VoxelShape> FIRST_SHAPES = shapes(1, 3);
    private static final Map<Direction, VoxelShape> SECOND_SHAPES = shapes(13, 15);

    public AurorianBenchBlock(Properties properties) { super(properties); }

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

    private static Map<Direction, VoxelShape> shapes(int legMin, int legMax) {
        return Shapes.rotateHorizontal(Shapes.or(box(0, 5, 2, 16, 7, 14), box(legMin, 0, 3, legMax, 5, 13)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(SECOND) ? SECOND_SHAPES : FIRST_SHAPES).get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return AurorianStoolBlock.trySit(level, pos, player);
    }
}
