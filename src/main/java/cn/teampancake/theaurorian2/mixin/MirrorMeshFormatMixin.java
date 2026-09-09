package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.renderer.mirror.MirrorShaderCompat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** SectionCompiler has no format parameter; Iris changes its layer format even on our worker. */
@Mixin(SectionCompiler.class)
public abstract class MirrorMeshFormatMixin {
    @ModifyArg(method = "getOrBeginLayer", at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/vertex/BufferBuilder;<init>(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 2)
    private VertexFormat theaurorian2$mirrorFormat(VertexFormat original) {
        return MirrorShaderCompat.compilingMesh() ? DefaultVertexFormat.BLOCK : original;
    }
}
