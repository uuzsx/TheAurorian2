package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class ArcanePrismEvents {
    private ArcanePrismEvents() {}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ArcanePrismItem.restoreCooldown(player);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ArcanePrismItem.restoreCooldown(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ArcanePrismItem.restoreCooldown(player);
    }
}
