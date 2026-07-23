package cn.teampancake.theaurorian2.client.resource;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.color.AurorianGrassColor;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class AurorianGrassColorReloadListener extends SimplePreparableReloadListener<int[]> {

    private static final Identifier LOCATION = TheAurorian2.id("textures/colormap/aurorian_grass.png");
    private static final int COLOR_MAP_SIZE = 256;

    @Override
    protected int[] prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (InputStream resource = manager.open(LOCATION); NativeImage image = NativeImage.read(resource)) {
            if (image.getWidth() != COLOR_MAP_SIZE || image.getHeight() != COLOR_MAP_SIZE) {
                throw new IllegalStateException("Aurorian grass colormap must be 256x256 pixels");
            }

            return image.getPixels();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load Aurorian grass colormap", exception);
        }
    }

    @Override
    protected void apply(int[] pixels, ResourceManager manager, ProfilerFiller profiler) {
        AurorianGrassColor.init(pixels);
    }
}
