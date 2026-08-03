package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;

public final class AurorianChestMinecartRenderer extends MinecartRenderer {

    private static final SpriteId CHEST_TEXTURE = new SpriteId(
            Sheets.CHEST_SHEET, TheAurorian2.id("entity/chest/silent_wood"));

    private final ChestModel chestModel;
    private final SpriteGetter sprites;

    public AurorianChestMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.CHEST_MINECART);
        this.chestModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
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
                CHEST_TEXTURE,
                this.sprites,
                0,
                null);
        poseStack.popPose();
    }
}
