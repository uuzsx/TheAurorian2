package cn.teampancake.theaurorian2.common.worldgen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/** Shared, seed-dependent geography for the biome source and noise router. */
public final class LakeIslandLayout {
    public static final int CELL_SIZE = 3584;
    public static final int WATER_LEVEL = 63;
    public static final int FOREST_BAND = 384;
    public static final int TERRAIN_BLEND = 192;
    private static final int SITE_ATTEMPTS = 6;
    private static final int SITE_CACHE_SIZE = 64;
    private static final Sample OUTSIDE = new Sample(null, 0, 0, 0, Zone.NONE, false, 0, WATER_LEVEL);
    private final Fields fields;
    private final ThreadLocal<Cache> cache = ThreadLocal.withInitial(Cache::new);
    private final Map<Long, Optional<Site>> sites = new LinkedHashMap<>(SITE_CACHE_SIZE, 0.75F, true);

    public LakeIslandLayout(Fields fields) { this.fields = fields; }

    public Sample sample(int x, int z) {
        Cache local = cache.get();
        int index = (x & 15) | ((z & 15) << 4);
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        if (local.columns[index] != null && local.keys[index] == key) return local.columns[index];
        int cellX = Math.floorDiv(x, CELL_SIZE), cellZ = Math.floorDiv(z, CELL_SIZE);
        if (!local.initialized || local.cellX != cellX || local.cellZ != cellZ) {
            local.initialized = true;
            local.cellX = cellX;
            local.cellZ = cellZ;
            local.site = siteForCell(cellX, cellZ);
        }
        Sample result = local.site == null ? OUTSIDE : sample(local.site, x, z);
        local.keys[index] = key;
        local.columns[index] = result;
        return result;
    }

    /** Returns the same candidate for every chunk of a cell, without loading any chunks. */
    public synchronized Site siteForCell(int cellX, int cellZ) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xffffffffL);
        Optional<Site> cached = sites.get(key);
        if (cached != null) return cached.orElse(null);
        Site site = createSite(cellX, cellZ);
        if (sites.size() == SITE_CACHE_SIZE) sites.remove(sites.keySet().iterator().next());
        sites.put(key, Optional.ofNullable(site));
        return site;
    }

    private Site createSite(int cellX, int cellZ) {
        int tileX = cellX * CELL_SIZE + CELL_SIZE / 2;
        int tileZ = cellZ * CELL_SIZE + CELL_SIZE / 2;
        var tile = new DensityFunction.SinglePointContext(tileX, 0, tileZ);
        long random = mix(Double.doubleToLongBits(fields.continents.compute(tile))
                ^ ((long) cellX * 0x9e3779b97f4a7c15L) ^ ((long) cellZ * 0x632be59bd9b4e019L));
        // One landscape per cell. Its forest and all density blending remain
        // well inside the cell, including the maximum 320-block center jitter.
        for (int attempt = 0; attempt < SITE_ATTEMPTS; attempt++) {
            Site site = candidate(tileX, tileZ, mix(random + attempt * 0x9e3779b97f4a7c15L));
            if (site != null && hasForestNeighborhood(site)) return site;
        }
        return null;
    }

    private Site candidate(int tileX, int tileZ, long random) {
        // Align seed-field samples with the native flat_cache lattice.
        int x = tileX + 4 * ((int) (unit(random) * 161) - 80);
        int z = tileZ + 4 * ((int) (unit(mix(random + 1)) * 161) - 80);
        var center = new DensityFunction.SinglePointContext(x, 0, z);
        if (fields.continents.compute(center) < -0.06 || fields.temperature.compute(center) < -0.6
                || fields.humidity.compute(center) < -0.3 || fields.ridges.compute(center) > 0.8) return null;
        double radius = 120 + unit(mix(random + 3)) * 40;
        double islandRadius = 40 + unit(mix(random + 4)) * 8;
        double angle = unit(mix(random + 5)) * Math.PI * 2;
        double phase = unit(mix(random + 6)) * Math.PI * 2;
        double islandAngle = unit(mix(random + 7)) * Math.PI * 2;
        double islandOffset = 6 + unit(mix(random + 8)) * 10;
        return new Site(x, z, radius, islandRadius,
                x + Math.cos(islandAngle) * islandOffset, z + Math.sin(islandAngle) * islandOffset,
                Math.cos(angle), Math.sin(angle), 1 + unit(mix(random + 9)) * 0.18, phase,
                fields.ridges.compute(center) >= 0);
    }

    private boolean hasForestNeighborhood(Site site) {
        // Only screen the broad setting. Shores and the island are designed together
        // below; they no longer have to coincide with rare pre-existing native coasts.
        int land = 0;
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            double radius = site.lakeRadius + 300;
            int x = quartAligned(site.centerX + Math.cos(angle) * radius);
            int z = quartAligned(site.centerZ + Math.sin(angle) * radius);
            for (int y = 64; y <= 144; y += 16) {
                if (fields.baselineTerrain.compute(new DensityFunction.SinglePointContext(x, y, z)) > 0) {
                    land++;
                    break;
                }
            }
            if (land + 11 - i < 9) return false;
        }
        return land >= 9;
    }

    private static int quartAligned(double coordinate) {
        return (int) Math.round(coordinate / 4) * 4;
    }

    private static Sample sample(Site site, int x, int z) {
        // The forest has its own broad outline; it does not form a narrow ring
        // tracing every bend of the lake shore.
        double forestDistance = site.forestDistance(x, z);
        double forestEdge = site.lakeRadius + FOREST_BAND;
        if (forestDistance >= forestEdge + TERRAIN_BLEND) return OUTSIDE;
        double lakeDistance = site.lakeDistance(x, z);
        double islandDistance = site.islandDistance(x, z);
        double shoreInfluence = 1 - smooth((lakeDistance - site.lakeRadius - 72) / 112);
        double regionInfluence = 1 - smooth((forestDistance - site.lakeRadius - 160) / TERRAIN_BLEND);
        double terrainWeight = Math.max(shoreInfluence, regionInfluence);
        double weight = Math.max(terrainWeight, 1 - smooth((forestDistance - forestEdge) / TERRAIN_BLEND));
        double waterWeight = smooth((islandDistance - site.islandRadius + 12) / 10)
                * (1 - smooth((lakeDistance - site.lakeRadius - 8) / 16));
        double islandWeight = 1 - smooth((islandDistance - site.islandRadius) / 12);
        Zone zone = site.containsIslandBiome(x, z) ? Zone.ISLAND
                : lakeDistance < site.lakeRadius ? Zone.LAKE
                : forestDistance < forestEdge ? Zone.FOREST : Zone.NONE;
        double height = site.surfaceHeight(x, z, lakeDistance, islandDistance);
        return new Sample(site, waterWeight, islandWeight, weight, zone,
                islandDistance < site.islandRadius - 14, terrainWeight, height);
    }
    public static double smooth(double value) {
        double t = Math.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double smoothMin(double a, double b, double width) {
        double blend = Math.clamp(0.5 + 0.5 * (b - a) / width, 0, 1);
        return lerp(b, a, blend) - width * blend * (1 - blend);
    }
    private static double coastNoise(double x, double z, int scale, long salt) {
        int gx = (int) Math.floor(x / scale), gz = (int) Math.floor(z / scale);
        double tx = smooth(x / scale - gx);
        double tz = smooth(z / scale - gz);
        return lerp(lerp(lattice(gx, gz, salt), lattice(gx + 1, gz, salt), tx),
                lerp(lattice(gx, gz + 1, salt), lattice(gx + 1, gz + 1, salt), tx), tz);
    }
    private static double lattice(int x, int z, long salt) {
        return 2 * unit(mix(salt ^ ((long) x * 0x9e3779b97f4a7c15L) ^ ((long) z * 0x632be59bd9b4e019L))) - 1;
    }
    private static double unit(long value) { return (value >>> 11) * 0x1.0p-53; }
    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    public enum Zone { NONE, ISLAND, LAKE, FOREST }
    public record Sample(Site site, double waterWeight, double islandWeight, double weight, Zone zone,
                         boolean islandCore, double terrainWeight, double surfaceHeight) {}
    public record Site(int centerX, int centerZ, double lakeRadius, double islandRadius,
                       double islandX, double islandZ, double cos, double sin, double aspect,
                       double phase, boolean curtainForest) {
        public double lakeDistance(double x, double z) {
            long salt = Double.doubleToLongBits(phase);
            double dx = x - centerX + 19 * coastNoise(x, z, 85, salt)
                    + 10 * coastNoise(x, z, 30, mix(salt + 1));
            double dz = z - centerZ + 19 * coastNoise(x, z, 85, mix(salt + 2))
                    + 10 * coastNoise(x, z, 30, mix(salt + 3));
            double u = dx * cos + dz * sin, v = (-dx * sin + dz * cos) / aspect;
            // Two unequal basins and a sheltered inlet give the lake an asymmetric
            // outline. Local shoreline noise supplies smaller bends and points.
            double main = Math.hypot((u + lakeRadius * 0.18) / 1.02,
                    (v - lakeRadius * 0.08) / 0.85) - lakeRadius * 0.86;
            double bay = Math.hypot((u - lakeRadius * 0.46) / 0.86,
                    (v + lakeRadius * 0.18) / 1.02) - lakeRadius * 0.69;
            double inlet = Math.hypot((u - lakeRadius * 0.05) / 1.05,
                    v - lakeRadius * 0.94) - lakeRadius * 0.44;
            double shore = -smoothMin(-smoothMin(main, bay, 18), inlet, 10)
                    + 4 * coastNoise(x, z, 21, mix(salt + 4));
            // Keep water all around the island even where a deep cove reaches inward.
            double islandClearance = Math.hypot(x - islandX, z - islandZ) - (islandRadius * 1.45 + 30);
            return lakeRadius + smoothMin(shore, islandClearance, 12);
        }

        public double islandDistance(double x, double z) {
            long salt = mix(Double.doubleToLongBits(phase) + 2);
            double dx = x - islandX + 3 * coastNoise(x, z, 20, salt)
                    + coastNoise(x, z, 7, mix(salt + 1));
            double dz = z - islandZ + 3 * coastNoise(x, z, 20, mix(salt + 2))
                    + coastNoise(x, z, 7, mix(salt + 3));
            double u = dx * cos - dz * sin, v = (dx * sin + dz * cos) / 0.94;
            return islandRadius + islandShape(u, v)
                    + 3 * coastNoise(x, z, 18, mix(salt + 4))
                    + 1.1 * coastNoise(x, z, 6, mix(salt + 5));
        }

        public boolean containsIslandBiome(double x, double z) {
            double dx = x - islandX, dz = z - islandZ;
            // Cover the dry shore despite fuzzy quart-biome sampling and terrain
            // interpolation. The unwarped shape gives a bounded, smooth fringe;
            // its extra underwater area still uses the ordinary lake-bed rules.
            return islandShape(dx * cos - dz * sin, (dx * sin + dz * cos) / 0.94) <= 28;
        }

        private double islandShape(double u, double v) {
            double main = Math.hypot((u + islandRadius * 0.18) / 1.04,
                    (v - islandRadius * 0.08) / 0.87) - islandRadius * 0.80;
            double point = Math.hypot((u - islandRadius * 0.43) / 0.94,
                    (v + islandRadius * 0.17) / 1.08) - islandRadius * 0.64;
            double cove = Math.hypot(u + islandRadius * 0.25,
                    v - islandRadius * 0.68) - islandRadius * 0.32;
            return -smoothMin(-smoothMin(main, point, 8), cove, 4);
        }

        public double forestDistance(double x, double z) {
            long salt = mix(Double.doubleToLongBits(phase) + 3);
            double dx = x - centerX + 68 * coastNoise(x, z, 220, salt);
            double dz = z - centerZ + 68 * coastNoise(x, z, 220, mix(salt));
            return Math.hypot(dx, dz / 1.10) + 24 * coastNoise(x, z, 90, mix(salt + 1));
        }

        private double surfaceHeight(double x, double z, double lakeDistance, double islandDistance) {
            long salt = mix(Double.doubleToLongBits(phase) + 4);
            double broad = coastNoise(x, z, 128, salt);
            double middle = coastNoise(x, z, 48, mix(salt + 1));
            double detail = coastNoise(x, z, 16, mix(salt + 2));
            double forest = Math.max(64.8, 67 + 3 * broad + 2 * middle + 1.6 * detail);
            double island = Math.max(64.5, 66.5 + 1.5 * coastNoise(x, z, 34, mix(salt + 5))
                    + 0.8 * detail + 0.45 * coastNoise(x, z, 6, mix(salt + 6)));
            double bed = 53 + 2.3 * coastNoise(x, z, 60, mix(salt + 3))
                    + 0.8 * coastNoise(x, z, 17, mix(salt + 4));
            double waterline = WATER_LEVEL + 0.3 + 0.85 * coastNoise(x, z, 17, mix(salt + 7));
            double outer = lakeDistance - lakeRadius;
            double inner = islandRadius - islandDistance;
            double shoreVariation = 0.5 + 0.5 * coastNoise(x, z, 30, mix(salt + 8));
            double islandVariation = 0.5 + 0.5 * coastNoise(x, z, 15, mix(salt + 9));
            // Land height varies independently of the shoreline. Keep its short,
            // low bank from becoming a continuous series of concentric terraces.
            double bank = waterline + (outer >= 0 ? forest - waterline : waterline - bed)
                    * Math.tanh(outer / (outer >= 0 ? 3 + 5 * shoreVariation : 12 + 9 * shoreVariation));
            double islandBank = waterline + (inner >= 0 ? island - waterline : waterline - bed)
                    * Math.tanh(inner / (inner >= 0 ? 2 + 3 * islandVariation : 9 + 7 * islandVariation));
            return Math.max(bed, Math.max(bank, islandBank));
        }
    }

    public record Fields(DensityFunction continents, DensityFunction temperature,
                         DensityFunction humidity, DensityFunction ridges, DensityFunction baselineTerrain) {
        public Fields readOnly() {
            return mapAll(function -> {
                if (function instanceof DensityFunctions.HolderHolder holder) return holder.function().value();
                if (function instanceof DensityFunctions.MarkerOrMarked marker) return marker.wrapped();
                return function;
            });
        }

        public Fields mapAll(DensityFunction.Visitor visitor) {
            // Candidate eligibility must not depend on a chunk's old-world Blender.
            // The actual terrain still receives the ordinary visitor and its blending.
            DensityFunction terrain = baselineTerrain.mapAll(new DensityFunction.Visitor() {
                @Override
                public DensityFunction apply(DensityFunction function) {
                    if (function == DensityFunctions.blendAlpha()) return DensityFunctions.constant(1);
                    if (function == DensityFunctions.blendOffset()) return DensityFunctions.constant(0);
                    return visitor.apply(function);
                }

                @Override
                public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noise) {
                    return visitor.visitNoise(noise);
                }
            });
            return new Fields(continents.mapAll(visitor), temperature.mapAll(visitor),
                    humidity.mapAll(visitor), ridges.mapAll(visitor), terrain);
        }
    }

    private static final class Cache {
        final long[] keys = new long[256];
        final Sample[] columns = new Sample[256];
        boolean initialized;
        int cellX, cellZ;
        Site site;
    }
}
