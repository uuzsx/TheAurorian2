package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.common.config.MirrorConfig;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.util.List;
import java.util.Set;

final class MirrorSceneRenderer implements AutoCloseable {
    final WeatherEffectRenderer weather = new WeatherEffectRenderer();
    private final MirrorSurface[] surfaces;
    private final SkyRenderer sky;
    private final ProjectionMatrixBuffer projection = new ProjectionMatrixBuffer("Aurorian mirrors");
    private final ByteBufferBuilder bytes = new ByteBufferBuilder(786432);
    private final MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(bytes);
    private final SubmitNodeStorage storage = new SubmitNodeStorage();
    private final GlobalSettingsUniform globals = new GlobalSettingsUniform();
    private final FeatureRenderDispatcher features;
    private final GpuBuffer fogBuffer;

    MirrorSceneRenderer(int count, int width) {
        var mc = Minecraft.getInstance();
        surfaces = new MirrorSurface[count];
        for (int i = 0; i < count; i++) {
            surfaces[i] = new MirrorSurface(i, width);
            mc.getTextureManager().register(surfaces[i].id, surfaces[i]);
        }
        sky = new SkyRenderer(mc.getTextureManager(), mc.getAtlasManager());
        features = new FeatureRenderDispatcher(storage, mc.getModelManager(), buffers, mc.getAtlasManager(),
                mc.renderBuffers().outlineBufferSource(), buffers, mc.font, mc.gameRenderer.getGameRenderState());
        fogBuffer = RenderSystem.getDevice().createBuffer(() -> "Aurorian mirror fog",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, FogRenderer.FOG_UBO_SIZE);
    }

    MirrorSurface surface(MirrorPlane plane, List<MirrorPlane> visible, Set<MirrorSurface> used) {
        for (var surface : surfaces) if (plane.equals(surface.plane)) return surface;
        for (var surface : surfaces) if (!used.contains(surface) && !visible.contains(surface.plane)) return surface;
        throw new IllegalStateException("No free mirror render target");
    }

    void render(MirrorReflections.View view, MirrorSceneCache cache, LevelRenderState world) {
        var mc = Minecraft.getInstance(); var target = view.surface.target;
        var oldColor = RenderSystem.outputColorTextureOverride; var oldDepth = RenderSystem.outputDepthTextureOverride;
        var oldProjection = RenderSystem.getProjectionMatrixBuffer(); var oldType = RenderSystem.getProjectionType();
        var oldFog = RenderSystem.getShaderFog();
        var oldGlobals = RenderSystem.getGlobalSettingsUniform();
        var mv = RenderSystem.getModelViewStack(); mv.pushMatrix().set(view.rotation);
        MirrorReflections.activeTarget(target);
        try {
            RenderSystem.outputColorTextureOverride = target.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();
            globals.update(target.width, target.height, mc.gameRenderer.getGameRenderState().optionsRenderState.glintStrength,
                    world.gameTime, mc.getDeltaTracker(), 0, view.reflectedEye, false);
            var fog = world.cameraRenderState.fogData;
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.getColorTexture(),
                    ARGB.colorFromFloat(1, fog.color.x, fog.color.y, fog.color.z), target.getDepthTexture(), 1);
            // Sky needs a far plane independent of the bounded terrain cache.
            RenderSystem.setProjectionMatrix(projection.getBuffer(view.plane.projection(view.eye, 2048,
                    RenderSystem.getDevice().isZZeroToOne())), ProjectionType.PERSPECTIVE);
            renderSky(world);
            RenderSystem.setProjectionMatrix(projection.getBuffer(view.projection), ProjectionType.PERSPECTIVE);
            try (var stack = MemoryStack.stackPush()) {
                float end = MirrorConfig.DISTANCE.get() + (float) view.plane.distance(view.eye);
                var data = Std140Builder.onStack(stack, FogRenderer.FOG_UBO_SIZE).putVec4(fog.color)
                        .putFloat(fog.environmentalStart).putFloat(fog.environmentalEnd)
                        .putFloat(end * .7F).putFloat(end).putFloat(fog.skyEnd).putFloat(fog.cloudEnd).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(fogBuffer.slice(), data);
            }
            RenderSystem.setShaderFog(fogBuffer.slice());
            cache.draw(view.surface, view.reflectedEye, view.rotation, view.frustum, false);
            var camera = new CameraRenderState(); camera.pos = view.reflectedEye;
            camera.orientation = view.rotation.getNormalizedRotation(new org.joml.Quaternionf()).conjugate();
            camera.viewRotationMatrix.set(view.rotation); camera.projectionMatrix.set(view.projection);
            camera.initialized = true;
            var pose = new PoseStack();
            for (var entity : view.entities) mc.getEntityRenderDispatcher().submit(entity, camera,
                    entity.x - view.reflectedEye.x, entity.y - view.reflectedEye.y, entity.z - view.reflectedEye.z, pose, storage);
            for (var block : view.blocks) {
                pose.pushPose(); pose.translate(block.blockPos.getX() - view.reflectedEye.x,
                        block.blockPos.getY() - view.reflectedEye.y, block.blockPos.getZ() - view.reflectedEye.z);
                mc.getBlockEntityRenderDispatcher().submit(block, pose, storage, camera); pose.popPose();
            }
            features.renderAllFeatures(); buffers.endBatch();
            cache.draw(view.surface, view.reflectedEye, view.rotation, view.frustum, true);
            weather.render(view.reflectedEye, view.weather);
            var options = mc.gameRenderer.getGameRenderState().optionsRenderState;
            if (options.cloudStatus != net.minecraft.client.CloudStatus.OFF && ARGB.alpha(world.cloudColor) > 0) {
                RenderSystem.setProjectionMatrix(projection.getBuffer(view.plane.projection(view.eye, 2048,
                        RenderSystem.getDevice().isZZeroToOne())), ProjectionType.PERSPECTIVE);
                RenderSystem.setShaderFog(oldFog);
                view.surface.clouds.render(world.cloudColor, options.cloudStatus, world.cloudHeight,
                        options.cloudRange, view.reflectedEye, world.gameTime, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
            }
            view.surface.ready = true; view.surface.updated = System.nanoTime();
        } finally {
            features.clearSubmitNodes();
            MirrorReflections.activeTarget(null);
            RenderSystem.outputColorTextureOverride = oldColor; RenderSystem.outputDepthTextureOverride = oldDepth;
            RenderSystem.setProjectionMatrix(oldProjection, oldType); RenderSystem.setShaderFog(oldFog); mv.popMatrix();
            RenderSystem.setGlobalSettingsUniform(oldGlobals);
        }
    }

    private void renderSky(LevelRenderState world) {
        var state = world.skyRenderState;
        if (state.skybox == DimensionType.Skybox.END) {
            sky.renderEndSky();
            if (state.endFlashIntensity > 0) sky.renderEndFlash(new PoseStack(), state.endFlashIntensity, state.endFlashXAngle, state.endFlashYAngle);
        } else if (state.skybox != DimensionType.Skybox.NONE) {
            sky.renderSkyDisc(state.skyColor);
            var pose = new PoseStack();
            sky.renderSunriseAndSunset(pose, state.sunAngle, state.sunriseAndSunsetColor);
            sky.renderSunMoonAndStars(pose, state.sunAngle, state.moonAngle, state.starAngle,
                    state.moonPhase, state.rainBrightness, state.starBrightness);
            if (state.shouldRenderDarkDisc) sky.renderDarkDisc();
        }
    }

    void submitSurface(MirrorReflections.View view, SubmitCustomGeometryEvent event) {
        var pose = event.getPoseStack(); pose.pushPose();
        var origin = view.plane.center().add(view.plane.normal().scale(.001));
        pose.translate(origin.x - view.eye.x, origin.y - view.eye.y, origin.z - view.eye.z);
        event.getSubmitNodeCollector().submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(view.surface.id, false),
                (p, b) -> {
                    vertex(p, b, view.plane, -MirrorPlane.HALF_WIDTH, -MirrorPlane.HALF_HEIGHT, 0, 0);
                    vertex(p, b, view.plane, MirrorPlane.HALF_WIDTH, -MirrorPlane.HALF_HEIGHT, 1, 0);
                    vertex(p, b, view.plane, MirrorPlane.HALF_WIDTH, MirrorPlane.HALF_HEIGHT, 1, 1);
                    vertex(p, b, view.plane, -MirrorPlane.HALF_WIDTH, MirrorPlane.HALF_HEIGHT, 0, 1);
                });
        pose.popPose();
    }
    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, MirrorPlane plane, float x, float y, float u, float v) {
        buffer.addVertex(pose, (float) plane.right().x * x, y, (float) plane.right().z * x)
                .setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880)
                .setNormal(pose, (float) plane.normal().x, 0, (float) plane.normal().z);
    }
    void endFrame() { features.endFrame(); storage.endFrame(); for (var surface : surfaces) surface.clouds.endFrame(); }
    @Override public void close() {
        features.close(); sky.close(); projection.close(); globals.close(); fogBuffer.close(); bytes.close();
        for (var surface : surfaces) Minecraft.getInstance().getTextureManager().release(surface.id);
    }
}
