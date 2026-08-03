package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class PhantomBlossomEvents {

    private static final int WAVE_DURATION_TICKS = 24;
    private static final int MARK_DURATION_TICKS = 100;
    private static final int MARK_DAMAGE_INTERVAL_TICKS = 20;
    private static final double BLOOM_RANGE = 12.0;
    private static final float BLOOM_DAMAGE = 18.0F;
    private static final float MARK_DAMAGE = 7.0F;
    private static final List<BloomWave> ACTIVE_WAVES = new ArrayList<>();
    private static final Map<UUID, ActiveMark> ACTIVE_MARKS = new HashMap<>();

    private PhantomBlossomEvents() {
    }

    static void startBloomWave(ServerLevel level, Player player) {
        Vec3 center = player.position().add(0.0, 0.9, 0.0);
        ACTIVE_WAVES.add(new BloomWave(level.dimension(), player.getUUID(), center));
        level.playSound(null, center.x(), center.y(), center.z(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.25F, 0.82F);
        level.playSound(null, center.x(), center.y(), center.z(),
                SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.PLAYERS, 0.8F, 0.72F);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickWaves(event.getServer());
        tickMarks(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        PhantomBlossomMark mark = target.getData(ModAttachments.PHANTOM_BLOSSOM_MARK);
        if (!mark.isActive(level.getGameTime())) {
            return;
        }

        target.setData(ModAttachments.PHANTOM_BLOSSOM_MARK, PhantomBlossomMark.EMPTY);
        PhantomBlossomRequiemItem.spawnCherryDeath(level, target);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE_WAVES.clear();
        ACTIVE_MARKS.clear();
    }

    private static void tickWaves(MinecraftServer server) {
        Iterator<BloomWave> iterator = ACTIVE_WAVES.iterator();
        while (iterator.hasNext()) {
            BloomWave wave = iterator.next();
            ServerLevel level = server.getLevel(wave.levelKey);
            ServerPlayer owner = server.getPlayerList().getPlayer(wave.ownerId);
            if (level == null || owner == null || owner.level() != level) {
                iterator.remove();
                continue;
            }

            wave.age++;
            double progress = Math.min(1.0, wave.age / (double) WAVE_DURATION_TICKS);
            double easedProgress = progress * progress * (3.0 - 2.0 * progress);
            double radius = BLOOM_RANGE * easedProgress;
            PhantomBlossomRequiemItem.spawnExpansionWave(level, wave.center, radius, wave.age);
            hitWaveTargets(level, owner, wave, radius);
            if (wave.age >= WAVE_DURATION_TICKS) {
                iterator.remove();
            }
        }
    }

    private static void hitWaveTargets(
            ServerLevel level, ServerPlayer owner, BloomWave wave, double radius) {
        AABB bounds = new AABB(wave.center, wave.center).inflate(radius + 1.0);
        double radiusSquared = radius * radius;
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                target -> PhantomBlossomRequiemItem.isValidTarget(owner, target)
                        && !wave.hitTargets.contains(target.getUUID())
                        && target.getBoundingBox().getCenter().distanceToSqr(wave.center) <= radiusSquared
                        && owner.hasLineOfSight(target));

        for (LivingEntity target : targets) {
            wave.hitTargets.add(target.getUUID());
            applyDeathMark(level, target, owner);
            boolean damaged = target.hurtServer(
                    level,
                    level.damageSources().playerAttack(owner),
                    BLOOM_DAMAGE);
            if (damaged) {
                PhantomBlossomRequiemItem.spawnTargetButterflies(level, target);
            } else {
                cancelDeathMark(target);
            }
        }
    }

    private static void applyDeathMark(
            ServerLevel level, LivingEntity target, ServerPlayer owner) {
        long now = level.getGameTime();
        long expiresAt = now + MARK_DURATION_TICKS;
        target.setData(
                ModAttachments.PHANTOM_BLOSSOM_MARK,
                new PhantomBlossomMark(owner.getUUID(), expiresAt));
        target.setData(ModAttachments.PHANTOM_BLOSSOM_DEATH_EFFECT, true);
        ACTIVE_MARKS.put(
                target.getUUID(),
                new ActiveMark(
                        level.dimension(),
                        target.getId(),
                        owner.getUUID(),
                        now + MARK_DAMAGE_INTERVAL_TICKS,
                        expiresAt));
    }

    private static void cancelDeathMark(LivingEntity target) {
        ACTIVE_MARKS.remove(target.getUUID());
        target.setData(ModAttachments.PHANTOM_BLOSSOM_MARK, PhantomBlossomMark.EMPTY);
        target.setData(ModAttachments.PHANTOM_BLOSSOM_DEATH_EFFECT, false);
    }

    private static void tickMarks(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ActiveMark>> iterator = ACTIVE_MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveMark mark = iterator.next().getValue();
            ServerLevel level = server.getLevel(mark.levelKey);
            Entity entity = level == null ? null : level.getEntity(mark.targetId);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
                iterator.remove();
                continue;
            }

            long now = level.getGameTime();
            if (now > mark.expiresAt) {
                clearExpiredMark(target);
                iterator.remove();
                continue;
            }
            if (now < mark.nextDamageAt) {
                continue;
            }

            ServerPlayer owner = server.getPlayerList().getPlayer(mark.ownerId);
            boolean damaged = target.hurtServer(
                    level,
                    owner == null
                            ? level.damageSources().magic()
                            : level.damageSources().playerAttack(owner),
                    MARK_DAMAGE);
            if (damaged) {
                PhantomBlossomRequiemItem.spawnTargetButterflies(level, target);
            }
            mark.nextDamageAt += MARK_DAMAGE_INTERVAL_TICKS;

            if (!target.isAlive()) {
                iterator.remove();
            } else if (mark.nextDamageAt > mark.expiresAt) {
                clearExpiredMark(target);
                iterator.remove();
            }
        }
    }

    private static void clearExpiredMark(LivingEntity target) {
        target.setData(ModAttachments.PHANTOM_BLOSSOM_MARK, PhantomBlossomMark.EMPTY);
        target.setData(ModAttachments.PHANTOM_BLOSSOM_DEATH_EFFECT, false);
    }

    private static final class BloomWave {

        private final ResourceKey<Level> levelKey;
        private final UUID ownerId;
        private final Vec3 center;
        private final Set<UUID> hitTargets = new HashSet<>();
        private int age;

        private BloomWave(ResourceKey<Level> levelKey, UUID ownerId, Vec3 center) {
            this.levelKey = levelKey;
            this.ownerId = ownerId;
            this.center = center;
        }
    }

    private static final class ActiveMark {

        private final ResourceKey<Level> levelKey;
        private final int targetId;
        private final UUID ownerId;
        private long nextDamageAt;
        private final long expiresAt;

        private ActiveMark(
                ResourceKey<Level> levelKey,
                int targetId,
                UUID ownerId,
                long nextDamageAt,
                long expiresAt) {
            this.levelKey = levelKey;
            this.targetId = targetId;
            this.ownerId = ownerId;
            this.nextDamageAt = nextDamageAt;
            this.expiresAt = expiresAt;
        }
    }
}
