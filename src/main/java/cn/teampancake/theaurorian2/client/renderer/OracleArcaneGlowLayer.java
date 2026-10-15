package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleArcaneEntity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Painted energy cores, travelling highlights and a bounded, angular edge glow. */
public final class OracleArcaneGlowLayer extends GeoRenderLayer<OracleArcaneEntity, Void, GeoRenderState> {
    private static final DataTicket<Integer> KIND = DataTickets.create("theaurorian2:oracle_glow_kind", Integer.class);
    private static final DataTicket<Float> AGE = DataTickets.create("theaurorian2:oracle_glow_age", Float.class);
    private static final String[] TEXTURES = {"shield", "blast", "trail", "trail", "clock", "repulsor"};
    private static final RenderType[] SURFACE = new RenderType[6], HALO = new RenderType[6];
    static {
        var pipeline = OracleGlowRender.pipeline("oracle_skill_glow");
        for (int i = 0; i < TEXTURES.length; i++) {
            var texture = TheAurorian2.id("textures/entity/oracle_arcane_" + TEXTURES[i] + ".png");
            SURFACE[i] = RenderType.create("oracle_skill_surface_" + i, RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", texture).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload().createRenderSetup());
            HALO[i] = RenderType.create("oracle_skill_halo_" + i, RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", texture).sortOnUpload().createRenderSetup());
        }
    }
    // Keyed by the six model kinds and their bone names, never by individual entities.
    private final List<Map<String, Part>> parts = List.of(new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>());

    public OracleArcaneGlowLayer(GeoRenderer<OracleArcaneEntity, Void, GeoRenderState> renderer) { super(renderer); }

    @Override public void addRenderData(OracleArcaneEntity entity, Void related, GeoRenderState state, float partialTick) {
        state.addGeckolibData(KIND, entity.kind() == OracleArcaneEntity.REPULSOR ? 5 : Math.min(entity.kind(), 4));
        state.addGeckolibData(AGE, entity.age(partialTick));
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> register) {
        if (OracleGlowRender.shadowPass()) return;
        int kind = pass.getGeckolibData(KIND);
        Map<String, Part> cache = parts.get(kind);
        for (GeoBone bone : pass.model().boneLookup().get().values()) {
            if (!(bone instanceof CuboidGeoBone cuboid) || cuboid.cubes.length == 0) continue;
            Part part = cache.get(bone.name());
            if (part == null || part.source != cuboid.cubes) {
                part = new Part(kind, bone.name(), cuboid.cubes);
                cache.put(bone.name(), part);
            }
            register.accept(bone, part.renderer);
        }
    }

    private static final class Part {
        final int kind;
        final boolean shaft, orb;
        final GeoCube[] source;
        final GeoQuad[] mesh;
        final PerBoneRender<GeoRenderState> renderer = this::submit;

        Part(int kind, String name, GeoCube[] cubes) {
            this.kind = kind; source = cubes;
            shaft = kind == 5 && name.equals("h_beam");
            orb = kind == 1 && name.equals("h_body");
            var faces = new ArrayList<GeoQuad>();
            var stack = new PoseStack();
            for (GeoCube cube : cubes) {
                stack.pushPose();
                cube.translateToPivotPoint(stack); cube.rotate(stack); cube.translateAwayFromPivotPoint(stack);
                var pose = stack.last();
                for (GeoQuad face : cube.quads()) {
                    if (face == null) continue;
                    GeoVertex[] v = face.vertices();
                    boolean first = Math.abs(axis(v[0]) - axis(v[1])) > .001F;
                    float span = Math.abs(axis(v[0]) - axis(v[first ? 1 : 3]));
                    int rows = shaft ? (span > 1 ? 32 : 1) : orb ? 8 : 1;
                    var normal = pose.normal().transform(new Vector3f(face.normalX(), face.normalY(), face.normalZ()));
                    for (int row = 0; row < rows; row++) {
                        float a = row / (float)rows, b = (row + 1F) / rows;
                        GeoVertex[] strip = first
                                ? new GeoVertex[]{mix(v[0],v[1],a),mix(v[0],v[1],b),mix(v[3],v[2],b),mix(v[3],v[2],a)}
                                : new GeoVertex[]{mix(v[0],v[3],a),mix(v[1],v[2],a),mix(v[1],v[2],b),mix(v[0],v[3],b)};
                        for (int i = 0; i < 4; i++) {
                            GeoVertex p = strip[i];
                            var pos = pose.pose().transformPosition(new Vector3f(p.posX(),p.posY(),p.posZ()));
                            strip[i] = new GeoVertex(pos.x,pos.y,pos.z,p.texU(),p.texV());
                        }
                        faces.add(new GeoQuad(strip,normal.x,normal.y,normal.z,face.direction()));
                    }
                }
                stack.popPose();
            }
            mesh = faces.toArray(GeoQuad[]::new);
        }

        private float axis(GeoVertex v) { return shaft ? v.posZ() : v.posY(); }

        void submit(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector collector) {
            bone.translateAwayFromPivotPoint(pass.poseStack());
            float age = pass.getGeckolibData(AGE);
            collector.order(1).submitCustomGeometry(pass.poseStack(), SURFACE[kind], (pose, buffer) -> {
                if (OracleGlowRender.shadowPass()) return;
                for (GeoQuad face : mesh) for (GeoVertex v : face.vertices()) {
                    // A steady emissive floor preserves readability between passing crests.
                    float phase = shaft ? -v.posZ() * 1.7F - age * .22F
                            : orb ? (v.posY() - 17F / 16) * 9 - age * .16F : age * .10F;
                    float crest = Math.max(0, Mth.cos(phase));
                    crest *= crest * crest;
                    float alpha = shaft ? .07F + .14F * crest : orb ? .04F + .09F * crest
                            : kind == 4 ? .06F : .08F + .06F * crest;
                    int color = (int)(255 * alpha) << 24 | 0xFFFFFF;
                    buffer.addVertex(pose,v.posX(),v.posY(),v.posZ()).setColor(color).setUv(v.texU(),v.texV())
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setNormal(pose,face.normalX(),face.normalY(),face.normalZ());
                }
            });
            if ((!shaft && !orb) || (orb && source.length < 2)) return;
            collector.order(1).submitCustomGeometry(pass.poseStack(), HALO[kind], (pose, buffer) -> {
                if (OracleGlowRender.shadowPass()) return;
                // Three faint nested outlines fall off outside the original signed hull.
                // Keep its winding/culling, cyan core and UVs; never fill it double-sided.
                // Beam expansion is transverse only and inherits the prism's 1/8 scale.
                var stack = pass.poseStack();
                stack.pushPose(); stack.last().set(pose);
                for (int layer = 0; layer < 3; layer++) {
                    float scale = 1.04F + layer * .065F;
                    float alpha = layer == 0 ? .07F : layer == 1 ? .035F : .015F;
                    stack.pushPose();
                    float y = (shaft ? 15 : 17) / 16F;
                    stack.translate(0,y,0); stack.scale(scale,scale,shaft ? 1 : scale); stack.translate(0,-y,0);
                    source[shaft ? 0 : 1].render(stack,buffer,LightCoordsUtil.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,
                            (int)(255 * alpha) << 24 | 0xFFFFFF);
                    stack.popPose();
                }
                stack.popPose();
            });
        }
    }

    private static GeoVertex mix(GeoVertex a, GeoVertex b, float t) {
        return new GeoVertex(Mth.lerp(t,a.posX(),b.posX()),Mth.lerp(t,a.posY(),b.posY()),Mth.lerp(t,a.posZ(),b.posZ()),
                Mth.lerp(t,a.texU(),b.texU()),Mth.lerp(t,a.texV(),b.texV()));
    }
}
