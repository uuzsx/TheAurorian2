package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.registry.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.jspecify.annotations.Nullable;

/**
 * Gives the oversized dungeon nine starts sharing one random-spread center.
 * Each start owns a bounded part of the building, keeping vanilla's structure
 * reference radius sufficient without changing structure generation globally.
 */
public final class RunestoneDungeonPlacement extends RandomSpreadStructurePlacement {

    private static final int SHARD_DISTANCE = 6;

    // Do not include placementCodec: per-chunk frequency or exclusion checks
    // could accept some shards while rejecting the rest of the same dungeon.
    public static final MapCodec<RunestoneDungeonPlacement> CODEC =
            RecordCodecBuilder.<RunestoneDungeonPlacement>mapCodec(instance -> instance.group(
                    Codec.intRange(32, 4096).fieldOf("spacing")
                            .forGetter(RunestoneDungeonPlacement::spacing),
                    Codec.intRange(16, 4096).fieldOf("separation")
                            .forGetter(RunestoneDungeonPlacement::separation),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt")
                            .forGetter(RunestoneDungeonPlacement::salt)
            ).apply(instance, RunestoneDungeonPlacement::new))
                    .validate(RunestoneDungeonPlacement::validate);

    public RunestoneDungeonPlacement(int spacing, int separation, int salt) {
        super(spacing, separation, RandomSpreadType.LINEAR, salt);
    }

    private static DataResult<RunestoneDungeonPlacement> validate(RunestoneDungeonPlacement placement) {
        return placement.spacing() > placement.separation()
                ? DataResult.success(placement)
                : DataResult.error(() -> "Spacing has to be larger than separation");
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int x, int z) {
        return this.anchorFor(state.getLevelSeed(), x, z) != null;
    }

    /** Returns a shard's shared center and world-aligned column/row, or null outside its nine starts. */
    public @Nullable Anchor anchorFor(long seed, int x, int z) {
        // Shifting by six keeps all nine starts inside their parent's grid cell:
        // random offset + shard offset + six is in [0, spacing - separation + 11].
        // With separation >= 16 this also holds across negative grid boundaries.
        ChunkPos center = super.getPotentialStructureChunk(seed, x + SHARD_DISTANCE, z + SHARD_DISTANCE);
        int dx = x - center.x();
        int dz = z - center.z();
        if (!isShardOffset(dx) || !isShardOffset(dz)) {
            return null;
        }
        return new Anchor(center, dx / SHARD_DISTANCE + 1, dz / SHARD_DISTANCE + 1);
    }

    private static boolean isShardOffset(int offset) {
        return offset == -SHARD_DISTANCE || offset == 0 || offset == SHARD_DISTANCE;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.RUNESTONE_DUNGEON_PLACEMENT.get();
    }

    public record Anchor(ChunkPos center, int column, int row) {
    }
}
