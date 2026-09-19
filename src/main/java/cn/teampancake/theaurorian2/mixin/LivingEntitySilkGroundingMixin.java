package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.effect.SpiderSilkGrounding;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySilkGroundingMixin {
    // NeoForge exposes no cancellable glide-start event; block only this eligibility result.
    @Inject(method = "canGlide", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$preventSilkGliding(CallbackInfoReturnable<Boolean> cir) {
        if (SpiderSilkGrounding.isGrounded((LivingEntity)(Object)this)) cir.setReturnValue(false);
    }
}
