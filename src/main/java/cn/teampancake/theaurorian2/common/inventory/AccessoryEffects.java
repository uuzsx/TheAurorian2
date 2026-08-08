package cn.teampancake.theaurorian2.common.inventory;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAccessoryItems;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AccessoryEffects {

    public static final int ARCANE_DAGGER_BASE_PERCENT = 10;
    public static final int ARCANE_DAGGER_PERCENT_PER_LEVEL = 10;
    public static final int ARCANE_DAGGER_MAX_LEVEL = 2;
    public static final int MOON_QUEEN_TROPHY_MAX_LEVEL = 4;
    public static final int MOON_QUEEN_TROPHY_BASE_PERCENT = 5;
    public static final int MOON_QUEEN_TROPHY_PERCENT_PER_LEVEL = 2;

    private static final Identifier ARCANE_DAGGER_ATTACK_SPEED =
            TheAurorian2.id("arcane_dagger_attack_speed");
    private static final Identifier MOON_QUEEN_TROPHY_ATTACK_DAMAGE =
            TheAurorian2.id("moon_queen_trophy_attack_damage");
    private static final Identifier MOON_QUEEN_TROPHY_MOVEMENT_SPEED =
            TheAurorian2.id("moon_queen_trophy_movement_speed");
    private static final Identifier MOON_QUEEN_TROPHY_MAX_HEALTH =
            TheAurorian2.id("moon_queen_trophy_max_health");

    private AccessoryEffects() {
    }

    public static void reconcile(Player player, AccessoryInventory inventory) {
        if (player.level().isClientSide()) {
            return;
        }

        int[] enhancementLevels = AccessoryEnhancements.calculate(inventory);
        double attackSpeedBonus = 0.0D;
        int moonQueenLevel = -1;
        for (int slot = 0; slot < AccessoryInventory.SLOT_COUNT; slot++) {
            if (inventory.getItem(slot).is(ModAccessoryItems.ARCANE_DAGGER.get())) {
                attackSpeedBonus += attackSpeedPercent(enhancementLevels[slot]) / 100.0D;
            }
            if (moonQueenLevel < 0
                    && inventory.getItem(slot).is(ModLegacyItems.TROPHY_MOON_QUEEN.get())) {
                moonQueenLevel = effectiveMoonQueenLevel(enhancementLevels[slot]);
            }
        }
        applyAttackSpeedBonus(player, attackSpeedBonus);
        applyMoonQueenTrophy(player, moonQueenLevel);
    }

    public static int attackSpeedPercent(int enhancementLevel) {
        int effectiveLevel = effectiveArcaneDaggerLevel(enhancementLevel);
        return ARCANE_DAGGER_BASE_PERCENT + effectiveLevel * ARCANE_DAGGER_PERCENT_PER_LEVEL;
    }

    public static int effectiveArcaneDaggerLevel(int enhancementLevel) {
        return Math.min(ARCANE_DAGGER_MAX_LEVEL, Math.max(0, enhancementLevel));
    }

    public static int effectiveMoonQueenLevel(int enhancementLevel) {
        return Math.min(MOON_QUEEN_TROPHY_MAX_LEVEL, Math.max(0, enhancementLevel));
    }

    public static int moonQueenPercent(int enhancementLevel) {
        return MOON_QUEEN_TROPHY_BASE_PERCENT
                + effectiveMoonQueenLevel(enhancementLevel) * MOON_QUEEN_TROPHY_PERCENT_PER_LEVEL;
    }

    public static double moonQueenMaxHealthBonus(int enhancementLevel) {
        return 1.0D + effectiveMoonQueenLevel(enhancementLevel) * 0.5D;
    }

    private static void applyAttackSpeedBonus(Player player, double bonus) {
        applyModifier(
                player.getAttribute(Attributes.ATTACK_SPEED),
                ARCANE_DAGGER_ATTACK_SPEED,
                bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void applyMoonQueenTrophy(Player player, int enhancementLevel) {
        double percentBonus = enhancementLevel < 0 ? 0.0D : moonQueenPercent(enhancementLevel) / 100.0D;
        double healthBonus = enhancementLevel < 0 ? 0.0D : moonQueenMaxHealthBonus(enhancementLevel);
        applyModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                MOON_QUEEN_TROPHY_ATTACK_DAMAGE,
                percentBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyModifier(
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                MOON_QUEEN_TROPHY_MOVEMENT_SPEED,
                percentBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyModifier(
                player.getAttribute(Attributes.MAX_HEALTH),
                MOON_QUEEN_TROPHY_MAX_HEALTH,
                healthBonus,
                AttributeModifier.Operation.ADD_VALUE);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(
            AttributeInstance attribute,
            Identifier id,
            double amount,
            AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }
        AttributeModifier current = attribute.getModifier(id);
        if (amount <= 0.0D) {
            if (current != null) {
                attribute.removeModifier(id);
            }
            return;
        }
        if (current != null
                && current.operation() == operation
                && Math.abs(current.amount() - amount) < 1.0E-9D) {
            return;
        }
        attribute.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static double moonQueenFraction(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return 0.0D;
        }
        AttributeModifier modifier = attackDamage.getModifier(MOON_QUEEN_TROPHY_ATTACK_DAMAGE);
        return modifier == null ? 0.0D : Math.max(0.0D, modifier.amount());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.isCriticalHit()) {
            return;
        }
        double chance = moonQueenFraction(player);
        if (chance > 0.0D && player.getRandom().nextDouble() < chance) {
            event.setCriticalHit(true);
            event.setDamageMultiplier(1.5F);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        double trophyReduction = moonQueenFraction(player);
        if (trophyReduction <= 0.0D) {
            return;
        }

        double resistanceReduction = 0.0D;
        MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
        if (resistance != null && !event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            resistanceReduction = Math.min(1.0D, (resistance.getAmplifier() + 1) * 0.2D);
        }
        if (trophyReduction <= resistanceReduction) {
            return;
        }

        double remainingAfterResistance = 1.0D - resistanceReduction;
        double replacementMultiplier = (1.0D - trophyReduction) / remainingAfterResistance;
        event.setNewDamage((float) (event.getNewDamage() * replacementMultiplier));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            reconcile(player, player.getData(ModAttachments.ACCESSORY_INVENTORY));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            reconcile(player, player.getData(ModAttachments.ACCESSORY_INVENTORY));
        }
    }
}
