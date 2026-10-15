package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SeaDrifterEntity;
import com.geckolib.loading.math.MolangQueries;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class SeaDrifterRenderer extends SimpleGeoMobRenderer<SeaDrifterEntity> {
    public SeaDrifterRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("sea_drifter"), .2F);
    }
    public static void registerAnimationQueries() {
        MolangQueries.setActorVariable("query.theaurorian2_sea_pitch", actor ->
                actor.animatable() instanceof SeaDrifterEntity ray
                        ? Mth.lerp(actor.partialTick(), ray.xRotO, ray.getXRot()) : 0);
        MolangQueries.setActorVariable("query.theaurorian2_sea_bank", actor ->
                actor.animatable() instanceof SeaDrifterEntity ray
                        ? Mth.clamp(Mth.wrapDegrees(ray.getYRot()-ray.yRotO)*-2, -18, 18) : 0);
    }
}
