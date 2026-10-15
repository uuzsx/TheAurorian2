package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

/** Original-texture light moving along the staff's three blue blades, in the animated weapon pose. */
public final class OracleWeaponLayer extends GeoRenderLayer<RisingClockOracleEntity, Void, GeoRenderState> {
    private static final DataTicket<Float> CAST = DataTickets.create("theaurorian2:oracle_weapon_cast", Float.class);
    private static final RenderType SURFACE;
    static {
        // Preserve Iris's program mapping. Alpha-weighted addition keeps the painted
        // blue/gold variation visible instead of covering it with a flat glint texture.
        var pipeline = OracleGlowRender.pipeline("oracle_weapon");
        SURFACE = RenderType.create("oracle_weapon_flow", RenderSetup.builder(pipeline)
                .withTexture("Sampler0", TheAurorian2.id("textures/entity/rising_clock_oracle.png"))
                // The subdivided overlay must not compete with the unsplit base face.
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .sortOnUpload().createRenderSetup());
    }
    private final PerBoneRender<GeoRenderState> weaponRenderer = this::submitWeapon;
    private GeoCube cachedBlade;
    private Slice[] slices = new Slice[0];

    public OracleWeaponLayer(GeoRenderer<RisingClockOracleEntity, Void, GeoRenderState> renderer) { super(renderer); }

    @Override public void addRenderData(RisingClockOracleEntity entity, Void related, GeoRenderState state, float partialTick) {
        int action = entity.action();
        boolean casting = entity.isAlive() && action != RisingClockOracleEntity.IDLE && action != RisingClockOracleEntity.GLIDE;
        state.addGeckolibData(CAST, casting ? Mth.clamp(entity.actionAge(partialTick) / 6, 0, 1) : 0F);
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> register) {
        var state = (LivingEntityRenderState)(Object)pass.renderState();
        if (state.isInvisible || state.deathTime > 0 || OracleGlowRender.shadowPass()) return;
        GeoBone bone = pass.model().boneLookup().get().get("weapon");
        if (bone instanceof CuboidGeoBone weapon && weapon.cubes.length == 12) {
            if (cachedBlade != weapon.cubes[3]) bakeSlices(weapon.cubes);
            register.accept(bone, weaponRenderer);
        }
    }

    private void submitWeapon(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector collector) {
        bone.translateAwayFromPivotPoint(pass.poseStack());
        float age = (float)pass.renderState().getAnimatableAge(), cast = pass.getGeckolibData(CAST);
        Slice[] currentSlices = slices;
        GeoCube[] cubes = ((CuboidGeoBone)bone).cubes;
        collector.order(1).submitCustomGeometry(pass.poseStack(), SURFACE, (pose, buffer) -> {
            if (OracleGlowRender.shadowPass()) return;
            float sweep = (age % 56) / 56 * 1.25F - .125F;
            for (Slice slice : currentSlices) {
                GeoQuad face = slice.face;
                for (GeoVertex vertex : face.vertices()) {
                    // Shared row edges get identical alpha, so no hard bands jump
                    // between pixels. Keep a steady base light throughout the cycle.
                    float distance = (vertex.posY() - 34.75F / 16) / (38F / 16) - sweep;
                    float crest = (float)Math.exp(-distance * distance / .003F);
                    int alpha = (int)(255 * (.16F + .08F * cast + .22F * crest));
                    vertex(pose, buffer, vertex.posX(), vertex.posY(), vertex.posZ(), vertex.texU(), vertex.texV(),
                            alpha << 24 | 0xFFFFFF, face.normalX(), face.normalY(), face.normalZ());
                }
            }
            // A steady, restrained glow connects the gold socket to the blade.
            // Cube 0 is the wooden/gold shaft and deliberately receives no overlay.
            var stack = pass.poseStack();
            stack.pushPose(); stack.last().set(pose);
            int color = (int)(255 * (.07F + .025F * cast)) << 24 | 0xFFFFFF;
            for (int i = 1; i < cubes.length; i++) if (i < 2 || i > 4) {
                stack.pushPose(); cubes[i].render(stack, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color); stack.popPose();
            }
            stack.popPose();
        });
    }

    /** Only subdivide baked blue faces once per model reload; UVs and winding stay exact. */
    private void bakeSlices(GeoCube[] cubes) {
        var result = new ArrayList<Slice>(120);
        for (int i = 2; i <= 4; i++) for (GeoQuad face : cubes[i].quads()) {
            if (face == null || face.direction().getAxis() != Direction.Axis.X) continue;
            GeoVertex[] v = face.vertices();
            boolean firstEdgeVertical = Math.abs(v[0].posY() - v[1].posY()) > .01F;
            int count = i == 3 ? 24 : 18;
            for (int row = 0; row < count; row++) {
                float a = row / (float)count, b = (row + 1F) / count;
                GeoVertex[] strip = firstEdgeVertical
                        ? new GeoVertex[]{mix(v[0],v[1],a),mix(v[0],v[1],b),mix(v[3],v[2],b),mix(v[3],v[2],a)}
                        : new GeoVertex[]{mix(v[0],v[3],a),mix(v[1],v[2],a),mix(v[1],v[2],b),mix(v[0],v[3],b)};
                result.add(new Slice(new GeoQuad(strip,face.normalX(),face.normalY(),face.normalZ(),face.direction()),
                        (strip[0].posY()+strip[1].posY()+strip[2].posY()+strip[3].posY())*.25F));
            }
        }
        slices = result.toArray(Slice[]::new);
        cachedBlade = cubes[3];
    }

    private static GeoVertex mix(GeoVertex a, GeoVertex b, float t) {
        return new GeoVertex(Mth.lerp(t,a.posX(),b.posX()),Mth.lerp(t,a.posY(),b.posY()),Mth.lerp(t,a.posZ(),b.posZ()),
                Mth.lerp(t,a.texU(),b.texU()),Mth.lerp(t,a.texV(),b.texV()));
    }
    private static void vertex(PoseStack.Pose p, VertexConsumer b, float x, float y, float z, float u, float v,
            int color, float nx, float ny, float nz) {
        b.addVertex(p,x,y,z).setColor(color).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(p,nx,ny,nz);
    }
    private record Slice(GeoQuad face, float height) {}
}
