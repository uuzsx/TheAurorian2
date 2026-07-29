package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianTwistingVinesBlock extends GrowingPlantHeadBlock {

    public static final MapCodec<AurorianTwistingVinesBlock> CODEC =
            simpleCodec(AurorianTwistingVinesBlock::new);
    private static final VoxelShape SHAPE = Block.column(8.0D, 0.0D, 15.0D);

    public AurorianTwistingVinesBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, false, NetherVines.GROW_PER_TICK_PROBABILITY);
    }

    @Override
    protected MapCodec<AurorianTwistingVinesBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.AURORIAN_TWISTING_VINES_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return NetherVines.isValidGrowthState(state);
    }
}
