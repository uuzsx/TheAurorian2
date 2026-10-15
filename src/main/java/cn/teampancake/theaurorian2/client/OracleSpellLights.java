package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleArcaneEntity;
import cn.teampancake.theaurorian2.common.entity.OracleFlamePath;
import cn.teampancake.theaurorian2.common.entity.OracleSpellEntity;
import cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

/** Temporary client light emission. Vanilla propagation supplies occlusion, falloff and
 * render-section invalidation; no light blocks, saved data or server light updates. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class OracleSpellLights {
    private static final int MAX_SOURCES = 64, MAX_SPELLS = 2048;
    private static final Set<Entity> SPELLS = new LinkedHashSet<>();
    private static final Set<RisingClockOracleEntity> ORACLES = new LinkedHashSet<>();
    private static ClientLevel level;
    private static Long2IntOpenHashMap sources = new Long2IntOpenHashMap();
    private static Long2IntOpenHashMap next = new Long2IntOpenHashMap();
    private static Vec3 camera = Vec3.ZERO;
    private static int ticks;

    private OracleSpellLights() {}

    /** Called only by the narrow BlockLightEngine hook; server levels always return zero. */
    public static int emission(BlockGetter world, long position) {
        return world == level ? sources.get(position) : 0;
    }

    @SubscribeEvent public static void join(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ClientLevel client)
                || !(event.getEntity() instanceof OracleArcaneEntity || event.getEntity() instanceof OracleSpellEntity
                    || event.getEntity() instanceof RisingClockOracleEntity)) return;
        if (level != client) reset(client);
        if (event.getEntity() instanceof RisingClockOracleEntity oracle) {
            if (ORACLES.size() < MAX_SPELLS) ORACLES.add(oracle);
        } else if (SPELLS.size() < MAX_SPELLS) SPELLS.add(event.getEntity());
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            SPELLS.remove(event.getEntity());
            ORACLES.remove(event.getEntity());
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { reset(null); }

    private static void reset(ClientLevel client) {
        SPELLS.clear(); ORACLES.clear(); sources.clear(); next.clear(); level = client; ticks = 0;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.level != level) { reset(mc.level); return; }
        if (level == null || mc.isPaused() || mc.getCameraEntity() == null || (++ticks & 1) != 0) return;
        if (SPELLS.isEmpty() && ORACLES.isEmpty() && sources.isEmpty()) return;
        camera = mc.getCameraEntity().position();
        next.clear();
        // Reserve the visible casters' light before filling the shared budget with spells.
        for (RisingClockOracleEntity oracle : ORACLES) {
            if (oracle.isAlive() && !oracle.isRemoved() && !oracle.isInvisible())
                point(oracle.position().add(0, oracle.getBbHeight() * .5, 0), 12);
            if (next.size() >= MAX_SOURCES) break;
        }
        for (Entity entity : SPELLS) {
            if (entity.isRemoved() || entity.position().distanceToSqr(camera) > 96*96) continue;
            if (entity instanceof OracleArcaneEntity arcane) arcane(arcane);
            else if (entity instanceof OracleSpellEntity terrain) terrain(terrain);
            if (next.size() >= MAX_SOURCES) break;
        }
        var previous = sources; sources = next; next = previous;
        // Only changed cells are queued. Periodically refresh active cells in case a
        // freshly received chunk light packet replaced the client's local light layer.
        boolean refresh = ticks % 20 == 0;
        for (var entry : sources.long2IntEntrySet())
            if (refresh || next.get(entry.getLongKey()) != entry.getIntValue())
                level.getLightEngine().checkBlock(BlockPos.of(entry.getLongKey()));
        for (var entry : next.long2IntEntrySet())
            if (!sources.containsKey(entry.getLongKey()))
                level.getLightEngine().checkBlock(BlockPos.of(entry.getLongKey()));
    }

    private static void point(Vec3 position, int brightness) {
        if (next.size() >= MAX_SOURCES || brightness <= 0 || position.distanceToSqr(camera) > 80*80) return;
        BlockPos pos = BlockPos.containing(position);
        if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) return;
        // Never inject emission inside an opaque block, including a newly placed wall.
        if (level.getBlockState(pos).getLightDampening() >= 15) return;
        long key = pos.asLong();
        next.put(key, Math.max(next.get(key), brightness));
    }

    private static void arcane(OracleArcaneEntity spell) {
        if (spell.isWarmingUp()) return;
        Vec3 pos = spell.position();
        float age = spell.age(0);
        switch (spell.kind()) {
            case OracleArcaneEntity.REPULSOR -> {
                if (age > spell.beamTicks()) return;
                Vec3 direction = spell.getLookAngle();
                int brightness = Math.round(15*spell.beamVisualFade(0));
                for (double distance = 0; distance < spell.beamLength(); distance += 8)
                    point(pos.add(direction.scale(distance)), brightness);
                point(spell.beamEnd().subtract(direction.scale(.15)), brightness);
            }
            case OracleArcaneEntity.FLAME -> {
                if (age >= OracleArcaneEntity.FLAME_DURATION) return;
                var path = spell.flamePath();
                for (int strand=0;strand<OracleFlamePath.STRANDS;strand++)
                    for (int step=12;step<=path.end(strand);step+=16)
                        point(path.point(strand,step),strand==0 ? 14 : 13);
            }
            case OracleArcaneEntity.BLAST, OracleArcaneEntity.HIT -> point(pos, 15);
            case OracleArcaneEntity.SHIELD -> point(pos.add(0, 1, 0), 13);
            case OracleArcaneEntity.TRAIL, OracleArcaneEntity.FIRE_PATCH -> point(pos.add(0, .35, 0), 12);
            case OracleArcaneEntity.BURN -> point(pos, 9);
            case OracleArcaneEntity.CLOCK -> {
                point(pos, 12);
                if (spell.empowered()) for (int i = 0; i < 8; i++) {
                    double angle = i*Math.PI/4;
                    point(pos.add(Math.cos(angle)*24, 0, Math.sin(angle)*24), 12);
                }
            }
            default -> { }
        }
    }

    private static void terrain(OracleSpellEntity spell) {
        float age = spell.age(0);
        if (spell.kind() == OracleSpellEntity.LIGHTNING) {
            float impact = age - OracleSpellEntity.LIGHTNING_IMPACT - spell.delay();
            int brightness = impact < 0 ? 9 : impact < 12 ? 15 : Math.max(0, 15-(int)(impact-11)*2);
            point(spell.position().add(0, .1, 0), brightness);
        } else if (spell.kind() == OracleSpellEntity.GRAVITY) {
            point(spell.position().add(0, .4, 0), 11);
        } else if (spell.kind() == OracleSpellEntity.LIFTED_BLOCK) {
            point(spell.position().add(0, OracleSpellEntity.liftOffset(age, spell.empowered())+1.1, 0), 10);
        }
    }
}
