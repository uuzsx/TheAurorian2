package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.network.PurificationRitualMusicPayload;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-thread ownership of active ritual tracks; no world scans or per-tick packets. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class PurificationRitualMusicState {
    private static final Set<GlobalPos> ACTIVE = new HashSet<>();
    private PurificationRitualMusicState() {}

    public static void setPlaying(ServerLevel level, BlockPos pos, boolean playing) {
        GlobalPos altar = GlobalPos.of(level.dimension(), pos.immutable());
        if (playing ? ACTIVE.add(altar) : ACTIVE.remove(altar)) {
            PacketDistributor.sendToPlayersInDimension(level, new PurificationRitualMusicPayload(altar, playing));
        }
    }

    private static void sync(ServerPlayer player) {
        for (GlobalPos altar : ACTIVE) {
            if (altar.dimension().equals(player.level().dimension())) {
                PacketDistributor.sendToPlayer(player, new PurificationRitualMusicPayload(altar, true));
            }
        }
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }
}
