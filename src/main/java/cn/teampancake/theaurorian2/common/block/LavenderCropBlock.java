package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
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

/** Keeps the existing four crop stages while using vanilla crop growth and bonemeal. */
public final class LavenderCropBlock extends BeetrootBlock {
    public static final MapCodec<BeetrootBlock> CODEC = simpleCodec(LavenderCropBlock::new);
    private static final VoxelShape[] SHAPES = boxes(3, age -> column(16.0, 0.0, 4 + age * 4));

    public LavenderCropBlock(BlockBehaviour.Properties properties) {
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
        // Keep the crop's exclusive soil rule even if another block opts into canSustainPlant.
        return level.getBlockState(pos.below()).is(ModStructureBlocks.AURORIAN_FARM_TILE.get())
                && super.canSurvive(state, level, pos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModLegacyItems.LAVENDER_SEEDS.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[getAge(state)];
    }
}
