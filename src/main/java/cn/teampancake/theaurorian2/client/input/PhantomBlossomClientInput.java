package cn.teampancake.theaurorian2.client.input;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomRequiemItem;
import cn.teampancake.theaurorian2.common.network.PhantomBlossomAttackPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class PhantomBlossomClientInput {

    private PhantomBlossomClientInput() {
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null
                || !(player.getMainHandItem().getItem() instanceof PhantomBlossomRequiemItem)) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(true);
        ClientPacketDistributor.sendToServer(PhantomBlossomAttackPayload.INSTANCE);
    }
}
