package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class OracleBossBar {
    private static final Identifier TEXTURE =
            TheAurorian2.id("textures/misc/bar/rising_clock_oracle_bars.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int FRAME_WIDTH = 190;
    private static final int FRAME_HEIGHT = 21;
    private static final int FRAME_TEXTURE_Y = 5;
    private static final int HEALTH_WIDTH = 180;
    private static final int HEALTH_HEIGHT = 5;
    private static final int HEALTH_X = 5;
    private static final int HEALTH_Y = 11;
    private static final int RAGE_FRAME_WIDTH = 72, RAGE_FRAME_HEIGHT = 10;
    private static final int RAGE_WIDTH = 46, RAGE_HEIGHT = 3;
    private static final int RAGE_Y = FRAME_HEIGHT + 2;
    private static final int BAR_SPACING = 62;
    private static boolean shiftedBossLayer;

    private OracleBossBar() {}

    @SubscribeEvent
    public static void finishBossLayer(RenderGuiLayerEvent.Post event) {
        if (shiftedBossLayer && event.getName().equals(VanillaGuiLayers.BOSS_OVERLAY)) {
            event.getGuiGraphics().pose().popMatrix();
            shiftedBossLayer = false;
        }
    }

    public static void render(CustomizeGuiOverlayEvent.BossEventProgress event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || !(mc.level.getEntity(event.getBossEvent().getId()) instanceof RisingClockOracleEntity boss)) return;
        event.setCanceled(true);
        event.setIncrement(BAR_SPACING);
        var g = event.getGuiGraphics();
        // NeoForge 26.1 advances its bar cursor only for uncancelled events. Reserve
        // our rows in the boss layer's pose so subsequent custom/vanilla bars do not overlap.
        // Push lazily: a cancelled layer never enters here and does not emit Post.
        if (!shiftedBossLayer) {
            g.pose().pushMatrix();
            shiftedBossLayer = true;
        }
        int center = event.getWindow().getGuiScaledWidth() / 2;
        int x = center - FRAME_WIDTH / 2, y = event.getY() + 1;
        // Keep the supplied atlas intact. Frame (0,5), fill (0,0) at offset (5,11)
        // reproduce its finished example at (1,226) exactly, including the side segments.
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, FRAME_TEXTURE_Y,
                FRAME_WIDTH, FRAME_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        int health = Mth.ceil(Mth.clamp(event.getBossEvent().getProgress(), 0, 1) * HEALTH_WIDTH);
        if (health > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + HEALTH_X, y + HEALTH_Y, 0, 0,
                    health, HEALTH_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        int rageX = x + FRAME_WIDTH - RAGE_FRAME_WIDTH;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rageX, y + RAGE_Y, 0, 34,
                RAGE_FRAME_WIDTH, RAGE_FRAME_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        int rageWidth = Mth.ceil(boss.rage() * RAGE_WIDTH / (float) RisingClockOracleEntity.MAX_RAGE);
        if (rageWidth > 0) {
            // Fill (13,30) overlays the inset (13,2), matching the author's full example.
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rageX + 13, y + RAGE_Y + 2, 13, 30,
                    rageWidth, RAGE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        g.centeredText(mc.font, event.getBossEvent().getName(), center, event.getY() - 11, 0xFFFFE5A3);
        int action = boss.action();
        if (action != RisingClockOracleEntity.IDLE) {
            String key = switch(action) {
                case RisingClockOracleEntity.GRAVITY -> "gravity";
                case RisingClockOracleEntity.LIGHTNING -> "lightning";
                case RisingClockOracleEntity.SHIELD -> "shield";
                case RisingClockOracleEntity.BLAST -> "blast";
                case RisingClockOracleEntity.FLAME -> "flame";
                case RisingClockOracleEntity.GLIDE -> "glide";
                case RisingClockOracleEntity.CHRONO -> "chrono";
                case RisingClockOracleEntity.REPULSOR -> "repulsor";
                case RisingClockOracleEntity.COFFIN -> "coffin";
                default -> "awaken";
            };
            float duration = boss.isFinalCasting() ? cn.teampancake.theaurorian2.common.entity.OracleCoffinEntity.FINAL_STRIKE : boss.castDuration();
            int castWidth = Mth.ceil(Mth.clamp(boss.actionAge(0) / duration, 0, 1) * HEALTH_WIDTH);
            g.fill(x + HEALTH_X, y + 35, x + HEALTH_X + castWidth, y + 36, 0xFFEED086);
            Component name = Component.translatable("boss.theaurorian2.oracle." + key);
            if (boss.empowered()) name = Component.translatable("boss.theaurorian2.oracle.empowered",name);
            g.centeredText(mc.font, name, center, y + 39, boss.empowered() ? 0xFFFFE5A3 : 0xFF91E4E1);
        }
        g.pose().translate(0,BAR_SPACING);
    }
}
