package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Remembers corruption armor-debt sessions that Holiness has forgiven. */
public final class CorruptionLedgerData extends SavedData {

    public static final Codec<CorruptionLedgerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.BOOL)
                            .optionalFieldOf("forgiven_sessions", Map.of())
                            .forGetter(data -> data.forgivenSessions))
            .apply(instance, CorruptionLedgerData::new));
    public static final SavedDataType<CorruptionLedgerData> TYPE =
            new SavedDataType<>(TheAurorian2.id("corruption_ledger"), CorruptionLedgerData::new, CODEC);

    private final Map<String, Boolean> forgivenSessions;

    public CorruptionLedgerData() {
        this(Map.of());
    }

    private CorruptionLedgerData(Map<String, Boolean> forgivenSessions) {
        this.forgivenSessions = new HashMap<>(forgivenSessions);
    }

    public static void forgive(ServerLevel level, UUID owner, long sessionId) {
        if (sessionId == 0L) {
            return;
        }
        CorruptionLedgerData data = get(level);
        data.forgivenSessions.put(key(owner.toString(), sessionId), true);
        data.setDirty();
    }

    public static boolean isForgiven(ServerLevel level, String owner, long sessionId) {
        return sessionId != 0L && get(level).forgivenSessions.containsKey(key(owner, sessionId));
    }

    private static CorruptionLedgerData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    private static String key(String owner, long sessionId) {
        return owner + "/" + Long.toUnsignedString(sessionId);
    }
}
