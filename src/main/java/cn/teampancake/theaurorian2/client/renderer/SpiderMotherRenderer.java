package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class SpiderMotherRenderer extends SimpleGeoMobRenderer<SpiderMotherEntity> {
    public SpiderMotherRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("spider_mother"), 1.4F);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        // misc.death owns the collapse pose; do not add the vanilla side roll.
        return 0.0F;
    }
}
