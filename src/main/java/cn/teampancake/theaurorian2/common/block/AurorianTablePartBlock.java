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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianTablePartBlock extends Block {

    public static final MapCodec<AurorianTablePartBlock> CODEC = simpleCodec(AurorianTablePartBlock::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 1, 3);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);

    public AurorianTablePartBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 1).setValue(VARIANT, 0));
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
        return AurorianTableBlock.collisionShape(state.getValue(PART));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AurorianTableBlock.collisionShape(state.getValue(PART));
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
        BlockPos masterPos = AurorianTableBlock.masterPos(pos, state.getValue(PART));
        BlockState masterState = level.getBlockState(masterPos);
        if (masterState.getBlock() instanceof AurorianTableBlock table) {
            return table.useItemOn(itemStack, masterState, level, masterPos, player, hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos masterPos = AurorianTableBlock.masterPos(pos, state.getValue(PART));
        BlockState masterState = level.getBlockState(masterPos);
        if (masterState.getBlock() instanceof AurorianTableBlock table) {
            return table.useWithoutItem(masterState, level, masterPos, player, hitResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos masterPos = AurorianTableBlock.masterPos(pos, state.getValue(PART));
            BlockState masterState = level.getBlockState(masterPos);
            if (masterState.getBlock() instanceof AurorianTableBlock table) {
                boolean dropTable = !player.preventsBlockDrops();
                if (!dropTable) {
                    table.dropDisplayedItem(level, masterPos);
                }
                level.destroyBlock(masterPos, dropTable, player);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos masterPos = AurorianTableBlock.masterPos(pos, state.getValue(PART));
        BlockState masterState = level.getBlockState(masterPos);
        if (masterState.getBlock() instanceof AurorianTableBlock) {
            level.destroyBlock(masterPos, true, null);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, VARIANT);
    }
}
