package cn.teampancake.theaurorian2.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PhantomBlossomTooltip(
        Component title,
        Component sendoffTitle,
        Component sendoffStats,
        Component bloomTitle,
        Component bloomCharge,
        Component bloomHit,
        Component bloomMark,
        Component developer,
        Component quote,
        int maxTextWidth) implements TooltipComponent {

    public PhantomBlossomTooltip {
        title = title.copy();
        sendoffTitle = sendoffTitle.copy();
        sendoffStats = sendoffStats.copy();
        bloomTitle = bloomTitle.copy();
        bloomCharge = bloomCharge.copy();
        bloomHit = bloomHit.copy();
        bloomMark = bloomMark.copy();
        developer = developer.copy();
        quote = quote.copy();
    }
}
