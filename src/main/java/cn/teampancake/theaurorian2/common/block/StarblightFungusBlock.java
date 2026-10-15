package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class StarblightFungusBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<StarblightFungusBlock> CODEC = simpleCodec(StarblightFungusBlock::new);
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    public static final IntegerProperty VARIANT = WallMushroomBlock.VARIANT;
    public static final TagKey<Block> SUPPORTS = TagKey.create(
            Registries.BLOCK, TheAurorian2.id("starblight_fungus_supports"));
    private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    private static final Map<Direction, VoxelShape> WALL_SHAPES =
            Shapes.rotateHorizontal(Block.box(0, 0, 8, 16, 16, 16));

    public StarblightFungusBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Existing saved states have only facing/variant: their default must remain wall-mounted.
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(VARIANT, 0).setValue(WALL, true));
    }

    @Override
    public MapCodec<StarblightFungusBlock> codec() {
        return CODEC;
    }

    public BlockState stateForAttachment(Direction direction, int variant) {
        return this.defaultBlockState().setValue(WALL, direction != Direction.UP)
                .setValue(FACING, direction == Direction.UP ? Direction.NORTH : direction)
                .setValue(VARIANT, variant);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(WALL) ? WALL_SHAPES.get(state.getValue(FACING)) : FLOOR_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(state.getValue(WALL) ? facing.getOpposite() : Direction.DOWN);
        BlockState support = level.getBlockState(supportPos);
        return support.is(SUPPORTS) || state.getValue(WALL) && support.isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) {
            return null;
        }
        BlockState state = this.stateForAttachment(face, variantAt(context.getClickedPos()));
        if (face == Direction.UP) {
            state = state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Direction support = state.getValue(WALL) ? state.getValue(FACING).getOpposite() : Direction.DOWN;
        return direction == support && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, WALL);
    }

    private static int variantAt(BlockPos pos) {
        long hash = pos.asLong();
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        return (int) Math.floorMod(hash, 5L);
    }
}
