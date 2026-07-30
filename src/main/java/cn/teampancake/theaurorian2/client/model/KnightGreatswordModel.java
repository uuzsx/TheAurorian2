package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.KnightGreatswordItem;
import com.geckolib.model.DefaultedGeoModel;

public final class KnightGreatswordModel extends DefaultedGeoModel<KnightGreatswordItem> {

    public KnightGreatswordModel(String textureName) {
        super(TheAurorian2.id("knight_greatsword"));
        withAltTexture(TheAurorian2.id(textureName));
    }

    @Override
    protected String subtype() {
        return "item";
    }
}
