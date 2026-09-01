package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ArcaneMagicCircleBlockEntity extends BlockEntity {

    public ArcaneMagicCircleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_MAGIC_CIRCLE.get(), pos, state);
    }
}
