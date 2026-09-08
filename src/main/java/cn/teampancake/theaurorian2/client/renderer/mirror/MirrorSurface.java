package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

/** Texture manager owns the color alias and the depth target together. */
final class MirrorSurface extends AbstractTexture {
    final Identifier id;
    final TextureTarget target;
    final MirrorCloudRenderer clouds;
    MirrorPlane plane;
    long updated;
    boolean ready;
    private boolean closed;
    MirrorSurface(int index, int width) {
        id = TheAurorian2.id("dynamic/long_mirror_" + index);
        target = new TextureTarget("Aurorian mirror " + index, width, width * 2, true);
        texture = target.getColorTexture(); textureView = target.getColorTextureView();
        sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        clouds = new MirrorCloudRenderer();
    }
    @Override public void close() {
        if (!closed) { closed = true; clouds.close(); target.destroyBuffers(); texture = null; textureView = null; }
    }
}
