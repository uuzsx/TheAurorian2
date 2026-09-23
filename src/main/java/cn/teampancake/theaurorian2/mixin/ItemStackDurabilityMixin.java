package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.world.AurorianBlessingEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityMixin {

    // PlayerDestroyItemEvent does not cover worn armor on all living entities. Observe the
    // final durability decision without replacing damage handling or the break callback.
    @Inject(method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void theaurorian2$forgettingEcho(int newDamage, LivingEntity owner, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!stack.isEmpty() && stack.isDamageableItem() && newDamage >= stack.getMaxDamage())
            cn.teampancake.theaurorian2.common.enchantment.ArmorEnchantmentEvents.armorBreaking(stack, owner);
    }

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
