package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.LightCoordsUtil;

/** A posed, emissive surface pass on charged feathers only. No extra entities or particles. */
public final class OracleRageWingLayer extends GeoRenderLayer<RisingClockOracleEntity, Void, GeoRenderState> {
    private static final DataTicket<Integer> RAGE = DataTickets.create("theaurorian2:oracle_wing_rage", Integer.class);
    // Facing the priest: bottom left, bottom right, then the next row upwards.
    // The original asset intentionally spells these bone names "righr".
    private static final String[] ORDER = {
            "feather_righr_4", "feather_righr_8", "feather_righr_3", "feather_righr_7",
            "feather_righr_2", "feather_righr_6", "feather_righr_1", "feather_righr_5"
    };
    private static final int FRAMES = 32, FRAME_HEIGHT = 34;
    private static final RenderType TYPE = RenderType.create("oracle_rage_wings",
            RenderSetup.builder(OracleArcaneRenderer.PIPELINE)
                    .withTexture("Sampler0", TheAurorian2.id("textures/entity/rising_clock_oracle_wing_flow.png"))
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload().createRenderSetup());
    private final PerBoneRender<GeoRenderState> wingRenderer = this::submitWing;

    public OracleRageWingLayer(GeoRenderer<RisingClockOracleEntity, Void, GeoRenderState> renderer) { super(renderer); }

    @Override public void addRenderData(RisingClockOracleEntity entity, Void related, GeoRenderState state, float partialTick) {
        state.addGeckolibData(RAGE, Mth.clamp(entity.rage(), 0, ORDER.length));
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> register) {
        if (((LivingEntityRenderState)(Object)pass.renderState()).isInvisible) return;
        int rage = pass.getGeckolibData(RAGE);
        if (rage == 0) return;
        var bones = pass.model().boneLookup().get();
        for (int i = 0; i < rage; i++) {
            GeoBone bone = bones.get(ORDER[i]);
            if (bone instanceof CuboidGeoBone) register.accept(bone, wingRenderer);
        }
    }

    private void submitWing(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector collector) {
        var cubes = ((CuboidGeoBone)bone).cubes;
        // Per-bone hooks arrive at the posed pivot. Cubes still use the original
        // model coordinates; remove only that final pivot translation.
        bone.translateAwayFromPivotPoint(pass.poseStack());
        int frame = Math.floorMod((int)Math.floor(pass.renderState().getAnimatableAge() * .5), FRAMES);
        collector.order(1).submitCustomGeometry(pass.poseStack(), TYPE, (pose, buffer) -> {
            var stack = pass.poseStack();
            stack.pushPose();
            stack.last().set(pose);
            VertexConsumer flowing = new FlowUv(buffer, frame);
            // The callback's matrix already captures this entity's complete animation.
            // Render immutable cubes directly, never mutate shared model bone visibility.
            for (var cube : cubes) {
                stack.pushPose();
                cube.render(stack, flowing, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                stack.popPose();
            }
            stack.popPose();
        });
    }

    /** Reuse the exact original face UVs, including the left feathers' mirrored U. */
    private record FlowUv(VertexConsumer delegate, int frame) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x,y,z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r,g,b,a); return this; }
        @Override public VertexConsumer setColor(int argb) { delegate.setColor(argb); return this; }
        @Override public VertexConsumer setUv(float u, float v) {
            delegate.setUv((u * 128 - 97) / 16, (v * 128 + 1 + frame * FRAME_HEIGHT) / (FRAMES * FRAME_HEIGHT));
            return this;
        }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u,v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u,v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x,y,z); return this; }
        @Override public VertexConsumer setLineWidth(float width) { delegate.setLineWidth(width); return this; }
    }
}
