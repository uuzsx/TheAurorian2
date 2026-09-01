package cn.teampancake.theaurorian2.common.worldgen.placement;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class NotInUmbraDarkMazePlacement extends PlacementModifier {

    public static final NotInUmbraDarkMazePlacement INSTANCE = new NotInUmbraDarkMazePlacement();
    public static final MapCodec<NotInUmbraDarkMazePlacement> CODEC = MapCodec.unit(() -> INSTANCE);
    private static final TagKey<Structure> EXCLUDED_STRUCTURES = TagKey.create(
            Registries.STRUCTURE, TheAurorian2.id("blocks_natural_decorations"));

    private NotInUmbraDarkMazePlacement() {
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        return isInsideExcludedStructure(context.getLevel(), pos) ? Stream.empty() : Stream.of(pos);
    }

    public static boolean isInsideExcludedStructure(WorldGenLevel level, BlockPos pos) {
        return level.getLevel()
                .structureManager()
                .getStructureWithPieceAt(pos, EXCLUDED_STRUCTURES)
                .isValid();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.NOT_IN_UMBRA_DARK_MAZE.get();
    }
}
