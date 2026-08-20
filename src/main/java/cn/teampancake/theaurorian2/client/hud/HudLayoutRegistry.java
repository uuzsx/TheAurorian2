package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.common.config.HudLayoutConfig;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudLayoutRegistry {

    public static final HudElement AURORIAN_BLESSING = new HudElement(
            HudLayoutConfig.Element.AURORIAN_BLESSING,
            () -> AurorianNightHud.HUD_WIDTH,
            () -> AurorianNightHud.HUD_HEIGHT,
            AurorianNightHud::renderPreview);
    public static final HudElement MOON_SHIELD = new HudElement(
            HudLayoutConfig.Element.MOON_SHIELD,
            MoonShieldHud::frameWidth,
            MoonShieldHud::frameHeight,
            MoonShieldHud::renderPreview);
    private static final List<HudElement> ELEMENTS = List.of(AURORIAN_BLESSING, MOON_SHIELD);

    private HudLayoutRegistry() {
    }

    public static List<HudElement> elements() {
        return ELEMENTS;
    }

    public static Position position(HudElement element, int screenWidth, int screenHeight) {
        HudLayoutConfig.Anchor anchor = HudLayoutConfig.anchor(element.configElement());
        int x = anchor.baseX(screenWidth, element.width())
                + HudLayoutConfig.offsetX(element.configElement());
        int y = anchor.baseY(screenHeight, element.height())
                + HudLayoutConfig.offsetY(element.configElement());
        return element.position.set(
                Math.clamp(x, 0, Math.max(0, screenWidth - element.width())),
                Math.clamp(y, 0, Math.max(0, screenHeight - element.height())));
    }

    public static void move(
            HudElement element,
            int x,
            int y,
            int screenWidth,
            int screenHeight) {
        int boundedX = Math.clamp(x, 0, Math.max(0, screenWidth - element.width()));
        int boundedY = Math.clamp(y, 0, Math.max(0, screenHeight - element.height()));
        HudLayoutConfig.Anchor anchor = HudLayoutConfig.Anchor.nearest(
                boundedX,
                boundedY,
                screenWidth,
                screenHeight,
                element.width(),
                element.height());
        HudLayoutConfig.set(
                element.configElement(),
                anchor,
                boundedX - anchor.baseX(screenWidth, element.width()),
                boundedY - anchor.baseY(screenHeight, element.height()));
    }

    public static void reset(HudElement element) {
        HudLayoutConfig.reset(element.configElement());
    }

    public static void resetAll() {
        for (HudElement element : ELEMENTS) {
            reset(element);
        }
    }

    public static void save() {
        HudLayoutConfig.save();
    }

    public static final class HudElement {

        private final HudLayoutConfig.Element configElement;
        private final IntSupplier width;
        private final IntSupplier height;
        private final PreviewRenderer previewRenderer;
        private final Position position = new Position();

        private HudElement(
                HudLayoutConfig.Element configElement,
                IntSupplier width,
                IntSupplier height,
                PreviewRenderer previewRenderer) {
            this.configElement = configElement;
            this.width = width;
            this.height = height;
            this.previewRenderer = previewRenderer;
        }

        public HudLayoutConfig.Element configElement() {
            return this.configElement;
        }

        public int width() {
            return this.width.getAsInt();
        }

        public int height() {
            return this.height.getAsInt();
        }

        public void renderPreview(GuiGraphicsExtractor graphics, int x, int y) {
            this.previewRenderer.render(graphics, x, y);
        }
    }

    public static final class Position {

        private int x;
        private int y;

        private Position set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }
    }

    @FunctionalInterface
    public interface PreviewRenderer {
        void render(GuiGraphicsExtractor graphics, int x, int y);
    }
}
