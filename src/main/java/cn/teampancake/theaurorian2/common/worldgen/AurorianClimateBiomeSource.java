package cn.teampancake.theaurorian2.common.worldgen;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

/** Uses the vanilla climate partition for mapped biomes and Aurorian ecology inland. */
public final class AurorianClimateBiomeSource extends BiomeSource {
    public static final ResourceKey<Biome> SWAMP = ResourceKey.create(Registries.BIOME, TheAurorian2.id("aurorian_swamp"));
    public static final ResourceKey<Biome> STARBLIGHT_SWAMP = ResourceKey.create(Registries.BIOME, TheAurorian2.id("starblight_swamp"));
    public static final MapCodec<AurorianClimateBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.CODEC.codec().fieldOf("vanilla").forGetter(source -> source.vanilla),
            MultiNoiseBiomeSource.CODEC.codec().fieldOf("land").forGetter(source -> source.land),
            Codec.unboundedMap(Identifier.CODEC, Biome.CODEC).fieldOf("replacements").forGetter(source -> source.replacements),
            RegistryOps.retrieveElement(SWAMP), RegistryOps.retrieveElement(STARBLIGHT_SWAMP)
    ).apply(instance, AurorianClimateBiomeSource::withSwamps));

    private final MultiNoiseBiomeSource vanilla;
    private final MultiNoiseBiomeSource land;
    private final Map<Identifier, Holder<Biome>> replacements;

    /** Upgrade only absent or former shared swamp mappings when a saved source is decoded. */
    public static AurorianClimateBiomeSource withSwamps(MultiNoiseBiomeSource vanilla, MultiNoiseBiomeSource land,
            Map<Identifier, Holder<Biome>> replacements, Holder<Biome> swamp, Holder<Biome> starblight) {
        var updated = new TreeMap<>(replacements);
        updated.compute(Identifier.withDefaultNamespace("swamp"),
                (key, old) -> old == null || old.is(STARBLIGHT_SWAMP) ? swamp : old);
        updated.putIfAbsent(Identifier.withDefaultNamespace("mangrove_swamp"), starblight);
        return new AurorianClimateBiomeSource(vanilla, land, updated);
    }

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
