package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.util.Mth;

public final class AurorianPigModel extends EntityModel<PigRenderState> {
    private final ModelPart head;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart backLeft;
    private final ModelPart backRight;

    public AurorianPigModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("all").getChild("body");
        this.head = body.getChild("bone13").getChild("head");
        this.frontLeft = body.getChild("bone");
        this.frontRight = body.getChild("bone7");
        this.backLeft = body.getChild("bone10");
        this.backRight = body.getChild("bone4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, 16.5F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition torso = body.addOrReplaceChild("bone13", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-5.0F, -8.75F, -8.0F, 10.0F, 10.0F, 9.0F)
                .texOffs(0, 6).addBox(0.0F, -12.25F, -8.0F, 0.0F, 7.0F, 15.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 2.25F, 0.0F));
        torso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(42, 5).addBox(-4.0F, -4.5F, -1.75F, 8.0F, 8.0F, 7.0F), PartPose.offsetAndRotation(0.0F, -2.25F, 2.0F, -0.1745F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(74, 6).addBox(-4.0F, -3.0F, -6.0F, 8.0F, 8.0F, 6.0F), PartPose.offset(0.0F, -5.0F, -8.0F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(120, 11).mirror().addBox(-3.5F, -3.25F, -0.5F, 1.0F, 4.0F, 1.0F).mirror(false).texOffs(120, 11).addBox(2.5F, -3.25F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -6.0F, 0.8727F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(103, 9).addBox(-2.5F, -2.0F, -2.25F, 5.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -6.0F, 0.1309F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(103, 17).mirror().addBox(-2.0F, -1.75F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(4.0F, -3.0F, -1.0F, -0.3927F, 0.0F, 0.48F));
        head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(103, 17).addBox(-1.0F, -1.75F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-4.0F, -3.0F, -1.0F, -0.3927F, 0.0F, -0.48F));
        torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(43, 24).addBox(0.0F, -1.0F, -1.0F, 0.0F, 3.0F, 8.0F), PartPose.offsetAndRotation(0.0F, -3.25F, 7.0F, -0.9163F, 0.0F, 0.0F));
        PartDefinition bone7 = body.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(-4.75F, 0.0F, -5.5F));
        PartDefinition frontRight = bone7.addOrReplaceChild("front_leg_right", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, -4.2554F, -2.6898F, 3.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, 0.2618F, 0.0F, 0.0F));
        PartDefinition bone8 = frontRight.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(0.0F, 1.5F, -0.15F));
        PartDefinition frontRightLower = bone8.addOrReplaceChild("front_leg_right_1", CubeListBuilder.create().texOffs(17, 31).addBox(-0.5F, -2.5316F, -4.7936F, 2.0F, 6.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 2.5F, 2.15F, -0.5672F, 0.0F, 0.0F));
        PartDefinition bone9 = frontRightLower.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -3.0F));
        bone9.addOrReplaceChild("front_leg_right_2", CubeListBuilder.create().texOffs(28, 30).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(4.75F, 0.0F, -5.5F));
        PartDefinition frontLeft = bone.addOrReplaceChild("front_leg_left", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-2.0F, -4.2554F, -2.6898F, 3.0F, 6.0F, 5.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, 0.2618F, 0.0F, 0.0F));
        PartDefinition bone2 = frontLeft.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 1.5F, -0.15F));
        PartDefinition frontLeftLower = bone2.addOrReplaceChild("front_leg_left_1", CubeListBuilder.create().texOffs(17, 31).mirror().addBox(-1.5F, -2.5316F, -4.7936F, 2.0F, 6.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 2.5F, 2.15F, -0.5672F, 0.0F, 0.0F));
        PartDefinition bone3 = frontLeftLower.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -3.0F));
        bone3.addOrReplaceChild("front_leg_left_2", CubeListBuilder.create().texOffs(28, 30).mirror().addBox(-2.0F, -3.0F, -2.0F, 3.0F, 2.0F, 4.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone10 = body.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(4.75F, 0.25F, 4.0F));
        PartDefinition backLeft = bone10.addOrReplaceChild("back_leg_left", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-2.0F, -3.5571F, -4.0717F, 3.0F, 6.0F, 5.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.48F, 0.0F, 0.0F));
        PartDefinition bone11 = backLeft.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.5F));
        PartDefinition backLeftLower = bone11.addOrReplaceChild("back_leg_left_1", CubeListBuilder.create().texOffs(17, 31).mirror().addBox(-1.5F, -2.97F, -0.2186F, 2.0F, 6.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 1.75F, 0.5F, 0.9599F, 0.0F, 0.0F));
        PartDefinition bone12 = backLeftLower.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(0.0F, 2.5F, 1.5F));
        bone12.addOrReplaceChild("back_leg_left_2", CubeListBuilder.create().texOffs(28, 30).mirror().addBox(-2.0F, -3.0F, -3.0F, 3.0F, 2.0F, 4.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, -0.48F, 0.0F, 0.0F));

        PartDefinition bone4 = body.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-4.75F, 0.25F, 4.0F));
        PartDefinition backRight = bone4.addOrReplaceChild("back_leg_right", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, -3.5571F, -4.0717F, 3.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.48F, 0.0F, 0.0F));
        PartDefinition bone5 = backRight.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.5F));
        PartDefinition backRightLower = bone5.addOrReplaceChild("back_leg_right_1", CubeListBuilder.create().texOffs(17, 31).addBox(-0.5F, -2.97F, -0.2186F, 2.0F, 6.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 1.75F, 0.5F, 0.9599F, 0.0F, 0.0F));
        PartDefinition bone6 = backRightLower.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.0F, 2.25F, 1.5F));
        bone6.addOrReplaceChild("back_leg_right_2", CubeListBuilder.create().texOffs(28, 30).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 2.75F, -1.0F, -0.48F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(PigRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.frontRight.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.1F * state.walkAnimationSpeed;
        this.frontLeft.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.1F * state.walkAnimationSpeed;
        this.backLeft.xRot = this.frontRight.xRot;
        this.backRight.xRot = this.frontLeft.xRot;
    }
}
