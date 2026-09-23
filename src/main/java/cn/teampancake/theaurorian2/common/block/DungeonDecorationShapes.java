package cn.teampancake.theaurorian2.common.block;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shared, immutable surface shapes. No texture sampling or allocations in collision/render loops. */
final class DungeonDecorationShapes {
    private static final Map<String, List<Map<Direction, VoxelShape>>> BAKED = load();

    private DungeonDecorationShapes() {}

    static List<Map<Direction, VoxelShape>> get(String id, boolean aligned) {
        return Objects.requireNonNull(BAKED.get(id + (aligned ? "/aligned" : "/legacy")), id);
    }

    private static Map<String, List<Map<Direction, VoxelShape>>> load() {
        var result = new HashMap<String, List<Map<Direction, VoxelShape>>>();
        try (var stream = DungeonDecorationShapes.class.getResourceAsStream("/assets/theaurorian2/shapes/dungeon_decorations.bin.gz");
                var input = new DataInputStream(new GZIPInputStream(Objects.requireNonNull(stream, "Dungeon decoration shapes")))) {
            if (input.readInt() != 0x41555244 || input.readUnsignedShort() != 1) throw new IOException("Invalid shape format");
            int layouts = input.readUnsignedShort();
            for (int layout = 0; layout < layouts; layout++) {
                String name = input.readUTF();
                int count = input.readUnsignedShort();
                var parts = new ArrayList<Map<Direction, VoxelShape>>(count);
                for (int part = 0; part < count; part++) {
                    int x = input.readShort(), y = input.readShort(), z = input.readShort();
                    int sx = input.readShort(), sy = input.readShort(), sz = input.readShort();
                    int bytes = input.readInt();
                    if (sx <= 0 || sy <= 0 || sz <= 0 || sx > 256 || sy > 256 || sz > 256
                            || bytes != (sx * sy * sz + 7) / 8) throw new IOException("Invalid shape bounds: " + name);
                    byte[] packed = input.readNBytes(bytes);
                    if (packed.length != bytes) throw new IOException("Truncated shape: " + name);
                    BitSet bits = BitSet.valueOf(packed);
                    var grid = new BitSetDiscreteVoxelShape(sx, sy, sz);
                    for (int index = bits.nextSetBit(0); index >= 0; index = bits.nextSetBit(index + 1)) {
                        grid.fill(index / (sy * sz), index / sz % sy, index % sz);
                    }
                    VoxelShape shape = bits.isEmpty() ? Shapes.empty() : new SurfaceShape(grid, x, y, z, sx, sy, sz);
                    parts.add(Shapes.rotateHorizontal(shape));
                }
                result.put(name, List.copyOf(parts));
            }
            if (input.read() != -1) throw new IOException("Trailing shape data");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load dungeon decoration geometry", exception);
        }
        return Map.copyOf(result);
    }

    private static final class SurfaceShape extends ArrayVoxelShape {
        SurfaceShape(BitSetDiscreteVoxelShape grid, int x, int y, int z, int sx, int sy, int sz) {
            super(grid, coordinates(x, sx), coordinates(y, sy), coordinates(z, sz));
        }

        private static double[] coordinates(int start, int size) {
            double[] result = new double[size + 1];
            for (int i = 0; i <= size; i++) result[i] = (start + i) / 32.0;
            return result;
        }
    }
}
