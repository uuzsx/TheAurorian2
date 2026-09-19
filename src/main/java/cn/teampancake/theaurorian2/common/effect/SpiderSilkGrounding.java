package cn.teampancake.theaurorian2.common.effect;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class SpiderSilkGrounding {
    public static final int DURATION_TICKS = 7 * 20;

    private SpiderSilkGrounding() {}

    public static void apply(LivingEntity entity) {
        if (entity.level().isClientSide() || !entity.isAlive() || entity.isSpectator()) return;
        // A deadline needs synchronization only on application/refresh and removal.
        // Unlike a potion, this also works on potion-immune bosses such as the Wither.
        entity.setData(ModAttachments.SILK_GROUNDED_UNTIL, entity.level().getGameTime() + DURATION_TICKS);
        suppressFlight(entity);
    }

    public static boolean isGrounded(Entity entity) {
        return entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator()
                && entity.hasData(ModAttachments.SILK_GROUNDED_UNTIL)
                && entity.getData(ModAttachments.SILK_GROUNDED_UNTIL) > entity.level().getGameTime();
    }

    @SubscribeEvent
    public static void beforeEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)
                || !entity.hasData(ModAttachments.SILK_GROUNDED_UNTIL)) return;
        if (isGrounded(entity)) {
            suppressFlight(entity);
        } else if (!entity.level().isClientSide()) {
            entity.removeData(ModAttachments.SILK_GROUNDED_UNTIL);
        }
    }

    private static void suppressFlight(LivingEntity entity) {
        if (entity.isPassenger()) entity.stopRiding();
        if (entity.isFallFlying()) entity.stopFallFlying();
        if (entity instanceof Player player && player.getAbilities().flying) {
            // Preserve mayfly: temporary grounding must not revoke a mod's flight permission.
            player.getAbilities().flying = false;
            if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
        }
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x, Math.min(movement.y, entity.onGround() ? -0.08 : -0.6), movement.z);
    }

    public static Vec3 constrainMovement(Entity entity, Vec3 movement) {
        if (!isGrounded(entity)) return movement;
        double down = Math.min(movement.y, entity.onGround() ? -0.08 : -0.6);
        return new Vec3(movement.x, down, movement.z);
    }
}
