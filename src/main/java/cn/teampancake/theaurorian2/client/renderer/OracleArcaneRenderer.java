package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleArcaneEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Signed cubes require culling: a double-sided pass fills the negative hull's silhouette. */
public final class OracleArcaneRenderer extends GeoEntityRenderer<OracleArcaneEntity, OracleArcaneRenderer.State> {
    // NeoForge's toBuilder preserves Iris's main AND shadow program mappings. A bare
    // snippet creates an unrecognized pipeline, so shader packs drop the spell geometry.
    // ENTITY_TRANSLUCENT_EMISSIVE also sets PER_FACE_LIGHTING, which overrides
    // NO_CARDINAL_LIGHTING and darkens inverted hulls. EYES is genuinely unlit.
    public static final RenderPipeline PIPELINE = RenderPipelines.EYES.toBuilder()
            .withLocation(TheAurorian2.id("pipeline/oracle_arcane"))
            .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("ALPHA_CUTOUT", .1F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL,false))
            .withCull(true).build();
    // The beam's binary-alpha nested hulls must write depth, including their core.
    public static final RenderPipeline BEAM_PIPELINE = RenderPipelines.EYES.toBuilder()
            .withLocation(TheAurorian2.id("pipeline/oracle_repulsor"))
            .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("ALPHA_CUTOUT", .1F)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL,true))
            .withCull(true).build();
    private static final String[] NAMES = {"shield","blast","trail","hit","clock","repulsor"};
    private static final Identifier[] MODELS = new Identifier[NAMES.length], TEXTURES = new Identifier[NAMES.length];
    private static final RenderType[] TYPES = new RenderType[NAMES.length];
    static {
        for(int i=0;i<NAMES.length;i++) {
            MODELS[i]=TheAurorian2.id("entity/oracle_arcane_"+NAMES[i]);
            TEXTURES[i]=TheAurorian2.id("textures/entity/oracle_arcane_"+(i==3?"trail":NAMES[i])+".png");
            TYPES[i]=RenderType.create("oracle_arcane_"+NAMES[i],RenderSetup.builder(i==5 ? BEAM_PIPELINE : PIPELINE)
                    .withTexture("Sampler0",TEXTURES[i]).sortOnUpload().createRenderSetup());
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public OracleArcaneRenderer(EntityRendererProvider.Context context) {
        super(context,new SpellModel()); shadowRadius=0;
        withRenderLayer((com.geckolib.renderer.layer.GeoRenderLayer)new OracleArcaneGlowLayer((com.geckolib.renderer.base.GeoRenderer)this));
    }
    @Override public State createRenderState(OracleArcaneEntity entity, Void related) { return new State(); }
    private static int modelIndex(int kind) { return kind==OracleArcaneEntity.REPULSOR ? 5 : Math.min(kind,4); }
    @Override public RenderType getRenderType(State state, Identifier texture) { return TYPES[modelIndex(state.kind)]; }
    @Override protected AABB getBoundingBoxForCulling(OracleArcaneEntity entity) {
        if(entity.kind()==OracleArcaneEntity.REPULSOR) return new AABB(entity.position(),entity.beamEnd()).inflate(6);
        if(entity.kind()==OracleArcaneEntity.CLOCK && entity.empowered()) return entity.getBoundingBox().inflate(96,15,96);
        double radius = switch (entity.kind()) {
            case OracleArcaneEntity.FLAME -> OracleArcaneEntity.FLAME_RANGE+1;
            case OracleArcaneEntity.BLAST, OracleArcaneEntity.TRAIL, OracleArcaneEntity.HIT -> 8;
            default -> 4;
        };
        return entity.getBoundingBox().inflate(radius);
    }
    @Override public void extractRenderState(OracleArcaneEntity entity, State state, float partialTick) {
        super.extractRenderState(entity,state,partialTick); state.kind=entity.kind();
        state.empowered=entity.empowered();
        state.warmingUp=entity.isWarmingUp();
        if(state.kind==OracleArcaneEntity.REPULSOR) {
            state.pitch=entity.getViewXRot(partialTick); state.beamLength=entity.beamLength();
            state.beamWidth=(entity.empowered() ? 2 : 1)*entity.beamVisualScale()*entity.beamVisualFade(partialTick);
        }
        if(state.kind==OracleArcaneEntity.FIRE_PATCH && entity.level() instanceof ClientLevel level) {
            state.fire.blockPos=entity.blockPosition(); state.fire.randomSeedPos=entity.blockPosition();
            state.fire.blockState=Blocks.SOUL_FIRE.defaultBlockState(); state.fire.biome=level.getBiome(entity.blockPosition());
            state.fire.cardinalLighting=level.cardinalLighting(); state.fire.lightEngine=level.getLightEngine();
        }
    }
    @Override public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if(state.warmingUp || state.kind==OracleArcaneEntity.REPULSOR && state.beamWidth<=0) return;
        if(state.kind==OracleArcaneEntity.FIRE_PATCH) {
            stack.pushPose(); stack.translate(-.5,0,-.5); collector.submitMovingBlock(stack,state.fire); stack.popPose(); return;
        }
        if(state.kind>OracleArcaneEntity.CLOCK && state.kind!=OracleArcaneEntity.REPULSOR) return;
        // Energy geometry emits light; it must not cast opaque shader-pack shadows.
        if(OracleGlowRender.shadowPass()) return;
        stack.pushPose();
        if(state.kind==OracleArcaneEntity.SHIELD) stack.scale(1.3F,1.3F,1.3F);
        if(state.kind==OracleArcaneEntity.CLOCK && state.empowered) {
            float scale=OracleArcaneEntity.REGIONAL_CLOCK_SCALE;
            stack.scale(scale,scale,scale);
        }
        if(state.kind==OracleArcaneEntity.BLAST) {
            float scale = state.empowered ? OracleArcaneEntity.EMPOWERED_BLAST_SCALE : OracleArcaneEntity.BLAST_SCALE;
            stack.scale(scale,scale,scale);
        } else if(state.kind==OracleArcaneEntity.TRAIL || state.kind==OracleArcaneEntity.HIT) {
            float scale=state.empowered ? 3 : 2; stack.scale(scale,scale,scale);
        }
        // The pack used different armor-stand attachment heights. Align each authored
        // root to our physical projectile anchor without moving any cube or pivot.
        if(state.kind==OracleArcaneEntity.BLAST) stack.translate(0,-17/16.0,0);
        else if(state.kind==OracleArcaneEntity.TRAIL) stack.translate(0,6/16.0,0);
        else if(state.kind==OracleArcaneEntity.HIT) stack.translate(0,-14/16.0,0);
        super.submit(state,stack,collector,camera); stack.popPose();
    }
    // GeckoLib mixes GeoRenderState into EntityRenderState at runtime, outside javac's bounds.
    @SuppressWarnings({"rawtypes","unchecked"})
    @Override protected void applyRotations(RenderPassInfo pass, PoseStack stack, float partialTick) {
        super.applyRotations(pass,stack,partialTick);
        State state=(State)(Object)pass.renderState();
        if(state.kind==OracleArcaneEntity.REPULSOR) {
            stack.mulPose(Axis.XP.rotationDegrees(-state.pitch));
            // Authored shaft spans 150 pixels, scaled 1.4 by h_body. Only its reach
            // changes with terrain; keep every original cube, pivot and animation.
            float width=state.beamWidth;
            stack.scale(width,width,state.beamLength/(150*1.4F/16));
            stack.translate(0,-15/16.0-.01,-1.5*1.4/16);
        }
    }
    private static final class SpellModel extends GeoModel<OracleArcaneEntity> {
        private static final DataTicket<Integer> KIND = DataTickets.create("theaurorian2:oracle_arcane_kind",Integer.class);
        @Override public void addAdditionalStateData(OracleArcaneEntity entity, Object related, GeoRenderState state) {
            state.addGeckolibData(KIND,modelIndex(entity.kind()));
        }
        @Override public Identifier getModelResource(GeoRenderState state) { return MODELS[state.getGeckolibData(KIND)]; }
        @Override public Identifier getTextureResource(GeoRenderState state) { return TEXTURES[state.getGeckolibData(KIND)]; }
        @Override public Identifier getAnimationResource(OracleArcaneEntity entity) { return MODELS[modelIndex(entity.kind())]; }
    }
    public static final class State extends EntityRenderState {
        private int kind;
        private boolean empowered, warmingUp;
        private float pitch, beamLength, beamWidth;
        private final MovingBlockRenderState fire = new MovingBlockRenderState();
    }
}
