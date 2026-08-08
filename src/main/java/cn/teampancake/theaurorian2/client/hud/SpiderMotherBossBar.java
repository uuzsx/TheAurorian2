package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

public final class SpiderMotherBossBar {

    private static final Identifier TEXTURE =
            TheAurorian2.id("textures/misc/bar/spider_mother_bars.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int FRAME_WIDTH = 186;
    private static final int FRAME_HEIGHT = 22;
    private static final int FRAME_TEXTURE_X = 0;
    private static final int FRAME_TEXTURE_Y = 5;
    private static final int HEALTH_WIDTH = 180;
    private static final int HEALTH_HEIGHT = 5;
    private static final int HEALTH_TEXTURE_X = 0;
    private static final int HEALTH_TEXTURE_Y = 0;
    private static final int HEALTH_X = 3;
    private static final int HEALTH_Y = 10;
    private static final int NAME_COLOR = 0xFF58F5B4;
    private static final int RAGE_NAME_COLOR = 0xFFD1FFDF;

    private SpiderMotherBossBar() {
    }

    public static void render(CustomizeGuiOverlayEvent.BossEventProgress event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || !(minecraft.level.getEntity(event.getBossEvent().getId()) instanceof SpiderMotherEntity mother)) {
            return;
        }

        event.setCanceled(true);
        event.setIncrement(32);
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int frameX = event.getWindow().getGuiScaledWidth() / 2 - FRAME_WIDTH / 2;
        int frameY = event.getY() - 3;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                frameX,
                frameY,
                FRAME_TEXTURE_X,
                FRAME_TEXTURE_Y,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE);
        int healthWidth = Mth.ceil(event.getBossEvent().getProgress() * HEALTH_WIDTH);
        if (healthWidth > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    frameX + HEALTH_X,
                    frameY + HEALTH_Y,
                    HEALTH_TEXTURE_X,
                    HEALTH_TEXTURE_Y,
                    healthWidth,
                    HEALTH_HEIGHT,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE);
        }
        graphics.centeredText(
                minecraft.font,
                event.getBossEvent().getName(),
                event.getWindow().getGuiScaledWidth() / 2,
                event.getY() - 11,
                mother.isRageActive() && mother.tickCount / 5 % 2 == 0 ? RAGE_NAME_COLOR : NAME_COLOR);
    }
}
