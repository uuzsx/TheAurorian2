package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.MoonChestBlock.Variant;
import cn.teampancake.theaurorian2.common.block.entity.MoonChestBlockEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import java.util.EnumMap;

public final class MoonChestRenderer extends GeoBlockRenderer<MoonChestBlockEntity, BlockEntityRenderState> {
    public MoonChestRenderer(BlockEntityRendererProvider.Context context) { super(context, new ChestModel()); }

    private static final class ChestModel extends GeoModel<MoonChestBlockEntity> {
        private static final DataTicket<Variant> VARIANT = DataTickets.create("theaurorian2:treasure_chest_variant", Variant.class);
        private record Resources(Identifier model, Identifier texture, Identifier animation) {}
        private static final EnumMap<Variant, Resources> RESOURCES = new EnumMap<>(Variant.class);
        static {
            for (Variant variant : Variant.values()) RESOURCES.put(variant, new Resources(
                    TheAurorian2.id("block/"+variant.model),
                    TheAurorian2.id("textures/block/"+variant.model+".png"),
                    TheAurorian2.id("block/"+variant.model)));
        }
        @Override
        public void addAdditionalStateData(MoonChestBlockEntity chest, Object related, GeoRenderState state) {
            state.addGeckolibData(VARIANT, chest.variant());
        }
        @Override
        public Identifier getModelResource(GeoRenderState state) { return RESOURCES.get(state.getGeckolibData(VARIANT)).model(); }
        @Override
        public Identifier getTextureResource(GeoRenderState state) { return RESOURCES.get(state.getGeckolibData(VARIANT)).texture(); }
        @Override
        public Identifier getAnimationResource(MoonChestBlockEntity chest) { return RESOURCES.get(chest.variant()).animation(); }
    }
}
