package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.world.AurorianBlessingCycle;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AstrologyForecastPayload(
        AurorianBlessingCycle.Blessing tomorrow,
        AurorianBlessingCycle.Blessing dayAfterTomorrow,
        AurorianBlessingCycle.Blessing thirdDay) implements CustomPacketPayload {

    public static final Type<AstrologyForecastPayload> TYPE =
            new Type<>(TheAurorian2.id("astrology_forecast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AstrologyForecastPayload> STREAM_CODEC =
            CustomPacketPayload.codec(AstrologyForecastPayload::write, AstrologyForecastPayload::new);

    private AstrologyForecastPayload(RegistryFriendlyByteBuf buffer) {
        this(readBlessing(buffer), readBlessing(buffer), readBlessing(buffer));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.tomorrow.slot());
        buffer.writeVarInt(this.dayAfterTomorrow.slot());
        buffer.writeVarInt(this.thirdDay.slot());
    }

    private static AurorianBlessingCycle.Blessing readBlessing(RegistryFriendlyByteBuf buffer) {
        return AurorianBlessingCycle.Blessing.fromSlot(buffer.readVarInt());
    }

    public List<AurorianBlessingCycle.Blessing> forecast() {
        return List.of(this.tomorrow, this.dayAfterTomorrow, this.thirdDay);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
