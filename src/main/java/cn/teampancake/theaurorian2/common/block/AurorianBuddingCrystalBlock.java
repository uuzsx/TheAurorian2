package cn.teampancake.theaurorian2.common.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public final class AurorianBuddingCrystalBlock extends BuddingAmethystBlock {

    private static final Direction[] DIRECTIONS = Direction.values();

    private final Supplier<? extends Block> smallBud;
    private final Supplier<? extends Block> mediumBud;
    private final Supplier<? extends Block> largeBud;
    private final Supplier<? extends Block> cluster;

    public AurorianBuddingCrystalBlock(
            BlockBehaviour.Properties properties,
            Supplier<? extends Block> smallBud,
            Supplier<? extends Block> mediumBud,
            Supplier<? extends Block> largeBud,
            Supplier<? extends Block> cluster) {
        super(properties);
        this.smallBud = smallBud;
        this.mediumBud = mediumBud;
        this.largeBud = largeBud;
        this.cluster = cluster;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) {
            return;
        }

        Direction growDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos growPos = pos.relative(growDirection);
        BlockState relativeState = level.getBlockState(growPos);
        Block nextStage = null;
        if (canClusterGrowAtState(relativeState)) {
            nextStage = this.smallBud.get();
        } else if (relativeState.is(this.smallBud.get())
                && relativeState.getValue(AmethystClusterBlock.FACING) == growDirection) {
            nextStage = this.mediumBud.get();
        } else if (relativeState.is(this.mediumBud.get())
                && relativeState.getValue(AmethystClusterBlock.FACING) == growDirection) {
            nextStage = this.largeBud.get();
        } else if (relativeState.is(this.largeBud.get())
                && relativeState.getValue(AmethystClusterBlock.FACING) == growDirection) {
            nextStage = this.cluster.get();
        }

        if (nextStage != null) {
            BlockState targetState = nextStage.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, growDirection)
                    .setValue(AmethystClusterBlock.WATERLOGGED, relativeState.getFluidState().is(Fluids.WATER));
            level.setBlockAndUpdate(growPos, targetState);
        }
    }
}
