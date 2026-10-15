package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleCoffinEntity;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** Flowing darkness with a violet advancing edge, a sealed hold and simultaneous shatter.
 * Surface light and shards use bounded cached meshes, without particle entities or terrain edits. */
public final class OracleCoffinRenderer extends EntityRenderer<OracleCoffinEntity, OracleCoffinRenderer.State> {
    // Iris maps these two vanilla parents separately: alpha for opaque black, EYES for light.
    public static final RenderPipeline PIPELINE = RenderPipelines.ENTITY_TRANSLUCENT_CULL.toBuilder()
            .withLocation(TheAurorian2.id("pipeline/oracle_black_coffin"))
            .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL,true)).withCull(false).build();
    public static final RenderPipeline GLOW_PIPELINE = OracleGlowRender.pipeline("oracle_coffin_flow")
            .toBuilder().withCull(false).build();
    private static final RenderType TYPE = type("oracle_black_coffin",PIPELINE);
    private static final RenderType GLOW = type("oracle_coffin_flow",GLOW_PIPELINE);
    private static final int STRIPS=32, FLOW_ROWS=56;
    private static final float[] FLOW_NOISE=createNoise();
    private static final int[] WALL_COLORS={0x030207,0x08050D,0x050309,0x0B0710,0x06040C,0x050309};
    private static final float[] FLOW_PERIOD={48,62,54,40,70,64}, FLOW_PHASE={0,.39F,.68F,.17F,.52F,.79F};
    private static final float[] FLOW_WIDTH={.012F,.030F,.016F,.008F,.035F,.024F};
    private static final int[] FLOW_COLORS={0xA881EC,0x9964DF,0x865AE2,0xAA7EEB,0xB294ED,0x8462B9};
    // Minecraft creates new render states each frame. Cache geometry by live entity instead;
    // extraction and deferred submission run sequentially on the render thread in this version.
    private final WeakHashMap<OracleCoffinEntity,Visual> visuals=new WeakHashMap<>();

    private static RenderType type(String name,RenderPipeline pipeline) {
        return RenderType.create(name,RenderSetup.builder(pipeline)
                .withTexture("Sampler0",TheAurorian2.id("textures/effect/solid_white.png")).useLightmap().useOverlay().createRenderSetup());
    }
    public OracleCoffinRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=0; }
    @Override public State createRenderState() { return new State(); }
    @Override protected AABB getBoundingBoxForCulling(OracleCoffinEntity entity) { return entity.volume().expandTowards(0,-50,0).inflate(4); }
    @Override public void extractRenderState(OracleCoffinEntity entity, State state, float partial) {
        super.extractRenderState(entity,state,partial);
        state.age=entity.age(partial); state.end=entity.dissolveAt();
        state.r=(float)entity.halfWidth(); state.h=(float)entity.height();
        state.seed=entity.getId();
        Visual visual=visuals.get(entity);
        if (visual==null) {
            visual=new Visual(state); visuals.put(entity,visual);
        }
        state.layout=visual.layout; state.mesh=visual.mesh; state.glow=visual.glow;
        state.flowColors=visual.flowColors;
        state.mesh.count=0; state.glow.count=0;
        build(state);
    }
    @Override public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.mesh==null || OracleGlowRender.shadowPass()) return;
        if (state.mesh.count>0) collector.submitCustomGeometry(stack,TYPE,state.mesh);
        // Custom geometry within one order is grouped in a HashMap; explicit ordering keeps
        // the opaque black layer from being drawn over its additive light.
        if (state.glow.count>0) collector.order(1).submitCustomGeometry(stack,GLOW,state.glow);
    }
    private static float unit(float x) { return Mth.clamp(x,0,1); }
    private static float smooth(float x) { x=unit(x); return x*x*(3-2*x); }
    private static float noise(int n) { n=(n^(n>>>16))*0x45d9f3b; n=(n^(n>>>16))*0x45d9f3b; return (n&65535)/65535F; }
    private static int color(float alpha,int rgb) { return (Mth.clamp((int)(alpha*255),0,255)<<24)|rgb; }

    private static float[] createNoise() {
        float[] values=new float[64*64];
        for(int i=0;i<values.length;i++) values[i]=noise(i*179+7187);
        return values;
    }
    private static float field(float x,float y) {
        int ix=Mth.floor(x),iy=Mth.floor(y);
        float fx=smooth(x-ix),fy=smooth(y-iy);
        return Mth.lerp(fy,Mth.lerp(fx,FLOW_NOISE[(iy&63)*64+(ix&63)],FLOW_NOISE[(iy&63)*64+((ix+1)&63)]),
                Mth.lerp(fx,FLOW_NOISE[((iy+1)&63)*64+(ix&63)],FLOW_NOISE[((iy+1)&63)*64+((ix+1)&63)]));
    }
    /** The Oracle's travelling Gaussian sheen, with distinct direction, width and timing per
     * face. Periodic distance keeps the wave continuous at wrap, without a full-face flash. */
    private static int flowColor(State s,int face,float x,float y,float age,float fade) {
        float u=x/(2*s.r),v=y/(face<4?s.h:2*s.r);
        float progress=switch(face) {
            case 0->v+u*.22F; case 1->1-v-u*.16F; case 2->.5F+u+v*.20F;
            case 3->v-u*.32F; case 4->.5F+u*.65F+v*.5F; default->1-v+u*.12F;
        };
        float phase=age/FLOW_PERIOD[face]+FLOW_PHASE[face];
        float distance=progress-((phase-Mth.floor(phase))*1.8F-.4F);
        distance-=1.8F*Math.round(distance/1.8F);
        float crest=(float)Math.exp(-distance*distance/FLOW_WIDTH[face]);
        float trail=(float)Math.exp(-(distance+.12F)*(distance+.12F)/.035F);
        float echo=face==3?(float)Math.exp(-(distance+.46F)*(distance+.46F)/.019F):0;
        return color((.018F+.27F*crest+.045F*trail+.075F*echo)*fade,FLOW_COLORS[face]);
    }
    private static float top(State s,int side,int i,float progress) {
        float n=noise(s.seed+side*197+i*41);
        // Slow lateral advection makes the leading tongues themselves move, without flicker.
        float swell=field(i*.29F+side*7-s.age*.025F,s.age*.03F);
        return Mth.clamp(s.h*(smooth(progress)+(n*.55F+swell*.45F-.43F)*.25F
                *(float)Math.sin(progress*Math.PI)),0,s.h);
    }

    private static void build(State s) {
        if (s.age>=s.end+OracleCoffinEntity.DISSOLVE_TICKS) return;
        if (s.end==OracleCoffinEntity.FINAL_STRIKE && s.age>=s.end) { fragments(s); return; }
        // Cancellation fades the surface already formed, without a burst or fresh growth.
        float fade=1-smooth((s.age-s.end)/10), a=Math.min(s.age,s.end), r=s.r, h=s.h;
        if (fade<=0) return;
        Mesh m=s.mesh,glow=s.glow;
        m.identity(r,h); glow.identity(r,h);
        float ground=r*(.15F+.85F*smooth(a/8));
        m.flat(-ground,-ground,ground,ground,.003F,color(.98F*fade,0x050309));
        float progress=unit((a-OracleCoffinEntity.GROW_START)/(OracleCoffinEntity.ROOF_START-OracleCoffinEntity.GROW_START));
        if (progress>0) for (int side=0;side<4;side++) {
            prepareFlow(s,side,FLOW_ROWS,h,a,fade);
            float previous=top(s,side,STRIPS-1,progress);
            for (int i=0;i<STRIPS;i++) {
                float height=top(s,side,i,progress);
                float x0=-r+2*r*i/STRIPS, x1=-r+2*r*(i+1)/STRIPS;
                m.wall(side,x0,x1,0,height,r,color(fade,WALL_COLORS[side]));
                surfaceColumn(s,side,i,FLOW_ROWS,h,height,a,fade);
                if (progress<1) {
                    float pulse=.7F+.3F*field(i*.33F-a*.11F,side*3+a*.025F);
                    rim(glow,side,x0,height,x1,height,fade*pulse);
                    if(i>0) rim(glow,side,x0,previous,x0,height,fade*pulse);
                }
                previous=height;
            }
            // Follow the actual grown height. The frame remains luminous through the hold.
            litEdge(glow,side,-r,0,-r,top(s,side,0,progress),a,fade);
            litEdge(glow,side,r,0,r,top(s,side,STRIPS-1,progress),a,fade);
            litEdge(glow,side,-r,0,r,0,a,fade);
            if(progress==1) litEdge(glow,side,-r,h,r,h,a,fade);
        }
        float roof=smooth((a-OracleCoffinEntity.ROOF_START)/(OracleCoffinEntity.SEAL-OracleCoffinEntity.ROOF_START));
        if (roof>0) {
            float inner=r*(1-roof); int c=color(fade,0x06040C);
            m.flat(-r,-r,r,-inner,h,c); m.flat(-r,inner,r,r,h,c);
            m.flat(-r,-inner,-inner,inner,h,c); m.flat(inner,-inner,r,inner,h,c);
            if(roof<1) {
                rim(glow,4,-inner,r-inner,inner,r-inner,fade);
                rim(glow,4,inner,r-inner,inner,r+inner,fade);
                rim(glow,4,inner,r+inner,-inner,r+inner,fade);
                rim(glow,4,-inner,r+inner,-inner,r-inner,fade);
            }
        }
        if (a>=OracleCoffinEntity.SEAL) {
            float capFade=fade*smooth((a-OracleCoffinEntity.SEAL)/3);
            for(int face=4;face<6;face++) {
                prepareFlow(s,face,STRIPS,2*r,a,capFade);
                for(int i=0;i<STRIPS;i++) surfaceColumn(s,face,i,STRIPS,2*r,2*r,a,capFade);
                litEdge(glow,face,-r,0,r,0,a,capFade);
                litEdge(glow,face,r,0,r,2*r,a,capFade);
                litEdge(glow,face,r,2*r,-r,2*r,a,capFade);
                litEdge(glow,face,-r,2*r,-r,0,a,capFade);
            }
        }
        // Reveal a fixed, sparse star field only on darkness that has already formed.
        for(Star star:s.layout.stars) {
            float reveal;
            if(star.face<4) {
                int first=Mth.clamp(Mth.floor((star.x-star.radius*3+r)/(2*r)*STRIPS),0,STRIPS-1);
                int last=Mth.clamp(Mth.floor((star.x+star.radius*3+r)/(2*r)*STRIPS),0,STRIPS-1);
                float ceiling=h;
                for(int i=first;i<=last;i++) ceiling=Math.min(ceiling,top(s,star.face,i,progress));
                reveal=smooth((ceiling-star.y-star.radius*3)/.55F);
            } else if(star.face==4) {
                float edge=Math.max(Math.abs(star.x),Math.abs(star.y-r));
                reveal=roof==0?0:smooth((edge-r*(1-roof)-star.radius*3)/.55F);
                if(roof==1) reveal=1;
            } else {
                float edge=Math.max(Math.abs(star.x),Math.abs(star.y-r));
                reveal=smooth((ground-edge-star.radius*3)/.55F);
            }
            starlight(glow,star,a,fade*reveal);
        }
    }

    private static void starlight(Mesh m,Star star,float age,float fade) {
        if(fade<=0) return;
        float wave=.5F+.5F*Mth.sin(age*star.speed+star.phase);
        float alpha=fade*star.brightness*(.38F+.62F*wave*wave);
        // Tiny lavender-white cores, soft violet halos and only a few subtle diffraction rays.
        diamond(m,star,star.radius*3,star.radius*3,alpha*.22F,0x9961D9);
        diamond(m,star,star.radius,star.radius,alpha,0xE7DDFF);
        if(star.flare) {
            diamond(m,star,star.radius*2.8F,star.radius*.22F,alpha*.48F,0xCCB7FC);
            diamond(m,star,star.radius*.22F,star.radius*3.6F,alpha*.48F,0xCCB7FC);
        }
    }

    private static void diamond(Mesh m,Star star,float rx,float ry,float alpha,int rgb) {
        int center=color(alpha,rgb),clear=color(0,rgb);
        for(int sign=-1;sign<=1;sign+=2) for(int i=0;i<4;i++) {
            int j=(i+1)%4;
            float x0=i==0?rx:i==2?-rx:0,y0=i==1?ry:i==3?-ry:0;
            float x1=j==0?rx:j==2?-rx:0,y1=j==1?ry:j==3?-ry:0;
            float n=sign*.045F;
            m.point(star.face,star.x,star.y,n,center);
            m.point(star.face,star.x+x0,star.y+y0,n,clear);
            m.point(star.face,star.x+x1,star.y+y1,n,clear);
            m.point(star.face,star.x+x1,star.y+y1,n,clear);
        }
    }

    private static void prepareFlow(State s,int face,int rows,float height,float age,float fade) {
        for(int j=0;j<=rows;j++) for(int i=0;i<=STRIPS;i++)
            s.flowColors[j*(STRIPS+1)+i]=flowColor(s,face,-s.r+2*s.r*i/STRIPS,height*j/rows,age,fade);
    }
    private static void surfaceColumn(State s,int face,int column,int rows,float height,float top,float age,float fade) {
        float x0=-s.r+2*s.r*column/STRIPS,x1=-s.r+2*s.r*(column+1)/STRIPS;
        for(int j=0;j<rows;j++) {
            float y0=height*j/rows,y1=Math.min(top,height*(j+1)/rows);
            if(y0>=top) break;
            int base=j*(STRIPS+1)+column,upper=base+STRIPS+1;
            int c0=s.flowColors[base],c1=s.flowColors[base+1],c2=s.flowColors[upper+1],c3=s.flowColors[upper];
            if(y1<height*(j+1)/rows) { c2=flowColor(s,face,x1,y1,age,fade); c3=flowColor(s,face,x0,y1,age,fade); }
            for(int sign=-1;sign<=1;sign+=2) {
                float n=sign*.024F;
                s.glow.point(face,x0,y0,n,c0); s.glow.point(face,x1,y0,n,c1);
                s.glow.point(face,x1,y1,n,c2); s.glow.point(face,x0,y1,n,c3);
            }
        }
    }

    private static void litEdge(Mesh m,int face,float x0,float y0,float x1,float y1,float age,float fade) {
        float length=Mth.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0));
        int steps=Mth.clamp(Mth.ceil(length),1,40);
        for(int i=0;i<steps;i++) {
            float a=i/(float)steps,b=(i+1F)/steps;
            float light=.64F+.28F*field(i*.35F-age*.09F,face*3+age*.015F);
            rim(m,face,Mth.lerp(a,x0,x1),Mth.lerp(a,y0,y1),Mth.lerp(b,x0,x1),Mth.lerp(b,y0,y1),fade*light);
        }
    }

    /** A soft purple fringe follows the advancing silhouette and structural edges. Vertex alpha blends out
     * on both sides; flowing intensity runs along the edge instead of flashing on and off. */
    private static void rim(Mesh m,int face,float x0,float y0,float x1,float y1,float alpha) {
        float dx=x1-x0,dy=y1-y0,length=Mth.sqrt(dx*dx+dy*dy);
        if(length<.001F) return;
        float px=-dy/length,py=dx/length;
        for(int band=0;band<5;band++) {
            float lo=switch(band){case 0->-.9F;case 1->-.27F;case 2->-.045F;case 3->.045F;default->.27F;};
            float hi=switch(band){case 0->-.27F;case 1->-.045F;case 2->.045F;case 3->.27F;default->.9F;};
            float al=switch(band){case 0->0;case 1,4->.19F;default->.70F;};
            float ah=switch(band){case 4->0;case 0,3->.19F;default->.70F;};
            int cl=color(alpha*al,band<2?0x7021BD:0xAD63F0),ch=color(alpha*ah,band>2?0x7021BD:0xAD63F0);
            for(int sign=-1;sign<=1;sign+=2) {
                float n=sign*.032F;
                m.point(face,x0+px*lo,y0+py*lo,n,cl); m.point(face,x1+px*lo,y1+py*lo,n,cl);
                m.point(face,x1+px*hi,y1+py*hi,n,ch); m.point(face,x0+px*hi,y0+py*hi,n,ch);
            }
        }
    }

    private static void fragments(State s) {
        float elapsed=s.age-s.end, fade=1-smooth((elapsed-5)/(OracleCoffinEntity.DISSOLVE_TICKS-5));
        float lightFade=fade*(1-smooth(elapsed/12));
        for (Plate plate:s.layout.plates) {
            // Every plate loses support at FINAL_STRIKE and falls together, without an outward blast.
            s.mesh.transform(plate,elapsed,s.r,s.h); s.glow.transform(plate,elapsed,s.r,s.h);
            int c=color(fade,WALL_COLORS[plate.face]);
            for (int i=0;i<plate.triangles.length;i+=3) {
                for (int k=0;k<4;k++) {
                    int n=plate.triangles[i+Math.min(k,2)];
                    float x=plate.x[n],y=plate.y[n];
                    s.mesh.point(plate.face,x,y,0,c);
                    if(lightFade>0) {
                        int light=flowColor(s,plate.face,x,y,s.age,lightFade);
                        // A soft surface sheen travels with the shard; no luminous crack edges.
                        s.glow.point(plate.face,x,y,.025F,light);
                    }
                }
                if(lightFade>0) for(int k=0;k<4;k++) {
                    int n=plate.triangles[i+Math.min(k,2)];
                    s.glow.point(plate.face,plate.x[n],plate.y[n],-.025F,flowColor(s,plate.face,plate.x[n],plate.y[n],s.age,lightFade));
                }
            }
            // Carry only the original box edges away with their own shards. Never leave a
            // stationary luminous frame behind, and never bring back internal crack outlines.
            if(elapsed<9) for(int i=0;i<plate.x.length;i++) {
                int j=(i+1)%plate.x.length;
                float x0=plate.x[i],x1=plate.x[j],y0=plate.y[i],y1=plate.y[j];
                float height=plate.face<4?s.h:2*s.r;
                boolean outside=(Math.abs(x0-x1)<.001F && Math.abs(Math.abs(x0)-s.r)<.001F)
                        || (Math.abs(y0-y1)<.001F && (Math.abs(y0)<.001F || Math.abs(y0-height)<.001F));
                if(outside) rim(s.glow,plate.face,x0,y0,x1,y1,(1-smooth(elapsed/9))*.75F);
            }
            if(lightFade>0) for(int i=0;i<plate.stars.size();i++) starlight(s.glow,plate.stars.get(i),s.age,lightFade);
        }
    }

    public static final class State extends EntityRenderState {
        Mesh mesh,glow; Layout layout; int[] flowColors; float age,r,h; int end,seed;
    }
    private static final class Visual {
        final Layout layout; final Mesh mesh,glow;
        final int[] flowColors=new int[(STRIPS+1)*(FLOW_ROWS+1)];
        Visual(State s) {
            layout=new Layout(s.seed,s.r,s.h);
            mesh=new Mesh(Math.max(4096,layout.plates.length*40));
            glow=new Mesh(190000);
        }
    }
    private static final class Layout {
        final Plate[] plates;
        final Star[] stars;
        Layout(int seed,float r,float h) {
            var plates=new ArrayList<Plate>();
            int cols=2,rows=5;
            for (int face=0;face<6;face++) {
                int ny=face<4?rows:cols; float height=face<4?h:2*r;
                float[][] x=new float[ny+1][cols+1],y=new float[ny+1][cols+1];
                for (int j=0;j<=ny;j++) for (int i=0;i<=cols;i++) {
                    int id=seed+face*3917+j*179+i*43;
                    x[j][i]=-r+(i+(i==0||i==cols?0:(noise(id)-.5F)*.6F))*2*r/cols;
                    y[j][i]=(j+(j==0||j==ny?0:(noise(id+917)-.5F)*.6F))*height/ny;
                }
                for (int j=0;j<ny;j++) for (int i=0;i<cols;i++) {
                    float[] px=new float[12],py=new float[12];
                    path(px,py,0,x[j][i],y[j][i],x[j][i+1],y[j][i+1],true);
                    path(px,py,3,x[j][i+1],y[j][i+1],x[j+1][i+1],y[j+1][i+1],false);
                    path(px,py,6,x[j+1][i+1],y[j+1][i+1],x[j+1][i],y[j+1][i],true);
                    path(px,py,9,x[j+1][i],y[j+1][i],x[j][i],y[j][i],false);
                    plates.add(new Plate(face,px,py,seed+face*7187+j*313+i*97));
                }
            }
            this.plates=plates.toArray(Plate[]::new);
            var stars=new ArrayList<Star>();
            for(int face=0;face<6;face++) {
                float height=face<4?h:2*r;
                int count=Mth.ceil(2*r*height*.18F);
                for(int i=0;i<count;i++) {
                    int id=seed+face*11779+i*419;
                    boolean flare=noise(id+607)>.91F;
                    float radius=flare?.11F+noise(id+881)*.035F:.05F+noise(id+881)*.045F;
                    float x=-r+.65F+noise(id)*(2*r-1.3F),y=.65F+noise(id+139)*(height-1.3F);
                    Star star=new Star(face,x,y,radius,.48F+noise(id+311)*.5F,
                            noise(id+577)*Mth.TWO_PI,.045F+noise(id+727)*.055F,flare);
                    for(Plate plate:this.plates) if(plate.face==face && plate.contains(x,y)) {
                        plate.stars.add(star); stars.add(star); break;
                    }
                }
            }
            this.stars=stars.toArray(Star[]::new);
        }
        private static void path(float[] x,float[] y,int k,float x0,float y0,float x1,float y1,boolean horizontal) {
            x[k]=x0; y[k]=y0;
            x[k+1]=horizontal?(x0+x1)*.5F:x0; y[k+1]=horizontal?y0:(y0+y1)*.5F;
            x[k+2]=horizontal?(x0+x1)*.5F:x1; y[k+2]=horizontal?y1:(y0+y1)*.5F;
        }
    }
    private record Star(int face,float x,float y,float radius,float brightness,float phase,float speed,boolean flare) {}
    private static final class Plate {
        final int face; final float[] x,y; final int[] triangles;
        final ArrayList<Star> stars=new ArrayList<>(2);
        final float cx,cy,spin,tilt,drift,sway,gravity;
        Plate(int face,float[] x,float[] y,int seed) {
            this.face=face; this.x=x; this.y=y;
            float sx=0,sy=0; for(int i=0;i<x.length;i++) { sx+=x[i]; sy+=y[i]; }
            cx=sx/x.length; cy=sy/y.length;
            spin=(noise(seed)-.5F)*.10F; tilt=(noise(seed+53)-.5F)*.14F;
            drift=(noise(seed+101)-.5F)*.028F; sway=(noise(seed+211)-.5F)*.024F;
            gravity=.035F+noise(seed+307)*.045F;
            triangles=triangulate(x,y);
        }
        private boolean contains(float px,float py) {
            boolean inside=false;
            for(int i=0,j=x.length-1;i<x.length;j=i++)
                if((y[i]>py)!=(y[j]>py) && px<(x[j]-x[i])*(py-y[i])/(y[j]-y[i])+x[i]) inside=!inside;
            return inside;
        }
        /** Ear clipping runs once, preserving concave stepped outlines exactly. */
        private static int[] triangulate(float[] x,float[] y) {
            int[] indices=new int[x.length],out=new int[(x.length-2)*3];
            int size=x.length,count=0; for(int i=0;i<size;i++) indices[i]=i;
            for(int attempt=0;size>2 && attempt<x.length*x.length;attempt++) {
                int at=attempt%size, a=indices[(at+size-1)%size],b=indices[at],c=indices[(at+1)%size];
                float cross=cross(x[a],y[a],x[b],y[b],x[c],y[c]);
                if (Math.abs(cross)<.00001F) { System.arraycopy(indices,at+1,indices,at,size-at-1); size--; attempt=-1; continue; }
                if (cross<0) continue;
                boolean occupied=false;
                for(int j=0;j<size;j++) {
                    int p=indices[j]; if(p==a||p==b||p==c) continue;
                    if(cross(x[a],y[a],x[b],y[b],x[p],y[p])>=-.00001F && cross(x[b],y[b],x[c],y[c],x[p],y[p])>=-.00001F
                            && cross(x[c],y[c],x[a],y[a],x[p],y[p])>=-.00001F) { occupied=true; break; }
                }
                if(occupied) continue;
                out[count++]=a; out[count++]=b; out[count++]=c;
                System.arraycopy(indices,at+1,indices,at,size-at-1); size--; attempt=-1;
            }
            if(size>2) throw new IllegalStateException("Invalid coffin fracture polygon");
            return Arrays.copyOf(out,count);
        }
        private static float cross(float ax,float ay,float bx,float by,float cx,float cy) { return (bx-ax)*(cy-ay)-(by-ay)*(cx-ax); }
    }
    private static final class Mesh implements SubmitNodeCollector.CustomGeometryRenderer {
        private final float[] positions; private final int[] colors;
        int count; float r,h,cx,cy,cos=1,sin,tiltCos=1,tiltSin,du,dv,dn,drop;
        Mesh(int capacity) { positions=new float[capacity*3]; colors=new int[capacity]; }
        void identity(float r,float h) { this.r=r; this.h=h; cx=cy=sin=tiltSin=du=dv=dn=drop=0; cos=tiltCos=1; }
        void transform(Plate p,float elapsed,float r,float h) {
            this.r=r; this.h=h; cx=p.cx; cy=p.cy;
            cos=(float)Math.cos(p.spin*elapsed); sin=(float)Math.sin(p.spin*elapsed);
            tiltCos=(float)Math.cos(p.tilt*elapsed); tiltSin=(float)Math.sin(p.tilt*elapsed);
            du=p.drift*elapsed; dv=p.face<4?0:p.sway*elapsed; dn=p.face<4?p.sway*elapsed:0;
            // World-space gravity also drops roof/floor plates; local normals point sideways or up.
            drop=-.025F*elapsed-p.gravity*elapsed*elapsed;
        }
        private void vertex(float x,float y,float z,int color) {
            int p=count*3; positions[p]=x; positions[p+1]=y+drop; positions[p+2]=z; colors[count++]=color;
        }
        void point(int face,float u,float v,float n,int color) {
            float x=u-cx,y=v-cy;
            float rx=x*cos-y*sin,ry=x*sin+y*cos;
            u=cx+rx+du; v=cy+ry*tiltCos-n*tiltSin+dv; n=ry*tiltSin+n*tiltCos+dn;
            switch(face) {
                case 0->vertex(u,v,-r-n,color); case 1->vertex(r+n,v,u,color);
                case 2->vertex(-u,v,r+n,color); case 3->vertex(-r-n,v,-u,color);
                case 4->vertex(u,h+n,v-r,color); default->vertex(u,-n,v-r,color);
            }
        }
        void wall(int side,float x0,float x1,float y0,float y1,float z,int color) {
            if (y1<=y0 || (color>>>24)==0) return;
            point(side,x0,y0,z-r,color); point(side,x1,y0,z-r,color); point(side,x1,y1,z-r,color); point(side,x0,y1,z-r,color);
        }
        void flat(float x0,float z0,float x1,float z1,float y,int color) {
            if (x1<=x0 || z1<=z0 || (color>>>24)==0) return;
            vertex(x0,y,z0,color); vertex(x1,y,z0,color); vertex(x1,y,z1,color); vertex(x0,y,z1,color);
        }
        @Override public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            if (OracleGlowRender.shadowPass()) return;
            for (int i=0;i<count;i++) {
                int p=i*3;
                buffer.addVertex(pose,positions[p],positions[p+1],positions[p+2]).setColor(colors[i]).setUv(.5F,.5F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose,0,1,0);
            }
        }
    }
}
