package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class SacrificeTableBlockEntity extends ModelledBlockEntity {

    public SacrificeTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SACRIFICE_TABLE.get(), pos, state, null);
    }
}
