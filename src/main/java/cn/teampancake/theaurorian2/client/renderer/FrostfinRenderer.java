package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.FrostfinEntity;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public final class FrostfinRenderer extends GeoEntityRenderer<FrostfinEntity, FrostfinRenderer.State> {
    public FrostfinRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(TheAurorian2.id("frostfin")));
        withScale(0.5F);
        this.shadowRadius = 0.3F;
    }

    @Override
    public State createRenderState(FrostfinEntity entity, Void relatedObject) {
        return new State();
    }

    @Override
    public void captureDefaultRenderState(FrostfinEntity entity, Void relatedObject, State state, float partialTick) {
        super.captureDefaultRenderState(entity, relatedObject, state, partialTick);
        state.fishScale = entity.getSalmonScale();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"}) // GeckoLib adds GeoRenderState to EntityRenderState at runtime.
    public void adjustRenderPose(RenderPassInfo passInfo) {
        super.adjustRenderPose(passInfo);
        var state = (State) (Object) passInfo.renderState();
        var pose = passInfo.poseStack();
        pose.scale(state.fishScale, state.fishScale, state.fishScale);
        // Keep source pivots/UVs intact; center the 51-pixel-long fish as a whole.
        pose.translate(0, 0.12F, -10.5F / 16);
        if (!state.isInWater) {
            // Vanilla salmon dry-land pose; the imported segmented tail keeps swimming.
            pose.mulPose(Axis.YP.rotationDegrees(1.3F * 4.3F * Mth.sin(1.7F * 0.6F * state.ageInTicks)));
            pose.translate(0.2F, 0.1F, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(90));
        }
    }

    public static final class State extends LivingEntityRenderState {
        public float fishScale = 1;
    }
}
