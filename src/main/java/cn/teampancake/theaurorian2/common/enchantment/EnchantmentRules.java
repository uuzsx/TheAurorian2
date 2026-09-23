package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantmentRules {
    public static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final TagKey<EntityType<?>> HUNTING_TARGETS = TagKey.create(Registries.ENTITY_TYPE, TheAurorian2.id("hunting_targets"));
    public static final TagKey<DamageType> LIGHTNING = net.minecraft.tags.DamageTypeTags.IS_LIGHTNING;
    public static final ResourceKey<DamageType> LIGHTNING_STRIKE = ResourceKey.create(Registries.DAMAGE_TYPE, TheAurorian2.id("enchantment_lightning"));
    public static final ResourceKey<DamageType> REFLECTION = ResourceKey.create(Registries.DAMAGE_TYPE, TheAurorian2.id("enchantment_reflection"));

    private EnchantmentRules() {}

    public static int level(LivingEntity entity, EquipmentSlot slot, ResourceKey<Enchantment> key) {
        return EnchantmentAccess.itemLevel(entity.getItemBySlot(slot), entity.registryAccess(), key);
    }

    public static int armorSum(LivingEntity entity, ResourceKey<Enchantment> key) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR) total += Math.clamp(level(entity, slot, key), 0, 4);
        return total;
    }

    public static boolean hasArmor(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR) if (!entity.getItemBySlot(slot).isEmpty()) return true;
        return false;
    }

    public static float armorChance(int sum) { return Math.clamp(sum, 0, 16) * 0.01F; }
    public static float lightningReduction(int sum) { return Math.min(0.8F, Math.max(0, sum) * 0.1F); }
    public static int throwRange(int level) { return 4 + 4 * Math.clamp(level, 1, 3); }
}
