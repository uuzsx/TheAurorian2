package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** The imported ground-contact silhouette, independent of the large airborne ornamentation. */
final class RunestoneDungeonTerrain {
    static final int BLEND = 24;
    static final int DEPTH = 12;
    static final int CENTER_X = 156;
    static final int CENTER_Z = 144;
    private static final int MIN_X = 82 - BLEND;
    private static final int MIN_Z = 90 - BLEND;
    private static final int WIDTH = 122 + BLEND * 2;
    private static final int DEPTH_Z = 124 + BLEND * 2;
    private static final byte[] DISTANCES = loadDistances();

    private RunestoneDungeonTerrain() {}

    static int distance(int x, int z) {
        int dx = x - MIN_X;
        int dz = z - MIN_Z;
        return dx < 0 || dx >= WIDTH || dz < 0 || dz >= DEPTH_Z
                ? BLEND : DISTANCES[dz * WIDTH + dx];
    }

    static BlockPos worldPosition(BlockPos anchor, Rotation rotation, int x, int y, int z) {
        return anchor.offset(new BlockPos(x - CENTER_X, y, z - CENTER_Z).rotate(rotation));
    }

    static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    static BoundingBox groundBox(BlockPos anchor, Rotation rotation, int column, int row) {
        int minX = Math.max(MIN_X, column * 120);
        int minZ = Math.max(MIN_Z, row * 120);
        int maxX = Math.min(MIN_X + WIDTH - 1, column * 120 + 119);
        int maxZ = Math.min(MIN_Z + DEPTH_Z - 1, row * 120 + 119);
        if (minX > maxX || minZ > maxZ) {
            return null;
        }
        BlockPos a = worldPosition(anchor, rotation, minX, -BLEND - 4, minZ);
        BlockPos b = worldPosition(anchor, rotation, maxX, BLEND, maxZ);
        return BoundingBox.fromCorners(a, b);
    }

    static void blend(WorldGenLevel level, BoundingBox chunkBox, BlockPos anchor, Rotation rotation,
                      int column, int row) {
        BoundingBox groundBox = groundBox(anchor, rotation, column, row);
        if (groundBox == null || !groundBox.intersects(chunkBox)) {
            return;
        }
        Rotation inverse = inverse(rotation);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int baseY = anchor.getY();
        for (int x = Math.max(chunkBox.minX(), groundBox.minX()); x <= Math.min(chunkBox.maxX(), groundBox.maxX()); x++) {
            for (int z = Math.max(chunkBox.minZ(), groundBox.minZ()); z <= Math.min(chunkBox.maxZ(), groundBox.maxZ()); z++) {
                BlockPos local = new BlockPos(x - anchor.getX(), 0, z - anchor.getZ()).rotate(inverse);
                int distance = distance(local.getX() + CENTER_X, local.getZ() + CENTER_Z);
                if (distance >= BLEND) {
                    continue;
                }
                // Surface structures run before vegetation. Each template owns disjoint columns,
                // so this height never includes another fragment of the same dungeon.
                int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                double t = (double) distance / BLEND;
                double weight = t * t * (3.0 - 2.0 * t);
                int target = (int) Math.round(baseY + (surface - baseY) * weight);
                target = Math.clamp(target, baseY - BLEND, baseY + BLEND);
                int bottom = Math.max(level.getMinY(), Math.min(target - 4, baseY - DEPTH));
                int top = Math.min(level.getMaxY() - 1, Math.min(baseY + BLEND, Math.max(surface, target) + 2));
                for (int y = bottom; y <= top; y++) {
                    if (!chunkBox.isInside(cursor.set(x, y, z))) {
                        continue;
                    }
                    if (y > target) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                    } else if (y == target && distance > 0) {
                        level.setBlock(cursor, ModBlocks.AURORIAN_GRASS_BLOCK.get().defaultBlockState(), 2);
                    } else if (y >= target - 3 && distance > 0) {
                        level.setBlock(cursor, ModBlocks.AURORIAN_DIRT.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(cursor, ModBlocks.AURORIAN_STONE.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static byte[] loadDistances() {
        byte[] distances = new byte[WIDTH * DEPTH_Z];
        Arrays.fill(distances, (byte) BLEND);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        String resource = "/data/theaurorian2/structure/runestone_dungeon/foundation.dat";
        try (var stream = RunestoneDungeonTerrain.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("Missing " + resource);
            DataInputStream input = new DataInputStream(stream);
            String magic = new String(input.readNBytes(8), StandardCharsets.US_ASCII);
            int minX = input.readInt(), minZ = input.readInt(), width = input.readInt(), depth = input.readInt();
            if (!magic.equals("AURORD01") || minX != 82 || minZ != 90 || width != 122 || depth != 124) {
                throw new IOException("Invalid dungeon foundation header");
            }
            byte[] mask = input.readNBytes((width * depth + 7) / 8);
            if (mask.length != (width * depth + 7) / 8) throw new IOException("Truncated foundation mask");
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    int bit = z * width + x;
                    if ((mask[bit / 8] & (1 << (bit % 8))) != 0) {
                        int index = (z + BLEND) * WIDTH + x + BLEND;
                        distances[index] = 0;
                        queue.add(index);
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load runestone dungeon foundation", exception);
        }
        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            int distance = distances[index] + 1;
            if (distance >= BLEND) continue;
            if (index % WIDTH > 0) expand(distances, queue, index - 1, distance);
            if (index % WIDTH < WIDTH - 1) expand(distances, queue, index + 1, distance);
            if (index >= WIDTH) expand(distances, queue, index - WIDTH, distance);
            if (index + WIDTH < distances.length) expand(distances, queue, index + WIDTH, distance);
        }
        return distances;
    }

    private static void expand(byte[] distances, ArrayDeque<Integer> queue, int index, int value) {
        if (distances[index] > value) {
            distances[index] = (byte) value;
            queue.addLast(index);
        }
    }
}
