package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.effect.NaturalHealingContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerNaturalHealingMixin {

    @Inject(method = "tickRegeneration", at = @At("HEAD"))
    private void theaurorian2$beginPeacefulHealing(CallbackInfo ci) {
        NaturalHealingContext.enter();
    }

    @Inject(method = "tickRegeneration", at = @At("RETURN"))
    private void theaurorian2$endPeacefulHealing(CallbackInfo ci) {
        NaturalHealingContext.exit();
    }
}
