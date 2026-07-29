package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CursedFrostTreeFeature extends Feature<NoneFeatureConfiguration> {

    public CursedFrostTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int height = 18 + random.nextInt(6);
        Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new HashSet<>();
        BlockState verticalLog = logState(Direction.Axis.Y);

        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= 1; x++) {
                for (int z = 0; z <= 1; z++) {
                    logs.put(origin.offset(x, y, z), verticalLog);
                }
            }
        }

        int canopyDepth = 12 + random.nextInt(3);
        int canopyBottom = height - canopyDepth;
        int crownTop = height + 2;
        for (int y = canopyBottom; y <= crownTop; y++) {
            int distanceFromTop = crownTop - y;
            int radius = Math.min(6, 1 + (distanceFromTop + 1) / 2);
            if (y <= canopyBottom + 1) {
                radius = Math.max(4, radius - 1);
            }
            addCanopyLayer(leaves, origin, y, radius, random);
        }

        for (int y = canopyBottom + 2; y < height; y += 3) {
            int radius = Math.min(6, 2 + (height - y) / 2);
            addSupportingBranches(logs, leaves, origin, y, Math.max(2, radius - 2), random);
        }

        leaves.removeAll(logs.keySet());
        return CustomTreeFeatureSupport.place(
                context.level(), origin, logs, leaves,
                ModBlocks.CURSED_FROST_TREE_LEAVES.get(), ModBlocks.CURSED_FROST_TREE_SAPLING.get());
    }

    private static void addCanopyLayer(
            Set<BlockPos> leaves, BlockPos origin, int y, int radius, RandomSource random) {
        double edge = radius + 0.35;
        double edgeSquared = edge * edge;
        double innerSquared = Math.max(0, radius - 1) * Math.max(0, radius - 1);
        for (int x = -radius; x <= radius + 1; x++) {
            for (int z = -radius; z <= radius + 1; z++) {
                double centeredX = x - 0.5;
                double centeredZ = z - 0.5;
                double distanceSquared = centeredX * centeredX + centeredZ * centeredZ;
                if (distanceSquared > edgeSquared) {
                    continue;
                }
                float holeChance = distanceSquared > innerSquared ? 0.22F : 0.035F;
                if (random.nextFloat() >= holeChance) {
                    leaves.add(origin.offset(x, y, z));
                }
            }
        }
    }

    private static void addSupportingBranches(
            Map<BlockPos, BlockState> logs,
            Set<BlockPos> leaves,
            BlockPos origin,
            int y,
            int length,
            RandomSource random) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.18F) {
                continue;
            }
            BlockPos branch = switch (direction) {
                case NORTH -> origin.offset(random.nextInt(2), y, 0);
                case SOUTH -> origin.offset(random.nextInt(2), y, 1);
                case WEST -> origin.offset(0, y, random.nextInt(2));
                case EAST -> origin.offset(1, y, random.nextInt(2));
                default -> throw new IllegalArgumentException("Expected a horizontal direction");
            };
            int actualLength = Math.max(1, length - random.nextInt(2));
            for (int step = 0; step < actualLength; step++) {
                branch = branch.relative(direction);
                logs.put(branch, logState(direction.getAxis()));
                if (step >= actualLength - 2) {
                    leaves.add(branch.above());
                    leaves.add(branch.below());
                }
            }
        }
    }

    private static BlockState logState(Direction.Axis axis) {
        return ModBlocks.CURSED_FROST_TREE_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
    }
}
