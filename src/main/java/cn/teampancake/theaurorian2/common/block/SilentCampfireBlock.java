package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.SilentCampfireBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class SilentCampfireBlock extends CampfireBlock {
    public SilentCampfireBlock(Properties properties) {
        super(true, 1, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SilentCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level instanceof ServerLevel serverLevel) {
            if (state.getValue(LIT)) {
                RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipes =
                        RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
                return createTickerHelper(
                        type,
                        ModBlockEntities.SILENT_CAMPFIRE.get(),
                        (innerLevel, pos, innerState, entity) ->
                                CampfireBlockEntity.cookTick(serverLevel, pos, innerState, entity, recipes));
            }
            return createTickerHelper(
                    type, ModBlockEntities.SILENT_CAMPFIRE.get(), CampfireBlockEntity::cooldownTick);
        }
        return state.getValue(LIT)
                ? createTickerHelper(
                        type, ModBlockEntities.SILENT_CAMPFIRE.get(), CampfireBlockEntity::particleTick)
                : null;
    }
}
