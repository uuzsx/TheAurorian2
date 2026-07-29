package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.block.entity.AurorianFurnaceBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.phys.Vec3;

public final class AurorianFurnaceRenderer implements BlockEntityRenderer<
        AurorianFurnaceBlockEntity, AurorianFurnaceRenderer.RenderState> {

    private static final float RACK_HEIGHT = 9.15F / 16.0F;
    private static final float RACK_DEPTH_FROM_FRONT = 5.25F / 16.0F;
    private static final float ITEM_SCALE = 0.42F;

    private final ItemModelResolver itemModelResolver;

    public AurorianFurnaceRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            AurorianFurnaceBlockEntity furnace,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(furnace, state, partialTick, cameraPosition, breakProgress);
        state.facing = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
        this.itemModelResolver.updateForTopItem(
                state.inputItem,
                furnace.getDisplayedInput(),
                ItemDisplayContext.FIXED,
                furnace.getLevel(),
                null,
                (int) furnace.getBlockPos().asLong());
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState) {
        if (state.inputItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        try {
            float frontOffset = 0.5F - RACK_DEPTH_FROM_FRONT;
            poseStack.translate(
                    0.5F + state.facing.getStepX() * frontOffset,
                    RACK_HEIGHT,
                    0.5F + state.facing.getStepZ() * frontOffset);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.facing.toYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            state.inputItem.submit(
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

        private final ItemStackRenderState inputItem = new ItemStackRenderState();
        private Direction facing = Direction.NORTH;
    }
}
