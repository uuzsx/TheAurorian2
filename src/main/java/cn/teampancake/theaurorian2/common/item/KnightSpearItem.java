package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.KnightSpearRenderer;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class KnightSpearItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String textureName;

    public KnightSpearItem(Properties properties, String textureName) {
        super(properties);
        this.textureName = textureName;
        GeoItem.registerSyncedAnimatable(this);
    }

    public String textureName() {
        return this.textureName;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<KnightSpearItem> renderer;

            @Override
            public GeoItemRenderer<KnightSpearItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new KnightSpearRenderer(KnightSpearItem.this);
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
        return this.cache;
    }
}
