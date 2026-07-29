package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AurorianTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AurorianTableEvents {

    private AurorianTableEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || event.getEntity().isSpectator()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos masterPos = findMasterPos(level, event.getPos());
        if (masterPos == null) {
            return;
        }

        BlockState masterState = level.getBlockState(masterPos);
        if (!(masterState.getBlock() instanceof AurorianTableBlock tableBlock)
                || !(level.getBlockEntity(masterPos) instanceof AurorianTableBlockEntity table)
                || table.getDisplayedItem().isEmpty()) {
            return;
        }

        event.setCanceled(true);
        if (!level.isClientSide()) {
            tableBlock.dropDisplayedItem(level, masterPos);
        }
    }

    private static @Nullable BlockPos findMasterPos(Level level, BlockPos clickedPos) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.getBlock() instanceof AurorianTableBlock) {
            return clickedPos;
        }
        if (clickedState.getBlock() instanceof AurorianTablePartBlock) {
            return AurorianTableBlock.masterPos(clickedPos, clickedState.getValue(AurorianTablePartBlock.PART));
        }
        return null;
    }
}
