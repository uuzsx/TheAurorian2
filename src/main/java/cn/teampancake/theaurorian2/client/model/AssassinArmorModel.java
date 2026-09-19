package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.AssassinArmorItem;
import com.geckolib.model.DefaultedGeoModel;

public final class AssassinArmorModel extends DefaultedGeoModel<AssassinArmorItem> {
    public AssassinArmorModel(String modelName) {
        super(TheAurorian2.id(modelName));
    }

    @Override
    protected String subtype() {
        return "armor";
    }
}
