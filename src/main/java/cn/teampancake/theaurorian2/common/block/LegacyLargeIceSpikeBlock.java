package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

/** Two-block crystal placeholder retaining both half and vertical-direction states. */
public final class LegacyLargeIceSpikeBlock extends DoublePlantBlock {
    public static final MapCodec<LegacyLargeIceSpikeBlock> CODEC = simpleCodec(LegacyLargeIceSpikeBlock::new);
    public static final EnumProperty<Direction> VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;

    public LegacyLargeIceSpikeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(VERTICAL_DIRECTION, Direction.UP));
    }

    @Override
    public MapCodec<LegacyLargeIceSpikeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VERTICAL_DIRECTION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        Direction direction = context.getClickedFace().getAxis() == Direction.Axis.Y
                ? context.getClickedFace()
                : Direction.UP;
        return state.setValue(VERTICAL_DIRECTION, direction);
    }
}
