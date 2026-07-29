package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AurorianFurnaceBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianFurnaceBlock extends FurnaceBlock {

    public AurorianFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.AURORIAN_FURNACE.get(),
                (tickLevel, pos, tickState, furnace) -> AurorianFurnaceBlockEntity.serverTick(
                        (ServerLevel) tickLevel, pos, tickState, furnace));
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof AurorianFurnaceBlockEntity furnace) {
            player.openMenu(furnace);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }
}
