package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.common.item.ModelledItem;
import cn.teampancake.theaurorian2.common.item.LegacyArmorItem;
import cn.teampancake.theaurorian2.common.item.HolyKnightArmorItem;
import cn.teampancake.theaurorian2.common.item.QueensPickaxeItem;
import cn.teampancake.theaurorian2.common.item.WorldScrollItem;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModLegacyItems {

    private static final Map<Category, List<DeferredItem<Item>>> ITEMS_BY_CATEGORY =
            new EnumMap<>(Category.class);

    static {
        for (Category category : Category.values()) {
            ITEMS_BY_CATEGORY.put(category, new ArrayList<>());
        }
    }

    public static final DeferredItem<Item> AURORIAN_CHAIN =
            register("aurorian_chain", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIAN_COAL =
            register("aurorian_coal", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIAN_COAL_NUGGET =
            register("aurorian_coal_nugget", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIAN_CRYSTAL =
            register("aurorian_crystal", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIAN_SLATE_BRICK =
            register("aurorian_slate_brick", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIANITE_INGOT =
            register("aurorianite_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> AURORIANITE_SCRAP =
            register("aurorianite_scrap", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> CERULEAN_INGOT =
            register("cerulean_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> CERULEAN_NUGGET =
            register("cerulean_nugget", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> CRYSTALLINE_INGOT =
            register("crystalline_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> CRYSTALLINE_SCRAP =
            register("crystalline_scrap", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> DREAM_DYEING_CRYSTAL_FRAGMENT =
            register("dream_dyeing_crystal_fragment", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> FRAGMENTED_DRUSE =
            register("fragmented_druse", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> HARD_RUNE_KNOWLEDGE_FRAGMENT =
            register("hard_rune_knowledge_fragment", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> LAVENDER =
            register("lavender", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> LAVENDER_SEEDS =
            registerLavenderSeeds();
    public static final DeferredItem<Item> MOONSILVER_INGOT =
            register("moonsilver_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> MOONSILVER_NUGGET =
            register("moonsilver_nugget", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> MOONSTONE_INGOT =
            register("moonstone_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> MOONSTONE_NUGGET =
            register("moonstone_nugget", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> PLANT_FIBER =
            register("plant_fiber", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> RUNE_KNOWLEDGE_FRAGMENT =
            register("rune_knowledge_fragment", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> SILK_BERRY_PLANT =
            register("silk_berry_plant", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> SPECTRAL_SILK =
            register("spectral_silk", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> UMBRA_INGOT =
            register("umbra_ingot", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> UMBRA_SCRAP =
            register("umbra_scrap", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> UNSTABLE_CRYSTAL =
            register("unstable_crystal", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> VAGRANT_NOTE_PAGE =
            register("vagrant_note_page", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> WEBBING =
            register("webbing", Category.INGREDIENTS, 64);
    public static final DeferredItem<Item> EQUINOX_MUSHROOM =
            register("equinox_mushroom", Category.FOOD, 1);
    public static final DeferredItem<Item> TEA =
            register("tea", Category.FOOD, 1);
    public static final DeferredItem<Item> AURORIAN_WINGED_FISH_BUCKET =
            registerFishBucket("aurorian_winged_fish_bucket", ModEntities.AURORIAN_WINGED_FISH);
    public static final DeferredItem<Item> AURORIANITE_AXE =
            register("aurorianite_axe", Category.TOOLS, 1);
    public static final DeferredItem<Item> AURORIANITE_PICKAXE =
            register("aurorianite_pickaxe", Category.TOOLS, 1);
    public static final DeferredItem<Item> BOOK_OF_SIN =
            register("book_of_sin", Category.ACCESSORIES, 1);
    public static final DeferredItem<Item> BROKEN_OX_HORN =
            register("broken_ox_horn", Category.INGREDIENTS, 1);
    public static final DeferredItem<Item> CRYSTALLINE_PICKAXE =
            register("crystalline_pickaxe", Category.TOOLS, 1);
    public static final DeferredItem<Item> DARK_STONE_KEY =
            register("dark_stone_key", Category.TOOLS, 1);
    public static final DeferredItem<Item> DEVELOPER_GIFT =
            register("developer_gift", Category.TOOLS, 1);
    public static final DeferredItem<Item> DUNGEON_LOCATOR =
            register("dungeon_locator", Category.TOOLS, 1);
    public static final DeferredItem<Item> LIVING_DIVINING_ROD =
            register("living_divining_rod", Category.TOOLS, 1);
    public static final DeferredItem<Item> LOCK_PICKS =
            register("lock_picks", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOON_FISH_BUCKET =
            registerFishBucket("moon_fish_bucket", ModEntities.MOON_FISH);
    public static final DeferredItem<Item> MOON_TEMPLE_CELL_KEY =
            register("moon_temple_cell_key", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOON_TEMPLE_CELL_KEY_FRAGMENT =
            register("moon_temple_cell_key_fragment", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOON_TEMPLE_KEY =
            register("moon_temple_key", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOON_WATER_BUCKET =
            register("moon_water_bucket", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSILVER_AXE =
            registerModelled("moonsilver_axe", Category.TOOLS, "moonsilver_axe_3d", null);
    public static final DeferredItem<Item> MOONSILVER_HOE =
            register("moonsilver_hoe", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSILVER_PICKAXE =
            registerModelled("moonsilver_pickaxe", Category.TOOLS, "moonsilver_pickaxe_3d", null);
    public static final DeferredItem<Item> MOONSILVER_SHOVEL =
            registerModelled("moonsilver_shovel", Category.TOOLS, "moonsilver_shovel_3d", null);
    public static final DeferredItem<Item> MOONSTONE_AXE =
            register("moonstone_axe", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSTONE_HOE =
            register("moonstone_hoe", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSTONE_PICKAXE =
            register("moonstone_pickaxe", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSTONE_SHOVEL =
            register("moonstone_shovel", Category.TOOLS, 1);
    public static final DeferredItem<Item> MOONSTONE_SICKLE =
            register("moonstone_sickle", Category.TOOLS, 1);
    public static final DeferredItem<Item> MUSIC_DISC_AURORIAN_FOREST =
            register("music_disc_aurorian_forest", Category.TOOLS, 1);
    public static final DeferredItem<Item> MUSIC_DISC_MOONLIT_VEIL =
            register("music_disc_moonlit_veil", Category.TOOLS, 1);
    public static final DeferredItem<Item> RED_BOOK =
            register("red_book", Category.TOOLS, 1);
    public static final DeferredItem<Item> RUNE_STONE_KEY =
            register("rune_stone_key", Category.TOOLS, 1);
    public static final DeferredItem<Item> RUNE_STONE_LOOT_KEY =
            register("rune_stone_loot_key", Category.TOOLS, 1);
    public static final DeferredItem<Item> SILENT_WOOD_SICKLE =
            register("silent_wood_sickle", Category.TOOLS, 1);
    public static final DeferredItem<Item> THE_AURORIAN_GUIDE =
            register("the_aurorian_guide", Category.TOOLS, 1);
    public static final DeferredItem<Item> UMBRA_PICKAXE =
            register("umbra_pickaxe", Category.TOOLS, 1);
    public static final DeferredItem<Item> VAGRANT_NOTE =
            register("vagrant_note", Category.TOOLS, 1);
    public static final DeferredItem<Item> WORLD_SCROLL =
            registerWorldScroll();
    public static final DeferredItem<Item> ABSORPTION_ORB =
            register("absorption_orb", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_ALLOY_STEEL_SWORD =
            register("aurorian_alloy_steel_sword", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_CHAKRAM =
            register("aurorian_chakram", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_FLYING_AXE =
            register("aurorian_flying_axe", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_NIGHT_KUNAI =
            register("aurorian_night_kunai", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_NIGHT_RIPPER =
            register("aurorian_night_ripper", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIAN_SLIME_BOOTS =
            registerArmor("aurorian_slime_boots", "aurorian_slime_boots", ArmorType.BOOTS, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> AURORIAN_STONE_SICKLE =
            register("aurorian_stone_sickle", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> AURORIANITE_SWORD =
            register("aurorianite_sword", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> CAT_BELL =
            register("cat_bell", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> CERULEAN_ARROW =
            register("cerulean_arrow", Category.EQUIPMENT, 64);
    public static final DeferredItem<Item> CERULEAN_BOOTS =
            registerArmor("cerulean_boots", "cerulean", ArmorType.BOOTS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CERULEAN_CHESTPLATE =
            registerArmor("cerulean_chestplate", "cerulean", ArmorType.CHESTPLATE, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CERULEAN_HELMET =
            registerArmor("cerulean_helmet", "cerulean", ArmorType.HELMET, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CERULEAN_LEGGINGS =
            registerArmor("cerulean_leggings", "cerulean", ArmorType.LEGGINGS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CERULEAN_SHIELD =
            register("cerulean_shield", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> CRIMSON_PACT_PENDANT =
            register("crimson_pact_pendant", Category.ACCESSORIES, 1);
    public static final DeferredItem<Item> CRYSTAL_ARROW =
            register("crystal_arrow", Category.EQUIPMENT, 64);
    public static final DeferredItem<Item> CRYSTAL_RUNE_BOOTS =
            registerArmor("crystal_rune_boots", "crystal_rune", ArmorType.BOOTS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CRYSTAL_RUNE_CHESTPLATE =
            registerArmor("crystal_rune_chestplate", "crystal_rune", ArmorType.CHESTPLATE, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CRYSTAL_RUNE_HELMET =
            registerArmor("crystal_rune_helmet", "crystal_rune", ArmorType.HELMET, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CRYSTAL_RUNE_LEGGINGS =
            registerArmor("crystal_rune_leggings", "crystal_rune", ArmorType.LEGGINGS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> CRYSTALLINE_SHIELD =
            register("crystalline_shield", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> CRYSTALLINE_SPEAR =
            register("crystalline_spear", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> CRYSTALLINE_SWORD =
            registerModelled("crystalline_sword", Category.EQUIPMENT, "crystalline_sword_3d", null);
    public static final DeferredItem<Item> DARK_AMULET =
            register("dark_amulet", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> DUNGEON_KEEPER_AMULET =
            register("dungeon_keeper_amulet", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> HOLY_KNIGHT_BOOTS =
            registerArmor("holy_knight_boots", "holy_knight", ArmorType.BOOTS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> HOLY_KNIGHT_CHESTPLATE =
            registerArmor("holy_knight_chestplate", "holy_knight", ArmorType.CHESTPLATE, ArmorMaterials.IRON);
    public static final DeferredItem<Item> HOLY_KNIGHT_HELMET =
            registerArmor("holy_knight_helmet", "holy_knight", ArmorType.HELMET, ArmorMaterials.IRON);
    public static final DeferredItem<Item> HOLY_KNIGHT_LEGGINGS =
            registerArmor("holy_knight_leggings", "holy_knight", ArmorType.LEGGINGS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> KEEPERS_BOW =
            register("keepers_bow", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> KNIGHT_BOOTS =
            registerArmor("knight_boots", "knight", ArmorType.BOOTS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> KNIGHT_CHESTPLATE =
            registerArmor("knight_chestplate", "knight", ArmorType.CHESTPLATE, ArmorMaterials.IRON);
    public static final DeferredItem<Item> KNIGHT_HELMET =
            registerArmor("knight_helmet", "knight", ArmorType.HELMET, ArmorMaterials.IRON);
    public static final DeferredItem<Item> KNIGHT_LEGGINGS =
            registerArmor("knight_leggings", "knight", ArmorType.LEGGINGS, ArmorMaterials.IRON);
    public static final DeferredItem<Item> KOPISH_DAGGER =
            register("kopish_dagger", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> LUCKY_RABBIT_EAR =
            register("lucky_rabbit_ear", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MOON_SHIELD =
            register("moon_shield", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MOON_SHURIKEN =
            register("moon_shuriken", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MOONSILVER_BOOTS =
            registerArmor("moonsilver_boots", "moonsilver", ArmorType.BOOTS, ArmorMaterials.DIAMOND);
    public static final DeferredItem<Item> MOONSILVER_BOW =
            registerModelled("moonsilver_bow", Category.EQUIPMENT, "moonsilver_bow_3d", "misc.idle");
    public static final DeferredItem<Item> MOONSILVER_CHESTPLATE =
            registerArmor("moonsilver_chestplate", "moonsilver", ArmorType.CHESTPLATE, ArmorMaterials.DIAMOND);
    public static final DeferredItem<Item> MOONSILVER_DAGGER =
            register("moonsilver_dagger", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MOONSILVER_GREAT_SWORD =
            registerModelled("moonsilver_great_sword", Category.EQUIPMENT, "moonsilver_great_sword", null);
    public static final DeferredItem<Item> MOONSILVER_HELMET =
            registerArmor("moonsilver_helmet", "moonsilver", ArmorType.HELMET, ArmorMaterials.DIAMOND);
    public static final DeferredItem<Item> MOONSILVER_LEGGINGS =
            registerArmor("moonsilver_leggings", "moonsilver", ArmorType.LEGGINGS, ArmorMaterials.DIAMOND);
    public static final DeferredItem<Item> MOONSILVER_SCYTHE =
            registerModelled("moonsilver_scythe", Category.EQUIPMENT, "moonsilver_scythe_3d", null);
    public static final DeferredItem<Item> MOONSILVER_SWORD =
            registerModelled("moonsilver_sword", Category.EQUIPMENT, "moonsilver_sword_3d", null);
    public static final DeferredItem<Item> MOONSTONE_SHIELD =
            register("moonstone_shield", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MOONSTONE_SWORD =
            register("moonstone_sword", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> MYSTERIUM_WOOL_BOOTS =
            registerArmor("mysterium_wool_boots", "mysterium_wool", ArmorType.BOOTS, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> MYSTERIUM_WOOL_CHESTPLATE =
            registerArmor("mysterium_wool_chestplate", "mysterium_wool", ArmorType.CHESTPLATE, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> MYSTERIUM_WOOL_HELMET =
            registerArmor("mysterium_wool_helmet", "mysterium_wool", ArmorType.HELMET, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> MYSTERIUM_WOOL_LEGGINGS =
            registerArmor("mysterium_wool_leggings", "mysterium_wool", ArmorType.LEGGINGS, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> NACREOUS_HALBERD =
            register("nacreous_halberd", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> QUEENS_PICKAXE = registerQueensPickaxe();
    @Deprecated
    public static final DeferredItem<Item> QUEENS_CHIPPER = QUEENS_PICKAXE;
    public static final DeferredItem<Item> RED_BOOK_RING =
            register("red_book_ring", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_BLAZE =
            register("runestone_blaze", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_DARKNESS =
            register("runestone_darkness", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_ICE =
            register("runestone_ice", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_LIFE =
            register("runestone_life", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_LIGHT =
            register("runestone_light", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_MOON =
            register("runestone_moon", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_MOUNTAIN =
            register("runestone_mountain", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_NATURE =
            register("runestone_nature", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_STORM =
            register("runestone_storm", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_THUNDER =
            register("runestone_thunder", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> RUNESTONE_WATER =
            register("runestone_water", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> SILENT_WOOD_BOW =
            registerSilentWoodBow();
    public static final DeferredItem<Item> SPECTRAL_BOOTS =
            registerArmor("spectral_boots", "spectral", ArmorType.BOOTS, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> SPECTRAL_CHESTPLATE =
            registerArmor("spectral_chestplate", "spectral", ArmorType.CHESTPLATE, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> SPECTRAL_HELMET =
            registerArmor("spectral_helmet", "spectral", ArmorType.HELMET, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> SPECTRAL_LEGGINGS =
            registerArmor("spectral_leggings", "spectral", ArmorType.LEGGINGS, ArmorMaterials.LEATHER);
    public static final DeferredItem<Item> SPIKED_CHESTPLATE =
            registerArmor("spiked_chestplate", "spiked_chestplate", ArmorType.CHESTPLATE, ArmorMaterials.IRON);
    public static final DeferredItem<Item> STEEL_DAGGER =
            register("steel_dagger", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> STICKY_SPIKER =
            register("sticky_spiker", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> TSLAT_SWORD =
            register("tslat_sword", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> UMBRA_KUNAI =
            register("umbra_kunai", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> UMBRA_SHIELD =
            register("umbra_shield", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> UMBRA_SWORD =
            register("umbra_sword", Category.EQUIPMENT, 1);
    public static final DeferredItem<Item> TROPHY_KEEPER =
            register("trophy_keeper", Category.ACCESSORIES, 1);
    public static final DeferredItem<Item> TROPHY_MOON_QUEEN =
            register("trophy_moon_queen", Category.ACCESSORIES, 1);
    public static final DeferredItem<Item> TROPHY_SPIDER_MOTHER =
            register("trophy_spider_mother", Category.ACCESSORIES, 1);

    private ModLegacyItems() {
    }

    private static DeferredItem<Item> register(String id, Category category, int maxStack) {
        DeferredItem<Item> item = ModItems.ITEMS.registerItem(
                id, properties -> new Item(maxStack == 64 ? properties : properties.stacksTo(maxStack)));
        ITEMS_BY_CATEGORY.get(category).add(item);
        return item;
    }

    private static DeferredItem<Item> registerArmor(String id, String model, ArmorType type, ArmorMaterial material) {
        DeferredItem<Item> item = ModItems.ITEMS.registerItem(id, properties -> model.equals("holy_knight")
                ? new HolyKnightArmorItem(properties, type)
                : new LegacyArmorItem(properties, model, type, material));
        ITEMS_BY_CATEGORY.get(Category.EQUIPMENT).add(item);
        return item;
    }

    private static DeferredItem<Item> registerLavenderSeeds() {
        DeferredItem<Item> seeds = ModItems.ITEMS.registerItem("lavender_seeds", properties -> new BlockItem(
                ModStructureBlocks.LAVENDER_CROP.get(), properties.useItemDescriptionPrefix()));
        ITEMS_BY_CATEGORY.get(Category.INGREDIENTS).add(seeds);
        return seeds;
    }

    private static DeferredItem<Item> registerSilentWoodBow() {
        DeferredItem<Item> bow = ModItems.ITEMS.registerItem("silent_wood_bow",
                properties -> new BowItem(properties.durability(384).enchantable(1)));
        ITEMS_BY_CATEGORY.get(Category.EQUIPMENT).add(bow);
        return bow;
    }

    private static DeferredItem<Item> registerFishBucket(String id, Supplier<? extends EntityType<? extends Mob>> type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerItem(id, properties -> new MobBucketItem(
                type.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH,
                properties.stacksTo(1).craftRemainder(Items.BUCKET)));
        ITEMS_BY_CATEGORY.get(Category.TOOLS).add(item);
        return item;
    }

    private static DeferredItem<Item> registerQueensPickaxe() {
        DeferredItem<Item> item = ModItems.ITEMS.registerItem(
                "queens_chipper", QueensPickaxeItem::new);
        ITEMS_BY_CATEGORY.get(Category.TOOLS).add(item);
        return item;
    }

    private static DeferredItem<Item> registerWorldScroll() {
        DeferredItem<Item> item = ModItems.ITEMS.<Item>registerItem(
                "world_scroll", WorldScrollItem::new);
        ITEMS_BY_CATEGORY.get(Category.ACCESSORIES).add(item);
        return item;
    }

    private static DeferredItem<Item> registerModelled(
            String id, Category category, String textureName, String idleAnimationName) {
        DeferredItem<Item> item = ModItems.ITEMS.<Item>registerItem(
                id,
                properties -> new ModelledItem(
                        properties.stacksTo(1), id, textureName, idleAnimationName));
        ITEMS_BY_CATEGORY.get(category).add(item);
        return item;
    }

    public static void forEachEquipment(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.EQUIPMENT).forEach(item -> consumer.accept(item.get()));
    }

    public static void forEachAccessories(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.ACCESSORIES).forEach(item -> consumer.accept(item.get()));
    }

    public static void forEachFood(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.FOOD).forEach(item -> consumer.accept(item.get()));
    }

    public static void forEachFunctional(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.FUNCTIONAL).forEach(item -> consumer.accept(item.get()));
    }

    public static void forEachIngredients(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.INGREDIENTS).forEach(item -> consumer.accept(item.get()));
    }

    public static void forEachTools(Consumer<Item> consumer) {
        ITEMS_BY_CATEGORY.get(Category.TOOLS).forEach(item -> consumer.accept(item.get()));
    }

    public static void bootstrap() {
    }

    private enum Category {
        ACCESSORIES,
        EQUIPMENT,
        FOOD,
        FUNCTIONAL,
        INGREDIENTS,
        TOOLS
    }
}
