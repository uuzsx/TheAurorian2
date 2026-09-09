package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.neoforged.fml.ModList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/** Optional Iris boundary. Its public API has no scoped offscreen vanilla-render operation. */
public final class MirrorShaderCompat {
    private static final Bridge IRIS = load();
    private static final ThreadLocal<Boolean> COMPILING_MESH = new ThreadLocal<>();
    private MirrorShaderCompat() {}

    static boolean supported() { return IRIS == null || IRIS.skipExtension != null; }
    static boolean shadowPass() { return IRIS != null && supported() && IRIS.shadowPass(); }

    // Save the exact flags, including a caller's existing override; never toggle the shader pack itself.
    static int beginRender() {
        if (IRIS == null) return -1;
        int saved = IRIS.skipExtension.get() ? 1 << IRIS.flags.length : 0;
        IRIS.skipExtension.set(true);
        for (int i = 0; i < IRIS.flags.length; i++) {
            if ((boolean) IRIS.flags[i].get()) saved |= 1 << i;
            IRIS.flags[i].set(i == 0);
        }
        return saved;
    }

    static void endRender(int saved) {
        if (saved < 0) return;
        for (int i = 0; i < IRIS.flags.length; i++) IRIS.flags[i].set((saved & 1 << i) != 0);
        IRIS.skipExtension.set((saved & 1 << IRIS.flags.length) != 0);
    }

    // Terrain compilation runs on our worker. Only touch Iris's thread-local flag there.
    static boolean beginMesh() {
        if (IRIS == null) return false;
        COMPILING_MESH.set(true);
        boolean old = IRIS.skipExtension.get(); IRIS.skipExtension.set(true); return old;
    }

    static void endMesh(boolean saved) {
        if (IRIS != null) { IRIS.skipExtension.set(saved); COMPILING_MESH.remove(); }
    }
    public static boolean compilingMesh() { return Boolean.TRUE.equals(COMPILING_MESH.get()); }

    @SuppressWarnings("unchecked")
    private static Bridge load() {
        if (!ModList.get().isLoaded("iris")) return null;
        try {
            var lookup = MethodHandles.publicLookup();
            var api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            var shadow = lookup.unreflect(api.getMethod("isRenderingShadowPass")).bindTo(instance);
            var state = Class.forName("net.irisshaders.iris.vertices.ImmediateState");
            String[] names = {"bypass", "safeToMultiply", "isRenderingLevel", "renderWithExtendedVertexFormat", "usingTessellation", "isRenderingBEs"};
            var flags = new VarHandle[names.length];
            for (int i = 0; i < names.length; i++) flags[i] = lookup.findStaticVarHandle(state, names[i], boolean.class);
            var skip = (ThreadLocal<Boolean>) state.getField("skipExtension").get(null);
            return new Bridge(shadow, flags, skip);
        } catch (ReflectiveOperationException | LinkageError e) {
            TheAurorian2.LOGGER.error("Cannot isolate mirror rendering from this Iris version; retaining static mirror glass", e);
            return new Bridge(null, new VarHandle[0], null);
        }
    }

    private record Bridge(MethodHandle shadow, VarHandle[] flags, ThreadLocal<Boolean> skipExtension) {
        boolean shadowPass() {
            try { return (boolean) shadow.invokeExact(); }
            catch (Throwable e) { throw new IllegalStateException("Cannot query Iris shadow pass", e); }
        }
    }
}
