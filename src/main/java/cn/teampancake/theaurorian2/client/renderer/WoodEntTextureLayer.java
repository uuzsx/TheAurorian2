package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.StarblightEntEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/** Rebind the same animated quads to existing log textures, without duplicating or repainting any atlas. */
public final class WoodEntTextureLayer extends CustomBoneTextureGeoLayer<StarblightEntEntity,Void,GeoRenderState> {
    private final Identifier[] textures;
    private GeoCube[] source;
    private List<GeoQuad[]> surfaces=List.of();

    public WoodEntTextureLayer(GeoRenderer<StarblightEntEntity,Void,GeoRenderState> renderer,String bone,StarblightEntEntity.Wood wood) {
        super(renderer,bone,TheAurorian2.id("textures/block/"+wood.logTexture()+".png"));
        textures=new Identifier[]{texture,TheAurorian2.id("textures/block/"+wood.logTexture()+"_top.png"),
                TheAurorian2.id("textures/entity/starblight_ent.png")};
    }
    @Override protected void renderBone(RenderPassInfo<GeoRenderState> pass,GeoBone bone,SubmitNodeCollector collector) {
        GeoCube[] cubes=((CuboidGeoBone)bone).cubes;
        if (source!=cubes) bake(cubes);
        var pose=pass.poseStack();pose.pushPose();bone.translateAwayFromPivotPoint(pose);
        int light=pass.packedLight(),overlay=pass.packedOverlay(),color=pass.renderColor();
        for (int i=0;i<surfaces.size();i++) {
            GeoQuad[] mesh=surfaces.get(i);if (mesh.length==0) continue;
            var type=getRenderType(pass.renderState(),textures[i]);if (type==null) continue;
            collector.submitCustomGeometry(pose,type,(snapshot,buffer)->{
                for (GeoQuad face:mesh) for (GeoVertex v:face.vertices()) {
                    buffer.addVertex(snapshot,v.posX(),v.posY(),v.posZ()).setColor(color).setUv(v.texU(),v.texV())
                            .setOverlay(overlay).setLight(light).setNormal(snapshot,face.normalX(),face.normalY(),face.normalZ());
                }
            });
        }
        pose.popPose();
    }
    private void bake(GeoCube[] cubes) {
        var bark=new ArrayList<GeoQuad>();var ends=new ArrayList<GeoQuad>();var face=new ArrayList<GeoQuad>();
        var groups=List.of(bark,ends,face);var transform=new PoseStack();boolean leg=boneName.startsWith("leg_");
        for (int index=0;index<cubes.length;index++) {
            GeoCube cube=cubes[index];transform.pushPose();
            cube.translateToPivotPoint(transform);cube.rotate(transform);cube.translateAwayFromPivotPoint(transform);
            for (GeoQuad quad:cube.quads()) {
                if (quad==null) continue;
                boolean innerFace=boneName.equals("body")&&index==1;
                int material=innerFace?2:!leg&&Math.abs(quad.normalY())>.5F?1:0;
                GeoVertex[] vertices=new GeoVertex[4];GeoVertex[] original=quad.vertices();
                for (int v=0;v<4;v++) {
                    GeoVertex old=original[v];float u=old.texU(),t=old.texV();
                    if (!innerFace) {
                        u=u*2-(material==1||leg?1:0);
                        t=t*2-(leg?.75F:0);
                    }
                    var p=transform.last().pose().transformPosition(new Vector3f(old.posX(),old.posY(),old.posZ()));
                    vertices[v]=new GeoVertex(p.x,p.y,p.z,u,t);
                }
                var n=transform.last().normal().transform(new Vector3f(quad.normalX(),quad.normalY(),quad.normalZ()));
                groups.get(material).add(new GeoQuad(vertices,n.x,n.y,n.z,quad.direction()));
            }
            transform.popPose();
        }
        surfaces=List.of(bark.toArray(GeoQuad[]::new),ends.toArray(GeoQuad[]::new),face.toArray(GeoQuad[]::new));source=cubes;
    }
}
