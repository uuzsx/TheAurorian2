package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.KnightSpearModel;
import cn.teampancake.theaurorian2.common.item.KnightSpearItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.KineticWeapon;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class KnightSpearRenderer extends GeoItemRenderer<KnightSpearItem> {

    private static final float SECOND_STAGE_TRANSITION_TICKS = 20.0F;
    private static final float THIRD_STAGE_TRANSITION_TICKS = 40.0F;
    private static final float RETURN_TO_IDLE_TICKS = 5.0F;
    private static final float THIRD_STAGE_X_ROTATION = -35.0F;
    private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);
    private final KnightSpearItem item;

    public KnightSpearRenderer(KnightSpearItem item) {
        super(new KnightSpearModel(item.textureName()));
        this.item = item;
        useAlternateGuiLighting();
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        ItemDisplayContext context = renderPassInfo.getOrDefaultGeckolibData(
                DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);
        HumanoidArm renderedArm = switch (context) {
            case FIRST_PERSON_RIGHT_HAND -> HumanoidArm.RIGHT;
            case FIRST_PERSON_LEFT_HAND -> HumanoidArm.LEFT;
            default -> null;
        };
        LocalPlayer player = Minecraft.getInstance().player;
        if (renderedArm == null || player == null) {
            return;
        }

        InteractionHand renderedHand = player.getMainArm() == renderedArm
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        float partialTick = renderPassInfo.renderState().getPartialTick();
        if (!player.isUsingItem()
                && renderedHand == InteractionHand.MAIN_HAND
                && player.swinging
                && player.swingingArm == renderedHand
                && player.getAttackAnim(partialTick) > 0.0F) {
            applyBasicAttackTransform(
                    renderPassInfo,
                    renderedArm == HumanoidArm.LEFT,
                    player.getAttackAnim(partialTick));
            return;
        }

        if (!player.isUsingItem() || player.getUseItem().getItem() != this.item) {
            return;
        }

        if (player.getUsedItemHand() != renderedHand) {
            return;
        }

        float useTicks = player.getUseItem().getUseDuration(player)
                - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
        KineticWeapon kineticWeapon = player.getUseItem().get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null) {
            return;
        }

        float delayTicks = kineticWeapon.delayTicks();
        float secondStageEnd = delayTicks + kineticWeapon.dismountConditions()
                .map(KineticWeapon.Condition::maxDurationTicks)
                .orElse(0);
        float thirdStageCenter = delayTicks + kineticWeapon.knockbackConditions()
                .map(KineticWeapon.Condition::maxDurationTicks)
                .orElse(0);
        float returnToIdleEnd = delayTicks + kineticWeapon.damageConditions()
                .map(KineticWeapon.Condition::maxDurationTicks)
                .orElse(0);

        float secondStageProgress = smoothProgress(progress(
                useTicks,
                secondStageEnd - SECOND_STAGE_TRANSITION_TICKS,
                secondStageEnd));
        float thirdStageProgress = smoothProgress(progress(
                useTicks,
                thirdStageCenter - THIRD_STAGE_TRANSITION_TICKS / 2.0F,
                thirdStageCenter + THIRD_STAGE_TRANSITION_TICKS / 2.0F));
        float returnToIdleProgress = smoothProgress(progress(
                useTicks,
                returnToIdleEnd - RETURN_TO_IDLE_TICKS,
                returnToIdleEnd));

        applyUseStageTransform(
                renderPassInfo,
                renderedArm == HumanoidArm.LEFT,
                secondStageProgress,
                thirdStageProgress,
                returnToIdleProgress);

        int direction = renderedArm == HumanoidArm.RIGHT ? 1 : -1;
        float swayWeight = secondStageProgress
                * (1.0F - thirdStageProgress)
                * (1.0F - returnToIdleProgress);
        if (swayWeight > 0.0F) {
            float sway = (float)Math.sin(useTicks * 0.3F) * 1.25F * swayWeight;
            renderPassInfo.poseStack().mulPose(Axis.ZP.rotationDegrees(direction * sway));
        }
    }

    private static void applyBasicAttackTransform(
            RenderPassInfo<GeoRenderState> renderPassInfo,
            boolean leftHand,
            float attackProgress) {
        float enterProgress = smoothProgress(progress(attackProgress, 0.0F, 0.06F));
        float returnProgress = smoothProgress(progress(attackProgress, 0.48F, 1.0F));
        float attackPoseWeight = enterProgress * (1.0F - returnProgress);
        float rotationX = -10.0F - 56.0F * attackPoseWeight;
        float rotationZ = 7.0F * attackPoseWeight;
        float translationY = 2.5F + 0.75F * attackPoseWeight;
        float translationZ = 4.0F + 2.5F * attackPoseWeight;
        Matrix4f targetTransform = itemTransform(
                leftHand,
                rotationX, 0.0F, rotationZ,
                -1.25F, translationY, translationZ,
                0.4F, 0.52F, 0.4F);
        Matrix4f idleTransform = itemTransform(
                leftHand,
                -10.0F, 0.0F, 0.0F,
                -1.25F, 2.5F, 4.0F,
                0.4F, 0.52F, 0.4F);
        Matrix4f geckolibOrigin = new Matrix4f().translate(0.5F, 0.51F, 0.5F);

        Matrix4f exactCorrection = new Matrix4f(geckolibOrigin).invert()
                .mul(new Matrix4f(idleTransform).invert())
                .mul(targetTransform)
                .mul(geckolibOrigin);
        renderPassInfo.poseStack().mulPose(exactCorrection);
    }

    private static void applyUseStageTransform(
            RenderPassInfo<GeoRenderState> renderPassInfo,
            boolean leftHand,
            float secondStageProgress,
            float thirdStageProgress,
            float returnToIdleProgress) {
        float rotationX = -66.0F + THIRD_STAGE_X_ROTATION * thirdStageProgress;
        float rotationY = lerp(90.0F, 0.0F, secondStageProgress);
        float rotationZ = 7.0F;
        float translationY = 3.25F;
        float translationZ = 6.5F;

        rotationX = lerp(rotationX, -10.0F, returnToIdleProgress);
        rotationY = lerp(rotationY, 0.0F, returnToIdleProgress);
        rotationZ = lerp(rotationZ, 0.0F, returnToIdleProgress);
        translationY = lerp(translationY, 2.5F, returnToIdleProgress);
        translationZ = lerp(translationZ, 4.0F, returnToIdleProgress);

        Matrix4f baseStageTransform = itemTransform(
                leftHand,
                -66.0F, 90.0F, 7.0F,
                -1.25F, 3.25F, 6.5F,
                0.4F, 0.52F, 0.4F);
        Matrix4f targetTransform = itemTransform(
                leftHand,
                rotationX, rotationY, rotationZ,
                -1.25F, translationY, translationZ,
                0.4F, 0.52F, 0.4F);
        Matrix4f geckolibOrigin = new Matrix4f().translate(0.5F, 0.51F, 0.5F);
        Matrix4f exactCorrection = new Matrix4f(geckolibOrigin).invert()
                .mul(new Matrix4f(baseStageTransform).invert())
                .mul(targetTransform)
                .mul(geckolibOrigin);
        renderPassInfo.poseStack().mulPose(exactCorrection);
    }

    private static float progress(float value, float start, float end) {
        if (end <= start) {
            return value >= end ? 1.0F : 0.0F;
        }

        return Math.clamp((value - start) / (end - start), 0.0F, 1.0F);
    }

    private static float smoothProgress(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private static Matrix4f itemTransform(
            boolean leftHand,
            float rotationX,
            float rotationY,
            float rotationZ,
            float translationX,
            float translationY,
            float translationZ,
            float scaleX,
            float scaleY,
            float scaleZ) {
        float mirroredTranslationX = leftHand ? -translationX : translationX;
        float mirroredRotationY = leftHand ? -rotationY : rotationY;
        float mirroredRotationZ = leftHand ? -rotationZ : rotationZ;

        return new Matrix4f()
                .translate(
                        mirroredTranslationX / 16.0F,
                        translationY / 16.0F,
                        translationZ / 16.0F)
                .rotate(new Quaternionf().rotationXYZ(
                        rotationX * DEG_TO_RAD,
                        mirroredRotationY * DEG_TO_RAD,
                        mirroredRotationZ * DEG_TO_RAD))
                .scale(scaleX, scaleY, scaleZ)
                .translate(-0.5F, -0.5F, -0.5F);
    }
}
