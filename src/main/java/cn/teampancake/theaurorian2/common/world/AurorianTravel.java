package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class AurorianTravel {

    private static final int COMBAT_LOCK_TICKS = 100;
    private static final int TRAVEL_COOLDOWN_TICKS = 100;
    private static final int SAFE_SEARCH_RADIUS = 6;

    private AurorianTravel() {
    }

    public static boolean canBegin(ServerPlayer player, boolean showMessage) {
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }
        if (player.isPassenger()) {
            return fail(player, showMessage, "message.theaurorian2.world_scroll.dismount");
        }
        if ((player.getLastHurtByMob() != null
                        && player.tickCount - player.getLastHurtByMobTimestamp() <= COMBAT_LOCK_TICKS)
                || (player.getLastHurtMob() != null
                        && player.tickCount - player.getLastHurtMobTimestamp() <= COMBAT_LOCK_TICKS)) {
            return fail(player, showMessage, "message.theaurorian2.world_scroll.combat");
        }
        if (!player.level().dimension().equals(Level.OVERWORLD)
                && !player.level().dimension().equals(TheAurorian2.AURORIAN_LEVEL)) {
            return fail(player, showMessage, "message.theaurorian2.world_scroll.wrong_dimension");
        }
        return true;
    }

    public static boolean travel(ServerPlayer player, ItemStack scroll) {
        if (!canBegin(player, true)) {
            return false;
        }

        ServerLevel source = player.level();
        boolean enteringAurorian = source.dimension().equals(Level.OVERWORLD);
        MinecraftServer server = source.getServer();
        ServerLevel destinationLevel = enteringAurorian
                ? server.getLevel(TheAurorian2.AURORIAN_LEVEL)
                : server.overworld();
        if (destinationLevel == null) {
            player.sendOverlayMessage(Component.translatable(
                    "message.theaurorian2.world_scroll.missing_dimension"));
            return false;
        }

        AurorianTravelData currentData = player.getData(ModAttachments.AURORIAN_TRAVEL);
        BlockPos origin = player.blockPosition().immutable();
        float originYaw = player.getYRot();
        float originPitch = player.getXRot();
        Vec3 destination;
        float destinationYaw;
        float destinationPitch;

        if (enteringAurorian) {
            BlockPos arrival = AurorianArrivalSiteData.getOrCreate(
                    destinationLevel, player, AurorianBlessingCycle.currentOverworldDay(server));
            destination = Vec3.atBottomCenterOf(arrival);
            destinationYaw = 0.0F;
            destinationPitch = 0.0F;
        } else {
            destination = findReturnDestination(destinationLevel, player, currentData.returnPoint());
            destinationYaw = currentData.returnYaw();
            destinationPitch = currentData.returnPitch();
        }

        source.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.9F, 0.65F);
        TeleportTransition transition = new TeleportTransition(
                destinationLevel,
                destination,
                Vec3.ZERO,
                destinationYaw,
                destinationPitch,
                entity -> arrivalEffects((ServerPlayer) entity));
        if (player.teleport(transition) == null) {
            player.sendOverlayMessage(Component.translatable(
                    "message.theaurorian2.world_scroll.interrupted"));
            return false;
        }

        if (enteringAurorian) {
            player.setData(
                    ModAttachments.AURORIAN_TRAVEL,
                    currentData.recordDeparture(
                            GlobalPos.of(source.dimension(), origin), originYaw, originPitch));
        }
        player.getCooldowns().addCooldown(scroll, TRAVEL_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.translatable(enteringAurorian
                ? currentData.enteredAurorian()
                        ? "message.theaurorian2.world_scroll.entered"
                        : "message.theaurorian2.world_scroll.first_arrival"
                : "message.theaurorian2.world_scroll.returned"));
        return true;
    }

    private static Vec3 findReturnDestination(
            ServerLevel level, ServerPlayer player, Optional<GlobalPos> storedPoint) {
        if (storedPoint.isPresent() && storedPoint.get().dimension().equals(Level.OVERWORLD)) {
            BlockPos center = storedPoint.get().pos();
            int[] yOffsets = {0, 1, -1, 2, -2, 3, -3};
            for (int radius = 0; radius <= SAFE_SEARCH_RADIUS; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                            continue;
                        }
                        for (int yOffset : yOffsets) {
                            Vec3 safe = DismountHelper.findSafeDismountLocation(
                                    EntityType.PLAYER,
                                    level,
                                    center.offset(x, yOffset, z),
                                    true);
                            if (safe != null) {
                                return safe;
                            }
                        }
                    }
                }
            }
        }

        return player.adjustSpawnLocation(level, level.getRespawnData().pos()).getBottomCenter();
    }

    private static void arrivalEffects(ServerPlayer player) {
        ServerLevel level = player.level();
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        level.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(),
                48, 0.75, 1.1, 0.75, 0.035);
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 0.8, player.getZ(),
                36, 0.55, 0.85, 0.55, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private static boolean fail(ServerPlayer player, boolean showMessage, String key) {
        if (showMessage) {
            player.sendOverlayMessage(Component.translatable(key));
        }
        return false;
    }
}
