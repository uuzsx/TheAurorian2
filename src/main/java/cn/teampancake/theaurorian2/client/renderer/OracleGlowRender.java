package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.fml.ModList;

/** Shared compatibility boundary for attached, additive surface light. */
final class OracleGlowRender {
    private static MethodHandle irisShadowPass = findIrisQuery("isRenderingShadowPass");
    private static MethodHandle irisShaderPack = findIrisQuery("isShaderPackInUse");

    private OracleGlowRender() {}

    static RenderPipeline pipeline(String name) {
        // Derive from EYES so Iris retains its entity-emissive program mapping.
        return RenderPipelines.EYES.toBuilder().withLocation(TheAurorian2.id("pipeline/" + name))
                .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("ALPHA_CUTOUT", .01F)
                .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)).withCull(true).build();
    }

    /** Public optional API, resolved once; also check when deferred geometry executes. */
    private static MethodHandle findIrisQuery(String query) {
        if (!ModList.get().isLoaded("iris")) return null;
        try {
            var api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return MethodHandles.publicLookup().unreflect(api.getMethod(query)).bindTo(instance);
        } catch (ReflectiveOperationException | LinkageError e) {
            TheAurorian2.LOGGER.warn("Cannot query Iris {} for surface glow", query, e);
            return null;
        }
    }

    static boolean shadowPass() {
        if (irisShadowPass == null) return false;
        try { return (boolean)irisShadowPass.invokeExact(); }
        catch (Throwable e) {
            irisShadowPass = null;
            TheAurorian2.LOGGER.warn("Iris shadow query failed for Oracle glow", e);
            return false;
        }
    }

    static boolean shaderPackActive() {
        if (irisShaderPack == null) return false;
        try { return (boolean)irisShaderPack.invokeExact(); }
        catch (Throwable e) {
            irisShaderPack = null;
            TheAurorian2.LOGGER.warn("Iris shader-pack query failed for surface glow", e);
            return false;
        }
    }
}
