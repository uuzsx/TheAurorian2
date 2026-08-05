package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.util.Mth;

public final class AurorianSheepModel extends EntityModel<SheepRenderState> {
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public AurorianSheepModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 11.75F, -6.75F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -2.0F, -7.0F, 8.0F, 5.0F, 8.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.829F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-1.25F, -4.0F, -2.0F, 3.0F, 5.0F, 3.0F).mirror(false).texOffs(0, 42).addBox(6.25F, -4.0F, -2.0F, 3.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -6.0F, 10.0F, 10.0F, 14.0F), PartPose.offset(0.0F, 16.0F, -1.0F));
        body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, -6.0F, 0.0F, 3.0F, 14.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(5.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.1309F));
        body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 24).addBox(0.0F, 0.0F, -6.0F, 0.0F, 3.0F, 14.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-5.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.1309F));
        body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(33, 35).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 3.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 8.0F, 0.1309F, 0.0F, 0.0F));
        body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(33, 35).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 3.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 5.0F, -6.0F, -0.1309F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(26, 41).addBox(-1.75F, -1.0F, -2.75F, 4.0F, 4.0F, 4.0F), PartPose.offset(-3.0F, 21.0F, 5.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(26, 41).mirror().addBox(-2.25F, -1.0F, -2.75F, 4.0F, 4.0F, 4.0F).mirror(false), PartPose.offset(3.0F, 21.0F, 5.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(26, 41).addBox(-1.75F, -1.0F, -2.75F, 4.0F, 4.0F, 4.0F), PartPose.offset(-3.0F, 21.0F, -4.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(26, 41).mirror().addBox(-2.25F, -1.0F, -2.75F, 4.0F, 4.0F, 4.0F).mirror(false), PartPose.offset(3.0F, 21.0F, -4.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 7.0F));
        tail.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(13, 42).addBox(-1.5F, -1.0F, -0.75F, 3.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SheepRenderState state) {
        super.setupAnim(state);
        this.head.y = 11.75F + state.headEatPositionScale * 9.0F * state.ageScale;
        this.head.xRot = state.headEatAngleScale;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        animateLegs(state.walkAnimationPos, state.walkAnimationSpeed);
    }

    private void animateLegs(float position, float speed) {
        this.rightHindLeg.xRot = Mth.cos(position * 0.6662F) * 1.4F * speed;
        this.leftHindLeg.xRot = Mth.cos(position * 0.6662F + Mth.PI) * 1.4F * speed;
        this.rightFrontLeg.xRot = this.leftHindLeg.xRot;
        this.leftFrontLeg.xRot = this.rightHindLeg.xRot;
    }
}
