package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianCeilingHangingSignBlock;
import cn.teampancake.theaurorian2.common.block.AurorianFurnaceChimneyBlock;
import cn.teampancake.theaurorian2.common.block.AurorianPlantBlock;
import cn.teampancake.theaurorian2.common.block.AurorianStandingSignBlock;
import cn.teampancake.theaurorian2.common.block.AurorianWallHangingSignBlock;
import cn.teampancake.theaurorian2.common.block.AurorianWallSignBlock;
import cn.teampancake.theaurorian2.common.block.CrystallineSwordPedestalBlock;
import cn.teampancake.theaurorian2.common.block.DarkStoneGateBlock;
import cn.teampancake.theaurorian2.common.block.DarkStoneGateKeyholeBlock;
import cn.teampancake.theaurorian2.common.block.LegacyAgeThreeCropBlock;
import cn.teampancake.theaurorian2.common.block.LegacyFacingBlock;
import cn.teampancake.theaurorian2.common.block.LegacyHorizontalFacingBlock;
import cn.teampancake.theaurorian2.common.block.LegacyLargeIceSpikeBlock;
import cn.teampancake.theaurorian2.common.block.LegacyLevelPlantBlock;
import cn.teampancake.theaurorian2.common.block.LegacyMoistureBlock;
import cn.teampancake.theaurorian2.common.block.LegacyPortalBlock;
import cn.teampancake.theaurorian2.common.block.LegacyVerticalDirectionBlock;
import cn.teampancake.theaurorian2.common.block.LockedStructureBlock;
import cn.teampancake.theaurorian2.common.block.SacrificeTableBlock;
import cn.teampancake.theaurorian2.common.block.SilentCampfireBlock;
import cn.teampancake.theaurorian2.common.block.SpiderMotherBarrierBlock;
import cn.teampancake.theaurorian2.common.block.SpiderMotherSpawnerBlock;
import cn.teampancake.theaurorian2.common.block.VerticalSlabBlock;
import cn.teampancake.theaurorian2.common.block.VerticalStairBlock;
import cn.teampancake.theaurorian2.common.item.ModelledBlockItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModStructureBlocks {
    private static final List<DeferredBlock<? extends Block>> BUILDING_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> NATURAL_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> DECORATIVE_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> FUNCTIONAL_BLOCKS = new ArrayList<>();
    private static final Map<String, DeferredBlock<? extends Block>> BLOCKS_BY_ID = new LinkedHashMap<>();

    private static final List<String> LEGACY_VERTICAL_STAIR_IDS = List.of(
            "vertical_aurorian_castle_rune_stone_stairs",
            "vertical_aurorian_cobblestone_stairs",
            "vertical_aurorian_granite_stairs",
            "vertical_aurorian_peridotite_stairs",
            "vertical_cerulean_castle_rune_stone_stairs",
            "vertical_chiseled_dark_stone_brick_stairs",
            "vertical_chiseled_moon_temple_brick_stairs",
            "vertical_chiseled_rune_stone_stairs",
            "vertical_crystalline_castle_rune_stone_stairs",
            "vertical_cursed_frost_wood_stairs",
            "vertical_curtain_wood_stairs",
            "vertical_dark_stone_brick_stairs",
            "vertical_luminous_aurorian_castle_rune_stone_stairs",
            "vertical_luminous_cerulean_castle_rune_stone_stairs",
            "vertical_luminous_crystalline_castle_rune_stone_stairs",
            "vertical_luminous_moon_castle_rune_stone_stairs",
            "vertical_luminous_moonsilver_castle_rune_stone_stairs",
            "vertical_moon_castle_rune_stone_stairs",
            "vertical_moonsilver_castle_rune_stone_stairs",
            "vertical_rune_stone_stairs",
            "vertical_smooth_aurorian_peridotite_stairs",
            "vertical_smooth_dark_stone_brick_stairs",
            "vertical_smooth_moon_temple_brick_stairs",
            "vertical_smooth_rune_stone_stairs",
            "vertical_transparent_rune_stone_stairs",
            "vertical_umbra_castle_rune_stone_stairs",
            "vertical_umbra_stone_cracked_stairs",
            "vertical_umbra_stone_roof_stairs",
            "vertical_umbra_stone_stairs");

    private static final List<String> LEGACY_VERTICAL_SLAB_IDS = List.of(
            "vertical_aurorian_castle_rune_stone_slab",
            "vertical_aurorian_cobblestone_slab",
            "vertical_aurorian_granite_slab",
            "vertical_aurorian_peridotite_slab",
            "vertical_cerulean_castle_rune_stone_slab",
            "vertical_chiseled_dark_stone_brick_slab",
            "vertical_chiseled_rune_stone_slab",
            "vertical_crystalline_castle_rune_stone_slab",
            "vertical_cursed_frost_wood_slab",
            "vertical_curtain_wood_slab",
            "vertical_dark_stone_brick_slab",
            "vertical_luminous_aurorian_castle_rune_stone_slab",
            "vertical_luminous_cerulean_castle_rune_stone_slab",
            "vertical_luminous_crystalline_castle_rune_stone_slab",
            "vertical_luminous_moon_castle_rune_stone_slab",
            "vertical_luminous_moonsilver_castle_rune_stone_slab",
            "vertical_moon_castle_rune_stone_slab",
            "vertical_moonsilver_castle_rune_stone_slab",
            "vertical_rune_stone_slab",
            "vertical_smooth_aurorian_peridotite_slab",
            "vertical_smooth_dark_stone_brick_slab",
            "vertical_smooth_moon_temple_brick_slab",
            "vertical_smooth_rune_stone_slab",
            "vertical_transparent_rune_stone_slab",
            "vertical_umbra_castle_rune_stone_slab",
            "vertical_umbra_stone_cracked_slab",
            "vertical_umbra_stone_roof_slab",
            "vertical_umbra_stone_slab");

    private static final List<String> LEGACY_STAIR_IDS = List.of(
            "aurorian_andesite_stairs",
            "aurorian_castle_rune_stone_stairs",
            "aurorian_granite_stairs",
            "cerulean_castle_rune_stone_stairs",
            "chiseled_dark_stone_brick_stairs",
            "chiseled_moon_temple_brick_stairs",
            "chiseled_rune_stone_stairs",
            "crystalline_castle_rune_stone_stairs",
            "dark_stone_brick_stairs",
            "luminous_aurorian_castle_rune_stone_stairs",
            "luminous_cerulean_castle_rune_stone_stairs",
            "luminous_crystalline_castle_rune_stone_stairs",
            "luminous_moon_castle_rune_stone_stairs",
            "luminous_moonsilver_castle_rune_stone_stairs",
            "moon_castle_rune_stone_stairs",
            "moonsilver_castle_rune_stone_stairs",
            "smooth_dark_stone_brick_stairs",
            "transparent_rune_stone_stairs",
            "umbra_castle_rune_stone_stairs",
            "umbra_stone_cracked_stairs",
            "umbra_stone_roof_stairs",
            "umbra_stone_stairs");

    private static final List<String> LEGACY_SLAB_IDS = List.of(
            "aurorian_castle_rune_stone_slab",
            "aurorian_granite_slab",
            "cerulean_castle_rune_stone_slab",
            "chiseled_dark_stone_brick_slab",
            "chiseled_moon_temple_brick_slab",
            "crystalline_castle_rune_stone_slab",
            "dark_stone_brick_slab",
            "luminous_aurorian_castle_rune_stone_slab",
            "luminous_cerulean_castle_rune_stone_slab",
            "luminous_crystalline_castle_rune_stone_slab",
            "luminous_moon_castle_rune_stone_slab",
            "luminous_moonsilver_castle_rune_stone_slab",
            "moon_castle_rune_stone_slab",
            "moonsilver_castle_rune_stone_slab",
            "rune_stone_slab",
            "smooth_dark_stone_brick_slab",
            "transparent_rune_stone_slab",
            "umbra_castle_rune_stone_slab",
            "umbra_stone_cracked_slab",
            "umbra_stone_roof_slab",
            "umbra_stone_slab");

    private static final List<String> LEGACY_WALL_IDS = List.of(
            "aurorian_castle_rune_stone_wall",
            "cerulean_castle_rune_stone_wall",
            "chiseled_dark_stone_brick_wall",
            "chiseled_moon_temple_brick_wall",
            "crystalline_castle_rune_stone_wall",
            "dark_stone_brick_wall",
            "luminous_aurorian_castle_rune_stone_wall",
            "luminous_cerulean_castle_rune_stone_wall",
            "luminous_crystalline_castle_rune_stone_wall",
            "luminous_moon_castle_rune_stone_wall",
            "luminous_moonsilver_castle_rune_stone_wall",
            "moon_castle_rune_stone_wall",
            "moonsilver_castle_rune_stone_wall",
            "smooth_dark_stone_brick_wall",
            "transparent_rune_stone_wall",
            "umbra_castle_rune_stone_wall",
            "umbra_stone_cracked_wall",
            "umbra_stone_roof_wall",
            "umbra_stone_wall");

    private static final List<String> LEGACY_FULL_BLOCK_IDS = List.of(
            "aurorian_barrier_stone",
            "aurorian_castle_rune_stone",
            "aurorian_coal_block",
            "cerulean_block",
            "cerulean_castle_rune_stone",
            "chiseled_dark_stone_bricks",
            "crystalline_castle_rune_stone",
            "dark_stone_bricks",
            "dark_stone_fancy",
            "dark_stone_lamp",
            "dark_stone_layers",
            "filthy_ice",
            "indigo_mushroom_crystal",
            "luminous_aurorian_castle_rune_stone",
            "luminous_cerulean_castle_rune_stone",
            "luminous_crystalline_castle_rune_stone",
            "luminous_moon_castle_rune_stone",
            "luminous_moonsilver_castle_rune_stone",
            "moon_castle_rune_stone",
            "moon_gem",
            "moon_sandstone",
            "moonsilver_block",
            "moonsilver_castle_rune_stone",
            "moonstone_block",
            "mysterium_wool",
            "red_aurorian_grass_block",
            "smooth_dark_stone_bricks",
            "umbra_castle_rune_stone",
            "umbra_stone",
            "umbra_stone_cracked",
            "umbra_stone_roof_tiles",
            "void_stone");

    private static final Set<String> LEGACY_STANDARD_BUILDING_IDS = Set.of(
            "aurorian_andesite_stairs",
            "aurorian_coal_block",
            "aurorian_granite_slab",
            "aurorian_granite_stairs",
            "cerulean_block",
            "moon_gem",
            "moon_sandstone",
            "moonsilver_block",
            "moonstone_block",
            "mysterium_wool");

    // These use dedicated state-compatible placeholders below, but remain part of the import catalog.
    private static final List<String> LEGACY_SPECIAL_IDS = List.of(
            "aurorian_farm_tile",
            "aurorian_portal",
            "mystical_barrier",
            "aurorian_glass_pane",
            "dark_stone_bars",
            "dark_stone_glass",
            "dark_stone_glass_pane",
            "dark_stone_pillar",
            "rune_stone_pillar",
            "dark_stone_gate",
            "moon_temple_cell_gate",
            "rune_stone_loot_gate",
            "dark_stone_gate_keyhole",
            "moon_temple_cell_gate_keyhole",
            "moon_temple_gate_keyhole",
            "rune_stone_loot_gate_keyhole",
            "moonlight_forge",
            "relic_table",
            "sacrifice_table",
            "scrapper",
            "crystalline_sword_pedestal",
            "laser_crystal",
            "urn",
            "lavender_crop",
            "silk_berry_crop",
            "wick_grass",
            "large_filthy_ice_spike",
            "medium_filthy_ice_spike",
            "small_filthy_ice_spike",
            "indigo_mushroom_stem",
            "moon_torch",
            "moon_wall_torch",
            "potted_aurorian_grass_light",
            "potted_cursed_frost_tree_sapling",
            "potted_curtain_tree_sapling",
            "potted_wick_grass",
            "molten_cerulean",
            "molten_moonsilver",
            "molten_moonstone",
            "cursed_frost_wood_hanging_sign",
            "cursed_frost_wood_sign",
            "cursed_frost_wood_wall_hanging_sign",
            "cursed_frost_wood_wall_sign",
            "curtain_wood_hanging_sign",
            "curtain_wood_sign",
            "curtain_wood_wall_hanging_sign",
            "curtain_wood_wall_sign");

    public static final BlockSetType SILENT_BLOCK_SET =
            BlockSetType.register(new BlockSetType("theaurorian2_silent"));
    public static final BlockSetType WEEPING_WILLOW_BLOCK_SET =
            BlockSetType.register(new BlockSetType("theaurorian2_weeping_willow"));
    public static final BlockSetType CURTAIN_BLOCK_SET =
            BlockSetType.register(new BlockSetType("theaurorian2_curtain"));
    public static final BlockSetType CURSED_FROST_BLOCK_SET =
            BlockSetType.register(new BlockSetType("theaurorian2_cursed_frost"));
    public static final WoodType SILENT_WOOD_TYPE = WoodType.register(
            new WoodType(TheAurorian2.MOD_ID + ":silent", SILENT_BLOCK_SET));
    public static final WoodType WEEPING_WILLOW_WOOD_TYPE = WoodType.register(
            new WoodType(TheAurorian2.MOD_ID + ":weeping_willow", WEEPING_WILLOW_BLOCK_SET));
    public static final WoodType CURTAIN_WOOD_TYPE = WoodType.register(
            new WoodType(TheAurorian2.MOD_ID + ":curtain", CURTAIN_BLOCK_SET));
    public static final WoodType CURSED_FROST_WOOD_TYPE = WoodType.register(
            new WoodType(TheAurorian2.MOD_ID + ":cursed_frost", CURSED_FROST_BLOCK_SET));

    public static final DeferredBlock<Block> AURORIAN_STONE_BRICKS = stone("aurorian_stone_bricks");
    public static final DeferredBlock<StairBlock> AURORIAN_STONE_STAIRS =
            stairs("aurorian_stone_stairs", ModBlocks.AURORIAN_STONE);
    public static final DeferredBlock<SlabBlock> AURORIAN_STONE_SLAB = slab("aurorian_stone_slab");
    public static final DeferredBlock<WallBlock> AURORIAN_STONE_WALL = wall("aurorian_stone_wall");
    public static final DeferredBlock<StairBlock> AURORIAN_STONE_BRICK_STAIRS =
            stairs("aurorian_stone_brick_stairs", AURORIAN_STONE_BRICKS);
    public static final DeferredBlock<SlabBlock> AURORIAN_STONE_BRICK_SLAB = slab("aurorian_stone_brick_slab");
    public static final DeferredBlock<WallBlock> AURORIAN_STONE_BRICK_WALL = wall("aurorian_stone_brick_wall");

    public static final DeferredBlock<SlabBlock> AURORIAN_ANDESITE_SLAB = slab("aurorian_andesite_slab");
    public static final DeferredBlock<WallBlock> AURORIAN_ANDESITE_WALL = wall("aurorian_andesite_wall");
    public static final DeferredBlock<StairBlock> AURORIAN_DIORITE_STAIRS =
            stairs("aurorian_diorite_stairs", ModBlocks.AURORIAN_DIORITE);
    public static final DeferredBlock<SlabBlock> AURORIAN_DIORITE_SLAB = slab("aurorian_diorite_slab");
    public static final DeferredBlock<WallBlock> AURORIAN_DIORITE_WALL = wall("aurorian_diorite_wall");
    public static final DeferredBlock<WallBlock> AURORIAN_GRANITE_WALL = wall("aurorian_granite_wall");
    public static final DeferredBlock<StairBlock> AURORIAN_PERIDOTITE_STAIRS =
            stairs("aurorian_peridotite_stairs", ModBlocks.AURORIAN_PERIDOTITE);
    public static final DeferredBlock<SlabBlock> AURORIAN_PERIDOTITE_SLAB = slab("aurorian_peridotite_slab");
    public static final DeferredBlock<WallBlock> AURORIAN_PERIDOTITE_WALL = wall("aurorian_peridotite_wall");
    public static final DeferredBlock<Block> SMOOTH_AURORIAN_PERIDOTITE =
            building("smooth_aurorian_peridotite", Block::new, stoneProperties(5.0F));
    public static final DeferredBlock<StairBlock> SMOOTH_AURORIAN_PERIDOTITE_STAIRS =
            stairs("smooth_aurorian_peridotite_stairs", SMOOTH_AURORIAN_PERIDOTITE);
    public static final DeferredBlock<SlabBlock> SMOOTH_AURORIAN_PERIDOTITE_SLAB =
            slab("smooth_aurorian_peridotite_slab");
    public static final DeferredBlock<WallBlock> SMOOTH_AURORIAN_PERIDOTITE_WALL =
            wall("smooth_aurorian_peridotite_wall");

    public static final DeferredBlock<SandBlock> MOON_SAND = building(
            "moon_sand", properties -> new SandBlock(new ColorRGBA(0xFFC6B5DA), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));
    public static final DeferredBlock<SandBlock> BRIGHT_MOON_SAND = building(
            "bright_moon_sand", properties -> new SandBlock(new ColorRGBA(0xFFE5DCF2), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));
    public static final DeferredBlock<Block> BRIGHT_MOON_SANDSTONE = building(
            "bright_moon_sandstone", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE));
    public static final DeferredBlock<Block> CUT_MOON_SANDSTONE = building(
            "cut_moon_sandstone", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_SANDSTONE));

    public static final DeferredBlock<TransparentBlock> AURORIAN_GLASS = building(
            "aurorian_glass", TransparentBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
    public static final DeferredBlock<TransparentBlock> MOON_GLASS = building(
            "moon_glass", TransparentBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
    public static final DeferredBlock<IronBarsBlock> MOON_GLASS_PANE = building(
            "moon_glass_pane", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE));

    public static final DeferredBlock<Block> RUNE_STONE = decorativeRuneStone("rune_stone");
    public static final DeferredBlock<StairBlock> RUNE_STONE_STAIRS =
            decorativeStairs("rune_stone_stairs", RUNE_STONE);
    public static final DeferredBlock<WallBlock> RUNE_STONE_WALL = decorativeWall("rune_stone_wall");
    public static final DeferredBlock<Block> SMOOTH_RUNE_STONE = decorativeRuneStone("smooth_rune_stone");
    public static final DeferredBlock<StairBlock> SMOOTH_RUNE_STONE_STAIRS =
            decorativeStairs("smooth_rune_stone_stairs", SMOOTH_RUNE_STONE);
    public static final DeferredBlock<SlabBlock> SMOOTH_RUNE_STONE_SLAB =
            decorativeSlab("smooth_rune_stone_slab");
    public static final DeferredBlock<WallBlock> SMOOTH_RUNE_STONE_WALL =
            decorativeWall("smooth_rune_stone_wall");
    public static final DeferredBlock<Block> CHISELED_RUNE_STONE = decorativeRuneStone("chiseled_rune_stone");
    public static final DeferredBlock<SlabBlock> CHISELED_RUNE_STONE_SLAB =
            decorativeSlab("chiseled_rune_stone_slab");
    public static final DeferredBlock<WallBlock> CHISELED_RUNE_STONE_WALL =
            decorativeWall("chiseled_rune_stone_wall");
    public static final DeferredBlock<Block> TRANSPARENT_RUNE_STONE = decorative(
            "transparent_rune_stone", Block::new, runeProperties(5.0F));
    public static final DeferredBlock<Block> RUNE_STONE_LAMP = decorative(
            "rune_stone_lamp", Block::new,
            () -> runeProperties(5.0F).get().lightLevel(state -> 15));
    public static final DeferredBlock<Block> RUNE_CRYSTAL = decorative(
            "rune_crystal", Block::new,
            () -> runeProperties(5.0F).get().lightLevel(state -> 3));
    public static final DeferredBlock<IronBarsBlock> RUNE_STONE_BARS = decorative(
            "rune_stone_bars", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS));
    public static final DeferredBlock<LockedStructureBlock> RUNE_STONE_GATE = locked("rune_stone_gate");
    public static final DeferredBlock<LockedStructureBlock> RUNE_STONE_GATE_KEYHOLE =
            locked("rune_stone_gate_keyhole");

    public static final DeferredBlock<RotatedPillarBlock> MOON_TEMPLE_PILLAR = decorative(
            "moon_temple_pillar", RotatedPillarBlock::new, runeProperties(5.0F));
    public static final DeferredBlock<Block> MOON_TEMPLE_BRICKS = decorativeRuneStone("moon_temple_bricks");
    public static final DeferredBlock<StairBlock> MOON_TEMPLE_BRICK_STAIRS =
            decorativeStairs("moon_temple_brick_stairs", MOON_TEMPLE_BRICKS);
    public static final DeferredBlock<SlabBlock> MOON_TEMPLE_BRICK_SLAB =
            decorativeSlab("moon_temple_brick_slab");
    public static final DeferredBlock<WallBlock> MOON_TEMPLE_BRICK_WALL =
            decorativeWall("moon_temple_brick_wall");
    public static final DeferredBlock<Block> SMOOTH_MOON_TEMPLE_BRICKS =
            decorativeRuneStone("smooth_moon_temple_bricks");
    public static final DeferredBlock<StairBlock> SMOOTH_MOON_TEMPLE_BRICK_STAIRS =
            decorativeStairs("smooth_moon_temple_brick_stairs", SMOOTH_MOON_TEMPLE_BRICKS);
    public static final DeferredBlock<SlabBlock> SMOOTH_MOON_TEMPLE_BRICK_SLAB =
            decorativeSlab("smooth_moon_temple_brick_slab");
    public static final DeferredBlock<WallBlock> SMOOTH_MOON_TEMPLE_BRICK_WALL =
            decorativeWall("smooth_moon_temple_brick_wall");
    public static final DeferredBlock<Block> CHISELED_MOON_TEMPLE_BRICKS =
            decorativeRuneStone("chiseled_moon_temple_bricks");
    public static final DeferredBlock<Block> MOON_TEMPLE_LAMP = decorative(
            "moon_temple_lamp", Block::new,
            () -> runeProperties(5.0F).get().lightLevel(state -> 15));
    public static final DeferredBlock<IronBarsBlock> MOON_TEMPLE_BARS = decorative(
            "moon_temple_bars", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS));
    public static final DeferredBlock<LockedStructureBlock> MOON_TEMPLE_GATE = locked("moon_temple_gate");

    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_AURORIAN_ANDESITE_SLAB =
            decorativeVerticalSlab("vertical_aurorian_andesite_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_AURORIAN_ANDESITE_STAIRS =
            decorativeVerticalStair("vertical_aurorian_andesite_stairs");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_AURORIAN_DIORITE_SLAB =
            decorativeVerticalSlab("vertical_aurorian_diorite_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_AURORIAN_DIORITE_STAIRS =
            decorativeVerticalStair("vertical_aurorian_diorite_stairs");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_AURORIAN_STONE_SLAB =
            decorativeVerticalSlab("vertical_aurorian_stone_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_AURORIAN_STONE_STAIRS =
            decorativeVerticalStair("vertical_aurorian_stone_stairs");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_AURORIAN_STONE_BRICK_SLAB =
            decorativeVerticalSlab("vertical_aurorian_stone_brick_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_AURORIAN_STONE_BRICK_STAIRS =
            decorativeVerticalStair("vertical_aurorian_stone_brick_stairs");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_CHISELED_MOON_TEMPLE_BRICK_SLAB =
            decorativeVerticalSlab("vertical_chiseled_moon_temple_brick_slab");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_MOON_TEMPLE_BRICK_SLAB =
            decorativeVerticalSlab("vertical_moon_temple_brick_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_MOON_TEMPLE_BRICK_STAIRS =
            decorativeVerticalStair("vertical_moon_temple_brick_stairs");

    public static final DeferredBlock<RotatedPillarBlock> WEEPING_WILLOW_LOG = building(
            "weeping_willow_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WEEPING_WILLOW_LOG = building(
            "stripped_weeping_willow_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG));
    public static final DeferredBlock<RotatedPillarBlock> WEEPING_WILLOW_WOOD = building(
            "weeping_willow_wood", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WEEPING_WILLOW_WOOD = building(
            "stripped_weeping_willow_wood", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD));
    public static final DeferredBlock<Block> WEEPING_WILLOW_PLANKS = building(
            "weeping_willow_planks", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    public static final DeferredBlock<StairBlock> WEEPING_WILLOW_STAIRS =
            stairs("weeping_willow_stairs", WEEPING_WILLOW_PLANKS);
    public static final DeferredBlock<SlabBlock> WEEPING_WILLOW_SLAB = woodSlab("weeping_willow_slab");
    public static final DeferredBlock<FenceBlock> WEEPING_WILLOW_FENCE = building(
            "weeping_willow_fence", FenceBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE));
    public static final DeferredBlock<FenceGateBlock> WEEPING_WILLOW_FENCE_GATE = building(
            "weeping_willow_fence_gate",
            properties -> new FenceGateBlock(WEEPING_WILLOW_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE));
    public static final DeferredBlock<DoorBlock> WEEPING_WILLOW_DOOR = noItemBuilding(
            "weeping_willow_door",
            properties -> new DoorBlock(WEEPING_WILLOW_BLOCK_SET, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR));
    public static final DeferredBlock<TrapDoorBlock> WEEPING_WILLOW_TRAPDOOR = building(
            "weeping_willow_trapdoor",
            properties -> new TrapDoorBlock(WEEPING_WILLOW_BLOCK_SET, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR));
    public static final DeferredBlock<PressurePlateBlock> WEEPING_WILLOW_PRESSURE_PLATE = building(
            "weeping_willow_pressure_plate",
            properties -> new PressurePlateBlock(WEEPING_WILLOW_BLOCK_SET, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE));
    public static final DeferredBlock<ButtonBlock> WEEPING_WILLOW_BUTTON = building(
            "weeping_willow_button",
            properties -> new ButtonBlock(WEEPING_WILLOW_BLOCK_SET, 30, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON));
    public static final DeferredBlock<TintedParticleLeavesBlock> WEEPING_WILLOW_LEAVES = natural(
            "weeping_willow_leaves", properties -> new TintedParticleLeavesBlock(0.01F, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_WEEPING_WILLOW_SLAB =
            decorativeVerticalWoodSlab("vertical_weeping_willow_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_WEEPING_WILLOW_STAIRS =
            decorativeVerticalWoodStair("vertical_weeping_willow_stairs");
    public static final DeferredBlock<VerticalSlabBlock> VERTICAL_SILENT_WOOD_SLAB =
            decorativeVerticalWoodSlab("vertical_silent_wood_slab");
    public static final DeferredBlock<VerticalStairBlock> VERTICAL_SILENT_WOOD_STAIRS =
            decorativeVerticalWoodStair("vertical_silent_wood_stairs");

    public static final DeferredBlock<AurorianStandingSignBlock> SILENT_WOOD_SIGN = noItemFunctional(
            "silent_wood_sign", properties -> new AurorianStandingSignBlock(SILENT_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN));
    public static final DeferredBlock<AurorianWallSignBlock> SILENT_WOOD_WALL_SIGN = hidden(
            "silent_wood_wall_sign", properties -> new AurorianWallSignBlock(SILENT_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN));
    public static final DeferredBlock<AurorianCeilingHangingSignBlock> SILENT_WOOD_HANGING_SIGN = noItemFunctional(
            "silent_wood_hanging_sign",
            properties -> new AurorianCeilingHangingSignBlock(SILENT_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN));
    public static final DeferredBlock<AurorianWallHangingSignBlock> SILENT_WOOD_WALL_HANGING_SIGN = hidden(
            "silent_wood_wall_hanging_sign",
            properties -> new AurorianWallHangingSignBlock(SILENT_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN));
    public static final DeferredBlock<AurorianStandingSignBlock> WEEPING_WILLOW_WOOD_SIGN = noItemFunctional(
            "weeping_willow_wood_sign",
            properties -> new AurorianStandingSignBlock(WEEPING_WILLOW_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN));
    public static final DeferredBlock<AurorianWallSignBlock> WEEPING_WILLOW_WOOD_WALL_SIGN = hidden(
            "weeping_willow_wood_wall_sign",
            properties -> new AurorianWallSignBlock(WEEPING_WILLOW_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN));
    public static final DeferredBlock<AurorianCeilingHangingSignBlock> WEEPING_WILLOW_WOOD_HANGING_SIGN = noItemFunctional(
            "weeping_willow_wood_hanging_sign",
            properties -> new AurorianCeilingHangingSignBlock(WEEPING_WILLOW_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN));
    public static final DeferredBlock<AurorianWallHangingSignBlock> WEEPING_WILLOW_WOOD_WALL_HANGING_SIGN = hidden(
            "weeping_willow_wood_wall_hanging_sign",
            properties -> new AurorianWallHangingSignBlock(WEEPING_WILLOW_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN));

    public static final DeferredBlock<SilentCampfireBlock> SILENT_CAMPFIRE = functional(
            "silent_campfire", SilentCampfireBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE));
    public static final DeferredBlock<AurorianFurnaceChimneyBlock> AURORIAN_FURNACE_CHIMNEY = functional(
            "aurorian_furnace_chimney", AurorianFurnaceChimneyBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2.0F));
    public static final DeferredBlock<HugeMushroomBlock> INDIGO_MUSHROOM_BLOCK = natural(
            "indigo_mushroom_block", HugeMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK).destroyTime(1.0F));
    public static final DeferredBlock<AurorianPlantBlock> EQUINOX_FLOWER = natural(
            "equinox_flower", AurorianPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));

    public static final DeferredBlock<FlowerPotBlock> POTTED_AURORIAN_GRASS =
            potted("potted_aurorian_grass", ModBlocks.AURORIAN_GRASS);
    public static final DeferredBlock<FlowerPotBlock> POTTED_EQUINOX_FLOWER =
            potted("potted_equinox_flower", EQUINOX_FLOWER);
    public static final DeferredBlock<FlowerPotBlock> POTTED_LAVENDER_PLANT =
            potted("potted_lavender_plant", ModBlocks.LAVENDER_PLANT);
    public static final DeferredBlock<FlowerPotBlock> POTTED_MOON_FROST_FLOWER =
            potted("potted_moon_frost_flower", ModBlocks.MOON_FROST_FLOWER);
    public static final DeferredBlock<FlowerPotBlock> POTTED_NEBULA_BLOSSOM_CLUSTER =
            potted("potted_nebula_blossom_cluster", ModBlocks.NEBULA_BLOSSOM_CLUSTER);
    public static final DeferredBlock<FlowerPotBlock> POTTED_PETUNIA_PLANT =
            potted("potted_petunia_plant", ModBlocks.PETUNIA_PLANT);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SILENT_TREE_SAPLING =
            potted("potted_silent_tree_sapling", ModBlocks.SILENT_TREE_SAPLING);
    public static final DeferredBlock<FlowerPotBlock> POTTED_VOID_CANDLE_FLOWER =
            potted("potted_void_candle_flower", ModBlocks.VOID_CANDLE_FLOWER);

    public static final DeferredBlock<LegacyMoistureBlock> AURORIAN_FARM_TILE = hidden(
            "aurorian_farm_tile", LegacyMoistureBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND));
    public static final DeferredBlock<LegacyPortalBlock> AURORIAN_PORTAL = hidden(
            "aurorian_portal", LegacyPortalBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL));
    public static final DeferredBlock<LegacyFacingBlock> MYSTICAL_BARRIER = hidden(
            "mystical_barrier", LegacyFacingBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable().noOcclusion());
    public static final DeferredBlock<IronBarsBlock> AURORIAN_GLASS_PANE = building(
            "aurorian_glass_pane", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE));
    public static final DeferredBlock<IronBarsBlock> DARK_STONE_BARS = decorative(
            "dark_stone_bars", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS));
    public static final DeferredBlock<TransparentBlock> DARK_STONE_GLASS = decorative(
            "dark_stone_glass", TransparentBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
    public static final DeferredBlock<IronBarsBlock> DARK_STONE_GLASS_PANE = decorative(
            "dark_stone_glass_pane", IronBarsBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE));
    public static final DeferredBlock<RotatedPillarBlock> DARK_STONE_PILLAR = decorative(
            "dark_stone_pillar", RotatedPillarBlock::new, runeProperties(5.0F));
    public static final DeferredBlock<RotatedPillarBlock> RUNE_STONE_PILLAR = decorative(
            "rune_stone_pillar", RotatedPillarBlock::new, runeProperties(5.0F));

    public static final DeferredBlock<DarkStoneGateBlock> DARK_STONE_GATE = decorative(
            "dark_stone_gate", DarkStoneGateBlock::new, runeProperties(5.0F));
    public static final DeferredBlock<Block> MOON_TEMPLE_CELL_GATE = decorativeRuneStone("moon_temple_cell_gate");
    public static final DeferredBlock<Block> RUNE_STONE_LOOT_GATE = decorativeRuneStone("rune_stone_loot_gate");
    public static final DeferredBlock<DarkStoneGateKeyholeBlock> DARK_STONE_GATE_KEYHOLE = decorative(
            "dark_stone_gate_keyhole", DarkStoneGateKeyholeBlock::new, runeProperties(5.0F));
    public static final DeferredBlock<LockedStructureBlock> MOON_TEMPLE_CELL_GATE_KEYHOLE =
            locked("moon_temple_cell_gate_keyhole");
    public static final DeferredBlock<LockedStructureBlock> MOON_TEMPLE_GATE_KEYHOLE =
            locked("moon_temple_gate_keyhole");
    public static final DeferredBlock<LockedStructureBlock> RUNE_STONE_LOOT_GATE_KEYHOLE =
            locked("rune_stone_loot_gate_keyhole");

    public static final DeferredBlock<LegacyHorizontalFacingBlock> MOONLIGHT_FORGE = horizontalFunctional(
            "moonlight_forge",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE).lightLevel(state -> 0));
    public static final DeferredBlock<LegacyHorizontalFacingBlock> RELIC_TABLE = horizontalFunctional(
            "relic_table", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
    public static final DeferredBlock<SacrificeTableBlock> SACRIFICE_TABLE = modelledFunctional(
            "sacrifice_table", SacrificeTableBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredBlock<LegacyHorizontalFacingBlock> SCRAPPER = horizontalFunctional(
            "scrapper", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
    public static final DeferredBlock<CrystallineSwordPedestalBlock> CRYSTALLINE_SWORD_PEDESTAL = modelledDecorative(
            "crystalline_sword_pedestal", CrystallineSwordPedestalBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
    public static final DeferredBlock<SpiderMotherBarrierBlock> SPIDER_MOTHER_BARRIER = hidden(
            "spider_mother_barrier",
            SpiderMotherBarrierBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable().noOcclusion());
    public static final DeferredBlock<SpiderMotherSpawnerBlock> SPIDER_MOTHER_SPAWNER = hidden(
            "spider_mother_spawner",
            SpiderMotherSpawnerBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable().noOcclusion());
    public static final DeferredBlock<Block> LASER_CRYSTAL = decorative(
            "laser_crystal", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> URN = decorative(
            "urn", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DECORATED_POT).noOcclusion());

    public static final DeferredBlock<LegacyAgeThreeCropBlock> LAVENDER_CROP = hidden(
            "lavender_crop", LegacyAgeThreeCropBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS));
    public static final DeferredBlock<LegacyAgeThreeCropBlock> SILK_BERRY_CROP = hidden(
            "silk_berry_crop", LegacyAgeThreeCropBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS));
    public static final DeferredBlock<LegacyLevelPlantBlock> WICK_GRASS = natural(
            "wick_grass", LegacyLevelPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel(state -> 15));
    public static final DeferredBlock<LegacyLargeIceSpikeBlock> LARGE_FILTHY_ICE_SPIKE = natural(
            "large_filthy_ice_spike", LegacyLargeIceSpikeBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LARGE_AMETHYST_BUD));
    public static final DeferredBlock<LegacyVerticalDirectionBlock> MEDIUM_FILTHY_ICE_SPIKE = natural(
            "medium_filthy_ice_spike", LegacyVerticalDirectionBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MEDIUM_AMETHYST_BUD));
    public static final DeferredBlock<LegacyVerticalDirectionBlock> SMALL_FILTHY_ICE_SPIKE = natural(
            "small_filthy_ice_spike", LegacyVerticalDirectionBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD));
    public static final DeferredBlock<HugeMushroomBlock> INDIGO_MUSHROOM_STEM = natural(
            "indigo_mushroom_stem", HugeMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM));

    public static final DeferredBlock<TorchBlock> MOON_TORCH = hidden(
            "moon_torch",
            properties -> new TorchBlock(ParticleTypes.CLOUD, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH));
    public static final DeferredBlock<WallTorchBlock> MOON_WALL_TORCH = hidden(
            "moon_wall_torch",
            properties -> new WallTorchBlock(ParticleTypes.CLOUD, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH));

    public static final DeferredBlock<FlowerPotBlock> POTTED_AURORIAN_GRASS_LIGHT =
            potted("potted_aurorian_grass_light", ModBlocks.AURORIAN_GRASS_LIGHT);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CURSED_FROST_TREE_SAPLING =
            potted("potted_cursed_frost_tree_sapling", ModBlocks.CURSED_FROST_TREE_SAPLING);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CURTAIN_TREE_SAPLING =
            potted("potted_curtain_tree_sapling", ModBlocks.CURTAIN_TREE_SAPLING);
    public static final DeferredBlock<FlowerPotBlock> POTTED_WICK_GRASS =
            potted("potted_wick_grass", WICK_GRASS);

    public static final DeferredBlock<Block> MOLTEN_CERULEAN = legacyMolten("molten_cerulean");
    public static final DeferredBlock<Block> MOLTEN_MOONSILVER = legacyMolten("molten_moonsilver");
    public static final DeferredBlock<Block> MOLTEN_MOONSTONE = legacyMolten("molten_moonstone");

    public static final DeferredBlock<AurorianStandingSignBlock> CURTAIN_WOOD_SIGN = noItemFunctional(
            "curtain_wood_sign", properties -> new AurorianStandingSignBlock(CURTAIN_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SIGN));
    public static final DeferredBlock<AurorianWallSignBlock> CURTAIN_WOOD_WALL_SIGN = hidden(
            "curtain_wood_wall_sign", properties -> new AurorianWallSignBlock(CURTAIN_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_WALL_SIGN));
    public static final DeferredBlock<AurorianCeilingHangingSignBlock> CURTAIN_WOOD_HANGING_SIGN = noItemFunctional(
            "curtain_wood_hanging_sign",
            properties -> new AurorianCeilingHangingSignBlock(CURTAIN_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_HANGING_SIGN));
    public static final DeferredBlock<AurorianWallHangingSignBlock> CURTAIN_WOOD_WALL_HANGING_SIGN = hidden(
            "curtain_wood_wall_hanging_sign",
            properties -> new AurorianWallHangingSignBlock(CURTAIN_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_WALL_HANGING_SIGN));
    public static final DeferredBlock<AurorianStandingSignBlock> CURSED_FROST_WOOD_SIGN = noItemFunctional(
            "cursed_frost_wood_sign",
            properties -> new AurorianStandingSignBlock(CURSED_FROST_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SIGN));
    public static final DeferredBlock<AurorianWallSignBlock> CURSED_FROST_WOOD_WALL_SIGN = hidden(
            "cursed_frost_wood_wall_sign",
            properties -> new AurorianWallSignBlock(CURSED_FROST_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_WALL_SIGN));
    public static final DeferredBlock<AurorianCeilingHangingSignBlock> CURSED_FROST_WOOD_HANGING_SIGN = noItemFunctional(
            "cursed_frost_wood_hanging_sign",
            properties -> new AurorianCeilingHangingSignBlock(CURSED_FROST_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN));
    public static final DeferredBlock<AurorianWallHangingSignBlock> CURSED_FROST_WOOD_WALL_HANGING_SIGN = hidden(
            "cursed_frost_wood_wall_hanging_sign",
            properties -> new AurorianWallHangingSignBlock(CURSED_FROST_WOOD_TYPE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN));

    static {
        registerLegacyBuildingSeries();
        FUNCTIONAL_BLOCKS.add(MOON_TORCH);
        ModBlocks.ITEMS.registerItem(
                "weeping_willow_door",
                properties -> new DoubleHighBlockItem(
                        WEEPING_WILLOW_DOOR.get(), properties.useBlockDescriptionPrefix()));
        ModBlocks.ITEMS.registerItem(
                "silent_wood_sign",
                properties -> new SignItem(SILENT_WOOD_SIGN.get(), SILENT_WOOD_WALL_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "silent_wood_hanging_sign",
                properties -> new HangingSignItem(
                        SILENT_WOOD_HANGING_SIGN.get(), SILENT_WOOD_WALL_HANGING_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "weeping_willow_wood_sign",
                properties -> new SignItem(
                        WEEPING_WILLOW_WOOD_SIGN.get(), WEEPING_WILLOW_WOOD_WALL_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "weeping_willow_wood_hanging_sign",
                properties -> new HangingSignItem(
                        WEEPING_WILLOW_WOOD_HANGING_SIGN.get(),
                        WEEPING_WILLOW_WOOD_WALL_HANGING_SIGN.get(),
                        properties));
        ModBlocks.ITEMS.registerItem(
                "moon_torch",
                properties -> new StandingAndWallBlockItem(
                        MOON_TORCH.get(), MOON_WALL_TORCH.get(), Direction.DOWN,
                        properties.useBlockDescriptionPrefix()));
        ModBlocks.ITEMS.registerItem(
                "curtain_wood_sign",
                properties -> new SignItem(CURTAIN_WOOD_SIGN.get(), CURTAIN_WOOD_WALL_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "curtain_wood_hanging_sign",
                properties -> new HangingSignItem(
                        CURTAIN_WOOD_HANGING_SIGN.get(), CURTAIN_WOOD_WALL_HANGING_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "cursed_frost_wood_sign",
                properties -> new SignItem(
                        CURSED_FROST_WOOD_SIGN.get(), CURSED_FROST_WOOD_WALL_SIGN.get(), properties));
        ModBlocks.ITEMS.registerItem(
                "cursed_frost_wood_hanging_sign",
                properties -> new HangingSignItem(
                        CURSED_FROST_WOOD_HANGING_SIGN.get(),
                        CURSED_FROST_WOOD_WALL_HANGING_SIGN.get(),
                        properties));
    }

    private ModStructureBlocks() {
    }

    public static void bootstrap() {
    }

    public static List<DeferredBlock<? extends Block>> buildingBlocks() {
        return Collections.unmodifiableList(BUILDING_BLOCKS);
    }

    public static List<DeferredBlock<? extends Block>> naturalBlocks() {
        return Collections.unmodifiableList(NATURAL_BLOCKS);
    }

    public static List<DeferredBlock<? extends Block>> decorativeBlocks() {
        return Collections.unmodifiableList(DECORATIVE_BLOCKS);
    }

    public static List<DeferredBlock<? extends Block>> functionalBlocks() {
        return Collections.unmodifiableList(FUNCTIONAL_BLOCKS);
    }

    public static Map<String, DeferredBlock<? extends Block>> blocksById() {
        return Collections.unmodifiableMap(BLOCKS_BY_ID);
    }

    public static Optional<BlockState> getStrippedState(BlockState state) {
        if (state.is(WEEPING_WILLOW_LOG.get())) {
            return Optional.of(STRIPPED_WEEPING_WILLOW_LOG.get()
                    .defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
        if (state.is(WEEPING_WILLOW_WOOD.get())) {
            return Optional.of(STRIPPED_WEEPING_WILLOW_WOOD.get()
                    .defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
        return Optional.empty();
    }

    private static void registerLegacyBuildingSeries() {
        for (String id : LEGACY_FULL_BLOCK_IDS) {
            if (id.equals("filthy_ice") || id.equals("indigo_mushroom_crystal")
                    || id.equals("red_aurorian_grass_block")) {
                natural(id, Block::new, legacyFullProperties(id));
            } else if (LEGACY_STANDARD_BUILDING_IDS.contains(id)) {
                building(id, Block::new, legacyFullProperties(id));
            } else {
                decorative(id, Block::new, legacyFullProperties(id));
            }
        }
        for (String id : LEGACY_STAIR_IDS) {
            if (LEGACY_STANDARD_BUILDING_IDS.contains(id)) {
                stairs(id, () -> Blocks.STONE_BRICKS);
            } else {
                decorativeStairs(id, () -> Blocks.STONE_BRICKS);
            }
        }
        for (String id : LEGACY_SLAB_IDS) {
            if (LEGACY_STANDARD_BUILDING_IDS.contains(id)) {
                slab(id);
            } else {
                decorativeSlab(id);
            }
        }
        for (String id : LEGACY_WALL_IDS) {
            decorativeWall(id);
        }
        for (String id : LEGACY_VERTICAL_STAIR_IDS) {
            if (id.contains("_wood_")) {
                decorativeVerticalWoodStair(id);
            } else {
                decorativeVerticalStair(id);
            }
        }
        for (String id : LEGACY_VERTICAL_SLAB_IDS) {
            if (id.contains("_wood_")) {
                decorativeVerticalWoodSlab(id);
            } else {
                decorativeVerticalSlab(id);
            }
        }
    }

    private static Supplier<BlockBehaviour.Properties> legacyFullProperties(String id) {
        if (id.equals("mysterium_wool")) {
            return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL);
        }
        if (id.equals("filthy_ice")) {
            return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_ICE);
        }
        if (id.equals("red_aurorian_grass_block")) {
            return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK);
        }
        if (id.equals("aurorian_barrier_stone")) {
            return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK);
        }
        if (id.startsWith("luminous_") || id.equals("dark_stone_lamp")) {
            return () -> runeProperties(5.0F).get().lightLevel(state -> 15);
        }
        if (id.equals("void_stone")) {
            return () -> runeProperties(5.0F).get().lightLevel(state -> 7);
        }
        if (id.equals("indigo_mushroom_crystal")) {
            return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).lightLevel(state -> 1);
        }
        if (id.contains("rune_stone") || id.startsWith("dark_stone") || id.startsWith("umbra_stone")) {
            return runeProperties(5.0F);
        }
        return stoneProperties(id.equals("aurorian_coal_block") ? 5.0F : 3.0F);
    }

    private static DeferredBlock<Block> stone(String id) {
        return building(id, Block::new, stoneProperties(2.0F));
    }

    private static DeferredBlock<Block> decorativeRuneStone(String id) {
        return decorative(id, Block::new, runeProperties(5.0F));
    }

    private static DeferredBlock<StairBlock> stairs(String id, Supplier<? extends Block> base) {
        return building(
                id,
                properties -> new StairBlock(base.get().defaultBlockState(), properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS));
    }

    private static DeferredBlock<SlabBlock> slab(String id) {
        return building(
                id, SlabBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB));
    }

    private static DeferredBlock<WallBlock> wall(String id) {
        return building(
                id, WallBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL));
    }

    private static DeferredBlock<StairBlock> decorativeStairs(String id, Supplier<? extends Block> base) {
        return decorative(
                id,
                properties -> new StairBlock(base.get().defaultBlockState(), properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS));
    }

    private static DeferredBlock<SlabBlock> decorativeSlab(String id) {
        return decorative(
                id, SlabBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB));
    }

    private static DeferredBlock<WallBlock> decorativeWall(String id) {
        return decorative(
                id, WallBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL));
    }

    private static DeferredBlock<SlabBlock> woodSlab(String id) {
        return building(
                id, SlabBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB));
    }

    private static DeferredBlock<VerticalSlabBlock> decorativeVerticalSlab(String id) {
        return decorative(id, VerticalSlabBlock::new, stoneProperties(2.0F));
    }

    private static DeferredBlock<VerticalStairBlock> decorativeVerticalStair(String id) {
        return decorative(id, VerticalStairBlock::new, stoneProperties(2.0F));
    }

    private static DeferredBlock<VerticalSlabBlock> decorativeVerticalWoodSlab(String id) {
        return decorative(
                id, VerticalSlabBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    }

    private static DeferredBlock<VerticalStairBlock> decorativeVerticalWoodStair(String id) {
        return decorative(
                id, VerticalStairBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    }

    private static DeferredBlock<LockedStructureBlock> locked(String id) {
        return decorative(id, LockedStructureBlock::new, runeProperties(5.0F));
    }

    private static DeferredBlock<LegacyHorizontalFacingBlock> horizontalFunctional(
            String id, Supplier<BlockBehaviour.Properties> properties) {
        return functional(id, LegacyHorizontalFacingBlock::new, properties);
    }

    private static <T extends Block> DeferredBlock<T> modelledFunctional(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerItem(id, itemProperties -> new ModelledBlockItem(block.get(), itemProperties, id));
        FUNCTIONAL_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> modelledDecorative(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerItem(id, itemProperties -> new ModelledBlockItem(block.get(), itemProperties, id));
        DECORATIVE_BLOCKS.add(block);
        return block;
    }

    private static DeferredBlock<Block> legacyMolten(String id) {
        return hidden(id, Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA));
    }

    private static DeferredBlock<FlowerPotBlock> potted(String id, Supplier<? extends Block> content) {
        return hidden(
                id,
                properties -> new FlowerPotBlock(
                        () -> (FlowerPotBlock) ModBlocks.AURORIAN_FLOWER_POT.get(), content, properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT));
    }

    private static Supplier<BlockBehaviour.Properties> stoneProperties(float strength) {
        return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(strength, 6.0F);
    }

    private static Supplier<BlockBehaviour.Properties> runeProperties(float strength) {
        return () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).strength(strength, 1200.0F);
    }

    private static <T extends Block> DeferredBlock<T> building(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerSimpleBlockItem(block);
        BUILDING_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> noItemBuilding(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        BUILDING_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> decorative(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerSimpleBlockItem(block);
        DECORATIVE_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> natural(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerSimpleBlockItem(block);
        NATURAL_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> functional(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        ModBlocks.ITEMS.registerSimpleBlockItem(block);
        FUNCTIONAL_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> noItemFunctional(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = register(id, factory, properties);
        FUNCTIONAL_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> hidden(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        return register(id, factory, properties);
    }

    private static <T extends Block> DeferredBlock<T> register(
            String id, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        if (BLOCKS_BY_ID.containsKey(id)) {
            throw new IllegalStateException("Duplicate structure block id: " + id);
        }
        DeferredBlock<T> block = ModBlocks.BLOCKS.registerBlock(id, factory, properties);
        BLOCKS_BY_ID.put(id, block);
        return block;
    }
}
