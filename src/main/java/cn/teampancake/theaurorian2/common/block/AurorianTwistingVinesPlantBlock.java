package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianTwistingVinesPlantBlock extends GrowingPlantBodyBlock {

    public static final MapCodec<AurorianTwistingVinesPlantBlock> CODEC =
            simpleCodec(AurorianTwistingVinesPlantBlock::new);
    private static final VoxelShape SHAPE = Block.column(8.0D, 0.0D, 16.0D);

    public AurorianTwistingVinesPlantBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, false);
    }

    @Override
    protected MapCodec<AurorianTwistingVinesPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return ModBlocks.AURORIAN_TWISTING_VINES.get();
    }
}
