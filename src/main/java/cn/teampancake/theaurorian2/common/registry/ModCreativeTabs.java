package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    private static final List<String> STONE_FAMILY_ORDER = List.of(
            "aurorian_stone",
            "aurorian_cobblestone",
            "aurorian_andesite",
            "aurorian_diorite",
            "aurorian_granite",
            "aurorian_peridotite",
            "aurorian_brick",
            "moon_sandstone",
            "aurorian_portal_frame",
            "aurorian_castle_rune_stone",
            "cerulean_castle_rune_stone",
            "crystalline_castle_rune_stone",
            "moon_castle_rune_stone",
            "moonsilver_castle_rune_stone",
            "umbra_castle_rune_stone",
            "rune_stone",
            "moon_temple",
            "dark_stone",
            "umbra_stone",
            "void_stone");

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheAurorian2.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING_BLOCKS = TABS.register(
            "building_blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.building_blocks"))
                    .icon(() -> new ItemStack(ModBlocks.AURORIAN_STONE.get()))
                    .displayItems((parameters, output) -> addBuildingBlocks(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NATURAL_BLOCKS = TABS.register(
            "natural_blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.natural_blocks"))
                    .icon(() -> new ItemStack(ModBlocks.AURORIAN_GRASS_BLOCK.get()))
                    .displayItems((parameters, output) -> addNaturalBlocks(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FUNCTIONAL_ITEMS = TABS.register(
            "functional_items",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.functional_items"))
                    .icon(() -> new ItemStack(ModBlocks.ASTROLOGY_TABLE_ITEM.get()))
                    .displayItems((parameters, output) -> addFunctionalItems(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_AND_UTILITIES = TABS.register(
            "tools_and_utilities",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.tools_and_utilities"))
                    .icon(() -> new ItemStack(ModItems.AURORIAN_STONE_PICKAXE.get()))
                    .displayItems((parameters, output) -> addToolsAndUtilities(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT = TABS.register(
            "equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.equipment"))
                    .icon(() -> new ItemStack(ModItems.STARLIGHT_RANGER_CHESTPLATE.get()))
                    .displayItems((parameters, output) -> addCombatItems(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD = TABS.register(
            "food",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.food"))
                    .icon(() -> new ItemStack(ModBlocks.BLUEBERRY.get()))
                    .displayItems(ModCreativeTabs::addFood)
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INGREDIENTS = TABS.register(
            "ingredients",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.ingredients"))
                    .icon(() -> new ItemStack(ModBlocks.CRYSTAL.get()))
                    .displayItems(ModCreativeTabs::addIngredients)
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ACCESSORIES_AND_ARTIFACTS = TABS.register(
            "accessories_and_artifacts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable(
                            "itemGroup.theaurorian2.accessories_and_artifacts"))
                    .icon(() -> new ItemStack(ModAccessoryItems.AURORIAN_BLESSING.get()))
                    .displayItems((parameters, output) -> addAccessoriesAndArtifacts(output))
                    .build());

    private ModCreativeTabs() {
    }

    private static void addAccessoriesAndArtifacts(CreativeModeTab.Output output) {
        ModAccessoryItems.ALL.forEach(item -> output.accept(item.get()));
        ModLegacyItems.forEachAccessories(output::accept);
    }

    private static void addBuildingBlocks(CreativeModeTab.Output output) {
        Map<Item, ItemStack> items = new LinkedHashMap<>();
        addBuildingBlock(items, ModBlocks.AURORIAN_STONE.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_EROSIVE.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_COBBLESTONE.get());
        addBuildingBlock(items, ModBlocks.MOSSY_AURORIAN_COBBLESTONE.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_COBBLESTONE_STAIRS.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_COBBLESTONE_SLAB.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_COBBLESTONE_WALL.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_PERIDOTITE.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_PORTAL_FRAME_BRICKS.get());
        addBuildingBlock(items, ModBlocks.SMOOTH_MOON_SANDSTONE.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_BRICKS.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_BRICK_STAIRS.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_BRICK_SLAB.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_BRICK_WALL.get());
        addBuildingBlock(items, ModBlocks.AURORIAN_TERRACOTTA.get());
        addWoodSet(items, ModBlocks.SILENT_WOOD);
        addWoodSet(items, ModBlocks.CURTAIN_WOOD);
        addWoodSet(items, ModBlocks.CURSED_FROST_WOOD);
        ModStructureBlocks.buildingBlocks().forEach(block -> addBuildingBlock(items, block.get()));
        ModStructureBlocks.decorativeBlocks().forEach(block -> addBuildingBlock(items, block.get()));

        List<ItemStack> sortedItems = new ArrayList<>(items.values());
        sortedItems.sort(Comparator
                .comparingInt(ModCreativeTabs::buildingSectionOrder)
                .thenComparingInt(ModCreativeTabs::buildingFamilyOrder)
                .thenComparingInt(ModCreativeTabs::buildingStyleOrder)
                .thenComparingInt(ModCreativeTabs::buildingVariantOrder)
                .thenComparing(
                        stack -> stack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ModCreativeTabs::itemPath));
        sortedItems.forEach(output::accept);
    }

    private static void addWoodSet(Map<Item, ItemStack> items, ModBlocks.WoodSet wood) {
        addBuildingBlock(items, wood.log().get());
        addBuildingBlock(items, wood.strippedLog().get());
        addBuildingBlock(items, wood.wood().get());
        addBuildingBlock(items, wood.strippedWood().get());
        addBuildingBlock(items, wood.planks().get());
        addBuildingBlock(items, wood.stairs().get());
        addBuildingBlock(items, wood.slab().get());
        addBuildingBlock(items, wood.fence().get());
        addBuildingBlock(items, wood.fenceGate().get());
        addBuildingBlock(items, wood.door().get());
        addBuildingBlock(items, wood.trapdoor().get());
        addBuildingBlock(items, wood.pressurePlate().get());
        addBuildingBlock(items, wood.button().get());
    }

    private static void addBuildingBlock(Map<Item, ItemStack> items, Block block) {
        ItemStack stack = new ItemStack(block);
        if (!stack.isEmpty()) {
            items.putIfAbsent(stack.getItem(), stack);
        }
    }

    private static int buildingSectionOrder(ItemStack stack) {
        String path = itemPath(stack);
        if (isWoodBuildingBlock(path)) {
            return 0;
        }
        if (isOtherBuildingBlock(path)) {
            return 2;
        }
        return 1;
    }

    private static int buildingFamilyOrder(ItemStack stack) {
        String path = itemPath(stack);
        int section = buildingSectionOrder(stack);
        if (section == 0) {
            if (path.contains("silent_")) {
                return 0;
            }
            if (path.contains("curtain_")) {
                return 1;
            }
            if (path.contains("cursed_frost_")) {
                return 2;
            }
            if (path.contains("weeping_willow_")) {
                return 3;
            }
            return 100;
        }
        if (section == 1) {
            for (int i = 0; i < STONE_FAMILY_ORDER.size(); i++) {
                if (path.contains(STONE_FAMILY_ORDER.get(i))) {
                    return i;
                }
            }
            return 100;
        }
        if (path.endsWith("_sand")) {
            return 0;
        }
        if (path.contains("_glass")) {
            return 1;
        }
        if (path.contains("_wool")) {
            return 2;
        }
        if (path.contains("terracotta")) {
            return 3;
        }
        if (path.endsWith("_block") || path.endsWith("_gem")) {
            return 4;
        }
        if (path.contains("_crystal")) {
            return 5;
        }
        if (path.endsWith("_bars")) {
            return 6;
        }
        if (path.contains("_lamp")) {
            return 7;
        }
        if (path.contains("_gate")) {
            return 8;
        }
        if (path.contains("pedestal")) {
            return 9;
        }
        if (path.equals("urn")) {
            return 10;
        }
        return 100;
    }

    private static int buildingStyleOrder(ItemStack stack) {
        if (buildingSectionOrder(stack) != 1) {
            return 0;
        }
        String path = itemPath(stack);
        if (path.contains("smooth_")) {
            return 20;
        }
        if (path.contains("chiseled_")) {
            return 30;
        }
        if (path.contains("luminous_")) {
            return 40;
        }
        if (path.contains("transparent_")) {
            return 50;
        }
        if (path.contains("_cracked")) {
            return 60;
        }
        if (path.contains("_roof")) {
            return 70;
        }
        if (path.contains("_fancy")) {
            return 80;
        }
        if (path.contains("_layers")) {
            return 90;
        }
        if (path.contains("_brick")) {
            return 10;
        }
        return 0;
    }

    private static int buildingVariantOrder(ItemStack stack) {
        String path = itemPath(stack);
        if (buildingSectionOrder(stack) == 0) {
            if (path.endsWith("_log")) {
                return path.startsWith("stripped_") ? 1 : 0;
            }
            if (path.endsWith("_wood")) {
                return path.startsWith("stripped_") ? 3 : 2;
            }
            if (path.endsWith("_planks")) {
                return 4;
            }
            if (path.startsWith("vertical_") && path.endsWith("_stairs")) {
                return 15;
            }
            if (path.startsWith("vertical_") && path.endsWith("_slab")) {
                return 16;
            }
            if (path.endsWith("_stairs")) {
                return 5;
            }
            if (path.endsWith("_slab")) {
                return 6;
            }
            if (path.endsWith("_fence")) {
                return 9;
            }
            if (path.endsWith("_fence_gate")) {
                return 10;
            }
            if (path.endsWith("_trapdoor")) {
                return 12;
            }
            if (path.endsWith("_door")) {
                return 11;
            }
            if (path.endsWith("_pressure_plate")) {
                return 13;
            }
            if (path.endsWith("_button")) {
                return 14;
            }
            return 100;
        }
        if (path.startsWith("vertical_") && path.endsWith("_stairs")) {
            return 5;
        }
        if (path.startsWith("vertical_") && path.endsWith("_slab")) {
            return 6;
        }
        if (path.endsWith("_stairs")) {
            return 2;
        }
        if (path.endsWith("_slab")) {
            return 3;
        }
        if (path.endsWith("_wall")) {
            return 4;
        }
        if (path.endsWith("_pillar")) {
            return 1;
        }
        return 0;
    }

    private static boolean isWoodBuildingBlock(String path) {
        return path.contains("silent_tree_")
                || path.contains("silent_wood_")
                || path.contains("curtain_tree_")
                || path.contains("curtain_wood_")
                || path.contains("cursed_frost_tree_")
                || path.contains("cursed_frost_wood_")
                || path.contains("weeping_willow_");
    }

    private static boolean isOtherBuildingBlock(String path) {
        return path.endsWith("_sand")
                || path.contains("_glass")
                || path.contains("terracotta")
                || path.contains("_wool")
                || path.endsWith("_block")
                || path.endsWith("_gem")
                || path.contains("_crystal")
                || path.endsWith("_bars")
                || path.contains("_lamp")
                || path.contains("_gate")
                || path.contains("pedestal")
                || path.equals("urn")
                || path.contains("barrier");
    }

    private static String itemPath(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static void addFunctionalItems(CreativeModeTab.Output output) {
        output.accept(ModItems.PURIFICATION_TEST_ITEM.get());
        output.accept(ModBlocks.ASTROLOGY_TABLE_ITEM.get());
        output.accept(ModBlocks.AURORIAN_CRAFTING_TABLE.get());
        output.accept(ModBlocks.AURORIAN_FURNACE.get());
        output.accept(ModBlocks.FIREPLACE.get());
        output.accept(ModBlocks.SILENT_WOOD_TABLE.get());
        output.accept(ModBlocks.CURTAIN_WOOD_TABLE.get());
        output.accept(ModBlocks.CURSED_FROST_WOOD_TABLE.get());
        output.accept(ModBlocks.AURORIAN_CHEST.get());
        output.accept(ModBlocks.SILENT_WOOD_LADDER.get());
        output.accept(ModBlocks.MYSTERIUM_WOOL_BED_ITEM.get());
        output.accept(ModBlocks.SILENT_WOOD_TORCH_ITEM.get());
        output.accept(ModBlocks.AURORIAN_RAIL.get());
        output.accept(ModBlocks.AURORIAN_FLOWER_POT.get());
        output.accept(ModItems.TRAINING_DUMMY.get());
        ModLegacyItems.forEachFunctional(output::accept);
        ModStructureBlocks.functionalBlocks().forEach(block -> output.accept(block.get()));
    }

    private static void addToolsAndUtilities(CreativeModeTab.Output output) {
        output.accept(ModItems.SILENT_WOOD_PICKAXE.get());
        output.accept(ModItems.SILENT_WOOD_AXE.get());
        output.accept(ModItems.SILENT_WOOD_SHOVEL.get());
        output.accept(ModItems.SILENT_WOOD_HOE.get());
        output.accept(ModItems.AURORIAN_STONE_PICKAXE.get());
        output.accept(ModItems.AURORIAN_STONE_AXE.get());
        output.accept(ModItems.AURORIAN_STONE_SHOVEL.get());
        output.accept(ModItems.AURORIAN_STONE_HOE.get());
        output.accept(ModBlocks.MOON_DEW_BUCKET.get());
        output.accept(ModItems.AURORIAN_CHEST_MINECART.get());
        ModLegacyItems.forEachTools(output::accept);
    }

    private static void addNaturalBlocks(CreativeModeTab.Output output) {
        output.accept(ModBlocks.AURORIAN_DIRT.get());
        output.accept(ModBlocks.AURORIAN_CLAY.get());
        output.accept(ModBlocks.AURORIAN_ANDESITE.get());
        output.accept(ModBlocks.AURORIAN_DIORITE.get());
        output.accept(ModBlocks.AURORIAN_GRANITE.get());
        output.accept(ModBlocks.AURORIAN_GRASS_BLOCK.get());
        output.accept(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get());
        output.accept(ModBlocks.AURORIAN_GRASS_ROCK_ITEM.get());
        output.accept(ModBlocks.MOON_SAND_RIVER.get());
        output.accept(ModBlocks.AURORIAN_DRIPSTONE_BLOCK.get());
        output.accept(ModBlocks.AURORIAN_POINTED_DRIPSTONE.get());
        output.accept(ModBlocks.LUMINOUS_MOSS_BLOCK.get());
        output.accept(ModBlocks.LUMINOUS_MOSS_CARPET.get());
        output.accept(ModBlocks.STAR_AZALEA.get());
        output.accept(ModBlocks.FLOWERING_STAR_AZALEA.get());
        output.accept(ModBlocks.MIST_SPORE_BLOSSOM.get());
        output.accept(ModBlocks.DEW_CAVE_VINES.get());
        output.accept(ModBlocks.STAR_GLOW_LICHEN.get());
        output.accept(ModBlocks.CERULEAN_CLUSTER.get());
        output.accept(ModBlocks.LARGE_CERULEAN_BUD.get());
        output.accept(ModBlocks.MEDIUM_CERULEAN_BUD.get());
        output.accept(ModBlocks.SMALL_CERULEAN_BUD.get());
        output.accept(ModBlocks.MOONSTONE_CLUSTER.get());
        output.accept(ModBlocks.LARGE_MOONSTONE_BUD.get());
        output.accept(ModBlocks.MEDIUM_MOONSTONE_BUD.get());
        output.accept(ModBlocks.SMALL_MOONSTONE_BUD.get());

        output.accept(ModBlocks.AURORIAN_COAL_ORE.get());
        output.accept(ModBlocks.AURORIAN_IRON_ORE.get());
        output.accept(ModBlocks.AURORIAN_COPPER_ORE.get());
        output.accept(ModBlocks.AURORIAN_GOLD_ORE.get());
        output.accept(ModBlocks.AURORIAN_LAPIS_ORE.get());
        output.accept(ModBlocks.AURORIAN_REDSTONE_ORE.get());
        output.accept(ModBlocks.AURORIAN_DIAMOND_ORE.get());
        output.accept(ModBlocks.AURORIAN_EMERALD_ORE.get());
        output.accept(ModBlocks.MOONSTONE_ORE.get());
        output.accept(ModBlocks.CERULEAN_ORE.get());
        output.accept(ModBlocks.GEODE_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_IRON_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_COPPER_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_GOLD_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_LAPIS_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_REDSTONE_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_DIAMOND_ORE.get());
        output.accept(ModBlocks.EROSIVE_AURORIAN_EMERALD_ORE.get());
        output.accept(ModBlocks.EROSIVE_MOONSTONE_ORE.get());
        output.accept(ModBlocks.EROSIVE_CERULEAN_ORE.get());
        output.accept(ModBlocks.EROSIVE_GEODE_ORE.get());

        output.accept(ModBlocks.SILENT_TREE_LEAVES.get());
        output.accept(ModBlocks.FRUITING_SILENT_TREE_LEAVES.get());
        output.accept(ModBlocks.SILENT_TREE_SAPLING.get());
        output.accept(ModBlocks.CURTAIN_TREE_LEAVES.get());
        output.accept(ModBlocks.CURTAIN_TREE_SAPLING.get());
        output.accept(ModBlocks.CURSED_FROST_TREE_LEAVES.get());
        output.accept(ModBlocks.CURSED_FROST_TREE_SAPLING.get());

        output.accept(ModBlocks.AURORIAN_GRASS.get());
        output.accept(ModBlocks.TALL_AURORIAN_GRASS.get());
        output.accept(ModBlocks.AURORIAN_GRASS_LIGHT.get());
        output.accept(ModBlocks.TALL_AURORIAN_GRASS_LIGHT.get());
        output.accept(ModBlocks.AURORIAN_WATER_GRASS.get());
        output.accept(ModBlocks.AURORIAN_LILY_PAD_ITEM.get());
        output.accept(ModBlocks.AURORIAN_WATER_MUSHROOM_ITEM.get());
        output.accept(ModBlocks.PETUNIA_PLANT.get());
        output.accept(ModBlocks.NEBULA_BLOSSOM_CLUSTER.get());
        output.accept(ModBlocks.MOON_FROST_FLOWER.get());
        output.accept(ModBlocks.VOID_CANDLE_FLOWER.get());
        output.accept(ModBlocks.LAVENDER_PLANT.get());
        output.accept(ModBlocks.TALL_LAVENDER_PLANT.get());
        output.accept(ModBlocks.DREAMSCAPE_PISTIL.get());
        output.accept(ModBlocks.FROST_TEARS_FLOWER.get());
        output.accept(ModBlocks.CRISPED_MALLOW.get());
        output.accept(ModBlocks.FROST_SNOW_GRASS.get());
        output.accept(ModBlocks.ICE_CALENDULA.get());
        output.accept(ModBlocks.WINTER_ROOT.get());
        output.accept(ModBlocks.TALL_WICK_GRASS.get());
        output.accept(ModBlocks.INDIGO_MUSHROOM.get());
        output.accept(ModBlocks.BROWN_MUSHROOM.get());
        output.accept(ModBlocks.DARK_BROWN_MUSHROOM.get());
        output.accept(ModBlocks.RED_MUSHROOM.get());
        output.accept(ModBlocks.AURORIAN_VINE.get());
        output.accept(ModBlocks.AURORIAN_TWISTING_VINES.get());
        ModStructureBlocks.naturalBlocks().forEach(block -> output.accept(block.get()));
    }

    private static void addCombatItems(CreativeModeTab.Output output) {
        output.accept(ModItems.SPIDER_MOTHER_SPAWN_EGG.get());
        output.accept(ModItems.SPIDERLING_SPAWN_EGG.get());
        output.accept(ModItems.SPIDERLING_CRYSTAL_SHELL_SPAWN_EGG.get());
        output.accept(ModItems.SPIDERLING_WALL_CLIMBER_SPAWN_EGG.get());
        output.accept(ModItems.PHANTOM_BLOSSOM_REQUIEM.get());
        output.accept(ModItems.SILENT_WOOD_SWORD.get());
        output.accept(ModItems.AURORIAN_STONE_SWORD.get());

        output.accept(ModItems.STARLIGHT_RANGER_HELMET.get());
        output.accept(ModItems.STARLIGHT_RANGER_CHESTPLATE.get());
        output.accept(ModItems.STARLIGHT_RANGER_LEGGINGS.get());
        output.accept(ModItems.STARLIGHT_RANGER_BOOTS.get());
        output.accept(ModItems.DAWNLIGHT_RANGER_HELMET.get());
        output.accept(ModItems.DAWNLIGHT_RANGER_CHESTPLATE.get());
        output.accept(ModItems.DAWNLIGHT_RANGER_LEGGINGS.get());
        output.accept(ModItems.DAWNLIGHT_RANGER_BOOTS.get());
        output.accept(ModItems.FORESTSHADE_RANGER_HELMET.get());
        output.accept(ModItems.FORESTSHADE_RANGER_CHESTPLATE.get());
        output.accept(ModItems.FORESTSHADE_RANGER_LEGGINGS.get());
        output.accept(ModItems.FORESTSHADE_RANGER_BOOTS.get());
        output.accept(ModItems.DUSKFLAME_RANGER_HELMET.get());
        output.accept(ModItems.DUSKFLAME_RANGER_CHESTPLATE.get());
        output.accept(ModItems.DUSKFLAME_RANGER_LEGGINGS.get());
        output.accept(ModItems.DUSKFLAME_RANGER_BOOTS.get());

        output.accept(ModItems.STARFORGED_KNIGHT_GREATSWORD.get());
        output.accept(ModItems.STARFORGED_KNIGHT_SPEAR.get());
        output.accept(ModItems.STARFORGED_KNIGHT_HELMET.get());
        output.accept(ModItems.STARFORGED_KNIGHT_CHESTPLATE.get());
        output.accept(ModItems.STARFORGED_KNIGHT_LEGGINGS.get());
        output.accept(ModItems.STARFORGED_KNIGHT_BOOTS.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_GREATSWORD.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_SPEAR.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_HELMET.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_CHESTPLATE.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_LEGGINGS.get());
        output.accept(ModItems.DAWNFORGED_KNIGHT_BOOTS.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_GREATSWORD.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_SPEAR.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_HELMET.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_CHESTPLATE.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_LEGGINGS.get());
        output.accept(ModItems.MOONFORGED_KNIGHT_BOOTS.get());
        ModLegacyItems.forEachEquipment(output::accept);
    }

    private static void addIngredients(
            CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(ModBlocks.PEBBLE_ITEM.get());
        output.accept(ModBlocks.SILENT_WOOD_STICK.get());
        output.accept(ModBlocks.RAW_MOONSTONE.get());
        output.accept(ModBlocks.RAW_CERULEAN.get());
        output.accept(ModBlocks.CRYSTAL.get());
        output.accept(ModBlocks.AURORIAN_CLAY_BALL.get());
        output.accept(ModBlocks.AURORIAN_BRICK.get());
        output.accept(ModItems.AURORIAN_LEATHER.get());
        ModLegacyItems.forEachIngredients(output::accept);
        parameters.holders().lookup(Registries.ENCHANTMENT).ifPresent(enchantments -> {
            addEnchantmentBooks(output, enchantments, ModEnchantments.IMPALE);
            addEnchantmentBooks(output, enchantments, ModEnchantments.OVERLOAD);
            addEnchantmentBooks(output, enchantments, ModEnchantments.SOUL_SLASH);
            addEnchantmentBooks(output, enchantments, ModEnchantments.NIGHT_WALKER);
            addEnchantmentBooks(output, enchantments, ModEnchantments.FREEZE_ASPECT);
        });
    }

    private static void addFood(
            CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(ModBlocks.BLUEBERRY.get());
        output.accept(ModBlocks.CLOUDBERRY.get());
        output.accept(ModBlocks.WHITE_GROUND_MUSHROOM_ITEM.get());
        output.accept(ModBlocks.BLUE_GROUND_MUSHROOM_ITEM.get());

        output.accept(ModItems.TEA_CUP.get());
        output.accept(ModItems.LAVENDER_TEA.get());
        output.accept(ModItems.SILK_BERRY_TEA.get());
        output.accept(ModItems.LAVENDER_SEEDY_TEA.get());
        output.accept(ModItems.PETUNIA_TEA.get());
        output.accept(ModItems.BEPSI.get());
        output.accept(ModItems.AURORIAN_SPECIALTY_DRINK.get());
        output.accept(ModItems.MOONLIT_BLUEBERRY_SPECIALTY_DRINK.get());
        output.accept(ModItems.SLEEPING_BLACK_TEA.get());
        output.accept(ModItems.WEEPING_WILLOW_SAP.get());

        output.accept(ModItems.AURORIAN_BEEF.get());
        output.accept(ModItems.AURORIAN_PORK.get());
        output.accept(ModItems.AURORIAN_MUTTON.get());
        output.accept(ModItems.AURORIAN_RABBIT.get());
        output.accept(ModItems.COOKED_AURORIAN_BEEF.get());
        output.accept(ModItems.COOKED_AURORIAN_PORK.get());
        output.accept(ModItems.COOKED_AURORIAN_MUTTON.get());
        output.accept(ModItems.COOKED_AURORIAN_RABBIT.get());
        output.accept(ModItems.SILK_BERRY_JAM.get());
        output.accept(ModItems.SILK_BERRY_JAM_SANDWICH.get());
        output.accept(ModItems.AURORIAN_SLIMEBALL.get());
        output.accept(ModItems.SILK_SHROOM_STEW.get());
        output.accept(ModItems.LAVENDER_BREAD.get());
        output.accept(ModItems.SOULLESS_FLESH.get());
        output.accept(ModItems.MOON_FISH.get());
        output.accept(ModItems.AURORIAN_WINGED_FISH.get());
        output.accept(ModItems.COOKED_MOON_FISH.get());
        output.accept(ModItems.COOKED_AURORIAN_WINGED_FISH.get());
        output.accept(ModItems.SILK_BERRY.get());
        output.accept(ModItems.AURORIAN_BERRY.get());
        output.accept(ModItems.CANDY.get());
        output.accept(ModItems.CANDY_CANE.get());
        output.accept(ModItems.GINGERBREAD_MAN.get());
        output.accept(ModItems.AURORIAN_BACON.get());
        output.accept(ModItems.STRANGE_MEAT.get());
        output.accept(ModItems.LAVENDER_SALAD.get());
        output.accept(ModItems.FAKE_ALGAL_PIT_FISH.get());
        output.accept(ModItems.SASHIMI.get());
        output.accept(ModItems.SILENT_WOOD_FRUIT.get());
        output.accept(ModItems.GOLDEN_SILENT_WOOD_FRUIT.get());
        output.accept(ModItems.KEBAB_WITH_MUSHROOM.get());
        output.accept(ModItems.AURORIAN_WINTER_ROOT.get());
        output.accept(ModItems.ROASTED_AURORIAN_WINTER_ROOT.get());
        output.accept(ModItems.DARK_STONE_SHRIMP.get());
        output.accept(ModItems.WHITE_CHOCOLATE.get());
        ModLegacyItems.forEachFood(output::accept);
    }

    private static void addEnchantmentBooks(
            CreativeModeTab.Output output,
            HolderLookup<Enchantment> enchantments,
            ResourceKey<Enchantment> key) {
        enchantments.get(key).ifPresent(enchantment -> IntStream
                .rangeClosed(enchantment.value().getMinLevel(), enchantment.value().getMaxLevel())
                .mapToObj(level -> EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level)))
                .forEach(output::accept));
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
