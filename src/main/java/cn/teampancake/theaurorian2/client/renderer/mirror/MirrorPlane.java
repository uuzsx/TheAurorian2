package cn.teampancake.theaurorian2.client.renderer.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Source model aperture: 14 x 30 pixels, at local Z=15/16, facing north. */
public record MirrorPlane(BlockPos base, Direction facing, Vec3 center, Vec3 normal, Vec3 right) {
    public static final float HALF_WIDTH = 7F / 16;
    public static final float HALF_HEIGHT = 15F / 16;

    public MirrorPlane(BlockPos base, Direction facing) {
        this(base.immutable(), facing,
                Vec3.atBottomCenterOf(base).add(0, 1, 0).add(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(-7D / 16)),
                Vec3.atLowerCornerOf(facing.getUnitVec3i()),
                Vec3.atLowerCornerOf(facing.getClockWise().getUnitVec3i()));
    }

    public double distance(Vec3 eye) { return eye.subtract(center).dot(normal); }
    public Vec3 reflect(Vec3 eye) { return eye.subtract(normal.scale(2 * distance(eye))); }
    public AABB bounds() { return new AABB(base).expandTowards(0, 1, 0); }
    public Vec3 point(float x, float y) { return center.add(right.scale(x)).add(0, y, 0); }

    public Matrix4f viewRotation() {
        return new Matrix4f().lookAlong((float) normal.x, 0, (float) normal.z, 0, 1, 0);
    }

    public Matrix4f projection(Vec3 eye, float far, boolean zeroToOne) {
        float d = (float) distance(eye);
        float x = (float) eye.subtract(center).dot(right);
        float y = (float) (eye.y - center.y);
        return new Matrix4f().setFrustum(-HALF_WIDTH - x, HALF_WIDTH - x,
                -HALF_HEIGHT - y, HALF_HEIGHT - y, Math.max(.015F, d), far, zeroToOne);
    }
}
