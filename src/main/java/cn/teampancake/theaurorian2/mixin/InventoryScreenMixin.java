package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.screen.AccessoryInventoryButtonEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    // NeoForge has no event between effect extraction and the inventory's slot extraction.
    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;"
                            + "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
                    shift = At.Shift.AFTER))
    private void theaurorian2$extractAccessoriesAboveEffects(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        graphics.nextStratum();
        AccessoryInventoryButtonEvents.extractAccessoryLayer(
                (InventoryScreen) (Object) this, graphics, mouseX, mouseY);
    }
}
