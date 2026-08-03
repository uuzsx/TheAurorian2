package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public final class AurorianDungeonFeature extends Feature<NoneFeatureConfiguration> {

    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public AurorianDungeonFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        int radiusX = random.nextInt(2) + 2;
        int radiusZ = random.nextInt(2) + 2;
        int minX = -radiusX - 1;
        int maxX = radiusX + 1;
        int minZ = -radiusZ - 1;
        int maxZ = radiusZ + 1;
        int openings = countOpenings(level, origin, minX, maxX, minZ, maxZ);
        if (openings < 1 || openings > 5) {
            return false;
        }

        buildRoom(level, origin, random, replaceable, minX, maxX, minZ, maxZ);
        placeEmptyChests(level, origin, random, replaceable, radiusX, radiusZ);
        safeSetBlock(level, origin, Blocks.SPAWNER.defaultBlockState(), replaceable);
        return true;
    }

    private static int countOpenings(
            WorldGenLevel level, BlockPos origin, int minX, int maxX, int minZ, int maxZ) {
        int openings = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = -1; y <= 4; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean solid = level.getBlockState(pos).isSolid();
                    if ((y == -1 || y == 4) && !solid) {
                        return Integer.MAX_VALUE;
                    }
                    if ((x == minX || x == maxX || z == minZ || z == maxZ)
                            && y == 0
                            && level.isEmptyBlock(pos)
                            && level.isEmptyBlock(pos.above())) {
                        openings++;
                    }
                }
            }
        }
        return openings;
    }

    private void buildRoom(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Predicate<BlockState> replaceable,
            int minX,
            int maxX,
            int minZ,
            int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = 3; y >= -1; y--) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean shell = x == minX || y == -1 || z == minZ || x == maxX || y == 4 || z == maxZ;
                    if (shell) {
                        if (pos.getY() >= level.getMinY() && !level.getBlockState(pos.below()).isSolid()) {
                            level.setBlock(pos, AIR, 2);
                        } else if (state.isSolid() && !state.is(ModBlocks.AURORIAN_CHEST)) {
                            BlockState wall = y == -1 && random.nextInt(4) != 0
                                    ? ModBlocks.MOSSY_AURORIAN_COBBLESTONE.get().defaultBlockState()
                                    : ModBlocks.AURORIAN_COBBLESTONE.get().defaultBlockState();
                            safeSetBlock(level, pos, wall, replaceable);
                        }
                    } else if (!state.is(ModBlocks.AURORIAN_CHEST) && !state.is(Blocks.SPAWNER)) {
                        safeSetBlock(level, pos, AIR, replaceable);
                    }
                }
            }
        }
    }

    private void placeEmptyChests(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Predicate<BlockState> replaceable,
            int radiusX,
            int radiusZ) {
        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                BlockPos pos = origin.offset(
                        random.nextInt(radiusX * 2 + 1) - radiusX,
                        0,
                        random.nextInt(radiusZ * 2 + 1) - radiusZ);
                if (!level.isEmptyBlock(pos)) {
                    continue;
                }
                int walls = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(direction)).isSolid()) {
                        walls++;
                    }
                }
                if (walls == 1) {
                    BlockState chestState = StructurePiece.reorient(
                            level, pos, ModBlocks.AURORIAN_CHEST.get().defaultBlockState());
                    safeSetBlock(level, pos, chestState, replaceable);
                    break;
                }
            }
        }
    }
}
