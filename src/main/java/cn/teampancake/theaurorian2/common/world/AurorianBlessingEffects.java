package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AurorianBlessingEffects {

    public static final double EXPLORATION_MOVEMENT_SPEED = 0.05D;
    public static final double EXPLORATION_JUMP_STRENGTH = 0.10D;
    public static final double EXPLORATION_FALL_DAMAGE_MULTIPLIER = -0.25D;
    public static final double COMBAT_ATTACK_DAMAGE = 1.0D;
    public static final double COMBAT_CRITICAL_CHANCE = 0.05D;
    public static final double PROTECTION_MAX_HEALTH = 2.0D;
    public static final double PROTECTION_DAMAGE_MULTIPLIER = 0.95D;
    public static final float MINING_SPEED_MULTIPLIER = 1.10F;
    public static final double EXTRA_ORE_CHANCE = 0.10D;

    private static final Identifier EXPLORATION_MOVEMENT =
            TheAurorian2.id("blessing_exploration_movement_speed");
    private static final Identifier EXPLORATION_JUMP =
            TheAurorian2.id("blessing_exploration_jump_strength");
    private static final Identifier EXPLORATION_FALL_DAMAGE =
            TheAurorian2.id("blessing_exploration_fall_damage");
    private static final Identifier COMBAT_ATTACK =
            TheAurorian2.id("blessing_combat_attack_damage");
    private static final Identifier PROTECTION_HEALTH =
            TheAurorian2.id("blessing_protection_max_health");

    private AurorianBlessingEffects() {
    }

    public static boolean preventsDurabilityLoss(ItemStack stack, LivingEntity owner) {
        if (!(owner instanceof Player player) || owner.level().isClientSide()) {
            return false;
        }
        if (MoonShieldSystem.isPurified(player)) {
            return false;
        }
        if (AurorianBlessingCycle.isActive(owner.level(), AurorianBlessingCycle.Blessing.COMBAT)
                && isWeapon(stack)) {
            return true;
        }
        return AurorianBlessingCycle.isActive(owner.level(), AurorianBlessingCycle.Blessing.PROTECTION)
                && isHumanoidArmor(stack);
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.has(DataComponents.WEAPON)
                || stack.is(ItemTags.WEAPON_ENCHANTABLE)
                || stack.is(ItemTags.BOW_ENCHANTABLE)
                || stack.is(ItemTags.CROSSBOW_ENCHANTABLE)
                || stack.is(ItemTags.TRIDENT_ENCHANTABLE)
                || stack.is(ItemTags.MACE_ENCHANTABLE);
    }

    private static boolean isHumanoidArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        reconcilePlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        reconcilePlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        reconcilePlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        reconcilePlayer(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (!event.isCriticalHit()
                && !player.level().isClientSide()
                && !MoonShieldSystem.isPurified(player)
                && AurorianBlessingCycle.isActive(player.level(), AurorianBlessingCycle.Blessing.COMBAT)
                && player.getRandom().nextDouble() < COMBAT_CRITICAL_CHANCE) {
            event.setCriticalHit(true);
            event.setDamageMultiplier(1.5F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && !MoonShieldSystem.isPurified(player)
                && AurorianBlessingCycle.isActive(player.level(), AurorianBlessingCycle.Blessing.PROTECTION)) {
            event.setNewDamage(event.getNewDamage() * (float) PROTECTION_DAMAGE_MULTIPLIER);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!MoonShieldSystem.isPurified(player)
                && AurorianBlessingCycle.isActive(player.level(), AurorianBlessingCycle.Blessing.MINING)) {
            event.setNewSpeed(event.getNewSpeed() * MINING_SPEED_MULTIPLIER);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) {
            return;
        }
        Level level = event.getLevel();
        BlockState state = event.getState();
        if (!MoonShieldSystem.isPurified(player)
                && AurorianBlessingCycle.isActive(level, AurorianBlessingCycle.Blessing.MINING)
                && state.is(Tags.Blocks.ORES)
                && player.getRandom().nextDouble() < EXTRA_ORE_CHANCE) {
            duplicateDrops(event);
        } else if (!MoonShieldSystem.isPurified(player)
                && AurorianBlessingCycle.isActive(level, AurorianBlessingCycle.Blessing.GROWTH)
                && state.is(BlockTags.CROPS)
                && isMatureCrop(state)) {
            duplicateDrops(event);
        }
    }

    @SubscribeEvent
    public static void onCropGrowPost(CropGrowEvent.Post event) {
        if (!(event.getLevel() instanceof Level level)
                || !AurorianBlessingCycle.isActive(level, AurorianBlessingCycle.Blessing.GROWTH)
                || !event.getState().is(BlockTags.CROPS)) {
            return;
        }
        BlockState accelerated = advanceCrop(event.getState(), 2);
        if (accelerated != event.getState()) {
            level.setBlock(event.getPos(), accelerated, 2);
        }
    }

    public static void reconcilePlayer(Player player) {
        boolean acceptsBlessings = !MoonShieldSystem.isPurified(player);
        boolean exploration = acceptsBlessings && AurorianBlessingCycle.isActive(
                player.level(), AurorianBlessingCycle.Blessing.EXPLORATION);
        boolean combat = acceptsBlessings && AurorianBlessingCycle.isActive(
                player.level(), AurorianBlessingCycle.Blessing.COMBAT);
        boolean protection = acceptsBlessings && AurorianBlessingCycle.isActive(
                player.level(), AurorianBlessingCycle.Blessing.PROTECTION);
        applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), EXPLORATION_MOVEMENT,
                exploration ? EXPLORATION_MOVEMENT_SPEED : 0.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyModifier(player.getAttribute(Attributes.JUMP_STRENGTH), EXPLORATION_JUMP,
                exploration ? EXPLORATION_JUMP_STRENGTH : 0.0D,
                AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER), EXPLORATION_FALL_DAMAGE,
                exploration ? EXPLORATION_FALL_DAMAGE_MULTIPLIER : 0.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), COMBAT_ATTACK,
                combat ? COMBAT_ATTACK_DAMAGE : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), PROTECTION_HEALTH,
                protection ? PROTECTION_MAX_HEALTH : 0.0D, AttributeModifier.Operation.ADD_VALUE);
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
        if (amount == 0.0D) {
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

    private static void duplicateDrops(BlockDropsEvent event) {
        BlockPos pos = event.getPos();
        List<ItemEntity> originalDrops = new ArrayList<>(event.getDrops());
        for (ItemEntity original : originalDrops) {
            ItemStack copy = original.getItem().copy();
            if (!copy.isEmpty()) {
                event.getDrops().add(new ItemEntity(
                        event.getLevel(),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        copy));
            }
        }
    }

    private static boolean isMatureCrop(BlockState state) {
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age) >= maxAge(age);
    }

    private static BlockState advanceCrop(BlockState state, int stages) {
        IntegerProperty age = ageProperty(state);
        if (age == null) {
            return state;
        }
        int current = state.getValue(age);
        return state.setValue(age, Math.min(maxAge(age), current + stages));
    }

    private static IntegerProperty ageProperty(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty
                    && property.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

    private static int maxAge(IntegerProperty age) {
        return age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
}
