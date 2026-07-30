package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.KnightGreatswordRenderer;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class KnightGreatswordItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String textureName;

    public KnightGreatswordItem(Properties properties, String textureName) {
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
            private @Nullable GeoItemRenderer<KnightGreatswordItem> renderer;

            @Override
            public GeoItemRenderer<KnightGreatswordItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new KnightGreatswordRenderer(KnightGreatswordItem.this);
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
