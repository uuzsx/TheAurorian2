package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlockTags;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class DarkStoneProtection {

    private DarkStoneProtection() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof Player player && isUnauthorized(event.getState(), player)) {
            event.setCanceled(true);
            event.setNewSpeed(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        if (isUnauthorized(event.getState(), event.getPlayer())) {
            event.setNotifyClient(true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos ->
                event.getLevel().getBlockState(pos).is(ModBlockTags.QUEENS_PICKAXE_PROTECTED));
    }

    private static boolean isUnauthorized(BlockState state, Player player) {
        return state.is(ModBlockTags.QUEENS_PICKAXE_PROTECTED)
                && !player.isCreative()
                && !player.getMainHandItem().is(ModLegacyItems.QUEENS_PICKAXE.get());
    }
}
