package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.world.AurorianBlessingEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityMixin {

    @Inject(
            method = "processDurabilityChange(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("HEAD"),
            cancellable = true)
    private void theaurorian2$protectBlessedEquipment(
            int amount,
            ServerLevel level,
            LivingEntity owner,
            CallbackInfoReturnable<Integer> callback) {
        if (amount > 0
                && owner != null
                && AurorianBlessingEffects.preventsDurabilityLoss((ItemStack) (Object) this, owner)) {
            callback.setReturnValue(0);
        }
    }
}
