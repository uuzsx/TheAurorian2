package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.screen.HudLayoutScreen;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig;
import cn.teampancake.theaurorian2.common.config.HudLayoutConfig.MoonShieldStyle;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.world.MoonShieldData;
import cn.teampancake.theaurorian2.common.world.MoonShieldSystem;
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
    private static final Identifier HORIZONTAL_BREAK_ANIMATION = texture("common_horizontal_break_animation");
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
    private static final int HORIZONTAL_BREAK_FRAME_HEIGHT = 17;
    private static final int HORIZONTAL_BREAK_TEXTURE_HEIGHT = 136;
    private static final int HORIZONTAL_BREAK_Y = -2;
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
    private static final int CRIMSON_VALUE_COLOR = 0xFFFFC5CF;
    private static final int CRIMSON_VALUE_OUTLINE_COLOR = 0xFF4A1222;
    private static final float RECOVERY_ANIMATION_TICKS = 20.0F;
    private static final Component[] VALUE_TEXT = createValueText();
    private static LocalPlayer animatedPlayer;
    private static float displayedShield;
    private static float targetShield;
    private static float recoveryPerTick;
    private static float breakTransitionTicks;

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

        updateAnimation(
                minecraft.player,
                data.shield(),
                data.crimson(),
                deltaTracker.getRealtimeDeltaTicks());
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
                data.maxShield(),
                data.shield() <= 0.0F,
                minecraft.player.getData(ModAttachments.MOON_SHIELD_RECOVERY_AT),
                minecraft.player.level().getGameTime());
    }

    static void renderPreview(GuiGraphicsExtractor graphics, int x, int y) {
        renderAt(
                graphics,
                x,
                y,
                MoonShieldData.MAX_SHIELD,
                style(),
                false,
                MoonShieldData.MAX_SHIELD,
                false,
                0L,
                0L);
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
            float maxShield,
            boolean depleted,
            long recoveryAt,
            long gameTime) {
        if (style == MoonShieldStyle.VERTICAL) {
            renderVertical(graphics, frameX, frameY, shield, crimson, maxShield);
        } else {
            renderHorizontal(
                    graphics,
                    frameX,
                    frameY,
                    shield,
                    crimson,
                    maxShield,
                    depleted,
                    recoveryAt,
                    gameTime);
        }
    }

    private static void renderHorizontal(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            boolean crimson,
            float maxShield,
            boolean depleted,
            long recoveryAt,
            long gameTime) {
        int fillX = frameX + HORIZONTAL_FILL_X;
        int fillY = frameY + HORIZONTAL_FILL_Y;
        if (!crimson && depleted) {
            renderHorizontalBreakAnimation(graphics, frameX, frameY, recoveryAt, gameTime);
            renderValue(
                    graphics,
                    frameX,
                    frameY + HORIZONTAL_VALUE_Y,
                    HORIZONTAL_FRAME_WIDTH,
                    shield,
                    maxShield,
                    false);
            return;
        }
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
        if (crimson && shield <= 0.0F) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    CRIMSON_HORIZONTAL_BROKEN,
                    fillX,
                    fillY,
                    0.0F,
                    0.0F,
                    HORIZONTAL_FILL_WIDTH,
                    HORIZONTAL_FILL_HEIGHT,
                    HORIZONTAL_FILL_WIDTH,
                    HORIZONTAL_FILL_HEIGHT);
        } else if (shield > 0.0F) {
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
                maxShield,
                crimson);
    }

    private static void renderHorizontalBreakAnimation(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            long recoveryAt,
            long gameTime) {
        int frame = horizontalBreakFrame(recoveryAt, gameTime);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                HORIZONTAL_BREAK_ANIMATION,
                frameX,
                frameY + HORIZONTAL_BREAK_Y,
                0.0F,
                frame * HORIZONTAL_BREAK_FRAME_HEIGHT,
                HORIZONTAL_FRAME_WIDTH,
                HORIZONTAL_BREAK_FRAME_HEIGHT,
                HORIZONTAL_FRAME_WIDTH,
                HORIZONTAL_BREAK_TEXTURE_HEIGHT);
    }

    private static int horizontalBreakFrame(long recoveryAt, long gameTime) {
        int transitionTicks = MoonShieldSystem.BREAK_TRANSITION_TICKS;
        if (breakTransitionTicks < transitionTicks) {
            return Math.min(
                    MoonShieldSystem.BREAK_TRANSITION_FRAME_COUNT - 1,
                    (int) breakTransitionTicks / MoonShieldSystem.BREAK_TRANSITION_FRAME_TICKS);
        }

        long remainingTicks = recoveryAt - gameTime;
        if (recoveryAt <= 0L || remainingTicks > transitionTicks) {
            return MoonShieldSystem.BREAK_TRANSITION_FRAME_COUNT - 1;
        }

        long repairTicks = Math.clamp(transitionTicks - Math.max(0L, remainingTicks), 0L, transitionTicks - 1L);
        int repairFrame = (int) (repairTicks / MoonShieldSystem.BREAK_TRANSITION_FRAME_TICKS);
        return MoonShieldSystem.BREAK_TRANSITION_FRAME_COUNT
                + Math.min(MoonShieldSystem.BREAK_TRANSITION_FRAME_COUNT - 1, repairFrame);
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
        renderVerticalValue(graphics, frameX, frameY, shield, maxShield, crimson);
    }

    private static void renderVerticalValue(
            GuiGraphicsExtractor graphics,
            int frameX,
            int frameY,
            float shield,
            float maxShield,
            boolean crimson) {
        renderValue(
                graphics,
                frameX + 1,
                frameY + VERTICAL_VALUE_Y,
                VERTICAL_FRAME_WIDTH,
                shield,
                maxShield,
                crimson);
    }

    private static void renderValue(
            GuiGraphicsExtractor graphics,
            int frameX,
            int textY,
            int frameWidth,
            float shield,
            float maxShield,
            boolean crimson) {
        Font font = Minecraft.getInstance().font;
        int roundedShield = Math.clamp(Math.round(shield), 0, Math.round(maxShield));
        Component text = VALUE_TEXT[roundedShield];
        int textX = frameX + (frameWidth - font.width(text)) / 2;
        int valueColor = crimson ? CRIMSON_VALUE_COLOR : VALUE_COLOR;
        int outlineColor = crimson ? CRIMSON_VALUE_OUTLINE_COLOR : VALUE_OUTLINE_COLOR;
        graphics.text(font, text, textX - 1, textY, outlineColor, false);
        graphics.text(font, text, textX + 1, textY, outlineColor, false);
        graphics.text(font, text, textX, textY - 1, outlineColor, false);
        graphics.text(font, text, textX, textY + 1, outlineColor, false);
        graphics.text(font, text, textX, textY, valueColor, false);
    }

    private static void updateAnimation(LocalPlayer player, float shield, boolean crimson, float deltaTicks) {
        if (animatedPlayer != player) {
            animatedPlayer = player;
            displayedShield = shield;
            targetShield = shield;
            recoveryPerTick = 0.0F;
            breakTransitionTicks = shield <= 0.0F && !crimson
                    ? MoonShieldSystem.BREAK_TRANSITION_TICKS
                    : 0.0F;
            return;
        }

        boolean justBroke = !crimson && shield <= 0.0F && targetShield > 0.0F;
        if (shield < targetShield) {
            displayedShield = shield;
            targetShield = shield;
            recoveryPerTick = 0.0F;
            if (justBroke) {
                breakTransitionTicks = 0.0F;
            }
        } else {
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

        if (!crimson && shield <= 0.0F && !justBroke) {
            breakTransitionTicks = Math.min(
                    MoonShieldSystem.BREAK_TRANSITION_TICKS,
                    breakTransitionTicks + Math.max(0.0F, deltaTicks));
        } else if (crimson || shield > 0.0F) {
            breakTransitionTicks = 0.0F;
        }
    }

    private static void resetAnimation() {
        animatedPlayer = null;
        displayedShield = 0.0F;
        targetShield = 0.0F;
        recoveryPerTick = 0.0F;
        breakTransitionTicks = 0.0F;
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
