package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.AurorianStorageCrateBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Model edges replace the voxel staircase visually; targeting and collision remain unchanged. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class StorageCrateOutline {
    private static volatile Map<BlockState, Outline> outlines = Map.of();

    private StorageCrateOutline() {}

    @SubscribeEvent
    public static void modelsBaked(ModelEvent.BakingCompleted event) {
        Map<BlockState, Outline> next = new IdentityHashMap<>();
        Map<BlockStateModel, Outline> models = new IdentityHashMap<>();
        event.getBakingResult().blockStateModels().forEach((state, model) -> {
            if (state.getBlock() instanceof AurorianStorageCrateBlock) {
                next.put(state, models.computeIfAbsent(model, StorageCrateOutline::bake));
            }
        });
        outlines = Map.copyOf(next);
    }

    @SubscribeEvent
    public static void extract(ExtractBlockOutlineRenderStateEvent event) {
        if (SharedConstants.DEBUG_SHAPES) return;
        Outline outline = outlines.get(event.getBlockState());
        if (outline != null && outline.lines.length != 0) event.addCustomRenderer(outline);
    }

    // These imported models are static. Resolve their already-rotated geometry once per reload,
    // without keeping a world reference or parsing resources during extraction/rendering.
    @SuppressWarnings("deprecation")
    private static Outline bake(BlockStateModel model) {
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(RandomSource.create(0), parts);
        Map<Edge, EdgeFaces> edges = new HashMap<>();
        for (var part : parts) {
            for (var quad : part.getQuads(null)) addQuad(edges, quad);
            for (var direction : Direction.values()) {
                for (var quad : part.getQuads(direction)) addQuad(edges, quad);
            }
        }
        return new Outline(edges.values().stream()
                .filter(edge -> edge.faces == 1 || edge.corner)
                .map(edge -> edge.line).toArray(Line[]::new));
    }

    private static void addQuad(Map<Edge, EdgeFaces> edges, BakedQuad quad) {
        Vector3f normal = new Vector3f(quad.position(1)).sub(quad.position(0))
                .cross(new Vector3f(quad.position(2)).sub(quad.position(0)));
        if (normal.lengthSquared() < 1.0e-12F) return;
        normal.normalize();
        for (int i = 0; i < 4; i++) {
            Vector3fc a = quad.position(i), b = quad.position((i + 1) % 4);
            if (a.distanceSquared(b) < 1.0e-12F) continue;
            Edge key = Edge.of(a, b);
            EdgeFaces found = edges.get(key);
            if (found == null) edges.put(key, new EdgeFaces(a, b, normal));
            else {
                found.faces++;
                // Suppress coplanar seams and the two-sided paper labels, not solid corners.
                found.corner |= Math.abs(found.normal.dot(normal)) < 0.9999F;
            }
        }
    }

    private record Point(int x, int y, int z) implements Comparable<Point> {
        static Point of(Vector3fc point) {
            return new Point(Math.round(point.x() * 100000), Math.round(point.y() * 100000),
                    Math.round(point.z() * 100000));
        }
        public int compareTo(Point other) {
            int order = Integer.compare(x, other.x);
            if (order == 0) order = Integer.compare(y, other.y);
            return order == 0 ? Integer.compare(z, other.z) : order;
        }
    }

    private record Edge(Point a, Point b) {
        static Edge of(Vector3fc a, Vector3fc b) {
            Point start = Point.of(a), end = Point.of(b);
            return start.compareTo(end) <= 0 ? new Edge(start, end) : new Edge(end, start);
        }
    }

    private static final class EdgeFaces {
        final Line line;
        final Vector3f normal;
        int faces = 1;
        boolean corner;

        EdgeFaces(Vector3fc a, Vector3fc b, Vector3fc normal) {
            this.line = new Line(new Vector3f(a), new Vector3f(b), new Vector3f(b).sub(a).normalize());
            this.normal = new Vector3f(normal);
        }
    }

    private record Line(Vector3f a, Vector3f b, Vector3f direction) {}

    private record Outline(Line[] lines) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState state, MultiBufferSource.BufferSource buffer,
                              PoseStack poses, boolean translucentPass, LevelRenderState levelState) {
            if (state.isTranslucent() != translucentPass) return true;
            var camera = levelState.cameraRenderState.pos;
            poses.pushPose();
            poses.translate(state.pos().getX() - camera.x, state.pos().getY() - camera.y,
                    state.pos().getZ() - camera.z);
            if (state.highContrast()) {
                draw(poses.last(), buffer.getBuffer(RenderTypes.secondaryBlockOutline()), 0xFF000000, 7);
            }
            float width = Minecraft.getInstance().gameRenderer.getGameRenderState()
                    .windowRenderState.appropriateLineWidth;
            draw(poses.last(), buffer.getBuffer(RenderTypes.lines()),
                    state.highContrast() ? 0xFF57FFE1 : 0x66000000, width);
            poses.popPose();
            buffer.endLastBatch();
            return true;
        }

        private void draw(PoseStack.Pose pose, VertexConsumer buffer, int color, float width) {
            for (Line line : lines) {
                buffer.addVertex(pose, line.a).setColor(color).setNormal(pose, line.direction).setLineWidth(width);
                buffer.addVertex(pose, line.b).setColor(color).setNormal(pose, line.direction).setLineWidth(width);
            }
        }
    }
}
