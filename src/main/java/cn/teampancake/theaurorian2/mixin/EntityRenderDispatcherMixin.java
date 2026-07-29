package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.ClientEffectEvents;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void theaurorian2$hideEntities(
            E entity,
            Frustum culler,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfoReturnable<Boolean> cir) {
        if (ClientEffectEvents.shouldHideEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
