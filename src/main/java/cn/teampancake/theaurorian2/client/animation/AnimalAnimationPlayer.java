package cn.teampancake.theaurorian2.client.animation;

import cn.teampancake.theaurorian2.client.renderer.state.AurorianAnimalRenderState;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Baked once per model; animation selection has no resource reads or allocations. */
public final class AnimalAnimationPlayer {
    private final KeyframeAnimation breathe, first, second, walk, run, startle;
    private final float firstLength, secondLength, walkLength, runLength;
    public AnimalAnimationPlayer(ModelPart root, AnimationDefinition breathe, AnimationDefinition first,
            AnimationDefinition second, AnimationDefinition walk, AnimationDefinition run, AnimationDefinition startle,
            float firstLength, float secondLength, float walkLength, float runLength) {
        this.breathe = breathe.bake(root); this.first = first.bake(root); this.second = second.bake(root);
        this.walk = walk.bake(root); this.run = run.bake(root); this.startle = startle.bake(root);
        this.firstLength = firstLength; this.secondLength = secondLength;
        this.walkLength = walkLength; this.runLength = runLength;
    }
    public void apply(AurorianAnimalRenderState state, ModelPart head, boolean rabbit) {
        if (state.deathTime > 0) return;
        float moving = rabbit ? (state.jumpCompletion > 0 ? 1 : 0) : Mth.clamp(state.walkAnimationSpeed * 4, 0, 1);
        float frightened = state.startleTime >= 0 && state.startleTime < 0.5F
                ? Math.min(1, (0.5F - state.startleTime) / 0.12F) : 0;
        float idle = state.idleBlend * (1 - moving) * (1 - state.panicBlend) * (1 - frightened)
                * (1 - state.headEatPositionScale);
        breathe.apply((long)(state.ageInTicks * 50), (1 - moving) * (1 - idle) * (1 - frightened));
        if (idle > 0) {
            float length = state.idleVariant == 1 ? firstLength : secondLength;
            (state.idleVariant == 1 ? first : second).apply((long)(Math.min(state.idleTime, length - 0.001F) * 1000), idle);
        }
        // Rabbit keyframes follow its actual hop, including landings and pauses between jumps.
        long walkTime = (long)(rabbit ? state.jumpCompletion * walkLength * 1000 : state.walkAnimationPos * 300);
        long runTime = (long)(rabbit ? state.jumpCompletion * runLength * 1000 : state.walkAnimationPos * 90);
        walk.apply(walkTime, moving * (1 - state.panicBlend) * (1 - frightened));
        run.apply(runTime, moving * state.panicBlend * (1 - frightened));
        if (frightened > 0) startle.apply((long)(state.startleTime * 1000), frightened);
        float look = (1 - idle) * (1 - frightened) * (1 - state.panicBlend * 0.75F);
        head.xRot += Mth.clamp(state.xRot, -25, 25) * Mth.DEG_TO_RAD * look;
        head.yRot += Mth.clamp(state.yRot, -40, 40) * Mth.DEG_TO_RAD * look;
    }
}
