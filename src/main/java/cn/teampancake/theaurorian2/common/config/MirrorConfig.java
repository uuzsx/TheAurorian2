package cn.teampancake.theaurorian2.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MirrorConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue MAX_MIRRORS;
    public static final ModConfigSpec.IntValue RESOLUTION;
    public static final ModConfigSpec.IntValue FPS;
    public static final ModConfigSpec.IntValue DISTANCE;
    static {
        var builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Render nearby long mirror reflections; false keeps the original static glass.")
                .define("enabled", true);
        MAX_MIRRORS = builder.comment("Maximum simultaneously visible reflected mirrors. Others retain static glass.")
                .defineInRange("maxMirrors", 2, 1, 4);
        RESOLUTION = builder.comment("Reflection width in pixels; height is twice this value.")
                .defineInRange("resolution", 256, 64, 512);
        FPS = builder.comment("Maximum reflection updates per second.").defineInRange("fps", 30, 5, 60);
        DISTANCE = builder.comment("Scene radius in blocks. Mirrors activate within eight blocks.")
                .defineInRange("sceneDistance", 16, 8, 24);
        SPEC = builder.build();
    }
    private MirrorConfig() {}
}
