package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

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
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.ASTROLOGY_TABLE_ITEM.get());
                        output.accept(ModBlocks.MOON_DEW_BUCKET.get());
                        output.accept(ModBlocks.AURORIAN_FLOWER_POT.get());
                        output.accept(ModItems.TRAINING_DUMMY.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT = TABS.register(
            "equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.equipment"))
                    .icon(() -> new ItemStack(ModItems.DIAMOND_ARCHER_CHESTPLATE.get()))
                    .displayItems((parameters, output) -> addEquipment(output))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD = TABS.register(
            "food",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.food"))
                    .icon(() -> new ItemStack(ModBlocks.BLUEBERRY.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.BLUEBERRY.get());
                        output.accept(ModBlocks.WHITE_GROUND_MUSHROOM_ITEM.get());
                        output.accept(ModBlocks.BLUE_GROUND_MUSHROOM_ITEM.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INGREDIENTS = TABS.register(
            "ingredients",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.ingredients"))
                    .icon(() -> new ItemStack(ModBlocks.CRYSTAL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.PEBBLE_ITEM.get());
                        output.accept(ModBlocks.SILENT_WOOD_STICK.get());
                        output.accept(ModBlocks.RAW_MOONSTONE.get());
                        output.accept(ModBlocks.RAW_CERULEAN.get());
                        output.accept(ModBlocks.CRYSTAL.get());
                        output.accept(ModBlocks.AURORIAN_CLAY_BALL.get());
                        output.accept(ModBlocks.AURORIAN_BRICK.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    private static void addBuildingBlocks(CreativeModeTab.Output output) {
        output.accept(ModBlocks.AURORIAN_STONE.get());
        output.accept(ModBlocks.AURORIAN_EROSIVE.get());
        output.accept(ModBlocks.AURORIAN_COBBLESTONE.get());
        output.accept(ModBlocks.SMOOTH_MOON_SANDSTONE.get());
        output.accept(ModBlocks.AURORIAN_BRICKS.get());
        output.accept(ModBlocks.AURORIAN_BRICK_STAIRS.get());
        output.accept(ModBlocks.AURORIAN_BRICK_SLAB.get());
        output.accept(ModBlocks.AURORIAN_BRICK_WALL.get());
        output.accept(ModBlocks.AURORIAN_TERRACOTTA.get());
        output.accept(ModBlocks.SILENT_TREE_LOG.get());
        output.accept(ModBlocks.CURTAIN_TREE_LOG.get());
        output.accept(ModBlocks.CURSED_FROST_TREE_LOG.get());
    }

    private static void addNaturalBlocks(CreativeModeTab.Output output) {
        output.accept(ModBlocks.AURORIAN_DIRT.get());
        output.accept(ModBlocks.AURORIAN_CLAY.get());
        output.accept(ModBlocks.AURORIAN_GRASS_BLOCK.get());
        output.accept(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get());
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
    }

    private static void addEquipment(CreativeModeTab.Output output) {
        output.accept(ModItems.DIAMOND_ARCHER_HELMET.get());
        output.accept(ModItems.DIAMOND_ARCHER_CHESTPLATE.get());
        output.accept(ModItems.DIAMOND_ARCHER_LEGGINGS.get());
        output.accept(ModItems.DIAMOND_ARCHER_BOOTS.get());
        output.accept(ModItems.GOLDEN_ARCHER_HELMET.get());
        output.accept(ModItems.GOLDEN_ARCHER_CHESTPLATE.get());
        output.accept(ModItems.GOLDEN_ARCHER_LEGGINGS.get());
        output.accept(ModItems.GOLDEN_ARCHER_BOOTS.get());
        output.accept(ModItems.IRON_ARCHER_HELMET.get());
        output.accept(ModItems.IRON_ARCHER_CHESTPLATE.get());
        output.accept(ModItems.IRON_ARCHER_LEGGINGS.get());
        output.accept(ModItems.IRON_ARCHER_BOOTS.get());
        output.accept(ModItems.NETHERITE_ARCHER_HELMET.get());
        output.accept(ModItems.NETHERITE_ARCHER_CHESTPLATE.get());
        output.accept(ModItems.NETHERITE_ARCHER_LEGGINGS.get());
        output.accept(ModItems.NETHERITE_ARCHER_BOOTS.get());
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
