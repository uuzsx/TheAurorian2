package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.inventory.AccessoryEnhancements;
import cn.teampancake.theaurorian2.common.inventory.AccessorySlot;
import cn.teampancake.theaurorian2.common.inventory.ArtifactRotation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "renderSlotContents", at = @At("HEAD"))
    private void theaurorian2$beginArtifactRotation(
            GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countText, CallbackInfo ci) {
        if (!(slot instanceof AccessorySlot) || !AccessoryEnhancements.isArtifact(stack)) {
            return;
        }
        int quarterTurns = ArtifactRotation.quarterTurns(stack);
        if (quarterTurns == 0) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().rotateAbout(
                quarterTurns * ((float) Math.PI / 2.0F), slot.x + 8.0F, slot.y + 8.0F);
    }

    @Inject(method = "renderSlotContents", at = @At("RETURN"))
    private void theaurorian2$endArtifactRotation(
            GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countText, CallbackInfo ci) {
        if (slot instanceof AccessorySlot
                && AccessoryEnhancements.isArtifact(stack)
                && ArtifactRotation.quarterTurns(stack) != 0) {
            graphics.pose().popMatrix();
        }
    }
}
