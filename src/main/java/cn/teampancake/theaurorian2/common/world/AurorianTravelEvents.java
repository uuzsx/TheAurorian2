package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public final class AurorianTravelEvents {

    private static final Identifier SMELT_IRON = Identifier.withDefaultNamespace("story/smelt_iron");

    private AurorianTravelEvents() {
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.wakeImmediately()
                || !(event.getEntity() instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || !player.level().dimension().equals(Level.OVERWORLD)) {
            return;
        }

        AurorianTravelData data = player.getData(ModAttachments.AURORIAN_TRAVEL);
        if (data.signalReceived() || !hasReachedIronAge(player)) {
            return;
        }

        boolean alreadyCarryingScroll = player.getInventory().contains(
                stack -> stack.is(ModLegacyItems.WORLD_SCROLL.get()));
        if (!alreadyCarryingScroll) {
            ItemStack scroll = new ItemStack(ModLegacyItems.WORLD_SCROLL.get());
            if (!player.getInventory().add(scroll)) {
                player.drop(scroll, false);
            }
        }

        player.setData(ModAttachments.AURORIAN_TRAVEL, data.receiveSignal());
        player.sendSystemMessage(Component.translatable(
                "message.theaurorian2.world_scroll.received"));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.75F);
    }

    private static boolean hasReachedIronAge(ServerPlayer player) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(SMELT_IRON);
        return advancement != null
                && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
