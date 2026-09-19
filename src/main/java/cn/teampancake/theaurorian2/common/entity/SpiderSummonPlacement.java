package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Small, loaded-chunk-only search shared by egg laying, hatching and reinforcements. */
final class SpiderSummonPlacement {

    private SpiderSummonPlacement() {}

    static @Nullable Vec3 find(ServerLevel level, Entity entity, Vec3 preferred) {
        for (int sample = 0; sample < 17; sample++) {
            double angle = (sample - 1) % 8 * Math.PI / 4.0;
            double radius = sample == 0 ? 0.0 : sample <= 8 ? 1.0 : 2.0;
            double x = preferred.x + Math.cos(angle) * radius;
            double z = preferred.z + Math.sin(angle) * radius;
            for (int step = 0; step < 7; step++) {
                int dy = step == 0 ? 0 : (step + 1) / 2 * (step % 2 == 1 ? -1 : 1);
                BlockPos floor = BlockPos.containing(x, Math.floor(preferred.y) + dy - 1, z);
                if (!level.hasChunkAt(floor) || !level.isInWorldBounds(floor)) {
                    continue;
                }
                var support = level.getBlockState(floor).getCollisionShape(level, floor);
                if (support.isEmpty() || !level.getFluidState(floor).isEmpty()) {
                    continue;
                }
                Vec3 position = new Vec3(x, floor.getY() + support.max(net.minecraft.core.Direction.Axis.Y), z);
                AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(position);
                if (level.hasChunksAt(BlockPos.containing(box.minX, box.minY, box.minZ),
                                BlockPos.containing(box.maxX, box.maxY, box.maxZ))
                        && level.getWorldBorder().isWithinBounds(box)
                        && !level.containsAnyLiquid(box)
                        && level.noCollision(entity, box)) {
                    return position;
                }
            }
        }
        return null;
    }
}
