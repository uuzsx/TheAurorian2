package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PurificationRitualPromptPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PurificationRitualPromptPayload> TYPE =
            new Type<>(TheAurorian2.id("purification_ritual_prompt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurificationRitualPromptPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PurificationRitualPromptPayload::pos,
                    PurificationRitualPromptPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
