package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class AurorianGrassRockItem extends BlockItem implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public AurorianGrassRockItem(Block block, Item.Properties properties) {
        super(block, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<AurorianGrassRockItem> renderer;

            @Override
            public GeoItemRenderer<AurorianGrassRockItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new NoCullItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private static final class NoCullItemRenderer extends GeoItemRenderer<AurorianGrassRockItem> {

        private NoCullItemRenderer() {
            super(new DefaultedBlockGeoModel<>(TheAurorian2.id("aurorian_grass_rock")));
        }

        @Override
        public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
            super.adjustRenderPose(renderPassInfo);
            renderPassInfo.poseStack().translate(0.0F, -0.5F, 0.0F);
        }

        @Override
        public RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
            return RenderTypes.entityCutout(texture);
        }
    }
}
