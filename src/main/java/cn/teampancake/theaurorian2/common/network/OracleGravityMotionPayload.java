package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A captured player's movement uses the same clock as the lifted terrain. */
public record OracleGravityMotionPayload(int spellId, long start, double baseY, boolean active, boolean empowered) implements CustomPacketPayload {
    public OracleGravityMotionPayload(int spellId, long start, double baseY, boolean active) { this(spellId,start,baseY,active,false); }
    public static final Type<OracleGravityMotionPayload> TYPE = new Type<>(TheAurorian2.id("oracle_gravity_motion"));
    public static final StreamCodec<RegistryFriendlyByteBuf,OracleGravityMotionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OracleGravityMotionPayload::spellId,
            ByteBufCodecs.VAR_LONG, OracleGravityMotionPayload::start,
            ByteBufCodecs.DOUBLE, OracleGravityMotionPayload::baseY,
            ByteBufCodecs.BOOL, OracleGravityMotionPayload::active,
            ByteBufCodecs.BOOL, OracleGravityMotionPayload::empowered, OracleGravityMotionPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
