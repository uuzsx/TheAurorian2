package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.AssassinArmorModel;
import cn.teampancake.theaurorian2.common.item.AssassinArmorItem;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class AssassinArmorRenderer extends GeoArmorRenderer<AssassinArmorItem, HumanoidRenderState> {
    public AssassinArmorRenderer(AssassinArmorItem item) {
        super(new AssassinArmorModel(item.modelName()));
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState state, ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> "head_attachment";
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
