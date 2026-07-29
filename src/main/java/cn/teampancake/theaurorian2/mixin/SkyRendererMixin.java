package cn.teampancake.theaurorian2.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    private static final Identifier AURORIAN_DIMENSION = Identifier.fromNamespaceAndPath(
            "theaurorian2", "the_aurorian");
    private static final float AURORIAN_MOON_SIZE = 26.0F;

    @ModifyConstant(method = "renderMoon", constant = @Constant(floatValue = 20.0F), require = 2)
    private float theaurorian2$enlargeAurorianMoon(float originalSize) {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null && level.dimension().identifier().equals(AURORIAN_DIMENSION)
                ? AURORIAN_MOON_SIZE
                : originalSize;
    }
}
