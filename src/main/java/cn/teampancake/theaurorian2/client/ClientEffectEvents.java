package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import cn.teampancake.theaurorian2.mixin.ClientInputAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ClientEffectEvents {

    private static final float EIDOLON_POISON_FOG_DISTANCE = 15.0F;
    private static final float EIDOLON_POISON_FOG_START_RATIO = 0.75F;
    private static final int EIDOLON_POISON_SHADER_HAZE_MAX_ALPHA = 72;
    private static final int EIDOLON_POISON_HAZE_RGB = 0xF9FCFF;

    private ClientEffectEvents() {
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)
                || !player.hasEffect(ModMobEffects.CONFUSION)) {
            return;
        }

        ClientInput input = event.getInput();
        Input keys = input.keyPresses;
        input.keyPresses = new Input(
                keys.backward(),
                keys.forward(),
                keys.right(),
                keys.left(),
                keys.shift(),
                keys.jump(),
                keys.sprint());
        Vec2 movement = input.getMoveVector();
        ((ClientInputAccessor) input).theaurorian2$setMoveVector(new Vec2(-movement.x, -movement.y));
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(ModMobEffects.DEAFNESS)) {
            return;
        }

        SoundInstance sound = event.getOriginalSound();
        SoundSource source = sound.getSource();
        boolean keep = source == SoundSource.MUSIC
                || source == SoundSource.AMBIENT
                || sound.isRelative();
        if (!keep) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float strength = getEidolonPoisonStrength((float) event.getPartialTick());
        if (strength > 0.0F) {
            event.setRed(Mth.lerp(strength, event.getRed(), 0.976F));
            event.setGreen(Mth.lerp(strength, event.getGreen(), 0.988F));
            event.setBlue(Mth.lerp(strength, event.getBlue(), 1.0F));
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float strength = getEidolonPoisonStrength((float) event.getPartialTick());
        if (strength > 0.0F) {
            float distance = Mth.lerp(
                    strength,
                    event.getFarPlaneDistance(),
                    EIDOLON_POISON_FOG_DISTANCE);
            event.setNearPlaneDistance(distance * EIDOLON_POISON_FOG_START_RATIO);
            event.setFarPlaneDistance(distance);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float strength = getEidolonPoisonStrength(partialTick);
        if (strength <= 0.0F) {
            return;
        }

        var graphics = event.getGuiGraphics();
        int alpha = Mth.clamp(
                Mth.floor(strength * EIDOLON_POISON_SHADER_HAZE_MAX_ALPHA),
                0,
                EIDOLON_POISON_SHADER_HAZE_MAX_ALPHA);
        graphics.fill(
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                alpha << 24 | EIDOLON_POISON_HAZE_RGB);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(ModMobEffects.TREMOR)) {
            return;
        }

        double time = player.tickCount + event.getPartialTick();
        event.setYaw(event.getYaw() + (float) Math.sin(time * 1.75D) * 1.25F);
        event.setPitch(event.getPitch() + (float) Math.sin(time * 2.35D + 0.8D) * 0.9F);
        event.setRoll(event.getRoll() + (float) Math.sin(time * 1.45D + 1.7D) * 1.1F);
    }

    public static boolean shouldHideEntity(Entity entity) {
        return hasCameraEffect(ModMobEffects.SHADOWED_SIGHT);
    }

    public static boolean shouldHideParticles() {
        return hasCameraEffect(ModMobEffects.SHADOWED_SIGHT);
    }

    private static boolean hasCameraEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        return camera instanceof LivingEntity living && living.hasEffect(effect);
    }

    private static float getEidolonPoisonStrength(float partialTick) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (!(camera instanceof LivingEntity living)) {
            return 0.0F;
        }

        MobEffectInstance effect = living.getEffect(ModMobEffects.EIDOLON_POISON);
        return effect == null ? 0.0F : effect.getBlendFactor(living, partialTick);
    }
}
