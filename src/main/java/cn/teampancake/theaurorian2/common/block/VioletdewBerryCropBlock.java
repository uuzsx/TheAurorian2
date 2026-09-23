package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModItems;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Four-stage crop planted with seeds on Aurorian farmland. */
public final class VioletdewBerryCropBlock extends BeetrootBlock {
    public static final MapCodec<BeetrootBlock> CODEC = simpleCodec(VioletdewBerryCropBlock::new);
    private static final VoxelShape[] SHAPES = {
            column(16.0, 0.0, 7.0), column(16.0, 0.0, 11.0),
            column(16.0, 0.0, 14.0), column(16.0, 0.0, 16.0)
    };

    public VioletdewBerryCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BeetrootBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModStructureBlocks.AURORIAN_FARM_TILE.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModStructureBlocks.AURORIAN_FARM_TILE.get())
                && super.canSurvive(state, level, pos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.VIOLETDEW_BERRY_SEEDS.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[getAge(state)];
    }
}
