package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.util.Mth;

public final class AurorianRabbitModel extends EntityModel<RabbitRenderState> {
    private final ModelPart head;
    private final ModelPart rightRearFoot;
    private final ModelPart leftRearFoot;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public AurorianRabbitModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("all").getChild("body");
        this.head = body.getChild("head");
        this.rightFrontLeg = body.getChild("arm_right");
        this.leftFrontLeg = body.getChild("arm_left");
        this.rightRearFoot = body.getChild("leg_right");
        this.leftRearFoot = body.getChild("leg_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -5.5F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(11, 14).addBox(-1.0F, -4.5F, 3.75F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 3).addBox(-2.5F, -4.0F, -3.75F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(45, 5).addBox(-0.5F, -1.5F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.75F, -4.0F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(50, 7).mirror().addBox(-1.0F, -3.0F, -0.25F, 2.0F, 4.0F, 1.0F).mirror(false), PartPose.offsetAndRotation(1.5F, -4.0F, 0.25F, -0.3491F, 0.0F, 0.2618F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(50, 7).addBox(-1.0F, -3.0F, -0.25F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-1.5F, -4.0F, 0.25F, -0.3491F, 0.0F, -0.2618F));
        body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(-2.0F, -0.5F, -3.0F));
        body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F).mirror(false), PartPose.offset(2.0F, -0.5F, -3.0F));
        body.addOrReplaceChild("leg_right", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-0.5F, -1.75F, -1.5F, 2.0F, 3.0F, 3.0F)
                .texOffs(0, 14).addBox(-0.25F, 1.0F, -2.5F, 2.0F, 1.0F, 3.0F), PartPose.offset(-3.0F, -1.5F, 3.0F));
        body.addOrReplaceChild("leg_left", CubeListBuilder.create()
                .texOffs(0, 19).mirror().addBox(-1.5F, -1.75F, -1.5F, 2.0F, 3.0F, 3.0F).mirror(false)
                .texOffs(0, 14).mirror().addBox(-1.75F, 1.0F, -2.5F, 2.0F, 1.0F, 3.0F).mirror(false), PartPose.offset(3.0F, -1.5F, 3.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(RabbitRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float jump = Mth.sin(state.jumpCompletion * Mth.PI);
        this.rightFrontLeg.xRot = (jump * -40.0F - 11.0F) * Mth.DEG_TO_RAD;
        this.leftFrontLeg.xRot = this.rightFrontLeg.xRot;
        this.rightRearFoot.xRot = jump * 50.0F * Mth.DEG_TO_RAD;
        this.leftRearFoot.xRot = this.rightRearFoot.xRot;
    }
}
