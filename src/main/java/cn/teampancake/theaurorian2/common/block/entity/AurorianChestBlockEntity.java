package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class AurorianChestBlockEntity extends ChestBlockEntity {

    public AurorianChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SILENT_WOOD_CHEST.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            // One scheduled update per legacy half, never an always-running migration tick.
            server.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("displayed_item", ItemStack.CODEC).ifPresent(item -> {
            if (getItem(0).isEmpty()) setItem(0, item);
        });
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().is(ModBlocks.SILENT_WOOD_CHEST.get())
                ? Component.translatable("container.theaurorian2.silent_wood_chest")
                : getBlockState().getBlock().getName();
    }
}
