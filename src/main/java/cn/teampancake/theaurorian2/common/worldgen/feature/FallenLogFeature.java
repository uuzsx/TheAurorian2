package cn.teampancake.theaurorian2.common.worldgen.feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FallenTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public final class FallenLogFeature extends Feature<FallenTreeConfiguration> {

    private static final int MAX_FALL_HEIGHT = 5;
    private static final int MAX_GROUND_GAP = 2;

    public FallenLogFeature() {
        super(FallenTreeConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenTreeConfiguration> context) {
        FallenTreeConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int logLength = config.logLength.sample(random) - 2;
        BlockPos.MutableBlockPos start = context.origin()
                .relative(direction, 2 + random.nextInt(2))
                .mutable();

        moveDownToGround(level, start);
        if (logLength <= 0 || !canPlaceEntireLog(level, logLength, start, direction)) {
            return false;
        }

        placeLog(config, level, random, logLength, start, direction);
        return true;
    }

    private void moveDownToGround(WorldGenLevel level, BlockPos.MutableBlockPos start) {
        start.move(Direction.UP);
        for (int i = 0; i <= MAX_FALL_HEIGHT; i++) {
            if (canRestOnGround(level, start)) {
                return;
            }
            start.move(Direction.DOWN);
        }
    }

    private boolean canPlaceEntireLog(
            WorldGenLevel level,
            int logLength,
            BlockPos.MutableBlockPos start,
            Direction direction) {
        int groundGap = 0;
        for (int i = 0; i < logLength; i++) {
            if (!TreeFeature.validTreePos(level, start)) {
                return false;
            }

            if (isOverSolidGround(level, start)) {
                groundGap = 0;
            } else if (++groundGap > MAX_GROUND_GAP) {
                return false;
            }
            start.move(direction);
        }

        start.move(direction.getOpposite(), logLength);
        return true;
    }

    private void placeLog(
            FallenTreeConfiguration config,
            WorldGenLevel level,
            RandomSource random,
            int logLength,
            BlockPos.MutableBlockPos start,
            Direction direction) {
        Set<BlockPos> logPositions = new HashSet<>();
        Function<BlockState, BlockState> horizontalAxis =
                state -> state.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis());

        for (int i = 0; i < logLength; i++) {
            BlockState log = horizontalAxis.apply(config.trunkProvider.getState(level, random, start));
            level.setBlock(start, log, 3);
            this.markAboveForPostProcessing(level, start);
            logPositions.add(start.immutable());
            start.move(direction);
        }

        decorateLogs(level, random, logPositions, config.logDecorators);
    }

    private boolean canRestOnGround(LevelAccessor level, BlockPos pos) {
        return TreeFeature.validTreePos(level, pos) && isOverSolidGround(level, pos);
    }

    private boolean isOverSolidGround(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
    }

    private void decorateLogs(
            WorldGenLevel level,
            RandomSource random,
            Set<BlockPos> logs,
            List<TreeDecorator> decorators) {
        if (decorators.isEmpty()) {
            return;
        }

        BiConsumer<BlockPos, BlockState> decorationSetter =
                (pos, state) -> level.setBlock(pos, state, 19);
        TreeDecorator.Context context =
                new TreeDecorator.Context(level, decorationSetter, random, logs, Set.of(), Set.of());
        decorators.forEach(decorator -> decorator.place(context));
    }
}
