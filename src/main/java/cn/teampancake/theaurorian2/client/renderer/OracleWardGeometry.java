package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.LightCoordsUtil;

/** One model-pixel extrusion, baked only when entity renderers reload their resources. */
final class OracleWardGeometry implements SubmitNodeCollector.CustomGeometryRenderer {
    static final Identifier TEXTURE = TheAurorian2.id("textures/entity/oracle_ward.png");
    private static final float HALF_WIDTH = .55F, HALF_DEPTH = 1/32F;
    private final float height, u0, v0, u1, v1;
    private final Edge[] edges;

    private OracleWardGeometry(NativeImage image) {
        int width = image.getWidth(), rows = image.getHeight();
        int[] pixels = image.getPixels();
        int left = width, right = 0, top = rows, bottom = 0;
        for (int y=0;y<rows;y++) for (int x=0;x<width;x++) if (solid(pixels,width,rows,x,y)) {
            left=Math.min(left,x); right=Math.max(right,x+1); top=Math.min(top,y); bottom=Math.max(bottom,y+1);
        }
        if (right<=left || bottom<=top) { height=u0=v0=u1=v1=0; edges=new Edge[0]; return; }
        float scale=2*HALF_WIDTH/(right-left);
        height=(bottom-top)*scale; u0=(float)left/width; v0=(float)top/rows; u1=(float)right/width; v1=(float)bottom/rows;
        List<Edge> rim = new ArrayList<>();
        // Merge contiguous edges on the same pixel row/column. Each samples its
        // original border texels, including the notch, pointed tip and any holes.
        for (int y=top;y<bottom;y++) for (int direction=-1;direction<=1;direction+=2) {
            int start=-1;
            for (int x=left;x<=right;x++) {
                boolean exposed=x<right && solid(pixels,width,rows,x,y) && !solid(pixels,width,rows,x,y+direction);
                if (exposed && start<0) start=x;
                if (!exposed && start>=0) {
                    float ax=direction<0?x:start, bx=direction<0?start:x, edgeY=y+(direction>0?1:0);
                    rim.add(new Edge((ax-left)*scale-HALF_WIDTH,(bottom-edgeY)*scale,
                            (bx-left)*scale-HALF_WIDTH,(bottom-edgeY)*scale,
                            (ax+(direction<0?-.01F:.01F))/width,(y+.5F)/rows,
                            (bx+(direction<0?.01F:-.01F))/width,(y+.5F)/rows,0,-direction));
                    start=-1;
                }
            }
        }
        for (int x=left;x<right;x++) for (int direction=-1;direction<=1;direction+=2) {
            int start=-1;
            for (int y=top;y<=bottom;y++) {
                boolean exposed=y<bottom && solid(pixels,width,rows,x,y) && !solid(pixels,width,rows,x+direction,y);
                if (exposed && start<0) start=y;
                if (!exposed && start>=0) {
                    float ay=direction>0?y:start, by=direction>0?start:y, edgeX=x+(direction>0?1:0);
                    rim.add(new Edge((edgeX-left)*scale-HALF_WIDTH,(bottom-ay)*scale,
                            (edgeX-left)*scale-HALF_WIDTH,(bottom-by)*scale,
                            (x+.5F)/width,(ay+(direction>0?-.01F:.01F))/rows,
                            (x+.5F)/width,(by+(direction>0?.01F:-.01F))/rows,direction,0));
                    start=-1;
                }
            }
        }
        edges=rim.toArray(Edge[]::new);
    }
    static OracleWardGeometry load(ResourceManager resources) {
        try (var stream=resources.open(TEXTURE); var image=NativeImage.read(stream)) { return new OracleWardGeometry(image); }
        catch (IOException exception) { throw new IllegalStateException("Cannot bake Oracle ward texture",exception); }
    }
    private static boolean solid(int[] pixels, int width, int height, int x, int y) {
        return x>=0 && y>=0 && x<width && y<height && (pixels[y*width+x]>>>24)>=26;
    }
    @Override public void render(PoseStack.Pose pose, VertexConsumer buffer) {
        if (height==0 || OracleGlowRender.shadowPass()) return;
        vertex(pose,buffer,HALF_WIDTH,0,HALF_DEPTH,u1,v1,0,0,1);
        vertex(pose,buffer,HALF_WIDTH,height,HALF_DEPTH,u1,v0,0,0,1);
        vertex(pose,buffer,-HALF_WIDTH,height,HALF_DEPTH,u0,v0,0,0,1);
        vertex(pose,buffer,-HALF_WIDTH,0,HALF_DEPTH,u0,v1,0,0,1);
        vertex(pose,buffer,HALF_WIDTH,0,-HALF_DEPTH,u1,v1,0,0,-1);
        vertex(pose,buffer,-HALF_WIDTH,0,-HALF_DEPTH,u0,v1,0,0,-1);
        vertex(pose,buffer,-HALF_WIDTH,height,-HALF_DEPTH,u0,v0,0,0,-1);
        vertex(pose,buffer,HALF_WIDTH,height,-HALF_DEPTH,u1,v0,0,0,-1);
        for (Edge edge : edges) {
            vertex(pose,buffer,edge.x0,edge.y0,HALF_DEPTH,edge.u0,edge.v0,edge.nx,edge.ny,0);
            vertex(pose,buffer,edge.x0,edge.y0,-HALF_DEPTH,edge.u0,edge.v0,edge.nx,edge.ny,0);
            vertex(pose,buffer,edge.x1,edge.y1,-HALF_DEPTH,edge.u1,edge.v1,edge.nx,edge.ny,0);
            vertex(pose,buffer,edge.x1,edge.y1,HALF_DEPTH,edge.u1,edge.v1,edge.nx,edge.ny,0);
        }
    }
    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z,
            float u, float v, float nx, float ny, float nz) {
        buffer.addVertex(pose,x,y,z).setColor(-1).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose,nx,ny,nz);
    }
    private record Edge(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float nx, float ny) {}
}
