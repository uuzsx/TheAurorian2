package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.WorldScrollTeleportEntity;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

public final class WorldScrollTeleportRenderer extends GeoEntityRenderer<WorldScrollTeleportEntity, EntityRenderState> {
    // Back-face culling preserves the author's negative-size inverted hulls.
    // The source diffuse/emissive atlases are identical: one emissive pass is sufficient.
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withLocation(TheAurorian2.id("pipeline/world_scroll_teleport"))
            .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(true).build();
    private static final RenderType TYPE = RenderType.create("world_scroll_teleport",
            RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", TheAurorian2.id("textures/entity/world_scroll_teleport.png"))
                    .sortOnUpload().createRenderSetup());

    public WorldScrollTeleportRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(TheAurorian2.id("world_scroll_teleport")));
        shadowRadius = 0;
    }

    @Override public RenderType getRenderType(EntityRenderState state, Identifier texture) { return TYPE; }

    @Override public int getRenderColor(WorldScrollTeleportEntity entity, Void related, float partialTick) {
        float alpha = entity.opacity(partialTick);
        var mc = Minecraft.getInstance();
        var viewer = mc.getCameraEntity();
        // Looking from inside an inverted beam shell must not turn first-person
        // play into a solid white screen. Distant and third-person views stay intact.
        if (mc.options.getCameraType().isFirstPerson() && viewer != null
                && Math.abs(viewer.getX() - entity.getX()) < 0.85
                && Math.abs(viewer.getZ() - entity.getZ()) < 0.85
                && viewer.getEyeY() > entity.getY() && viewer.getEyeY() < entity.getY() + 3) {
            alpha *= 0.12F;
        }
        return ((int) (255 * alpha) << 24) | 0xFFFFFF;
    }

    @Override protected AABB getBoundingBoxForCulling(WorldScrollTeleportEntity entity) {
        // Include the authored sky beam, even when the small anchor is off screen.
        return new AABB(entity.getX() - 8, entity.getY() - 2, entity.getZ() - 8,
                entity.getX() + 8, entity.getY() + 2002, entity.getZ() + 8);
    }
}
