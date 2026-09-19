package cn.teampancake.theaurorian2.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public final class LakeIslandBiomeSource extends BiomeSource {

    public static final MapCodec<LakeIslandBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("delegate").forGetter(source -> source.delegate),
            Biome.CODEC.fieldOf("island").forGetter(source -> source.island),
            Biome.CODEC.fieldOf("lake").forGetter(source -> source.lake),
            Biome.CODEC.fieldOf("silent_forest").forGetter(source -> source.silentForest),
            Biome.CODEC.fieldOf("curtain_forest").forGetter(source -> source.curtainForest)
    ).apply(instance, LakeIslandBiomeSource::new));

    private final BiomeSource delegate;
    private final Holder<Biome> island;
    private final Holder<Biome> lake;
    private final Holder<Biome> silentForest;
    private final Holder<Biome> curtainForest;
    private final ThreadLocal<LastLayout> lastLayout = new ThreadLocal<>();

    public LakeIslandBiomeSource(BiomeSource delegate, Holder<Biome> island, Holder<Biome> lake,
                                Holder<Biome> silentForest, Holder<Biome> curtainForest) {
        this.delegate = delegate;
        this.island = island;
        this.lake = lake;
        this.silentForest = silentForest;
        this.curtainForest = curtainForest;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(this.delegate.possibleBiomes().stream(),
                Stream.of(this.island, this.lake, this.silentForest, this.curtainForest));
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        if (quartY < 0) {
            return this.delegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
        }
        LakeIslandLayout.Sample sample = this.layout(sampler).sample(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ));
        return switch (sample.zone()) {
            case NONE -> this.delegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
            case ISLAND -> this.island;
            case LAKE -> this.lake;
            case FOREST -> sample.site().curtainForest() ? this.curtainForest : this.silentForest;
        };
    }

    public LakeIslandLayout layout(Climate.Sampler sampler) {
        LastLayout cached = this.lastLayout.get();
        // A sampler owns seeded noise; identity prevents reusing a layout across worlds.
        if (cached == null || cached.sampler() != sampler) {
            DensityFunction depth = sampler.depth();
            if (depth instanceof DensityFunctions.HolderHolder holder) depth = holder.function().value();
            if (!(depth instanceof LakeIslandDensity owner) || !owner.isDepth()) {
                throw new IllegalStateException("Lake-island biome source requires its seeded depth density function");
            }
            cached = new LastLayout(sampler, owner.layout());
            this.lastLayout.set(cached);
        }
        return cached.layout();
    }

    private record LastLayout(Climate.Sampler sampler, LakeIslandLayout layout) {
    }
}
