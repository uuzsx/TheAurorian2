package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Single-block vertical crystal placeholder retaining the legacy up/down state. */
public final class LegacyVerticalDirectionBlock extends Block {
    public static final MapCodec<LegacyVerticalDirectionBlock> CODEC = simpleCodec(LegacyVerticalDirectionBlock::new);
    public static final EnumProperty<Direction> VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;

    public LegacyVerticalDirectionBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VERTICAL_DIRECTION, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace().getAxis() == Direction.Axis.Y
                ? context.getClickedFace()
                : Direction.UP;
        return this.defaultBlockState().setValue(VERTICAL_DIRECTION, direction);
    }
}
