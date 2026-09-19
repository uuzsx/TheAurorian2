package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.MoonreaverSkeletonEntity;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MoonreaverSkeletonRenderer
        extends GeoEntityRenderer<MoonreaverSkeletonEntity, MoonreaverSkeletonRenderer.State> {
    private final HumanoidModel<HumanoidRenderState> humanoidPose;
    private final float headScale;
    private final HumanoidRenderState poseState = new HumanoidRenderState();
    private final Quaternionf localArmRotation = new Quaternionf();
    private final Vector3f localArmAngles = new Vector3f();

    public MoonreaverSkeletonRenderer(EntityRendererProvider.Context context) {
        this(context, false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // GeckoLib injects GeoRenderState into vanilla render states.
    public MoonreaverSkeletonRenderer(EntityRendererProvider.Context context, boolean armored) {
        super(context, new DefaultedEntityGeoModel<>(TheAurorian2.id("moonreaver_skeleton")));
        this.humanoidPose = new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON));
        this.headScale = armored ? 0.95F : 1.0F;
        this.shadowRadius = 0.5F;
        // The source names left/right from the viewer's side, opposite to Minecraft's anatomical arms.
        this.withRenderLayer(new ItemInHandGeoLayer(context, this, "left_item", "right_item"));
        if (armored) this.withRenderLayer(new MoonreaverSkeletonArmorLayer(this, context));
    }

    @Override
    public State createRenderState(MoonreaverSkeletonEntity entity, Void related) { return new State(); }

    @Override
    public void captureDefaultRenderState(MoonreaverSkeletonEntity entity, Void related, State state, float partialTick) {
        super.captureDefaultRenderState(entity, related, state, partialTick);
        state.aggressive = entity.isAggressive();
        state.bow = entity.getMainHandItem().getItem() instanceof BowItem;
        state.leftHanded = entity.getMainArm() == HumanoidArm.LEFT;
        state.riding = entity.isPassenger();
        state.swing = entity.getAttackAnim(partialTick);
        state.attackArm = entity.swingingArm == InteractionHand.OFF_HAND ? entity.getMainArm().getOpposite() : entity.getMainArm();
        state.rightHandOccupied = !entity.getItemHeldByArm(HumanoidArm.RIGHT).isEmpty();
        state.leftHandOccupied = !entity.getItemHeldByArm(HumanoidArm.LEFT).isEmpty();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void adjustModelBonesForRender(RenderPassInfo pass, BoneSnapshots bones) {
        State state = (State) (Object) pass.renderState();
        BoneSnapshot head = bones.get("head").orElseThrow();
        // Scale around the neck pivot, including during death, so the captain's head stays seated on the armor.
        head.setScale(headScale, headScale, headScale);
        if (state.deathTime > 0) return;
        BoneSnapshot right = bones.get("hand_left").orElseThrow();
        BoneSnapshot left = bones.get("hand_right").orElseThrow();
        head.setRotX(-state.xRot * Mth.DEG_TO_RAD).setRotY(-state.yRot * Mth.DEG_TO_RAD);
        float stride = state.walkAnimationPos * 0.6662F;
        float speed = Math.min(state.walkAnimationSpeed, 1);
        bones.get("leg_left").orElseThrow().setRotX(-Mth.cos(stride) * 1.4F * speed);
        bones.get("leg_right").orElseThrow().setRotX(-Mth.cos(stride + Mth.PI) * 1.4F * speed);
        if (state.riding) {
            bones.get("leg_left").orElseThrow().setRotation(1.4137F, -Mth.PI / 10, Mth.PI / 40);
            bones.get("leg_right").orElseThrow().setRotation(1.4137F, Mth.PI / 10, -Mth.PI / 40);
        }
        if (state.aggressive && state.bow) {
            // Reuse vanilla's two-arm bow aim and idle bob; only convert the model coordinate system.
            prepareHumanoidPose(state);
            poseState.attackTime = 0;
            poseState.rightArmPose = state.leftHanded ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.BOW_AND_ARROW;
            poseState.leftArmPose = state.leftHanded ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.EMPTY;
            humanoidPose.setupAnim(poseState);
            right.setRotation(-humanoidPose.rightArm.xRot, -humanoidPose.rightArm.yRot, humanoidPose.rightArm.zRot);
            left.setRotation(-humanoidPose.leftArm.xRot, -humanoidPose.leftArm.yRot, humanoidPose.leftArm.zRot);
        } else if (!state.bow) {
            applyMeleePose(state, bones, head, right, left);
        } else {
            right.setRotX(-Mth.cos(stride + Mth.PI) * speed);
            left.setRotX(-Mth.cos(stride) * speed);
        }
    }

    private void prepareHumanoidPose(State state) {
        poseState.xRot = state.xRot;
        poseState.yRot = state.yRot;
        poseState.ageInTicks = state.ageInTicks;
        poseState.mainArm = state.leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        poseState.attackArm = state.attackArm;
        poseState.attackTime = state.swing;
        poseState.walkAnimationPos = state.walkAnimationPos;
        poseState.walkAnimationSpeed = Math.min(state.walkAnimationSpeed, 1);
        poseState.isPassenger = state.riding;
        poseState.rightArmPose = state.rightHandOccupied ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        poseState.leftArmPose = state.leftHandOccupied ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
    }

    private void applyMeleePose(State state, BoneSnapshots bones, BoneSnapshot head, BoneSnapshot right, BoneSnapshot left) {
        prepareHumanoidPose(state);
        // HumanoidModel supplies the player's third-person sword swing, driven by the real attack progress.
        humanoidPose.setupAnim(poseState);
        float bodyYaw = -humanoidPose.body.yRot;
        bones.get("body").orElseThrow().setRotY(bodyYaw);
        head.setRotY(-humanoidPose.head.yRot - bodyYaw);
        applyArmPose(right, humanoidPose.rightArm, bodyYaw);
        applyArmPose(left, humanoidPose.leftArm, bodyYaw);
    }

    private void applyArmPose(BoneSnapshot bone, ModelPart arm, float bodyYaw) {
        // Unlike vanilla's flat hierarchy, these arms inherit the torso rotation. Remove it locally
        // so torso, arms, held weapon and armor follow the same swing without rotating twice.
        localArmRotation.rotationY(-bodyYaw).rotateZYX(arm.zRot, -arm.yRot, -arm.xRot)
                .getEulerAnglesZYX(localArmAngles);
        bone.setRotation(localArmAngles.x, localArmAngles.y, localArmAngles.z);
    }

    public static final class State extends LivingEntityRenderState {
        public boolean aggressive, bow, leftHanded, riding, rightHandOccupied, leftHandOccupied;
        public HumanoidArm attackArm = HumanoidArm.RIGHT;
        public float swing;
    }
}
