package cn.teampancake.theaurorian2.mixin;

import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.jspecify.annotations.Nullable;

/** Reuse the decoded resource-pack cloud mask without file I/O or changing the main cloud renderer. */
@Mixin(CloudRenderer.class)
public interface MirrorCloudTextureAccessor {
    @Accessor("texture") CloudRenderer.@Nullable TextureData theaurorian2$cloudTexture();
}
