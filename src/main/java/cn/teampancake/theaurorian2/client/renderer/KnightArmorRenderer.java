package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.KnightArmorModel;
import cn.teampancake.theaurorian2.common.item.KnightArmorItem;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.equipment.ArmorType;

public final class KnightArmorRenderer extends GeoArmorRenderer<KnightArmorItem, HumanoidRenderState> {

    private final ArmorType armorType;

    public KnightArmorRenderer(KnightArmorItem item) {
        super(new KnightArmorModel(item.textureName()));
        this.armorType = item.armorType();
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState renderState, ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> "Head";
            case CHEST -> "Body";
            case LEFT_ARM -> "Left Arm";
            case RIGHT_ARM -> "Right Arm";
            case LEFT_LEG -> "Left Leg";
            case RIGHT_LEG -> "Right Leg";
            case LEFT_FOOT -> "Left Leg";
            case RIGHT_FOOT -> "Right Leg";
        };
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustModelBonesForRender(RenderPassInfo renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        if (this.armorType == ArmorType.LEGGINGS) {
            snapshots.ifPresent("Left Boot", snapshot -> snapshot.skipRender(true));
            snapshots.ifPresent("Right Boot", snapshot -> snapshot.skipRender(true));
        } else if (this.armorType == ArmorType.BOOTS) {
            // The boot bones are children of the legs and must inherit their player animation.
            snapshots.ifPresent("Left Leg", snapshot -> snapshot.skipRender(true));
            snapshots.ifPresent("Right Leg", snapshot -> snapshot.skipRender(true));
        }
    }
}
