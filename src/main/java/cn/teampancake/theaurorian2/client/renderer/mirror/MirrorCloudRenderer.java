package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.mixin.MirrorCloudTextureAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.util.profiling.Profiler;
import java.util.Optional;

/** A separate cloud mesh for each mirror, sharing only immutable, already-decoded texture data. */
final class MirrorCloudRenderer extends CloudRenderer {
    MirrorCloudRenderer() {
        var mc = Minecraft.getInstance();
        var texture = ((MirrorCloudTextureAccessor) mc.levelRenderer.getCloudRenderer()).theaurorian2$cloudTexture();
        apply(Optional.ofNullable(texture), mc.getResourceManager(), Profiler.get());
    }
}
