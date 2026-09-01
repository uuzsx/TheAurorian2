package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.WorldScrollItem;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class WorldScrollClientEffects {

    private static final int VEIL_RGB = 0x071426;
    private static final int MOONLIGHT_RGB = 0xD9F4FF;

    private WorldScrollClientEffects() {
    }

    @SubscribeEvent
    public static void renderVeil(RenderGuiEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        float progress = progress(player, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        if (progress <= 0.0F) {
            return;
        }

        float veil = smooth(Mth.clamp((progress - 0.08F) / 0.92F, 0.0F, 1.0F));
        int alpha = Mth.clamp(Mth.floor(veil * 232.0F), 0, 232);
        var graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | VEIL_RGB);

        float opening = smooth(Mth.clamp((progress - 0.38F) / 0.62F, 0.0F, 1.0F));
        int width = Mth.floor(graphics.guiWidth() * opening);
        int lineAlpha = Mth.clamp(Mth.floor(opening * 220.0F), 0, 220);
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        graphics.fill(
                centerX - width / 2,
                centerY,
                centerX + width / 2,
                centerY + 1,
                lineAlpha << 24 | MOONLIGHT_RGB);
    }

    @SubscribeEvent
    public static void tiltCamera(ViewportEvent.ComputeCameraAngles event) {
        float progress = progress(Minecraft.getInstance().player, (float) event.getPartialTick());
        if (progress <= 0.0F) {
            return;
        }
        float eased = smooth(progress);
        event.setPitch(event.getPitch() - eased * 2.5F);
        event.setRoll(event.getRoll() + (float) Math.sin(progress * Math.PI * 3.0) * eased * 1.4F);
    }

    private static float progress(LocalPlayer player, float partialTick) {
        if (player == null
                || !player.isUsingItem()
                || !player.getUseItem().is(ModLegacyItems.WORLD_SCROLL.get())) {
            return 0.0F;
        }
        return Mth.clamp(
                player.getTicksUsingItem(partialTick) / WorldScrollItem.USE_DURATION,
                0.0F,
                1.0F);
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
