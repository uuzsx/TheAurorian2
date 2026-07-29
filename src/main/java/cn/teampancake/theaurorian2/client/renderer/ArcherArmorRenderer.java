package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.ArcherArmorModel;
import cn.teampancake.theaurorian2.common.item.ArcherArmorItem;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class ArcherArmorRenderer extends GeoArmorRenderer<ArcherArmorItem, HumanoidRenderState> {

    public ArcherArmorRenderer(ArcherArmorItem item) {
        super(new ArcherArmorModel(item.textureName()));
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState renderState, ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> "head";
            case CHEST -> "body";
            case LEFT_ARM -> "left_arm";
            case RIGHT_ARM -> "right_arm";
            case LEFT_LEG -> "left_leg";
            case RIGHT_LEG -> "right_leg";
            case LEFT_FOOT -> "left_feet";
            case RIGHT_FOOT -> "right_feet";
        };
    }
}
