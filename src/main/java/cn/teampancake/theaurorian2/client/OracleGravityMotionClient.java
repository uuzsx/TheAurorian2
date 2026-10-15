package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleSpellEntity;
import cn.teampancake.theaurorian2.common.network.OracleGravityMotionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Predicts only the local player's lift. Capture, damage and release are server decisions. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class OracleGravityMotionClient {
    private static ClientLevel level;
    private static OracleGravityMotionPayload motion;

    private OracleGravityMotionClient() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        motion = null;
        level = null;
    }

    public static void handle(OracleGravityMotionPayload payload) {
        var minecraft = Minecraft.getInstance();
        if (payload.active()) {
            motion = payload;
            level = minecraft.level;
        } else if (motion != null && motion.spellId() == payload.spellId()) {
            if (minecraft.player != null) minecraft.player.fallDistance = 0;
            motion = null;
            level = null;
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Pre event) {
        if (motion == null || event.getEntity() != Minecraft.getInstance().player) return;
        var player = Minecraft.getInstance().player;
        if (player.level() != level || !player.isAlive() || player.isPassenger() || player.getAbilities().instabuild) {
            motion = null;
            level = null;
            return;
        }
        long age = level.getGameTime() - motion.start();
        if (age >= OracleSpellEntity.IMPACT + 20) {
            motion = null;
            level = null;
            return;
        }
        if (age < OracleSpellEntity.LIFT_START) return;
        double height = age >= OracleSpellEntity.IMPACT ? -.5 : OracleSpellEntity.liftOffset(age + 1,motion.empowered());
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x * .65, Mth.clamp(motion.baseY() + height - player.getY(), motion.empowered() ? -3.5 : -2.5, motion.empowered() ? 2 : 1.5), velocity.z * .65);
        // The server applies the actual fall damage once on contact, followed by spell damage.
        player.fallDistance = 0;
    }
}
