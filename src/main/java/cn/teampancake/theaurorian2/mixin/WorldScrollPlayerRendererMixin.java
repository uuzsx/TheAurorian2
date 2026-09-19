package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.WorldScrollTeleportClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class WorldScrollPlayerRendererMixin {
    // The public render-state API only provides uniform scale. Apply the authored
    // X/Z collapse inside vanilla's balanced pose stack; Pre/Post events cannot
    // safely bracket a push/pop if another mod cancels player rendering.
    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
    private void theaurorian2$teleportWidth(AvatarRenderState state, PoseStack pose, CallbackInfo ci) {
        Float width = state.getRenderData(WorldScrollTeleportClient.PLAYER_WIDTH);
        if (width != null && width < 1) pose.scale(Math.max(width, 0.001F), 1, Math.max(width, 0.001F));
    }
}
