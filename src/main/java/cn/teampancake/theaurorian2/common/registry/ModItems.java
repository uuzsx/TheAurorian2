package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.ArcherArmorItem;
import cn.teampancake.theaurorian2.common.item.TrainingDummyItem;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheAurorian2.MOD_ID);

    public static final DeferredItem<TrainingDummyItem> TRAINING_DUMMY = ITEMS.registerItem(
            "training_dummy", properties -> new TrainingDummyItem(properties.stacksTo(16)));

    public static final DeferredItem<ArcherArmorItem> DIAMOND_ARCHER_HELMET =
            archerArmor("diamond_archer_helmet", ArmorType.HELMET, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DIAMOND_ARCHER_CHESTPLATE =
            archerArmor("diamond_archer_chestplate", ArmorType.CHESTPLATE, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DIAMOND_ARCHER_LEGGINGS =
            archerArmor("diamond_archer_leggings", ArmorType.LEGGINGS, "diamond_archer_armor");
    public static final DeferredItem<ArcherArmorItem> DIAMOND_ARCHER_BOOTS =
            archerArmor("diamond_archer_boots", ArmorType.BOOTS, "diamond_archer_armor");

    public static final DeferredItem<ArcherArmorItem> GOLDEN_ARCHER_HELMET =
            archerArmor("golden_archer_helmet", ArmorType.HELMET, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> GOLDEN_ARCHER_CHESTPLATE =
            archerArmor("golden_archer_chestplate", ArmorType.CHESTPLATE, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> GOLDEN_ARCHER_LEGGINGS =
            archerArmor("golden_archer_leggings", ArmorType.LEGGINGS, "golden_archer_armor");
    public static final DeferredItem<ArcherArmorItem> GOLDEN_ARCHER_BOOTS =
            archerArmor("golden_archer_boots", ArmorType.BOOTS, "golden_archer_armor");

    public static final DeferredItem<ArcherArmorItem> IRON_ARCHER_HELMET =
            archerArmor("iron_archer_helmet", ArmorType.HELMET, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> IRON_ARCHER_CHESTPLATE =
            archerArmor("iron_archer_chestplate", ArmorType.CHESTPLATE, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> IRON_ARCHER_LEGGINGS =
            archerArmor("iron_archer_leggings", ArmorType.LEGGINGS, "iron_archer_armor");
    public static final DeferredItem<ArcherArmorItem> IRON_ARCHER_BOOTS =
            archerArmor("iron_archer_boots", ArmorType.BOOTS, "iron_archer_armor");

    public static final DeferredItem<ArcherArmorItem> NETHERITE_ARCHER_HELMET =
            archerArmor("netherite_archer_helmet", ArmorType.HELMET, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> NETHERITE_ARCHER_CHESTPLATE =
            archerArmor("netherite_archer_chestplate", ArmorType.CHESTPLATE, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> NETHERITE_ARCHER_LEGGINGS =
            archerArmor("netherite_archer_leggings", ArmorType.LEGGINGS, "netherite_archer_armor");
    public static final DeferredItem<ArcherArmorItem> NETHERITE_ARCHER_BOOTS =
            archerArmor("netherite_archer_boots", ArmorType.BOOTS, "netherite_archer_armor");

    private ModItems() {
    }

    private static DeferredItem<ArcherArmorItem> archerArmor(String name, ArmorType type, String textureName) {
        return ITEMS.registerItem(name, properties -> new ArcherArmorItem(
                properties.humanoidArmor(ArmorMaterials.LEATHER, type), textureName));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
