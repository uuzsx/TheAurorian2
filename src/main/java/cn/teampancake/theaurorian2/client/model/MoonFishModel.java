package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class MoonFishModel {
    private MoonFishModel() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        // The legacy model faces +X; align its nose with vanilla fish movement (-Z).
        PartDefinition all = mesh.getRoot().addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0, 24, 0, 0, (float) (Math.PI / 2), 0));
        all.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 7)
                .addBox(0, -2, -1.5F, 3, 4, 3), PartPose.offset(7, -6, 0));
        all.addOrReplaceChild("main_body", CubeListBuilder.create()
                .texOffs(0, 11).addBox(-2, -6, -0.5F, 2, 2, 1)
                .texOffs(6, 10).addBox(5, -6, -1, 2, 2, 2)
                .texOffs(14, 9).addBox(1, -7, -1, 3, 3, 2)
                .texOffs(0, 0).addBox(-3, -4, -1.5F, 10, 4, 3)
                .texOffs(14, 25).addBox(-2, 0, 0, 8, 3, 0)
                .texOffs(19, 14).addBox(0, -2, -3.5F, 4, 0, 7), PartPose.offset(0, -4, 0));
        // CodModel animates this child; preserve the old front_tail geometry and pivot.
        PartDefinition tail = all.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(18, 17).addBox(-7, -8, 0, 4, 5, 0), PartPose.ZERO);
        tail.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 16).mirror()
                .addBox(-5, -3, 0, 4, 5, 0), PartPose.offset(-6, -6, 0));
        return LayerDefinition.create(mesh, 48, 48);
    }
}
