package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.WallClimberSpiderlingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class WallClimberSpiderlingRenderer extends SimpleGeoMobRenderer<WallClimberSpiderlingEntity> {

    public WallClimberSpiderlingRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("spiderling_wall_climber"), 0.45F);
    }
}
