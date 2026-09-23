package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.minecraft.world.phys.EntityHitResult;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class CombatEnchantmentEvents {
    private CombatEnchantmentEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void projectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity victim)
                || !(victim.level() instanceof ServerLevel)) return;
        float chance = EnchantmentRules.armorChance(EnchantmentRules.armorSum(victim, ModEnchantments.VIRTUALIZATION));
        if (chance <= 0) return;
        EnchantmentState state = victim.getData(ModAttachments.ENCHANTMENT_STATE);
        // A projectile crossing the hitbox must not get a second roll in LivingIncomingDamageEvent.
        state.virtualProjectile = event.getProjectile().getId();
        state.virtualProjectileTick = victim.level().getGameTime();
        if (victim.getRandom().nextFloat() < chance) {
            event.setCanceled(true);
            dodgeFeedback(victim);
        }
    }

    private static void dodgeFeedback(LivingEntity victim) {
        ServerLevel level = (ServerLevel) victim.level();
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5,
                victim.getZ(), 10, 0.25, 0.4, 0.25, 0.025);
        level.playSound(null, victim.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.7F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void critical(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || (EnchantmentRules.level(player, EquipmentSlot.MAINHAND, ModEnchantments.SUNDER_ARMOR_SLASH) == 0
                && EnchantmentRules.level(player, EquipmentSlot.MAINHAND, ModEnchantments.LIGHTNING_DAMAGE) == 0)) return;
        EnchantmentState state = player.getData(ModAttachments.ENCHANTMENT_STATE);
        state.criticalTarget = event.getTarget().getId();
        state.criticalTick = player.level().getGameTime();
        state.criticalMultiplier = event.isCriticalHit() ? event.getDamageMultiplier() : 1;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void incoming(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || event.getAmount() <= 0) return;
        DamageSource source = event.getSource();
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (victim.hasData(ModAttachments.ENCHANTMENT_STATE)
                    && victim.getData(ModAttachments.ENCHANTMENT_STATE).guardianTicks > 0) {
                event.setCanceled(true);
                return;
            }
            boolean attack = source.getDirectEntity() instanceof LivingEntity || source.getDirectEntity() instanceof Projectile;
            boolean alreadyRolled = source.getDirectEntity() instanceof Projectile projectile && victim.hasData(ModAttachments.ENCHANTMENT_STATE)
                    && victim.getData(ModAttachments.ENCHANTMENT_STATE).virtualProjectile == projectile.getId()
                    && victim.getData(ModAttachments.ENCHANTMENT_STATE).virtualProjectileTick == level.getGameTime();
            if (attack && !alreadyRolled && !source.is(EnchantmentRules.REFLECTION)
                    && victim.getRandom().nextFloat() < EnchantmentRules.armorChance(EnchantmentRules.armorSum(victim, ModEnchantments.VIRTUALIZATION))) {
                event.setCanceled(true);
                dodgeFeedback(victim);
                return;
            }
        }
        if (source.is(DamageTypeTags.IS_FIRE) && EnchantmentRules.level(victim, EquipmentSlot.CHEST, ModEnchantments.MOLTEN_CORE) > 0)
            victim.getData(ModAttachments.ENCHANTMENT_STATE).heatTicks = 80;

        float amount = event.getAmount();
        if (isMelee(source) && source.getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            float strength = attacker instanceof Player player ? player.getAttackStrengthScale(0.5F) : 1;
            if (EnchantmentAccess.has(weapon, level.registryAccess(), ModEnchantments.LEGENDARY_HERO))
                amount += heroBonus(attacker, victim) * strength;
            int savage = EnchantmentAccess.itemLevel(weapon, level.registryAccess(), ModEnchantments.SAVAGE);
            if (savage > 0 && victim.is(EnchantmentRules.HUNTING_TARGETS)) amount += 2 * savage * strength;
            int sunder = EnchantmentAccess.itemLevel(weapon, level.registryAccess(), ModEnchantments.SUNDER_ARMOR_SLASH);
            if (sunder > 0 && EnchantmentRules.hasArmor(victim)) {
                float physical = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (attacker instanceof Player) physical *= 0.2F + strength * strength * 0.8F;
                physical += weapon.getItem().getAttackDamageBonus(victim, physical, source);
                if (attacker.hasData(ModAttachments.ENCHANTMENT_STATE)) {
                    EnchantmentState state = attacker.getData(ModAttachments.ENCHANTMENT_STATE);
                    if (state.criticalTick == level.getGameTime() && state.criticalTarget == victim.getId())
                        physical *= state.criticalMultiplier;
                }
                amount += Math.min(event.getAmount(), physical) * (Math.min(5, sunder) * 0.1F);
            }
        }
        if (source.is(EnchantmentRules.LIGHTNING))
            amount *= 1 - EnchantmentRules.lightningReduction(EnchantmentRules.armorSum(victim, ModEnchantments.LIGHTNING_RESISTANCE));
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforeHealthDamage(LivingDamageEvent.Pre event) {
        if (!event.getEntity().level().isClientSide() && !event.getSource().is(EnchantmentRules.REFLECTION)
                && EnchantmentRules.armorSum(event.getEntity(), ModEnchantments.REFLECT_AURA) > 0)
            event.getEntity().getData(ModAttachments.ENCHANTMENT_STATE).reflectionHealth = event.getEntity().getHealth();
    }

    public static boolean isMelee(DamageSource source) {
        return source.getDirectEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity()
                && !source.is(EnchantmentRules.LIGHTNING_STRIKE) && !source.is(EnchantmentRules.REFLECTION)
                && (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO));
    }

    public static float heroBonus(LivingEntity attacker, LivingEntity victim) {
        int count = 0;
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(20),
                entity -> entity != attacker && entity != victim && entity.isAlive() && !entity.isSpectator()
                        && entity.distanceToSqr(attacker) <= 400 && (entity instanceof Villager || entity instanceof Player))) {
            if (nearby instanceof Player && !attacker.isAlliedTo(nearby)
                    && (attacker.getLastHurtByMob() == nearby || nearby.getLastHurtByMob() == attacker)) continue;
            if (++count == 10) break;
        }
        return count * 0.5F;
    }

    @SubscribeEvent
    public static void afterDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || event.getHealthDamage() <= 0) return;
        DamageSource source = event.getSource();
        if (!source.is(EnchantmentRules.REFLECTION) && source.getEntity() instanceof LivingEntity attacker
                && attacker != victim && attacker.isAlive()
                && victim.getRandom().nextFloat() < EnchantmentRules.armorChance(EnchantmentRules.armorSum(victim, ModEnchantments.REFLECT_AURA))) {
            float lost = Math.min(victim.getData(ModAttachments.ENCHANTMENT_STATE).reflectionHealth, event.getHealthDamage());
            attacker.hurtServer(level, new DamageSource(level.registryAccess().getOrThrow(EnchantmentRules.REFLECTION), victim), lost);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, attacker.getX(), attacker.getY() + 1, attacker.getZ(), 8, 0.2, 0.3, 0.2, 0);
        }
        if (isMelee(source) && source.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
            ItemStack weapon = attacker.getMainHandItem();
            int lightning = EnchantmentAccess.itemLevel(weapon, level.registryAccess(), ModEnchantments.LIGHTNING_DAMAGE);
            if (lightning == 0 || attacker instanceof Player player && player.getAttackStrengthScale(0.5F) <= 0.9F) return;
            EnchantmentState state = attacker.getData(ModAttachments.ENCHANTMENT_STATE);
            // Sweeping hits belong to the same swing; only its primary target advances the combo.
            if (attacker instanceof Player && (state.criticalTick != level.getGameTime() || state.criticalTarget != victim.getId())) return;
            ItemStack identity = weapon.copy();
            if (identity.isDamageableItem()) identity.setDamageValue(0);
            if (level.getGameTime() - state.lightningLastHit > 100 || !ItemStack.isSameItemSameComponents(identity, state.lightningWeapon)) state.lightningHits = 0;
            state.lightningWeapon = identity;
            state.lightningLastHit = level.getGameTime();
            if (++state.lightningHits >= 3) {
                state.lightningHits = 0;
                if (victim.isAlive()) victim.hurtServer(level, new DamageSource(level.registryAccess().getOrThrow(EnchantmentRules.LIGHTNING_STRIKE), attacker), lightning * 2);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5,
                        victim.getZ(), 18, 0.3, 0.5, 0.3, 0.1);
                level.playSound(null, victim.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.75F, 1.6F);
            }
        }
    }

    @SubscribeEvent
    public static void huntingLoot(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !victim.is(EnchantmentRules.HUNTING_TARGETS)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker) || !isMelee(event.getSource())) return;
        int rank = EnchantmentRules.level(attacker, EquipmentSlot.MAINHAND, ModEnchantments.SAVAGE);
        if (rank <= 0 || victim.getRandom().nextFloat() >= Math.min(3, rank) * 0.05F) return;
        // Roll only the entity's base loot table, never equipment/inventory drops or the death event itself.
        victim.getLootTable().ifPresent(table -> victim.dropFromLootTable(level, event.getSource(), event.isRecentlyHit(), table,
                stack -> { if (!stack.isEmpty()) event.getDrops().add(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), stack)); }));
    }
}
