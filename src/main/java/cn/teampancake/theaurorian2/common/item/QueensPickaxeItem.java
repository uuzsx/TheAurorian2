package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.registry.ModBlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;

public final class QueensPickaxeItem extends Item {

    private static final float PROTECTED_BLOCK_SPEED = 12.0F;

    public QueensPickaxeItem(Properties properties) {
        super(properties.pickaxe(ToolMaterial.NETHERITE, 1.0F, -2.8F));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(ModBlockTags.QUEENS_PICKAXE_PROTECTED)
                ? PROTECTED_BLOCK_SPEED
                : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(ModBlockTags.QUEENS_PICKAXE_PROTECTED) || super.isCorrectToolForDrops(stack, state);
    }
}
