package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.inventory.AccessoryMenuExtension;
import cn.teampancake.theaurorian2.common.inventory.AccessorySlot;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu implements AccessoryMenuExtension {

    @Unique
    private final List<AccessorySlot> theaurorian2$accessorySlots = new ArrayList<>();
    @Unique
    private AccessoryInventory theaurorian2$accessoryInventory;

    protected InventoryMenuMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void theaurorian2$addAccessorySlots(
            Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        AccessoryInventory accessories = owner.getData(ModAttachments.ACCESSORY_INVENTORY);
        this.theaurorian2$accessoryInventory = accessories;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 6; column++) {
                AccessorySlot slot = new AccessorySlot(
                        accessories, column + row * 6, 191 + column * 18, 12 + row * 18);
                slot.setActive(false);
                this.theaurorian2$accessorySlots.add(slot);
                this.addSlot(slot);
            }
        }
    }

    @Override
    public List<AccessorySlot> theaurorian2$getAccessorySlots() {
        return this.theaurorian2$accessorySlots;
    }

    @Override
    public AccessoryInventory theaurorian2$getAccessoryInventory() {
        return this.theaurorian2$accessoryInventory;
    }

    @Override
    public void theaurorian2$setAccessoriesOpen(boolean open) {
        this.theaurorian2$accessorySlots.forEach(slot -> slot.setActive(open));
    }
}
