package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.AurorianSheepModel;
import cn.teampancake.theaurorian2.common.entity.AurorianSheepEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianAnimalRenderState;
import net.minecraft.resources.Identifier;

public final class AurorianSheepRenderer extends MobRenderer<AurorianSheepEntity, AurorianAnimalRenderState, AurorianSheepModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheAurorian2.id("aurorian_sheep"), "main");
    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/aurorian_sheep.png");
    public AurorianSheepRenderer(EntityRendererProvider.Context context) { super(context, new AurorianSheepModel(context.bakeLayer(LAYER)), 0.7F); }
    @Override public AurorianAnimalRenderState createRenderState() { return new AurorianAnimalRenderState(); }
    @Override public void extractRenderState(AurorianSheepEntity entity, AurorianAnimalRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.extract(entity.visualState, partialTick);
        state.headEatAngleScale = entity.getHeadEatAngleScale(partialTick);
        state.headEatPositionScale = entity.getHeadEatPositionScale(partialTick);
    }
    @Override public Identifier getTextureLocation(AurorianAnimalRenderState state) { return TEXTURE; }
}
