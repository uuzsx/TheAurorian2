package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.stream.IntStream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
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
                        output.accept(ModBlocks.MOON_DEW_BUCKET.get());
                        output.accept(ModBlocks.AURORIAN_FLOWER_POT.get());
                        output.accept(ModItems.TRAINING_DUMMY.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT = TABS.register(
            "equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.equipment"))
                    .icon(() -> new ItemStack(ModItems.STARLIGHT_RANGER_CHESTPLATE.get()))
                    .displayItems(ModCreativeTabs::addEquipment)
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
        addWoodSet(output, ModBlocks.SILENT_WOOD);
        addWoodSet(output, ModBlocks.CURTAIN_WOOD);
        addWoodSet(output, ModBlocks.CURSED_FROST_WOOD);
    }

    private static void addWoodSet(CreativeModeTab.Output output, ModBlocks.WoodSet wood) {
        output.accept(wood.log().get());
        output.accept(wood.strippedLog().get());
        output.accept(wood.wood().get());
        output.accept(wood.strippedWood().get());
        output.accept(wood.planks().get());
        output.accept(wood.stairs().get());
        output.accept(wood.slab().get());
        output.accept(wood.fence().get());
        output.accept(wood.fenceGate().get());
        output.accept(wood.door().get());
        output.accept(wood.trapdoor().get());
        output.accept(wood.pressurePlate().get());
        output.accept(wood.button().get());
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
    }

    private static void addEquipment(
            CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(ModItems.SILENT_WOOD_SWORD.get());
        output.accept(ModItems.SILENT_WOOD_PICKAXE.get());
        output.accept(ModItems.SILENT_WOOD_AXE.get());
        output.accept(ModItems.SILENT_WOOD_SHOVEL.get());
        output.accept(ModItems.SILENT_WOOD_HOE.get());
        output.accept(ModItems.AURORIAN_STONE_SWORD.get());
        output.accept(ModItems.AURORIAN_STONE_PICKAXE.get());
        output.accept(ModItems.AURORIAN_STONE_AXE.get());
        output.accept(ModItems.AURORIAN_STONE_SHOVEL.get());
        output.accept(ModItems.AURORIAN_STONE_HOE.get());

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
