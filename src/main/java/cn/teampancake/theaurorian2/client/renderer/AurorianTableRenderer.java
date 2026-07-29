package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.block.entity.AurorianTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class AurorianTableRenderer implements BlockEntityRenderer<
        AurorianTableBlockEntity, AurorianTableRenderer.RenderState> {

    private static final float TABLE_CENTER = 1.0F;
    private static final float DISPLAY_HEIGHT = 0.64F;
    private static final float ITEM_SCALE = 0.55F;

    private final ItemModelResolver itemModelResolver;

    public AurorianTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            AurorianTableBlockEntity table,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTick, cameraPosition, breakProgress);
        state.displayRotation = table.getDisplayRotation();
        this.itemModelResolver.updateForTopItem(
                state.displayedItem,
                table.getDisplayedItem(),
                ItemDisplayContext.FIXED,
                table.getLevel(),
                null,
                (int) table.getBlockPos().asLong());
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState) {
        if (state.displayedItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(TABLE_CENTER, DISPLAY_HEIGHT, TABLE_CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.displayRotation * 45.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            state.displayedItem.submit(
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
        } finally {
            poseStack.popPose();
        }
    }

    public static final class RenderState extends BlockEntityRenderState {

        private final ItemStackRenderState displayedItem = new ItemStackRenderState();
        private int displayRotation;
    }
}
