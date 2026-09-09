package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.LongMirrorBlockEntity;
import cn.teampancake.theaurorian2.common.config.MirrorConfig;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.joml.Matrix4f;
import java.util.*;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class MirrorReflections {
    private static final ContextKey<List<View>> VIEWS = new ContextKey<>(TheAurorian2.id("mirror_views"));
    private static MirrorSceneRenderer renderer;
    private static volatile MirrorSceneCache cache;
    private static ClientLevel level;
    private static volatile boolean reset;
    private static boolean failed;
    private static long lastVisible;
    private static int settings;
    // Used only during a scoped secondary render, never during world extraction or ticking.
    private static RenderTarget activeTarget;

    private MirrorReflections() {}
    public static RenderTarget activeTarget() { return activeTarget; }
    static void activeTarget(RenderTarget target) { activeTarget = target; }
    public static void invalidateSection(int x, int y, int z) {
        var current = cache;
        if (current != null) current.invalidate(x, y, z);
    }
    public static void requestReset() { reset = true; }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (level != mc.level || reset) { close(); level = mc.level; failed = false; reset = false; }
        if (renderer != null && (failed || !MirrorConfig.ENABLED.get() || System.nanoTime() - lastVisible > 5_000_000_000L)) close();
    }

    @SubscribeEvent public static void extract(ExtractLevelRenderStateEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || !MirrorConfig.ENABLED.get() || failed || reset
                || !MirrorShaderCompat.supported() || MirrorShaderCompat.shadowPass()) return;
        var worldState = event.getRenderState();
        var eye = event.getCamera().position();
        var planes = new ArrayList<MirrorPlane>();
        var seen = new HashSet<net.minecraft.core.BlockPos>();
        for (var state : worldState.blockEntityRenderStates) if (state instanceof LongMirrorRenderer.State mirror) {
            var plane = mirror.plane;
            if (plane == null || !seen.add(plane.base()) || plane.center().distanceToSqr(eye) > 64
                    || plane.distance(eye) < .04 || !event.getFrustum().isVisible(plane.bounds())) continue;
            planes.add(plane);
        }
        planes.sort(Comparator.comparingDouble(p -> p.center().distanceToSqr(eye)));
        // Five rays are bounded per candidate, and an opaque wall prevents the mirror pass entirely.
        planes.removeIf(p -> !visible(mc, p, eye));
        if (planes.size() > MirrorConfig.MAX_MIRRORS.get()) planes.subList(MirrorConfig.MAX_MIRRORS.get(), planes.size()).clear();
        if (planes.isEmpty()) return;
        long now = System.nanoTime(); lastVisible = now;
        int currentSettings = (MirrorConfig.MAX_MIRRORS.get() * 1024 + MirrorConfig.RESOLUTION.get()) * 32 + MirrorConfig.DISTANCE.get();
        if (renderer != null && settings != currentSettings) close();
        try {
            if (renderer == null) {
                level = mc.level; settings = currentSettings;
                cache = new MirrorSceneCache(level);
                renderer = new MirrorSceneRenderer(MirrorConfig.MAX_MIRRORS.get(), MirrorConfig.RESOLUTION.get());
            }
            cache.update(planes, MirrorConfig.DISTANCE.get(), now);
            var views = new ArrayList<View>();
            var used = new HashSet<MirrorSurface>();
            // Keep slots stable while mirror distance ordering changes.
            for (var plane : planes) {
                MirrorSurface surface = renderer.surface(plane, planes, used);
                used.add(surface);
                boolean changed = !plane.equals(surface.plane);
                if (changed) { surface.plane = plane; surface.ready = false; surface.updated = 0; }
                boolean due = now - surface.updated >= 1_000_000_000L / MirrorConfig.FPS.get();
                View view = new View(plane, surface, eye, due);
                if (due) extractScene(event, view);
                views.add(view);
            }
            worldState.setRenderData(VIEWS, views);
        } catch (Exception e) { fail(e); }
    }

    private static boolean visible(Minecraft mc, MirrorPlane plane, Vec3 eye) {
        // Transparent blocks must not disable a visible mirror. Partial occluders are left to the depth buffer.
        for (int i = 0; i < 5; i++) {
            float x = i == 0 ? 0 : ((i & 1) == 0 ? -.35F : .35F);
            float y = i == 0 ? 0 : (i < 3 ? -.8F : .8F);
            var hit = mc.level.clip(new ClipContext(eye, plane.point(x, y).add(plane.normal().scale(.005)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player) {
                @Override public net.minecraft.world.phys.shapes.VoxelShape getBlockShape(
                        net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level,
                        net.minecraft.core.BlockPos pos) {
                    return state.isSolidRender() ? super.getBlockShape(state, level, pos)
                            : net.minecraft.world.phys.shapes.Shapes.empty();
                }
            });
            if (hit.getType() == HitResult.Type.MISS) return true;
        }
        return false;
    }

    private static void extractScene(ExtractLevelRenderStateEvent event, View view) {
        var mc = Minecraft.getInstance();
        float partial = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        // The local player must be extracted explicitly even in first person.
        addEntity(view, mc.player, partial);
        var area = view.plane.bounds().inflate(MirrorConfig.DISTANCE.get());
        var entities = mc.level.getEntities(mc.player, area, e -> !e.isRemoved());
        entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(view.reflectedEye)));
        for (var entity : entities) {
            if (view.entities.size() >= 48) break;
            if (view.frustum.isVisible(entity.getBoundingBox().inflate(.5))) addEntity(view, entity, partial);
        }
        var dispatcher = mc.getBlockEntityRenderDispatcher();
        for (var be : cache.blockEntities(view.frustum)) {
            if (view.blocks.size() >= 64) break;
            if (be instanceof LongMirrorBlockEntity) continue; // No recursive mirrors.
            var blockRenderer = dispatcher.getRenderer(be);
            if (blockRenderer != null && view.frustum.isVisible(blockRenderer.getRenderBoundingBox(be))) {
                var state = extractBlock(be, partial, view.reflectedEye);
                if (state != null) view.blocks.add(state);
            }
        }
        renderer.weather.extractRenderState(mc.level, (int) mc.level.getGameTime(), partial, view.reflectedEye, view.weather);
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityRenderState extractBlock(T be, float partial, Vec3 eye) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var blockRenderer = dispatcher.getRenderer(be);
        return extractBlockWithRenderer(be, blockRenderer, partial, eye);
    }
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends BlockEntityRenderState> S extractBlockWithRenderer(
            T be, net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T, S> renderer, float partial, Vec3 eye) {
        if (renderer == null) return null;
        S state = renderer.createRenderState(); renderer.extractRenderState(be, state, partial, eye, null); return state;
    }
    private static void addEntity(View view, net.minecraft.world.entity.Entity entity, float partial) {
        var state = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partial);
        state.nameTag = null; state.outlineColor = 0;
        view.entities.add(state);
    }

    @SubscribeEvent public static void submit(SubmitCustomGeometryEvent event) {
        var views = event.getLevelRenderState().getRenderData(VIEWS);
        if (views == null || renderer == null || failed || MirrorShaderCompat.shadowPass()) return;
        try {
            for (var view : views) {
                // Keep static glass (or the previous complete frame) while new sections are being prepared.
                if (view.due && cache.ready(view.frustum)) renderer.render(view, cache, event.getLevelRenderState());
                if (view.surface.ready) renderer.submitSurface(view, event);
            }
        } catch (Exception e) { fail(e); }
    }

    @SubscribeEvent public static void shutdown(GameShuttingDownEvent event) { close(); }
    @SubscribeEvent public static void endFrame(RenderFrameEvent.Post event) {
        if (renderer != null) renderer.endFrame();
    }
    private static void fail(Exception e) {
        failed = true;
        TheAurorian2.LOGGER.error("Mirror reflection disabled for this session; retaining static mirror glass", e);
        // Cleanup on the next tick, after already-submitted geometry has finished using its textures.
    }
    private static void close() {
        if (cache != null) { cache.close(); cache = null; }
        if (renderer != null) { renderer.close(); renderer = null; }
    }

    static final class View {
        final MirrorPlane plane;
        final MirrorSurface surface;
        final Vec3 eye;
        final Vec3 reflectedEye;
        final Matrix4f rotation;
        final Matrix4f projection;
        final Frustum frustum;
        final boolean due;
        final List<EntityRenderState> entities = new ArrayList<>();
        final List<BlockEntityRenderState> blocks = new ArrayList<>();
        final WeatherRenderState weather = new WeatherRenderState();
        View(MirrorPlane plane, MirrorSurface surface, Vec3 eye, boolean due) {
            this.plane = plane; this.surface = surface; this.eye = eye; this.due = due;
            reflectedEye = plane.reflect(eye); rotation = plane.viewRotation();
            projection = plane.projection(eye, (float) plane.distance(eye) + MirrorConfig.DISTANCE.get() * 2 + 4,
                    com.mojang.blaze3d.systems.RenderSystem.getDevice().isZZeroToOne());
            frustum = new Frustum(rotation, projection); frustum.prepare(reflectedEye.x, reflectedEye.y, reflectedEye.z);
        }
    }
}
