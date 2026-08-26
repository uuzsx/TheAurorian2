package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.network.PurificationRitualConfirmPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Final confirmation for the irreversible loss of the Aurorian blessings. */
public final class PurificationRitualScreen extends Screen {

    private static final Identifier PAGE_TEXTURE =
            TheAurorian2.id("textures/gui/star_signs.png");
    private static final int PAGE_WIDTH = 142;
    private static final int PAGE_HEIGHT = 188;
    private static final int PAGE_TEXTURE_WIDTH = 512;
    private static final int PAGE_TEXTURE_HEIGHT = 188;
    private static final int EMBLEM_U = 426;
    private static final int EMBLEM_WIDTH = 58;
    private static final int EMBLEM_HEIGHT = 89;
    private static final int TEXT_AREA_WIDTH = 128;
    private static final int TEXT_LINE_SPACING = 10;
    private static final int TEXT_COLOR = 0xFFA05D49;
    private static final float TITLE_SCALE = 1.0F;
    private static final int TITLE_Y_OFFSET = 22;
    private static final int TITLE_COLOR = 0xFF8D4B3D;
    private static final int FIRST_PAGE_TEXT_LINES = 10;
    private static final int FOLLOWING_PAGE_TEXT_LINES = 14;
    private static final int PAGE_EDGE_INSET = 4;
    private static final int PAGE_CORNER_WIDTH = 36;
    private static final int PAGE_CORNER_HEIGHT = 28;
    private static final int BUTTON_WIDTH = 66;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_GAP = 4;
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            TheAurorian2.id("widget/selena_button"),
            TheAurorian2.id("widget/selena_button_disabled"),
            TheAurorian2.id("widget/selena_button_highlighted"));
    private static final String[] POEM_LINE_KEYS = {
        "gui.theaurorian2.purification.poem.line1",
        "gui.theaurorian2.purification.poem.line2",
        "gui.theaurorian2.purification.poem.line3",
        "gui.theaurorian2.purification.poem.line4",
        "gui.theaurorian2.purification.poem.line5",
        "gui.theaurorian2.purification.poem.line6",
        "gui.theaurorian2.purification.poem.line7",
        "gui.theaurorian2.purification.poem.line8",
        "gui.theaurorian2.purification.poem.line9",
        "gui.theaurorian2.purification.poem.line10",
        "gui.theaurorian2.purification.poem.line11",
        "gui.theaurorian2.purification.poem.line12",
        "gui.theaurorian2.purification.poem.line13",
        "gui.theaurorian2.purification.poem.line14",
        "gui.theaurorian2.purification.poem.line15"
    };
    private final net.minecraft.core.BlockPos altarPos;
    private int currentPage;

    public PurificationRitualScreen(net.minecraft.core.BlockPos altarPos) {
        super(Component.translatable("gui.theaurorian2.purification.title"));
        this.altarPos = altarPos;
    }

    @Override
    protected void init() {
        int buttonY = Math.min(
                this.pageTop() + PAGE_HEIGHT + BUTTON_GAP,
                this.height - BUTTON_HEIGHT - BUTTON_GAP);
        int firstButtonX = this.width / 2 - BUTTON_WIDTH - BUTTON_GAP / 2;
        int secondButtonX = this.width / 2 + BUTTON_GAP / 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.theaurorian2.purification.confirm"),
                        ignored -> this.confirm())
                .bounds(firstButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build(SelenaButton::new));
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.theaurorian2.purification.cancel"),
                        ignored -> this.onClose())
                .bounds(secondButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build(SelenaButton::new));
    }

    private void confirm() {
        ClientPacketDistributor.sendToServer(new PurificationRitualConfirmPayload(this.altarPos));
        this.onClose();
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
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_TEXTURE,
                this.pageLeft(),
                this.pageTop(),
                this.currentPage * PAGE_WIDTH,
                0.0F,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                PAGE_TEXTURE_WIDTH,
                PAGE_TEXTURE_HEIGHT);
        if (this.currentPage == 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    PAGE_TEXTURE,
                    this.pageLeft() + PAGE_WIDTH - EMBLEM_WIDTH + 13,
                    this.pageTop() - 12,
                    EMBLEM_U,
                    0.0F,
                    EMBLEM_WIDTH,
                    EMBLEM_HEIGHT,
                    PAGE_TEXTURE_WIDTH,
                    PAGE_TEXTURE_HEIGHT);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (this.currentPage == 0) {
            Component title = Component.translatable("gui.theaurorian2.purification.poem.title");
            graphics.pose().pushMatrix();
            graphics.pose().translate(this.width / 2.0F, this.pageTop() + TITLE_Y_OFFSET);
            graphics.pose().scale(TITLE_SCALE, TITLE_SCALE);
            graphics.text(this.font, title, -this.font.width(title) / 2, 0, TITLE_COLOR, false);
            graphics.pose().popMatrix();
        }

        List<FormattedCharSequence> renderedLines = this.renderedLinesForCurrentPage();
        if (renderedLines.isEmpty()) {
            return;
        }

        int textHeight = this.font.lineHeight
                + (renderedLines.size() - 1) * TEXT_LINE_SPACING;
        int textY = this.currentPage == 0
                ? this.pageTop() + (PAGE_HEIGHT - textHeight) / 2 + 12
                : this.pageTop() + 16;
        for (FormattedCharSequence line : renderedLines) {
            int textX = this.width / 2 - this.font.width(line) / 2;
            graphics.text(this.font, line, textX, textY, TEXT_COLOR, false);
            textY += TEXT_LINE_SPACING;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.isInsidePage(event.x(), event.y())) {
            int left = this.pageLeft();
            int top = this.pageTop();
            int right = left + PAGE_WIDTH;
            int bottom = top + PAGE_HEIGHT;
            boolean previousCorner = event.x() >= left + PAGE_EDGE_INSET
                    && event.x() <= left + PAGE_EDGE_INSET + PAGE_CORNER_WIDTH
                    && event.y() >= bottom - PAGE_CORNER_HEIGHT - PAGE_EDGE_INSET
                    && event.y() <= bottom - PAGE_EDGE_INSET;
            boolean nextCorner = event.x() >= right - PAGE_CORNER_WIDTH - PAGE_EDGE_INSET
                    && event.x() <= right - PAGE_EDGE_INSET
                    && event.y() >= bottom - PAGE_CORNER_HEIGHT - PAGE_EDGE_INSET
                    && event.y() <= bottom - PAGE_EDGE_INSET;

            if (nextCorner && this.currentPage < this.pageCount() - 1) {
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
        if (event.key() == 262 && this.currentPage < this.pageCount() - 1) {
            this.currentPage++;
            return true;
        }
        if (event.key() == 263 && this.currentPage > 0) {
            this.currentPage--;
            return true;
        }
        return super.keyPressed(event);
    }

    private List<FormattedCharSequence> renderedLinesForCurrentPage() {
        List<FormattedCharSequence> allLines = new ArrayList<>();
        for (String key : POEM_LINE_KEYS) {
            allLines.addAll(this.font.split(Component.translatable(key), TEXT_AREA_WIDTH));
        }

        int pageCount = this.pageCount(allLines.size());
        this.currentPage = Math.max(0, Math.min(this.currentPage, pageCount - 1));
        int start = this.currentPage == 0
                ? 0
                : FIRST_PAGE_TEXT_LINES + (this.currentPage - 1) * FOLLOWING_PAGE_TEXT_LINES;
        int pageCapacity = this.currentPage == 0
                ? FIRST_PAGE_TEXT_LINES
                : FOLLOWING_PAGE_TEXT_LINES;
        int end = Math.min(start + pageCapacity, allLines.size());
        return allLines.subList(start, end);
    }

    private int pageCount() {
        int lineCount = 0;
        for (String key : POEM_LINE_KEYS) {
            lineCount += this.font.split(Component.translatable(key), TEXT_AREA_WIDTH).size();
        }
        return this.pageCount(lineCount);
    }

    private int pageCount(int lineCount) {
        if (lineCount <= FIRST_PAGE_TEXT_LINES) {
            return 1;
        }
        int remainingLines = lineCount - FIRST_PAGE_TEXT_LINES;
        return 1 + (remainingLines + FOLLOWING_PAGE_TEXT_LINES - 1) / FOLLOWING_PAGE_TEXT_LINES;
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

    private static final class SelenaButton extends Button {

        private SelenaButton(Builder builder) {
            super(builder);
        }

        @Override
        protected void extractContents(
                GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    BUTTON_SPRITES.get(this.isActive(), this.isHoveredOrFocused()),
                    this.getX(),
                    this.getY(),
                    this.getWidth(),
                    this.getHeight());
            this.extractDefaultLabel(
                    graphics.textRendererForWidget(
                            this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        }
    }
}
