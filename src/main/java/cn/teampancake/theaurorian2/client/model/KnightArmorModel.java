package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.KnightArmorItem;
import com.geckolib.model.DefaultedGeoModel;

public final class KnightArmorModel extends DefaultedGeoModel<KnightArmorItem> {

    public KnightArmorModel(String textureName) {
        super(TheAurorian2.id("knight_armor"));
        withAltTexture(TheAurorian2.id(textureName));
    }

    @Override
    protected String subtype() {
        return "armor";
    }
}
