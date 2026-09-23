package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.entity.ReturningAxeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public final class ReturningAxeRenderer extends EntityRenderer<ReturningAxeEntity, ReturningAxeRenderer.RenderState> {
    private final ItemModelResolver resolver;
    public ReturningAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
        resolver = context.getItemModelResolver();
    }
    @Override
    public RenderState createRenderState() { return new RenderState(); }
    @Override
    public void extractRenderState(ReturningAxeEntity entity, RenderState state, float partial) {
        super.extractRenderState(entity, state, partial);
        var velocity = entity.getDeltaMovement();
        state.flightYaw = (float) Math.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG;
        state.flightPitch = -(float) Math.atan2(velocity.y, velocity.horizontalDistance()) * Mth.RAD_TO_DEG;
        resolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.FIXED, entity);
    }
    @Override
    public void submit(RenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(state.flightYaw));
        pose.mulPose(Axis.XP.rotationDegrees(state.flightPitch));
        // Turn the item face into the flight plane so it tumbles end over end, independently of the camera.
        pose.mulPose(Axis.YP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotationDegrees(state.ageInTicks * -35));
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    public static final class RenderState extends ThrownItemRenderState {
        private float flightYaw;
        private float flightPitch;
    }
}
