package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** The centered master block for the three-by-three purification altar base. */
public final class PurificationAltarBaseBlock extends Block {

    public static final MapCodec<PurificationAltarBaseBlock> CODEC =
            simpleCodec(PurificationAltarBaseBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final BlockPos[] PART_OFFSETS = {
        new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1),
        new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
        new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1)
    };

    public PurificationAltarBaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(
            net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        for (BlockPos offset : PART_OFFSETS) {
            level.setBlock(
                    pos.offset(offset),
                    ModBlocks.PURIFICATION_ALTAR_BASE_PART.get().defaultBlockState()
                            .setValue(PurificationAltarBasePartBlock.OFFSET_X, offset.getX() + 1)
                            .setValue(PurificationAltarBasePartBlock.OFFSET_Z, offset.getZ() + 1),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, net.minecraft.server.level.ServerLevel level,
            BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        for (BlockPos offset : PART_OFFSETS) {
            BlockPos partPos = pos.offset(offset);
            if (level.getBlockState(partPos).is(ModBlocks.PURIFICATION_ALTAR_BASE_PART.get())) {
                level.setBlock(partPos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos center = context.getClickedPos();
        LevelReader level = context.getLevel();
        return canSurviveAt(level, center) && hasClearFootprint(level, center, context)
                ? this.defaultBlockState()
                : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSurviveAt(level, pos);
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
        if (directionToNeighbour == Direction.DOWN && !canSurviveAt(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(
                state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    private static boolean canSurviveAt(LevelReader level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos supportPos = center.offset(x, -1, z);
                if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasClearFootprint(
            LevelReader level, BlockPos center, BlockPlaceContext context) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = center.offset(x, 0, z);
                if (!target.equals(center) && !level.getBlockState(target).canBeReplaced(context)) {
                    return false;
                }
            }
        }
        return true;
    }
}
