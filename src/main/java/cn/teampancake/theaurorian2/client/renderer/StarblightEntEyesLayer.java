package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import cn.teampancake.theaurorian2.common.entity.StarblightEntEntity;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
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
import org.joml.Vector3f;

/** Two painted eye rectangles on the original animated face; the black face and bark stay lit normally. */
public final class StarblightEntEyesLayer extends GeoRenderLayer<StarblightEntEntity,Void,GeoRenderState> {
    private static final float[][] EYES={{3/32F,18/32F,6/32F,20/32F},{10/32F,18/32F,13/32F,20/32F}};
    private static final RenderType SURFACE=RenderType.create("starblight_ent_eyes",
            RenderSetup.builder(OracleGlowRender.pipeline("starblight_ent_eyes"))
                    .withTexture("Sampler0",TheAurorian2.id("textures/entity/starblight_ent.png"))
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
    // Opaque white texels replace the source's pink paint, so gray and blue stay neutral at any light level.
    private static final RenderType COLORED_SURFACE=RenderType.create("wood_ent_eyes",
            RenderSetup.builder(OracleGlowRender.pipeline("wood_ent_eyes").toBuilder()
                            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build())
                    .withTexture("Sampler0",TheAurorian2.id("textures/effect/solid_white.png"))
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
    private final RenderType surface;
    private final int color;
    private final PerBoneRender<GeoRenderState> eyesRenderer=this::submit;
    private GeoCube[] source;
    private GeoQuad[] eyes=new GeoQuad[0];

    public StarblightEntEyesLayer(GeoRenderer<StarblightEntEntity,Void,GeoRenderState> renderer,StarblightEntEntity.Wood wood) {
        super(renderer);
        surface=wood==StarblightEntEntity.Wood.STARBLIGHT?SURFACE:COLORED_SURFACE;
        color=switch (wood) {
            case STARBLIGHT,CURSED_FROST -> 0xFFFFFFFF;
            case SILENT_WOOD -> 0xFFB8E3FF;
            case CURTAIN_TREE -> 0xFFA0A0A0;
        };
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone,PerBoneRender<GeoRenderState>> register) {
        if (((LivingEntityRenderState)(Object)pass.renderState()).isInvisible||OracleGlowRender.shadowPass()) return;
        if (pass.model().boneLookup().get().get("body") instanceof CuboidGeoBone bone) {
            if (source!=bone.cubes) bake(bone.cubes);
            if (eyes.length>0) register.accept(bone,eyesRenderer);
        }
    }
    private void submit(RenderPassInfo<GeoRenderState> pass,GeoBone bone,SubmitNodeCollector collector) {
        bone.translateAwayFromPivotPoint(pass.poseStack());
        GeoQuad[] mesh=eyes;
        collector.order(1).submitCustomGeometry(pass.poseStack(),surface,(pose,buffer)->{
            if (OracleGlowRender.shadowPass()) return;
            for (GeoQuad face:mesh) for (GeoVertex vertex:face.vertices()) {
                buffer.addVertex(pose,vertex.posX(),vertex.posY(),vertex.posZ()).setColor(color)
                        .setUv(vertex.texU(),vertex.texV()).setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose,face.normalX(),face.normalY(),face.normalZ());
            }
        });
    }
    private void bake(GeoCube[] cubes) {
        var result=new ArrayList<GeoQuad>(2);var transform=new PoseStack();
        for (GeoCube cube:cubes) {
            transform.pushPose();cube.translateToPivotPoint(transform);cube.rotate(transform);cube.translateAwayFromPivotPoint(transform);
            for (GeoQuad face:cube.quads()) {
                if (face==null) continue;
                GeoVertex[] v=face.vertices();float minU=1,minV=1,maxU=0,maxV=0;
                for (GeoVertex vertex:v) { minU=Math.min(minU,vertex.texU());maxU=Math.max(maxU,vertex.texU());minV=Math.min(minV,vertex.texV());maxV=Math.max(maxV,vertex.texV()); }
                float du=v[1].texU()-v[0].texU(),dv=v[1].texV()-v[0].texV();
                float eu=v[3].texU()-v[0].texU(),ev=v[3].texV()-v[0].texV(),det=du*ev-dv*eu;
                if (Math.abs(det)<1e-8F) continue;
                for (float[] rect:EYES) {
                    if (minU>rect[0]||maxU<rect[2]||minV>rect[1]||maxV<rect[3]) continue;
                    GeoVertex[] patch=new GeoVertex[4];
                    for (int i=0;i<4;i++) {
                        float u=Math.clamp(v[i].texU(),rect[0],rect[2]),t=Math.clamp(v[i].texV(),rect[1],rect[3]);
                        float a=((u-v[0].texU())*ev-(t-v[0].texV())*eu)/det;
                        float b=(du*(t-v[0].texV())-dv*(u-v[0].texU()))/det;
                        var p=new Vector3f(v[0].posX()+a*(v[1].posX()-v[0].posX())+b*(v[3].posX()-v[0].posX()),
                                v[0].posY()+a*(v[1].posY()-v[0].posY())+b*(v[3].posY()-v[0].posY()),
                                v[0].posZ()+a*(v[1].posZ()-v[0].posZ())+b*(v[3].posZ()-v[0].posZ()));
                        transform.last().pose().transformPosition(p);patch[i]=new GeoVertex(p.x,p.y,p.z,u,t);
                    }
                    var n=transform.last().normal().transform(new Vector3f(face.normalX(),face.normalY(),face.normalZ()));
                    result.add(new GeoQuad(patch,n.x,n.y,n.z,face.direction()));
                }
            }
            transform.popPose();
        }
        eyes=result.toArray(GeoQuad[]::new);source=cubes;
    }
}
