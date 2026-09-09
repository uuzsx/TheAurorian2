package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Retains barrel opening, loot and inventory persistence with a six-row inventory. */
public final class DoubleStorageCrateBlockEntity extends BarrelBlockEntity {
    public DoubleStorageCrateBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        setItems(NonNullList.withSize(getContainerSize(), ItemStack.EMPTY));
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.DOUBLE_STORAGE_CRATE.get();
    }

    @Override
    public int getContainerSize() { return 54; }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.sixRows(containerId, inventory, this);
    }
}
