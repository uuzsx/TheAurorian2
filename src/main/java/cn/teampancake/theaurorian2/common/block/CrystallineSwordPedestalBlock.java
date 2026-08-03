package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.CrystallineSwordPedestalBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class CrystallineSwordPedestalBlock extends HorizontalEntityBlock {

    public static final MapCodec<CrystallineSwordPedestalBlock> CODEC =
            simpleCodec(CrystallineSwordPedestalBlock::new);
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    private static final int UNSEAL_TICKS = 10;
    private static final int SEAL_TICKS = 12;

    public CrystallineSwordPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(PHASE, Phase.SEALED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystallineSwordPedestalBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && state.getValue(PHASE).isTransition()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PHASE);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        Phase phase = state.getValue(PHASE);
        if (phase == Phase.SEALED) {
            return unseal(state, level, pos, player);
        }
        if (phase == Phase.UNSEALED) {
            return takeSword(state, level, pos, player);
        }
        if (phase == Phase.EMPTY && stack.is(ModLegacyItems.CRYSTALLINE_SWORD.get())) {
            return seal(stack, state, level, pos, player);
        }
        return phase.isTransition() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Phase phase = state.getValue(PHASE);
        if (phase == Phase.SEALED) {
            return unseal(state, level, pos, player);
        }
        if (phase == Phase.UNSEALED) {
            return takeSword(state, level, pos, player);
        }
        return phase.isTransition() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        if (phase == Phase.UNSEALING) {
            finishTransition(level, pos, state, Phase.UNSEALED);
        } else if (phase == Phase.SEALING) {
            finishTransition(level, pos, state, Phase.SEALED);
        }
    }

    private InteractionResult unseal(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            beginTransition(serverLevel, pos, state, Phase.UNSEALING, UNSEAL_TICKS, player);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult takeSword(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack sword = new ItemStack(ModLegacyItems.CRYSTALLINE_SWORD.get());
            if (!player.addItem(sword)) {
                player.drop(sword, false);
            }
            finishTransition(serverLevel, pos, state, Phase.EMPTY);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult seal(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            stack.consume(1, player);
            beginTransition(serverLevel, pos, state, Phase.SEALING, SEAL_TICKS, player);
        }
        return InteractionResult.SUCCESS;
    }

    private void beginTransition(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            Phase phase,
            int duration,
            Player player) {
        BlockState transitionState = state.setValue(PHASE, phase);
        level.setBlock(pos, transitionState, Block.UPDATE_ALL);
        level.scheduleTick(pos, this, duration);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, transitionState));
    }

    private static void finishTransition(ServerLevel level, BlockPos pos, BlockState state, Phase phase) {
        BlockState finishedState = state.setValue(PHASE, phase);
        level.setBlock(pos, finishedState, Block.UPDATE_ALL);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(finishedState));
    }

    public enum Phase implements StringRepresentable {
        SEALED("sealed"),
        UNSEALING("unsealing"),
        UNSEALED("unsealed"),
        EMPTY("empty"),
        SEALING("sealing");

        private final String serializedName;

        Phase(String serializedName) {
            this.serializedName = serializedName;
        }

        public boolean isTransition() {
            return this == UNSEALING || this == SEALING;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
