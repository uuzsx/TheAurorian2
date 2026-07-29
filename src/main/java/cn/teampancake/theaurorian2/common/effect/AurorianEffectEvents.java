package cn.teampancake.theaurorian2.common.effect;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModDamageTypes;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import cn.teampancake.theaurorian2.common.world.PressureImmunityData;
import cn.teampancake.theaurorian2.common.world.CorruptionLedgerData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AurorianEffectEvents {

    private static final String FORBIDDEN_MARKER = "theaurorian2_forbidden_curse";
    private static final String ARMOR_DEBT = "theaurorian2_corruption_armor_debt";
    private static final String ARMOR_DEBT_OWNER = "theaurorian2_corruption_armor_owner";
    private static final String ARMOR_DEBT_SESSION = "theaurorian2_corruption_armor_session";
    private static final net.minecraft.resources.Identifier CRYSTALLIZATION_MODIFIER =
            TheAurorian2.id("crystallization_health_loss");
    private static final int VULNERABILITY_INVULNERABILITY_TICKS = 15;

    private static final Map<UUID, FoodSnapshot> CORRUPTION_FOOD_SNAPSHOTS = new java.util.HashMap<>();
    private static final Map<UUID, FoodSnapshot> INCANTATION_FOOD_SNAPSHOTS = new java.util.HashMap<>();
    private static final Map<UUID, ArmorSnapshot> ARMOR_SNAPSHOTS = new java.util.HashMap<>();
    private static final Map<UUID, Float> CRYSTALLIZATION_HEALTH_SNAPSHOTS = new java.util.HashMap<>();
    private static final Map<LivingEntity, MotionState> LACERATION_MOTION = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> SETTLING_CORRUPTION = ThreadLocal.withInitial(() -> false);

    private AurorianEffectEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        Holder<MobEffect> incoming = event.getEffectInstance().getEffect();

        if (entity.hasEffect(ModMobEffects.HOLINESS)
                && incoming.value().getCategory() == MobEffectCategory.HARMFUL) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (entity.hasEffect(ModMobEffects.INCANTATION)
                && incoming.value().isBeneficial()
                && !incoming.is(ModMobEffects.HOLINESS.getKey())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (incoming.is(ModMobEffects.PRESSURE.getKey())
                && entity.level() instanceof ServerLevel level
                && entity instanceof Player
                && PressureImmunityData.isImmune(level)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        Holder<MobEffect> added = event.getEffectInstance().getEffect();

        if (added.is(ModMobEffects.HOLINESS.getKey())) {
            AurorianEffects.cleanseWithHoliness(entity);
        } else if (added.is(ModMobEffects.INCANTATION.getKey())) {
            clearBeneficialEffects(entity);
        } else if (added.is(ModMobEffects.TREMOR.getKey())
                && event.getOldEffectInstance() == null
                && entity instanceof Player player) {
            dropMainHand(player);
        } else if (added.is(ModMobEffects.CORRUPTION.getKey())
                && event.getOldEffectInstance() == null) {
            long sessionId = entity.getRandom().nextLong();
            entity.setData(
                    ModAttachments.CORRUPTION_DATA,
                    CorruptionData.begin(sessionId == 0L ? 1L : sessionId));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        Holder<MobEffect> effect = event.getEffect();
        if (EffectRemovalContext.current() == EffectRemovalContext.Reason.MILK
                && AurorianEffects.isMilkResistantCurse(effect)) {
            event.setCanceled(true);
            return;
        }

        if (effect.is(ModMobEffects.CORRUPTION.getKey())) {
            if (EffectRemovalContext.current() == EffectRemovalContext.Reason.HOLINESS) {
                discardCorruption(event.getEntity(), true);
            } else {
                settleCorruption(event.getEntity());
            }
        } else if (effect.is(ModMobEffects.CRYSTALLIZATION.getKey())) {
            clearCrystallization(event.getEntity());
        } else if (effect.is(ModMobEffects.FORBIDDEN_CURSE.getKey())
                && event.getEntity() instanceof Player player) {
            setForbiddenForInventory(player, false);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        Holder<MobEffect> effect = event.getEffectInstance().getEffect();
        if (effect.is(ModMobEffects.CORRUPTION.getKey())) {
            settleCorruption(event.getEntity());
        } else if (effect.is(ModMobEffects.CRYSTALLIZATION.getKey())) {
            clearCrystallization(event.getEntity());
        } else if (effect.is(ModMobEffects.FORBIDDEN_CURSE.getKey())
                && event.getEntity() instanceof Player player) {
            setForbiddenForInventory(player, false);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModMobEffects.INCANTATION)
                || entity.hasEffect(ModMobEffects.PRESSURE) && NaturalHealingContext.isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModMobEffects.VULNERABILITY)) {
            entity.invulnerableTime = Math.min(entity.invulnerableTime, VULNERABILITY_INVULNERABILITY_TICKS);
            event.setInvulnerabilityTicks(VULNERABILITY_INVULNERABILITY_TICKS);
        }

        if (!SETTLING_CORRUPTION.get() && entity.hasEffect(ModMobEffects.CRYSTALLIZATION)) {
            CRYSTALLIZATION_HEALTH_SNAPSHOTS.put(entity.getUUID(), entity.getHealth());
        }

        if (!SETTLING_CORRUPTION.get()
                && entity.hasEffect(ModMobEffects.CORRUPTION)
                && shouldDelay(event)) {
            ARMOR_SNAPSHOTS.put(entity.getUUID(), ArmorSnapshot.capture(entity));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (SETTLING_CORRUPTION.get()
                || !entity.hasEffect(ModMobEffects.CORRUPTION)
                || isImmediateDamage(event.getSource())) {
            return;
        }

        CorruptionData data = entity.getData(ModAttachments.CORRUPTION_DATA);
        ArmorSnapshot snapshot = ARMOR_SNAPSHOTS.remove(entity.getUUID());
        if (snapshot != null) {
            snapshot.restoreAndRecord(entity, data.sessionId());
        }

        float reducedDamage = Math.max(0.0F, event.getNewDamage());
        float absorbed = Math.min(entity.getAbsorptionAmount(), reducedDamage);
        if (absorbed > 0.0F) {
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - absorbed);
        }
        float healthDebt = reducedDamage - absorbed;
        if (healthDebt > 0.0F) {
            entity.setData(ModAttachments.CORRUPTION_DATA, data.addHealth(healthDebt));
        }
        event.setNewDamage(0.0F);
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Float healthBefore = CRYSTALLIZATION_HEALTH_SNAPSHOTS.remove(entity.getUUID());
        boolean lostHealth = healthBefore != null
                ? entity.getHealth() < healthBefore
                : event.getHealthDamage() > 0.0F;
        if (!SETTLING_CORRUPTION.get()
                && lostHealth
                && entity.hasEffect(ModMobEffects.CRYSTALLIZATION)
                && entity.getHealth() > 2.0F
                && entity.getRandom().nextBoolean()) {
            increaseCrystallization(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.hasEffect(ModMobEffects.CORRUPTION)) {
            CORRUPTION_FOOD_SNAPSHOTS.put(player.getUUID(), FoodSnapshot.capture(player.getFoodData()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        FoodSnapshot snapshot = CORRUPTION_FOOD_SNAPSHOTS.remove(player.getUUID());
        if (snapshot != null && player.hasEffect(ModMobEffects.CORRUPTION)) {
            snapshot.deferLoss(player);
        }

        boolean forbidden = player.hasEffect(ModMobEffects.FORBIDDEN_CURSE);
        setForbiddenForInventory(player, forbidden);
        if (!player.hasEffect(ModMobEffects.CORRUPTION) && player instanceof ServerPlayer serverPlayer) {
            settleArmorDebts(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.level().isClientSide()) {
            return;
        }

        if (entity.hasEffect(ModMobEffects.VULNERABILITY)) {
            entity.invulnerableTime = Math.min(entity.invulnerableTime, VULNERABILITY_INVULNERABILITY_TICKS);
        }
        reconcileCrystallization(entity);
        tickLaceration(entity);
        CRYSTALLIZATION_HEALTH_SNAPSHOTS.remove(entity.getUUID());
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(ModMobEffects.INCANTATION)) {
            INCANTATION_FOOD_SNAPSHOTS.put(player.getUUID(), FoodSnapshot.capture(player.getFoodData()));
        }
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        FoodSnapshot snapshot = INCANTATION_FOOD_SNAPSHOTS.remove(player.getUUID());
        if (snapshot != null && player.hasEffect(ModMobEffects.INCANTATION)) {
            snapshot.restore(player.getFoodData());
        }
    }

    @SubscribeEvent
    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (event.getStack() instanceof ItemStack stack && hasBooleanMarker(stack, FORBIDDEN_MARKER)) {
            ItemEnchantments.Mutable enchantments = event.getEnchantments();
            enchantments.removeIf(enchantment -> true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        discardCorruption(entity, true);
        clearCrystallization(entity);
        LACERATION_MOTION.remove(entity);
        ARMOR_SNAPSHOTS.remove(entity.getUUID());
        CORRUPTION_FOOD_SNAPSHOTS.remove(entity.getUUID());
        INCANTATION_FOOD_SNAPSHOTS.remove(entity.getUUID());
        CRYSTALLIZATION_HEALTH_SNAPSHOTS.remove(entity.getUUID());
        if (entity instanceof Player player) {
            setForbiddenForInventory(player, false);
        }
    }

    private static void clearBeneficialEffects(LivingEntity entity) {
        ArrayList<Holder<MobEffect>> beneficial = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value().isBeneficial()
                    && !instance.getEffect().is(ModMobEffects.HOLINESS.getKey())) {
                beneficial.add(instance.getEffect());
            }
        }
        beneficial.forEach(entity::removeEffect);
    }

    private static void dropMainHand(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        ItemEntity dropped = player.drop(held.copyAndClear(), false);
        if (dropped != null) {
            dropped.setPickUpDelay(3 * 20);
        }
    }

    private static boolean shouldDelay(LivingIncomingDamageEvent event) {
        return !isImmediateDamage(event.getSource());
    }

    private static boolean isImmediateDamage(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL);
    }

    private static void settleCorruption(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        CorruptionData data = entity.getExistingData(ModAttachments.CORRUPTION_DATA)
                .orElse(CorruptionData.EMPTY);
        entity.removeData(ModAttachments.CORRUPTION_DATA);
        if (entity instanceof ServerPlayer player) {
            FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(Math.max(0, foodData.getFoodLevel() - data.foodDebt()));
            foodData.setSaturation(Math.max(0.0F, foodData.getSaturationLevel() - data.saturationDebt()));
            settleArmorDebts(player);
        }

        if (data.healthDebt() <= 0.0F || !entity.isAlive()) {
            return;
        }

        float absorption = entity.getAbsorptionAmount();
        entity.setAbsorptionAmount(0.0F);
        SETTLING_CORRUPTION.set(true);
        try {
            entity.hurtServer(level, ModDamageTypes.randomCorruptionSettlementSource(level), data.healthDebt());
        } finally {
            SETTLING_CORRUPTION.set(false);
            if (entity.isAlive()) {
                entity.setAbsorptionAmount(absorption);
            }
        }
    }

    private static void discardCorruption(LivingEntity entity, boolean forgiveRemoteArmorDebt) {
        CorruptionData data = entity.getExistingData(ModAttachments.CORRUPTION_DATA)
                .orElse(CorruptionData.EMPTY);
        if (forgiveRemoteArmorDebt
                && data.sessionId() != 0L
                && entity.level() instanceof ServerLevel level) {
            CorruptionLedgerData.forgive(level, entity.getUUID(), data.sessionId());
        }
        entity.removeData(ModAttachments.CORRUPTION_DATA);
        if (entity instanceof Player player) {
            forEachPlayerStack(player, AurorianEffectEvents::clearArmorDebt);
        }
    }

    private static void increaseCrystallization(LivingEntity entity) {
        float currentLoss = entity.getData(ModAttachments.CRYSTALLIZATION_LOSS);
        float available = entity.getMaxHealth() - 2.0F;
        float increase = Math.min(1.0F, available);
        if (increase <= 0.0F) {
            return;
        }
        float newLoss = currentLoss + increase;
        entity.setData(ModAttachments.CRYSTALLIZATION_LOSS, newLoss);
        applyCrystallizationModifier(entity, newLoss);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static void reconcileCrystallization(LivingEntity entity) {
        float loss = entity.getExistingData(ModAttachments.CRYSTALLIZATION_LOSS).orElse(0.0F);
        if (entity.hasEffect(ModMobEffects.CRYSTALLIZATION)) {
            if (loss > 0.0F) {
                applyCrystallizationModifier(entity, loss);
            }
        } else if (loss > 0.0F) {
            clearCrystallization(entity);
        }
    }

    private static void applyCrystallizationModifier(LivingEntity entity, float loss) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        AttributeModifier current = maxHealth.getModifier(CRYSTALLIZATION_MODIFIER);
        if (current != null && Math.abs(current.amount() + loss) < 1.0E-6D) {
            return;
        }
        maxHealth.removeModifier(CRYSTALLIZATION_MODIFIER);
        maxHealth.addTransientModifier(new AttributeModifier(
                CRYSTALLIZATION_MODIFIER,
                -loss,
                AttributeModifier.Operation.ADD_VALUE));
    }

    private static void clearCrystallization(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(CRYSTALLIZATION_MODIFIER);
        }
        entity.removeData(ModAttachments.CRYSTALLIZATION_LOSS);
    }

    private static void tickLaceration(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModMobEffects.LACERATION);
        if (effect == null || !(entity instanceof ServerPlayer player)) {
            LACERATION_MOTION.remove(entity);
            return;
        }

        Vec3 current = entity.position();
        MotionState state = LACERATION_MOTION.computeIfAbsent(entity, ignored -> new MotionState(current));
        double dx = current.x - state.lastPosition.x;
        double dz = current.z - state.lastPosition.z;
        double moved = Math.sqrt(dx * dx + dz * dz);
        state.lastPosition = current;

        Input input = player.getLastClientInput();
        boolean hasMovementInput = input.forward() || input.backward() || input.left() || input.right();
        boolean activeWalking = hasMovementInput
                && player.onGround()
                && !player.isPassenger()
                && !player.isSwimming()
                && !player.isFallFlying()
                && !player.getAbilities().flying;
        if (!activeWalking || moved <= 0.0D || moved > 1.5D) {
            return;
        }

        state.distance += moved;
        while (state.distance >= 1.0D && entity.isAlive()) {
            state.distance -= 1.0D;
            entity.hurtServer(
                    player.level(),
                    ModDamageTypes.source(player.level(), ModDamageTypes.LACERATION),
                    effect.getAmplifier() + 1.0F);
        }
    }

    private static void setForbiddenForInventory(Player player, boolean active) {
        forEachPlayerStack(player, stack -> setBooleanMarker(
                stack,
                FORBIDDEN_MARKER,
                active && stack.isEnchanted()));
    }

    private static void settleArmorDebts(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            ArmorDebt debt = takeArmorDebt(stack);
            if (shouldApplyArmorDebt(player.level(), debt) && stack.isDamageableItem()) {
                stack.hurtAndBreak(debt.amount(), player, slot);
            }
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            ArmorDebt debt = takeArmorDebt(stack);
            if (shouldApplyArmorDebt(player.level(), debt) && stack.isDamageableItem()) {
                stack.hurtAndBreak(debt.amount(), player.level(), player, item -> { });
            }
        }
    }

    private static void forEachPlayerStack(Player player, java.util.function.Consumer<ItemStack> consumer) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            consumer.accept(stack);
        }
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            consumer.accept(player.getItemBySlot(slot));
        }
    }

    private static void addArmorDebt(ItemStack stack, UUID owner, long sessionId, int amount) {
        if (stack.isEmpty() || amount <= 0) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(ARMOR_DEBT, tag.getIntOr(ARMOR_DEBT, 0) + amount);
            tag.putString(ARMOR_DEBT_OWNER, owner.toString());
            tag.putLong(ARMOR_DEBT_SESSION, sessionId);
        });
    }

    private static ArmorDebt takeArmorDebt(ItemStack stack) {
        if (stack.isEmpty()) {
            return ArmorDebt.EMPTY;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int debt = tag.getIntOr(ARMOR_DEBT, 0);
        String owner = tag.getStringOr(ARMOR_DEBT_OWNER, "");
        long sessionId = tag.getLongOr(ARMOR_DEBT_SESSION, 0L);
        if (debt > 0) {
            clearArmorDebt(stack);
        }
        return new ArmorDebt(debt, owner, sessionId);
    }

    private static boolean shouldApplyArmorDebt(ServerLevel level, ArmorDebt debt) {
        return debt.amount() > 0 && !CorruptionLedgerData.isForgiven(level, debt.owner(), debt.sessionId());
    }

    private static void clearArmorDebt(ItemStack stack) {
        if (!stack.isEmpty()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.remove(ARMOR_DEBT);
                tag.remove(ARMOR_DEBT_OWNER);
                tag.remove(ARMOR_DEBT_SESSION);
            });
        }
    }

    private static boolean hasBooleanMarker(ItemStack stack, String key) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getBooleanOr(key, false);
    }

    private static void setBooleanMarker(ItemStack stack, String key, boolean enabled) {
        if (stack.isEmpty() || hasBooleanMarker(stack, key) == enabled) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (enabled) {
                tag.putBoolean(key, true);
            } else {
                tag.remove(key);
            }
        });
    }

    private record FoodSnapshot(int food, float saturation) {

        private static FoodSnapshot capture(FoodData data) {
            return new FoodSnapshot(data.getFoodLevel(), data.getSaturationLevel());
        }

        private void restore(FoodData data) {
            data.setFoodLevel(this.food);
            data.setSaturation(this.saturation);
        }

        private void deferLoss(Player player) {
            FoodData data = player.getFoodData();
            int foodLoss = Math.max(0, this.food - data.getFoodLevel());
            float saturationLoss = Math.max(0.0F, this.saturation - data.getSaturationLevel());
            if (foodLoss > 0 || saturationLoss > 0.0F) {
                CorruptionData corruption = player.getData(ModAttachments.CORRUPTION_DATA);
                player.setData(ModAttachments.CORRUPTION_DATA, corruption.addFood(foodLoss, saturationLoss));
                this.restore(data);
            }
        }
    }

    private static final class MotionState {
        private Vec3 lastPosition;
        private double distance;

        private MotionState(Vec3 lastPosition) {
            this.lastPosition = lastPosition;
        }
    }

    private record ArmorDebt(int amount, String owner, long sessionId) {
        private static final ArmorDebt EMPTY = new ArmorDebt(0, "", 0L);
    }

    private record ArmorSnapshot(EnumMap<EquipmentSlot, ItemStack> armor) {

        private static ArmorSnapshot capture(LivingEntity entity) {
            EnumMap<EquipmentSlot, ItemStack> armor = new EnumMap<>(EquipmentSlot.class);
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    armor.put(slot, entity.getItemBySlot(slot).copy());
                }
            }
            return new ArmorSnapshot(armor);
        }

        private void restoreAndRecord(LivingEntity entity, long sessionId) {
            this.armor.forEach((slot, before) -> {
                if (before.isEmpty() || !before.isDamageableItem()) {
                    return;
                }
                ItemStack current = entity.getItemBySlot(slot);
                if (current.isEmpty()) {
                    int debt = Math.max(1, before.getMaxDamage() - before.getDamageValue());
                    ItemStack restored = before.copy();
                    addArmorDebt(restored, entity.getUUID(), sessionId, debt);
                    entity.setItemSlot(slot, restored);
                } else if (ItemStack.isSameItem(before, current)) {
                    int debt = current.getDamageValue() - before.getDamageValue();
                    if (debt > 0) {
                        current.setDamageValue(before.getDamageValue());
                        addArmorDebt(current, entity.getUUID(), sessionId, debt);
                    }
                }
            });
        }
    }
}
