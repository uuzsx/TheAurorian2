package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.StupidCatBlockEntity;
import cn.teampancake.theaurorian2.client.model.DollGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class StupidCatRenderer extends GeoBlockRenderer<StupidCatBlockEntity, BlockEntityRenderState> {
    public StupidCatRenderer(BlockEntityRendererProvider.Context context) {
        this(context, "stupid_cat");
    }
    public StupidCatRenderer(BlockEntityRendererProvider.Context context, String modelName) {
        super(context, new DollGeoModel<>(TheAurorian2.id(modelName)));
    }
    @Override
    public RenderType getRenderType(BlockEntityRenderState state, Identifier texture) {
        // Open/closed eye art shares a plane; render only its outward-facing side.
        return RenderTypes.entityCutoutCull(texture);
    }
}
