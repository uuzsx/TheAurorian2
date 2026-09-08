package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A contained bucket of water, not a flowing or waterlogged block. */
public final class AurorianWoodBucketBlock extends WaterVesselBlock {
    public static final MapCodec<AurorianWoodBucketBlock> CODEC = simpleCodec(AurorianWoodBucketBlock::new);
    private static final VoxelShape BODY = Shapes.or(box(1.5, 0, 1.5, 14.5, 1, 14.5),
            box(1.5, 1, 1.5, 2.5, 14, 14.5), box(13.5, 1, 1.5, 14.5, 14, 14.5),
            box(2.5, 1, 1.5, 13.5, 14, 2.5), box(2.5, 1, 13.5, 13.5, 14, 14.5));
    private static final VoxelShape NORTH_SOUTH = Shapes.or(BODY,
            box(1.5, 14, 5.5, 2.5, 18, 10.5), box(13.5, 14, 5.5, 14.5, 18, 10.5));
    private static final VoxelShape EAST_WEST = Shapes.or(BODY,
            box(5.5, 14, 1.5, 10.5, 18, 2.5), box(5.5, 14, 13.5, 10.5, 18, 14.5));
    public AurorianWoodBucketBlock(Properties properties) { super(properties); }

    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }
}
