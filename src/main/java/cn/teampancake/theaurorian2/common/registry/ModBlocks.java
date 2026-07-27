package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianDoublePlantBlock;
import cn.teampancake.theaurorian2.common.block.AurorianCaveVinesBlock;
import cn.teampancake.theaurorian2.common.block.AurorianCaveVinesPlantBlock;
import cn.teampancake.theaurorian2.common.block.AurorianGrassBlock;
import cn.teampancake.theaurorian2.common.block.AurorianPlantBlock;
import cn.teampancake.theaurorian2.common.block.AurorianTallGrassBlock;
import cn.teampancake.theaurorian2.common.block.AurorianTwistingVinesBlock;
import cn.teampancake.theaurorian2.common.block.AurorianTwistingVinesPlantBlock;
import cn.teampancake.theaurorian2.common.block.AurorianWaterGrassBlock;
import cn.teampancake.theaurorian2.common.block.AurorianWaterSurfacePlantBlock;
import cn.teampancake.theaurorian2.common.block.AstrologyTableBlock;
import cn.teampancake.theaurorian2.common.block.BlueberryBushBlock;
import cn.teampancake.theaurorian2.common.block.ColoredParticleLeavesBlock;
import cn.teampancake.theaurorian2.common.block.ColdAurorianPlantBlock;
import cn.teampancake.theaurorian2.common.block.GroundBranchBlock;
import cn.teampancake.theaurorian2.common.block.GroundMushroomBlock;
import cn.teampancake.theaurorian2.common.block.LogMushroomBlock;
import cn.teampancake.theaurorian2.common.block.LuminousAurorianDoublePlantBlock;
import cn.teampancake.theaurorian2.common.block.LuminousAurorianGrassBlock;
import cn.teampancake.theaurorian2.common.block.PebbleBlock;
import cn.teampancake.theaurorian2.common.block.SnowfieldTallPlantBlock;
import cn.teampancake.theaurorian2.common.block.TallWickGrassBlock;
import cn.teampancake.theaurorian2.common.block.TallAurorianWaterGrassBlock;
import cn.teampancake.theaurorian2.common.block.WallMushroomBlock;
import cn.teampancake.theaurorian2.common.item.AstrologyTableItem;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.food.Foods;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheAurorian2.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheAurorian2.MOD_ID);

    public static final DeferredBlock<Block> AURORIAN_STONE = BLOCKS.registerSimpleBlock(
            "aurorian_stone", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final DeferredBlock<Block> AURORIAN_EROSIVE = BLOCKS.registerSimpleBlock(
            "aurorian_erosive", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final DeferredBlock<Block> AURORIAN_COBBLESTONE = BLOCKS.registerSimpleBlock(
            "aurorian_cobblestone", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE));
    public static final DeferredBlock<Block> AURORIAN_DIRT = BLOCKS.registerSimpleBlock(
            "aurorian_dirt", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
    public static final DeferredBlock<Block> AURORIAN_CLAY = BLOCKS.registerSimpleBlock(
            "aurorian_clay", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY));
    public static final DeferredBlock<PointedDripstoneBlock> AURORIAN_POINTED_DRIPSTONE = BLOCKS.registerBlock(
            "aurorian_pointed_dripstone", PointedDripstoneBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE));
    public static final DeferredBlock<Block> AURORIAN_DRIPSTONE_BLOCK = BLOCKS.registerSimpleBlock(
            "aurorian_dripstone_block", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK));
    public static final DeferredBlock<Block> LUMINOUS_MOSS_BLOCK = BLOCKS.registerSimpleBlock(
            "luminous_moss_block", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK));
    public static final DeferredBlock<CarpetBlock> LUMINOUS_MOSS_CARPET = BLOCKS.registerBlock(
            "luminous_moss_carpet", CarpetBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET));
    public static final DeferredBlock<AzaleaBlock> STAR_AZALEA = BLOCKS.registerBlock(
            "star_azalea", AzaleaBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA));
    public static final DeferredBlock<AzaleaBlock> FLOWERING_STAR_AZALEA = BLOCKS.registerBlock(
            "flowering_star_azalea", AzaleaBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWERING_AZALEA));
    public static final DeferredBlock<SporeBlossomBlock> MIST_SPORE_BLOSSOM = BLOCKS.registerBlock(
            "mist_spore_blossom", SporeBlossomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPORE_BLOSSOM));
    public static final DeferredBlock<AurorianCaveVinesBlock> DEW_CAVE_VINES = BLOCKS.registerBlock(
            "dew_cave_vines", AurorianCaveVinesBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES));
    public static final DeferredBlock<AurorianCaveVinesPlantBlock> DEW_CAVE_VINES_PLANT = BLOCKS.registerBlock(
            "dew_cave_vines_plant", AurorianCaveVinesPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES_PLANT));
    public static final DeferredBlock<GlowLichenBlock> STAR_GLOW_LICHEN = BLOCKS.registerBlock(
            "star_glow_lichen", GlowLichenBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLOW_LICHEN));

    public static final DeferredBlock<Block> AURORIAN_BRICKS = BLOCKS.registerSimpleBlock(
            "aurorian_bricks", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS));
    public static final DeferredBlock<StairBlock> AURORIAN_BRICK_STAIRS = BLOCKS.registerBlock(
            "aurorian_brick_stairs",
            properties -> new StairBlock(AURORIAN_BRICKS.get().defaultBlockState(), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_STAIRS));
    public static final DeferredBlock<SlabBlock> AURORIAN_BRICK_SLAB = BLOCKS.registerBlock(
            "aurorian_brick_slab", SlabBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_SLAB));
    public static final DeferredBlock<WallBlock> AURORIAN_BRICK_WALL = BLOCKS.registerBlock(
            "aurorian_brick_wall", WallBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_WALL));
    public static final DeferredBlock<FlowerPotBlock> AURORIAN_FLOWER_POT = BLOCKS.registerBlock(
            "aurorian_flower_pot",
            properties -> new FlowerPotBlock(null, () -> Blocks.AIR, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT));
    public static final DeferredBlock<Block> AURORIAN_TERRACOTTA = BLOCKS.registerSimpleBlock(
            "aurorian_terracotta", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA));
    public static final DeferredBlock<AurorianGrassBlock> AURORIAN_GRASS_BLOCK = BLOCKS.registerBlock(
            "aurorian_grass_block", AurorianGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));
    public static final DeferredBlock<AurorianGrassBlock> LIGHT_AURORIAN_GRASS_BLOCK = BLOCKS.registerBlock(
            "light_aurorian_grass_block", AurorianGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).lightLevel(state -> 2));
    public static final DeferredBlock<SandBlock> MOON_SAND_RIVER = BLOCKS.registerBlock(
            "moon_sand_river", properties -> new SandBlock(new ColorRGBA(0xFFB7A9D6), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));
    public static final DeferredBlock<Block> SMOOTH_MOON_SANDSTONE = BLOCKS.registerSimpleBlock(
            "smooth_moon_sandstone", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE));
    public static final DeferredBlock<LiquidBlock> MOON_DEW_BLOCK = BLOCKS.registerBlock(
            "moon_dew", properties -> new LiquidBlock(ModFluids.MOON_DEW.get(), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER));

    public static final DeferredBlock<AurorianTallGrassBlock> AURORIAN_GRASS = BLOCKS.registerBlock(
            "aurorian_grass", AurorianTallGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS));
    public static final DeferredBlock<AurorianDoublePlantBlock> TALL_AURORIAN_GRASS = BLOCKS.registerBlock(
            "tall_aurorian_grass", AurorianDoublePlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS));
    public static final DeferredBlock<LuminousAurorianGrassBlock> AURORIAN_GRASS_LIGHT = BLOCKS.registerBlock(
            "aurorian_grass_light", LuminousAurorianGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel(state -> 2));
    public static final DeferredBlock<LuminousAurorianDoublePlantBlock> TALL_AURORIAN_GRASS_LIGHT = BLOCKS.registerBlock(
            "tall_aurorian_grass_light", LuminousAurorianDoublePlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS).lightLevel(state -> 2));
    public static final DeferredBlock<AurorianWaterGrassBlock> AURORIAN_WATER_GRASS = BLOCKS.registerBlock(
            "aurorian_water_grass", AurorianWaterGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS));
    public static final DeferredBlock<TallAurorianWaterGrassBlock> TALL_AURORIAN_WATER_GRASS = BLOCKS.registerBlock(
            "tall_aurorian_water_grass", TallAurorianWaterGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_SEAGRASS));
    public static final DeferredBlock<AurorianWaterSurfacePlantBlock> AURORIAN_LILY_PAD = BLOCKS.registerBlock(
            "aurorian_lily_pad",
            properties -> new AurorianWaterSurfacePlantBlock(
                    Block.box(0.5, 0.0, 0.5, 15.5, 0.5, 15.5), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD)
                    .lightLevel(state -> state.getValue(AurorianWaterSurfacePlantBlock.LEVEL)));
    public static final DeferredBlock<AurorianWaterSurfacePlantBlock> AURORIAN_WATER_MUSHROOM = BLOCKS.registerBlock(
            "aurorian_water_mushroom",
            properties -> new AurorianWaterSurfacePlantBlock(
                    Block.box(4.0, 0.0, 4.0, 12.0, 10.5, 12.0), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD)
                    .lightLevel(state -> state.getValue(AurorianWaterSurfacePlantBlock.LEVEL)));
    public static final DeferredBlock<AurorianPlantBlock> PETUNIA_PLANT = plant("petunia_plant");
    public static final DeferredBlock<AurorianPlantBlock> NEBULA_BLOSSOM_CLUSTER = plant("nebula_blossom_cluster");
    public static final DeferredBlock<AurorianPlantBlock> MOON_FROST_FLOWER = plant("moon_frost_flower");
    public static final DeferredBlock<AurorianPlantBlock> VOID_CANDLE_FLOWER = plant("void_candle_flower");
    public static final DeferredBlock<AurorianPlantBlock> LAVENDER_PLANT = plant("lavender_plant");
    public static final DeferredBlock<SnowfieldTallPlantBlock> DREAMSCAPE_PISTIL = BLOCKS.registerBlock(
            "dreamscape_pistil", SnowfieldTallPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS));
    public static final DeferredBlock<SnowfieldTallPlantBlock> FROST_TEARS_FLOWER = BLOCKS.registerBlock(
            "frost_tears_flower", SnowfieldTallPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS));
    public static final DeferredBlock<ColdAurorianPlantBlock> CRISPED_MALLOW = coldPlant("crisped_mallow");
    public static final DeferredBlock<ColdAurorianPlantBlock> FROST_SNOW_GRASS = coldPlant("frost_snow_grass");
    public static final DeferredBlock<ColdAurorianPlantBlock> ICE_CALENDULA = coldPlant("ice_calendula");
    public static final DeferredBlock<ColdAurorianPlantBlock> WINTER_ROOT = coldPlant("winter_root");
    public static final DeferredBlock<AurorianDoublePlantBlock> TALL_LAVENDER_PLANT = BLOCKS.registerBlock(
            "tall_lavender_plant", AurorianDoublePlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LILAC));
    public static final DeferredBlock<TallWickGrassBlock> TALL_WICK_GRASS = BLOCKS.registerBlock(
            "tall_wick_grass", TallWickGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SUNFLOWER)
                    .lightLevel(state -> state.getValue(TallWickGrassBlock.LEVEL)));
    public static final DeferredBlock<LogMushroomBlock> INDIGO_MUSHROOM = BLOCKS.registerBlock(
            "indigo_mushroom", LogMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM));
    public static final DeferredBlock<BlueberryBushBlock> BLUEBERRY_BUSH = BLOCKS.registerBlock(
            "blueberry_bush", BlueberryBushBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH));
    public static final DeferredBlock<GroundBranchBlock> SILENT_WOOD_STICK = BLOCKS.registerBlock(
            "silent_wood_stick", GroundBranchBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEAD_BUSH).noOcclusion());
    public static final DeferredBlock<PebbleBlock> PEBBLE = BLOCKS.registerBlock(
            "pebble", PebbleBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEAD_BUSH)
                    .strength(0.2F)
                    .sound(SoundType.STONE)
                    .noOcclusion());
    public static final DeferredBlock<GroundMushroomBlock> WHITE_GROUND_MUSHROOM = BLOCKS.registerBlock(
            "white_ground_mushroom", GroundMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).noOcclusion());
    public static final DeferredBlock<GroundMushroomBlock> BLUE_GROUND_MUSHROOM = BLOCKS.registerBlock(
            "blue_ground_mushroom", GroundMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).noOcclusion());
    public static final DeferredBlock<WallMushroomBlock> BROWN_MUSHROOM = BLOCKS.registerBlock(
            "brown_mushroom", WallMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
                    .strength(0.1F)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredBlock<WallMushroomBlock> DARK_BROWN_MUSHROOM = BLOCKS.registerBlock(
            "dark_brown_mushroom", WallMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
                    .strength(0.1F)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredBlock<WallMushroomBlock> RED_MUSHROOM = BLOCKS.registerBlock(
            "red_mushroom", WallMushroomBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM)
                    .strength(0.1F)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredBlock<VineBlock> AURORIAN_VINE = BLOCKS.registerBlock(
            "aurorian_vine", VineBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.VINE));
    public static final DeferredBlock<AurorianTwistingVinesBlock> AURORIAN_TWISTING_VINES = BLOCKS.registerBlock(
            "aurorian_twisting_vines", AurorianTwistingVinesBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TWISTING_VINES));
    public static final DeferredBlock<AurorianTwistingVinesPlantBlock> AURORIAN_TWISTING_VINES_PLANT = BLOCKS.registerBlock(
            "aurorian_twisting_vines_plant", AurorianTwistingVinesPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TWISTING_VINES_PLANT));
    public static final DeferredBlock<AstrologyTableBlock> ASTROLOGY_TABLE = BLOCKS.registerBlock(
            "astrology_table", AstrologyTableBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(6.0F)
                    .lightLevel(state -> 7)
                    .noOcclusion());

    public static final DeferredBlock<RotatedPillarBlock> SILENT_TREE_LOG = BLOCKS.registerBlock(
            "silent_tree_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG));
    public static final DeferredBlock<ColoredParticleLeavesBlock> SILENT_TREE_LEAVES = BLOCKS.registerBlock(
            "silent_tree_leaves",
            properties -> new ColoredParticleLeavesBlock(0.01F, 0xFF69A0DB, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final DeferredBlock<SaplingBlock> SILENT_TREE_SAPLING = BLOCKS.registerBlock(
            "silent_tree_sapling", properties -> new SaplingBlock(ModTreeGrowers.SILENT_TREE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));
    public static final DeferredBlock<RotatedPillarBlock> CURTAIN_TREE_LOG = BLOCKS.registerBlock(
            "curtain_tree_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG));
    public static final DeferredBlock<ColoredParticleLeavesBlock> CURTAIN_TREE_LEAVES = BLOCKS.registerBlock(
            "curtain_tree_leaves",
            properties -> new ColoredParticleLeavesBlock(0.01F, 0xFF768F93, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES));
    public static final DeferredBlock<SaplingBlock> CURTAIN_TREE_SAPLING = BLOCKS.registerBlock(
            "curtain_tree_sapling", properties -> new SaplingBlock(ModTreeGrowers.CURTAIN_TREE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING));
    public static final DeferredBlock<RotatedPillarBlock> CURSED_FROST_TREE_LOG = BLOCKS.registerBlock(
            "cursed_frost_tree_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LOG));
    public static final DeferredBlock<ColoredParticleLeavesBlock> CURSED_FROST_TREE_LEAVES = BLOCKS.registerBlock(
            "cursed_frost_tree_leaves",
            properties -> new ColoredParticleLeavesBlock(0.01F, 0xFFE1E9ED, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES));
    public static final DeferredBlock<SaplingBlock> CURSED_FROST_TREE_SAPLING = BLOCKS.registerBlock(
            "cursed_frost_tree_sapling", properties -> new SaplingBlock(ModTreeGrowers.CURSED_FROST_TREE, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING));

    public static final DeferredBlock<DropExperienceBlock> AURORIAN_COAL_ORE = ore("aurorian_coal_ore", Blocks.COAL_ORE, UniformInt.of(0, 2));
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_IRON_ORE = ore("aurorian_iron_ore", Blocks.IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_COPPER_ORE = ore("aurorian_copper_ore", Blocks.COPPER_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_GOLD_ORE = ore("aurorian_gold_ore", Blocks.GOLD_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_LAPIS_ORE = ore("aurorian_lapis_ore", Blocks.LAPIS_ORE, UniformInt.of(2, 5));
    public static final DeferredBlock<RedStoneOreBlock> AURORIAN_REDSTONE_ORE = redstoneOre("aurorian_redstone_ore", Blocks.REDSTONE_ORE);
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_DIAMOND_ORE = ore("aurorian_diamond_ore", Blocks.DIAMOND_ORE, UniformInt.of(3, 7));
    public static final DeferredBlock<DropExperienceBlock> AURORIAN_EMERALD_ORE = ore("aurorian_emerald_ore", Blocks.EMERALD_ORE, UniformInt.of(3, 7));
    public static final DeferredBlock<DropExperienceBlock> MOONSTONE_ORE = ore("moonstone_ore", Blocks.IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> CERULEAN_ORE = ore("cerulean_ore", Blocks.IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> GEODE_ORE = ore("geode_ore", Blocks.DIAMOND_ORE, ConstantInt.ZERO);

    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_IRON_ORE = ore("erosive_aurorian_iron_ore", Blocks.DEEPSLATE_IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_COPPER_ORE = ore("erosive_aurorian_copper_ore", Blocks.DEEPSLATE_COPPER_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_GOLD_ORE = ore("erosive_aurorian_gold_ore", Blocks.DEEPSLATE_GOLD_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_LAPIS_ORE = ore("erosive_aurorian_lapis_ore", Blocks.DEEPSLATE_LAPIS_ORE, UniformInt.of(2, 5));
    public static final DeferredBlock<RedStoneOreBlock> EROSIVE_AURORIAN_REDSTONE_ORE = redstoneOre("erosive_aurorian_redstone_ore", Blocks.DEEPSLATE_REDSTONE_ORE);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_DIAMOND_ORE = ore("erosive_aurorian_diamond_ore", Blocks.DEEPSLATE_DIAMOND_ORE, UniformInt.of(3, 7));
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_AURORIAN_EMERALD_ORE = ore("erosive_aurorian_emerald_ore", Blocks.DEEPSLATE_EMERALD_ORE, UniformInt.of(3, 7));
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_MOONSTONE_ORE = ore("erosive_moonstone_ore", Blocks.DEEPSLATE_IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_CERULEAN_ORE = ore("erosive_cerulean_ore", Blocks.DEEPSLATE_IRON_ORE, ConstantInt.ZERO);
    public static final DeferredBlock<DropExperienceBlock> EROSIVE_GEODE_ORE = ore("erosive_geode_ore", Blocks.DEEPSLATE_DIAMOND_ORE, ConstantInt.ZERO);

    public static final DeferredItem<BlockItem> AURORIAN_STONE_ITEM = ITEMS.registerSimpleBlockItem(AURORIAN_STONE);
    public static final DeferredItem<BlockItem> AURORIAN_EROSIVE_ITEM = ITEMS.registerSimpleBlockItem(AURORIAN_EROSIVE);
    public static final DeferredItem<BlockItem> AURORIAN_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AURORIAN_DIRT);
    public static final DeferredItem<BlockItem> AURORIAN_GRASS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AURORIAN_GRASS_BLOCK);
    public static final DeferredItem<BlockItem> MOON_SAND_RIVER_ITEM = ITEMS.registerSimpleBlockItem(MOON_SAND_RIVER);
    public static final DeferredItem<BlockItem> PEBBLE_ITEM = ITEMS.registerSimpleBlockItem(PEBBLE);
    public static final DeferredItem<Item> WHITE_GROUND_MUSHROOM_ITEM = ITEMS.registerItem(
            "white_ground_mushroom",
            properties -> new Item(properties.food(groundMushroomFood())));
    public static final DeferredItem<Item> BLUE_GROUND_MUSHROOM_ITEM = ITEMS.registerItem(
            "blue_ground_mushroom",
            properties -> new Item(properties.food(groundMushroomFood())));
    public static final DeferredItem<PlaceOnWaterBlockItem> AURORIAN_LILY_PAD_ITEM = ITEMS.registerItem(
            "aurorian_lily_pad",
            properties -> new PlaceOnWaterBlockItem(AURORIAN_LILY_PAD.get(), properties));
    public static final DeferredItem<PlaceOnWaterBlockItem> AURORIAN_WATER_MUSHROOM_ITEM = ITEMS.registerItem(
            "aurorian_water_mushroom",
            properties -> new PlaceOnWaterBlockItem(AURORIAN_WATER_MUSHROOM.get(), properties));
    public static final DeferredItem<Item> RAW_MOONSTONE = ITEMS.registerSimpleItem("raw_moonstone");
    public static final DeferredItem<Item> RAW_CERULEAN = ITEMS.registerSimpleItem("raw_cerulean");
    public static final DeferredItem<Item> CRYSTAL = ITEMS.registerSimpleItem("crystal");
    public static final DeferredItem<Item> AURORIAN_CLAY_BALL = ITEMS.registerSimpleItem("aurorian_clay_ball");
    public static final DeferredItem<Item> AURORIAN_BRICK = ITEMS.registerSimpleItem("aurorian_brick");
    public static final DeferredItem<BucketItem> MOON_DEW_BUCKET = ITEMS.registerItem(
            "moon_dew_bucket",
            properties -> new BucketItem(
                    ModFluids.MOON_DEW.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BlockItem> BLUEBERRY = ITEMS.registerItem(
            "blueberry",
            properties -> new BlockItem(
                    BLUEBERRY_BUSH.get(), properties.useItemDescriptionPrefix().food(Foods.SWEET_BERRIES)));
    public static final DeferredItem<AstrologyTableItem> ASTROLOGY_TABLE_ITEM = ITEMS.registerItem(
            "astrology_table", properties -> new AstrologyTableItem(ASTROLOGY_TABLE.get(), properties));

    static {
        BLOCKS.getEntries().stream()
                .filter(block -> block != AURORIAN_STONE && block != AURORIAN_EROSIVE
                        && block != AURORIAN_DIRT && block != AURORIAN_GRASS_BLOCK
                        && block != MOON_SAND_RIVER && block != MOON_DEW_BLOCK
                        && block != BLUEBERRY_BUSH && block != PEBBLE
                        && block != WHITE_GROUND_MUSHROOM && block != BLUE_GROUND_MUSHROOM
                        && block != AURORIAN_LILY_PAD && block != AURORIAN_WATER_MUSHROOM
                        && block != ASTROLOGY_TABLE
                        && block != AURORIAN_TWISTING_VINES_PLANT
                        && block != DEW_CAVE_VINES_PLANT
                        && block != TALL_AURORIAN_WATER_GRASS)
                .forEach(block -> ITEMS.registerSimpleBlockItem(block));
    }

    private ModBlocks() {
    }

    private static DeferredBlock<DropExperienceBlock> ore(String name, Block vanillaOre, IntProvider experience) {
        return BLOCKS.registerBlock(name, properties -> new DropExperienceBlock(experience, properties),
                () -> BlockBehaviour.Properties.ofFullCopy(vanillaOre));
    }

    private static DeferredBlock<RedStoneOreBlock> redstoneOre(String name, Block vanillaOre) {
        return BLOCKS.registerBlock(name, RedStoneOreBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(vanillaOre));
    }

    private static DeferredBlock<AurorianPlantBlock> plant(String name) {
        return BLOCKS.registerBlock(name, AurorianPlantBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
    }

    private static DeferredBlock<ColdAurorianPlantBlock> coldPlant(String name) {
        return BLOCKS.registerBlock(name, ColdAurorianPlantBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
    }

    private static FoodProperties groundMushroomFood() {
        return new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).build();
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
