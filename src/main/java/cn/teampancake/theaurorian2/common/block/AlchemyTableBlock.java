package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AlchemyTableBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Both halves open the primary inventory; only the primary owns persistent data. */
public final class AlchemyTableBlock extends PairedFurnitureBlock implements EntityBlock {
    public static final MapCodec<AlchemyTableBlock> CODEC = simpleCodec(AlchemyTableBlock::new);
    public AlchemyTableBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected Direction extensionDirection(BlockState state) { return state.getValue(FACING).getClockWise(); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(SECOND) ? null : new AlchemyTableBlockEntity(pos, state);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos primary = state.getValue(SECOND) ? otherPos(state, pos) : pos;
        if (!level.isClientSide() && level.getBlockEntity(primary) instanceof AlchemyTableBlockEntity table) player.openMenu(table);
        return InteractionResult.SUCCESS;
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof AlchemyTableBlockEntity table) table.process(level);
    }
}
