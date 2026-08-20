package cn.teampancake.theaurorian2.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MoonShieldData(boolean purified, boolean crimson, int crimsonLevel, float shield) {

    public static final float MAX_SHIELD = 10.0F;
    public static final float MAX_CRIMSON_SHIELD = 20.0F;
    public static final int MAX_CRIMSON_LEVEL = 2;
    public static final MoonShieldData EMPTY = new MoonShieldData(false, false, 0, 0.0F);
    public static final Codec<MoonShieldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("purified", false).forGetter(MoonShieldData::purified),
                    Codec.BOOL.optionalFieldOf("crimson", false).forGetter(MoonShieldData::crimson),
                    Codec.INT.optionalFieldOf("crimson_level", 0).forGetter(MoonShieldData::crimsonLevel),
                    Codec.FLOAT.optionalFieldOf("shield", 0.0F).forGetter(MoonShieldData::shield))
            .apply(instance, MoonShieldData::new));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MoonShieldData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MoonShieldData::purified,
                    ByteBufCodecs.BOOL,
                    MoonShieldData::crimson,
                    ByteBufCodecs.VAR_INT,
                    MoonShieldData::crimsonLevel,
                    ByteBufCodecs.FLOAT,
                    MoonShieldData::shield,
                    MoonShieldData::new);

    public MoonShieldData(boolean purified, float shield) {
        this(purified, false, 0, shield);
    }

    public MoonShieldData {
        crimsonLevel = Math.clamp(crimsonLevel, 0, MAX_CRIMSON_LEVEL);
        float maxShield = crimson && purified ? maxCrimsonShield(crimsonLevel) : MAX_SHIELD;
        shield = Float.isFinite(shield) ? Math.clamp(shield, 0.0F, maxShield) : 0.0F;
        if (!purified) {
            crimson = false;
            crimsonLevel = 0;
            shield = 0.0F;
        }
    }

    public static MoonShieldData active() {
        return new MoonShieldData(true, false, 0, MAX_SHIELD);
    }

    public MoonShieldData withShield(float amount) {
        return new MoonShieldData(this.purified, this.crimson, this.crimsonLevel, amount);
    }

    public MoonShieldData withMode(boolean crimson, int crimsonLevel) {
        return new MoonShieldData(this.purified, crimson, crimsonLevel, this.shield);
    }

    public float maxShield() {
        return crimson ? maxCrimsonShield(crimsonLevel) : MAX_SHIELD;
    }

    public static float maxCrimsonShield(int level) {
        return MAX_SHIELD + Math.clamp(level, 0, MAX_CRIMSON_LEVEL) * 5.0F;
    }
}
