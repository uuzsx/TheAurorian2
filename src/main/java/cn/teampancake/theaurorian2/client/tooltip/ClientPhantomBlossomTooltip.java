package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.common.item.PhantomBlossomTooltip;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class ClientPhantomBlossomTooltip implements ClientTooltipComponent {

    private static final int MIN_TEXT_WIDTH = 136;
    private static final int TITLE_SECTION_HEIGHT = 15;
    private static final int LINE_HEIGHT = 10;
    private static final int DETAIL_INDENT = 5;
    private static final int GROUP_GAP = 4;
    private static final int FOOTER_GAP = 6;
    private static final int QUOTE_GAP = 3;
    private static final int TITLE_COLOR = 0xFF76B2C4;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private final PhantomBlossomTooltip tooltip;
    private Font layoutFont;
    private List<FormattedCharSequence> sendoffStats = List.of();
    private List<FormattedCharSequence> bloomCharge = List.of();
    private List<FormattedCharSequence> bloomHit = List.of();
    private List<FormattedCharSequence> bloomMark = List.of();
    private List<FormattedCharSequence> quote = List.of();
    private int width;

    public ClientPhantomBlossomTooltip(PhantomBlossomTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight(Font font) {
        ensureLayout(font);
        return TITLE_SECTION_HEIGHT
                + LINE_HEIGHT + this.sendoffStats.size() * LINE_HEIGHT
                + GROUP_GAP
                + LINE_HEIGHT
                + (this.bloomCharge.size() + this.bloomHit.size() + this.bloomMark.size()) * LINE_HEIGHT
                + FOOTER_GAP + LINE_HEIGHT
                + QUOTE_GAP + this.quote.size() * LINE_HEIGHT;
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

        int lineY = y + TITLE_SECTION_HEIGHT;
        drawLeft(graphics, font, this.tooltip.sendoffTitle().getVisualOrderText(), x, lineY);
        lineY += LINE_HEIGHT;
        lineY = drawDetails(graphics, font, this.sendoffStats, x, lineY);

        lineY += GROUP_GAP;
        drawLeft(graphics, font, this.tooltip.bloomTitle().getVisualOrderText(), x, lineY);
        lineY += LINE_HEIGHT;
        lineY = drawDetails(graphics, font, this.bloomCharge, x, lineY);
        lineY = drawDetails(graphics, font, this.bloomHit, x, lineY);
        lineY = drawDetails(graphics, font, this.bloomMark, x, lineY);

        lineY += FOOTER_GAP;
        drawCentered(graphics, font, this.tooltip.developer().getVisualOrderText(), x, lineY);
        lineY += LINE_HEIGHT + QUOTE_GAP;
        for (FormattedCharSequence line : this.quote) {
            drawCentered(graphics, font, line, x, lineY);
            lineY += LINE_HEIGHT;
        }
    }

    private int drawDetails(
            GuiGraphicsExtractor graphics,
            Font font,
            List<FormattedCharSequence> lines,
            int x,
            int y) {
        for (FormattedCharSequence line : lines) {
            drawLeft(graphics, font, line, x + DETAIL_INDENT, y);
            y += LINE_HEIGHT;
        }
        return y;
    }

    private void drawLeft(
            GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y) {
        graphics.text(font, text, x, y, DEFAULT_COLOR, true);
    }

    private void drawCentered(
            GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y) {
        int lineX = x + (this.width - font.width(text)) / 2;
        graphics.text(font, text, lineX, y, DEFAULT_COLOR, true);
    }

    private void ensureLayout(Font font) {
        if (this.layoutFont == font) {
            return;
        }

        int detailWidth = Math.max(1, this.tooltip.maxTextWidth() - DETAIL_INDENT);
        this.sendoffStats = split(font, this.tooltip.sendoffStats(), detailWidth);
        this.bloomCharge = split(font, this.tooltip.bloomCharge(), detailWidth);
        this.bloomHit = split(font, this.tooltip.bloomHit(), detailWidth);
        this.bloomMark = split(font, this.tooltip.bloomMark(), detailWidth);
        this.quote = split(font, this.tooltip.quote(), this.tooltip.maxTextWidth());

        int measuredWidth = Math.max(font.width(this.tooltip.title()), font.width(this.tooltip.sendoffTitle()));
        measuredWidth = Math.max(measuredWidth, font.width(this.tooltip.bloomTitle()));
        measuredWidth = Math.max(measuredWidth, font.width(this.tooltip.developer()));
        measuredWidth = Math.max(measuredWidth, maxWidth(font, this.sendoffStats) + DETAIL_INDENT);
        measuredWidth = Math.max(measuredWidth, maxWidth(font, this.bloomCharge) + DETAIL_INDENT);
        measuredWidth = Math.max(measuredWidth, maxWidth(font, this.bloomHit) + DETAIL_INDENT);
        measuredWidth = Math.max(measuredWidth, maxWidth(font, this.bloomMark) + DETAIL_INDENT);
        measuredWidth = Math.max(measuredWidth, maxWidth(font, this.quote));

        this.layoutFont = font;
        this.width = Math.min(this.tooltip.maxTextWidth(), Math.max(MIN_TEXT_WIDTH, measuredWidth));
    }

    private static List<FormattedCharSequence> split(Font font, Component text, int maxWidth) {
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        return lines.isEmpty() ? List.of(Component.empty().getVisualOrderText()) : List.copyOf(lines);
    }

    private static int maxWidth(Font font, List<FormattedCharSequence> lines) {
        return lines.stream().mapToInt(font::width).max().orElse(0);
    }
}
