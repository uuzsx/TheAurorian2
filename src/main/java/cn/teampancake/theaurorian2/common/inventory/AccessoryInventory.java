package cn.teampancake.theaurorian2.common.inventory;

import cn.teampancake.theaurorian2.common.registry.ModItemTags;
import java.util.stream.Stream;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class AccessoryInventory extends SimpleContainer implements ValueIOSerializable {

    public static final int SLOT_COUNT = 24;

    private final Player owner;
    private boolean loading;

    public AccessoryInventory(Player owner) {
        super(SLOT_COUNT);
        this.owner = owner;
    }

    public static boolean isAccessory(ItemStack stack) {
        return stack.is(ModItemTags.ACCESSORIES_AND_ARTIFACTS);
    }

    public Stream<ItemStack> equippedItems() {
        return this.getItems().stream().filter(stack -> !stack.isEmpty());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isAccessory(stack);
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> items =
                output.list("Items", ItemStackWithSlot.CODEC);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = this.getItem(slot);
            if (!stack.isEmpty()) {
                items.add(new ItemStackWithSlot(slot, stack));
            }
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        this.loading = true;
        try {
            this.clearContent();
            input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(entry -> {
                if (entry.isValidInContainer(SLOT_COUNT) && isAccessory(entry.stack())) {
                    this.setItem(entry.slot(), entry.stack());
                }
            });
        } finally {
            this.loading = false;
            this.setChanged();
        }
    }

    @Override
    public void setChanged() {
        if (!this.loading) {
            AccessoryEffects.reconcile(this.owner, this);
        }
    }
}
