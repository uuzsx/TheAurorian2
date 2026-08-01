package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.ArcherArmorItem;
import cn.teampancake.theaurorian2.common.item.KnightArmorItem;
import cn.teampancake.theaurorian2.common.item.KnightGreatswordItem;
import cn.teampancake.theaurorian2.common.item.KnightSpearItem;
import cn.teampancake.theaurorian2.common.item.TrainingDummyItem;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheAurorian2.MOD_ID);

    public static final DeferredItem<TrainingDummyItem> TRAINING_DUMMY = ITEMS.registerItem(
            "training_dummy", properties -> new TrainingDummyItem(properties.stacksTo(16)));

    public static final DeferredItem<Item> SILENT_WOOD_SWORD = ITEMS.registerItem(
            "silent_wood_sword",
            properties -> new Item(properties.sword(ModToolMaterials.WOOD, 3.0F, -2.2F)));
    public static final DeferredItem<Item> SILENT_WOOD_PICKAXE = ITEMS.registerItem(
            "silent_wood_pickaxe",
            properties -> new Item(properties.pickaxe(ModToolMaterials.WOOD, 1.0F, -2.8F)));
    public static final DeferredItem<ShovelItem> SILENT_WOOD_SHOVEL = ITEMS.registerItem(
            "silent_wood_shovel",
            properties -> new ShovelItem(ModToolMaterials.WOOD, 1.5F, -3.0F, properties));
    public static final DeferredItem<AxeItem> SILENT_WOOD_AXE = ITEMS.registerItem(
            "silent_wood_axe",
            properties -> new AxeItem(ModToolMaterials.WOOD, 6.0F, -3.2F, properties));
    public static final DeferredItem<HoeItem> SILENT_WOOD_HOE = ITEMS.registerItem(
            "silent_wood_hoe",
            properties -> new HoeItem(ModToolMaterials.WOOD, 0.0F, -3.0F, properties));

    public static final DeferredItem<Item> AURORIAN_STONE_SWORD = ITEMS.registerItem(
            "aurorian_stone_sword",
            properties -> new Item(properties.sword(ModToolMaterials.STONE, 3.5F, -2.2F)));
    public static final DeferredItem<Item> AURORIAN_STONE_PICKAXE = ITEMS.registerItem(
            "aurorian_stone_pickaxe",
            properties -> new Item(properties.pickaxe(ModToolMaterials.STONE, 1.0F, -2.8F)));
    public static final DeferredItem<ShovelItem> AURORIAN_STONE_SHOVEL = ITEMS.registerItem(
            "aurorian_stone_shovel",
            properties -> new ShovelItem(ModToolMaterials.STONE, 1.5F, -3.0F, properties));
    public static final DeferredItem<AxeItem> AURORIAN_STONE_AXE = ITEMS.registerItem(
            "aurorian_stone_axe",
            properties -> new AxeItem(ModToolMaterials.STONE, 7.0F, -3.2F, properties));
    public static final DeferredItem<HoeItem> AURORIAN_STONE_HOE = ITEMS.registerItem(
            "aurorian_stone_hoe",
            properties -> new HoeItem(ModToolMaterials.STONE, -1.0F, -2.0F, properties));

    public static final DeferredItem<Item> TEA_CUP = ITEMS.registerSimpleItem("tea_cup");
    public static final DeferredItem<Item> LAVENDER_TEA = tea(
            "lavender_tea", MobEffects.RESISTANCE, 300, 0);
    public static final DeferredItem<Item> SILK_BERRY_TEA = tea(
            "silk_berry_tea", MobEffects.REGENERATION, 100, 0);
    public static final DeferredItem<Item> LAVENDER_SEEDY_TEA = tea(
            "lavender_seedy_tea", MobEffects.SPEED, 200, 0);
    public static final DeferredItem<Item> PETUNIA_TEA = tea(
            "petunia_tea", MobEffects.STRENGTH, 300, 0);
    public static final DeferredItem<Item> BEPSI = tea(
            "bepsi", MobEffects.SPEED, 3600, 0);
    public static final DeferredItem<Item> AURORIAN_SPECIALTY_DRINK = drink(
            "aurorian_specialty_drink", MobEffects.SPEED, 600, 0);
    public static final DeferredItem<Item> MOONLIT_BLUEBERRY_SPECIALTY_DRINK = drink(
            "moonlit_blueberry_specialty_drink", MobEffects.SPEED, 600, 1);
    public static final DeferredItem<Item> SLEEPING_BLACK_TEA = tea("sleeping_black_tea");
    public static final DeferredItem<Item> WEEPING_WILLOW_SAP = ITEMS.registerItem(
            "weeping_willow_sap",
            properties -> new Item(properties.stacksTo(16).food(
                    foodProperties(0, 0.0F, true),
                    Consumables.defaultDrink()
                            .onConsume(new RemoveStatusEffectsConsumeEffect(MobEffects.POISON))
                            .build())));

    public static final DeferredItem<Item> AURORIAN_BEEF = vanillaFood("aurorian_beef", Foods.BEEF);
    public static final DeferredItem<Item> AURORIAN_PORK = vanillaFood("aurorian_pork", Foods.PORKCHOP);
    public static final DeferredItem<Item> AURORIAN_MUTTON = vanillaFood("aurorian_mutton", Foods.MUTTON);
    public static final DeferredItem<Item> AURORIAN_RABBIT = vanillaFood("aurorian_rabbit", Foods.RABBIT);
    public static final DeferredItem<Item> COOKED_AURORIAN_BEEF = vanillaFood(
            "cooked_aurorian_beef", Foods.COOKED_BEEF);
    public static final DeferredItem<Item> COOKED_AURORIAN_PORK = vanillaFood(
            "cooked_aurorian_pork", Foods.COOKED_PORKCHOP);
    public static final DeferredItem<Item> COOKED_AURORIAN_MUTTON = vanillaFood(
            "cooked_aurorian_mutton", Foods.COOKED_MUTTON);
    public static final DeferredItem<Item> COOKED_AURORIAN_RABBIT = vanillaFood(
            "cooked_aurorian_rabbit", Foods.COOKED_RABBIT);
    public static final DeferredItem<Item> SILK_BERRY_JAM = food("silk_berry_jam", 2, 0.5F);
    public static final DeferredItem<Item> SILK_BERRY_JAM_SANDWICH = food(
            "silk_berry_jam_sandwich", 6, 0.9F);
    public static final DeferredItem<Item> AURORIAN_SLIMEBALL = food("aurorian_slimeball", 1, 0.2F);
    public static final DeferredItem<Item> SILK_SHROOM_STEW = bowlFood("silk_shroom_stew", 6, 1.0F);
    public static final DeferredItem<Item> LAVENDER_BREAD = food("lavender_bread", 4, 0.4F);
    public static final DeferredItem<Item> SOULLESS_FLESH = food("soulless_flesh", 2, 0.1F);
    public static final DeferredItem<Item> MOON_FISH = food("moon_fish", 2, 0.4F);
    public static final DeferredItem<Item> AURORIAN_WINGED_FISH = food(
            "aurorian_winged_fish", 2, 0.4F);
    public static final DeferredItem<Item> COOKED_MOON_FISH = food("cooked_moon_fish", 5, 6.0F);
    public static final DeferredItem<Item> COOKED_AURORIAN_WINGED_FISH = food(
            "cooked_aurorian_winged_fish", 5, 6.0F);
    public static final DeferredItem<Item> SILK_BERRY = food("silk_berry", 1, 0.1F);
    public static final DeferredItem<Item> AURORIAN_BERRY = vanillaFood("aurorian_berry", Foods.APPLE);
    public static final DeferredItem<Item> CANDY = food("candy", 4, 0.2F);
    public static final DeferredItem<Item> CANDY_CANE = effectFood(
            "candy_cane", 4, 0.4F, MobEffects.LUCK, 300, 0);
    public static final DeferredItem<Item> GINGERBREAD_MAN = food("gingerbread_man", 6, 0.4F);
    public static final DeferredItem<Item> AURORIAN_BACON = effectFood(
            "aurorian_bacon", 2, 0.8F, MobEffects.REGENERATION, 60, 0);
    public static final DeferredItem<Item> STRANGE_MEAT = alwaysEdibleFood("strange_meat", 8, 0.9F);
    public static final DeferredItem<Item> LAVENDER_SALAD = bowlFood("lavender_salad", 4, 5.0F);
    public static final DeferredItem<Item> FAKE_ALGAL_PIT_FISH = food(
            "fake_algal_pit_fish", 4, 5.0F);
    public static final DeferredItem<Item> SASHIMI = effectFood(
            "sashimi", 5, 0.1F, MobEffects.LUCK, 400, 0);
    public static final DeferredItem<Item> SILENT_WOOD_FRUIT = food(
            "silent_wood_fruit", 3, 2.5F);
    public static final DeferredItem<Item> GOLDEN_SILENT_WOOD_FRUIT = food(
            "golden_silent_wood_fruit", 4, 5.0F);
    public static final DeferredItem<Item> KEBAB_WITH_MUSHROOM = effectFood(
            "kebab_with_mushroom", 12, 15.0F, MobEffects.RESISTANCE, 1200, 0);
    public static final DeferredItem<Item> AURORIAN_WINTER_ROOT = food(
            "aurorian_winter_root", 1, 0.8F);
    public static final DeferredItem<Item> ROASTED_AURORIAN_WINTER_ROOT = food(
            "roasted_aurorian_winter_root", 4, 6.0F);
    public static final DeferredItem<Item> DARK_STONE_SHRIMP = food("dark_stone_shrimp", 3, 0.8F);
    public static final DeferredItem<Item> WHITE_CHOCOLATE = alwaysEdibleFood(
            "white_chocolate", 0, 0.0F);

    public static final DeferredItem<ArcherArmorItem> STARLIGHT_RANGER_HELMET =
            archerArmor("starlight_ranger_helmet", ArmorType.HELMET, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> STARLIGHT_RANGER_CHESTPLATE =
            archerArmor("starlight_ranger_chestplate", ArmorType.CHESTPLATE, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> STARLIGHT_RANGER_LEGGINGS =
            archerArmor("starlight_ranger_leggings", ArmorType.LEGGINGS, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> STARLIGHT_RANGER_BOOTS =
            archerArmor("starlight_ranger_boots", ArmorType.BOOTS, "diamond_archer_armor");

    public static final DeferredItem<ArcherArmorItem> DAWNLIGHT_RANGER_HELMET =
            archerArmor("dawnlight_ranger_helmet", ArmorType.HELMET, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DAWNLIGHT_RANGER_CHESTPLATE =
            archerArmor("dawnlight_ranger_chestplate", ArmorType.CHESTPLATE, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DAWNLIGHT_RANGER_LEGGINGS =
            archerArmor("dawnlight_ranger_leggings", ArmorType.LEGGINGS, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DAWNLIGHT_RANGER_BOOTS =
            archerArmor("dawnlight_ranger_boots", ArmorType.BOOTS, "golden_archer_armor");

    public static final DeferredItem<ArcherArmorItem> FORESTSHADE_RANGER_HELMET =
            archerArmor("forestshade_ranger_helmet", ArmorType.HELMET, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> FORESTSHADE_RANGER_CHESTPLATE =
            archerArmor("forestshade_ranger_chestplate", ArmorType.CHESTPLATE, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> FORESTSHADE_RANGER_LEGGINGS =
            archerArmor("forestshade_ranger_leggings", ArmorType.LEGGINGS, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> FORESTSHADE_RANGER_BOOTS =
            archerArmor("forestshade_ranger_boots", ArmorType.BOOTS, "iron_archer_armor");

    public static final DeferredItem<ArcherArmorItem> DUSKFLAME_RANGER_HELMET =
            archerArmor("duskflame_ranger_helmet", ArmorType.HELMET, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DUSKFLAME_RANGER_CHESTPLATE =
            archerArmor("duskflame_ranger_chestplate", ArmorType.CHESTPLATE, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DUSKFLAME_RANGER_LEGGINGS =
            archerArmor("duskflame_ranger_leggings", ArmorType.LEGGINGS, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DUSKFLAME_RANGER_BOOTS =
            archerArmor("duskflame_ranger_boots", ArmorType.BOOTS, "netherite_archer_armor");

    public static final DeferredItem<KnightArmorItem> STARFORGED_KNIGHT_HELMET =
            knightArmor("starforged_knight_helmet", ArmorType.HELMET, "starforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> STARFORGED_KNIGHT_CHESTPLATE =
            knightArmor("starforged_knight_chestplate", ArmorType.CHESTPLATE, "starforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> STARFORGED_KNIGHT_LEGGINGS =
            knightArmor("starforged_knight_leggings", ArmorType.LEGGINGS, "starforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> STARFORGED_KNIGHT_BOOTS =
            knightArmor("starforged_knight_boots", ArmorType.BOOTS, "starforged_knight_armor");

    public static final DeferredItem<KnightArmorItem> DAWNFORGED_KNIGHT_HELMET =
            knightArmor("dawnforged_knight_helmet", ArmorType.HELMET, "dawnforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> DAWNFORGED_KNIGHT_CHESTPLATE =
            knightArmor("dawnforged_knight_chestplate", ArmorType.CHESTPLATE, "dawnforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> DAWNFORGED_KNIGHT_LEGGINGS =
            knightArmor("dawnforged_knight_leggings", ArmorType.LEGGINGS, "dawnforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> DAWNFORGED_KNIGHT_BOOTS =
            knightArmor("dawnforged_knight_boots", ArmorType.BOOTS, "dawnforged_knight_armor");

    public static final DeferredItem<KnightArmorItem> MOONFORGED_KNIGHT_HELMET =
            knightArmor("moonforged_knight_helmet", ArmorType.HELMET, "moonforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> MOONFORGED_KNIGHT_CHESTPLATE =
            knightArmor("moonforged_knight_chestplate", ArmorType.CHESTPLATE, "moonforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> MOONFORGED_KNIGHT_LEGGINGS =
            knightArmor("moonforged_knight_leggings", ArmorType.LEGGINGS, "moonforged_knight_armor");
    public static final DeferredItem<KnightArmorItem> MOONFORGED_KNIGHT_BOOTS =
            knightArmor("moonforged_knight_boots", ArmorType.BOOTS, "moonforged_knight_armor");

    public static final DeferredItem<KnightGreatswordItem> STARFORGED_KNIGHT_GREATSWORD =
            knightGreatsword("starforged_knight_greatsword", "starforged_knight_greatsword");
    public static final DeferredItem<KnightGreatswordItem> DAWNFORGED_KNIGHT_GREATSWORD =
            knightGreatsword("dawnforged_knight_greatsword", "dawnforged_knight_greatsword");
    public static final DeferredItem<KnightGreatswordItem> MOONFORGED_KNIGHT_GREATSWORD =
            knightGreatsword("moonforged_knight_greatsword", "moonforged_knight_greatsword");

    public static final DeferredItem<KnightSpearItem> STARFORGED_KNIGHT_SPEAR =
            knightSpear("starforged_knight_spear", "starforged_knight_spear");
    public static final DeferredItem<KnightSpearItem> DAWNFORGED_KNIGHT_SPEAR =
            knightSpear("dawnforged_knight_spear", "dawnforged_knight_spear");
    public static final DeferredItem<KnightSpearItem> MOONFORGED_KNIGHT_SPEAR =
            knightSpear("moonforged_knight_spear", "moonforged_knight_spear");

    private ModItems() {
    }

    private static DeferredItem<ArcherArmorItem> archerArmor(String name, ArmorType type, String textureName) {
        return ITEMS.registerItem(name, properties -> new ArcherArmorItem(
                properties.humanoidArmor(ArmorMaterials.LEATHER, type), textureName, type));
    }

    private static DeferredItem<KnightArmorItem> knightArmor(String name, ArmorType type, String textureName) {
        return ITEMS.registerItem(name, properties -> new KnightArmorItem(
                properties.humanoidArmor(ArmorMaterials.IRON, type), textureName, type));
    }

    private static DeferredItem<KnightGreatswordItem> knightGreatsword(String name, String textureName) {
        return ITEMS.registerItem(name, properties -> new KnightGreatswordItem(
                properties.sword(ModToolMaterials.KNIGHT_GREATSWORD, 11.0F, -3.1F), textureName));
    }

    private static DeferredItem<KnightSpearItem> knightSpear(String name, String textureName) {
        return ITEMS.registerItem(name, properties -> new KnightSpearItem(properties.spear(
                ModToolMaterials.KNIGHT_SPEAR,
                1.0F / 1.4F,
                0.95F,
                0.6F,
                2.5F,
                11.0F,
                6.75F,
                5.1F,
                11.25F,
                4.6F), textureName));
    }

    private static DeferredItem<Item> vanillaFood(String name, FoodProperties food) {
        return ITEMS.registerItem(name, properties -> new Item(properties.food(food)));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturationModifier) {
        return ITEMS.registerItem(name, properties -> new Item(
                properties.food(foodProperties(nutrition, saturationModifier, false))));
    }

    private static DeferredItem<Item> alwaysEdibleFood(
            String name, int nutrition, float saturationModifier) {
        return ITEMS.registerItem(name, properties -> new Item(
                properties.food(foodProperties(nutrition, saturationModifier, true))));
    }

    private static DeferredItem<Item> bowlFood(String name, int nutrition, float saturationModifier) {
        return ITEMS.registerItem(name, properties -> new Item(properties
                .stacksTo(1)
                .food(foodProperties(nutrition, saturationModifier, false))
                .usingConvertsTo(Items.BOWL)));
    }

    private static DeferredItem<Item> effectFood(
            String name,
            int nutrition,
            float saturationModifier,
            Holder<MobEffect> effect,
            int duration,
            int amplifier) {
        return ITEMS.registerItem(name, properties -> new Item(properties.food(
                foodProperties(nutrition, saturationModifier, false),
                effectConsumable(Consumables.defaultFood(), effect, duration, amplifier))));
    }

    private static DeferredItem<Item> tea(
            String name, Holder<MobEffect> effect, int duration, int amplifier) {
        return ITEMS.registerItem(name, properties -> new Item(properties
                .stacksTo(1)
                .food(
                        foodProperties(0, 0.0F, true),
                        effectConsumable(Consumables.defaultDrink(), effect, duration, amplifier))
                .usingConvertsTo(TEA_CUP.get())));
    }

    private static DeferredItem<Item> tea(String name) {
        return ITEMS.registerItem(name, properties -> new Item(properties
                .stacksTo(1)
                .food(foodProperties(0, 0.0F, true), Consumables.DEFAULT_DRINK)
                .usingConvertsTo(TEA_CUP.get())));
    }

    private static DeferredItem<Item> drink(
            String name, Holder<MobEffect> effect, int duration, int amplifier) {
        return ITEMS.registerItem(name, properties -> new Item(properties
                .stacksTo(16)
                .food(
                        foodProperties(0, 0.0F, true),
                        effectConsumable(Consumables.defaultDrink(), effect, duration, amplifier))));
    }

    private static FoodProperties foodProperties(
            int nutrition, float saturationModifier, boolean alwaysEdible) {
        FoodProperties.Builder builder = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturationModifier);
        if (alwaysEdible) {
            builder.alwaysEdible();
        }
        return builder.build();
    }

    private static Consumable effectConsumable(
            Consumable.Builder builder,
            Holder<MobEffect> effect,
            int duration,
            int amplifier) {
        return builder
                .onConsume(new ApplyStatusEffectsConsumeEffect(
                        new MobEffectInstance(effect, duration, amplifier)))
                .build();
    }

    public static void register(IEventBus modEventBus) {
        ModLegacyItems.bootstrap();
        ITEMS.register(modEventBus);
    }
}
