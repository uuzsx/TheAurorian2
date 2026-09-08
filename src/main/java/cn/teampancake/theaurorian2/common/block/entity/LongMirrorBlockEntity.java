package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render discovery only: no ticker, persistent payload, or custom network traffic. */
public final class LongMirrorBlockEntity extends BlockEntity {
    public LongMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LONG_MIRROR.get(), pos, state);
    }
}
