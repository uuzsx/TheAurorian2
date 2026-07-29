package cn.teampancake.theaurorian2.client.input;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.hud.AurorianNightHud;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
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
    private static final KeyMapping TOGGLE_NIGHT_HUD = new KeyMapping(
            "key.theaurorian2.toggle_night_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            CATEGORY);

    private ModKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE_NIGHT_HUD);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_NIGHT_HUD.consumeClick()) {
            AurorianNightHud.toggleVisible();
        }
    }
}
