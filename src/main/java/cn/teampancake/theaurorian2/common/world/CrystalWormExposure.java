package cn.teampancake.theaurorian2.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Online underground time; offline time never advances the encounter timer. */
public record CrystalWormExposure(int seconds, int attemptInSeconds) {
    public static final int MAX_SECONDS = 20 * 60;
    public static final CrystalWormExposure EMPTY = new CrystalWormExposure(0, 60);
    public static final Codec<CrystalWormExposure> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, MAX_SECONDS).optionalFieldOf("seconds", 0).forGetter(CrystalWormExposure::seconds),
            Codec.intRange(0, 180).optionalFieldOf("attempt_in", 60).forGetter(CrystalWormExposure::attemptInSeconds)
    ).apply(i, CrystalWormExposure::new));
}
