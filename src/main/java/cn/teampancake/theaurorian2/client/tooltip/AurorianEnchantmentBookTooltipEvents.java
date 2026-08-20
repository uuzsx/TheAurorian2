package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.AurorianEnchantmentBookTooltip;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentTooltips;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomTooltip;
import cn.teampancake.theaurorian2.common.inventory.AccessoryEffects;
import cn.teampancake.theaurorian2.common.inventory.AccessoryEnhancements;
import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.inventory.AccessorySlot;
import cn.teampancake.theaurorian2.common.inventory.AccessoryTooltip;
import cn.teampancake.theaurorian2.common.registry.ModAccessoryItems;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import cn.teampancake.theaurorian2.common.world.MoonShieldData;
import cn.teampancake.theaurorian2.common.world.MoonShieldSystem;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class AurorianEnchantmentBookTooltipEvents {

    private static final int TITLE_COLOR = 0x76B2C4;
    private static final int BODY_COLOR = 0x9EB8C8;
    private static final int DEVELOPER_BODY_COLOR = 0xAFC5D2;
    private static final int MOON_QUEEN_COLOR = 0xEA98FA;
    private static final int MAX_TEXT_WIDTH = 160;
    private static final int DEVELOPER_ITEM_MAX_TEXT_WIDTH = 172;
    private static final int SCREEN_MARGIN = 48;

    private AurorianEnchantmentBookTooltipEvents() {
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AurorianEnchantmentBookTooltip.class, ClientAurorianEnchantmentBookTooltip::new);
        event.register(PhantomBlossomTooltip.class, ClientPhantomBlossomTooltip::new);
        event.register(AccessoryTooltip.class, ClientAccessoryTooltip::new);
    }

    @SubscribeEvent
    public static void gatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModAccessoryItems.ARCANE_DAGGER.get())
                || stack.is(ModLegacyItems.TROPHY_MOON_QUEEN.get())
                || stack.is(ModLegacyItems.CRIMSON_PACT_PENDANT.get())
                || AccessoryEnhancements.isArtifact(stack)) {
            gatherAccessoryTooltip(event, stack);
            return;
        }
        boolean developerItem = stack.is(ModItems.PHANTOM_BLOSSOM_REQUIEM.get());
        if (developerItem) {
            gatherDeveloperItemTooltip(event, stack);
            return;
        }
        if (!EnchantmentTooltips.hasAurorianEnchantment(stack)) {
            return;
        }

        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (elements.isEmpty()) {
            return;
        }

        int titleIndex = elements.size() > 1 && elements.get(1).left().isPresent() ? 1 : 0;
        Component title = elements.get(titleIndex).left()
                .map(AurorianEnchantmentBookTooltipEvents::asComponent)
                .orElseGet(stack::getHoverName)
                .copy()
                .withStyle(style -> style.withColor(TITLE_COLOR));
        List<Component> body = new ArrayList<>();
        for (int i = titleIndex + 1; i < elements.size(); i++) {
            elements.get(i).left().ifPresent(text -> {
                var component = asComponent(text).copy();
                body.add(component.withStyle(style -> style.withColor(BODY_COLOR)));
            });
        }

        int maxTextWidth = Math.min(MAX_TEXT_WIDTH, Math.max(72, event.getScreenWidth() - SCREEN_MARGIN));
        elements.clear();
        elements.add(Either.right(new AurorianEnchantmentBookTooltip(title, body, maxTextWidth)));
        event.setMaxWidth(maxTextWidth);
    }

    @SubscribeEvent
    public static void selectTooltipTexture(RenderTooltipEvent.Texture event) {
        if (event.getItemStack().is(ModItems.PHANTOM_BLOSSOM_REQUIEM.get())
                || event.getItemStack().is(ModAccessoryItems.ARCANE_DAGGER.get())
                || event.getItemStack().is(ModLegacyItems.TROPHY_MOON_QUEEN.get())
                || event.getItemStack().is(ModLegacyItems.CRIMSON_PACT_PENDANT.get())
                || AccessoryEnhancements.isArtifact(event.getItemStack())
                || EnchantmentTooltips.hasAurorianEnchantment(event.getItemStack())) {
            event.setTexture(TheAurorian2.id("aurorian_enchantment_book"));
        }
    }

    private static void gatherAccessoryTooltip(RenderTooltipEvent.GatherComponents event, ItemStack stack) {
        int maxTextWidth = Math.min(176, Math.max(112, event.getScreenWidth() - SCREEN_MARGIN));
        List<Component> body;
        if (stack.is(ModAccessoryItems.ARCANE_DAGGER.get())) {
            int level = hoveredEnhancementLevel(stack);
            int percent = AccessoryEffects.attackSpeedPercent(level);
            Component value = Component.literal(percent + "%")
                    .withStyle(style -> style.withColor(0x75F28B));
            body = List.of(
                    Component.translatable(
                                    "item.theaurorian2.arcane_dagger.tooltip.attack_speed", value)
                            .withStyle(style -> style.withColor(0xD2DCE3)),
                    enhancementLine(level, AccessoryEffects.ARCANE_DAGGER_MAX_LEVEL, 0x75F28B),
                    rarityLine("accessory.theaurorian2.rarity.rare", 0x75F28B),
                    Component.translatable("item.theaurorian2.arcane_dagger.tooltip.flavor")
                            .withStyle(style -> style.withColor(0xAFC5D2).withItalic(true)));
        } else if (stack.is(ModLegacyItems.TROPHY_MOON_QUEEN.get())) {
            body = moonQueenTrophyTooltip(hoveredEnhancementLevel(stack));
        } else if (stack.is(ModLegacyItems.CRIMSON_PACT_PENDANT.get())) {
            body = crimsonPactPendantTooltip(hoveredEnhancementLevel(stack));
        } else if (stack.is(ModAccessoryItems.SEALED_ARTIFACT_ADVANCE.get())) {
            body = List.of(
                    Component.translatable("item.theaurorian2.sealed_artifact_advance.tooltip.effect")
                            .withStyle(style -> style.withColor(0xD2DCE3)),
                    Component.translatable("item.theaurorian2.sealed_artifact_advance.tooltip.level")
                            .withStyle(style -> style.withColor(0x69AFFF)),
                    rotatableLine(),
                    rarityLine("accessory.theaurorian2.rarity.uncommon", 0x69AFFF),
                    Component.translatable("item.theaurorian2.sealed_artifact_advance.tooltip.flavor")
                            .withStyle(style -> style.withColor(0xAFC5D2).withItalic(true)));
        } else if (stack.is(ModAccessoryItems.SEALED_ARTIFACT_CHOICE.get())) {
            body = artifactTooltip("sealed_artifact_choice");
        } else {
            body = artifactTooltip("sealed_artifact_desire");
        }

        event.getTooltipElements().clear();
        event.getTooltipElements().add(Either.right(new AccessoryTooltip(
                stack.getHoverName(), body, maxTextWidth)));
        event.setMaxWidth(maxTextWidth);
    }

    private static List<Component> artifactTooltip(String artifactId) {
        String key = "item.theaurorian2." + artifactId + ".tooltip.";
        return List.of(
                Component.translatable(key + "effect")
                        .withStyle(style -> style.withColor(0xD2DCE3)),
                Component.translatable(key + "level")
                        .withStyle(style -> style.withColor(0xB86BFF)),
                rotatableLine(),
                rarityLine("accessory.theaurorian2.rarity.epic", 0xB86BFF),
                Component.translatable(key + "flavor")
                        .withStyle(style -> style.withColor(0xAFC5D2).withItalic(true)));
    }

    private static List<Component> moonQueenTrophyTooltip(int enhancementLevel) {
        int level = AccessoryEffects.effectiveMoonQueenLevel(enhancementLevel);
        Component percent = mythicValue(AccessoryEffects.moonQueenPercent(level) + "%");
        Component maxHealth = mythicValue(formatHealth(AccessoryEffects.moonQueenMaxHealthBonus(level)));
        String key = "item.theaurorian2.trophy_moon_queen.tooltip.";
        return List.of(
                statLine(key + "critical_chance", percent),
                statLine(key + "melee_damage", percent),
                statLine(key + "movement_speed", percent),
                statLine(key + "max_health", maxHealth),
                statLine(key + "damage_reduction", percent),
                enhancementLine(
                        enhancementLevel, AccessoryEffects.MOON_QUEEN_TROPHY_MAX_LEVEL, MOON_QUEEN_COLOR),
                Component.translatable("accessory.theaurorian2.tooltip.unique")
                        .withStyle(style -> style.withColor(0xFFD25F).withBold(true)),
                rarityLine("accessory.theaurorian2.rarity.mythic", MOON_QUEEN_COLOR),
                Component.translatable(key + "flavor")
                        .withStyle(style -> style.withColor(0xAFC5D2).withItalic(true)));
    }

    private static List<Component> crimsonPactPendantTooltip(int enhancementLevel) {
        int level = AccessoryEffects.effectiveCrimsonPactLevel(enhancementLevel);
        Component maxShield = legendaryValue(formatHealth(MoonShieldData.maxCrimsonShield(level)));
        Component recovery = legendaryValue(formatHealth(MoonShieldSystem.crimsonRecovery(level)));
        String key = "item.theaurorian2.crimson_pact_pendant.tooltip.";
        return List.of(
                Component.translatable(key + "effect")
                        .withStyle(style -> style.withColor(0xD2DCE3)),
                Component.translatable(key + "kill_reward")
                        .withStyle(style -> style.withColor(0xD2DCE3)),
                Component.translatable(key + "recovery", recovery)
                        .withStyle(style -> style.withColor(0xD2DCE3)),
                Component.translatable(key + "max_shield", maxShield)
                        .withStyle(style -> style.withColor(0xD2DCE3)),
                enhancementLine(
                        enhancementLevel,
                        AccessoryEffects.CRIMSON_PACT_PENDANT_MAX_LEVEL,
                        0xFFD25F),
                Component.translatable("accessory.theaurorian2.tooltip.unique")
                        .withStyle(style -> style.withColor(0xFFD25F).withBold(true)),
                rarityLine("accessory.theaurorian2.rarity.legendary", 0xFFD25F),
                Component.translatable(key + "flavor")
                        .withStyle(style -> style.withColor(0xAFC5D2).withItalic(true)));
    }

    private static Component statLine(String key, Component value) {
        return Component.translatable(key, value)
                .withStyle(style -> style.withColor(0xD2DCE3));
    }

    private static Component mythicValue(String value) {
        return Component.literal(value).withStyle(style -> style.withColor(MOON_QUEEN_COLOR));
    }

    private static Component legendaryValue(String value) {
        return Component.literal(value).withStyle(style -> style.withColor(0xFFD25F));
    }

    private static Component rotatableLine() {
        return Component.translatable("accessory.theaurorian2.tooltip.rotatable")
                .withStyle(style -> style.withColor(0x69AFFF).withBold(true));
    }

    private static Component enhancementLine(int level, int maxLevel, int rarityColor) {
        int currentLevel = Math.max(0, level);
        int currentColor = currentLevel > maxLevel ? 0xFFD25F : rarityColor;
        Component current = Component.literal(Integer.toString(currentLevel))
                .withStyle(style -> style.withColor(currentColor));
        Component maximum = Component.literal(Integer.toString(maxLevel))
                .withStyle(style -> style.withColor(rarityColor));
        return Component.translatable(
                        "accessory.theaurorian2.tooltip.current_enhancement", current, maximum)
                .withStyle(style -> style.withColor(0xD2DCE3));
    }

    private static String formatHealth(double value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
    }

    private static Component rarityLine(String rarityKey, int rarityColor) {
        Component rarity = Component.translatable(rarityKey)
                .withStyle(style -> style.withColor(rarityColor));
        return Component.translatable("accessory.theaurorian2.tooltip.rarity", rarity)
                .withStyle(style -> style.withColor(0xD2DCE3));
    }

    private static int hoveredEnhancementLevel(ItemStack stack) {
        if (!(Minecraft.getInstance().screen instanceof InventoryScreen screen)) {
            return 0;
        }
        if (!(screen.getHoveredSlot() instanceof AccessorySlot slot)
                || !ItemStack.isSameItemSameComponents(slot.getItem(), stack)
                || !(slot.container instanceof AccessoryInventory inventory)) {
            return 0;
        }
        int[] levels = AccessoryEnhancements.calculate(inventory);
        return Math.max(0, levels[slot.getContainerSlot()]);
    }

    private static Component asComponent(FormattedText text) {
        return text instanceof Component component ? component.copy() : Component.literal(text.getString());
    }

    private static void gatherDeveloperItemTooltip(RenderTooltipEvent.GatherComponents event, ItemStack stack) {
        int maxTextWidth = Math.min(
                DEVELOPER_ITEM_MAX_TEXT_WIDTH,
                Math.max(104, event.getScreenWidth() - SCREEN_MARGIN));
        Component eight = gold("8");
        Component halfSecond = gold("0.5");
        Component oneAndHalfSeconds = gold("1.5");
        Component twelve = gold("12");
        Component eighteen = gold("18");
        Component five = gold("5");
        Component seven = gold("7");

        PhantomBlossomTooltip tooltip = new PhantomBlossomTooltip(
                stack.getHoverName(),
                Component.translatable("item.theaurorian2.phantom_blossom_requiem.tooltip.sendoff_title")
                        .withStyle(style -> style.withColor(0x55E4EC)),
                body("item.theaurorian2.phantom_blossom_requiem.tooltip.sendoff_stats", eight, halfSecond),
                Component.translatable("item.theaurorian2.phantom_blossom_requiem.tooltip.bloom_title")
                        .withStyle(style -> style.withColor(0xE78BDD)),
                body("item.theaurorian2.phantom_blossom_requiem.tooltip.bloom_charge", oneAndHalfSeconds, twelve),
                body("item.theaurorian2.phantom_blossom_requiem.tooltip.bloom_hit", eighteen, five),
                body("item.theaurorian2.phantom_blossom_requiem.tooltip.bloom_mark", seven),
                Component.translatable("item.theaurorian2.phantom_blossom_requiem.developer")
                        .withStyle(style -> style.withColor(0xE3B341)),
                Component.translatable("item.theaurorian2.phantom_blossom_requiem.quote")
                        .withStyle(style -> style.withColor(0xB4B0BD).withItalic(true)),
                maxTextWidth);

        event.getTooltipElements().clear();
        event.getTooltipElements().add(Either.right(tooltip));
        event.setMaxWidth(maxTextWidth);
    }

    private static Component body(String key, Object... values) {
        return Component.translatable(key, values)
                .withStyle(style -> style.withColor(DEVELOPER_BODY_COLOR));
    }

    private static Component gold(String value) {
        return Component.literal(value).withStyle(style -> style.withColor(0xF2C14E));
    }
}
