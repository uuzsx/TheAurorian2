package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AurorianTilling {
    private AurorianTilling() {
    }

    @SubscribeEvent
    public static void modifySoil(BlockEvent.BlockToolModificationEvent event) {
        if (!event.getItemAbility().equals(ItemAbilities.HOE_TILL)
                || !event.getHeldItemStack().canPerformAction(ItemAbilities.HOE_TILL)) {
            return;
        }
        var state = event.getState();
        if ((state.is(ModBlocks.AURORIAN_DIRT.get()) || ModBlocks.isAurorianGrassBlock(state))
                && event.getContext().getClickedFace() != Direction.DOWN
                && event.getLevel().getBlockState(event.getPos().above()).isAir()) {
            event.setFinalState(ModStructureBlocks.AURORIAN_FARM_TILE.get().defaultBlockState());
        }
    }
}
