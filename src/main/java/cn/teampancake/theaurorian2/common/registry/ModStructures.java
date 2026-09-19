package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.structure.AurorianSkyIslandPiece;
import cn.teampancake.theaurorian2.common.worldgen.structure.AurorianSkyIslandStructure;
import cn.teampancake.theaurorian2.common.worldgen.structure.AurorianMineshaftStructure;
import cn.teampancake.theaurorian2.common.worldgen.structure.UmbraDarkMazePiece;
import cn.teampancake.theaurorian2.common.worldgen.structure.UmbraDarkMazeStructure;
import cn.teampancake.theaurorian2.common.worldgen.structure.RunestoneDungeonStructure;
import cn.teampancake.theaurorian2.common.worldgen.structure.RunestoneDungeonPiece;
import cn.teampancake.theaurorian2.common.worldgen.structure.RunestoneDungeonPlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_TYPE, TheAurorian2.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PIECE, TheAurorian2.MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PLACEMENT, TheAurorian2.MOD_ID);
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<RunestoneDungeonPlacement>>
            RUNESTONE_DUNGEON_PLACEMENT = STRUCTURE_PLACEMENTS.register(
                    "runestone_dungeon", () -> () -> RunestoneDungeonPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<RunestoneDungeonStructure>> RUNESTONE_DUNGEON =
            STRUCTURE_TYPES.register("runestone_dungeon", () -> () -> RunestoneDungeonStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> RUNESTONE_DUNGEON_PIECE =
            STRUCTURE_PIECES.register("runestone_dungeon", () -> RunestoneDungeonPiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<AurorianSkyIslandStructure>>
            AURORIAN_SKY_ISLAND_GROUP = STRUCTURE_TYPES.register(
                    "aurorian_sky_island_group", () -> () -> AurorianSkyIslandStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<AurorianMineshaftStructure>>
            AURORIAN_MINESHAFT = STRUCTURE_TYPES.register(
                    "aurorian_mineshaft", () -> () -> AurorianMineshaftStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<UmbraDarkMazeStructure>> UMBRA_DARK_MAZE =
            STRUCTURE_TYPES.register("umbra_dark_maze", () -> () -> UmbraDarkMazeStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> AURORIAN_SKY_ISLAND_GROUP_PIECE =
            STRUCTURE_PIECES.register(
                    "aurorian_sky_island_group", () -> (context, tag) -> new AurorianSkyIslandPiece(tag));
    public static final DeferredHolder<StructurePieceType, StructurePieceType> UMBRA_DARK_MAZE_PIECE =
            STRUCTURE_PIECES.register("umbra_dark_maze", () -> (context, tag) -> new UmbraDarkMazePiece(tag));

    private ModStructures() {
    }

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECES.register(modEventBus);
        STRUCTURE_PLACEMENTS.register(modEventBus);
    }
}
