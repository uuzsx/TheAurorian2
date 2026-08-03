package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathParticleMixin {

    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$replaceMarkedDeathParticles(byte eventId, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (eventId == 60 && entity.getData(ModAttachments.PHANTOM_BLOSSOM_DEATH_EFFECT)) {
            ci.cancel();
        }
    }
}
