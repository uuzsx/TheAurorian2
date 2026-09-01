package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.world.AurorianTravel;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class WorldScrollItem extends Item {

    public static final int USE_DURATION = 60;

    public WorldScrollItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer && !AurorianTravel.canBegin(serverPlayer, true)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 0.7F);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPYGLASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int usedTicks = USE_DURATION - ticksRemaining;
        if (usedTicks > 0 && usedTicks % 4 == 0) {
            double progress = usedTicks / (double) USE_DURATION;
            double radius = 1.4 - progress * 0.75;
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3.0 + usedTicks * 0.08;
                serverLevel.sendParticles(
                        i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                        entity.getX() + Math.cos(angle) * radius,
                        entity.getY() + 0.25 + progress * 1.4 + i % 3 * 0.18,
                        entity.getZ() + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (usedTicks == 36) {
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9F, 1.45F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            AurorianTravel.travel(player, stack);
        }
        return stack;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag flag) {
        builder.accept(Component.translatable("item.theaurorian2.world_scroll.tooltip.use")
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("item.theaurorian2.world_scroll.tooltip.flavor")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
