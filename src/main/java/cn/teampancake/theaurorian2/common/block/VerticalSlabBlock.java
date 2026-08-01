package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class VerticalSlabBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<VerticalSlabBlock> CODEC = simpleCodec(VerticalSlabBlock::new);
    public static final EnumProperty<VerticalSlabShape> SHAPE =
            EnumProperty.create("shape", VerticalSlabShape.class);
    public static final EnumProperty<Connection> CONNECTION =
            EnumProperty.create("connection", Connection.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape[] SHAPES = {
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0),
        Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0),
        Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0),
        Shapes.block()
    };
    private static final VoxelShape[] CONNECTED_SHAPES = {
        Block.box(8.0, 0.0, 8.0, 16.0, 16.0, 16.0),
        Block.box(0.0, 0.0, 8.0, 8.0, 16.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 8.0),
        Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 8.0)
    };

    public VerticalSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, VerticalSlabShape.NORTH)
                .setValue(CONNECTION, Connection.NONE)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, CONNECTION, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VerticalSlabShape shape = state.getValue(SHAPE);
        Connection connection = state.getValue(CONNECTION);
        if (shape == VerticalSlabShape.FULL || connection == Connection.NONE) {
            return SHAPES[shape.ordinal()];
        }
        Direction direction = shape.direction();
        Direction corner = connection == Connection.LEFT ? direction : direction.getClockWise();
        return CONNECTED_SHAPES[corner.get2DDataValue()];
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
        if (clickedState.is(this) && clickedState.getValue(SHAPE) != VerticalSlabShape.FULL) {
            return clickedState
                    .setValue(SHAPE, VerticalSlabShape.FULL)
                    .setValue(CONNECTION, Connection.NONE)
                    .setValue(WATERLOGGED, false);
        }

        Direction facing = context.getHorizontalDirection();
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(SHAPE, VerticalSlabShape.fromDirection(facing))
                .setValue(CONNECTION, connectionAt(context.getLevel(), context.getClickedPos(), facing))
                .setValue(WATERLOGGED, fluid.is(Fluids.WATER));
    }

    private static Connection connectionAt(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState backState = level.getBlockState(pos.relative(facing));
        if (!(backState.getBlock() instanceof VerticalSlabBlock)
                || backState.getValue(SHAPE) == VerticalSlabShape.FULL) {
            return Connection.NONE;
        }

        Direction backDirection = backState.getValue(SHAPE).direction();
        Connection backConnection = backState.getValue(CONNECTION);
        BlockState leftState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        if ((!isSameFacingSlab(leftState, facing))
                && backDirection == facing.getClockWise()
                && (backConnection == Connection.NONE || backConnection == Connection.RIGHT)) {
            return Connection.RIGHT;
        }

        BlockState rightState = level.getBlockState(pos.relative(facing.getClockWise()));
        if ((!isSameFacingSlab(rightState, facing))
                && backDirection == facing.getCounterClockWise()
                && (backConnection == Connection.NONE || backConnection == Connection.LEFT)) {
            return Connection.LEFT;
        }
        return Connection.NONE;
    }

    private static boolean isSameFacingSlab(BlockState state, Direction facing) {
        return state.getBlock() instanceof VerticalSlabBlock
                && state.getValue(SHAPE) != VerticalSlabShape.FULL
                && state.getValue(SHAPE).direction() == facing;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        VerticalSlabShape shape = state.getValue(SHAPE);
        return shape != VerticalSlabShape.FULL
                && context.getItemInHand().is(this.asItem())
                && shape.direction().getOpposite() == context.getClickedFace();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.getValue(SHAPE) != VerticalSlabShape.FULL
                && SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(SHAPE) != VerticalSlabShape.FULL && fluid == Fluids.WATER;
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
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        VerticalSlabShape shape = state.getValue(SHAPE);
        return shape == VerticalSlabShape.FULL
                ? state
                : state.setValue(CONNECTION, connectionAt(level, pos, shape.direction()));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
    }

    public enum Connection implements StringRepresentable {
        LEFT,
        RIGHT,
        NONE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
