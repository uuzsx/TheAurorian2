package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class AurorianChestBlock extends ChestBlock {

    public AurorianChestBlock(Properties properties) {
        super(
                ModBlockEntities.SILENT_WOOD_CHEST::get,
                SoundEvents.CHEST_OPEN,
                SoundEvents.CHEST_CLOSE,
                properties);
    }

    @Override
    public boolean chestCanConnectTo(BlockState state) {
        return false;
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(
            BlockState state, Level level, BlockPos pos, boolean ignoreBeingBlocked) {
        // Old saves may still contain LEFT/RIGHT until their scheduled migration.
        return super.combine(state.setValue(TYPE, ChestType.SINGLE), level, pos, ignoreBeingBlocked);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TYPE) != ChestType.SINGLE) {
            // Changing only the state retains this half's inventory, name and loot table.
            state = state.setValue(TYPE, ChestType.SINGLE);
            level.setBlock(pos, state, UPDATE_ALL);
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianChestBlockEntity(pos, state);
    }
}
