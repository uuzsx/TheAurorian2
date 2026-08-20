package cn.teampancake.theaurorian2.client.input;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.hud.AurorianNightHud;
import cn.teampancake.theaurorian2.client.screen.HudLayoutScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ModKeyMappings {

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(TheAurorian2.id("main"));
    private static final KeyMapping OPEN_HUD_LAYOUT = new KeyMapping(
            "key.theaurorian2.open_hud_layout",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            CATEGORY);
    private static final KeyMapping TOGGLE_NIGHT_HUD = new KeyMapping(
            "key.theaurorian2.toggle_night_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private ModKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_HUD_LAYOUT);
        event.register(TOGGLE_NIGHT_HUD);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_HUD_LAYOUT.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null && minecraft.level != null && minecraft.player != null) {
                minecraft.setScreen(new HudLayoutScreen());
            }
        }

        while (TOGGLE_NIGHT_HUD.consumeClick()) {
            if (Minecraft.getInstance().screen == null) {
                AurorianNightHud.toggleVisible();
            }
        }
    }
}
