package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianAnimalRenderState;
import cn.teampancake.theaurorian2.client.animation.AnimalAnimationPlayer;
import static cn.teampancake.theaurorian2.client.animation.AurorianRabbitAnimations.*;

public final class AurorianRabbitModel extends EntityModel<AurorianAnimalRenderState> {
    private final ModelPart head;
    private final AnimalAnimationPlayer animations;

    public AurorianRabbitModel(ModelPart root) {
        super(root);
        head = root.getChild("motion_root").getChild("all").getChild("body").getChild("head");
        animations = new AnimalAnimationPlayer(root, IDLE_BREATHE, IDLE_LISTEN, IDLE_SNIFF,
                WALK_HOP, RUN_SCARED, STARTLE, 4.6F, 4.5F, 1.1F, 0.48F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("motion_root", CubeListBuilder.create(), PartPose.ZERO);
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
    public void setupAnim(AurorianAnimalRenderState state) {
        super.setupAnim(state);
        animations.apply(state, head, true);
    }
}
