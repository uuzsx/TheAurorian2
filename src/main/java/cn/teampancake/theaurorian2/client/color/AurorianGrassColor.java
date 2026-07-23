package cn.teampancake.theaurorian2.client.color;

import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ColorMapColorUtil;

public final class AurorianGrassColor {

    public static final double NORMAL_TEMPERATURE = 0.8;
    public static final double NORMAL_DOWNFALL = 0.4;
    private static final int COLOR_MAP_SIZE = 256 * 256;
    private static final int FALLBACK_COLOR = 0xFFFFFFFF;
    private static int[] pixels = createFallbackPixels();

    private AurorianGrassColor() {
    }

    public static void init(int[] pixels) {
        if (pixels.length != COLOR_MAP_SIZE) {
            throw new IllegalArgumentException("Aurorian grass colormap must contain exactly 65536 pixels");
        }

        AurorianGrassColor.pixels = pixels;
    }

    public static int get(double temperature, double downfall) {
        double clampedTemperature = Mth.clamp(temperature, 0.0, 1.0);
        double clampedDownfall = Mth.clamp(downfall, 0.0, 1.0);
        return ColorMapColorUtil.get(clampedTemperature, clampedDownfall, pixels, FALLBACK_COLOR);
    }

    public static int getDefaultColor() {
        return get(NORMAL_TEMPERATURE, NORMAL_DOWNFALL);
    }

    private static int[] createFallbackPixels() {
        int[] fallbackPixels = new int[COLOR_MAP_SIZE];
        Arrays.fill(fallbackPixels, FALLBACK_COLOR);
        return fallbackPixels;
    }
}
