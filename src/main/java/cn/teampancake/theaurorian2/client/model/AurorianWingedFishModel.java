package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Legacy winged-fish geometry: original pivots, rotations, UVs and zero-width fins.
 * Bedrock Y is reflected into ModelPart Y; body_back is named tail_fin for CodModel. */
public final class AurorianWingedFishModel {
    private AurorianWingedFishModel() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body_front = mesh.getRoot().addOrReplaceChild("body_front", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -3.0F, -4.0F, 3.0F, 6.0F, 8.0F),
                PartPose.offset(0.0F, 20.0F, -7.0F));
        PartDefinition head = body_front.addOrReplaceChild("head", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -2.5F, -4.0F, 2.0F, 5.0F, 4.0F).texOffs(38, 4).addBox(-2.0F, -2.5F, -4.0F, 4.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -0.5F, -4.0F));
        PartDefinition leftFin = body_front.addOrReplaceChild("leftFin", CubeListBuilder.create().texOffs(2, 0).addBox(-2.00752F, -2.56703F, -18.0F, 2.0F, 0.0F, 2.0F),
                PartPose.offsetAndRotation(1.5F, 5.0F, 14.0F, 0.0F, 0.0F, -0.61086524F));
        PartDefinition rightFin = body_front.addOrReplaceChild("rightFin", CubeListBuilder.create().texOffs(-2, 0).addBox(0.00742F, -2.56703F, -11.0F, 2.0F, 0.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, 5.0F, 7.0F, 0.0F, 0.0F, 0.61086524F));
        PartDefinition dorsal_front = body_front.addOrReplaceChild("dorsal_front", CubeListBuilder.create().texOffs(48, 11).addBox(0.0F, -5.5F, -6.0F, 0.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 0.5F, 8.0F));
        PartDefinition wing = body_front.addOrReplaceChild("wing", CubeListBuilder.create(),
                PartPose.offset(0.0F, 6.0F, 11.0F));
        PartDefinition right1 = wing.addOrReplaceChild("right1", CubeListBuilder.create(),
                PartPose.offset(-1.5F, -6.0F, -9.0F));
        right1.addOrReplaceChild("cube_rotation_0", CubeListBuilder.create().texOffs(0, 24).addBox(-9.0F, -4.0F, 0.0F, 9.0F, 8.0F, 0.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.17809725F, 0.0F));
        PartDefinition right2 = wing.addOrReplaceChild("right2", CubeListBuilder.create(),
                PartPose.offset(-1.5F, -6.0F, -12.5F));
        right2.addOrReplaceChild("cube_rotation_0", CubeListBuilder.create().texOffs(18, 20).addBox(-12.0F, -6.5F, 0.0F, 13.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.78539816F, 0.0F));
        PartDefinition left1 = wing.addOrReplaceChild("left1", CubeListBuilder.create().texOffs(0, 24).addBox(-9.0F, -4.0F, 0.0F, 9.0F, 8.0F, 0.0F),
                PartPose.offsetAndRotation(1.5F, -6.0F, -9.0F, 0.0F, 1.96349541F, 0.0F));
        PartDefinition left2 = wing.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(18, 20).addBox(-12.0F, -6.5F, 0.0F, 13.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(1.5F, -6.0F, -12.5F, 0.0F, 2.35619449F, 0.0F));
        PartDefinition body_back = body_front.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(22, 5).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition tailfin = body_back.addOrReplaceChild("tailfin", CubeListBuilder.create().texOffs(52, 21).addBox(0.0F, -2.5F, 0.0F, 0.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition dorsal_back = body_back.addOrReplaceChild("dorsal_back", CubeListBuilder.create().texOffs(52, 9).addBox(0.0F, -6.5F, -1.0F, 0.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 1.5F, 1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
