package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.effect.SpiderSilkGrounding;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.MoverType;

@Mixin(Entity.class)
public abstract class EntitySilkGroundingMixin {
    @Unique private boolean theaurorian2$restoreSilkNoPhysics;

    // Phasing flyers (e.g. Vexes) must land on the floor instead of falling through it.
    @Inject(method = "move", at = @At("HEAD"))
    private void theaurorian2$groundPhasingFlyer(MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        this.theaurorian2$restoreSilkNoPhysics = entity.noPhysics && SpiderSilkGrounding.isGrounded(entity);
        if (this.theaurorian2$restoreSilkNoPhysics) entity.noPhysics = false;
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void theaurorian2$restorePhasingFlag(MoverType type, Vec3 movement, CallbackInfo ci) {
        if (this.theaurorian2$restoreSilkNoPhysics) {
            ((Entity)(Object)this).noPhysics = true;
            this.theaurorian2$restoreSilkNoPhysics = false;
        }
    }

    // Tick events alone cannot stop the Wither's AI adding lift later in the same tick.
    // Constrain only marked entities' movement argument; retain vanilla collision handling.
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 theaurorian2$groundSilkMovement(Vec3 movement) {
        return SpiderSilkGrounding.constrainMovement((Entity)(Object)this, movement);
    }
}
