package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class ModelledBlockItem extends BlockItem implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;

    public ModelledBlockItem(Block block, Item.Properties properties, String modelName) {
        super(block, properties.useBlockDescriptionPrefix());
        this.modelName = modelName;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<ModelledBlockItem> renderer;

            @Override
            public GeoItemRenderer<ModelledBlockItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<ModelledBlockItem>(
                            new DefaultedBlockGeoModel<>(TheAurorian2.id(ModelledBlockItem.this.modelName)))
                            .useAlternateGuiLighting();
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
}
