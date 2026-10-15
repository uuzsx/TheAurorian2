package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleSpellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;

/** Ground warnings use the user's pixel art; impacts retain vanilla lightning and physical terrain. */
public final class OracleSpellRenderer extends EntityRenderer<OracleSpellEntity, OracleSpellRenderer.State> {
    private static final RenderType WARNING_TYPE = RenderType.create("oracle_lightning_warning",
            RenderSetup.builder(OracleArcaneRenderer.PIPELINE)
                    .withTexture("Sampler0", TheAurorian2.id("textures/effect/oracle_lightning_warning.png"))
                    .sortOnUpload().createRenderSetup());
    // Circumscribe the strike's 2.5-block square with the painted outer ring.
    private static final float WARNING_RADIUS = 1.9F;
    private static final SubmitNodeCollector.CustomGeometryRenderer WARNING_QUAD = (pose, buffer) -> {
        warningVertex(pose, buffer, -WARNING_RADIUS, -WARNING_RADIUS, 0, 1);
        warningVertex(pose, buffer, -WARNING_RADIUS, WARNING_RADIUS, 0, 0);
        warningVertex(pose, buffer, WARNING_RADIUS, WARNING_RADIUS, 1, 0);
        warningVertex(pose, buffer, WARNING_RADIUS, -WARNING_RADIUS, 1, 1);
    };
    private final LightningBoltRenderer lightningRenderer;

    public OracleSpellRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
        lightningRenderer = new LightningBoltRenderer(context);
    }

    @Override public State createRenderState() { return new State(); }

    @Override protected AABB getBoundingBoxForCulling(OracleSpellEntity entity) {
        if (entity.kind() == OracleSpellEntity.LIGHTNING) {
            return new AABB(entity.getX() - 40, entity.getY() - .5, entity.getZ() - 40,
                    entity.getX() + 40, entity.getY() + 129, entity.getZ() + 40);
        }
        // Include the shaking block and the complete lift while its anchor stays on the ground.
        return new AABB(entity.getX() - .75, entity.getY()-.1, entity.getZ() - .75,
                entity.getX() + .75, entity.getY() + entity.liftHeight()+1, entity.getZ() + .75);
    }

    @Override public void extractRenderState(OracleSpellEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.kind = entity.kind();
        state.empowered = entity.empowered();
        state.age = entity.age(partialTick);
        state.seed = entity.seed();
        state.delay = entity.delay();
        state.warningYOffset = 0;
        if (state.kind == OracleSpellEntity.LIGHTNING && state.age < OracleSpellEntity.LIGHTNING_IMPACT + state.delay) {
            BlockPos floor = BlockPos.containing(entity.getX(), entity.getY() - .03, entity.getZ());
            var shape = entity.level().getBlockState(floor).getCollisionShape(entity.level(), floor);
            // The spell anchor is above a whole block. Keep the warning on slabs and other low surfaces too.
            if (!shape.isEmpty()) state.warningYOffset = (float) (shape.max(Direction.Axis.Y) - 1);
        }
        if (state.kind == OracleSpellEntity.LIFTED_BLOCK && entity.level() instanceof ClientLevel level) {
            var pos = entity.blockPosition();
            state.block.randomSeedPos = pos;
            // The entity's anchor stays on the ground; sample light where its block is actually lifted.
            state.block.blockPos = BlockPos.containing(entity.getX(),
                    entity.getY()+OracleSpellEntity.liftOffset(state.age,state.empowered), entity.getZ());
            state.block.blockState = entity.blockState();
            state.block.biome = level.getBiome(pos);
            state.block.cardinalLighting = level.cardinalLighting();
            state.block.lightEngine = level.getLightEngine();
        }
    }

    @Override public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.kind == OracleSpellEntity.LIFTED_BLOCK) {
            stack.pushPose();
            stack.translate(0, OracleSpellEntity.liftOffset(state.age,state.empowered)+.5, 0);
            if (state.age < OracleSpellEntity.LIFT_START) {
                float phase = state.age*2.2F+(state.seed&255);
                stack.translate(Math.sin(phase)*.055,Math.abs(Math.sin(phase*1.3))*.04,Math.cos(phase)*.055);
                stack.mulPose(Axis.XP.rotationDegrees((float)Math.sin(phase)*2.5F));
                stack.mulPose(Axis.ZP.rotationDegrees((float)Math.cos(phase)*2.5F));
            }
            stack.translate(-.5,-.5,-.5);
            collector.submitMovingBlock(stack, state.block);
            stack.popPose();
            return;
        }
        float impactAge = state.age - OracleSpellEntity.LIGHTNING_IMPACT - state.delay;
        if (state.kind == OracleSpellEntity.LIGHTNING && impactAge < 0) {
            stack.pushPose();
            // The anchor already sits 0.025 blocks above the surface, avoiding z-fighting.
            stack.translate(0, state.warningYOffset, 0);
            stack.mulPose(Axis.YP.rotationDegrees(state.age * 4.5F + (state.seed & 255) * (360F / 256F)));
            collector.submitCustomGeometry(stack, WARNING_TYPE, WARNING_QUAD);
            stack.popPose();
        }
        if (state.kind == OracleSpellEntity.LIGHTNING
                && (impactAge >= 0 && impactAge < 3 || impactAge >= 5 && impactAge < 7 || impactAge >= 9 && impactAge < 11)) {
            // Render vanilla lightning without spawning another damage source or causing fires.
            var bolt = new LightningBoltRenderState();
            bolt.seed = state.seed;
            lightningRenderer.submit(bolt, stack, collector, camera);
        }
        // The gravity field is an invisible server-authoritative controller, not a visual effect.
    }

    private static void warningVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float z, float u, float v) {
        buffer.addVertex(pose, x, 0, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0, 1, 0);
    }

    public static final class State extends EntityRenderState {
        private int kind, seed, delay;
        private boolean empowered;
        private float age, warningYOffset;
        private final MovingBlockRenderState block = new MovingBlockRenderState();
    }
}
