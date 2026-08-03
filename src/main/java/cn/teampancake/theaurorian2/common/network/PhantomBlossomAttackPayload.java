package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomRequiemItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PhantomBlossomAttackPayload() implements CustomPacketPayload {

    public static final PhantomBlossomAttackPayload INSTANCE = new PhantomBlossomAttackPayload();
    public static final Type<PhantomBlossomAttackPayload> TYPE =
            new Type<>(TheAurorian2.id("phantom_blossom_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhantomBlossomAttackPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public static void handle(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof PhantomBlossomRequiemItem item) {
            item.tryCastSendoff(player, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
