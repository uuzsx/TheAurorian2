package cn.teampancake.theaurorian2.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;

/** A local forest lake landscape, composed before native caves and surface generation. */
public final class LakeIslandDensity implements DensityFunction.SimpleFunction {
    private static final Codec<Mode> MODE_CODEC = Codec.STRING.xmap(
            value -> Mode.valueOf(value.toUpperCase(Locale.ROOT)), value -> value.name().toLowerCase(Locale.ROOT));
    public static final MapCodec<LakeIslandDensity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(value -> value.input),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continents").forGetter(value -> value.fields.continents()),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(value -> value.fields.temperature()),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("humidity").forGetter(value -> value.fields.humidity()),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("ridges").forGetter(value -> value.fields.ridges()),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("baseline_terrain").forGetter(value -> value.fields.baselineTerrain()),
            MODE_CODEC.fieldOf("mode").forGetter(value -> value.mode)
    ).apply(instance, LakeIslandDensity::new));
    private static final KeyDispatchDataCodec<LakeIslandDensity> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);
    private final DensityFunction input;
    private final LakeIslandLayout.Fields fields;
    private final Mode mode;
    private final LakeIslandLayout layout;
    private final boolean reusableLayout;

    public LakeIslandDensity(DensityFunction input, DensityFunction continents, DensityFunction temperature,
                             DensityFunction humidity, DensityFunction ridges, DensityFunction baselineTerrain, Mode mode) {
        this.input = input;
        this.fields = new LakeIslandLayout.Fields(continents, temperature, humidity, ridges, baselineTerrain);
        this.mode = mode;
        this.layout = new LakeIslandLayout(fields);
        this.reusableLayout = false;
    }

    private LakeIslandDensity(DensityFunction input, LakeIslandLayout.Fields fields, Mode mode,
                              LakeIslandLayout layout, boolean reusableLayout) {
        this.input = input;
        this.fields = fields;
        this.mode = mode;
        this.layout = layout;
        this.reusableLayout = reusableLayout;
    }

    @Override
    public double compute(FunctionContext context) {
        LakeIslandLayout.Sample column = layout.sample(context.blockX(), context.blockZ());
        if (column.weight() == 0) return input.compute(context);
        double original = input.compute(context);
        double water = column.waterWeight();
        switch (mode) {
            case LANDSCAPE -> {
                // Shape unsqueezed terrain, before cave cutting, slides and old-world
                // blending. Deep underground and the outer landscape remain native.
                double amount = column.terrainWeight() * LakeIslandLayout.smooth((context.blockY() - 24) / 24.0);
                if (amount == 0) return original;
                double shaped = (column.surfaceHeight() - context.blockY()) * 0.2;
                return original + (shaped - original) * amount;
            }
            case SURFACE -> {
                // The aquifer and surface painter must see the same new shore height.
                if (column.terrainWeight() == 0) return original;
                return original + (column.surfaceHeight() + 2 - original) * column.terrainWeight();
            }
            case ISLAND_WEIGHT -> { return column.islandWeight(); }
            case CONTINENTS -> {
                double land = original + (Math.max(original, 0.10) - original) * column.islandWeight();
                return land + (-0.85 - land) * water;
            }
            case EROSION -> {
                // High native erosion selects plains instead of mountain splines.
                return original + (0.75 - original) * column.islandWeight();
            }
            case RIDGES_FOLDED -> {
                // Retain seed-dependent relief without tall peaks or broad river troughs.
                double plains = 0.58 + 0.10 * Math.clamp(original, -1, 1);
                return original + (plains - original) * column.islandWeight();
            }
            case TERRAIN -> {
                if (water == 0 || column.terrainWeight() < 0.999) return original;
                // Seal only the shallow lake bed. Keep the native cave graph beneath
                // it rather than filling the entire underground with a height field.
                double depth = column.surfaceHeight() - context.blockY();
                if (depth <= 0 || depth >= 7) return original;
                double shell = LakeIslandLayout.smooth(depth / 2) * LakeIslandLayout.smooth((7 - depth) / 3);
                return Math.max(original, 0.075 * shell);
            }
            case FLOOD -> {
                if (!nearLakeWater(context, column)) return original;
                return original + (1 - original) * LakeIslandLayout.smooth(water / 0.18);
            }
            case DEPTH -> {
                if (!nearLakeWater(context, column)) return original;
                return original + (Math.min(original, 0) - original) * LakeIslandLayout.smooth(water / 0.18);
            }
            default -> throw new IllegalStateException();
        }
    }

    private static boolean nearLakeWater(FunctionContext context, LakeIslandLayout.Sample column) {
        // Aquifer centers lie on a 12-block lattice with additional random offsets.
        // Leave enough vertical margin for neighboring centers, but not deep caves.
        return column.waterWeight() > 0 && context.blockY() >= Math.max(24, column.surfaceHeight() - 28)
                && context.blockY() <= LakeIslandLayout.WATER_LEVEL + 24;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        LayoutMapping mapping = new LayoutMapping(visitor);
        var mapped = fields.mapAll(mapping);
        // Chunk-local transformations of the terrain input do not change site eligibility.
        DensityFunction mappedInput = input.mapAll(visitor);
        boolean seeded = mapping.hasSeededNoise && !mapping.hasUnseededNoise;
        boolean reuse = reusableLayout && seeded && !mapping.seedChanged && !mapping.semanticChange;
        boolean shareable = reuse || seeded && mapping.seedChanged && !mapping.semanticChange;
        // Retain the RandomState-era snapshot, never a NoiseChunk's mutable caches.
        // Resource codecs start unseeded; a different seed or semantic visitor forks it.
        LakeIslandLayout nextLayout = reuse ? layout : new LakeIslandLayout(mapped.readOnly());
        return visitor.apply(new LakeIslandDensity(mappedInput, mapped, mode, nextLayout, shareable));
    }

    public LakeIslandLayout layout() { return layout; }
    public boolean isDepth() { return mode == Mode.DEPTH; }

    // Native spline visitors canonicalize equivalent density nodes. Cache state is
    // incidental; seeded noise objects in the fields keep different worlds apart.
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof LakeIslandDensity density && mode == density.mode
                && input.equals(density.input) && fields.equals(density.fields);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * input.hashCode() + fields.hashCode()) + mode.hashCode();
    }

    private static final class LayoutMapping implements Visitor {
        private final Visitor delegate;
        private boolean hasSeededNoise;
        private boolean hasUnseededNoise;
        private boolean seedChanged;
        private boolean semanticChange;

        private LayoutMapping(Visitor delegate) { this.delegate = delegate; }

        @Override
        public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noise) {
            DensityFunction.NoiseHolder mapped = delegate.visitNoise(noise);
            hasSeededNoise |= mapped.noise() != null;
            hasUnseededNoise |= mapped.noise() == null;
            seedChanged |= mapped.noise() != noise.noise();
            semanticChange |= !mapped.noiseData().equals(noise.noiseData());
            return mapped;
        }

        @Override
        public DensityFunction apply(DensityFunction function) {
            DensityFunction mapped = delegate.apply(function);
            if (mapped == function || mapped.equals(function)) return mapped;
            if (function instanceof BlendedNoise && mapped instanceof BlendedNoise) {
                seedChanged = true;
                return mapped;
            }
            if (function instanceof DensityFunctions.HolderHolder holder) {
                if (Objects.equals(mapped, holder.function().value())) return mapped;
                if (mapped instanceof DensityFunctions.HolderHolder mappedHolder
                        && Objects.equals(mappedHolder.function().value(), holder.function().value())) return mapped;
            }
            if (function instanceof DensityFunctions.MarkerOrMarked marker) {
                if (Objects.equals(mapped, marker.wrapped())) return mapped;
                if (mapped instanceof DensityFunctions.MarkerOrMarked marked
                        && marked.type() == marker.type() && Objects.equals(marked.wrapped(), marker.wrapped())) return mapped;
            }
            semanticChange = true;
            return mapped;
        }
    }

    @Override public double minValue() {
        return switch (mode) {
            case LANDSCAPE -> Math.min(input.minValue(), -4096);
            case SURFACE -> Math.min(input.minValue(), 48);
            case ISLAND_WEIGHT -> Math.min(input.minValue(), 0);
            case CONTINENTS -> Math.min(input.minValue(), -0.85);
            case EROSION -> Math.min(input.minValue(), 0.75);
            case RIDGES_FOLDED -> Math.min(input.minValue(), 0.48);
            case TERRAIN -> input.minValue();
            case FLOOD -> Math.min(input.minValue(), 1);
            case DEPTH -> Math.min(input.minValue(), 0);
        };
    }
    @Override public double maxValue() {
        return switch (mode) {
            case LANDSCAPE -> Math.max(input.maxValue(), 4096);
            case SURFACE -> Math.max(input.maxValue(), 84);
            case ISLAND_WEIGHT -> Math.max(input.maxValue(), 1);
            case CONTINENTS -> Math.max(input.maxValue(), 0.10);
            case EROSION -> Math.max(input.maxValue(), 0.75);
            case RIDGES_FOLDED -> Math.max(input.maxValue(), 0.68);
            case FLOOD -> Math.max(input.maxValue(), 1);
            case TERRAIN -> Math.max(input.maxValue(), 0.075);
            default -> input.maxValue();
        };
    }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() { return KEY_CODEC; }
    public enum Mode { CONTINENTS, EROSION, RIDGES_FOLDED, ISLAND_WEIGHT, LANDSCAPE, SURFACE, TERRAIN, FLOOD, DEPTH }
}
