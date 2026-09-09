package cn.teampancake.theaurorian2.common.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The supplied two barrel shapes, backed by the vanilla barrel inventory. */
public final class AurorianStorageBarrelBlock extends BarrelBlock {
    private static final MapCodec<BarrelBlock> STORAGE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("horizontal").forGetter(block -> ((AurorianStorageBarrelBlock) block).horizontal),
            propertiesCodec()).apply(instance, AurorianStorageBarrelBlock::new));
    private static final VoxelShape UPRIGHT = Shapes.or(
            box(2, 1, 2, 14, 15, 14), box(1, 2, 1, 15, 4, 15), box(1, 12, 1, 15, 14, 15),
            box(2, 15, 13, 14, 16, 14), box(2, 15, 2, 14, 16, 3), box(13, 15, 3, 14, 16, 13),
            box(2, 15, 3, 3, 16, 13), box(2, 0, 3, 3, 1, 13), box(2, 0, 13, 14, 1, 14),
            box(13, 0, 3, 14, 1, 13), box(2, 0, 2, 14, 1, 3));
    // Original barrel_3 coordinates and its slight offset are intentional source geometry.
    private static final Map<Direction, VoxelShape> HORIZONTAL = Shapes.rotateHorizontal(Shapes.or(
            box(0.62526, 1.02474, 2, 14.62526, 13.02474, 14),
            box(11.62526, 0.02474, 1, 13.62526, 14.02474, 15),
            box(1.62526, 0.02474, 1, 3.62526, 14.02474, 15),
            box(-0.37474, 1.02474, 13, 0.62526, 13.02474, 14),
            box(-0.37474, 1.02474, 2, 0.62526, 13.02474, 3),
            box(-0.37474, 12.02474, 3, 0.62526, 13.02474, 13),
            box(-0.37474, 1.02474, 3, 0.62526, 2.02474, 13),
            box(14.62526, 1.02474, 3, 15.62526, 2.02474, 13),
            box(14.62526, 1.02474, 13, 15.62526, 13.02474, 14),
            box(14.62526, 12.02474, 3, 15.62526, 13.02474, 13),
            box(14.62526, 1.02474, 2, 15.62526, 13.02474, 3)), OctahedralGroup.BLOCK_ROT_Y_90);
    private final boolean horizontal;

    public AurorianStorageBarrelBlock(boolean horizontal, Properties properties) {
        super(properties);
        this.horizontal = horizontal;
        registerDefaultState(defaultBlockState().setValue(FACING, horizontal ? Direction.NORTH : Direction.UP));
    }

    @Override
    public MapCodec<BarrelBlock> codec() { return STORAGE_CODEC; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, horizontal ? context.getHorizontalDirection().getOpposite() : Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return horizontal ? HORIZONTAL.getOrDefault(state.getValue(FACING), HORIZONTAL.get(Direction.NORTH)) : UPRIGHT;
    }
}
