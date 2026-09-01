package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class AurorianBlessingCycle {

    public enum Blessing {
        EXPLORATION("exploration"),
        COMBAT("combat"),
        PROTECTION("protection"),
        MINING("mining"),
        GROWTH("growth");

        private final String textureName;

        Blessing(String textureName) {
            this.textureName = textureName;
        }

        public int slot() {
            return this.ordinal();
        }

        public String textureName() {
            return this.textureName;
        }

        public static Blessing fromSlot(int slot) {
            Blessing[] blessings = values();
            return blessings[Math.floorMod(slot, blessings.length)];
        }
    }

    private static final ResourceKey<WorldClock> BLESSING_CLOCK = ResourceKey.create(
            Registries.WORLD_CLOCK, TheAurorian2.id("aurorian_blessing"));
    private static final long DAY_TICKS = 24_000L;
    private static final long CYCLE_DAYS = Blessing.values().length;
    private static final long CYCLE_TICKS = DAY_TICKS * CYCLE_DAYS;
    private static final long PREPARE_NEXT_DAY_TICK = 18_000L;
    private static final long AURORIAN_NIGHT_END = 12_000L;

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
        long worldDay = blessingDay(server, Math.floorDiv(overworldTicks, DAY_TICKS));
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

    public static List<Blessing> forecast(MinecraftServer server, int forecastDays) {
        if (forecastDays < 1) {
            throw new IllegalArgumentException("Forecast length must be positive");
        }

        Holder<WorldClock> overworldClock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        long currentWorldDay = blessingDay(
                server,
                Math.floorDiv(server.clockManager().getTotalTicks(overworldClock), DAY_TICKS));
        long worldSeed = server.overworld().getSeed();
        List<Blessing> forecast = new ArrayList<>(forecastDays);
        for (int daysAhead = 1; daysAhead <= forecastDays; daysAhead++) {
            forecast.add(Blessing.fromSlot(blessingSlot(worldSeed, currentWorldDay + daysAhead)));
        }

        return List.copyOf(forecast);
    }

    public static boolean isActive(Level level, Blessing blessing) {
        if (!level.dimensionTypeRegistration().is(TheAurorian2.AURORIAN_DIMENSION_TYPE)
                || Math.floorMod(level.getDefaultClockTime(), DAY_TICKS) >= AURORIAN_NIGHT_END) {
            return false;
        }
        Holder<WorldClock> blessingClock = level.registryAccess().getOrThrow(BLESSING_CLOCK);
        long blessingTicks = Math.floorMod(
                level.clockManager().getTotalTicks(blessingClock), CYCLE_TICKS);
        return Blessing.fromSlot((int) (blessingTicks / DAY_TICKS)) == blessing;
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

    public static long currentOverworldDay(MinecraftServer server) {
        Holder<WorldClock> overworldClock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        return Math.floorDiv(server.clockManager().getTotalTicks(overworldClock), DAY_TICKS);
    }

    private static long blessingDay(MinecraftServer server, long currentOverworldDay) {
        ServerLevel aurorian = server.getLevel(TheAurorian2.AURORIAN_LEVEL);
        return aurorian == null
                ? currentOverworldDay
                : AurorianArrivalSiteData.relativeBlessingDay(aurorian, currentOverworldDay);
    }

    private static long mixSeed(long worldSeed, long cycle) {
        long mixed = worldSeed ^ cycle * 0x9E3779B97F4A7C15L ^ 0x4155524F5249414EL;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }
}
