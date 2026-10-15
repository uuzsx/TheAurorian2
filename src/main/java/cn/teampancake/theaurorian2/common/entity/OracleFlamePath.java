package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The same bounded curves drive fire contact, client sprites and their light sources. */
public final class OracleFlamePath {
    public static final int STRANDS = 4, STEPS = 64;
    public static final double CONTACT_RADIUS = .45;
    private final Vec3[][] points = new Vec3[STRANDS][STEPS+1];
    private final int[] ends = new int[STRANDS];
    private long updatedAt = Long.MIN_VALUE;
    private AABB bounds;

    public void update(OracleArcaneEntity spell) {
        long now = spell.level().getGameTime();
        if (updatedAt == now) return;
        updatedAt = now;
        Vec3 origin = spell.position(), forward = spell.getLookAngle();
        double yaw = Math.toRadians(spell.getYRot());
        Vec3 right = new Vec3(Math.cos(yaw),0,Math.sin(yaw)), up = forward.cross(right);
        double minX=origin.x,minY=origin.y,minZ=origin.z,maxX=origin.x,maxY=origin.y,maxZ=origin.z;
        double phase = spell.age(0)*.13;
        for (int strand=0;strand<STRANDS;strand++) {
            points[strand][0] = origin;
            ends[strand] = 0;
            for (int step=1;step<=STEPS;step++) {
                double distance = OracleArcaneEntity.FLAME_RANGE*step/STEPS;
                double radius = strand == 0 ? 0 : 1.5*Math.sin(Math.PI*step/STEPS);
                double angle = distance*Math.PI/4-phase+(strand-1)*Math.PI*2/3;
                double side=radius*Math.cos(angle), height=radius*Math.sin(angle);
                Vec3 point = new Vec3(origin.x+forward.x*distance+right.x*side+up.x*height,
                        origin.y+forward.y*distance+up.y*height,
                        origin.z+forward.z*distance+right.z*side+up.z*height);
                if (!spell.level().hasChunkAt(BlockPos.containing(point))) break;
                var hit = spell.level().clip(new ClipContext(points[strand][step-1],point,
                        ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,spell));
                if (hit.getType()!=HitResult.Type.MISS) point = hit.getLocation();
                points[strand][step] = point;
                ends[strand] = step;
                minX=Math.min(minX,point.x);minY=Math.min(minY,point.y);minZ=Math.min(minZ,point.z);
                maxX=Math.max(maxX,point.x);maxY=Math.max(maxY,point.y);maxZ=Math.max(maxZ,point.z);
                if (hit.getType()!=HitResult.Type.MISS) break;
            }
        }
        bounds = new AABB(minX,minY,minZ,maxX,maxY,maxZ).inflate(CONTACT_RADIUS);
    }

    public AABB bounds() { return bounds; }
    public int end(int strand) { return ends[strand]; }
    public Vec3 point(int strand,int step) { return points[strand][step]; }

    public boolean touches(AABB victim) {
        AABB expanded = victim.inflate(CONTACT_RADIUS);
        for (int strand=0;strand<STRANDS;strand++) {
            for (int step=1;step<=ends[strand];step++) {
                Vec3 a = points[strand][step-1], b = points[strand][step];
                if (expanded.contains(a) || expanded.clip(a,b).isPresent()) return true;
            }
        }
        return false;
    }
}
