package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianStorageCrateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

/** Upgrade the old barrel entity ID before the chunk's deferred block entity loading. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class StorageCrateMigration {
    private StorageCrateMigration() {}

    @SubscribeEvent
    public static void load(ChunkDataEvent.Load event) {
        var chunk = event.getChunk();
        for (var tag : event.getData().blockEntities()) {
            if (!"minecraft:barrel".equals(tag.getStringOr("id", ""))) continue;
            var pos = BlockEntity.getPosFromTag(chunk.getPos(), tag);
            if (chunk.getBlockState(pos).getBlock() instanceof AurorianStorageCrateBlock crate && crate.isDoubleCrate()) {
                // Preserve slots 0..26, components, lock, loot table, seed and attachments verbatim.
                // Only inspect this loaded chunk; never query the level during chunk loading.
                tag.putString("id", TheAurorian2.id("double_storage_crate").toString());
                chunk.markUnsaved();
            }
        }
    }
}
