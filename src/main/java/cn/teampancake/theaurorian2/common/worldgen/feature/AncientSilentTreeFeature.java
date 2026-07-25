package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class AncientSilentTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final double TWO_PI = Math.PI * 2.0D;

    public AncientSilentTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        for (BlockPos origin : AncientTreeFeatureSupport.findPlacementCandidates(
                context.level(), context.origin(), random)) {
            int height = 30 + random.nextInt(9);
            TreePlan plan = createTreePlan(origin, height, random);
            if (!AncientTreeFeatureSupport.extendTrunkToTerrain(
                    context.level(), origin, plan.logs(), logState(Direction.Axis.Y))) {
                continue;
            }
            if (CustomTreeFeatureSupport.place(
                    context.level(), origin, plan.logs(), plan.leaves(),
                    ModBlocks.SILENT_TREE_LEAVES.get(), ModBlocks.SILENT_TREE_SAPLING.get())) {
                WallMushroomPlacement.placeOnAncientTree(
                        context.level(), origin, plan.logs(), random);
                return true;
            }
        }
        return false;
    }

    static TreePlan createTreePlan(BlockPos origin, int height, RandomSource random) {
        Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new HashSet<>();

        buildTrunk(logs, origin, height);
        addCanopyTier(logs, leaves, origin, height - 14, 5, 6, 8, 10, 2, 5, 2, random);
        addCanopyTier(logs, leaves, origin, height - 8, 4, 5, 7, 9, 2, 4, 2, random);
        addCanopyTier(logs, leaves, origin, height - 3, 3, 4, 5, 7, 3, 4, 3, random);
        addRaggedSpheroid(leaves, origin.above(height + 2), 4, 3, random);

        leaves.removeAll(logs.keySet());
        return new TreePlan(logs, leaves);
    }

    private static void buildTrunk(
            Map<BlockPos, BlockState> logs, BlockPos origin, int height) {
        BlockState verticalLog = logState(Direction.Axis.Y);
        for (int y = 0; y <= height; y++) {
            double radius = trunkRadius(y, height);
            int blockRadius = Mth.ceil(radius);
            for (int x = -blockRadius; x <= blockRadius; x++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        logs.put(origin.offset(x, y, z), verticalLog);
                    }
                }
            }
        }
    }

    private static double trunkRadius(int y, int height) {
        int taperStart = height - 5;
        if (y <= taperStart) {
            return 1.5D;
        }
        double progress = (y - taperStart) / 5.0D;
        return Mth.lerp(progress, 1.5D, 0.8D);
    }

    private static void addCanopyTier(
            Map<BlockPos, BlockState> logs,
            Set<BlockPos> leaves,
            BlockPos origin,
            int branchY,
            int minBranches,
            int maxBranches,
            int minLength,
            int maxLength,
            int rise,
            int leafRadius,
            int leafHeight,
            RandomSource random) {
        int branchCount = minBranches + random.nextInt(maxBranches - minBranches + 1);
        double angleOffset = random.nextDouble() * TWO_PI;
        double angleStep = TWO_PI / branchCount;
        double startX = origin.getX() + 0.5D;
        double startY = origin.getY() + branchY;
        double startZ = origin.getZ() + 0.5D;

        for (int branch = 0; branch < branchCount; branch++) {
            double angle = angleOffset + branch * angleStep + (random.nextDouble() - 0.5D) * 0.22D;
            int length = minLength + random.nextInt(maxLength - minLength + 1);
            BranchTip mainTip = drawCurvedBranch(
                    logs, startX, startY, startZ, angle, length, rise, 1.4D, length / 3);

            addRaggedSpheroid(leaves, mainTip.pos(), Math.max(3, leafRadius - 1), leafHeight, random);

            double forkAngle = 0.28D + random.nextDouble() * 0.18D;
            int forkLength = 3 + random.nextInt(3);
            BranchTip leftTip = drawCurvedBranch(
                    logs,
                    mainTip.pos().getX() + 0.5D,
                    mainTip.pos().getY(),
                    mainTip.pos().getZ() + 0.5D,
                    angle - forkAngle,
                    forkLength,
                    1 + random.nextInt(2),
                    0.6D,
                    0);
            BranchTip rightTip = drawCurvedBranch(
                    logs,
                    mainTip.pos().getX() + 0.5D,
                    mainTip.pos().getY(),
                    mainTip.pos().getZ() + 0.5D,
                    angle + forkAngle,
                    Math.max(2, forkLength - random.nextInt(2)),
                    1 + random.nextInt(2),
                    0.6D,
                    0);

            addRaggedSpheroid(leaves, leftTip.pos(), leafRadius, leafHeight, random);
            addRaggedSpheroid(leaves, rightTip.pos(), leafRadius, leafHeight, random);
        }
    }

    private static BranchTip drawCurvedBranch(
            Map<BlockPos, BlockState> logs,
            double startX,
            double startY,
            double startZ,
            double angle,
            int length,
            int rise,
            double arch,
            int thickSteps) {
        BlockPos previous = BlockPos.containing(startX, startY, startZ);
        BlockPos tip = previous;
        for (int step = 0; step <= length; step++) {
            double progress = step / (double) length;
            double lateralCurve = Math.sin(progress * Math.PI) * 0.75D;
            double x = startX
                    + Math.cos(angle) * step
                    + Math.cos(angle + Math.PI / 2.0D) * lateralCurve;
            double y = startY + rise * progress + Math.sin(progress * Math.PI) * arch;
            double z = startZ
                    + Math.sin(angle) * step
                    + Math.sin(angle + Math.PI / 2.0D) * lateralCurve;
            tip = BlockPos.containing(x, y, z);
            Direction.Axis axis = dominantAxis(previous, tip);
            addLogBrush(logs, tip, axis, step <= thickSteps ? 1 : 0);
            previous = tip;
        }
        return new BranchTip(tip);
    }

    private static void addLogBrush(
            Map<BlockPos, BlockState> logs, BlockPos center, Direction.Axis axis, int radius) {
        BlockState branchLog = logState(axis);
        logs.put(center, branchLog);
        if (radius == 0) {
            return;
        }
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                if (first * first + second * second > radius * radius) {
                    continue;
                }
                BlockPos offset = switch (axis) {
                    case X -> center.offset(0, first, second);
                    case Y -> center.offset(first, 0, second);
                    case Z -> center.offset(first, second, 0);
                };
                logs.put(offset, branchLog);
            }
        }
    }

    private static void addRaggedSpheroid(
            Set<BlockPos> leaves,
            BlockPos center,
            int horizontalRadius,
            int verticalRadius,
            RandomSource random) {
        for (int y = -verticalRadius; y <= verticalRadius; y++) {
            for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
                for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                    double horizontal = (x * x + z * z)
                            / ((horizontalRadius + 0.35D) * (horizontalRadius + 0.35D));
                    double vertical = y * y
                            / ((verticalRadius + 0.35D) * (verticalRadius + 0.35D));
                    double distance = horizontal + vertical;
                    if (distance > 1.0D) {
                        continue;
                    }
                    float holeChance = distance > 0.76D ? 0.24F : 0.025F;
                    if (random.nextFloat() >= holeChance) {
                        leaves.add(center.offset(x, y, z));
                    }
                }
            }
        }

        int edgeClusters = 6 + random.nextInt(5);
        for (int cluster = 0; cluster < edgeClusters; cluster++) {
            double angle = random.nextDouble() * TWO_PI;
            int distance = Math.max(1, horizontalRadius - 1);
            BlockPos edge = center.offset(
                    Mth.floor(Math.cos(angle) * distance),
                    random.nextInt(Math.max(1, verticalRadius * 2 + 1)) - verticalRadius,
                    Mth.floor(Math.sin(angle) * distance));
            leaves.add(edge);
            leaves.add(edge.east());
            leaves.add(edge.south());
            leaves.add(edge.offset(1, 0, 1));
        }
    }

    private static Direction.Axis dominantAxis(BlockPos from, BlockPos to) {
        int x = Math.abs(to.getX() - from.getX());
        int y = Math.abs(to.getY() - from.getY());
        int z = Math.abs(to.getZ() - from.getZ());
        if (y > x && y > z) {
            return Direction.Axis.Y;
        }
        return x >= z ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static BlockState logState(Direction.Axis axis) {
        return ModBlocks.SILENT_TREE_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
    }

    private record BranchTip(BlockPos pos) {
    }

    record TreePlan(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves) {
    }
}
