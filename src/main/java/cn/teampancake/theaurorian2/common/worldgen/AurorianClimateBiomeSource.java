package cn.teampancake.theaurorian2.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

/** Uses the vanilla climate partition for mapped biomes and Aurorian ecology inland. */
public final class AurorianClimateBiomeSource extends BiomeSource {
    public static final MapCodec<AurorianClimateBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.CODEC.codec().fieldOf("vanilla").forGetter(source -> source.vanilla),
            MultiNoiseBiomeSource.CODEC.codec().fieldOf("land").forGetter(source -> source.land),
            Codec.unboundedMap(Identifier.CODEC, Biome.CODEC).fieldOf("replacements").forGetter(source -> source.replacements)
    ).apply(instance, AurorianClimateBiomeSource::new));

    private final MultiNoiseBiomeSource vanilla;
    private final MultiNoiseBiomeSource land;
    private final Map<Identifier, Holder<Biome>> replacements;

    public AurorianClimateBiomeSource(MultiNoiseBiomeSource vanilla, MultiNoiseBiomeSource land,
                                     Map<Identifier, Holder<Biome>> replacements) {
        this.vanilla = vanilla;
        this.land = land;
        // Keep feature sorting stable across JVM runs, including old-save reloads.
        this.replacements = Collections.unmodifiableMap(new TreeMap<>(replacements));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() { return CODEC; }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(this.land.possibleBiomes().stream(), this.replacements.values().stream());
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        return select(sampler.sample(quartX, quartY, quartZ));
    }

    public Holder<Biome> select(Climate.TargetPoint climate) {
        Holder<Biome> original = this.vanilla.getNoiseBiome(climate);
        Holder<Biome> replacement = this.replacements.get(original.unwrapKey().orElseThrow().identifier());
        return replacement != null ? replacement : this.land.getNoiseBiome(climate);
    }
}
