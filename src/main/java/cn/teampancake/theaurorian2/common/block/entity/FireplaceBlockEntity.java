package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;

public final class FireplaceBlockEntity extends AbstractFurnaceBlockEntity {

    private static final Component DEFAULT_NAME = Component.translatable("container.theaurorian2.fireplace");

    public FireplaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIREPLACE.get(), pos, state, RecipeType.SMOKING);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected int getBurnDuration(FuelValues fuelValues, ItemStack stack) {
        return super.getBurnDuration(fuelValues, stack) / 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new SmokerMenu(containerId, inventory, this, this.dataAccess);
    }
}
