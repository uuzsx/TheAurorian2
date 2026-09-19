package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.crafting.AlchemyFormulas;
import cn.teampancake.theaurorian2.common.inventory.AlchemyTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {
    private static final List<Component> HELP = List.of(text("help"), text("water"), text("ingredient"), text("bottle"));
    private List<FormattedCharSequence> helpLines = List.of(), previewLines = List.of();
    private List<Component> tooltip = List.of();
    private ItemStack lastPreview;
    private Component amount = Component.empty();
    private int lastLevel = -1;
    private int lastHistoryCount = -1;
    private boolean scrolling;
    private int lastScrollRequest = -1;
    private static final net.minecraft.resources.Identifier TEXTURE = TheAurorian2.id("textures/gui/alchemy_table.png");
    private static final net.minecraft.resources.Identifier WATER = TheAurorian2.id("liquid/water");
    private static final net.minecraft.resources.Identifier SCROLL = TheAurorian2.id("alchemy_table/scrollbar_vertical");
    private static final Component MORE = text("more");
    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 350, 174);
    }
    private static Component text(String key) { return Component.translatable("gui.theaurorian2.alchemy." + key); }
    @Override protected void init() {
        super.init();
        helpLines = new ArrayList<>();
        for (Component line : HELP) helpLines.addAll(font.split(line, 83));
        lastPreview = null; lastLevel = -1; updatePreview();
    }
    @Override protected void containerTick() { super.containerTick(); updatePreview(); }
    private void updatePreview() {
        if (lastHistoryCount != menu.historyCount()) { lastHistoryCount = menu.historyCount(); lastScrollRequest = -1; }
        if (lastPreview != menu.preview()) {
            lastPreview = menu.preview();
            tooltip = lastPreview.isEmpty() ? List.of(text("empty")) : getTooltipFromContainerItem(lastPreview);
            previewLines = new ArrayList<>();
            for (Component line : tooltip) {
                if (line.getString().isEmpty()) break;
                previewLines.addAll(font.split(line, 83));
            }
        }
        if (lastLevel != menu.liquidLevel()) {
            lastLevel = menu.liquidLevel();
            amount = Component.translatable("gui.theaurorian2.alchemy.amount", lastLevel);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {}
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        var texture = TEXTURE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, 261, imageHeight, 512, 256);
        int height = Math.clamp(33 * menu.progress() / menu.duration(), 0, 33);
        if (height > 0) graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 147, topPos + 72 - height, 350, 33 - height, 30, height, 512, 256);
        int liquidHeight = menu.liquidLevel() * 33;
        int color = 0xFF000000 | (menu.liquidData() == 0 ? 0x3F76E4 : AlchemyFormulas.getPotionColor(menu.liquidData()));
        if (liquidHeight > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WATER, leftPos + 218, topPos + 132 - liquidHeight, 12, liquidHeight, color);
            graphics.item(menu.preview(), leftPos + 216, topPos + 144);
        }
        if (menu.historyCount() > 9) {
            int y = topPos + 10 + 139 * menu.historyOffset() / (menu.historyCount() - 9);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL, leftPos + 3, y, 8, 24);
        }
        graphics.fill(leftPos + 263, topPos + 2, leftPos + imageWidth, topPos + 173, 0xDC182032);
        int y = topPos + 7;
        for (var line : helpLines) { graphics.text(font, line, leftPos + 267, y, 0xFFD7DCEE, false); y += 10; }
        graphics.text(font, amount, leftPos + 267, y + 5, 0xFFA9D8EA, false);
        y += 20;
        for (var line : previewLines) {
            if (y > topPos + 145) break;
            graphics.text(font, line, leftPos + 267, y, 0xFFE4E8F8, false); y += 10;
        }
        graphics.text(font, MORE, leftPos + 267, topPos + 162, 0xFF9CAFC9, false);
    }
    private boolean overTank(double x, double y) { return x >= leftPos + 214 && x < leftPos + 258 && y >= topPos + 20 && y < topPos + 166; }
    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        if (overTank(x, y) || x >= leftPos + 266 && x < leftPos + imageWidth && y >= topPos && y < topPos + imageHeight)
            graphics.setTooltipForNextFrame(font, tooltip, menu.preview().getTooltipImage(), menu.preview(), x, y);
        else super.extractTooltip(graphics, x, y);
    }
    private void send(int button) {
        if (minecraft.gameMode == null || button != 0 && button == lastScrollRequest) return;
        if (button != 0) lastScrollRequest = button;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && overTank(event.x(), event.y())) { send(0); return true; }
        if (event.button() == 0 && event.x() >= leftPos && event.x() < leftPos + 13
                && event.y() >= topPos + 10 && event.y() < topPos + 173 && menu.historyCount() > 9) {
            scrolling = true; scrollTo(event.y()); return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    private void scrollTo(double y) {
        int max = Math.max(0, menu.historyCount() - 9);
        send(1 + Math.clamp((int) Math.round((y - topPos - 22) / 139.0 * max), 0, max));
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (scrolling) { scrollTo(event.y()); return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { scrolling = false; return super.mouseReleased(event); }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (x >= leftPos && x < leftPos + 36 && y >= topPos && y < topPos + imageHeight && menu.historyCount() > 9) {
            send(1 + Math.clamp(menu.historyOffset() - (int) Math.signum(sy), 0, menu.historyCount() - 9)); return true;
        }
        return super.mouseScrolled(x, y, sx, sy);
    }
}
