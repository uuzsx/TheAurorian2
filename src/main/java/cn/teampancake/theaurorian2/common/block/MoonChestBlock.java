package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.MoonChestBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

/** Single treasure chest. Only the server can consume a key and unlock it. */
public final class MoonChestBlock extends ChestBlock {
    public static final BooleanProperty UNLOCKED = BooleanProperty.create("unlocked");
    public static final BooleanProperty UNLOCKING = BooleanProperty.create("unlocking");
    // Transient viewer state; permanent unlock state is independent of the lid.
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape NORTH_SOUTH = box(1, 0, 3, 15, 10, 13);
    private static final VoxelShape EAST_WEST = box(3, 0, 1, 13, 10, 15);
    public enum Variant {
        TWO("treasure_chest_2", 12), THREE("moon_chest", 10), FOUR("treasure_chest_4", 13);
        public final String model;
        public final int unlockTicks;
        Variant(String model, int unlockTicks) { this.model = model; this.unlockTicks = unlockTicks; }
        public Item key() {
            return switch (this) {
                case TWO -> ModItems.TREASURE_CHEST_2_KEY.get();
                case THREE -> ModItems.MOON_CHEST_KEY.get();
                case FOUR -> ModItems.TREASURE_CHEST_4_KEY.get();
            };
        }
    }
    private final Variant variant;

    public MoonChestBlock(Properties properties) {
        this(properties, Variant.THREE);
    }

    public MoonChestBlock(Properties properties, Variant variant) {
        super(ModBlockEntities.MOON_CHEST::get, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
        this.variant = variant;
        registerDefaultState(defaultBlockState().setValue(UNLOCKED, false).setValue(UNLOCKING, false).setValue(OPEN, false));
    }

    public Variant variant() { return variant; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UNLOCKED, UNLOCKING, OPEN);
    }

    @Override
    public boolean chestCanConnectTo(BlockState state) { return false; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST : NORTH_SOUTH;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoonChestBlockEntity(pos, state);
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(
            BlockState state, Level level, BlockPos pos, boolean ignoreBeingBlocked) {
        // Also prevents vanilla hoppers from bypassing the key requirement.
        return state.getValue(UNLOCKED) && !state.getValue(UNLOCKING) ? super.combine(state, level, pos, ignoreBeingBlocked)
                : DoubleBlockCombiner.Combiner::acceptNone;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(UNLOCKED) && !state.getValue(UNLOCKING) && stack.is(variant.key())) {
            if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof MoonChestBlockEntity chest
                    && chest.beginUnlock()) {
                // Testing in creative follows the same one-key contract as survival.
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!state.getValue(UNLOCKED) || state.getValue(UNLOCKING)) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(
                    Component.translatable(state.getValue(UNLOCKING)
                            ? "message.theaurorian2.chest_unlocking" : "message.theaurorian2.moon_chest_locked"), true);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(UNLOCKING) && level.getBlockEntity(pos) instanceof MoonChestBlockEntity chest)
            chest.finishUnlock();
        super.tick(level.getBlockState(pos), level, pos, random);
    }
}
