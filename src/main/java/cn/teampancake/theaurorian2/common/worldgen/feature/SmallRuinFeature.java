package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class SmallRuinFeature extends Feature<SmallRuinConfiguration> {

    private static final int TERRAIN_SAMPLE_MARGIN = 1;
    private static final int MAX_HEIGHT_DIFFERENCE = 2;
    private static final int UPDATE_FLAGS = 19;

    public SmallRuinFeature() {
        super(SmallRuinConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<SmallRuinConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Optional<StructureTemplate> optionalTemplate = level.getLevel()
                .getStructureManager()
                .get(context.config().template());
        if (optionalTemplate.isEmpty()) {
            return false;
        }

        StructureTemplate template = optionalTemplate.get();
        Rotation rotation = Rotation.getRandom(random);
        Mirror[] mirrors = Mirror.values();
        Mirror mirror = mirrors[random.nextInt(mirrors.length)];
        Vec3i size = template.getSize(rotation);
        if (size.getX() > 16 || size.getZ() > 16) {
            return false;
        }

        int chunkMargin = size.getX() + 2 <= 16 && size.getZ() + 2 <= 16 ? 1 : 0;
        ChunkPos chunk = new ChunkPos(context.origin().getX() >> 4, context.origin().getZ() >> 4);
        int startX = clampStart(
                context.origin().getX() - size.getX() / 2,
                chunk.getMinBlockX(),
                chunk.getMaxBlockX(),
                size.getX(),
                chunkMargin);
        int startZ = clampStart(
                context.origin().getZ() - size.getZ() / 2,
                chunk.getMinBlockZ(),
                chunk.getMaxBlockZ(),
                size.getZ(),
                chunkMargin);
        TerrainSample terrain = sampleTerrain(level, startX, startZ, size.getX(), size.getZ());
        if (terrain == null || terrain.baseY + size.getY() >= level.getMaxY()) {
            return false;
        }
        if (!hasClearance(level, startX, terrain.baseY, startZ, size)) {
            return false;
        }

        BlockPos footprintOrigin = new BlockPos(startX, terrain.baseY, startZ);
        BlockPos placementOrigin = template.getZeroPositionWithTransform(footprintOrigin, mirror, rotation);
        BoundingBox bounds = new BoundingBox(
                startX,
                terrain.baseY,
                startZ,
                startX + size.getX() - 1,
                terrain.baseY + size.getY() - 1,
                startZ + size.getZ() - 1);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(mirror)
                .setRotation(rotation)
                .setBoundingBox(bounds)
                .setRandom(random)
                .setIgnoreEntities(true)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
        boolean placed = template.placeInWorld(
                level, placementOrigin, placementOrigin, settings, random, UPDATE_FLAGS);
        if (placed) {
            fillShortFoundation(level, startX, startZ, terrain);
        }
        return placed;
    }

    private static int clampStart(int desired, int chunkMin, int chunkMax, int size, int chunkMargin) {
        int minimum = chunkMin + chunkMargin;
        int maximum = chunkMax - chunkMargin - size + 1;
        return Mth.clamp(desired, minimum, maximum);
    }

    private static TerrainSample sampleTerrain(
            WorldGenLevel level, int startX, int startZ, int width, int depth) {
        int[][] heights = new int[width][depth];
        BlockState[][] surfaces = new BlockState[width][depth];
        int minimumHeight = Integer.MAX_VALUE;
        int maximumHeight = Integer.MIN_VALUE;
        int baseY = Integer.MIN_VALUE;

        for (int localX = -TERRAIN_SAMPLE_MARGIN; localX < width + TERRAIN_SAMPLE_MARGIN; localX++) {
            for (int localZ = -TERRAIN_SAMPLE_MARGIN; localZ < depth + TERRAIN_SAMPLE_MARGIN; localZ++) {
                int x = startX + localX;
                int z = startZ + localZ;
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (topY <= level.getMinY() || topY >= level.getMaxY()) {
                    return null;
                }

                BlockPos surfacePos = new BlockPos(x, topY - 1, z);
                BlockState surface = level.getBlockState(surfacePos);
                if (!level.getFluidState(surfacePos).isEmpty()
                        || surface.is(Blocks.BEDROCK)
                        || surface.is(BlockTags.LEAVES)
                        || surface.is(BlockTags.LOGS)
                        || !surface.isFaceSturdy(level, surfacePos, Direction.UP)) {
                    return null;
                }

                minimumHeight = Math.min(minimumHeight, topY);
                maximumHeight = Math.max(maximumHeight, topY);
                if (maximumHeight - minimumHeight > MAX_HEIGHT_DIFFERENCE) {
                    return null;
                }

                if (localX >= 0 && localX < width && localZ >= 0 && localZ < depth) {
                    heights[localX][localZ] = topY;
                    surfaces[localX][localZ] = surface;
                    baseY = Math.max(baseY, topY);
                }
            }
        }

        return new TerrainSample(baseY, heights, surfaces);
    }

    private static boolean hasClearance(
            WorldGenLevel level, int startX, int baseY, int startZ, Vec3i size) {
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int y = 0; y < size.getY(); y++) {
                    BlockState state = level.getBlockState(new BlockPos(startX + x, baseY + y, startZ + z));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void fillShortFoundation(
            WorldGenLevel level, int startX, int startZ, TerrainSample terrain) {
        for (int x = 0; x < terrain.heights.length; x++) {
            for (int z = 0; z < terrain.heights[x].length; z++) {
                if (level.getBlockState(new BlockPos(startX + x, terrain.baseY, startZ + z)).isAir()) {
                    continue;
                }
                int topY = terrain.heights[x][z];
                BlockState surface = terrain.surfaces[x][z];
                BlockState fill = foundationFill(surface);
                for (int y = topY; y < terrain.baseY; y++) {
                    BlockState state = y == terrain.baseY - 1
                            ? surface.getBlock().defaultBlockState()
                            : fill;
                    level.setBlock(new BlockPos(startX + x, y, startZ + z), state, UPDATE_FLAGS);
                }
            }
        }
    }

    private static BlockState foundationFill(BlockState surface) {
        if (surface.is(ModBlocks.AURORIAN_GRASS_BLOCK)
                || surface.is(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK)) {
            return ModBlocks.AURORIAN_DIRT.get().defaultBlockState();
        }
        return surface.getBlock().defaultBlockState();
    }

    private record TerrainSample(int baseY, int[][] heights, BlockState[][] surfaces) {
    }
}
