package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class WarblerShoulders {
    private WarblerShoulders() {}

    @SubscribeEvent
    public static void afterPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var left = player.getShoulderEntityLeft();
        var right = player.getShoulderEntityRight();
        byte mask = (byte) ((isWarbler(left) ? 1 : 0) | (isWarbler(right) ? 2 : 0));
        // 26.1 only synchronizes vanilla parrot variants. The actual birds stay in
        // vanilla shoulder NBT; this transient byte is rebuilt after loading a save.
        byte previous = player.getExistingData(ModAttachments.AZURE_WARBLER_SHOULDERS).orElse((byte) 0);
        if (mask != previous) player.setData(ModAttachments.AZURE_WARBLER_SHOULDERS, mask);
        if (mask != 0 && player.getRandom().nextInt(200) == 0
                && (((mask & 1) != 0 && !left.getBooleanOr("Silent", false))
                    || ((mask & 2) != 0 && !right.getBooleanOr("Silent", false)))) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PARROT_AMBIENT, SoundSource.NEUTRAL, 1.0F,
                    1.0F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F);
        }
    }

    private static boolean isWarbler(CompoundTag tag) {
        return "theaurorian2:azure_warbler".equals(tag.getStringOr("id", ""));
    }
}
