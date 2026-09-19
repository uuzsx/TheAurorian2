package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A spreading, arched willow with living branches supporting its hanging foliage. */
public final class WeepingWillowTreeFeature extends Feature<NoneFeatureConfiguration> {
    public WeepingWillowTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.below()).is(BlockTags.DIRT)) return false;
        RandomSource random = context.random();
        TreePlan plan = createTreePlan(origin, 10 + random.nextInt(3), random);
        addRootFlare(level, origin, plan.logs(), random);

        // Distance propagation must only cross foliage that can actually be placed.
        // Do not let an obstruction leave unsupported hanging leaves behind it.
        int plannedCount = plan.leaves().size();
        plan.leaves().removeIf(pos -> !canReplace(level.getBlockState(pos)));
        if (plan.leaves().size() < plannedCount * 0.8) return false;
        return CustomTreeFeatureSupport.place(level, origin, plan.logs(), plan.leaves(),
                ModStructureBlocks.WEEPING_WILLOW_LEAVES.get(), ModBlocks.WEEPING_WILLOW_SAPLING.get());
    }

    static TreePlan createTreePlan(BlockPos origin, int height, RandomSource random) {
        Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new HashSet<>();
        List<BlockPos> spine = new ArrayList<>();
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos cursor = origin;
        int bend = 3 + random.nextInt(2);
        for (int y = 0; y <= height; y++) {
            logs.put(cursor, logState(Direction.Axis.Y));
            if (y == bend || y == height - 2) {
                cursor = connect(logs, cursor, cursor.relative(lean));
            }
            spine.add(cursor);
            cursor = cursor.above();
        }

        double phase = random.nextDouble() * Math.PI * 2;
        int arms = 5 + random.nextInt(2);
        for (int i = 0; i < arms; i++) {
            double angle = phase + i * Math.PI * 2 / arms + (random.nextDouble() - 0.5) * 0.36;
            double reach = 5.5 + random.nextDouble() * 1.7;
            BlockPos start = spine.get(height - 4 + random.nextInt(2));
            BlockPos peak = origin.offset(
                    (int) Math.round(Math.cos(angle) * reach * 0.48), height + random.nextInt(2),
                    (int) Math.round(Math.sin(angle) * reach * 0.48));
            BlockPos tip = origin.offset(
                    (int) Math.round(Math.cos(angle) * reach), height - 2 + random.nextInt(2),
                    (int) Math.round(Math.sin(angle) * reach));
            BlockPos forkStart = curvedBranch(logs, leaves, start, peak, tip, random);
            addCrownLobe(leaves, tip.above(), 2.6, 2.2, 1.5, random);
            addDrapes(leaves, tip, angle, origin.getY() + 2, random);

            // A few shorter forks break the umbrella outline without radial symmetry.
            if (i % 2 == 0) {
                double forkAngle = angle + (random.nextBoolean() ? 0.55 : -0.55);
                BlockPos forkTip = origin.offset(
                        (int) Math.round(Math.cos(forkAngle) * (reach - 0.6)), height - 1,
                        (int) Math.round(Math.sin(forkAngle) * (reach - 0.6)));
                BlockPos forkPeak = forkStart.above(2);
                curvedBranch(logs, leaves, forkStart, forkPeak, forkTip, random);
                addCrownLobe(leaves, forkTip.above(), 2.2, 2.0, 1.35, random);
                addDrapes(leaves, forkTip, forkAngle, origin.getY() + 3, random);
            }
        }

        addCrownLobe(leaves, spine.get(height).above(), 3.1, 2.8, 1.7, random);
        leaves.removeAll(logs.keySet());
        return new TreePlan(logs, leaves);
    }

    private static BlockPos curvedBranch(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves,
                                     BlockPos start, BlockPos control, BlockPos end, RandomSource random) {
        BlockPos cursor = start;
        BlockPos midpoint = start;
        for (int step = 1; step <= 12; step++) {
            double t = step / 12.0;
            double a = (1 - t) * (1 - t), b = 2 * t * (1 - t), c = t * t;
            BlockPos next = new BlockPos(
                    (int) Math.round(a * start.getX() + b * control.getX() + c * end.getX()),
                    (int) Math.round(a * start.getY() + b * control.getY() + c * end.getY()),
                    (int) Math.round(a * start.getZ() + b * control.getZ() + c * end.getZ()));
            cursor = connect(logs, cursor, next);
            if (step == 6) midpoint = cursor;
            if (step == 6 || step == 9) {
                addCrownLobe(leaves, cursor.above(), 2.8, 2.4, 1.5, random);
            }
        }
        return midpoint;
    }

    private static BlockPos connect(Map<BlockPos, BlockState> logs, BlockPos from, BlockPos to) {
        BlockPos cursor = from;
        // Face-connected steps keep diagonal branches supported and log axes correct.
        while (!cursor.equals(to)) {
            int dx = to.getX() - cursor.getX(), dy = to.getY() - cursor.getY(), dz = to.getZ() - cursor.getZ();
            Direction direction;
            if (Math.abs(dy) > Math.max(Math.abs(dx), Math.abs(dz))) direction = dy > 0 ? Direction.UP : Direction.DOWN;
            else if (Math.abs(dx) >= Math.abs(dz) && dx != 0) direction = dx > 0 ? Direction.EAST : Direction.WEST;
            else direction = dz > 0 ? Direction.SOUTH : Direction.NORTH;
            cursor = cursor.relative(direction);
            logs.putIfAbsent(cursor, logState(direction.getAxis()));
        }
        return cursor;
    }

    private static void addCrownLobe(Set<BlockPos> leaves, BlockPos center,
                                     double rx, double rz, double ry, RandomSource random) {
        int radiusX = (int) Math.ceil(rx), radiusZ = (int) Math.ceil(rz), radiusY = (int) Math.ceil(ry);
        for (int y = -radiusY; y <= radiusY; y++) {
            for (int x = -radiusX; x <= radiusX; x++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double edge = x * x / (rx * rx) + z * z / (rz * rz) + y * y / (ry * ry);
                    if (edge <= 1.0 && (edge < 0.75 || random.nextFloat() > 0.13F)) {
                        leaves.add(center.offset(x, y, z));
                    }
                }
            }
        }
    }

    private static void addDrapes(Set<BlockPos> leaves, BlockPos branch, double angle,
                                  int minimumY, RandomSource random) {
        Direction outward = Math.abs(Math.cos(angle)) > Math.abs(Math.sin(angle))
                ? (Math.cos(angle) > 0 ? Direction.EAST : Direction.WEST)
                : (Math.sin(angle) > 0 ? Direction.SOUTH : Direction.NORTH);
        Direction side = outward.getClockWise();
        for (Direction direction : new Direction[]{outward, side, side.getOpposite()}) {
            BlockPos anchor = branch.relative(direction);
            int length = 3 + random.nextInt(4);
            for (int y = 0; y < length && anchor.getY() - y >= minimumY; y++) {
                leaves.add(anchor.below(y));
            }
        }
        // A shorter central strand makes the hanging groups taper rather than end flat.
        int innerLength = 2 + random.nextInt(2);
        for (int y = 1; y <= innerLength && branch.getY() - y >= minimumY; y++) {
            leaves.add(branch.below(y));
        }
    }

    private static void addRootFlare(WorldGenLevel level, BlockPos origin,
                                      Map<BlockPos, BlockState> logs, RandomSource random) {
        int roots = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            BlockPos foot = origin.relative(direction);
            if (level.getBlockState(foot.below()).is(BlockTags.DIRT)
                    && canReplace(level.getBlockState(foot)) && canReplace(level.getBlockState(foot.above()))) {
                logs.put(foot, logState(Direction.Axis.Y));
                logs.put(foot.above(), logState(Direction.Axis.Y));
                if (++roots == 2) break;
            }
        }
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.LEAVES)
                || state.is(ModBlocks.WEEPING_WILLOW_SAPLING.get());
    }

    private static BlockState logState(Direction.Axis axis) {
        return ModStructureBlocks.WEEPING_WILLOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    record TreePlan(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves) {}
}
