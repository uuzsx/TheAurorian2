package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.client.hud.HudLayoutRegistry;
import cn.teampancake.theaurorian2.client.hud.HudLayoutRegistry.HudElement;
import cn.teampancake.theaurorian2.client.hud.HudLayoutRegistry.Position;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig.MoonShieldStyle;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class HudLayoutScreen extends Screen {

    private static final int BACKGROUND_COLOR = 0xA0606060;
    private static final int SELECTION_COLOR = 0xC8FFFFFF;
    private static final int SNAP_DISTANCE = 4;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int RESET_SELECTED_WIDTH = 108;
    private static final int RESET_ALL_WIDTH = 76;
    private static final int DONE_WIDTH = 54;
    private static final int SHIELD_STYLE_WIDTH = 130;

    private @Nullable HudElement selected;
    private @Nullable Button resetSelectedButton;
    private @Nullable Button shieldStyleButton;
    private boolean dragging;
    private boolean dirty;
    private double dragOffsetX;
    private double dragOffsetY;
    private final int[] xSnapCandidates = new int[3 + 5 * (HudLayoutRegistry.elements().size() - 1)];
    private final int[] ySnapCandidates = new int[3 + 5 * (HudLayoutRegistry.elements().size() - 1)];

    public HudLayoutScreen() {
        super(Component.translatable("gui.theaurorian2.hud_layout.title"));
    }

    @Override
    protected void init() {
        int totalWidth = RESET_SELECTED_WIDTH + RESET_ALL_WIDTH + DONE_WIDTH + BUTTON_GAP * 2;
        int left = (this.width - totalWidth) / 2;
        int top = this.height - BUTTON_HEIGHT - 6;
        this.shieldStyleButton = this.addRenderableWidget(Button.builder(
                        shieldStyleMessage(),
                        button -> this.toggleShieldStyle())
                .bounds(
                        (this.width - SHIELD_STYLE_WIDTH) / 2,
                        top - BUTTON_HEIGHT - BUTTON_GAP,
                        SHIELD_STYLE_WIDTH,
                        BUTTON_HEIGHT)
                .build());
        this.resetSelectedButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.theaurorian2.hud_layout.reset_selected"),
                        button -> this.resetSelected())
                .bounds(left, top, RESET_SELECTED_WIDTH, BUTTON_HEIGHT)
                .build());
        this.resetSelectedButton.active = this.selected != null;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.theaurorian2.hud_layout.reset_all"),
                        button -> this.resetAll())
                .bounds(left + RESET_SELECTED_WIDTH + BUTTON_GAP, top, RESET_ALL_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.theaurorian2.hud_layout.done"),
                        button -> this.onClose())
                .bounds(left + RESET_SELECTED_WIDTH + RESET_ALL_WIDTH + BUTTON_GAP * 2, top, DONE_WIDTH, BUTTON_HEIGHT)
                .build());
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
        graphics.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (HudElement element : HudLayoutRegistry.elements()) {
            Position position = HudLayoutRegistry.position(element, this.width, this.height);
            element.renderPreview(graphics, position.x(), position.y());
        }

        if (this.selected != null) {
            Position position = HudLayoutRegistry.position(this.selected, this.width, this.height);
            int left = Math.max(0, position.x() - 2);
            int top = Math.max(0, position.y() - 2);
            int right = Math.min(this.width, position.x() + this.selected.width() + 2);
            int bottom = Math.min(this.height, position.y() + this.selected.height() + 2);
            graphics.outline(
                    left,
                    top,
                    right - left,
                    bottom - top,
                    SELECTION_COLOR);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (event.button() != 0) {
            return false;
        }

        this.select(elementAt(event.x(), event.y()));
        if (this.selected == null) {
            this.dragging = false;
            return true;
        }

        Position position = HudLayoutRegistry.position(this.selected, this.width, this.height);
        this.dragOffsetX = event.x() - position.x();
        this.dragOffsetY = event.y() - position.y();
        this.dragging = true;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!this.dragging || this.selected == null || event.button() != 0) {
            return super.mouseDragged(event, dx, dy);
        }

        int x = (int)Math.round(event.x() - this.dragOffsetX);
        int y = (int)Math.round(event.y() - this.dragOffsetY);
        this.moveWithSnapping(this.selected, x, y);
        this.dirty = true;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.dragging) {
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        this.saveIfDirty();
        super.onClose();
    }

    @Override
    public void removed() {
        this.saveIfDirty();
    }

    private @Nullable HudElement elementAt(double mouseX, double mouseY) {
        List<HudElement> elements = HudLayoutRegistry.elements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            Position position = HudLayoutRegistry.position(element, this.width, this.height);
            if (mouseX >= position.x()
                    && mouseX < position.x() + element.width()
                    && mouseY >= position.y()
                    && mouseY < position.y() + element.height()) {
                return element;
            }
        }
        return null;
    }

    private void select(@Nullable HudElement element) {
        this.selected = element;
        if (this.resetSelectedButton != null) {
            this.resetSelectedButton.active = element != null;
        }
    }

    private void resetSelected() {
        if (this.selected != null) {
            HudLayoutRegistry.reset(this.selected);
            this.dirty = true;
        }
    }

    private void resetAll() {
        HudLayoutRegistry.resetAll();
        this.dirty = true;
    }

    private void toggleShieldStyle() {
        HudLayoutConfig.setMoonShieldStyle(HudLayoutConfig.moonShieldStyle().next());
        if (this.shieldStyleButton != null) {
            this.shieldStyleButton.setMessage(shieldStyleMessage());
        }
        this.dirty = true;
    }

    private static Component shieldStyleMessage() {
        String styleKey = HudLayoutConfig.moonShieldStyle() == MoonShieldStyle.HORIZONTAL
                ? "gui.theaurorian2.hud_layout.shield_style.horizontal"
                : "gui.theaurorian2.hud_layout.shield_style.vertical";
        return Component.translatable(
                "gui.theaurorian2.hud_layout.shield_style",
                Component.translatable(styleKey));
    }

    private void moveWithSnapping(HudElement element, int x, int y) {
        int maxX = Math.max(0, this.width - element.width());
        int maxY = Math.max(0, this.height - element.height());
        int xCandidateCount = 3;
        int yCandidateCount = 3;
        this.xSnapCandidates[0] = 0;
        this.xSnapCandidates[1] = (this.width - element.width()) / 2;
        this.xSnapCandidates[2] = maxX;
        this.ySnapCandidates[0] = 0;
        this.ySnapCandidates[1] = (this.height - element.height()) / 2;
        this.ySnapCandidates[2] = maxY;

        for (HudElement other : HudLayoutRegistry.elements()) {
            if (other == element) {
                continue;
            }
            Position otherPosition = HudLayoutRegistry.position(other, this.width, this.height);
            this.xSnapCandidates[xCandidateCount++] = otherPosition.x() - element.width();
            this.xSnapCandidates[xCandidateCount++] = otherPosition.x();
            this.xSnapCandidates[xCandidateCount++] =
                    otherPosition.x() + (other.width() - element.width()) / 2;
            this.xSnapCandidates[xCandidateCount++] =
                    otherPosition.x() + other.width() - element.width();
            this.xSnapCandidates[xCandidateCount++] = otherPosition.x() + other.width();
            this.ySnapCandidates[yCandidateCount++] = otherPosition.y() - element.height();
            this.ySnapCandidates[yCandidateCount++] = otherPosition.y();
            this.ySnapCandidates[yCandidateCount++] =
                    otherPosition.y() + (other.height() - element.height()) / 2;
            this.ySnapCandidates[yCandidateCount++] =
                    otherPosition.y() + other.height() - element.height();
            this.ySnapCandidates[yCandidateCount++] = otherPosition.y() + other.height();
        }

        HudLayoutRegistry.move(
                element,
                snapAxis(Math.clamp(x, 0, maxX), 0, maxX, this.xSnapCandidates, xCandidateCount),
                snapAxis(Math.clamp(y, 0, maxY), 0, maxY, this.ySnapCandidates, yCandidateCount),
                this.width,
                this.height);
    }

    private static int snapAxis(
            int position,
            int minimum,
            int maximum,
            int[] candidates,
            int candidateCount) {
        int snapped = position;
        int closestDistance = SNAP_DISTANCE + 1;
        for (int i = 0; i < candidateCount; i++) {
            int candidate = candidates[i];
            if (candidate < minimum || candidate > maximum) {
                continue;
            }
            int distance = Math.abs(position - candidate);
            if (distance < closestDistance) {
                snapped = candidate;
                closestDistance = distance;
            }
        }
        return snapped;
    }

    private void saveIfDirty() {
        if (this.dirty) {
            HudLayoutRegistry.save();
            this.dirty = false;
        }
    }
}
