package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class SpiderMotherRenderer extends SimpleGeoMobRenderer<SpiderMotherEntity> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SpiderMotherRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("spider_mother"), 1.4F);
        withRenderLayer((GeoRenderLayer) new SpiderMotherGlowLayer((GeoRenderer) this, context.getResourceManager()));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        // misc.death owns the collapse pose; do not add the vanilla side roll.
        return 0.0F;
    }
}
