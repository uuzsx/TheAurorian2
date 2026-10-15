package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEnvironmentAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class StarblightFogRenderer {

    private static final double FULL_FOG_HEIGHT = 96.0;
    private static final double CLEAR_HEIGHT = 120.0;

    private StarblightFogRenderer() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void color(ViewportEvent.ComputeFogColor event) {
        var level = Minecraft.getInstance().level;
        var camera = event.getCamera();
        if (level == null || !level.dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        if (camera.entity() instanceof LivingEntity entity
                && (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS))) {
            return;
        }
        var probe = camera.attributeProbe();
        float partialTick = (float) event.getPartialTick();
        float strength = strength(camera, partialTick);
        if (strength <= 0) {
            return;
        }
        // The dimension's blessing timeline replaces vanilla fog_color after the biome layer.
        // Separate interpolated attributes retain local fog without altering the global timeline.
        int color = probe.getValue(ModEnvironmentAttributes.LOCAL_FOG_COLOR.get(), partialTick);
        int base = probe.getValue(EnvironmentAttributes.FOG_COLOR, partialTick);
        float baseBrightness = Math.max(ARGB.redFloat(base), Math.max(ARGB.greenFloat(base), ARGB.blueFloat(base)));
        float brightness = baseBrightness > 0 ? Math.max(event.getRed(), Math.max(event.getGreen(), event.getBlue())) / baseBrightness : 0;
        event.setRed(Mth.lerp(strength, event.getRed(), Mth.clamp(ARGB.redFloat(color) * brightness, 0, 1)));
        event.setGreen(Mth.lerp(strength, event.getGreen(), Mth.clamp(ARGB.greenFloat(color) * brightness, 0, 1)));
        event.setBlue(Mth.lerp(strength, event.getBlue(), Mth.clamp(ARGB.blueFloat(color) * brightness, 0, 1)));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void distance(ViewportEvent.RenderFog event) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || !level.dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                || !(event.getEnvironment() instanceof AtmosphericFogEnvironment)) {
            return;
        }
        var camera = event.getCamera();
        float partialTick = (float) event.getPartialTick();
        float strength = strength(camera, partialTick);
        if (strength <= 0) {
            return;
        }
        var probe = camera.attributeProbe();
        var fog = event.getFogData();
        float localStart = probe.getValue(ModEnvironmentAttributes.LOCAL_FOG_START_DISTANCE.get(), partialTick);
        float localEnd = probe.getValue(ModEnvironmentAttributes.LOCAL_FOG_END_DISTANCE.get(), partialTick);
        // Start from the native fog result so weather, boss fog and render-distance limits survive.
        float weatherOffset = fog.environmentalStart
                - probe.getValue(EnvironmentAttributes.FOG_START_DISTANCE, partialTick);
        fog.environmentalStart = Mth.lerp(strength, fog.environmentalStart, localStart + weatherOffset);
        fog.environmentalEnd = Mth.lerp(strength, fog.environmentalEnd, Math.min(fog.environmentalEnd, localEnd));
        fog.skyEnd = Mth.lerp(strength, fog.skyEnd, Math.min(fog.skyEnd, localEnd));
        fog.cloudEnd = Mth.lerp(strength, fog.cloudEnd, Math.min(fog.cloudEnd, localEnd));
        if (minecraft.gui.getBossOverlay().shouldCreateWorldFog()) {
            fog.environmentalStart = Math.min(fog.environmentalStart, 10.0F);
        }
    }

    private static float strength(Camera camera, float partialTick) {
        float height = (float) Mth.clamp((CLEAR_HEIGHT - camera.position().y)
                / (CLEAR_HEIGHT - FULL_FOG_HEIGHT), 0.0, 1.0);
        float heightFade = height * height * (3.0F - 2.0F * height);
        return heightFade * Mth.clamp(camera.attributeProbe()
                .getValue(ModEnvironmentAttributes.LOCAL_FOG_STRENGTH.get(), partialTick), 0, 1);
    }
}
