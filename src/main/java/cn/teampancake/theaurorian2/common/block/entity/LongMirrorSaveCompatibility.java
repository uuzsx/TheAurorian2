package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianLongMirrorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Old mirrors had no block entity. Preserve their block IDs and fill the missing render markers once on load. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class LongMirrorSaveCompatibility {
    private LongMirrorSaveCompatibility() {}
    @SubscribeEvent public static void load(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        var sections = chunk.getSections();
        var pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < sections.length; i++) {
            var section = sections[i];
            if (!section.maybeHas(state -> state.getBlock() instanceof AurorianLongMirrorBlock)) continue;
            int y0 = chunk.getSectionYFromSectionIndex(i) << 4;
            for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                if (!(section.getBlockState(x, y, z).getBlock() instanceof AurorianLongMirrorBlock)) continue;
                pos.set(chunk.getPos().getMinBlockX() + x, y0 + y, chunk.getPos().getMinBlockZ() + z);
                if (!chunk.getBlockEntities().containsKey(pos)) {
                    chunk.getBlockEntity(pos.immutable(), LevelChunk.EntityCreationType.IMMEDIATE);
                    if (!chunk.getLevel().isClientSide()) chunk.markUnsaved();
                }
            }
        }
    }
}
