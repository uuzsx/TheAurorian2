package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.ReturningAxeEntity;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class RoundaboutThrow {
    public static final int USE_DURATION = 72000;
    public static final int CHARGE_TICKS = 8;
    private RoundaboutThrow() {}

    public static boolean hasThrow(ItemStack stack) {
        if (!stack.is(ItemTags.AXES)) return false;
        for (var holder : stack.getEnchantments().keySet())
            if (holder.is(ModEnchantments.ROUNDABOUT_THROW) && stack.getEnchantments().getLevel(holder) > 0) return true;
        return false;
    }

    @SubscribeEvent
    public static void use(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !hasThrow(event.getItemStack())
                || !EnchantmentAccess.has(event.getItemStack(), event.getEntity().registryAccess(), ModEnchantments.ROUNDABOUT_THROW)) return;
        event.setCanceled(true);
        if (event.getEntity().getCooldowns().isOnCooldown(event.getItemStack())
                || event.getEntity().hasData(ModAttachments.RETURNING_AXE)
                && !event.getEntity().getData(ModAttachments.RETURNING_AXE).stack.isEmpty()) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        event.getEntity().startUsingItem(event.getHand());
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    @SubscribeEvent
    public static void release(LivingEntityUseItemEvent.Stop event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !hasThrow(event.getItem())) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player && USE_DURATION - event.getDuration() >= CHARGE_TICKS)
            launch(player, event.getItem());
    }

    public static void launch(ServerPlayer player, ItemStack stack) {
        int rank = EnchantmentAccess.itemLevel(stack, player.registryAccess(), ModEnchantments.ROUNDABOUT_THROW);
        if (rank <= 0 || player.getMainHandItem() != stack || player.isSpectator()) return;
        ReturningAxeData data = player.getData(ModAttachments.RETURNING_AXE);
        if (!data.stack.isEmpty()) return;
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
        if (stack.isEmpty()) return;
        ReturningAxeEntity projectile = new ReturningAxeEntity(ModEntities.RETURNING_AXE.get(), player.level());
        data.stack = stack.split(1);
        data.flightId = projectile.getUUID().toString();
        data.slot = player.getInventory().getSelectedSlot();
        data.elapsed = 0;
        projectile.launch(player, data.stack, EnchantmentRules.throwRange(rank), damage);
        if (!player.level().addFreshEntity(projectile)) {
            recover(player);
            return;
        }
        player.getCooldowns().addCooldown(data.stack, 12);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
    }

    public static void recover(ServerPlayer player) {
        if (!player.hasData(ModAttachments.RETURNING_AXE)) return;
        ReturningAxeData data = player.getData(ModAttachments.RETURNING_AXE);
        if (data.stack.isEmpty()) return;
        ItemStack stack = data.stack;
        data.stack = ItemStack.EMPTY;
        data.flightId = "";
        if (data.slot >= 0 && data.slot < 9 && player.getInventory().getItem(data.slot).isEmpty()) {
            player.getInventory().setItem(data.slot, stack);
        } else if (!player.getInventory().add(stack)) {
            var dropped = player.drop(stack, false);
            if (dropped != null) dropped.setTarget(player.getUUID());
        }
        player.getInventory().setChanged();
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.hasData(ModAttachments.RETURNING_AXE)) {
            ReturningAxeData data = player.getData(ModAttachments.RETURNING_AXE);
            if (!data.stack.isEmpty() && ++data.elapsed > 120) recover(player);
        }
    }
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer player) recover(player); }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { if (event.getEntity() instanceof ServerPlayer player) recover(player); }
    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { if (event.getEntity() instanceof ServerPlayer player) recover(player); }
    @SubscribeEvent
    public static void death(LivingDeathEvent event) { if (event.getEntity() instanceof ServerPlayer player) recover(player); }
}
