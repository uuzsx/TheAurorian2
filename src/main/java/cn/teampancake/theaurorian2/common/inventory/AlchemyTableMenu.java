package cn.teampancake.theaurorian2.common.inventory;

import cn.teampancake.theaurorian2.common.registry.ModAlchemy;
import cn.teampancake.theaurorian2.common.block.entity.AlchemyTableBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AlchemyTableMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final SimpleContainer history = new SimpleContainer(9);
    private final ContainerData historyData = new SimpleContainerData(2);
    private int lastHistoryRevision = -1;
    private ItemStack preview = ItemStack.EMPTY;
    public AlchemyTableMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(5), new SimpleContainerData(4));
    }
    public AlchemyTableMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModAlchemy.MENU.get(), id);
        checkContainerSize(container, 5); checkContainerDataCount(data, 4);
        this.container = container; this.data = data;
        addSlot(new Slot(container, 0, 58, 21));
        addSlot(new Slot(container, 1, 82, 21));
        addSlot(new Slot(container, 2, 107, 21));
        addSlot(new Slot(container, 3, 175, 21) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.POTION); }
        });
        addSlot(new Slot(container, 4, 127, 47) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 46 + col * 18, 89 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 46 + col * 18, 147));
        for (int row = 0; row < 9; row++) addSlot(new Slot(history, row, 15, 7 + row * 18) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
            @Override public boolean isFake() { return true; }
        });
        addDataSlots(data);
        addDataSlots(historyData);
        refreshHistory();
    }
    public int liquidLevel() { return data.get(2); }
    public int liquidData() { return data.get(3); }
    public int historyOffset() { return historyData.get(0); }
    public int historyCount() { return historyData.get(1); }
    public ItemStack preview() { return preview; }
    @Override public void setData(int id, int value) {
        super.setData(id, value);
        if (id == 2 || id == 3) preview = liquidLevel() == 0 ? ItemStack.EMPTY : AlchemyTableBlockEntity.tankPotion(liquidData());
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (player.containerMenu != this || !stillValid(player) || !(container instanceof AlchemyTableBlockEntity table)) return false;
        if (button == 0) {
            if (!table.interactTank(player, this)) return false;
        } else if (button >= 1 && button <= 120) {
            historyData.set(0, Math.clamp(button - 1, 0, Math.max(0, table.historySize() - 9)));
            lastHistoryRevision = -1;
        } else return false;
        broadcastChanges();
        return true;
    }
    private void refreshHistory() {
        if (!(container instanceof AlchemyTableBlockEntity table) || lastHistoryRevision == table.historyRevision()) return;
        lastHistoryRevision = table.historyRevision();
        int start = Math.clamp(historyOffset(), 0, Math.max(0, table.historySize() - 9));
        historyData.set(0, start); historyData.set(1, table.historySize());
        for (int row = 0; row < 9; row++) history.setItem(row, start + row < table.historySize() ? table.historyItem(start + row) : ItemStack.EMPTY);
    }
    @Override public void broadcastChanges() { refreshHistory(); super.broadcastChanges(); }
    @Override public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (slot >= 41) return;
        super.clicked(slot, button, input, player);
    }
    public int progress() { return data.get(0); }
    public int duration() { return Math.max(1, data.get(1)); }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= 41) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        boolean moved;
        if (index < 5) moved = moveItemStackTo(stack, 5, 41, true);
        else if (stack.is(Items.POTION)) {
            moved = moveItemStackTo(stack, 3, 4, false);
            if (!moved) moved = moveItemStackTo(stack, 0, 3, false);
        } else moved = moveItemStackTo(stack, 0, 3, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
