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
import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Restrained original-texture light: a gold armor sheen and a slower, broader cyan flow. */
public final class OracleBodyGlowLayer extends GeoRenderLayer<RisingClockOracleEntity, Void, GeoRenderState> {
    private static final DataTicket<Float> CAST = DataTickets.create("theaurorian2:oracle_body_cast", Float.class);
    private static final RenderType SURFACE = RenderType.create("oracle_body_glow",
            RenderSetup.builder(OracleGlowRender.pipeline("oracle_body_glow"))
                    .withTexture("Sampler0", TheAurorian2.id("textures/entity/rising_clock_oracle.png"))
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).sortOnUpload().createRenderSetup());
    // Explicit model parts keep the dark sleeves, torso, cloth, weapon and rage feathers untouched.
    private final Part[] parts = {
            new Part("body", false, 1, 2), new Part("head", false, 1, 2, 3, 4),
            new Part("arm_right", false, 0, 2, 3), new Part("arm_left", false, 0, 2, 3),
            new Part("arm_right_2", true, 0), new Part("arm_left_2", true, 0),
            new Part("armor_front", true, 0), new Part("armor_back", true, 0),
            new Part("armor_right", true, 0), new Part("armor_left", true, 0)
    };

    public OracleBodyGlowLayer(GeoRenderer<RisingClockOracleEntity, Void, GeoRenderState> renderer) { super(renderer); }

    @Override public void addRenderData(RisingClockOracleEntity entity, Void related, GeoRenderState state, float partialTick) {
        int action = entity.action();
        boolean casting = entity.isAlive() && action != RisingClockOracleEntity.IDLE && action != RisingClockOracleEntity.GLIDE;
        state.addGeckolibData(CAST, casting ? Mth.clamp(entity.actionAge(partialTick) / 6, 0, 1) : 0F);
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> register) {
        var state = (LivingEntityRenderState)(Object)pass.renderState();
        if (state.isInvisible || state.deathTime > 0 || OracleGlowRender.shadowPass()) return;
        var bones = pass.model().boneLookup().get();
        for (Part part : parts) if (bones.get(part.name) instanceof CuboidGeoBone bone) {
            if (part.indices[part.indices.length - 1] >= bone.cubes.length) continue;
            if (part.source != bone.cubes) part.bake(bone.cubes);
            register.accept(bone, part.renderer);
        }
    }

    private static final class Part {
        final String name;
        final boolean cyan;
        final int[] indices;
        final PerBoneRender<GeoRenderState> renderer = this::submit;
        GeoCube[] source;
        GeoQuad[] faces = new GeoQuad[0];
        float low, height;

        Part(String name, boolean cyan, int... indices) { this.name = name; this.cyan = cyan; this.indices = indices; }

        void submit(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector collector) {
            bone.translateAwayFromPivotPoint(pass.poseStack());
            float age = (float)pass.renderState().getAnimatableAge(), cast = pass.getGeckolibData(CAST);
            GeoQuad[] mesh = faces;
            float bottom = low, span = height;
            collector.order(1).submitCustomGeometry(pass.poseStack(), SURFACE, (pose, buffer) -> {
                if (OracleGlowRender.shadowPass()) return;
                // The wave leaves the surface before wrapping. No all-over brightness pulse.
                float sweep = (age % (cyan ? 72 : 96)) / (cyan ? 72 : 96) * 1.6F - .3F;
                for (GeoQuad face : mesh) for (GeoVertex v : face.vertices()) {
                    float progress = (v.posY() - bottom) / span;
                    if (!cyan) progress += v.posX() * .16F; // Gold catches a narrow diagonal highlight.
                    float d = progress - sweep;
                    float crest = (float)Math.exp(-d * d / (cyan ? .04F : .014F));
                    float strength = cyan ? .24F + .20F * crest + .07F * cast : .12F + .22F * crest + .04F * cast;
                    // Sample the painted pixels themselves, including their shading and gold edging.
                    int color = (int)(255 * strength) << 24 | 0xFFFFFF;
                    buffer.addVertex(pose,v.posX(),v.posY(),v.posZ()).setColor(color).setUv(v.texU(),v.texV())
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setNormal(pose,face.normalX(),face.normalY(),face.normalZ());
                }
            });
        }

        void bake(GeoCube[] cubes) {
            var result = new ArrayList<GeoQuad>();
            var transform = new PoseStack();
            low = Float.POSITIVE_INFINITY;
            float top = Float.NEGATIVE_INFINITY;
            for (int index : indices) {
                GeoCube cube = cubes[index];
                transform.pushPose();
                cube.translateToPivotPoint(transform); cube.rotate(transform); cube.translateAwayFromPivotPoint(transform);
                var pose = transform.last();
                for (GeoQuad face : cube.quads()) {
                    if (face == null) continue;
                    GeoVertex[] v = face.vertices();
                    boolean vertical = Math.abs(v[0].posY() - v[1].posY()) > .001F;
                    float length = Math.abs(v[0].posY() - v[vertical ? 1 : 3].posY());
                    int rows = Mth.clamp(Mth.ceil(length * 16), 1, 16);
                    Vector3f normal = pose.normal().transform(new Vector3f(face.normalX(),face.normalY(),face.normalZ()));
                    for (int row = 0; row < rows; row++) {
                        float a = row / (float)rows, b = (row + 1F) / rows;
                        GeoVertex[] strip = vertical
                                ? new GeoVertex[]{mix(v[0],v[1],a),mix(v[0],v[1],b),mix(v[3],v[2],b),mix(v[3],v[2],a)}
                                : new GeoVertex[]{mix(v[0],v[3],a),mix(v[1],v[2],a),mix(v[1],v[2],b),mix(v[0],v[3],b)};
                        for (int i = 0; i < strip.length; i++) {
                            GeoVertex p = strip[i];
                            var pos = pose.pose().transformPosition(new Vector3f(p.posX(),p.posY(),p.posZ()));
                            strip[i] = new GeoVertex(pos.x,pos.y,pos.z,p.texU(),p.texV());
                            low = Math.min(low,pos.y); top = Math.max(top,pos.y);
                        }
                        result.add(new GeoQuad(strip,normal.x,normal.y,normal.z,face.direction()));
                    }
                }
                transform.popPose();
            }
            height = Math.max(.01F, top - low);
            faces = result.toArray(GeoQuad[]::new);
            source = cubes;
        }
    }

    private static GeoVertex mix(GeoVertex a, GeoVertex b, float t) {
        return new GeoVertex(Mth.lerp(t,a.posX(),b.posX()),Mth.lerp(t,a.posY(),b.posY()),Mth.lerp(t,a.posZ(),b.posZ()),
                Mth.lerp(t,a.texU(),b.texU()),Mth.lerp(t,a.texV(),b.texV()));
    }
}
