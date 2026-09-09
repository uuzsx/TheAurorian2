package cn.teampancake.theaurorian2.client.renderer.mirror;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded section snapshots, one worker job at a time; GPU work stays on the render thread. */
final class MirrorSceneCache implements AutoCloseable {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        var thread = new Thread(r, "Aurorian mirror mesher"); thread.setDaemon(true); return thread;
    });
    private static final int MAX_SECTIONS = 256;
    private final ClientLevel level;
    private final ConcurrentHashMap<Long, Section> sections = new ConcurrentHashMap<>();
    private Future<Compiled> pending;
    private Section pendingSection;
    private long pendingVersion;
    private long lastSelection;
    private final ByteBufferBuilder sortingBytes = new ByteBufferBuilder(65536);

    MirrorSceneCache(ClientLevel level) { this.level = level; }

    void invalidate(int x, int y, int z) {
        var section = sections.get(SectionPos.asLong(x, y, z));
        if (section != null) section.version.incrementAndGet();
    }

    void update(List<MirrorPlane> mirrors, int radius, long now) {
        receive();
        if (now - lastSelection > 200_000_000L) {
            lastSelection = now;
            var selected = new HashSet<Long>();
            for (var mirror : mirrors) {
                var c = mirror.center();
                for (int x = SectionPos.blockToSectionCoord(c.x - radius); x <= SectionPos.blockToSectionCoord(c.x + radius); x++)
                    for (int z = SectionPos.blockToSectionCoord(c.z - radius); z <= SectionPos.blockToSectionCoord(c.z + radius); z++)
                        for (int y = Math.max(level.getMinSectionY(), SectionPos.blockToSectionCoord(c.y - radius));
                                y < Math.min(level.getMaxSectionY(), SectionPos.blockToSectionCoord(c.y + radius) + 1); y++) {
                            if (selected.size() < MAX_SECTIONS && level.hasChunk(x, z)) selected.add(SectionPos.asLong(x, y, z));
                        }
            }
            sections.entrySet().removeIf(entry -> {
                if (selected.contains(entry.getKey())) return false;
                entry.getValue().close(); return true;
            });
            for (long key : selected) sections.computeIfAbsent(key, Section::new);
        }
        if (pending != null || mirrors.isEmpty()) return;
        Section next = null;
        double score = Double.MAX_VALUE;
        for (var section : sections.values()) {
            if (section.uploadedVersion == section.version.get()) continue;
            double distance = mirrors.stream().mapToDouble(m -> section.bounds.getCenter().distanceToSqr(m.center())).min().orElse(0);
            // Changed geometry takes precedence over unseen outer sections.
            if (section.uploadedVersion >= 0) distance -= 10000;
            if (distance < score) { next = section; score = distance; }
        }
        if (next == null) return;
        var pos = next.pos;
        if (!level.hasChunk(pos.x(), pos.z())) return;
        pendingSection = next;
        pendingVersion = next.version.get();
        // Snapshot creation is bounded to one 3x3x3 section neighborhood per frame.
        var region = new RenderRegionCache().createRegion(level, pos.asLong());
        var mc = Minecraft.getInstance();
        var models = mc.getModelManager();
        var compiler = new SectionCompiler(mc.options.ambientOcclusion().get(), mc.options.cutoutLeaves().get(),
                models.getBlockStateModelSet(), models.getFluidStateModelSet(),
                mc.getBlockColors(), mc.getBlockEntityRenderDispatcher());
        var additional = net.neoforged.neoforge.client.ClientHooks.gatherAdditionalRenderers(pos.origin(), level);
        pending = WORKER.submit(() -> compile(compiler, pos, region, additional));
    }

    private static Compiled compile(SectionCompiler compiler, SectionPos pos, RenderSectionRegion region,
            List<net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer> additional) {
        boolean extension = MirrorShaderCompat.beginMesh();
        try (var builders = new SectionBufferBuilderPack()) {
            var result = compiler.compile(pos, region, VertexSorting.DISTANCE_TO_ORIGIN, builders, additional);
            try {
                var layers = new ArrayList<LayerData>();
                for (var entry : result.renderedLayers.entrySet()) {
                    var mesh = entry.getValue();
                    layers.add(new LayerData(entry.getKey(), copy(mesh.vertexBuffer()),
                            mesh.indexBuffer() == null ? null : copy(mesh.indexBuffer()), mesh.drawState().indexCount(),
                            mesh.drawState().indexType(), entry.getKey().translucent() ? result.transparencyState : null));
                }
                return new Compiled(layers, List.copyOf(result.blockEntities));
            } finally { result.release(); }
        } finally { MirrorShaderCompat.endMesh(extension); }
    }

    private static byte[] copy(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()]; buffer.duplicate().get(bytes); return bytes;
    }

    private void receive() {
        if (pending == null || !pending.isDone()) return;
        try {
            var result = pending.get();
            var section = pendingSection;
            if (sections.get(section.pos.asLong()) == section && section.version.get() == pendingVersion) {
                section.close();
                for (var data : result.layers) section.meshes.add(new Mesh(data));
                section.blockEntities = result.blockEntities;
                section.uploadedVersion = pendingVersion;
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (ExecutionException e) { throw new IllegalStateException("Compiling mirror scene", e.getCause()); }
        finally { pending = null; pendingSection = null; }
    }

    List<BlockEntity> blockEntities(Frustum frustum) {
        var result = new ArrayList<BlockEntity>();
        for (var section : sections.values()) if (frustum.isVisible(section.bounds)) {
            for (var be : section.blockEntities) if (!be.isRemoved()) result.add(be);
        }
        return result;
    }

    boolean ready(Frustum frustum) {
        if (sections.isEmpty()) return false;
        for (var section : sections.values()) {
            if (section.uploadedVersion < 0 && frustum.isVisible(section.bounds)) return false;
        }
        return true;
    }

    void draw(MirrorSurface target, Vec3 reflectedEye, Matrix4f rotation, Frustum frustum, boolean translucent) {
        var mc = Minecraft.getInstance();
        var atlas = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        var selected = new ArrayList<Section>();
        for (var section : sections.values()) if (!section.meshes.isEmpty() && frustum.isVisible(section.bounds)) selected.add(section);
        if (translucent) selected.sort(Comparator.comparingDouble((Section s) -> -s.bounds.getCenter().distanceToSqr(reflectedEye)));
        var modelView = rotation;
        var infos = new DynamicUniforms.ChunkSectionInfo[selected.size()];
        int maxIndices = 6;
        for (int i = 0; i < selected.size(); i++) {
            var section = selected.get(i); var origin = section.pos.origin();
            infos[i] = new DynamicUniforms.ChunkSectionInfo(modelView, origin.getX(), origin.getY(), origin.getZ(),
                    1, atlas.getWidth(0), atlas.getHeight(0));
            for (var mesh : section.meshes) if (mesh.layer.translucent() == translucent) {
                if (translucent) mesh.sort(reflectedEye.subtract(Vec3.atLowerCornerOf(origin)), sortingBytes);
                maxIndices = Math.max(maxIndices, mesh.count);
            }
        }
        if (selected.isEmpty()) return;
        var uniforms = RenderSystem.getDynamicUniforms().writeChunkSections(infos);
        var sequential = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        var defaultIndices = sequential.getBuffer(maxIndices);
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Aurorian mirror terrain",
                target.getTextureView(), OptionalInt.empty(), target.target.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("Sampler0", atlas, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.bindTexture("Sampler2", mc.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            for (int i = 0; i < selected.size(); i++) for (var mesh : selected.get(i).meshes) {
                if (mesh.layer.translucent() != translucent) continue;
                pass.setPipeline(mesh.layer.pipeline()); pass.setUniform("ChunkSection", uniforms[i]);
                pass.setVertexBuffer(0, mesh.vertices);
                pass.setIndexBuffer(mesh.indices == null ? defaultIndices : mesh.indices, mesh.indices == null ? sequential.type() : mesh.indexType);
                pass.drawIndexed(0, 0, mesh.count, 1);
            }
        }
    }

    @Override public void close() {
        if (pending != null) pending.cancel(true);
        sections.values().forEach(Section::close); sections.clear(); sortingBytes.close();
    }

    private record LayerData(ChunkSectionLayer layer, byte[] vertices, byte[] indices, int count,
            VertexFormat.IndexType indexType, MeshData.SortState sorting) {}
    private record Compiled(List<LayerData> layers, List<BlockEntity> blockEntities) {}
    private static final class Section implements AutoCloseable {
        final SectionPos pos;
        final AABB bounds;
        final AtomicLong version = new AtomicLong();
        long uploadedVersion = -1;
        final List<Mesh> meshes = new ArrayList<>();
        List<BlockEntity> blockEntities = List.of();
        Section(long key) { pos = SectionPos.of(key); bounds = new AABB(pos.origin()).expandTowards(15, 15, 15); }
        @Override public void close() { meshes.forEach(Mesh::close); meshes.clear(); blockEntities = List.of(); }
    }
    private static final class Mesh implements AutoCloseable {
        final ChunkSectionLayer layer;
        final GpuBuffer vertices;
        final GpuBuffer indices;
        final int count;
        final VertexFormat.IndexType indexType;
        final MeshData.SortState sorting;
        Vec3 sortedFrom;
        Mesh(LayerData data) {
            layer = data.layer; count = data.count; indexType = data.indexType; sorting = data.sorting;
            vertices = upload(data.vertices, GpuBuffer.USAGE_VERTEX);
            indices = data.indices == null ? null : upload(data.indices, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST);
        }
        void sort(Vec3 eye, ByteBufferBuilder bytes) {
            if (sorting == null || indices == null || sortedFrom != null && sortedFrom.distanceToSqr(eye) < .0625) return;
            try (var sorted = sorting.buildSortedIndexBuffer(bytes, VertexSorting.byDistance((float) eye.x, (float) eye.y, (float) eye.z))) {
                if (sorted != null) RenderSystem.getDevice().createCommandEncoder().writeToBuffer(indices.slice(), sorted.byteBuffer());
            }
            bytes.clear(); sortedFrom = eye;
        }
        static GpuBuffer upload(byte[] bytes, int usage) {
            var buffer = MemoryUtil.memAlloc(bytes.length);
            try { buffer.put(bytes).flip(); return RenderSystem.getDevice().createBuffer(() -> "Aurorian mirror mesh", usage, buffer); }
            finally { MemoryUtil.memFree(buffer); }
        }
        @Override public void close() { vertices.close(); if (indices != null) indices.close(); }
    }
}
