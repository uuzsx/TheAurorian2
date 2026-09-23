package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiritEntity;
import com.geckolib.animation.object.EasingType;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.MolangQueries;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class SpiritRenderer extends SimpleGeoMobRenderer<SpiritEntity> {
    public SpiritRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("spirit"), 0.35F);
    }

    public static void registerAnimationQueries() {
        // GeckoLib 5.5.2 uses the destination key's easing. Its stock Catmull-Rom
        // repeats the segment's start as the preceding point; Blockbench uses i - 2.
        // Keep this correction local to Spirit's exported curves.
        EasingType.register("theaurorian2:spirit_catmullrom", new EasingType.CatmullRomEasing() {
            @Override
            public void modifyKeyframes(Keyframe[] frames, int i, MathParser parser) {
                Keyframe frame = frames[i];
                frames[i] = new Keyframe(frame.startTime(), frame.length(), frame.startValue(),
                        frame.endValue(), frame.easingType(), new MathValue[] {
                                frames[Math.max(0, i - 2)].endValue(),
                                frames[Math.min(frames.length - 1, i + 1)].endValue()});
            }
        });
        for (int i = 0; i < 4; i++) {
            final int group = i;
            MolangQueries.setActorVariable("query.theaurorian2_spirit_glide_" + group,
                    actor -> actor.animatable() instanceof SpiritEntity spirit
                            ? spirit.glideWeight(group, actor.partialTick()) : 0);
        }
    }
}
