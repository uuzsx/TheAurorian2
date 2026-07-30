package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.KnightGreatswordModel;
import cn.teampancake.theaurorian2.common.item.KnightGreatswordItem;
import com.geckolib.renderer.GeoItemRenderer;

public final class KnightGreatswordRenderer extends GeoItemRenderer<KnightGreatswordItem> {

    public KnightGreatswordRenderer(KnightGreatswordItem item) {
        super(new KnightGreatswordModel(item.textureName()));
        useAlternateGuiLighting();
    }
}
