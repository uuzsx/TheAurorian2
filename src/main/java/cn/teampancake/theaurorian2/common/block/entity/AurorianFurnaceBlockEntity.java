package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianFurnaceBlockEntity extends AbstractFurnaceBlockEntity {

    private ItemStack lastSyncedInput = ItemStack.EMPTY;

    public AurorianFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURORIAN_FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.theaurorian2.aurorian_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FurnaceMenu(containerId, inventory, this, this.dataAccess);
    }

    public ItemStack getDisplayedInput() {
        return getItem(SLOT_INPUT);
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, AurorianFurnaceBlockEntity furnace) {
        AbstractFurnaceBlockEntity.serverTick(level, pos, state, furnace);
        furnace.syncDisplayedInput();
    }

    private void syncDisplayedInput() {
        ItemStack input = getDisplayedInput();
        if (ItemStack.isSameItemSameComponents(input, this.lastSyncedInput)) {
            return;
        }

        this.lastSyncedInput = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
