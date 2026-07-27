package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianChestBlock extends ChestBlock {

    public AurorianChestBlock(Properties properties) {
        super(
                ModBlockEntities.AURORIAN_CHEST::get,
                SoundEvents.CHEST_OPEN,
                SoundEvents.CHEST_CLOSE,
                properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianChestBlockEntity(pos, state);
    }
}
