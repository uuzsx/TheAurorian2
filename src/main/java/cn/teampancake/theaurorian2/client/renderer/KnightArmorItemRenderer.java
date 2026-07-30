package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.client.model.KnightArmorModel;
import cn.teampancake.theaurorian2.common.item.KnightArmorItem;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.world.item.equipment.ArmorType;

public final class KnightArmorItemRenderer extends GeoItemRenderer<KnightArmorItem> {

    private static final String[] ARMOR_BONES = {
        "Head", "Body", "Left Arm", "Right Arm",
        "Left Leg", "Right Leg", "Left Boot", "Right Boot"
    };

    private final ArmorType armorType;

    public KnightArmorItemRenderer(KnightArmorItem item) {
        super(new KnightArmorModel(item.textureName()));
        this.armorType = item.armorType();
        withScale(itemScale(this.armorType));
        useAlternateGuiLighting();
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        float yOffset = verticalOffset(this.armorType);
        float xOffset = horizontalOffset(this.armorType);

        for (String boneName : ARMOR_BONES) {
            snapshots.ifPresent(boneName, snapshot -> {
                boolean visible = isVisibleBone(this.armorType, boneName);
                snapshot.skipRender(!visible);
                if (visible) {
                    snapshot.setTranslation(xOffset, yOffset, 0.0F);
                }
            });
        }
    }

    private static boolean isVisibleBone(ArmorType type, String boneName) {
        return switch (type) {
            case HELMET -> boneName.equals("Head");
            case CHESTPLATE -> boneName.equals("Body")
                    || boneName.equals("Left Arm")
                    || boneName.equals("Right Arm");
            case LEGGINGS -> boneName.equals("Left Leg") || boneName.equals("Right Leg");
            case BOOTS -> boneName.equals("Left Boot") || boneName.equals("Right Boot");
            default -> false;
        };
    }

    private static float verticalOffset(ArmorType type) {
        return switch (type) {
            case HELMET -> -26.5F;
            case CHESTPLATE -> -14.0F;
            case LEGGINGS -> -8.5F;
            case BOOTS -> -4.5F;
            default -> 0.0F;
        };
    }

    private static float horizontalOffset(ArmorType type) {
        return switch (type) {
            case CHESTPLATE -> 2.0F;
            case LEGGINGS -> 1.0F;
            case BOOTS -> -6.0F;
            default -> 0.0F;
        };
    }

    private static float itemScale(ArmorType type) {
        return switch (type) {
            case HELMET -> 1.0F;
            case CHESTPLATE -> 0.8F;
            case LEGGINGS -> 1.15F;
            case BOOTS -> 1.45F;
            default -> 1.0F;
        };
    }
}
