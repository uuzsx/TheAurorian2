package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.CrystalWormEntity;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Rare cave encounters; all searches are bounded and only touch loaded chunks. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class CrystalWormSpawning {
    public static final int LOCAL_LIMIT = 2;
    private CrystalWormSpawning() {}

    public static boolean underground(ServerLevel level, BlockPos pos) {
        return level.dimension().equals(TheAurorian2.AURORIAN_LEVEL) && level.hasChunkAt(pos)
                && pos.getY() > level.getMinY() + 4 && !level.canSeeSky(pos)
                && depth(level, pos) >= 16;
    }

    public static int depth(ServerLevel level, BlockPos pos) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - pos.getY();
    }

    public static boolean habitat(ServerLevel level, BlockPos pos) {
        return underground(level, pos) && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir() && level.getFluidState(pos.below()).isEmpty()
                && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }

    public static float naturalChance(int depth) {
        return Mth.lerp(Mth.clamp((depth - 16) / 128F, 0, 1), .12F, .72F);
    }

    public static float encounterChance(int depth, int undergroundSeconds) {
        float deeper = Mth.clamp((depth - 16) / 128F, 0, 1);
        float longer = Mth.clamp((undergroundSeconds - 120) / 1080F, 0, 1);
        return (.03F + .12F * deeper) * (1 + 2 * longer);
    }

    public static boolean hasRoom(ServerLevel level, BlockPos pos, CrystalWormEntity except) {
        AABB area = new AABB(pos).inflate(80, 48, 80);
        return level.getEntitiesOfClass(CrystalWormEntity.class, area,
                w -> w != except && w.isAlive()).size() < LOCAL_LIMIT;
    }

    @SubscribeEvent public static void limitNaturalSpawns(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof CrystalWormEntity worm) || event.isSpawnCancelled()
                || event.getSpawnType() != EntitySpawnReason.NATURAL && event.getSpawnType() != EntitySpawnReason.CHUNK_GENERATION) return;
        if (!hasRoom(event.getLevel().getLevel(), worm.blockPosition(), worm)) event.setSpawnCancelled(true);
    }

    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post event) {
        // Spread players over the second; no player/block/entity scans run every tick.
        if (event.getEntity() instanceof ServerPlayer player && (player.tickCount + player.getId()) % 20 == 0) {
            updateExposure(player);
        }
    }

    public static void updateExposure(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!player.isAlive() || player.isSpectator() || player.isCreative() || level.getDifficulty() == Difficulty.PEACEFUL
                || !underground(level, player.blockPosition())) {
            if (player.hasData(ModAttachments.CRYSTAL_WORM_EXPOSURE)) player.removeData(ModAttachments.CRYSTAL_WORM_EXPOSURE);
            return;
        }
        if (!player.connection.hasClientLoaded() || player.isChangingDimension()) return;
        CrystalWormExposure old = player.getData(ModAttachments.CRYSTAL_WORM_EXPOSURE);
        int seconds = Math.min(CrystalWormExposure.MAX_SECONDS, old.seconds() + 1);
        int next = Math.max(0, old.attemptInSeconds() - 1);
        if (next == 0) {
            next = 60;
            if (seconds >= 120 && level.getGameRules().get(GameRules.SPAWN_MOBS)
                    && player.getRandom().nextFloat() < encounterChance(depth(level, player.blockPosition()), seconds)
                    && trySpawnNear(player)) next = 180;
        }
        player.setData(ModAttachments.CRYSTAL_WORM_EXPOSURE, new CrystalWormExposure(seconds, next));
    }

    public static boolean trySpawnNear(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!player.isAlive() || player.isCreative() || player.isSpectator() || level.getDifficulty() == Difficulty.PEACEFUL
                || !player.connection.hasClientLoaded() || player.isChangingDimension()
                || !level.getGameRules().get(GameRules.SPAWN_MOBS) || !underground(level, player.blockPosition())
                || !hasRoom(level, player.blockPosition(), null)) return false;
        for (int i = 0; i < 18; i++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2;
            double radius = 24 + player.getRandom().nextDouble() * 8;
            BlockPos column = BlockPos.containing(player.getX() + Math.cos(angle) * radius,
                    player.getY() + 3, player.getZ() + Math.sin(angle) * radius);
            if (!level.hasChunkAt(column)) continue;
            // Nine blocks per column at most, not an unbounded ground/whole-chunk search.
            for (int down = 0; down < 9; down++) {
                BlockPos pos = column.below(down);
                if (!habitat(level, pos)) continue;
                if (!hasRoom(level, pos, null)) return false;
                CrystalWormEntity worm = ModEntities.CRYSTAL_WORM.get().create(level, EntitySpawnReason.NATURAL);
                if (worm == null) return false;
                worm.setPos(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .5);
                if (!EventHooks.checkSpawnPosition(worm, level, EntitySpawnReason.NATURAL)) continue;
                EventHooks.finalizeMobSpawn(worm, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
                if (!worm.isSpawnCancelled() && level.addFreshEntity(worm)) return true;
            }
        }
        return false;
    }
}
