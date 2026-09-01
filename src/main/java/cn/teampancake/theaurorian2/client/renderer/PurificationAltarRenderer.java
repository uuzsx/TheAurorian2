package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Renders the animated purification altar book. */
public final class PurificationAltarRenderer
        implements BlockEntityRenderer<PurificationAltarBlockEntity, PurificationAltarRenderer.RenderState> {

    private static final double RENDER_RADIUS = 4.1D;
    private static final float BOOK_Y = 1.47F;
    private static final SpriteId BOOK_TEXTURE = new SpriteId(
            TextureAtlas.LOCATION_BLOCKS,
            TheAurorian2.id("entity/enchantment/purification_altar_book"));
    private static final Identifier SHIELD_TEXTURE =
            TheAurorian2.id("textures/entity/purification_shield.png");
    // Eyes uses the emissive entity shader, so the shield remains bright regardless of
    // the altar's orbit angle or the surrounding world's directional lighting.  Both
    // windings are submitted by ShieldQuadGeometry to make the quad visible from either side.
    private static final RenderType SHIELD_RENDER_TYPE = RenderTypes.eyes(SHIELD_TEXTURE);
    private static final float SHIELD_RADIUS = 0.72F;
    private static final float SHIELD_Y = 0.92F;
    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public PurificationAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            PurificationAltarBlockEntity blockEntity,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.time = blockEntity.time + partialTick;
        state.animationTime = blockEntity.time + partialTick;
        state.shieldCount = blockEntity.getShieldCount();
        state.shieldFade = blockEntity.isRitualActive() ? 1.0F : blockEntity.getShieldFade();
        state.updateShieldOpacity();
        state.open = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        state.flip = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        float rotationDelta = blockEntity.rot - blockEntity.oRot;
        while (rotationDelta >= (float) Math.PI) {
            rotationDelta -= (float) (Math.PI * 2.0D);
        }
        while (rotationDelta < (float) -Math.PI) {
            rotationDelta += (float) (Math.PI * 2.0D);
        }
        state.yRot = blockEntity.oRot + rotationDelta * partialTick;
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera) {
        submitShields(state, poseStack, submitNodeCollector);
        submitBook(state, poseStack, submitNodeCollector);
    }

    private void submitShields(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector) {
        if (state.shieldCount <= 0 || state.shieldFade <= 0.0F) {
            return;
        }

        for (int index = 0; index < state.shieldCount; index++) {
            float angle = state.animationTime * 0.012F + index * (Mth.TWO_PI / 4.0F);
            poseStack.pushPose();
            poseStack.translate(
                    0.5F + Mth.cos(angle) * SHIELD_RADIUS,
                    SHIELD_Y,
                    0.5F + Mth.sin(angle) * SHIELD_RADIUS);
            // The quad is vertical in the local XY plane. Turn it 90 degrees
            // so its face is parallel to the altar side and points outward.
            poseStack.mulPose(Axis.YP.rotation(-angle + Mth.HALF_PI));
            submitNodeCollector.submitCustomGeometry(
                    poseStack,
                    SHIELD_RENDER_TYPE,
                    state.shieldGeometry);
            poseStack.popPose();
        }
    }

    private void submitBook(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0.5F, BOOK_Y, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(state.time * 0.1F) * 0.01F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(-state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float pageFlip1 = Mth.clamp(Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float pageFlip2 = Mth.clamp(Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        BookModel.State bookState = BookModel.State.forAnimation(
                state.time, pageFlip1, pageFlip2, state.open);
        submitNodeCollector.submitModel(
                this.bookModel,
                bookState,
                poseStack,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                BOOK_TEXTURE,
                this.sprites,
                0,
                state.breakProgress);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(PurificationAltarBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        return new AABB(
                centerX - RENDER_RADIUS,
                pos.getY(),
                centerZ - RENDER_RADIUS,
                centerX + RENDER_RADIUS,
                pos.getY() + 2.25D,
                centerZ + RENDER_RADIUS);
    }

    public static final class RenderState extends BlockEntityRenderState {
        private float flip;
        private float open;
        private float time;
        private float yRot;
        private float animationTime;
        private float shieldFade;
        private int shieldCount;
        private final ShieldQuadGeometry shieldGeometry = new ShieldQuadGeometry(0.50F, 0.50F);

        private void updateShieldOpacity() {
            this.shieldGeometry.setOpacity(this.shieldFade);
        }
    }

    private static final class ShieldQuadGeometry implements SubmitNodeCollector.CustomGeometryRenderer {

        private final float halfWidth;
        private final float height;
        private int alpha = 255;

        private ShieldQuadGeometry(float width, float height) {
            this.halfWidth = width * 0.5F;
            this.height = height;
        }

        private void setOpacity(float opacity) {
            this.alpha = Mth.clamp((int) (opacity * 255.0F), 0, 255);
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            // Keep the front face toward the orbit exterior after the 180-degree horizontal flip.
            addVertex(pose, buffer, halfWidth, 0.0F, 1.0F, 1.0F);
            addVertex(pose, buffer, halfWidth, height, 1.0F, 0.0F);
            addVertex(pose, buffer, -halfWidth, height, 0.0F, 0.0F);
            addVertex(pose, buffer, -halfWidth, 0.0F, 0.0F, 1.0F);

            // Eyes keeps face culling enabled. Submit the same quad with the opposite
            // winding so the shield stays visible when viewed from behind as well.
            addVertex(pose, buffer, halfWidth, 0.0F, 1.0F, 1.0F);
            addVertex(pose, buffer, -halfWidth, 0.0F, 0.0F, 1.0F);
            addVertex(pose, buffer, -halfWidth, height, 0.0F, 0.0F);
            addVertex(pose, buffer, halfWidth, height, 1.0F, 0.0F);
        }

        private void addVertex(
                PoseStack.Pose pose,
                VertexConsumer buffer,
                float x,
                float y,
                float u,
                float v) {
            buffer.addVertex(pose, x, y, 0.0F)
                    .setColor(255, 255, 255, this.alpha)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightCoordsUtil.FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
        }
    }
}
