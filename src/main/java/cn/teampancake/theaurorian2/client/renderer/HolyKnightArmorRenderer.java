package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.HolyKnightArmorModel;
import cn.teampancake.theaurorian2.common.item.HolyKnightArmorItem;
import com.geckolib.renderer.GeoArmorRenderer;
import java.util.List;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorType;

public final class HolyKnightArmorRenderer extends GeoArmorRenderer<HolyKnightArmorItem, HumanoidRenderState> {
    private static final List<ArmorSegment> ROBE_SEGMENTS = List.of(ArmorSegment.CHEST);
    private final boolean robe;

    public HolyKnightArmorRenderer(HolyKnightArmorItem item) {
        super(new HolyKnightArmorModel());
        this.robe = item.armorType() == ArmorType.LEGGINGS;
    }

    @Override
    public List<ArmorSegment> getSegmentsForSlot(HumanoidRenderState state, EquipmentSlot slot) {
        // This model has one continuous robe, which follows the torso and is rendered once.
        return slot == EquipmentSlot.LEGS ? ROBE_SEGMENTS : super.getSegmentsForSlot(state, slot);
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState state, ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> "armorHead";
            case CHEST -> this.robe ? "waist_attachment" : "armorBody";
            case LEFT_ARM -> "armorLeftArm";
            case RIGHT_ARM -> "armorRightArm";
            case LEFT_FOOT -> "armorLeftBoot";
            case RIGHT_FOOT -> "armorRightBoot";
            case LEFT_LEG, RIGHT_LEG -> "armorLegs";
        };
    }
}
