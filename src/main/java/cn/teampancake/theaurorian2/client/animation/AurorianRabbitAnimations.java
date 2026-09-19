package cn.teampancake.theaurorian2.client.animation;

import net.minecraft.client.animation.*;

/** Generated from the approved animal Blockbench animations by scripts/export_animal_animations.py. */
public final class AurorianRabbitAnimations {
    private AurorianRabbitAnimations() {}
    public static final AnimationDefinition IDLE_BREATHE = AnimationDefinition.Builder.withLength(3.0F).looping()
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
            new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.scaleVec(1.01F, 1.012F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
    public static final AnimationDefinition IDLE_LISTEN = AnimationDefinition.Builder.withLength(4.6F).looping()
        .addAnimation("cube_r1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6F, KeyframeAnimations.degreeVec(12.0F, -16.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.6F, KeyframeAnimations.degreeVec(10.0F, -18.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.4F, KeyframeAnimations.degreeVec(-3.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.6F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, -10.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(6.0F, 7.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.6F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-4.0F, 14.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.6F, KeyframeAnimations.degreeVec(-2.0F, -12.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.6F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
    public static final AnimationDefinition IDLE_SNIFF = AnimationDefinition.Builder.withLength(4.5F).looping()
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.8F, KeyframeAnimations.degreeVec(13.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.4F, KeyframeAnimations.degreeVec(15.0F, -5.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0F, KeyframeAnimations.degreeVec(12.0F, 5.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.5F, KeyframeAnimations.degreeVec(16.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.3F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.5F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.12F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.08F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.12F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, 0.08F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.12F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
    public static final AnimationDefinition WALK_HOP = AnimationDefinition.Builder.withLength(1.1F).looping()
        .addAnimation("motion_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.posVec(0.0F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.posVec(0.0F, 0.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.posVec(0.0F, 0.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(-7.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(12.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(3.2F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(12.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(3.2F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("arm_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(28.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("arm_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(28.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(3.0F, 0.0F, -0.36F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -2.4F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(24.0F, 0.0F, -2.88F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(8.0F, 0.0F, -0.96F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(-7.0F, 0.0F, -0.84F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.132F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.36F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.297F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 2.4F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.495F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 2.88F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.704F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.96F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.88F, KeyframeAnimations.degreeVec(-7.0F, 0.0F, 0.84F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
    public static final AnimationDefinition RUN_SCARED = AnimationDefinition.Builder.withLength(0.48F).looping()
        .addAnimation("motion_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.posVec(0.0F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.posVec(0.0F, 0.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.posVec(0.0F, 2.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.posVec(0.0F, 1.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(-7.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(17.25F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(-34.5F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(-23.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(11.5F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(4.6F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(17.25F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(-34.5F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(-23.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(11.5F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(4.6F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("arm_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(28.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("arm_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(28.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(4.8F, 0.0F, -0.36F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(32.0F, 0.0F, -2.4F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(38.4F, 0.0F, -2.88F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(12.8F, 0.0F, -0.96F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(-11.2F, 0.0F, -0.84F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.0576F, KeyframeAnimations.degreeVec(4.8F, 0.0F, 0.36F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1296F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 2.4F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.216F, KeyframeAnimations.degreeVec(38.4F, 0.0F, 2.88F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3072F, KeyframeAnimations.degreeVec(12.8F, 0.0F, 0.96F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.384F, KeyframeAnimations.degreeVec(-11.2F, 0.0F, 0.84F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.48F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
    public static final AnimationDefinition STARTLE = AnimationDefinition.Builder.withLength(0.5F)
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.09F, KeyframeAnimations.degreeVec(-12.0F, -6.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.24F, KeyframeAnimations.degreeVec(-7.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("motion_root", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.12F, KeyframeAnimations.posVec(0.0F, -0.35F, 0.2F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.26F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .addAnimation("cube_r2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.26F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0.0F, 0.0F, -0.0F), AnimationChannel.Interpolations.CATMULLROM)))
        .build();
}
