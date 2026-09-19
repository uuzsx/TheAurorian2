package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.AurorianRabbitModel;
import cn.teampancake.theaurorian2.common.entity.AurorianRabbitEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianAnimalRenderState;
import net.minecraft.resources.Identifier;

public final class AurorianRabbitRenderer extends MobRenderer<AurorianRabbitEntity, AurorianAnimalRenderState, AurorianRabbitModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheAurorian2.id("aurorian_rabbit"), "main");
    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/aurorian_rabbit.png");

    public AurorianRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new AurorianRabbitModel(context.bakeLayer(LAYER)), 0.3F);
    }

    @Override public AurorianAnimalRenderState createRenderState() { return new AurorianAnimalRenderState(); }
    @Override public void extractRenderState(AurorianRabbitEntity entity, AurorianAnimalRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.extract(entity.visualState, partialTick);
        state.jumpCompletion = entity.getJumpCompletion(partialTick);
    }
    @Override public Identifier getTextureLocation(AurorianAnimalRenderState state) { return TEXTURE; }
}
