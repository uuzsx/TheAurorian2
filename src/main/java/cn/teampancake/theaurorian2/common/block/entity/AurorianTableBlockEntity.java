package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AurorianTableBlockEntity extends BlockEntity {

    private ItemStack displayedItem = ItemStack.EMPTY;
    private int displayRotation;

    public AurorianTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURORIAN_TABLE.get(), pos, state);
    }

    public ItemStack getDisplayedItem() {
        return this.displayedItem;
    }

    public int getDisplayRotation() {
        return this.displayRotation;
    }

    public void setDisplayedItem(ItemStack itemStack) {
        this.displayedItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copyWithCount(1);
        this.displayRotation = 0;
        sync();
    }

    public void rotateDisplayedItem() {
        if (!this.displayedItem.isEmpty()) {
            this.displayRotation = (this.displayRotation + 1) % 8;
            sync();
        }
    }

    public ItemStack takeDisplayedItem() {
        ItemStack result = this.displayedItem;
        this.displayedItem = ItemStack.EMPTY;
        this.displayRotation = 0;
        sync();
        return result;
    }

    private void sync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.displayedItem.isEmpty()) {
            output.store("displayed_item", ItemStack.CODEC, this.displayedItem);
            output.putByte("display_rotation", (byte) this.displayRotation);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.displayedItem = input.read("displayed_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.displayRotation = Math.floorMod(input.getByteOr("display_rotation", (byte) 0), 8);
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
