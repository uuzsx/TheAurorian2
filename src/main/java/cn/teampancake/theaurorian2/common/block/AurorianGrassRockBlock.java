package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AurorianGrassRockBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class AurorianGrassRockBlock extends BaseEntityBlock {

    public static final MapCodec<AurorianGrassRockBlock> CODEC = simpleCodec(AurorianGrassRockBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 5.0, 28.0, 18.0, 30.0);
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            Block.box(7.0, 0.0, 11.0, 15.0, 2.0, 19.0),
            Block.box(7.0, 0.0, 16.0, 15.0, 7.0, 24.0),
            Block.box(10.0, 0.0, 9.0, 20.0, 3.0, 19.0),
            Block.box(12.0, 0.0, 15.0, 19.0, 5.0, 20.0),
            Block.box(13.0, 0.0, 22.0, 21.0, 4.0, 29.0),
            Block.box(15.0, 0.0, 15.0, 24.0, 5.0, 24.0),
            Block.box(16.0, 5.0, 19.0, 22.0, 8.0, 23.0),
            Block.box(22.0, 0.0, 13.0, 28.0, 4.0, 19.0));

    public AurorianGrassRockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianGrassRockBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos northWest = pos.below();
        return isSturdySupport(level, northWest)
                && isSturdySupport(level, northWest.east())
                && isSturdySupport(level, northWest.south())
                && isSturdySupport(level, northWest.east().south());
    }

    private static boolean isSturdySupport(LevelReader level, BlockPos pos) {
        BlockState support = level.getBlockState(pos);
        return support.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        return state.canSurvive(context.getLevel(), pos) && hasClearFootprint(context.getLevel(), pos)
                ? state
                : null;
    }

    private static boolean hasClearFootprint(LevelReader level, BlockPos origin) {
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                BlockPos target = origin.offset(x, 0, z);
                if (!target.equals(origin)) {
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        if (directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(
                state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
}
