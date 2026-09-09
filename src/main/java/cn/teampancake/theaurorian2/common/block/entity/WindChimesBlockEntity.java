package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class WindChimesBlockEntity extends ModelledBlockEntity {
    public WindChimesBlockEntity(BlockPos pos, BlockState state) {
        super(state.is(ModBlocks.AMETHYST_WIND_CHIMES.get()) ? ModBlockEntities.AMETHYST_WIND_CHIMES.get()
                : ModBlockEntities.BAMBOO_WIND_CHIMES.get(), pos, state, "idle");
    }
}
