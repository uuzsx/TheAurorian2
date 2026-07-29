package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.ClientEffectEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$hideParticles(
            ParticlesRenderState renderState,
            Frustum frustum,
            Camera camera,
            float partialTick,
            CallbackInfo ci) {
        if (ClientEffectEvents.shouldHideParticles()) {
            ci.cancel();
        }
    }
}
