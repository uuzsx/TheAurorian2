package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.AurorianEnchantmentBookTooltip;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentTooltips;
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
    private static final int MAX_TEXT_WIDTH = 160;
    private static final int SCREEN_MARGIN = 48;

    private AurorianEnchantmentBookTooltipEvents() {
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AurorianEnchantmentBookTooltip.class, ClientAurorianEnchantmentBookTooltip::new);
    }

    @SubscribeEvent
    public static void gatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
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
            elements.get(i).left().ifPresent(text -> body.add(asComponent(text)
                    .copy()
                    .withStyle(style -> style.withColor(BODY_COLOR))));
        }

        int maxTextWidth = Math.min(MAX_TEXT_WIDTH, Math.max(72, event.getScreenWidth() - SCREEN_MARGIN));
        elements.clear();
        elements.add(Either.right(new AurorianEnchantmentBookTooltip(title, body, maxTextWidth)));
        event.setMaxWidth(maxTextWidth);
    }

    @SubscribeEvent
    public static void selectTooltipTexture(RenderTooltipEvent.Texture event) {
        if (EnchantmentTooltips.hasAurorianEnchantment(event.getItemStack())) {
            event.setTexture(TheAurorian2.id("aurorian_enchantment_book"));
        }
    }

    private static Component asComponent(FormattedText text) {
        return text instanceof Component component ? component.copy() : Component.literal(text.getString());
    }
}
