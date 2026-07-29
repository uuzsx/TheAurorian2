package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class AstrologyTableItem extends BlockItem implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public AstrologyTableItem(Block block, Item.Properties properties) {
        super(block, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<AstrologyTableItem> renderer;

            @Override
            public GeoItemRenderer<AstrologyTableItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new CulledItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AstrologyTableItem>(
                "idle", state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private static final class CulledItemRenderer extends GeoItemRenderer<AstrologyTableItem> {

        private CulledItemRenderer() {
            super(new DefaultedBlockGeoModel<>(TheAurorian2.id("astrology_table")));
        }

        @Override
        public RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
            return RenderTypes.entityCutoutCull(texture);
        }
    }
}
