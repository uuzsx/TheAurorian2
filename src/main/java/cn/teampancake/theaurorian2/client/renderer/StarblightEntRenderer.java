package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.StarblightEntEntity;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class StarblightEntRenderer extends GeoEntityRenderer<StarblightEntEntity,StarblightEntRenderer.State> {
    public StarblightEntRenderer(EntityRendererProvider.Context context) {
        this(context,StarblightEntEntity.Wood.STARBLIGHT);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public StarblightEntRenderer(EntityRendererProvider.Context context,StarblightEntEntity.Wood wood) {
        super(context,new DefaultedEntityGeoModel<>(TheAurorian2.id("starblight_ent")));
        shadowRadius=.35F;
        if (wood!=StarblightEntEntity.Wood.STARBLIGHT) {
            for (String bone:new String[]{"body","log_1","leg_left","leg_right"}) {
                withRenderLayer((GeoRenderLayer)new WoodEntTextureLayer((GeoRenderer)this,bone,wood));
            }
        }
        withRenderLayer((GeoRenderLayer)new StarblightEntEyesLayer((GeoRenderer)this,wood));
    }
    @Override public void extractRenderState(StarblightEntEntity entity,State state,float partial) {
        state.disguised=entity.isDisguised();
        super.extractRenderState(entity,state,partial);
    }
    @Override public State createRenderState(StarblightEntEntity entity,Void related) { return new State(); }
    @Override public void captureDefaultRenderState(StarblightEntEntity entity,Void related,State state,float partial) {
        super.captureDefaultRenderState(entity,related,state,partial);
        state.disguised=entity.isDisguised();
        if (state.disguised&&entity.level() instanceof ClientLevel level) {
            BlockPos pos=entity.blockPosition();var block=state.log;
            block.blockPos=pos;block.randomSeedPos=pos;block.blockState=entity.wood().disguise();
            block.biome=level.getBiome(pos);block.cardinalLighting=level.cardinalLighting();block.lightEngine=level.getLightEngine();
        }
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        if (!state.disguised) { super.submit(state,pose,collector,camera);return; }
        // The dormant disguise uses the actual block mesh, texture and block-light pipeline.
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-state.bodyRot));pose.translate(-.5,0,-.5);
        collector.submitMovingBlock(pose,state.log);pose.popPose();
    }
    @Override protected float getShadowRadius(State state) { return state.disguised?0:super.getShadowRadius(state); }
    @Override protected float getDeathMaxRotation(GeoRenderState state) { return 0; }
    @Override protected AABB getBoundingBoxForCulling(StarblightEntEntity entity) { return super.getBoundingBoxForCulling(entity).inflate(.6); }
    public static final class State extends LivingEntityRenderState {
        public boolean disguised;
        public final MovingBlockRenderState log=new MovingBlockRenderState();
    }
}
