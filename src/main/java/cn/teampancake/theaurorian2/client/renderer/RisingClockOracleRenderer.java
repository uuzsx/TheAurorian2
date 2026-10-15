package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class RisingClockOracleRenderer extends SimpleGeoMobRenderer<RisingClockOracleEntity> {
    private static final RenderType OUTLINE = RenderTypes.outline(TheAurorian2.id("textures/entity/rising_clock_oracle.png"));
    // The established solid-emissive pipeline also gives these extruded shields
    // proper depth, so entities drawn later cannot show through their opaque faces.
    private static final RenderType WARD = RenderType.create("oracle_ward",
            RenderSetup.builder(OracleArcaneRenderer.BEAM_PIPELINE)
                    .withTexture("Sampler0",OracleWardGeometry.TEXTURE).createRenderSetup());
    private final OracleWardGeometry wardGeometry;
    @SuppressWarnings({"rawtypes","unchecked"})
    public RisingClockOracleRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("rising_clock_oracle"), .8F);
        wardGeometry = OracleWardGeometry.load(context.getResourceManager());
        scaleWidth = .8F; scaleHeight = .8F;
        withRenderLayer((GeoRenderLayer) new OracleRageWingLayer((GeoRenderer) this));
        withRenderLayer((GeoRenderLayer) new OracleWeaponLayer((GeoRenderer) this));
        withRenderLayer((GeoRenderLayer) new OracleBodyGlowLayer((GeoRenderer) this));
    }
    @Override public LivingEntityRenderState createRenderState(RisingClockOracleEntity entity, Void related) { return new WardState(); }
    @Override public void extractRenderState(RisingClockOracleEntity entity, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(entity,state,partialTick);
        var ward = (WardState)state;
        ward.mask = entity.isAlive() && !entity.isFinalCasting() ? entity.wardMask() : 0;
        ward.angle = (entity.tickCount + partialTick) * .0225F;
    }
    @Override public void submit(LivingEntityRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state,stack,collector,camera);
        var ward = (WardState)state;
        if (ward.mask == 0 || state.isInvisible || OracleGlowRender.shadowPass()) return;
        // Submit at the entity origin, outside the animated/scaled body transform.
        for (int slot = 0; slot < 4; slot++) if ((ward.mask & 1 << slot) != 0) {
            float angle = ward.angle + slot * Mth.HALF_PI;
            stack.pushPose();
            stack.translate(Mth.cos(angle)*2.05F,.85F,Mth.sin(angle)*2.05F);
            stack.mulPose(Axis.YP.rotation(-angle + Mth.HALF_PI));
            collector.submitCustomGeometry(stack,WARD,wardGeometry);
            stack.popPose();
        }
    }
    @Override protected AABB getBoundingBoxForCulling(RisingClockOracleEntity entity) {
        AABB bounds = super.getBoundingBoxForCulling(entity);
        return entity.wardCount() > 0 ? bounds.inflate(2.6,0,2.6) : bounds;
    }
    private static final class WardState extends LivingEntityRenderState {
        int mask;
        float angle;
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    @Override public void submitRenderTasks(RenderPassInfo pass, OrderedSubmitNodeCollector collector, RenderType type) {
        super.submitRenderTasks(pass,collector,type);
        // GeckoLib submits custom geometry, which does not get vanilla ModelFeatureRenderer's
        // second glowing pass. Submit the same posed bones to the vanilla outline target.
        var state = (LivingEntityRenderState)(Object)pass.renderState();
        if (state.appearsGlowing() && type != OUTLINE) super.submitRenderTasks(pass,collector,OUTLINE);
    }
    @Override public boolean shouldRender(RisingClockOracleEntity entity, Frustum frustum, double x, double y, double z) {
        // Suppress the whole model, shadow and even an existing glowing outline during the blink.
        return !entity.isVanishing() && super.shouldRender(entity,frustum,x,y,z);
    }
    @Override protected int getBlockLightLevel(RisingClockOracleEntity entity, BlockPos pos) {
        // Keep the original material and directional shading, including its painted details.
        return Math.max(12, super.getBlockLightLevel(entity, pos));
    }
    @Override protected float getDeathMaxRotation(GeoRenderState state) { return 0; }
}
