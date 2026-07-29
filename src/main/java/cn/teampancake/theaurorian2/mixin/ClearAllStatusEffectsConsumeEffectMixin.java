package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.effect.EffectRemovalContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public abstract class ClearAllStatusEffectsConsumeEffectMixin {

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean theaurorian2$markMilkRemoval(LivingEntity entity) {
        return EffectRemovalContext.run(EffectRemovalContext.Reason.MILK, entity::removeAllEffects);
    }
}
