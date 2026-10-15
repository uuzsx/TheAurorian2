package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.ArcanePrismItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Intent only: aim uses vanilla player rotation; damage, ownership and cooldown stay server-side. */
public record ArcanePrismAttackPayload() implements CustomPacketPayload {
    public static final ArcanePrismAttackPayload INSTANCE = new ArcanePrismAttackPayload();
    public static final Type<ArcanePrismAttackPayload> TYPE = new Type<>(TheAurorian2.id("arcane_prism_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcanePrismAttackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static void handle(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof ArcanePrismItem item) item.tryCast(player);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
