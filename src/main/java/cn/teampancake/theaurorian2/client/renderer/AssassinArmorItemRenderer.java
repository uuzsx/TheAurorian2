package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.AssassinArmorModel;
import cn.teampancake.theaurorian2.common.item.AssassinArmorItem;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.world.item.equipment.ArmorType;

public final class AssassinArmorItemRenderer extends GeoItemRenderer<AssassinArmorItem> {
    private static final String[] PARTS = {
            "head", "body", "left_arm", "right_arm", "left_leg", "right_leg", "left_feet", "right_feet"
    };
    private final ArmorType armorType;

    public AssassinArmorItemRenderer(AssassinArmorItem item) {
        super(new AssassinArmorModel(item.modelName()));
        this.armorType = item.armorType();
        useAlternateGuiLighting();
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> pass, BoneSnapshots snapshots) {
        for (String part : PARTS) {
            snapshots.ifPresent(part, snapshot -> snapshot.skipRender(!switch (this.armorType) {
                case HELMET -> part.equals("head");
                case CHESTPLATE -> part.equals("body") || part.equals("left_arm") || part.equals("right_arm");
                case LEGGINGS -> part.equals("left_leg") || part.equals("right_leg");
                case BOOTS -> part.equals("left_feet") || part.equals("right_feet");
                default -> false;
            }));
        }
    }
}
