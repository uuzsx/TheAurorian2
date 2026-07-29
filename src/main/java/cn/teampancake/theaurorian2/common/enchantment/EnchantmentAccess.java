package cn.teampancake.theaurorian2.common.enchantment;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Safe registry access shared by future enchantment effects and event handlers. */
public final class EnchantmentAccess {

    private EnchantmentAccess() {
    }

    public static Optional<Holder.Reference<Enchantment>> find(
            RegistryAccess registryAccess, ResourceKey<Enchantment> key) {
        return registryAccess.get(key);
    }

    public static int itemLevel(
            ItemStack stack, RegistryAccess registryAccess, ResourceKey<Enchantment> key) {
        return find(registryAccess, key)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    public static int activeLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        return find(entity.registryAccess(), key)
                .map(enchantment -> EnchantmentHelper.getEnchantmentLevel(enchantment, entity))
                .orElse(0);
    }

    public static boolean has(ItemStack stack, RegistryAccess registryAccess, ResourceKey<Enchantment> key) {
        return itemLevel(stack, registryAccess, key) > 0;
    }

    public static boolean isActive(LivingEntity entity, ResourceKey<Enchantment> key) {
        return activeLevel(entity, key) > 0;
    }
}
