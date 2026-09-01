package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class DarkStoneGateKeyholeBlock extends LockedStructureBlock {
    public static final MapCodec<DarkStoneGateKeyholeBlock> CODEC = simpleCodec(DarkStoneGateKeyholeBlock::new);
    private static final int BREAK_DELAY_TICKS = 8;
    private static final int SPREAD_DELAY_TICKS = 2;
    private static final int MAX_CONNECTED_BLOCKS = 256;
    private static final int MAX_SEARCH_DISTANCE = 16;

    public DarkStoneGateKeyholeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
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
        if (!stack.is(ModLegacyItems.DARK_STONE_KEY.get())) {
            return InteractionResult.PASS;
        }
        if (state.getValue(UNLOCKED)) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(UNLOCKED, true), Block.UPDATE_ALL);
            stack.consume(1, player);
            SpiderMotherSpawnerBlock.activateNearest((ServerLevel) level, pos);
            scheduleGateRemoval((ServerLevel) level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this) && state.getValue(UNLOCKED)) {
            level.destroyBlock(pos, false);
        }
    }

    private static void scheduleGateRemoval(ServerLevel level, BlockPos origin) {
        ArrayDeque<GateSearchNode> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        int connectedBlocks = 0;
        pending.add(new GateSearchNode(origin.immutable(), 0));

        while (!pending.isEmpty() && connectedBlocks < MAX_CONNECTED_BLOCKS) {
            GateSearchNode node = pending.removeFirst();
            BlockPos pos = node.pos();
            if (!visited.add(pos) || origin.distManhattan(pos) > MAX_SEARCH_DISTANCE || !level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!isDarkStoneGate(state)) {
                continue;
            }
            connectedBlocks++;
            level.scheduleTick(pos, state.getBlock(), BREAK_DELAY_TICKS + node.distance() * SPREAD_DELAY_TICKS);
            for (Direction direction : Direction.values()) {
                pending.addLast(new GateSearchNode(pos.relative(direction).immutable(), node.distance() + 1));
            }
        }
    }

    private static boolean isDarkStoneGate(BlockState state) {
        return state.is(ModStructureBlocks.DARK_STONE_GATE.get())
                || state.is(ModStructureBlocks.DARK_STONE_GATE_KEYHOLE.get());
    }

    private record GateSearchNode(BlockPos pos, int distance) {
    }
}
