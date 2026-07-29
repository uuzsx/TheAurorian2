package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jspecify.annotations.Nullable;

public final class MysteriumWoolBedBlock extends BedBlock {

    public static final MapCodec<BedBlock> CODEC = simpleCodec(MysteriumWoolBedBlock::new);

    public MysteriumWoolBedBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.BLUE, properties);
    }

    @Override
    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == BedPart.HEAD ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // The legacy two-block model is rendered from the head half, so no vanilla bed renderer is needed.
        return null;
    }
}
