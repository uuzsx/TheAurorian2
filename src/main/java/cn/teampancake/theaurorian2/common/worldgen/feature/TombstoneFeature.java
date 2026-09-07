package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class TombstoneFeature extends Feature<NoneFeatureConfiguration> {

    private static final int UPDATE_FLAGS = 19;
    private static final int[][] FLOWER_RING_OFFSETS = {
            {-2, 0}, {-2, -1}, {-2, 1},
            {2, 0}, {2, -1}, {2, 1},
            {0, -2}, {0, 2},
            {-1, -2}, {1, -2}, {-1, 2}, {1, 2}
    };

    public TombstoneFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!canPlaceTombstone(level, origin)) {
            return false;
        }

        List<BlockPos> flowerPositions = findFlowerPositions(level, origin);
        if (flowerPositions.isEmpty()) {
            return false;
        }

        BlockPos urnPos = origin.below(2);
        if (!canPlaceUrn(level, urnPos)) {
            return false;
        }

        RandomSource random = context.random();
        int equinoxIndex = random.nextInt(flowerPositions.size());
        level.setBlock(origin, ModStructureBlocks.RIP.get().defaultBlockState(), UPDATE_FLAGS);
        level.setBlock(urnPos, ModStructureBlocks.URN.get().defaultBlockState(), UPDATE_FLAGS);

        for (int i = 0; i < flowerPositions.size(); i++) {
            Block flower = i == equinoxIndex
                    ? ModStructureBlocks.EQUINOX_FLOWER.get()
                    : randomAurorianFlower(random);
            level.setBlock(flowerPositions.get(i), flower.defaultBlockState(), UPDATE_FLAGS);
        }
        return true;
    }

    private static boolean canPlaceTombstone(WorldGenLevel level, BlockPos origin) {
        return origin.getY() > level.getMinY() + 1
                && origin.getY() < level.getMaxY()
                && level.getBlockState(origin).isAir()
                && level.getBlockState(origin.below()).is(ModBlocks.AURORIAN_GRASS_BLOCK.get());
    }

    private static boolean canPlaceUrn(WorldGenLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinY() || pos.getY() >= level.getMaxY()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return level.getFluidState(pos).isEmpty()
                && !state.is(Blocks.BEDROCK)
                && (state.is(BlockTags.DIRT) || state.isAir() || state.canBeReplaced());
    }

    private static List<BlockPos> findFlowerPositions(WorldGenLevel level, BlockPos origin) {
        List<BlockPos> positions = new ArrayList<>(FLOWER_RING_OFFSETS.length);
        for (int[] offset : FLOWER_RING_OFFSETS) {
            int x = origin.getX() + offset[0];
            int z = origin.getZ() + offset[1];
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (surfaceY <= level.getMinY() || surfaceY >= level.getMaxY()) {
                continue;
            }
            BlockPos flowerPos = new BlockPos(x, surfaceY, z);
            if (canPlaceFlower(level, flowerPos)) {
                positions.add(flowerPos);
            }
        }
        return positions;
    }

    private static boolean canPlaceFlower(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.getBlockState(pos.below()).is(ModBlocks.AURORIAN_GRASS_BLOCK.get())
                && level.getFluidState(pos).isEmpty()
                && (state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    private static Block randomAurorianFlower(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> ModBlocks.MOON_FROST_FLOWER.get();
            case 1 -> ModBlocks.VOID_CANDLE_FLOWER.get();
            case 2 -> ModBlocks.NEBULA_BLOSSOM_CLUSTER.get();
            default -> ModBlocks.PETUNIA_PLANT.get();
        };
    }
}
