package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class ArmorEnchantmentEvents {
    private static final Identifier HEAT_MODIFIER = TheAurorian2.id("enchantment_molten_core");
    private ArmorEnchantmentEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void guardian(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || EnchantmentRules.level(entity, EquipmentSlot.CHEST, ModEnchantments.GUARDIAN) == 0) return;
        entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        entity.setHealth(entity.getMaxHealth());
        entity.clearFire();
        entity.getData(ModAttachments.ENCHANTMENT_STATE).guardianTicks = 40;
        event.setCanceled(true);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, entity.getX(), entity.getY() + 1, entity.getZ(), 35, 0.4, 0.6, 0.4, 0.08);
        level.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
    }

    @SubscribeEvent
    public static void experience(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.experienceLevel >= 30 || event.getAmount() <= 0) return;
        int rank = Math.min(4, EnchantmentRules.level(player, EquipmentSlot.HEAD, ModEnchantments.CLEAR_MIND));
        if (rank == 0) return;
        EnchantmentState state = player.getData(ModAttachments.ENCHANTMENT_STATE);
        long tenths = (long) event.getAmount() * rank + state.experienceTenths;
        state.experienceTenths = (int) (tenths % 10);
        event.setAmount((int) Math.min(Integer.MAX_VALUE, event.getAmount() + tenths / 10));
    }

    @SubscribeEvent
    public static void wounded(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() > 0 && !event.getEntity().level().isClientSide()) startSpring(event.getEntity());
    }

    public static void startSpring(LivingEntity entity) {
        if (!entity.isAlive() || entity.getHealth() >= entity.getMaxHealth() * 0.2F
                || EnchantmentRules.level(entity, EquipmentSlot.CHEST, ModEnchantments.SPRING_OF_LIFE) == 0) return;
        EnchantmentState state = entity.getData(ModAttachments.ENCHANTMENT_STATE);
        if (state.springPulses > 0) return;
        EnchantmentHelper.updateEnchantments(entity.getItemBySlot(EquipmentSlot.CHEST), enchantments ->
                enchantments.removeIf(holder -> holder.is(ModEnchantments.SPRING_OF_LIFE)));
        state.springPulses = 5;
        state.springDelay = 0;
        healPulse(entity, state);
    }

    private static void healPulse(LivingEntity entity, EnchantmentState state) {
        entity.heal(entity.getMaxHealth() * 0.2F);
        state.springPulses--;
        state.springDelay = 20;
        if (entity.level() instanceof ServerLevel level)
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 1, entity.getZ(), 7, 0.3, 0.5, 0.3, 0);
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide() || !entity.isAlive()) return;
        EnchantmentState state = entity.hasData(ModAttachments.ENCHANTMENT_STATE) ? entity.getData(ModAttachments.ENCHANTMENT_STATE) : null;
        if (state != null) {
            if (state.guardianTicks > 0) state.guardianTicks--;
            if (state.heatTicks > 0) state.heatTicks--;
            if (state.springPulses > 0 && --state.springDelay <= 0) healPulse(entity, state);
        }
        // Fixed, local equipment checks; no radius scans or packets for inactive enchantments.
        if (entity.tickCount % 5 != 0) return;
        startSpring(entity);
        int molten = Math.min(3, EnchantmentRules.level(entity, EquipmentSlot.CHEST, ModEnchantments.MOLTEN_CORE));
        boolean hot = molten > 0 && (entity.level().dimension().equals(Level.NETHER) || entity.isOnFire() || entity.isInLava()
                || entity.level().getBlockState(entity.blockPosition()).is(BlockTags.FIRE));
        if (hot) {
            state = entity.getData(ModAttachments.ENCHANTMENT_STATE);
            state.heatTicks = 80;
        }
        int strength = molten > 0 && state != null && state.heatTicks > 0 ? molten : 0;
        var attack = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;
        var previous = attack.getModifier(HEAT_MODIFIER);
        if (strength == 0) {
            if (previous != null) attack.removeModifier(HEAT_MODIFIER);
        } else if (previous == null || previous.amount() != strength) {
            attack.removeModifier(HEAT_MODIFIER);
            attack.addTransientModifier(new AttributeModifier(HEAT_MODIFIER, strength, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** Called just before a worn stack is destroyed by durability, including non-player armor. */
    public static void armorBreaking(ItemStack stack, LivingEntity owner) {
        if (owner == null || !(owner.level() instanceof ServerLevel level)) return;
        boolean worn = false;
        for (EquipmentSlot slot : EnchantmentRules.ARMOR) if (owner.getItemBySlot(slot) == stack) worn = true;
        if (!worn) return;
        int rank = Math.min(5, EnchantmentAccess.itemLevel(stack, level.registryAccess(), ModEnchantments.AMNESIA_CURSE));
        if (rank == 0) return;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(15),
                entity -> entity != owner && entity.isAlive() && !entity.isSpectator() && entity.distanceToSqr(owner) <= 225
                        && !owner.isAlliedTo(entity)
                        && !(entity instanceof TamableAnimal pet && pet.isTame())
                        && (entity instanceof Enemy || entity instanceof Mob mob && mob.getTarget() == owner
                            || entity instanceof Player && entity.getLastHurtMob() == owner))) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, rank * 120), owner);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, rank * 120), owner);
        }
        level.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY() + 1, owner.getZ(), 25, 0.6, 0.5, 0.6, 0.3);
        level.playSound(null, owner.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 0.75F);
    }
}
