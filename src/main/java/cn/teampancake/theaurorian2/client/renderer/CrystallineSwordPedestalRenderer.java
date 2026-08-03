package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.CrystallineSwordPedestalBlock;
import cn.teampancake.theaurorian2.common.block.entity.CrystallineSwordPedestalBlockEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public final class CrystallineSwordPedestalRenderer
        extends GeoBlockRenderer<CrystallineSwordPedestalBlockEntity, BlockEntityRenderState> {

    private static final DataTicket<CrystallineSwordPedestalBlock.Phase> PHASE =
            DataTicket.create("crystalline_sword_pedestal_phase", new TypeToken<>() {});

    public CrystallineSwordPedestalRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(TheAurorian2.id("crystalline_sword_pedestal")));
    }

    @Override
    public void captureDefaultRenderState(
            CrystallineSwordPedestalBlockEntity pedestal,
            @Nullable Void relatedObject,
            BlockEntityRenderState renderState,
            float partialTick) {
        super.captureDefaultRenderState(pedestal, relatedObject, renderState, partialTick);
        ((com.geckolib.renderer.base.GeoRenderState) (Object) renderState).addGeckolibData(
                PHASE, pedestal.getBlockState().getValue(CrystallineSwordPedestalBlock.PHASE));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustModelBonesForRender(
            RenderPassInfo renderPassInfo, BoneSnapshots snapshots) {
        boolean empty = renderPassInfo.getOrDefaultGeckolibData(
                PHASE, CrystallineSwordPedestalBlock.Phase.SEALED) == CrystallineSwordPedestalBlock.Phase.EMPTY;
        snapshots.ifPresent("sword", snapshot -> snapshot.skipRender(empty).skipChildrenRender(empty));
        snapshots.ifPresent("tentacle", snapshot -> snapshot.skipRender(empty).skipChildrenRender(empty));
    }
}
