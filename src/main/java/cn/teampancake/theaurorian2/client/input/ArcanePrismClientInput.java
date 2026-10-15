package cn.teampancake.theaurorian2.client.input;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.ArcanePrismItem;
import cn.teampancake.theaurorian2.common.item.ArcanePrismChannel;
import cn.teampancake.theaurorian2.mixin.ClientInputAccessor;
import cn.teampancake.theaurorian2.common.network.ArcanePrismAttackPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ArcanePrismClientInput {
    private ArcanePrismClientInput() {}
    @SubscribeEvent public static void movement(MovementInputUpdateEvent event) {
        if (!ArcanePrismChannel.isHolding(event.getEntity())) return;
        event.getInput().keyPresses = Input.EMPTY;
        ((ClientInputAccessor)event.getInput()).theaurorian2$setMoveVector(Vec2.ZERO);
        event.getEntity().setDeltaMovement(Vec3.ZERO);
    }
    @SubscribeEvent public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        if (!event.isAttack() || player == null || !(player.getMainHandItem().getItem() instanceof ArcanePrismItem)) return;
        // Consume the same attack binding over air, entities and blocks; never also mine or melee.
        event.setCanceled(true);
        event.setSwingHand(false);
        if (!player.getCooldowns().isOnCooldown(player.getMainHandItem())) {
            event.setSwingHand(true);
            ClientPacketDistributor.sendToServer(ArcanePrismAttackPayload.INSTANCE);
        }
    }
}
