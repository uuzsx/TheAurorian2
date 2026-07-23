package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class AurorianSkyIslandPiece extends StructurePiece {

    private static final int GROUP_RADIUS = 124;
    private static final int MAX_BOUNDING_Y = 232;
    private static final int CRATER_BIOME_DEPTH = 32;
    private static final int CRATER_BIOME_HEIGHT = 24;
    private static final int CELL_SIZE = 4;
    private static final long ORE_CELL_CHANCE = 360L;
    private static final ResourceKey<Biome> WASTE_STONE_CRATER_BIOME = ResourceKey.create(
            Registries.BIOME, TheAurorian2.id("waste_stone_crater"));
    private static final TagKey<Block> AURORIAN_ORES = TagKey.create(
            Registries.BLOCK, TheAurorian2.id("aurorian_ores"));

    private final int centerX;
    private final int centerZ;
    private final int groundY;
    private final int topY;
    private final long shapeSeed;

    public AurorianSkyIslandPiece(int centerX, int centerZ, int groundY, int topY, long shapeSeed) {
        super(
                ModStructures.AURORIAN_SKY_ISLAND_GROUP_PIECE.get(),
                0,
                createBoundingBox(centerX, centerZ, groundY));
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.groundY = groundY;
        this.topY = topY;
        this.shapeSeed = shapeSeed;
    }

    public AurorianSkyIslandPiece(CompoundTag tag) {
        super(ModStructures.AURORIAN_SKY_ISLAND_GROUP_PIECE.get(), tag);
        this.centerX = tag.getIntOr("CenterX", this.boundingBox.getCenter().getX());
        this.centerZ = tag.getIntOr("CenterZ", this.boundingBox.getCenter().getZ());
        this.groundY = tag.getIntOr("GroundY", 64);
        this.topY = tag.getIntOr("TopY", 218);
        this.shapeSeed = tag.getLongOr("ShapeSeed", 0L);
    }

    private static BoundingBox createBoundingBox(int centerX, int centerZ, int groundY) {
        return new BoundingBox(
                centerX - GROUP_RADIUS,
                groundY - CRATER_BIOME_DEPTH - 4,
                centerZ - GROUP_RADIUS,
                centerX + GROUP_RADIUS,
                MAX_BOUNDING_Y,
                centerZ + GROUP_RADIUS);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CenterX", this.centerX);
        tag.putInt("CenterZ", this.centerZ);
        tag.putInt("GroundY", this.groundY);
        tag.putInt("TopY", this.topY);
        tag.putLong("ShapeSeed", this.shapeSeed);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos referencePos) {
        BlockState stone = ModBlocks.AURORIAN_STONE.get().defaultBlockState();
        BlockState dirt = ModBlocks.AURORIAN_DIRT.get().defaultBlockState();
        BlockState islandGrass = ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get().defaultBlockState();
        BlockState geodeOre = ModBlocks.GEODE_ORE.get().defaultBlockState();
        BlockState moonDew = ModBlocks.MOON_DEW_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        List<Island> islands = this.createIslands();
        Crater crater = this.createCrater(islands.getFirst());

        this.carveWasteStoneCrater(level, chunkBox, crater, stone, pos);
        this.paintWasteStoneCraterBiome(level, chunkPos, crater);

        for (Island island : islands) {
            int minX = Math.max(chunkBox.minX(), island.centerX() - island.radiusX() - 3);
            int maxX = Math.min(chunkBox.maxX(), island.centerX() + island.radiusX() + 3);
            int minZ = Math.max(chunkBox.minZ(), island.centerZ() - island.radiusZ() - 3);
            int maxZ = Math.min(chunkBox.maxZ(), island.centerZ() + island.radiusZ() + 3);

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double edge = island.edgeDistance(x, z);
                    if (edge > 1.0) {
                        continue;
                    }

                    int surfaceWave = (int) Math.round(
                            0.65 * Math.sin((x + island.phaseA() * 19.0) * 0.16)
                                    + 0.55 * Math.cos((z - island.phaseB() * 23.0) * 0.14));
                    int solidTop = island.topY()
                            - (int) Math.round(Math.pow(edge, 2.5) * (island.basin() ? 4.0 : 3.0))
                            + surfaceWave;
                    int waterY = Integer.MIN_VALUE;
                    boolean lakeFloor = false;

                    if (island.basin()) {
                        double lakeDistance = island.lakeDistance(x, z);
                        if (lakeDistance <= 1.0) {
                            int lakeWave = (int) Math.round(0.45 * Math.sin(x * 0.21 + z * 0.13));
                            solidTop = Math.min(
                                    solidTop,
                                    island.topY() - 7 + (int) Math.round(lakeDistance * 2.0) + lakeWave);
                            waterY = island.topY() - 3;
                            lakeFloor = true;
                        }
                    }

                    int thickness = island.basin()
                            ? 7 + (int) Math.round((1.0 - edge) * 22.0)
                            : 4 + (int) Math.round((1.0 - edge) * 13.0);
                    int bottomY = solidTop - thickness;
                    int dirtDepth = 3 + Math.floorMod(columnHash(x, z), 3);

                    for (int y = bottomY; y <= solidTop; y++) {
                        BlockState state;
                        if (!lakeFloor && y == solidTop) {
                            state = islandGrass;
                        } else if (y >= solidTop - dirtDepth) {
                            state = dirt;
                        } else {
                            state = this.isGeodeOre(x, y, z) ? geodeOre : stone;
                        }

                        level.setBlock(pos.set(x, y, z), state, 2);
                    }

                    if (lakeFloor) {
                        for (int y = solidTop + 1; y <= waterY; y++) {
                            level.setBlock(pos.set(x, y, z), moonDew, 2);
                        }
                    }
                }
            }
        }
    }

    private void carveWasteStoneCrater(
            WorldGenLevel level,
            BoundingBox chunkBox,
            Crater crater,
            BlockState stone,
            BlockPos.MutableBlockPos pos) {
        int minX = Math.max(chunkBox.minX(), crater.centerX() - crater.radiusX() - 3);
        int maxX = Math.min(chunkBox.maxX(), crater.centerX() + crater.radiusX() + 3);
        int minZ = Math.max(chunkBox.minZ(), crater.centerZ() - crater.radiusZ() - 3);
        int maxZ = Math.min(chunkBox.maxZ(), crater.centerZ() + crater.radiusZ() + 3);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double edge = crater.edgeDistance(x, z);
                if (edge > 1.0) {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                int floorY = Math.max(level.getMinY() + 1, surfaceY - crater.depthAt(x, z, edge));
                BlockState exposedState = level.getBlockState(pos.set(x, floorY, z));
                if (!exposedState.is(AURORIAN_ORES)) {
                    level.setBlock(pos, stone, 2);
                }

                int clearTopY = Math.min(level.getMaxY(), surfaceY + 2);
                for (int y = floorY + 1; y <= clearTopY; y++) {
                    level.setBlock(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private void paintWasteStoneCraterBiome(WorldGenLevel level, ChunkPos chunkPos, Crater crater) {
        ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        Holder<Biome> wasteStoneBiome = level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(WASTE_STONE_CRATER_BIOME);
        int minY = Math.max(level.getMinY(), this.groundY - CRATER_BIOME_DEPTH);
        int maxY = Math.min(level.getMaxY(), this.groundY + CRATER_BIOME_HEIGHT);
        int minSectionY = Math.max(chunk.getMinSectionY(), SectionPos.blockToSectionCoord(minY));
        int maxSectionY = Math.min(chunk.getMaxSectionY(), SectionPos.blockToSectionCoord(maxY));
        int quartMinX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int quartMinZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());

        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            int quartMinY = QuartPos.fromSection(sectionY);
            section.fillBiomesFromNoise(
                    (quartX, quartY, quartZ, sampler) -> {
                        int blockX = QuartPos.toBlock(quartX) + 2;
                        int blockY = QuartPos.toBlock(quartY) + 2;
                        int blockZ = QuartPos.toBlock(quartZ) + 2;
                        if (blockY >= minY
                                && blockY <= maxY
                                && crater.edgeDistance(blockX, blockZ) <= 1.0) {
                            return wasteStoneBiome;
                        }

                        return section.getNoiseBiome(quartX & 3, quartY & 3, quartZ & 3);
                    },
                    level.getLevel().getChunkSource().randomState().sampler(),
                    quartMinX,
                    quartMinY,
                    quartMinZ);
        }
    }

    private Crater createCrater(Island mainIsland) {
        int radiusX = (int) Math.round(mainIsland.radiusX() * 1.2);
        int radiusZ = (int) Math.round(mainIsland.radiusZ() * 1.2);
        int maxDepth = 15 + Math.floorMod((int) mix(this.shapeSeed ^ 0x61c8864680b583ebL), 4);
        return new Crater(
                mainIsland.centerX(),
                mainIsland.centerZ(),
                radiusX,
                radiusZ,
                maxDepth,
                mainIsland.phaseA() + 0.73,
                mainIsland.phaseB() - 0.41);
    }

    private List<Island> createIslands() {
        RandomSource random = RandomSource.create(this.shapeSeed);
        List<Island> islands = new ArrayList<>();
        islands.add(new Island(
                this.centerX,
                this.centerZ,
                this.topY,
                46 + random.nextInt(9),
                40 + random.nextInt(9),
                true,
                random.nextDouble() * Math.PI * 2.0,
                random.nextDouble() * Math.PI * 2.0));

        int smallIslandCount = 6 + random.nextInt(4);
        double angleOffset = random.nextDouble() * Math.PI * 2.0;
        for (int index = 0; index < smallIslandCount; index++) {
            double angle = angleOffset
                    + Math.PI * 2.0 * index / smallIslandCount
                    + (random.nextDouble() - 0.5) * 0.42;
            int distance = 68 + random.nextInt(28);
            int radiusX = 12 + random.nextInt(11);
            int radiusZ = 10 + random.nextInt(10);
            int islandX = this.centerX + (int) Math.round(Math.cos(angle) * distance);
            int islandZ = this.centerZ + (int) Math.round(Math.sin(angle) * distance);
            int islandTopY = Mth.clamp(this.topY - 10 + random.nextInt(19), 200, 228);
            islands.add(new Island(
                    islandX,
                    islandZ,
                    islandTopY,
                    radiusX,
                    radiusZ,
                    false,
                    random.nextDouble() * Math.PI * 2.0,
                    random.nextDouble() * Math.PI * 2.0));
        }

        return islands;
    }

    private int columnHash(int x, int z) {
        return (int) mix(this.shapeSeed ^ (long) x * 341873128712L ^ (long) z * 132897987541L);
    }

    private boolean isGeodeOre(int x, int y, int z) {
        int cellX = Math.floorDiv(x, CELL_SIZE);
        int cellY = Math.floorDiv(y, CELL_SIZE);
        int cellZ = Math.floorDiv(z, CELL_SIZE);
        long hash = mix(
                this.shapeSeed
                        ^ (long) cellX * 341873128712L
                        ^ (long) cellY * 42317861L
                        ^ (long) cellZ * 132897987541L);
        if (Math.floorMod(hash, ORE_CELL_CHANCE) != 0L) {
            return false;
        }

        int oreX = Math.floorMod((int) (hash >>> 8), CELL_SIZE);
        int oreY = Math.floorMod((int) (hash >>> 16), CELL_SIZE);
        int oreZ = Math.floorMod((int) (hash >>> 24), CELL_SIZE);
        int dx = Math.floorMod(x, CELL_SIZE) - oreX;
        int dy = Math.floorMod(y, CELL_SIZE) - oreY;
        int dz = Math.floorMod(z, CELL_SIZE) - oreZ;
        return dx * dx + dy * dy + dz * dz <= 3;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53l;
        return value ^ value >>> 33;
    }

    private record Island(
            int centerX,
            int centerZ,
            int topY,
            int radiusX,
            int radiusZ,
            boolean basin,
            double phaseA,
            double phaseB) {

        private double edgeDistance(int x, int z) {
            double dx = (x - this.centerX) / (double) this.radiusX;
            double dz = (z - this.centerZ) / (double) this.radiusZ;
            double angle = Math.atan2(dz, dx);
            double wobble = 1.0
                    + 0.075 * Math.sin(angle * 5.0 + this.phaseA)
                    + 0.04 * Math.sin(angle * 9.0 + this.phaseB);
            return Math.sqrt(dx * dx + dz * dz) / wobble;
        }

        private double lakeDistance(int x, int z) {
            double dx = (x - this.centerX) / (this.radiusX * 0.38);
            double dz = (z - this.centerZ) / (this.radiusZ * 0.34);
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    private record Crater(
            int centerX,
            int centerZ,
            int radiusX,
            int radiusZ,
            int maxDepth,
            double phaseA,
            double phaseB) {

        private double edgeDistance(int x, int z) {
            double dx = (x - this.centerX) / (double) this.radiusX;
            double dz = (z - this.centerZ) / (double) this.radiusZ;
            double angle = Math.atan2(dz, dx);
            double wobble = 1.0
                    + 0.09 * Math.sin(angle * 5.0 + this.phaseA)
                    + 0.045 * Math.sin(angle * 11.0 + this.phaseB);
            return Math.sqrt(dx * dx + dz * dz) / wobble;
        }

        private int depthAt(int x, int z, double edge) {
            double bowl = Math.pow(Math.max(0.0, 1.0 - edge * edge), 1.25);
            double ripple = 0.7 * Math.sin(x * 0.17 + this.phaseA)
                    + 0.55 * Math.cos(z * 0.15 + this.phaseB)
                    + 0.35 * Math.sin((x + z) * 0.11);
            double noiseFade = Mth.clamp((1.0 - edge) / 0.22, 0.0, 1.0);
            int depth = (int) Math.round(1.0 + (this.maxDepth - 1.0) * bowl + ripple * noiseFade);
            return Mth.clamp(depth, 1, this.maxDepth + 1);
        }
    }

}
