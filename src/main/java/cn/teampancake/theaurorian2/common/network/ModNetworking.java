package cn.teampancake.theaurorian2.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    private static final String NETWORK_VERSION = "2";

    private ModNetworking() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(AstrologyForecastPayload.TYPE, AstrologyForecastPayload.STREAM_CODEC);
        registrar.playToServer(
                PhantomBlossomAttackPayload.TYPE,
                PhantomBlossomAttackPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PhantomBlossomAttackPayload.handle(player);
                    }
                }));
    }
}
