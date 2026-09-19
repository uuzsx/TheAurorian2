package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.AurorianPigModel;
import cn.teampancake.theaurorian2.common.entity.AurorianPigEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianAnimalRenderState;
import net.minecraft.resources.Identifier;

public final class AurorianPigRenderer extends MobRenderer<AurorianPigEntity, AurorianAnimalRenderState, AurorianPigModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheAurorian2.id("aurorian_pig"), "main");
    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/aurorian_pig.png");
    public AurorianPigRenderer(EntityRendererProvider.Context context) { super(context, new AurorianPigModel(context.bakeLayer(LAYER)), 0.7F); }
    @Override public AurorianAnimalRenderState createRenderState() { return new AurorianAnimalRenderState(); }
    @Override public Identifier getTextureLocation(AurorianAnimalRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(AurorianPigEntity entity, AurorianAnimalRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.extract(entity.visualState, partialTick);
    }
}
