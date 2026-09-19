package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class RunestoneDungeonStructure extends Structure {
    public static final MapCodec<RunestoneDungeonStructure> CODEC = simpleCodec(RunestoneDungeonStructure::new);
    private static final ResourceKey<StructureSet> SET = ResourceKey.create(
            Registries.STRUCTURE_SET, TheAurorian2.id("runestone_dungeons"));
    public static final int HEIGHT = 236;
    private static final Identifier PLAINS_HILLS = TheAurorian2.id("aurorian_plains_hills");

    public RunestoneDungeonStructure(StructureSettings settings) { super(settings); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var placement = context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(SET).value().placement();
        if (!(placement instanceof RunestoneDungeonPlacement dungeonPlacement)) return Optional.empty();
        var fragment = dungeonPlacement.anchorFor(context.seed(), context.chunkPos().x(), context.chunkPos().z());
        if (fragment == null) return Optional.empty();
        ChunkPos center = fragment.center();
        int x = center.getMiddleBlockX(), z = center.getMiddleBlockZ();
        if (!isPlains(context, x, 80, z)) return Optional.empty();
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(context.seed(), center.x(), center.z(), 719382451);
        Rotation rotation = Rotation.values()[random.nextInt(4)];
        Optional<Integer> ground = groundHeight(context, x, z, rotation);
        if (ground.isEmpty()) return Optional.empty();
        BlockPos anchor = new BlockPos(x, ground.get(), z);
        BlockPos localFragment = new BlockPos(fragment.column() - 1, 0, fragment.row() - 1)
                .rotate(RunestoneDungeonTerrain.inverse(rotation));
        int column = localFragment.getX() + 1, row = localFragment.getZ() + 1;
        // All nine starts test the same surface biome and terrain, including pieces far above ground.
        return Optional.of(new GenerationStub(anchor.above(), builder -> builder.addPiece(
                new RunestoneDungeonPiece(context.structureTemplateManager(), anchor, rotation, column, row))));
    }

    private static Optional<Integer> groundHeight(GenerationContext context, int x, int z, Rotation rotation) {
        int[] heights = new int[144];
        int count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        BlockPos anchor = new BlockPos(x, 0, z);
        for (int localX = 58; localX <= 227; localX += 16) {
            for (int localZ = 66; localZ <= 237; localZ += 16) {
                if (RunestoneDungeonTerrain.distance(localX, localZ) >= RunestoneDungeonTerrain.BLEND) continue;
                BlockPos pos = RunestoneDungeonTerrain.worldPosition(anchor, rotation, localX, 0, localZ);
                int height = context.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(),
                        Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                var biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(pos.getX()),
                        QuartPos.fromBlock(height + 4), QuartPos.fromBlock(pos.getZ()), context.randomState().sampler());
                if (height <= context.chunkGenerator().getSeaLevel()
                        || (!context.validBiome().test(biome) && !biome.is(PLAINS_HILLS))) {
                    return Optional.empty();
                }
                min = Math.min(min, height);
                max = Math.max(max, height);
                if (max - min > 24) return Optional.empty();
                heights[count++] = height;
            }
        }
        if (count == 0) return Optional.empty();
        Arrays.sort(heights, 0, count);
        int base = heights[count / 2];
        if (base + HEIGHT > context.heightAccessor().getMaxY()
                || base - RunestoneDungeonTerrain.DEPTH < context.heightAccessor().getMinY()
                || base - min > 12 || max - base > 12) return Optional.empty();
        return Optional.of(base);
    }

    private static boolean isPlains(GenerationContext context, int x, int y, int z) {
        return context.validBiome().test(context.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler()));
    }

    @Override
    public StructureType<?> type() { return ModStructures.RUNESTONE_DUNGEON.get(); }
}
