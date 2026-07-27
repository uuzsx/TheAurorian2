package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class EnchantmentTooltips {

    private static final int DESCRIPTION_COLOR = 0x9EB8C8;

    private EnchantmentTooltips() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        ItemEnchantments enchantments = stack.getOrDefault(
                DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int level = enchantments.getLevel(enchantment);
            enchantment.unwrapKey().ifPresent(key -> addDescription(event, key.identifier(), level));
        }
    }

    public static boolean hasAurorianEnchantment(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        ItemEnchantments enchantments = stack.getOrDefault(
                DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchantments.keySet().stream().anyMatch(enchantment -> enchantment.unwrapKey()
                .map(key -> key.identifier().getNamespace().equals(TheAurorian2.MOD_ID))
                .orElse(false));
    }

    private static void addDescription(ItemTooltipEvent event, Identifier enchantmentId, int level) {
        if (!enchantmentId.getNamespace().equals(TheAurorian2.MOD_ID)) {
            return;
        }

        String translationKey = "enchantment." + enchantmentId.getNamespace()
                + "." + enchantmentId.getPath() + ".desc";
        Component description = switch (enchantmentId.getPath()) {
            case "impale" -> Component.translatable(translationKey, highlightedNumber(level));
            case "overload", "night_walker" -> Component.translatable(
                    translationKey, highlightedNumber(halfPointValue(level)));
            case "soul_slash" -> Component.translatable(translationKey, highlightedNumber(level * 5));
            case "freeze_aspect" -> Component.translatable(
                    translationKey,
                    highlightedNumber(level * 4),
                    highlightedNumber(level * 4 - 1));
            default -> Component.translatable(translationKey);
        };
        event.getToolTip().add(description.copy().withStyle(style -> style.withColor(DESCRIPTION_COLOR)));
    }

    private static Component highlightedNumber(int value) {
        return highlightedNumber(Integer.toString(value));
    }

    private static Component highlightedNumber(String value) {
        return Component.literal(value).withStyle(ChatFormatting.GOLD);
    }

    private static String halfPointValue(int level) {
        return level % 2 == 0 ? Integer.toString(level / 2) : level / 2 + ".5";
    }
}
