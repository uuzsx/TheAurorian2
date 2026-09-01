package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Starts or stops the ritual music for every client in the ritual's dimension. */
public record PurificationRitualMusicPayload(boolean playing) implements CustomPacketPayload {

    public static final Type<PurificationRitualMusicPayload> TYPE =
            new Type<>(TheAurorian2.id("purification_ritual_music"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurificationRitualMusicPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    PurificationRitualMusicPayload::playing,
                    PurificationRitualMusicPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
