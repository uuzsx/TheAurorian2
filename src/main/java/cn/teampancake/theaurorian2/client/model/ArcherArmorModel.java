package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.ArcherArmorItem;
import com.geckolib.model.DefaultedGeoModel;

public final class ArcherArmorModel extends DefaultedGeoModel<ArcherArmorItem> {

    public ArcherArmorModel(String textureName) {
        super(TheAurorian2.id("archer_armor"));
        withAltTexture(TheAurorian2.id(textureName));
    }

    @Override
    protected String subtype() {
        return "armor";
    }
}
