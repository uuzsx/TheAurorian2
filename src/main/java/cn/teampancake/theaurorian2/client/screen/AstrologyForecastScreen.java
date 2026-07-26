package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.world.AurorianBlessingCycle;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class AstrologyForecastScreen extends Screen {

    private static final Identifier PAGE_TEXTURE =
            TheAurorian2.id("textures/gui/star_signs.png");
    private static final int PAGE_WIDTH = 142;
    private static final int PAGE_HEIGHT = 188;
    private static final int PAGE_TEXTURE_WIDTH = 512;
    private static final int PAGE_TEXTURE_HEIGHT = 188;
    private static final int EMBLEM_U = 426;
    private static final int EMBLEM_WIDTH = 58;
    private static final int EMBLEM_HEIGHT = 89;
    private static final int CORNER_WIDTH = 36;
    private static final int CORNER_HEIGHT = 28;
    private static final int EDGE_INSET = 4;

    private final List<AurorianBlessingCycle.Blessing> forecast;
    private int currentPage;

    public AstrologyForecastScreen(List<AurorianBlessingCycle.Blessing> forecast) {
        super(Component.translatable("gui.theaurorian2.astrology_forecast"));
        if (forecast.size() != 3) {
            throw new IllegalArgumentException("Astrology forecast requires exactly three days");
        }

        this.forecast = List.copyOf(forecast);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = this.pageLeft();
        int top = this.pageTop();
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_TEXTURE,
                left,
                top,
                this.currentPage * PAGE_WIDTH,
                0.0F,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                PAGE_TEXTURE_WIDTH,
                PAGE_TEXTURE_HEIGHT);

        Identifier blessingTexture = TheAurorian2.id(
                "textures/gui/bless/" + this.forecast.get(this.currentPage).textureName() + ".png");
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                blessingTexture,
                left,
                top,
                0.0F,
                0.0F,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                PAGE_WIDTH,
                PAGE_HEIGHT);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_TEXTURE,
                left + PAGE_WIDTH - EMBLEM_WIDTH + 7,
                top + 6,
                EMBLEM_U,
                0.0F,
                EMBLEM_WIDTH,
                EMBLEM_HEIGHT,
                PAGE_TEXTURE_WIDTH,
                PAGE_TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.isInsidePage(event.x(), event.y())) {
            int left = this.pageLeft();
            int top = this.pageTop();
            int pageRight = left + PAGE_WIDTH;
            int pageBottom = top + PAGE_HEIGHT;
            boolean previousCorner = event.x() >= left + EDGE_INSET
                    && event.x() <= left + EDGE_INSET + CORNER_WIDTH
                    && event.y() >= pageBottom - CORNER_HEIGHT - EDGE_INSET
                    && event.y() <= pageBottom - EDGE_INSET;
            boolean nextCorner = event.x() >= pageRight - CORNER_WIDTH - EDGE_INSET
                    && event.x() <= pageRight - EDGE_INSET
                    && event.y() >= pageBottom - CORNER_HEIGHT - EDGE_INSET
                    && event.y() <= pageBottom - EDGE_INSET;

            if (nextCorner && this.currentPage < this.forecast.size() - 1) {
                this.currentPage++;
                return true;
            }

            if (previousCorner && this.currentPage > 0) {
                this.currentPage--;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 262 && this.currentPage < this.forecast.size() - 1) {
            this.currentPage++;
            return true;
        }

        if (event.key() == 263 && this.currentPage > 0) {
            this.currentPage--;
            return true;
        }

        return super.keyPressed(event);
    }

    private boolean isInsidePage(double mouseX, double mouseY) {
        return mouseX >= this.pageLeft()
                && mouseX <= this.pageLeft() + PAGE_WIDTH
                && mouseY >= this.pageTop()
                && mouseY <= this.pageTop() + PAGE_HEIGHT;
    }

    private int pageLeft() {
        return (this.width - PAGE_WIDTH) / 2;
    }

    private int pageTop() {
        return (this.height - PAGE_HEIGHT) / 2;
    }
}
