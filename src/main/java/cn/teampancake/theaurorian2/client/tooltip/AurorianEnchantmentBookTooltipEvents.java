package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.AurorianEnchantmentBookTooltip;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentTooltips;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomTooltip;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
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
    private static final int MAX_TEXT_WIDTH = 160;
    private static final int DEVELOPER_ITEM_MAX_TEXT_WIDTH = 172;
    private static final int SCREEN_MARGIN = 48;

    private AurorianEnchantmentBookTooltipEvents() {
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AurorianEnchantmentBookTooltip.class, ClientAurorianEnchantmentBookTooltip::new);
        event.register(PhantomBlossomTooltip.class, ClientPhantomBlossomTooltip::new);
    }

    @SubscribeEvent
    public static void gatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
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
                || EnchantmentTooltips.hasAurorianEnchantment(event.getItemStack())) {
            event.setTexture(TheAurorian2.id("aurorian_enchantment_book"));
        }
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
