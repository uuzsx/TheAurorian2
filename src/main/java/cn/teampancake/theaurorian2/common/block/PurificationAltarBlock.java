package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.world.MoonShieldSystem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The one-by-one altar model, including its two-block-high collision footprint. */
public final class PurificationAltarBlock extends BaseEntityBlock {

    public static final MapCodec<PurificationAltarBlock> CODEC = simpleCodec(PurificationAltarBlock::new);
    public static final BooleanProperty RITUAL_ACTIVE = BooleanProperty.create("ritual_active");
    public static final IntegerProperty SHIELD_COUNT = IntegerProperty.create("shield_count", 0, 4);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 24.0, 15.0);

    public PurificationAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(RITUAL_ACTIVE, false)
                .setValue(SHIELD_COUNT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PurificationAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return createTickerHelper(
                    blockEntityType,
                    ModBlockEntities.PURIFICATION_ALTAR.get(),
                    PurificationAltarBlockEntity::bookAnimationTick);
        }
        if (level instanceof ServerLevel) {
            return createTickerHelper(
                    blockEntityType,
                    ModBlockEntities.PURIFICATION_ALTAR.get(),
                    PurificationAltarBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (MoonShieldSystem.isPurified(serverPlayer)) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.theaurorian2.purification.already_completed"));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.getBlockEntity(pos) instanceof PurificationAltarBlockEntity altar) {
            altar.requestRitual(serverPlayer);
        }
        return InteractionResult.SUCCESS_SERVER;
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
        BlockPos pos = context.getClickedPos();
        LevelReader level = context.getLevel();
        BlockPos upperPos = pos.above();
        if (!canSurviveAt(level, pos)
                || !level.getBlockState(upperPos).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState();
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

    private static boolean canSurviveAt(LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).is(ModBlocks.PURIFICATION_ALTAR_BASE.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RITUAL_ACTIVE, SHIELD_COUNT);
    }
}
