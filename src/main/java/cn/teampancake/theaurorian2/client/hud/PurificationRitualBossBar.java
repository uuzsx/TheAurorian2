package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

public final class PurificationRitualBossBar {

    private static final Identifier TEXTURE =
            TheAurorian2.id("textures/gui/purification_ritual_progress.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int FRAME_TEXTURE_X = 7;
    private static final int FRAME_TEXTURE_Y = 1;
    private static final int FRAME_WIDTH = 194;
    private static final int FRAME_HEIGHT = 14;
    private static final int PROGRESS_TEXTURE_X = 16;
    private static final int PROGRESS_TEXTURE_Y = 22;
    private static final int PROGRESS_WIDTH = 176;
    private static final int PROGRESS_HEIGHT = 4;
    private static final int PROGRESS_X = 9;
    private static final int PROGRESS_Y = 5;
    private static final int TITLE_COLOR = 0xFFFFFFF0;

    private PurificationRitualBossBar() {
    }

    public static void render(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!PurificationAltarBlockEntity.isProgressEvent(event.getBossEvent().getId())) {
            return;
        }

        event.setCanceled(true);
        event.setIncrement(26);
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int screenCenterX = event.getWindow().getGuiScaledWidth() / 2;
        int frameX = screenCenterX - FRAME_WIDTH / 2;
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
        int progressWidth = Mth.ceil(event.getBossEvent().getProgress() * PROGRESS_WIDTH);
        if (progressWidth > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    frameX + PROGRESS_X,
                    frameY + PROGRESS_Y,
                    PROGRESS_TEXTURE_X,
                    PROGRESS_TEXTURE_Y,
                    progressWidth,
                    PROGRESS_HEIGHT,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE);
        }

        Minecraft minecraft = Minecraft.getInstance();
        graphics.centeredText(
                minecraft.font,
                event.getBossEvent().getName(),
                screenCenterX,
                event.getY() - 11,
                TITLE_COLOR);
    }
}
