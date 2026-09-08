package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.renderer.mirror.MirrorReflections;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge has no event covering both client block and light mesh invalidation behind the camera. */
@Mixin(LevelRenderer.class)
public abstract class MirrorSceneInvalidationMixin {
    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    private void theaurorian2$invalidateMirrorSection(int x, int y, int z, boolean changedByPlayer, CallbackInfo ci) {
        MirrorReflections.invalidateSection(x, y, z);
    }
    @Inject(method = "allChanged", at = @At("HEAD"))
    private void theaurorian2$resetMirrorResources(CallbackInfo ci) { MirrorReflections.requestReset(); }
}
