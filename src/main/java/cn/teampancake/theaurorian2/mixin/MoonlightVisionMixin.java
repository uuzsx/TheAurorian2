package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.EnchantmentVision;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No viewer-local glow event is exposed; do not mutate shared entity metadata. */
@Mixin(Minecraft.class)
public abstract class MoonlightVisionMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
    private void theaurorian2$moonlightOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && EnchantmentVision.highlighted(entity)) cir.setReturnValue(true);
    }
}
