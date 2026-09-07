package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.AurorianChestModel;
import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;

public final class AurorianChestRenderer extends ChestRenderer<AurorianChestBlockEntity> {
    public static final SpriteId TEXTURE = new SpriteId(
            Sheets.CHEST_SHEET, TheAurorian2.id("entity/chest/aurorian_chest"));
    private final AurorianChestModel single = new AurorianChestModel("single");
    private final AurorianChestModel left = new AurorianChestModel("left");
    private final AurorianChestModel right = new AurorianChestModel("right");
    private final SpriteGetter sprites;
    private final Map<Block, SpriteId> woodTextures = Map.of(
            ModBlocks.WEEPING_WILLOW_CHEST.get(), texture("weeping_willow_chest"),
            ModBlocks.CURTAIN_WOOD_CHEST.get(), texture("curtain_wood_chest"),
            ModBlocks.CURSED_FROST_WOOD_CHEST.get(), texture("cursed_frost_wood_chest"),
            ModBlocks.FILTHY_WOOD_CHEST.get(), texture("filthy_wood_chest"));

    private static SpriteId texture(String name) {
        return new SpriteId(Sheets.CHEST_SHEET, TheAurorian2.id("entity/chest/" + name));
    }

    @Override
    protected SpriteId getCustomSprite(AurorianChestBlockEntity chest, ChestRenderState state) {
        return woodTextures.getOrDefault(chest.getBlockState().getBlock(), TEXTURE);
    }

    public AurorianChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.sprites = context.sprites();
    }

    @Override
    public void submit(ChestRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(modelTransformation(state.facing));
        float closed = 1.0F - state.open;
        // Vanilla LEFT occupies the positive-X half of the south-facing model.
        AurorianChestModel model = switch (state.type) {
            case LEFT -> right;
            case RIGHT -> left;
            case SINGLE -> single;
        };
        collector.submitModel(model, 1.0F - closed * closed * closed, poseStack,
                state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.customSprite != null ? state.customSprite : TEXTURE,
                this.sprites, 0, state.breakProgress);
        poseStack.popPose();
    }
}
