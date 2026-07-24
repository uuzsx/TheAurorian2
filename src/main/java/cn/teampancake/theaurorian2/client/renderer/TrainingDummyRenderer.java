package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.DamageNumberEntity;
import cn.teampancake.theaurorian2.common.entity.TrainingDummyEntity;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;

public final class TrainingDummyRenderer extends GeoEntityRenderer<TrainingDummyEntity, TrainingDummyRenderState> {

    private static final float TEXT_SCALE = 0.025F;
    private static final float TEXT_X = 20.0F;

    public TrainingDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(TheAurorian2.id("training_dummy")));
        this.shadowRadius = 0.35F;
    }

    @Override
    public TrainingDummyRenderState createRenderState(TrainingDummyEntity entity, Void relatedObject) {
        return new TrainingDummyRenderState();
    }

    @Override
    public void extractRenderState(TrainingDummyEntity entity, TrainingDummyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.damagePerSecond = entity.getDamagePerSecond();
        state.totalDamage = entity.getTotalDamage();
        state.lastHitDamage = entity.getLastHitDamage();
    }

    @Override
    public void submit(
            TrainingDummyRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.pushPose();
        poseStack.translate(0.0F, 1.65F, 0.0F);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        int light = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, 2);
        this.submitStatLine(
                poseStack,
                submitNodeCollector,
                Component.translatable(
                        "gui.theaurorian2.training_dummy.dps", DamageNumberEntity.formatDamage(state.damagePerSecond)),
                -10.0F,
                light);
        this.submitStatLine(
                poseStack,
                submitNodeCollector,
                Component.translatable(
                        "gui.theaurorian2.training_dummy.total_damage", DamageNumberEntity.formatDamage(state.totalDamage)),
                0.0F,
                light);
        this.submitStatLine(
                poseStack,
                submitNodeCollector,
                Component.translatable(
                        "gui.theaurorian2.training_dummy.last_hit_damage", DamageNumberEntity.formatDamage(state.lastHitDamage)),
                10.0F,
                light);
        poseStack.popPose();
    }

    private void submitStatLine(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Component text,
            float y,
            int light) {
        submitNodeCollector.submitText(
                poseStack,
                TEXT_X,
                y,
                text.getVisualOrderText(),
                true,
                Font.DisplayMode.NORMAL,
                light,
                0xFFFFFFFF,
                0,
                0);
    }
}
