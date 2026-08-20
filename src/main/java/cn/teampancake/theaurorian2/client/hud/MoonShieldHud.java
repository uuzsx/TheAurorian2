package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.screen.HudLayoutScreen;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig.MoonShieldStyle;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.world.MoonShieldData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class MoonShieldHud {

    private static final Identifier HORIZONTAL_FRAME = texture("common_horizontal_frame");
    private static final Identifier HORIZONTAL_FILL = texture("common_horizontal");
    private static final Identifier HORIZONTAL_BROKEN = texture("common_broken_horizontal");
    private static final Identifier VERTICAL_FRAME = texture("common_vertical_frame");
    private static final Identifier VERTICAL_FILL = texture("common_vertical");
    private static final Identifier VERTICAL_BROKEN = texture("common_broken_vertical");
    private static final Identifier CRIMSON_HORIZONTAL_FRAME = texture("blood_moon_horizontal_frame");
    private static final Identifier CRIMSON_HORIZONTAL_FILL = texture("blood_moon_horizontal");
    private static final Identifier CRIMSON_HORIZONTAL_BROKEN = texture("blood_moon_broken_horizonal");
    private static final Identifier CRIMSON_VERTICAL_FRAME = texture("blood_moon_vertical_frame");
    private static final Identifier CRIMSON_VERTICAL_FILL = texture("blood_moon_vertical");
    private static final Identifier CRIMSON_VERTICAL_BROKEN = texture("blood_moon_broken_vertical");
    private static final int HORIZONTAL_FRAME_WIDTH = 78;
    private static final int HORIZONTAL_FRAME_HEIGHT = 12;
    private static final int HORIZONTAL_FILL_WIDTH = 73;
    private static final int HORIZONTAL_FILL_HEIGHT = 8;
    private static final int HORIZONTAL_FILL_X = 3;
    private static final int HORIZONTAL_FILL_Y = 0;
    private static final int HORIZONTAL_VALUE_Y = 1;
    private static final int VERTICAL_FRAME_WIDTH = 18;
    private static final int VERTICAL_FRAME_HEIGHT = 74;
    private static final int VERTICAL_FILL_WIDTH = 8;
    private static final int VERTICAL_FILL_HEIGHT = 56;
    private static final int VERTICAL_FILL_X = 6;
    private static final int VERTICAL_FILL_Y = 7;
    private static final int VERTICAL_VALUE_Y = 32;
    private static final int VALUE_COLOR = 0xFFF2FBFF;
    private static final int VALUE_OUTLINE_COLOR = 0xFF232A42;
    private static final float RECOVERY_ANIMATION_TICKS = 20.0F;
    private static final Component[] VALUE_TEXT = createValueText();
    private static LocalPlayer animatedPlayer;
    private static float displayedShield;
    private static float targetShield;
    private static float recoveryPerTick;

    private MoonShieldHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.options.hideGui
                || minecraft.screen instanceof HudLayoutScreen
                || minecraft.player.isSpectator()) {
            return;
        }

        MoonShieldData data = minecraft.player.getData(ModAttachments.MOON_SHIELD);
        if (!data.purified()) {
            resetAnimation();
            return;
        }

        updateAnimation(minecraft.player, data.shield(), deltaTracker.getRealtimeDeltaTicks());
        HudLayoutRegistry.Position position = HudLayoutRegistry.position(
                HudLayoutRegistry.MOON_SHIELD,
                graphics.guiWidth(),
                graphics.guiHeight());
        renderAt(
                graphics,
                position.x(),
                position.y(),
                displayedShield,
                style(),
                data.crimson(),
                data.maxShield());
    }

    static void renderPreview(GuiGraphicsExtractor graphics, int x, int y) {
        renderAt(graphics, x, y, MoonShieldData.MAX_SHIELD, style(), false, MoonShieldData.MAX_SHIELD);
    }

    static int frameWidth() {
        return style() == MoonShieldStyle.HORIZONTAL
                ? HORIZONTAL_FRAME_WIDTH
                : VERTICAL_FRAME_WIDTH;
    }

    static int frameHeight() {
        return style() == MoonShieldStyle.HORIZONTAL
                ? HORIZONTAL_FRAME_HEIGHT
                : VERTICAL_FRAME_HEIGHT;
    }

    private static void renderAt(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            MoonShieldStyle style,
            boolean crimson,
            float maxShield) {
        if (style == MoonShieldStyle.VERTICAL) {
            renderVertical(graphics, frameX, frameY, shield, crimson, maxShield);
        } else {
            renderHorizontal(graphics, frameX, frameY, shield, crimson, maxShield);
        }
    }

    private static void renderHorizontal(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            boolean crimson,
            float maxShield) {
        int fillX = frameX + HORIZONTAL_FILL_X;
        int fillY = frameY + HORIZONTAL_FILL_Y;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                crimson ? CRIMSON_HORIZONTAL_FRAME : HORIZONTAL_FRAME,
                frameX,
                frameY,
                0.0F,
                0.0F,
                HORIZONTAL_FRAME_WIDTH,
                HORIZONTAL_FRAME_HEIGHT,
                HORIZONTAL_FRAME_WIDTH,
                HORIZONTAL_FRAME_HEIGHT);
        if (shield <= 0.0F) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    crimson ? CRIMSON_HORIZONTAL_BROKEN : HORIZONTAL_BROKEN,
                    fillX,
                    fillY,
                    0.0F,
                    0.0F,
                    HORIZONTAL_FILL_WIDTH,
                    HORIZONTAL_FILL_HEIGHT,
                    HORIZONTAL_FILL_WIDTH,
                    HORIZONTAL_FILL_HEIGHT);
        } else {
            int width = Mth.ceil(shield / maxShield * HORIZONTAL_FILL_WIDTH);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    crimson ? CRIMSON_HORIZONTAL_FILL : HORIZONTAL_FILL,
                    fillX,
                    fillY,
                    0.0F,
                    0.0F,
                    width,
                    HORIZONTAL_FILL_HEIGHT,
                    HORIZONTAL_FILL_WIDTH,
                    HORIZONTAL_FILL_HEIGHT);
        }
        renderValue(
                graphics,
                frameX,
                frameY + HORIZONTAL_VALUE_Y,
                HORIZONTAL_FRAME_WIDTH,
                shield,
                maxShield);
    }

    private static void renderVertical(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            boolean crimson,
            float maxShield) {
        int fillX = frameX + VERTICAL_FILL_X;
        int fillY = frameY + VERTICAL_FILL_Y;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                crimson ? CRIMSON_VERTICAL_FRAME : VERTICAL_FRAME,
                frameX,
                frameY,
                0.0F,
                0.0F,
                VERTICAL_FRAME_WIDTH,
                VERTICAL_FRAME_HEIGHT,
                VERTICAL_FRAME_WIDTH,
                VERTICAL_FRAME_HEIGHT);
        if (shield <= 0.0F) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    crimson ? CRIMSON_VERTICAL_BROKEN : VERTICAL_BROKEN,
                    fillX,
                    fillY,
                    0.0F,
                    0.0F,
                    VERTICAL_FILL_WIDTH,
                    VERTICAL_FILL_HEIGHT,
                    VERTICAL_FILL_WIDTH,
                    VERTICAL_FILL_HEIGHT);
        } else {
            int height = Mth.ceil(shield / maxShield * VERTICAL_FILL_HEIGHT);
            int sourceY = VERTICAL_FILL_HEIGHT - height;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    crimson ? CRIMSON_VERTICAL_FILL : VERTICAL_FILL,
                    fillX,
                    fillY + sourceY,
                    0.0F,
                    sourceY,
                    VERTICAL_FILL_WIDTH,
                    height,
                    VERTICAL_FILL_WIDTH,
                    VERTICAL_FILL_HEIGHT);
        }
        renderVerticalValue(graphics, frameX, frameY, shield, maxShield);
    }

    private static void renderVerticalValue(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            float maxShield) {
        renderValue(
                graphics,
                frameX + 1,
                frameY + VERTICAL_VALUE_Y,
                VERTICAL_FRAME_WIDTH,
                shield,
                maxShield);
    }

    private static void renderValue(
            GuiGraphicsExtractor graphics,
            int frameX,
            int textY,
            int frameWidth,
            float shield,
            float maxShield) {
        Font font = Minecraft.getInstance().font;
        int roundedShield = Math.clamp(Math.round(shield), 0, Math.round(maxShield));
        Component text = VALUE_TEXT[roundedShield];
        int textX = frameX + (frameWidth - font.width(text)) / 2;
        graphics.text(font, text, textX - 1, textY, VALUE_OUTLINE_COLOR, false);
        graphics.text(font, text, textX + 1, textY, VALUE_OUTLINE_COLOR, false);
        graphics.text(font, text, textX, textY - 1, VALUE_OUTLINE_COLOR, false);
        graphics.text(font, text, textX, textY + 1, VALUE_OUTLINE_COLOR, false);
        graphics.text(font, text, textX, textY, VALUE_COLOR, false);
    }

    private static void updateAnimation(LocalPlayer player, float shield, float deltaTicks) {
        if (animatedPlayer != player) {
            animatedPlayer = player;
            displayedShield = shield;
            targetShield = shield;
            recoveryPerTick = 0.0F;
            return;
        }

        if (shield < targetShield) {
            displayedShield = shield;
            targetShield = shield;
            recoveryPerTick = 0.0F;
            return;
        }

        if (shield > targetShield) {
            targetShield = shield;
            recoveryPerTick = (targetShield - displayedShield) / RECOVERY_ANIMATION_TICKS;
        }

        if (displayedShield < targetShield) {
            displayedShield = Math.min(
                    targetShield,
                    displayedShield + recoveryPerTick * Math.max(0.0F, deltaTicks));
        }
    }

    private static void resetAnimation() {
        animatedPlayer = null;
        displayedShield = 0.0F;
        targetShield = 0.0F;
        recoveryPerTick = 0.0F;
    }

    private static Identifier texture(String name) {
        return TheAurorian2.id("textures/misc/shield/" + name + ".png");
    }

    private static MoonShieldStyle style() {
        return HudLayoutConfig.moonShieldStyle();
    }

    private static Component[] createValueText() {
        int maxShield = Math.round(MoonShieldData.MAX_CRIMSON_SHIELD);
        Component[] values = new Component[maxShield + 1];
        for (int value = 0; value <= maxShield; value++) {
            values[value] = Component.literal(Integer.toString(value));
        }
        return values;
    }
}
