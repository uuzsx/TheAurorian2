package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.enchantment.EnchantmentRules;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** No exhaustion event carries its cause. Modify only movement/jump call sites. */
@Mixin(ServerPlayer.class)
public abstract class WindRunnerMixin {
    @ModifyArg(method = {"checkMovementStatistics", "jumpFromGround"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"), index = 0)
    private float theaurorian2$walkingExhaustion(float amount) {
        return EnchantmentRules.level((ServerPlayer) (Object) this, EquipmentSlot.FEET, ModEnchantments.WIND_RUNNER) > 0 ? amount * 0.5F : amount;
    }
}
