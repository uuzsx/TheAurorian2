package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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

public final class CurtainTreeFeature extends Feature<NoneFeatureConfiguration> {

    public CurtainTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int height = 12 + random.nextInt(4);
        TreePlan plan = createTreePlan(origin, height, random, ModBlocks.CURTAIN_TREE_LOG.get());
        return CustomTreeFeatureSupport.place(
                context.level(), origin, plan.logs(), plan.leaves(),
                ModBlocks.CURTAIN_TREE_LEAVES.get(), ModBlocks.CURTAIN_TREE_SAPLING.get());
    }

    static TreePlan createTreePlan(
            BlockPos origin, int height, RandomSource random, RotatedPillarBlock logBlock) {
        Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new HashSet<>();
        Map<Integer, BlockPos> trunkCenters = new HashMap<>();
        List<BranchTip> branchTips = new ArrayList<>();
        BlockState verticalLog = logState(logBlock, Direction.Axis.Y);

        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction secondLean = random.nextBoolean() ? clockwise(lean) : counterClockwise(lean);
        int firstBend = height - 6;
        int secondBend = height - 2;
        boolean hasSecondBend = random.nextFloat() < 0.45F;
        BlockPos cursor = origin;
        for (int y = 0; y <= height; y++) {
            logs.put(cursor, verticalLog);
            if (y == firstBend || y == secondBend && hasSecondBend) {
                Direction bend = y == firstBend ? lean : secondLean;
                cursor = cursor.relative(bend);
                logs.put(cursor, logState(logBlock, bend.getAxis()));
            }
            trunkCenters.put(y, cursor);
            cursor = cursor.above();
        }

        List<Direction> directions = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int branchCount = 5 + random.nextInt(2);
        for (int branchIndex = 0; branchIndex < branchCount; branchIndex++) {
            Direction outward = directions.get(branchIndex % directions.size());
            int branchY = height - 5 + random.nextInt(4);
            int length = 3 + random.nextInt(3);
            Direction side = random.nextBoolean() ? clockwise(outward) : counterClockwise(outward);
            int curveAt = 2 + random.nextInt(Math.max(1, length - 1));
            BlockPos branch = trunkCenters.get(branchY);

            for (int step = 1; step <= length; step++) {
                branch = branch.relative(outward);
                logs.put(branch, logState(logBlock, outward.getAxis()));
                if (step == curveAt && random.nextFloat() < 0.75F) {
                    branch = branch.relative(side);
                    logs.put(branch, logState(logBlock, side.getAxis()));
                }
                if (step >= length - 1 && random.nextBoolean()) {
                    branch = branch.above();
                    logs.put(branch, verticalLog);
                }
                if (step >= length - 2) {
                    addLooseLeafCluster(leaves, branch, random);
                }
            }
            branchTips.add(new BranchTip(branch, outward));
        }

        BlockPos crownCenter = trunkCenters.get(height);
        for (int dy = -3; dy <= 1; dy++) {
            int radius = switch (dy) {
                case -3 -> 3;
                case -2, -1 -> 5;
                case 0 -> 4;
                default -> 2;
            };
            addBrokenCanopyLayer(leaves, crownCenter.above(dy), radius, random);
        }

        for (BranchTip tip : branchTips) {
            if (random.nextFloat() < 0.9F) {
                addLeafCurtain(leaves, tip.pos().relative(tip.outward()), 2 + random.nextInt(4));
            }
            if (random.nextFloat() < 0.4F) {
                Direction side = random.nextBoolean() ? clockwise(tip.outward()) : counterClockwise(tip.outward());
                BlockPos secondAnchor = tip.pos().relative(side);
                leaves.add(secondAnchor);
                addLeafCurtain(leaves, secondAnchor.relative(side), 2 + random.nextInt(3));
            }
        }

        leaves.removeAll(logs.keySet());
        return new TreePlan(logs, leaves);
    }

    private static void addLooseLeafCluster(Set<BlockPos> leaves, BlockPos center, RandomSource random) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int distance = Math.abs(dx) + Math.abs(dz) + Math.abs(dy);
                    if (distance <= 3 && (distance <= 1 || random.nextFloat() > 0.22F)) {
                        leaves.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
    }

    private static void addBrokenCanopyLayer(Set<BlockPos> leaves, BlockPos center, int radius, RandomSource random) {
        int radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared <= radiusSquared + 1) {
                    float holeChance = distanceSquared > (radius - 1) * (radius - 1) ? 0.32F : 0.16F;
                    if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 || random.nextFloat() >= holeChance) {
                        leaves.add(center.offset(dx, 0, dz));
                    }
                }
            }
        }
    }

    private static void addLeafCurtain(Set<BlockPos> leaves, BlockPos anchor, int length) {
        for (int y = 0; y < length; y++) {
            leaves.add(anchor.below(y));
        }
    }

    private static BlockState logState(RotatedPillarBlock logBlock, Direction.Axis axis) {
        return logBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static Direction clockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> throw new IllegalArgumentException("Expected a horizontal direction");
        };
    }

    private static Direction counterClockwise(Direction direction) {
        return clockwise(clockwise(clockwise(direction)));
    }

    private record BranchTip(BlockPos pos, Direction outward) {
    }

    record TreePlan(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves) {
    }
}
