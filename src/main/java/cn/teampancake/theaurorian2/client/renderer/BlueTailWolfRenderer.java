package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.BlueTailWolfEntity;
import com.geckolib.loading.math.MolangQueries;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class BlueTailWolfRenderer extends SimpleGeoMobRenderer<BlueTailWolfEntity> {
    public BlueTailWolfRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("blue_tail_wolf"), 0.55F);
    }

    @Override public void extractRenderState(BlueTailWolfEntity entity, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.scale *= state.ageScale;
    }

    public static void registerAnimationQueries() {
        for (int i = 0; i < 4; i++) {
            final int channel = i;
            MolangQueries.setActorVariable("query.theaurorian2_wolf_pose_" + i, actor ->
                    actor.animatable() instanceof BlueTailWolfEntity wolf ? wolf.poseWeight(channel, actor.partialTick()) : 0);
        }
        MolangQueries.setActorVariable("query.theaurorian2_wolf_gesture", actor ->
                actor.animatable() instanceof BlueTailWolfEntity wolf ? wolf.idleGestureWeight(actor.partialTick()) : 0);
    }
}
