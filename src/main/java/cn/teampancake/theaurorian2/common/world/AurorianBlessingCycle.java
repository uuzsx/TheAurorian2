package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class AurorianBlessingCycle {

    private static final ResourceKey<WorldClock> BLESSING_CLOCK = ResourceKey.create(
            Registries.WORLD_CLOCK, TheAurorian2.id("aurorian_blessing"));
    private static final long DAY_TICKS = 24_000L;
    private static final long CYCLE_DAYS = 5L;
    private static final long CYCLE_TICKS = DAY_TICKS * CYCLE_DAYS;
    private static final long PREPARE_NEXT_DAY_TICK = 18_000L;

    private AurorianBlessingCycle() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.overworld() == null) {
            return;
        }

        Holder<WorldClock> overworldClock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        Holder<WorldClock> blessingClock = server.registryAccess().getOrThrow(BLESSING_CLOCK);
        float overworldRate = server.clockManager().getRate(overworldClock);
        if (Float.compare(server.clockManager().getRate(blessingClock), overworldRate) != 0) {
            server.clockManager().setRate(blessingClock, overworldRate);
        }

        long overworldTicks = server.clockManager().getTotalTicks(overworldClock);
        long worldDay = Math.floorDiv(overworldTicks, DAY_TICKS);
        long dayTime = Math.floorMod(overworldTicks, DAY_TICKS);
        long worldSeed = server.overworld().getSeed();
        int timelineSegment = blessingSlot(worldSeed, worldDay);
        if (dayTime >= PREPARE_NEXT_DAY_TICK) {
            int nextBlessingSlot = blessingSlot(worldSeed, worldDay + 1L);
            timelineSegment = Math.floorMod(nextBlessingSlot - 1, (int)CYCLE_DAYS);
        }

        long expectedTicks = timelineSegment * DAY_TICKS + dayTime;
        long currentTicks = Math.floorMod(server.clockManager().getTotalTicks(blessingClock), CYCLE_TICKS);

        if (currentTicks != expectedTicks) {
            server.clockManager().setTotalTicks(blessingClock, expectedTicks);
        }
    }

    private static int blessingSlot(long worldSeed, long worldDay) {
        int dayInCycle = (int)Math.floorMod(worldDay, CYCLE_DAYS);
        if (dayInCycle == 0) {
            return 0;
        }

        long cycle = Math.floorDiv(worldDay, CYCLE_DAYS);
        int[] shuffledSlots = {1, 2, 3, 4};
        Random random = new Random(mixSeed(worldSeed, cycle));
        for (int index = shuffledSlots.length - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            int slot = shuffledSlots[index];
            shuffledSlots[index] = shuffledSlots[swapIndex];
            shuffledSlots[swapIndex] = slot;
        }
        return shuffledSlots[dayInCycle - 1];
    }

    private static long mixSeed(long worldSeed, long cycle) {
        long mixed = worldSeed ^ cycle * 0x9E3779B97F4A7C15L ^ 0x4155524F5249414EL;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }
}
