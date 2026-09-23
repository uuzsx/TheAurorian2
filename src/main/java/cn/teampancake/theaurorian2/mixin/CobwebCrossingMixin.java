package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.enchantment.EnchantmentRules;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** WebBlock has no per-entity speed event; leave every other sticky block untouched. */
@Mixin(WebBlock.class)
public abstract class CobwebCrossingMixin {
    @Redirect(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"))
    private void theaurorian2$crossWeb(Entity entity, BlockState state, Vec3 multiplier) {
        if (!(entity instanceof LivingEntity living) || EnchantmentRules.level(living, EquipmentSlot.FEET, ModEnchantments.COBWEB_CROSSING) == 0)
            entity.makeStuckInBlock(state, multiplier);
    }
}
