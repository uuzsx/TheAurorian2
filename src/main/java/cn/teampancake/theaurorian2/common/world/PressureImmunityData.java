package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-wide Moon Queen progression used by Pressure immunity. */
public final class PressureImmunityData extends SavedData {

    public static final Codec<PressureImmunityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("moon_queen_defeated", false)
                            .forGetter(PressureImmunityData::isMoonQueenDefeated))
            .apply(instance, PressureImmunityData::new));
    public static final SavedDataType<PressureImmunityData> TYPE =
            new SavedDataType<>(TheAurorian2.id("pressure_immunity"), PressureImmunityData::new, CODEC);

    private boolean moonQueenDefeated;

    public PressureImmunityData() {
        this(false);
    }

    private PressureImmunityData(boolean moonQueenDefeated) {
        this.moonQueenDefeated = moonQueenDefeated;
    }

    public boolean isMoonQueenDefeated() {
        return this.moonQueenDefeated;
    }

    public static boolean isImmune(ServerLevel level) {
        return get(level.getServer()).isMoonQueenDefeated();
    }

    public static void markMoonQueenDefeated(MinecraftServer server) {
        PressureImmunityData data = get(server);
        if (!data.moonQueenDefeated) {
            data.moonQueenDefeated = true;
            data.setDirty();
        }
        server.getPlayerList().getPlayers().forEach(player -> player.removeEffect(ModMobEffects.PRESSURE));
    }

    private static PressureImmunityData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
