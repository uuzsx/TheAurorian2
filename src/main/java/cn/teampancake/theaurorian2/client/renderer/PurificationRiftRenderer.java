package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.PurificationRiftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;

/** Billboard renderer for a purification ritual rift. */
public final class PurificationRiftRenderer
        extends EntityRenderer<PurificationRiftEntity, PurificationRiftRenderer.RenderState> {

    private static final int FRAME_TICKS = 2;
    private static final int OPEN_FRAME_COUNT = 4;
    private static final int LOOP_FRAME_COUNT = 6;
    private static final float RIFT_SIZE = 4.0F;
    private static final Identifier OPEN_TEXTURE =
            TheAurorian2.id("textures/entity/purification_rift.png");
    private static final Identifier LOOP_TEXTURE =
            TheAurorian2.id("textures/entity/purification_rift_loop.png");
    private static final RenderType OPEN_RENDER_TYPE =
            RenderTypes.entityTranslucentEmissive(OPEN_TEXTURE);
    private static final RenderType LOOP_RENDER_TYPE =
            RenderTypes.entityTranslucentEmissive(LOOP_TEXTURE);
    private static final QuadGeometry[] OPEN_FRAMES = createFrames(OPEN_FRAME_COUNT);
    private static final QuadGeometry[] LOOP_FRAMES = createFrames(LOOP_FRAME_COUNT);

    public PurificationRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(PurificationRiftEntity entity, RenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.tickCount + partialTick;
        state.closing = entity.isClosing();
        state.closingTicks = entity.getClosingTicks(partialTick);
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        RenderType renderType;
        QuadGeometry frame;
        if (state.closing) {
            int closingFrame = Math.min(
                    OPEN_FRAME_COUNT - 1,
                    (int) (state.closingTicks / FRAME_TICKS));
            renderType = OPEN_RENDER_TYPE;
            frame = OPEN_FRAMES[OPEN_FRAME_COUNT - 1 - closingFrame];
        } else if (state.age < OPEN_FRAME_COUNT * FRAME_TICKS) {
            int openingFrame = Math.min(
                    OPEN_FRAME_COUNT - 1,
                    (int) (state.age / FRAME_TICKS));
            renderType = OPEN_RENDER_TYPE;
            frame = OPEN_FRAMES[openingFrame];
        } else {
            int loopFrame = (int) ((state.age - OPEN_FRAME_COUNT * FRAME_TICKS) / FRAME_TICKS)
                    % LOOP_FRAME_COUNT;
            renderType = LOOP_RENDER_TYPE;
            frame = LOOP_FRAMES[loopFrame];
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, -0.5F, 0.0F);
        poseStack.mulPose(camera.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, frame);
        poseStack.popPose();
    }

    @Override
    protected AABB getBoundingBoxForCulling(PurificationRiftEntity entity) {
        BlockPos pos = entity.blockPosition();
        return new AABB(pos).inflate(4.5D);
    }

    private static QuadGeometry[] createFrames(int frameCount) {
        QuadGeometry[] frames = new QuadGeometry[frameCount];
        for (int frame = 0; frame < frameCount; frame++) {
            frames[frame] = new QuadGeometry(
                    RIFT_SIZE,
                    RIFT_SIZE,
                    frame / (float) frameCount,
                    (frame + 1) / (float) frameCount);
        }
        return frames;
    }

    public static final class RenderState extends EntityRenderState {
        private float age;
        private float closingTicks;
        private boolean closing;
    }

    private static final class QuadGeometry implements SubmitNodeCollector.CustomGeometryRenderer {

        private final float halfWidth;
        private final float height;
        private final float minU;
        private final float maxU;

        private QuadGeometry(float width, float height, float minU, float maxU) {
            this.halfWidth = width * 0.5F;
            this.height = height;
            this.minU = minU;
            this.maxU = maxU;
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            addVertex(pose, buffer, -halfWidth, 0.0F, minU, 1.0F);
            addVertex(pose, buffer, -halfWidth, height, minU, 0.0F);
            addVertex(pose, buffer, halfWidth, height, maxU, 0.0F);
            addVertex(pose, buffer, halfWidth, 0.0F, maxU, 1.0F);
        }

        private static void addVertex(
                PoseStack.Pose pose,
                VertexConsumer buffer,
                float x,
                float y,
                float u,
                float v) {
            buffer.addVertex(pose, x, y, 0.0F)
                    .setColor(-1)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightCoordsUtil.FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
        }
    }
}
