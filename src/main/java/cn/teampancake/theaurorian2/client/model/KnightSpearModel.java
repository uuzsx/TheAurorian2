package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.KnightSpearItem;
import com.geckolib.model.DefaultedGeoModel;

public final class KnightSpearModel extends DefaultedGeoModel<KnightSpearItem> {

    public KnightSpearModel(String textureName) {
        super(TheAurorian2.id("knight_spear"));
        withAltTexture(TheAurorian2.id(textureName));
    }

    @Override
    protected String subtype() {
        return "item";
    }
}
