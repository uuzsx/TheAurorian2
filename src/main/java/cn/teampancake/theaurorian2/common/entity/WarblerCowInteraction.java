package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class WarblerCowInteraction {
    private WarblerCowInteraction() {}

    public static boolean spawnOnCow(AurorianCowEntity cow, @Nullable ItemStack egg,
            @Nullable Player player, EntitySpawnReason reason) {
        if (!(cow.level() instanceof ServerLevel level) || !cow.canAcceptWarbler()) return false;
        var bird = ModEntities.AZURE_WARBLER.get().create(level,
                egg == null ? null : EntityType.createDefaultStackConfig(level, egg, player),
                BlockPos.containing(cow.getX(), cow.getY() + cow.getBbHeight(), cow.getZ()), reason, false, false);
        if (bird == null) return false;
        if (!bird.perchOnCow(cow)) {
            bird.discard();
            return false;
        }
        if (!level.addFreshEntity(bird)) {
            bird.stopRiding();
            bird.discard();
            return false;
        }
        level.gameEvent(player, GameEvent.ENTITY_PLACE, bird.position());
        return true;
    }

    @SubscribeEvent
    public static void afterDamage(LivingDamageEvent.Post event) {
        if (event.getInflictedDamage() <= 0 || event.getEntity().level().isClientSide()) return;
        var source = event.getSource().getSourcePosition();
        if (event.getEntity() instanceof AurorianCowEntity cow) {
            // Passenger lists are immutable snapshots, so detaching here is safe, even on lethal hits.
            for (var passenger : cow.getPassengers()) {
                if (passenger instanceof AzureWarblerEntity bird) {
                    bird.startleFrom(source == null ? cow.position() : source);
                }
            }
        } else if (event.getEntity() instanceof AzureWarblerEntity bird) {
            bird.startleFrom(source == null ? bird.position() : source);
        }
    }
}
