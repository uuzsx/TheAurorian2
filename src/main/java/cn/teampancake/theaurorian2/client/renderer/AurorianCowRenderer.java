package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.AurorianCowModel;
import cn.teampancake.theaurorian2.common.entity.AurorianCowEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import cn.teampancake.theaurorian2.client.renderer.state.AurorianCowRenderState;
import net.minecraft.resources.Identifier;

public final class AurorianCowRenderer extends MobRenderer<AurorianCowEntity, AurorianCowRenderState, AurorianCowModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheAurorian2.id("aurorian_cow"), "main");
    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/aurorian_cow.png");
    public AurorianCowRenderer(EntityRendererProvider.Context context) { super(context, new AurorianCowModel(context.bakeLayer(LAYER)), 1.0F); }
    @Override public AurorianCowRenderState createRenderState() { return new AurorianCowRenderState(); }
    @Override public Identifier getTextureLocation(AurorianCowRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(AurorianCowEntity entity, AurorianCowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.panicBlend = entity.panicBlend(partialTick);
        state.idleBlend = entity.idleBlend(partialTick);
        state.idleVariant = entity.idleVariant();
        state.idleTime = entity.idleTime(partialTick);
        state.startleTime = entity.startleTime(partialTick);
    }
}
