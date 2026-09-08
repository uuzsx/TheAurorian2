package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianJugBlock extends WaterVesselBlock {
    public static final MapCodec<AurorianJugBlock> CODEC = simpleCodec(AurorianJugBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(
            box(4.5, 0, 4.5, 11.5, 8, 11.5), box(5.5, 8, 5.5, 10.5, 9, 10.5),
            box(4.5, 9, 4.5, 11.5, 11, 11.5), box(11.5, 1, 7.875, 14.5, 7, 8.125)));

    public AurorianJugBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
