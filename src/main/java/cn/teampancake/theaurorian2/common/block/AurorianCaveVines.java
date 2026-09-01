package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

final class AurorianCaveVines {

    private AurorianCaveVines() {
    }

    static InteractionResult use(Entity entity, BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(CaveVines.BERRIES)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, pos, new ItemStack(ModItems.DEW_FRUIT.get()));
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.CAVE_VINES_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    Mth.randomBetween(serverLevel.getRandom(), 0.8F, 1.2F));
            BlockState harvestedState = state.setValue(CaveVines.BERRIES, false);
            serverLevel.setBlock(pos, harvestedState, Block.UPDATE_CLIENTS);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, harvestedState));
        }

        return InteractionResult.SUCCESS;
    }
}
