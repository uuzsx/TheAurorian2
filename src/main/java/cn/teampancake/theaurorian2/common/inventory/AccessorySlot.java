package cn.teampancake.theaurorian2.common.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AccessorySlot extends Slot {

    private boolean active;

    public AccessorySlot(AccessoryInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return AccessoryInventory.isAccessory(stack);
    }

    @Override
    public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
