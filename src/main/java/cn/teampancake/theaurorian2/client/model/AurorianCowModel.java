package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public final class AurorianCowModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public AurorianCowModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightHindLeg = body.getChild("right_leg_2");
        this.leftHindLeg = body.getChild("left_leg_2");
        this.rightFrontLeg = body.getChild("right_leg_1");
        this.leftFrontLeg = body.getChild("left_leg_1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -4.0F, -11.0F, 18.0F, 16.0F, 22.0F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 53).addBox(-5.0F, -4.0F, -11.75F, 10.0F, 7.0F, 12.0F), PartPose.offsetAndRotation(0.0F, 1.25F, -10.75F, 0.3054F, 0.0F, 0.0F));
        PartDefinition horn = head.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(43, 47).addBox(-4.7899F, -0.7158F, -2.2687F, 5.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(-4.5F, -2.0F, -2.0F, -0.6378F, 0.1582F, 0.2095F));
        horn.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(43, 53).addBox(-0.6569F, -6.2642F, -2.2687F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
        PartDefinition horn2 = head.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(43, 47).mirror().addBox(-0.2101F, -0.7158F, -2.2687F, 5.0F, 3.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(4.5F, -2.0F, -2.0F, -0.6378F, -0.1582F, -0.2095F));
        horn2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(43, 53).mirror().addBox(-2.3431F, -6.2642F, -2.2687F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 38).addBox(-4.5F, 0.0F, -10.25F, 9.0F, 3.0F, 12.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
        body.addOrReplaceChild("right_leg_1", CubeListBuilder.create().texOffs(56, 53).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(-5.5F, 11.5F, -7.0F));
        body.addOrReplaceChild("left_leg_1", CubeListBuilder.create().texOffs(56, 53).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F).mirror(false), PartPose.offset(5.5F, 11.5F, -7.0F));
        body.addOrReplaceChild("right_leg_2", CubeListBuilder.create().texOffs(56, 53).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(-5.5F, 11.5F, 7.0F));
        body.addOrReplaceChild("left_leg_2", CubeListBuilder.create().texOffs(56, 53).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F).mirror(false), PartPose.offset(5.5F, 11.5F, 7.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(81, 23).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 13.0F, 2.0F).texOffs(81, 39).addBox(-1.5F, 11.0F, 0.5F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 2.0F, 11.0F));
        return LayerDefinition.create(mesh, 96, 96);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.rightHindLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leftHindLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
        this.rightFrontLeg.xRot = this.leftHindLeg.xRot;
        this.leftFrontLeg.xRot = this.rightHindLeg.xRot;
    }
}
