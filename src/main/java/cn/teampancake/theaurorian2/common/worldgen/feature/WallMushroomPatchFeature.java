package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class WallMushroomPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final int UPDATE_FLAGS = 19;
    private static final int SEARCH_ATTEMPTS = 48;
    private static final int SEARCH_RADIUS = 4;
    private static final int SKY_EXPOSURE_RADIUS = 6;

    public WallMushroomPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            BlockPos pos = context.origin().offset(
                    random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS);
            if (pos.getY() <= level.getMinY() || pos.getY() >= level.getMaxY()
                    || !level.getBlockState(pos).isAir()) {
                continue;
            }
            for (Direction facing : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (canAttachToCaveWall(level, pos, facing)) {
                    return placeCluster(level, pos, facing, random);
                }
            }
        }
        return false;
    }

    private static boolean placeCluster(
            WorldGenLevel level, BlockPos center, Direction facing, RandomSource random) {
        Block mushroom = WallMushroomPlacement.randomMushroom(random);
        int radius = 1 + random.nextInt(2);
        int targetCount = 2 + random.nextInt(4);
        Set<BlockPos> placed = new HashSet<>();

        place(level, center, facing, mushroom, random);
        placed.add(center);
        for (int attempt = 0; attempt < 32 && placed.size() < targetCount; attempt++) {
            int lateral = random.nextInt(radius * 2 + 1) - radius;
            int vertical = random.nextInt(radius * 2 + 1) - radius;
            BlockPos candidate = facing.getAxis() == Direction.Axis.X
                    ? center.offset(0, vertical, lateral)
                    : center.offset(lateral, vertical, 0);
            if (!placed.contains(candidate)
                    && level.getBlockState(candidate).isAir()
                    && canAttachToCaveWall(level, candidate, facing)) {
                place(level, candidate, facing, mushroom, random);
                placed.add(candidate);
            }
        }
        return !placed.isEmpty();
    }

    private static void place(
            WorldGenLevel level,
            BlockPos pos,
            Direction facing,
            Block mushroom,
            RandomSource random) {
        level.setBlock(pos, WallMushroomPlacement.stateFor(mushroom, facing, random), UPDATE_FLAGS);
    }

    private static boolean canAttachToCaveWall(
            WorldGenLevel level, BlockPos mushroomPos, Direction facing) {
        BlockPos supportPos = mushroomPos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        return (support.is(ModBlocks.AURORIAN_STONE.get())
                        || support.is(ModBlocks.AURORIAN_EROSIVE.get()))
                && support.isFaceSturdy(level, supportPos, facing)
                && isCaveInterior(level, mushroomPos);
    }

    private static boolean isCaveInterior(WorldGenLevel level, BlockPos pos) {
        if (level.canSeeSky(pos)) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int distance = 1; distance <= SKY_EXPOSURE_RADIUS; distance++) {
                BlockPos probe = pos.relative(direction, distance);
                if (!level.getBlockState(probe).isAir()) {
                    break;
                }
                if (level.canSeeSky(probe)) {
                    return false;
                }
            }
        }
        return true;
    }
}
