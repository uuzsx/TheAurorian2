package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianGrassBlock;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.TallGrassBlock;
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
    public static final DeferredBlock<Block> AURORIAN_DIRT = BLOCKS.registerSimpleBlock(
            "aurorian_dirt", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
    public static final DeferredBlock<AurorianGrassBlock> AURORIAN_GRASS_BLOCK = BLOCKS.registerBlock(
            "aurorian_grass_block", AurorianGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));
    public static final DeferredBlock<SandBlock> MOON_SAND_RIVER = BLOCKS.registerBlock(
            "moon_sand_river", properties -> new SandBlock(new ColorRGBA(0xFFB7A9D6), properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));

    public static final DeferredBlock<TallGrassBlock> AURORIAN_GRASS = BLOCKS.registerBlock(
            "aurorian_grass", TallGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS));
    public static final DeferredBlock<DoublePlantBlock> TALL_AURORIAN_GRASS = BLOCKS.registerBlock(
            "tall_aurorian_grass", DoublePlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS));
    public static final DeferredBlock<TallGrassBlock> AURORIAN_GRASS_LIGHT = BLOCKS.registerBlock(
            "aurorian_grass_light", TallGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel(state -> 2));
    public static final DeferredBlock<BushBlock> PETUNIA_PLANT = plant("petunia_plant");
    public static final DeferredBlock<BushBlock> NEBULA_BLOSSOM_CLUSTER = plant("nebula_blossom_cluster");
    public static final DeferredBlock<BushBlock> MOON_FROST_FLOWER = plant("moon_frost_flower");
    public static final DeferredBlock<BushBlock> VOID_CANDLE_FLOWER = plant("void_candle_flower");

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
    public static final DeferredItem<Item> RAW_MOONSTONE = ITEMS.registerSimpleItem("raw_moonstone");
    public static final DeferredItem<Item> RAW_CERULEAN = ITEMS.registerSimpleItem("raw_cerulean");
    public static final DeferredItem<Item> CRYSTAL = ITEMS.registerSimpleItem("crystal");

    static {
        BLOCKS.getEntries().stream()
                .filter(block -> block != AURORIAN_STONE && block != AURORIAN_EROSIVE
                        && block != AURORIAN_DIRT && block != AURORIAN_GRASS_BLOCK && block != MOON_SAND_RIVER)
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

    private static DeferredBlock<BushBlock> plant(String name) {
        return BLOCKS.registerBlock(name, BushBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
