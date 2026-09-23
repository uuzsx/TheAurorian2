package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Static, unticked props. Each occupied cell is selectable; removal pays loot only at the broken cell. */
public class DungeonDecorationBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ALIGNED = BooleanProperty.create("aligned");
    private static final IntegerProperty TWO_PARTS = IntegerProperty.create("part", 0, 1);
    private static final IntegerProperty THREE_PARTS = IntegerProperty.create("part", 0, 2);
    private static final IntegerProperty FOUR_PARTS = IntegerProperty.create("part", 0, 3);
    public static final MapCodec<DungeonDecorationBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DungeonDecorationType.CODEC.fieldOf("decoration").forGetter(block -> block.type),
            propertiesCodec()).apply(instance, DungeonDecorationBlock::create));
    private final DungeonDecorationType type;

    private DungeonDecorationBlock(DungeonDecorationType type, Properties properties) {
        super(properties);
        this.type = type;
        BlockState state = stateDefinition.any().setValue(FACING, Direction.NORTH);
        if (partProperty() != null) state = state.setValue(partProperty(), 0);
        // Old saves omit this property and must retain their original occupied cells.
        if (hasAlignmentProperty()) state = state.setValue(ALIGNED, false);
        registerDefaultState(state);
    }

    public static DungeonDecorationBlock create(DungeonDecorationType type, Properties properties) {
        return switch (type.partCount()) {
            case 2 -> type.hasAlignment() ? new AlignedTwoPart(type, properties) : new TwoPart(type, properties);
            case 3 -> new ThreePart(type, properties);
            case 4 -> type.hasAlignment() ? new AlignedFourPart(type, properties) : new FourPart(type, properties);
            default -> new DungeonDecorationBlock(type, properties);
        };
    }

    public DungeonDecorationType decorationType() { return type; }
    protected @Nullable IntegerProperty partProperty() { return null; }
    protected boolean hasAlignmentProperty() { return false; }
    public boolean aligned(BlockState state) { return state.hasProperty(ALIGNED) && state.getValue(ALIGNED); }
    public int partCount(BlockState state) { return type.partCount(aligned(state)); }
    public int part(BlockState state) { return partProperty() == null ? 0 : state.getValue(partProperty()); }
    public BlockState withPart(BlockState state, int part) {
        return partProperty() == null ? state : state.setValue(partProperty(), part);
    }

    public BlockState placementState(Direction facing) {
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return hasAlignmentProperty() ? state.setValue(ALIGNED, true) : state;
    }

    public BlockPos partPos(BlockPos origin, BlockState state, int part) {
        Direction facing = state.getValue(FACING);
        BlockPos offset = type.offset(part, aligned(state));
        return origin.relative(facing.getClockWise(), offset.getX())
                .relative(facing.getOpposite(), offset.getZ()).above(offset.getY());
    }

    public BlockPos origin(BlockState state, BlockPos pos) {
        return pos.subtract(partPos(BlockPos.ZERO, state, part(state)));
    }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        if (partProperty() != null) builder.add(partProperty());
        if (hasAlignmentProperty()) builder.add(ALIGNED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction supportFace = type.hanging() ? Direction.DOWN : Direction.UP;
        if (context.getClickedFace() != supportFace) return null;
        BlockState state = placementState(context.getHorizontalDirection().getOpposite());
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        if (!canSurvive(state, level, origin)) return null;
        for (int part = 0; part < partCount(state); part++) {
            BlockPos pos = partPos(origin, state, part);
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.getBlockState(pos).canBeReplaced(context)
                    || !level.isUnobstructed(withPart(state, part), pos, CollisionContext.empty())) return null;
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            for (int part = 1; part < partCount(state); part++) {
                level.setBlock(partPos(pos, state, part), withPart(state, part), UPDATE_ALL);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos origin = origin(state, pos);
        Direction direction = type.hanging() ? Direction.UP : Direction.DOWN;
        BlockPos support = origin.relative(direction);
        BlockState supportState = level.getBlockState(support);
        return supportState.isFaceSturdy(level, support, direction.getOpposite())
                || type.hanging() && supportState.is(BlockTags.CHAINS)
                && supportState.hasProperty(BlockStateProperties.AXIS)
                && supportState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        // Only the anchor reacts to its support; the removal callback clears the other parts without loot.
        if (part(state) == 0 && direction == (type.hanging() ? Direction.UP : Direction.DOWN)
                && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos origin = origin(state, pos);
        Direction facing = state.getValue(FACING);
        for (int part = 0; part < partCount(state); part++) {
            BlockPos other = partPos(origin, state, part);
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this) && otherState.getValue(FACING) == facing
                    && aligned(otherState) == aligned(state) && part(otherState) == part) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), UPDATE_ALL | UPDATE_SUPPRESS_DROPS);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return type.shape(part(state), state.getValue(FACING), aligned(state));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathType) { return false; }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = rotate(state, mirror.getRotation(state.getValue(FACING)));
        return mirror == Mirror.NONE ? mirrored : withPart(mirrored, type.mirroredPart(part(state), aligned(state)));
    }

    // Separate definitions avoid unreachable part states on smaller props.
    private static class TwoPart extends DungeonDecorationBlock {
        private TwoPart(DungeonDecorationType type, Properties properties) { super(type, properties); }
        @Override protected IntegerProperty partProperty() { return TWO_PARTS; }
    }
    private static final class ThreePart extends DungeonDecorationBlock {
        private ThreePart(DungeonDecorationType type, Properties properties) { super(type, properties); }
        @Override protected IntegerProperty partProperty() { return THREE_PARTS; }
    }
    private static class FourPart extends DungeonDecorationBlock {
        private FourPart(DungeonDecorationType type, Properties properties) { super(type, properties); }
        @Override protected IntegerProperty partProperty() { return FOUR_PARTS; }
    }
    private static final class AlignedTwoPart extends TwoPart {
        private AlignedTwoPart(DungeonDecorationType type, Properties properties) { super(type, properties); }
        @Override protected boolean hasAlignmentProperty() { return true; }
    }
    private static final class AlignedFourPart extends FourPart {
        private AlignedFourPart(DungeonDecorationType type, Properties properties) { super(type, properties); }
        @Override protected boolean hasAlignmentProperty() { return true; }
    }
}
