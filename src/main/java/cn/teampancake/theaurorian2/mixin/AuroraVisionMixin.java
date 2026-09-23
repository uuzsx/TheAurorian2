package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.EnchantmentVision;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge exposes no lightmap intensity event. Touch only the local render state after extraction. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class AuroraVisionMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void theaurorian2$gentleVision(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (state.needsUpdate) state.nightVisionEffectIntensity = Math.max(state.nightVisionEffectIntensity, EnchantmentVision.auroraIntensity());
    }
}
