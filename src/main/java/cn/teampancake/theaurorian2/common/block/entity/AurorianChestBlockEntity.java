package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianChestBlockEntity extends ChestBlockEntity {

    public AurorianChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SILENT_WOOD_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().is(ModBlocks.SILENT_WOOD_CHEST.get())
                ? Component.translatable("container.theaurorian2.silent_wood_chest")
                : getBlockState().getBlock().getName();
    }
}
