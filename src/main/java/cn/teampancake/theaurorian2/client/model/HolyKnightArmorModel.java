package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.item.HolyKnightArmorItem;
import com.geckolib.model.DefaultedGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class HolyKnightArmorModel extends DefaultedGeoModel<HolyKnightArmorItem> {
    private static final Identifier TEXTURE = TheAurorian2.id("textures/item/holy_knight_armor.png");

    public HolyKnightArmorModel() {
        super(TheAurorian2.id("holy_knight_armor"));
    }

    @Override
    protected String subtype() {
        return "armor";
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }
}
