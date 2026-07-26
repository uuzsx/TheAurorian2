package cn.teampancake.theaurorian2.common.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    private static final String NETWORK_VERSION = "1";

    private ModNetworking() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(AstrologyForecastPayload.TYPE, AstrologyForecastPayload.STREAM_CODEC);
    }
}
