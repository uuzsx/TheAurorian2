package cn.teampancake.theaurorian2.client.tooltip;

import cn.teampancake.theaurorian2.common.enchantment.AurorianEnchantmentBookTooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class ClientAurorianEnchantmentBookTooltip implements ClientTooltipComponent {

    private static final int MIN_TEXT_WIDTH = 72;
    private static final int BODY_TOP = 14;
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_COLOR = 0xFF76B2C4;
    private static final int BODY_COLOR = 0xFF9EB8C8;
    private final Component title;
    private final List<Component> body;
    private final int maxTextWidth;
    private Font layoutFont;
    private List<FormattedCharSequence> wrappedBody = List.of();
    private int width;

    public ClientAurorianEnchantmentBookTooltip(AurorianEnchantmentBookTooltip tooltip) {
        this.title = tooltip.title();
        this.body = tooltip.body();
        this.maxTextWidth = tooltip.maxTextWidth();
    }

    @Override
    public int getHeight(Font font) {
        ensureLayout(font);
        return BODY_TOP + this.wrappedBody.size() * LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        ensureLayout(font);
        return this.width;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        ensureLayout(font);
        graphics.centeredText(font, this.title, x + this.width / 2, y, TITLE_COLOR);

        int lineY = y + BODY_TOP;
        for (FormattedCharSequence line : this.wrappedBody) {
            graphics.text(font, line, x, lineY, BODY_COLOR, true);
            lineY += LINE_HEIGHT;
        }
    }

    private void ensureLayout(Font font) {
        if (this.layoutFont == font) {
            return;
        }

        List<FormattedCharSequence> lines = new ArrayList<>();
        int measuredWidth = Math.max(MIN_TEXT_WIDTH, font.width(this.title));
        for (Component component : this.body) {
            List<FormattedCharSequence> splitLines = font.split(component, this.maxTextWidth);
            if (splitLines.isEmpty()) {
                splitLines = List.of(Component.empty().getVisualOrderText());
            }

            for (FormattedCharSequence line : splitLines) {
                lines.add(line);
                measuredWidth = Math.max(measuredWidth, font.width(line));
            }
        }

        this.layoutFont = font;
        this.wrappedBody = List.copyOf(lines);
        this.width = measuredWidth;
    }
}
