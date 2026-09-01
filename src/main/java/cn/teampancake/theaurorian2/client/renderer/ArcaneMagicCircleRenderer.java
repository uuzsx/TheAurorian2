package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.ArcaneMagicCircleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ArcaneMagicCircleRenderer
        implements BlockEntityRenderer<ArcaneMagicCircleBlockEntity, ArcaneMagicCircleRenderer.RenderState> {

    private static final Identifier OUTER_TEXTURE =
            TheAurorian2.id("textures/effect/arcane_magic_circle_outer.png");
    private static final Identifier NETWORK_TEXTURE =
            TheAurorian2.id("textures/effect/arcane_magic_circle_network.png");
    private static final Identifier NODES_TEXTURE =
            TheAurorian2.id("textures/effect/arcane_magic_circle_nodes.png");
    private static final Identifier INNER_TEXTURE =
            TheAurorian2.id("textures/effect/arcane_magic_circle_inner.png");
    private static final Identifier CORE_TEXTURE =
            TheAurorian2.id("textures/effect/arcane_magic_circle_core.png");
    private static final RenderType OUTER_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(OUTER_TEXTURE);
    private static final RenderType NETWORK_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(NETWORK_TEXTURE);
    private static final RenderType NODES_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(NODES_TEXTURE);
    private static final RenderType INNER_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(INNER_TEXTURE);
    private static final RenderType CORE_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(CORE_TEXTURE);
    private static final float CIRCLE_SIZE = 8.0F;
    private static final float OUTER_Y = 0.021F;
    private static final float NETWORK_Y = 0.024F;
    private static final float NODES_Y = 0.027F;
    private static final float INNER_Y = 0.030F;
    private static final float CORE_Y = 0.033F;
    private static final double RENDER_RADIUS = 4.1;
    private static final QuadGeometry QUAD = new QuadGeometry(CIRCLE_SIZE);

    public ArcaneMagicCircleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            ArcaneMagicCircleBlockEntity blockEntity,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.animationTime = blockEntity.getLevel() == null
                ? 0.0F
                : blockEntity.getLevel().getGameTime() + partialTick;
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        submitCircle(poseStack, submitNodeCollector, state.animationTime, 1.0F, 0.0F);
    }

    /** Reuses the animated circle layers for other ritual-capable renderers. */
    public static void submitCircle(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            float animationTime,
            float scale,
            float yOffset) {
        float outerPulse = 1.0F + Mth.sin(animationTime * 0.040F) * 0.008F;
        float innerPulse = 1.0F + Mth.sin(animationTime * 0.052F + 1.7F) * 0.006F;
        float corePulse = 1.0F + Mth.sin(animationTime * 0.085F + 0.8F) * 0.014F;
        float networkRotation = animationTime * 0.10F;
        submitLayer(
                poseStack, submitNodeCollector, OUTER_RENDER_TYPE,
                yOffset + OUTER_Y, animationTime * 0.06F, outerPulse * scale);
        submitLayer(poseStack, submitNodeCollector, NETWORK_RENDER_TYPE,
                yOffset + NETWORK_Y, networkRotation, scale);
        submitLayer(poseStack, submitNodeCollector, NODES_RENDER_TYPE,
                yOffset + NODES_Y, networkRotation, scale);
        submitLayer(
                poseStack, submitNodeCollector, INNER_RENDER_TYPE,
                yOffset + INNER_Y, -animationTime * 0.16F, innerPulse * scale);
        submitLayer(
                poseStack, submitNodeCollector, CORE_RENDER_TYPE,
                yOffset + CORE_Y, animationTime * 0.32F, corePulse * scale);
    }

    private static void submitLayer(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            RenderType renderType,
            float y,
            float rotation,
            float scale) {
        poseStack.pushPose();
        poseStack.translate(0.5F, y, 0.5F);
        poseStack.scale(scale, 1.0F, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, QUAD);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(ArcaneMagicCircleBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        return new AABB(
                centerX - RENDER_RADIUS,
                pos.getY(),
                centerZ - RENDER_RADIUS,
                centerX + RENDER_RADIUS,
                pos.getY() + 0.12,
                centerZ + RENDER_RADIUS);
    }

    public static final class RenderState extends BlockEntityRenderState {
        private float animationTime;
    }

    private static final class QuadGeometry implements SubmitNodeCollector.CustomGeometryRenderer {

        private final float halfSize;

        private QuadGeometry(float size) {
            this.halfSize = size * 0.5F;
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            addVertex(pose, buffer, -halfSize, -halfSize, 0.0F, 1.0F);
            addVertex(pose, buffer, -halfSize, halfSize, 0.0F, 0.0F);
            addVertex(pose, buffer, halfSize, halfSize, 1.0F, 0.0F);
            addVertex(pose, buffer, halfSize, -halfSize, 1.0F, 1.0F);
        }

        private static void addVertex(
                PoseStack.Pose pose,
                VertexConsumer buffer,
                float x,
                float z,
                float u,
                float v) {
            buffer.addVertex(pose, x, 0.0F, z)
                    .setColor(-1)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightCoordsUtil.FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        }
    }
}
