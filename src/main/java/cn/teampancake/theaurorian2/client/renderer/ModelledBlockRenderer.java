package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.common.block.entity.ModelledBlockEntity;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;

public final class ModelledBlockRenderer<T extends ModelledBlockEntity>
        extends GeoBlockRenderer<T, BlockEntityRenderState> {

    public ModelledBlockRenderer(BlockEntityRendererProvider.Context context, Identifier modelId) {
        super(context, new DefaultedBlockGeoModel<>(modelId));
    }
}
