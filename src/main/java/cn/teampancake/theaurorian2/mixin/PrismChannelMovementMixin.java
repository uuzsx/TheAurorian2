package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.item.ArcanePrismChannel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Tick/input events cannot prevent gravity, knockback or network movement between ticks.
 * Constrain only the movement argument of a player actively channeling the prism. */
@Mixin(Entity.class)
public abstract class PrismChannelMovementMixin {
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 theaurorian2$holdPrismChannel(Vec3 movement) {
        Entity entity = (Entity)(Object)this;
        if (!ArcanePrismChannel.isHolding(entity)) return movement;
        entity.setDeltaMovement(Vec3.ZERO);
        entity.resetFallDistance();
        return Vec3.ZERO;
    }
}
