package cn.teampancake.theaurorian2.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import cn.teampancake.theaurorian2.client.model.AurorianChestModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Direction;

public final class AurorianChestMinecartRenderer extends MinecartRenderer {

    private final AurorianChestModel chestModel;
    private final SpriteGetter sprites;

    public AurorianChestMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.CHEST_MINECART);
        this.chestModel = new AurorianChestModel("single");
        this.sprites = context.getSprites();
    }

    @Override
    protected void submitMinecartContents(
            MinecartRenderState state,
            BlockModelRenderState blockModel,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords) {
        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(Direction.NORTH));
        submitNodeCollector.submitModel(
                this.chestModel,
                0.0F,
                poseStack,
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                AurorianChestRenderer.TEXTURE,
                this.sprites,
                0,
                null);
        poseStack.popPose();
    }
}
