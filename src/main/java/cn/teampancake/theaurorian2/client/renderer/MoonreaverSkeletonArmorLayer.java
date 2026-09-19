package cn.teampancake.theaurorian2.client.renderer;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemArmorGeoLayer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Attaches the existing wearable armor to the imported skeleton without duplicating its assets. */
@SuppressWarnings({"rawtypes", "unchecked"}) // Vanilla render states receive GeoRenderState through GeckoLib.
public final class MoonreaverSkeletonArmorLayer extends ItemArmorGeoLayer {
    private static final List<RenderData> PARTS = List.of(
            RenderData.body("armor_body"),
            RenderData.rightArm("hand_left"), RenderData.leftArm("hand_right"),
            RenderData.rightLeg("leg_left"), RenderData.leftLeg("leg_right"),
            RenderData.rightFoot("leg_left"), RenderData.leftFoot("leg_right"));
    private static final Vec3 BODY_SCALE = new Vec3(1, 1, 1);
    private static final Vec3 ARM_SCALE = new Vec3(1, 14.0 / 12.0, 1);
    private static final Vec3 LEG_SCALE = new Vec3(1, 13.0 / 12.0, 1);

    public MoonreaverSkeletonArmorLayer(MoonreaverSkeletonRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer, context);
    }

    @Override
    protected List<RenderData> getRelevantBones(RenderPassInfo pass) { return PARTS; }

    @Override
    protected Vec3 getScaleFactorForBone(GeoBone bone, ModelPart part) {
        // Retain plate armor thickness instead of squeezing it to a skeleton's two-pixel limbs.
        return switch (bone.name()) {
            case "hand_left", "hand_right" -> ARM_SCALE;
            case "leg_left", "leg_right" -> LEG_SCALE;
            default -> BODY_SCALE;
        };
    }
}
