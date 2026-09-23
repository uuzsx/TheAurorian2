package cn.teampancake.theaurorian2.common.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** A namespaced component field preserves every unrelated item component. */
public record TerraBinding(String dimension, BlockPos pos, String chestId) {
    private static final String KEY = "theaurorian2:terra_binding";

    public static TerraBinding read(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);
        String dimension = data.getStringOr("dimension", "");
        String id = data.getStringOr("chest", "");
        if (Identifier.tryParse(dimension) == null || id.isEmpty()) return null;
        return new TerraBinding(dimension, BlockPos.of(data.getLongOr("pos", 0)), id);
    }

    public void write(ItemStack stack) {
        CompoundTag binding = new CompoundTag();
        binding.putString("dimension", dimension);
        binding.putLong("pos", pos.asLong());
        binding.putString("chest", chestId);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.put(KEY, binding));
    }

    public static void clear(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.remove(KEY));
    }
}
