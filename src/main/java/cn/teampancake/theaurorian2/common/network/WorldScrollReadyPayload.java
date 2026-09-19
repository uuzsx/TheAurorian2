package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.WorldScrollTeleportEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** One acknowledgement per arrival, after the destination view is ready. */
public record WorldScrollReadyPayload(int effectId) implements CustomPacketPayload {
    public static final Type<WorldScrollReadyPayload> TYPE = new Type<>(TheAurorian2.id("world_scroll_ready"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorldScrollReadyPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, WorldScrollReadyPayload::effectId, WorldScrollReadyPayload::new);

    public static void handle(ServerPlayer player, int effectId) {
        WorldScrollTeleportEntity effect = WorldScrollTeleportEntity.active(player);
        if (effect != null && effect.getId() == effectId && effect.isWaitingForView()) effect.startArrival();
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
