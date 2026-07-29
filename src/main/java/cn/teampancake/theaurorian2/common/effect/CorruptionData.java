package cn.teampancake.theaurorian2.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CorruptionData(float healthDebt, int foodDebt, float saturationDebt, long sessionId) {

    public static final CorruptionData EMPTY = new CorruptionData(0.0F, 0, 0.0F, 0L);
    public static final Codec<CorruptionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.optionalFieldOf("health_debt", 0.0F).forGetter(CorruptionData::healthDebt),
                    Codec.INT.optionalFieldOf("food_debt", 0).forGetter(CorruptionData::foodDebt),
                    Codec.FLOAT.optionalFieldOf("saturation_debt", 0.0F).forGetter(CorruptionData::saturationDebt),
                    Codec.LONG.optionalFieldOf("session_id", 0L).forGetter(CorruptionData::sessionId))
            .apply(instance, CorruptionData::new));

    public static CorruptionData begin(long sessionId) {
        return new CorruptionData(0.0F, 0, 0.0F, sessionId);
    }

    public CorruptionData addHealth(float amount) {
        return new CorruptionData(
                Math.max(0.0F, this.healthDebt + amount),
                this.foodDebt,
                this.saturationDebt,
                this.sessionId);
    }

    public CorruptionData addFood(int food, float saturation) {
        return new CorruptionData(
                this.healthDebt,
                Math.max(0, this.foodDebt + food),
                Math.max(0.0F, this.saturationDebt + saturation),
                this.sessionId);
    }

    public boolean isEmpty() {
        return this.healthDebt <= 0.0F && this.foodDebt <= 0 && this.saturationDebt <= 0.0F;
    }
}
