package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible upper interaction and collision cell for the altar model. */
public final class PurificationAltarUpperBlock extends Block {

    public static final MapCodec<PurificationAltarUpperBlock> CODEC =
            simpleCodec(PurificationAltarUpperBlock::new);
    public static final BooleanProperty RITUAL_ACTIVE = BooleanProperty.create("ritual_active");
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);

    public PurificationAltarUpperBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RITUAL_ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        BlockPos altarPos = pos.below();
        BlockState altarState = level.getBlockState(altarPos);
        if (altarState.getBlock() instanceof PurificationAltarBlock altar) {
            return altar.useWithoutItem(altarState, level, altarPos, player, hitResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos altarPos = pos.below();
        BlockState altarState = level.getBlockState(altarPos);
        if (altarState.getBlock() instanceof PurificationAltarBlock altar) {
            return altar.useWithoutItem(altarState, level, altarPos, player, hitResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // The visible lower altar owns the destruction particles.
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos altarPos = pos.below();
            if (level.getBlockState(altarPos).is(ModBlocks.PURIFICATION_ALTAR.get())) {
                level.destroyBlock(altarPos, !player.getAbilities().instabuild, player);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos altarPos = pos.below();
        if (level.getBlockState(altarPos).is(ModBlocks.PURIFICATION_ALTAR.get())) {
            level.destroyBlock(altarPos, true, null);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RITUAL_ACTIVE);
    }
}
