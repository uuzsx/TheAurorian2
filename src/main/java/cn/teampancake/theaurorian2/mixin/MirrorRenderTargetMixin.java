package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.renderer.mirror.MirrorReflections;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sky and weather ignore RenderSystem output overrides; route only the scoped mirror pass. */
@Mixin(RenderTarget.class)
public abstract class MirrorRenderTargetMixin {
    @Inject(method = "getColorTextureView", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$mirrorColor(CallbackInfoReturnable<GpuTextureView> ci) {
        var target = MirrorReflections.activeTarget();
        if (target != null && target != (Object) this) ci.setReturnValue(target.getColorTextureView());
    }
    @Inject(method = "getDepthTextureView", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$mirrorDepth(CallbackInfoReturnable<GpuTextureView> ci) {
        var target = MirrorReflections.activeTarget();
        if (target != null && target != (Object) this) ci.setReturnValue(target.getDepthTextureView());
    }
}
