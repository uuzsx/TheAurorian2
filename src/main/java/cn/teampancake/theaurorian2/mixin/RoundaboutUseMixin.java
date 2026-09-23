package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.enchantment.RoundaboutThrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Use-start events cannot supply a consistent client duration/pose for arbitrary axe items. */
@Mixin(ItemStack.class)
public abstract class RoundaboutUseMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$axeChargeDuration(LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (RoundaboutThrow.hasThrow((ItemStack) (Object) this)) cir.setReturnValue(RoundaboutThrow.USE_DURATION);
    }
    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$axeChargePose(CallbackInfoReturnable<ItemUseAnimation> cir) {
        if (RoundaboutThrow.hasThrow((ItemStack) (Object) this)) cir.setReturnValue(ItemUseAnimation.TRIDENT);
    }
}
