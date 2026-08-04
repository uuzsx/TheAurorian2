package cn.teampancake.theaurorian2.common.inventory;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record AccessoryTooltip(
        Component title,
        List<Component> body,
        int maxTextWidth) implements TooltipComponent {

    public AccessoryTooltip {
        title = title.copy();
        body = List.copyOf(body);
    }
}
