package cn.teampancake.theaurorian2.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.GlobalPos;

/** Player-owned state for the summons and the last safe route back to the Overworld. */
public record AurorianTravelData(
        boolean signalReceived,
        boolean enteredAurorian,
        Optional<GlobalPos> returnPoint,
        float returnYaw,
        float returnPitch) {

    public static final AurorianTravelData EMPTY =
            new AurorianTravelData(false, false, Optional.empty(), 0.0F, 0.0F);
    public static final Codec<AurorianTravelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("signal_received", false)
                            .forGetter(AurorianTravelData::signalReceived),
                    Codec.BOOL.optionalFieldOf("entered_aurorian", false)
                            .forGetter(AurorianTravelData::enteredAurorian),
                    GlobalPos.CODEC.optionalFieldOf("return_point")
                            .forGetter(AurorianTravelData::returnPoint),
                    Codec.FLOAT.optionalFieldOf("return_yaw", 0.0F)
                            .forGetter(AurorianTravelData::returnYaw),
                    Codec.FLOAT.optionalFieldOf("return_pitch", 0.0F)
                            .forGetter(AurorianTravelData::returnPitch))
            .apply(instance, AurorianTravelData::new));

    public AurorianTravelData receiveSignal() {
        return new AurorianTravelData(
                true, this.enteredAurorian, this.returnPoint, this.returnYaw, this.returnPitch);
    }

    public AurorianTravelData recordDeparture(GlobalPos point, float yaw, float pitch) {
        return new AurorianTravelData(true, true, Optional.of(point), yaw, pitch);
    }
}
