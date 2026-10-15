package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleArcaneEntity;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ArcanePrismItem extends Item {
    public static final int COOLDOWN_TICKS = 20 * 60 * 5;
    public static final Identifier COOLDOWN_GROUP = TheAurorian2.id("arcane_prism");

    public ArcanePrismItem(Properties properties) { super(properties); }

    public boolean tryCast(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isRemoved()
                || player.getMainHandItem().getItem() != this || player.isUsingItem() || player.isPassenger()) return false;
        long now = player.level().getServer().overworld().getGameTime();
        // Per-player saved time is authoritative; a new stack, relog or respawn cannot bypass it.
        if (player.getData(ModAttachments.ARCANE_PRISM_READY_AT) > now
                || player.getCooldowns().isOnCooldown(getDefaultInstance())) return false;
        if (OracleArcaneEntity.createPrismBeam(player.level(), player) == null) return false;
        player.setData(ModAttachments.ARCANE_PRISM_READY_AT, now + COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(COOLDOWN_GROUP, COOLDOWN_TICKS);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, .8F, 1.3F);
        return true;
    }

    public static void restoreCooldown(ServerPlayer player) {
        if (!player.hasData(ModAttachments.ARCANE_PRISM_READY_AT)) return;
        int remaining = (int)Math.clamp(player.getData(ModAttachments.ARCANE_PRISM_READY_AT)
                - player.level().getServer().overworld().getGameTime(), 0, COOLDOWN_TICKS);
        if (remaining > 0) player.getCooldowns().addCooldown(COOLDOWN_GROUP, remaining);
    }

    @Override public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) { return true; }
    @Override public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) { return false; }

    @Override @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.theaurorian2.arcane_prism.cast").withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("item.theaurorian2.arcane_prism.damage").withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.theaurorian2.arcane_prism.cooldown").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.theaurorian2.arcane_prism.channel").withStyle(ChatFormatting.GRAY));
    }
}
