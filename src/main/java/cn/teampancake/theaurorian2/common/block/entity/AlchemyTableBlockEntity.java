package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.crafting.*;
import cn.teampancake.theaurorian2.common.inventory.AlchemyTableMenu;
import cn.teampancake.theaurorian2.common.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class AlchemyTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] OUTPUT_SLOTS = {4, 3};
    private static final int[] INPUT_SLOTS = {0, 1, 2, 3};
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private List<ItemStack> snapshot = List.of();
    private AlchemyProcessing.Plan plan;
    private int progress;
    private boolean crafting;
    private int liquidLevel;
    private int liquidData;
    private int historyRevision;
    private final List<ItemStack> materials = new ArrayList<>();
    private final ContainerData data = new ContainerData() {
        public int get(int id) { return switch (id) {
            case 0 -> progress; case 1 -> plan == null ? 140 : plan.time();
            case 2 -> liquidLevel; case 3 -> liquidData; default -> 0;
        }; }
        public void set(int id, int value) { if (id == 0) progress = value; }
        public int getCount() { return 4; }
    };
    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ALCHEMY_TABLE.get(), pos, state); }
    @Override protected Component getDefaultName() { return Component.translatable("block.theaurorian2.alchemy_table"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new AlchemyTableMenu(id, inventory, this, data); }
    @Override public int getContainerSize() { return 5; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> value) { items = value; }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(5, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        liquidLevel = Math.clamp(input.getIntOr("LiquidLevel", 0), 0, 3);
        liquidData = liquidLevel == 0 ? 0 : input.getIntOr("LiquidData", 0) & 0x7FFF;
        materials.clear();
        if (liquidLevel > 0) input.read("AlchemyMaterials", ItemStack.CODEC.listOf(0, 128)).ifPresent(list ->
                list.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> materials.add(stack.copyWithCount(1))));
        historyRevision++;
        // Partial jobs restart after loading; ingredients are only charged on completion.
        progress = 0; plan = null; snapshot = List.of();
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output, items);
        output.putInt("LiquidLevel", liquidLevel); output.putInt("LiquidData", liquidData);
        output.store("AlchemyMaterials", ItemStack.CODEC.listOf(0, 128), materials);
    }

    public int historySize() { return materials.size(); }
    public int historyRevision() { return historyRevision; }
    public ItemStack historyItem(int index) { return materials.get(index).copy(); }

    public static ItemStack tankPotion(int bits) {
        ItemStack stack = new ItemStack(Items.POTION);
        if (bits == 0) stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        else {
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(),
                    Optional.of(AlchemyFormulas.getPotionColor(bits)), AlchemyFormulas.getPotionEffects(bits), Optional.empty()));
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable(
                    "item.theaurorian2.potion.effect." + AlchemyFormulas.getPotionPrefix(bits)).withStyle(style -> style.withItalic(false)));
        }
        return stack;
    }

    /** Called only by the matching, still-valid server menu; the client sends an action, never an item or recipe result. */
    public boolean interactTank(Player player, AlchemyTableMenu menu) {
        if (!(level instanceof ServerLevel) || player.level() != level || player.containerMenu != menu || !stillValid(player)) return false;
        ItemStack carried = menu.getCarried();
        if (carried.is(Items.WATER_BUCKET)) {
            if (liquidLevel == 3) return false;
            liquidData = liquidLevel == 0 ? 0 : AlchemyFormulas.applyIngredient(liquidData, "-1-3-5-7-9-11-13");
            liquidLevel = 3;
            if (liquidData == 0) { materials.clear(); historyRevision++; }
            exchangeCursor(player, menu, new ItemStack(Items.BUCKET));
        } else {
            if (liquidLevel == 0) return false;
            if (carried.is(Items.GLASS_BOTTLE)) {
                exchangeCursor(player, menu, tankPotion(liquidData));
                if (--liquidLevel == 0) { liquidData = 0; materials.clear(); historyRevision++; }
            } else {
                var ingredient = carried.typeHolder().getData(AlchemyFormulaData.INGREDIENTS);
                int next = carried.is(Items.NETHER_WART) ? AlchemyFormulas.applyNetherWart(liquidData)
                        : ingredient == null ? liquidData : AlchemyFormulas.applyIngredient(liquidData, ingredient.formula());
                if (next == liquidData) return false;
                liquidData = next;
                if (materials.size() == 128) materials.removeFirst();
                materials.add(carried.copyWithCount(1)); historyRevision++;
                var remainder = carried.getCraftingRemainder();
                if (!player.hasInfiniteMaterials()) carried.shrink(1);
                // Some ingredients (e.g. dragon breath) have a reusable container.
                if (remainder != null && !player.hasInfiniteMaterials()) giveOrDrop(player, remainder.create());
            }
        }
        super.setChanged();
        return true;
    }

    private static void exchangeCursor(Player player, AlchemyTableMenu menu, ItemStack result) {
        ItemStack carried = menu.getCarried();
        if (player.hasInfiniteMaterials()) { giveOrDrop(player, result); return; }
        carried.shrink(1);
        if (carried.isEmpty()) menu.setCarried(result); else giveOrDrop(player, result);
    }
    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
    @Override public void onLoad() { super.onLoad(); wake(); }
    @Override public void setChanged() { super.setChanged(); if (!crafting) wake(); }
    private void wake() { if (level instanceof ServerLevel server && !isRemoved()) server.scheduleTick(worldPosition, getBlockState().getBlock(), 1); }
    private boolean sameInputs() {
        if (snapshot.size() != 4) return false;
        for (int i = 0; i < 4; i++) if (!ItemStack.matches(snapshot.get(i), items.get(i))) return false;
        return true;
    }
    public void process(ServerLevel server) {
        // Re-evaluate on each inventory change; while active only four bounded comparisons are needed.
        if (!sameInputs() || plan == null) {
            snapshot = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) snapshot.add(items.get(i).copy());
            progress = 0;
            var input = new AlchemyRecipe.Input(snapshot);
            var fixed = server.recipeAccess().getRecipeFor(ModAlchemy.RECIPE_TYPE.get(), input, server);
            plan = fixed.map(holder -> new AlchemyProcessing.Plan(holder.value().assemble(input), new int[]{1, 1, 1, 1}, false, holder.value().time()))
                    .orElseGet(() -> AlchemyProcessing.prepare(snapshot));
        }
        if (plan == null) return;
        ItemStack output = plan.output(), current = items.get(4);
        if (!current.isEmpty() && (!ItemStack.isSameItemSameComponents(current, output)
                || current.getCount() + output.getCount() > Math.min(getMaxStackSize(), output.getMaxStackSize()))) { progress = 0; return; }
        if (++progress < plan.time()) { wake(); return; }
        crafting = true;
        try {
            for (int i = 0; i < 4; i++) {
                int used = plan.consumption()[i];
                if (used == 0) continue;
                var template = items.get(i).getCraftingRemainder();
                ItemStack remainder = i == 3 ? (plan.bottle() ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY)
                        : plan.bottle() || template == null ? ItemStack.EMPTY : template.create();
                items.get(i).shrink(used);
                if (!remainder.isEmpty()) {
                    if (items.get(i).isEmpty()) items.set(i, remainder);
                    else Containers.dropItemStack(server, worldPosition.getX() + 0.5, worldPosition.getY() + 1,
                            worldPosition.getZ() + 0.5, remainder);
                }
            }
            if (current.isEmpty()) items.set(4, output.copy()); else current.grow(output.getCount());
            progress = 0; plan = null; snapshot = List.of();
            super.setChanged();
        } finally { crafting = false; }
        wake();
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot < 3 || slot == 3 && stack.is(Items.POTION); }
    @Override public int[] getSlotsForFace(Direction face) { return face == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) { return canPlaceItem(slot, stack) && !stack.is(Items.GLASS_BOTTLE); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) { return slot == 4 || slot == 3 && stack.is(Items.GLASS_BOTTLE); }
}
