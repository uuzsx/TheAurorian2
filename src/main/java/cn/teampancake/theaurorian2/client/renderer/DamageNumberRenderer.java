package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.entity.DamageNumberEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public final class DamageNumberRenderer extends EntityRenderer<DamageNumberEntity, EntityRenderState> {

    private static final float TEXT_SCALE = 0.025F;

    public DamageNumberRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(
            EntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (state.nameTag == null || state.nameTagAttachment == null) {
            return;
        }

        Vec3 attachment = state.nameTagAttachment;
        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + 0.5, attachment.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        submitNodeCollector.submitText(
                poseStack,
                -this.getFont().width(state.nameTag) / 2.0F,
                0.0F,
                state.nameTag.getVisualOrderText(),
                true,
                Font.DisplayMode.NORMAL,
                LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, 2),
                0xFFFFFFFF,
                0,
                0);
        poseStack.popPose();
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
