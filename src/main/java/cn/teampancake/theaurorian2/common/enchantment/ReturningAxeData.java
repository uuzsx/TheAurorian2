package cn.teampancake.theaurorian2.common.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The player's save owns the real axe; the projectile is only its temporary visual/attack. */
public final class ReturningAxeData {
    public ItemStack stack = ItemStack.EMPTY;
    public String flightId = "";
    public int slot;
    public int elapsed;

    public static ReturningAxeData read(ValueInput input) {
        ReturningAxeData data = new ReturningAxeData();
        data.stack = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        data.flightId = input.getStringOr("flight", "");
        data.slot = input.getIntOr("slot", 0);
        return data;
    }

    public boolean write(ValueOutput output) {
        if (stack.isEmpty()) return false;
        output.store("stack", ItemStack.CODEC, stack);
        output.putString("flight", flightId);
        output.putInt("slot", slot);
        return true;
    }
}
