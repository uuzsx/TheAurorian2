package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianChestBlockEntity extends ChestBlockEntity {

    public AurorianChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURORIAN_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.theaurorian2.aurorian_chest");
    }
}
