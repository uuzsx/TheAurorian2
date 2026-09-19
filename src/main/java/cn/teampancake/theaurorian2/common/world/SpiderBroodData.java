package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Counts eggs and offspring even while their chunks or mother are unloaded. */
public final class SpiderBroodData extends SavedData {

    private static final Codec<SpiderBroodData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC)
                            .optionalFieldOf("members", Map.of()).forGetter(data -> data.members))
            .apply(instance, SpiderBroodData::new));
    private static final SavedDataType<SpiderBroodData> TYPE = new SavedDataType<>(
            TheAurorian2.id("spider_broods"), () -> new SpiderBroodData(Map.of()), CODEC);
    private final Map<UUID, UUID> members;
    private final Map<UUID, Integer> counts = new HashMap<>();

    private SpiderBroodData(Map<UUID, UUID> members) {
        this.members = new HashMap<>(members);
        this.members.values().forEach(mother -> this.counts.merge(mother, 1, Integer::sum));
    }

    public static SpiderBroodData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int count(UUID mother) {
        return this.counts.getOrDefault(mother, 0);
    }

    public void add(UUID member, UUID mother) {
        if (mother.equals(this.members.get(member))) {
            return;
        }
        this.remove(member);
        this.members.put(member, mother);
        this.counts.merge(mother, 1, Integer::sum);
        this.setDirty();
    }

    public void remove(UUID member) {
        UUID mother = this.members.remove(member);
        if (mother != null) {
            this.counts.computeIfPresent(mother, (key, count) -> count <= 1 ? null : count - 1);
            this.setDirty();
        }
    }
}
