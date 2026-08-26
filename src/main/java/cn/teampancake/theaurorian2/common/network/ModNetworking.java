package cn.teampancake.theaurorian2.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    private static final String NETWORK_VERSION = "3";

    private ModNetworking() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(AstrologyForecastPayload.TYPE, AstrologyForecastPayload.STREAM_CODEC);
        registrar.playToClient(PurificationRitualPromptPayload.TYPE, PurificationRitualPromptPayload.STREAM_CODEC);
        registrar.playToServer(
                PurificationRitualConfirmPayload.TYPE,
                PurificationRitualConfirmPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PurificationRitualConfirmPayload.handle(player, payload.pos());
                    }
                }));
        registrar.playToServer(
                PhantomBlossomAttackPayload.TYPE,
                PhantomBlossomAttackPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PhantomBlossomAttackPayload.handle(player);
                    }
                }));
        registrar.playToServer(
                RotateArtifactPayload.TYPE,
                RotateArtifactPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        RotateArtifactPayload.handle(player, payload.slot());
                    }
                }));
    }
}
