package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public class LegacyArmorItem extends Item {
    public static final ResourceKey<EquipmentAsset> ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, TheAurorian2.id("legacy_armor"));
    private final String modelName;
    private final ArmorType armorType;

    public LegacyArmorItem(Properties properties, String modelName, ArmorType type, ArmorMaterial material) {
        super(properties.humanoidArmor(material, type).component(DataComponents.EQUIPPABLE,
                Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(ASSET).build()));
        this.modelName = modelName;
        this.armorType = type;
    }

    public String modelName() {
        return this.modelName;
    }

    public ArmorType armorType() {
        return this.armorType;
    }
}
