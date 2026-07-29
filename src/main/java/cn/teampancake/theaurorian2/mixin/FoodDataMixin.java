package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.effect.NaturalHealingContext;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void theaurorian2$beginNaturalHealing(ServerPlayer player, CallbackInfo ci) {
        NaturalHealingContext.enter();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void theaurorian2$endNaturalHealing(ServerPlayer player, CallbackInfo ci) {
        NaturalHealingContext.exit();
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
    private void theaurorian2$preventPressureRegenerationExhaustion(
            FoodData foodData, float exhaustion, ServerPlayer player) {
        if (!player.hasEffect(ModMobEffects.PRESSURE)) {
            foodData.addExhaustion(exhaustion);
        }
    }
}
