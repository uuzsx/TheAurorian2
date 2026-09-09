package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.block.entity.WindChimesBlockEntity;
import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class WindChimesRenderer extends GeoBlockRenderer<WindChimesBlockEntity, BlockEntityRenderState> {
    // The source's negative-size cubes form an inverted-hull outline. Both passes must cull
    // back faces, otherwise the solid purple outline shell covers the crystal texture.
    public static final RenderPipeline EMISSIVE_CULL =
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(TheAurorian2.id("pipeline/wind_chimes_emissive_cull"))
                    .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .withCull(true).build();

    @Override
    public RenderType getRenderType(BlockEntityRenderState state, Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }

    // GeckoLib adds GeoRenderState to vanilla BlockEntityRenderState at runtime.
    // Keep its original data store; only the layer's compile-time generic needs bridging.
    @SuppressWarnings({"rawtypes", "unchecked"})
    public WindChimesRenderer(BlockEntityRendererProvider.Context context, Identifier model, boolean glowing) {
        super(context, new DefaultedBlockGeoModel<>(model));
        if (glowing) {
            Identifier emissive = model.withPath("textures/block/" + model.getPath() + "_e.png");
            RenderType emissiveType = RenderType.create("wind_chimes_emissive_cull",
                    RenderSetup.builder(EMISSIVE_CULL).withTexture("Sampler0", emissive)
                            .sortOnUpload().createRenderSetup());
            withRenderLayer((GeoRenderLayer) new AutoGlowingGeoLayer((GeoRenderer) this) {
                @Override
                protected Identifier getTextureResource(GeoRenderState state) { return emissive; }
                @Override
                protected RenderType getRenderType(GeoRenderState state) { return emissiveType; }
            });
        }
    }
}
