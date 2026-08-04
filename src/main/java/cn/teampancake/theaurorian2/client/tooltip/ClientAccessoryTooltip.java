package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.common.inventory.AccessoryTooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

public final class ClientAccessoryTooltip implements ClientTooltipComponent {

    private static final int MIN_TEXT_WIDTH = 112;
    private static final int BODY_TOP = 15;
    private static final int LINE_HEIGHT = 10;
    private static final int BODY_GAP = 2;
    private static final int TITLE_COLOR = 0xFF76B2C4;

    private final AccessoryTooltip tooltip;
    private Font layoutFont;
    private List<FormattedCharSequence> lines = List.of();
    private int width;

    public ClientAccessoryTooltip(AccessoryTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight(Font font) {
        ensureLayout(font);
        return BODY_TOP + this.lines.size() * LINE_HEIGHT
                + Math.max(0, this.tooltip.body().size() - 1) * BODY_GAP;
    }

    @Override
    public int getWidth(Font font) {
        ensureLayout(font);
        return this.width;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        ensureLayout(font);
        graphics.centeredText(font, this.tooltip.title(), x + this.width / 2, y, TITLE_COLOR);

        int lineY = y + BODY_TOP;
        int lineIndex = 0;
        for (int componentIndex = 0; componentIndex < this.tooltip.body().size(); componentIndex++) {
            List<FormattedCharSequence> wrapped = split(font, this.tooltip.body().get(componentIndex));
            for (FormattedCharSequence line : wrapped) {
                graphics.text(font, line, x, lineY, 0xFFFFFFFF, true);
                lineY += LINE_HEIGHT;
                lineIndex++;
            }
            if (componentIndex + 1 < this.tooltip.body().size()) {
                lineY += BODY_GAP;
            }
        }
    }

    private void ensureLayout(Font font) {
        if (this.layoutFont == font) {
            return;
        }
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        int measuredWidth = Math.max(MIN_TEXT_WIDTH, font.width(this.tooltip.title()));
        for (var component : this.tooltip.body()) {
            for (FormattedCharSequence line : split(font, component)) {
                wrappedLines.add(line);
                measuredWidth = Math.max(measuredWidth, font.width(line));
            }
        }
        this.layoutFont = font;
        this.lines = List.copyOf(wrappedLines);
        this.width = Math.min(this.tooltip.maxTextWidth(), measuredWidth);
    }

    private List<FormattedCharSequence> split(Font font, net.minecraft.network.chat.Component component) {
        List<FormattedCharSequence> split = font.split(component, this.tooltip.maxTextWidth());
        return split.isEmpty() ? List.of(net.minecraft.network.chat.Component.empty().getVisualOrderText()) : split;
    }
}
