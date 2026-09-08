package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import cn.teampancake.theaurorian2.common.block.entity.LongMirrorBlockEntity;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class AurorianLongMirrorBlock extends PairedFurnitureBlock implements EntityBlock {
    public static final MapCodec<AurorianLongMirrorBlock> CODEC = simpleCodec(AurorianLongMirrorBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(box(0, 0, 14, 16, 16, 16));

    public AurorianLongMirrorBlock(Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LongMirrorBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected Direction extensionDirection(BlockState state) { return Direction.UP; }

    @Override
    protected Direction placementFacing(BlockPlaceContext context) { return context.getClickedFace(); }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.getClickedFace().getAxis().isHorizontal()) return null;
        BlockState state = super.getStateForPlacement(context);
        return state != null && hasWallSupport(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    private boolean hasWallSupport(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos base = state.getValue(SECOND) ? pos.below() : pos;
        Direction facing = state.getValue(FACING);
        BlockPos wall = base.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing)
                && level.getBlockState(wall.above()).isFaceSturdy(level, wall.above(), facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return hasWallSupport(state, level, pos); }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !hasWallSupport(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
