package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBiomeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class AurorianAllaySpawning {
    private AurorianAllaySpawning() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerDefaultPlacement(RegisterSpawnPlacementsEvent event) {
        // Declare vanilla's implicit defaults for NeoForge's spawn-list validation.
        // Register early so other mods can still supply their own allay placements.
        event.register(EntityType.ALLAY, SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void checkForestSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getEntityType() != EntityType.ALLAY
                || (event.getSpawnType() != EntitySpawnReason.NATURAL
                    && event.getSpawnType() != EntitySpawnReason.CHUNK_GENERATION)) {
            return;
        }
        var level = event.getLevel();
        var pos = event.getPos();
        if (!level.getLevel().dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                || !level.getBiome(pos).is(ModBiomeTags.HAS_FOREST_ALLAYS)) {
            return;
        }

        // Vanilla allays have no natural spawn placement. Constrain only our forest spawns;
        // commands, structures, duplication and other mods' spawn rules remain untouched.
        // Ignoring leaves allows the forest floor beneath a canopy, but excludes caves.
        if (!SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, EntityType.ALLAY)
                || pos.getY() < level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        pos.getX(), pos.getZ())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }
}
