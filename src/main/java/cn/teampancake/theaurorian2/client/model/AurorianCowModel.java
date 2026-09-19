package cn.teampancake.theaurorian2.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianCowRenderState;
import cn.teampancake.theaurorian2.client.animation.AurorianCowAnimations;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.util.Mth;

public final class AurorianCowModel extends EntityModel<AurorianCowRenderState> {
    private final ModelPart head;
    private final KeyframeAnimation breathe, chew, sniff, tailSwish, walk, run, startle;

    public AurorianCowModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.head = body.getChild("head");
        breathe = AurorianCowAnimations.IDLE_BREATHE.bake(root);
        chew = AurorianCowAnimations.IDLE_CHEW_LOOK.bake(root);
        sniff = AurorianCowAnimations.IDLE_SNIFF.bake(root);
        tailSwish = AurorianCowAnimations.IDLE_TAIL_SWISH.bake(root);
        walk = AurorianCowAnimations.WALK_RELAXED.bake(root);
        run = AurorianCowAnimations.RUN_SCARED.bake(root);
        startle = AurorianCowAnimations.STARTLE.bake(root);
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
    public void setupAnim(AurorianCowRenderState state) {
        super.setupAnim(state);
        if (state.deathTime > 0) return;
        float moving = Mth.clamp(state.walkAnimationSpeed * 4, 0, 1);
        float frightened = state.startleTime >= 0 && state.startleTime < 0.55F
                ? Math.min(1, (0.55F - state.startleTime) / 0.12F) : 0;
        float idle = state.idleBlend * (1 - moving) * (1 - state.panicBlend) * (1 - frightened);
        breathe.apply((long)(state.ageInTicks * 50), (1 - moving) * (1 - idle) * (1 - frightened));
        KeyframeAnimation gesture = switch (state.idleVariant) { case 1 -> chew; case 2 -> sniff; default -> tailSwish; };
        if (idle > 0) {
            float length = state.idleVariant == 1 ? 7 : state.idleVariant == 2 ? 6 : 3.6F;
            gesture.apply((long)(Math.min(state.idleTime, length - 0.001F) * 1000), idle);
        }
        walk.apply((long)(state.walkAnimationPos * 300), moving * (1 - state.panicBlend) * (1 - frightened));
        run.apply((long)(state.walkAnimationPos * 90), moving * state.panicBlend * (1 - frightened));
        if (frightened > 0) startle.apply((long)(state.startleTime * 1000), frightened);
        float look = (1 - idle) * (1 - frightened) * (1 - state.panicBlend * 0.75F);
        this.head.xRot += Mth.clamp(state.xRot, -25, 25) * Mth.DEG_TO_RAD * look;
        this.head.yRot += Mth.clamp(state.yRot, -40, 40) * Mth.DEG_TO_RAD * look;
    }
}
