package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class AurorianCaveVinesBlock extends CaveVinesBlock {

    public AurorianCaveVinesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.DEW_CAVE_VINES_PLANT.get();
    }

    @Override
    protected BlockState getGrowIntoState(BlockState growFromState, RandomSource random) {
        return super.getGrowIntoState(growFromState, random).setValue(BERRIES, false);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, net.minecraft.core.BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ModBlocks.DEW_CAVE_VINES.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, net.minecraft.core.BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, net.minecraft.core.BlockPos pos, BlockState state) {
        return false;
    }
}
