package cn.teampancake.theaurorian2.common.enchantment;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record AurorianEnchantmentBookTooltip(
        Component title,
        List<Component> body,
        int maxTextWidth) implements TooltipComponent {

    public AurorianEnchantmentBookTooltip {
        title = title.copy();
        body = List.copyOf(body);
    }
}
