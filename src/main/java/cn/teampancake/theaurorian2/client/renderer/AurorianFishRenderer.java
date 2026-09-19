package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Uses the actual vanilla cod animation and dry-land rotation with the legacy fish geometry. */
public final class AurorianFishRenderer extends CodRenderer {
    public static final ModelLayerLocation MOON_LAYER =
            new ModelLayerLocation(TheAurorian2.id("moon_fish"), "main");
    public static final ModelLayerLocation WINGED_LAYER =
            new ModelLayerLocation(TheAurorian2.id("aurorian_winged_fish"), "main");
    private final Identifier texture;

    public AurorianFishRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer,
            String rootPart, String textureName) {
        super(context);
        this.model = new CodModel(context.bakeLayer(layer).getChild(rootPart));
        this.texture = TheAurorian2.id("textures/entity/" + textureName + ".png");
        this.shadowRadius = 0.2F;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }
}
