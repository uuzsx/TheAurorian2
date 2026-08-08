package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderVenomProjectileEntity;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class SpiderVenomProjectileRenderer
        extends GeoEntityRenderer<SpiderVenomProjectileEntity, EntityRenderState> {

    public SpiderVenomProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(TheAurorian2.id("spider_silk")));
        this.shadowRadius = 0.0F;
        this.scaleWidth = 0.55F;
        this.scaleHeight = 0.55F;
    }
}
