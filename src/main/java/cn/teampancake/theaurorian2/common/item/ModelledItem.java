package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class ModelledItem extends Item implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;
    private final String textureName;
    private final @Nullable RawAnimation idleAnimation;

    public ModelledItem(
            Properties properties, String modelName, String textureName, @Nullable String idleAnimationName) {
        super(properties);
        this.modelName = modelName;
        this.textureName = textureName;
        this.idleAnimation = idleAnimationName == null
                ? null
                : RawAnimation.begin().thenLoop(idleAnimationName);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<ModelledItem> renderer;

            @Override
            public GeoItemRenderer<ModelledItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    DefaultedItemGeoModel<ModelledItem> model = new DefaultedItemGeoModel<>(
                            TheAurorian2.id(ModelledItem.this.modelName));
                    model.withAltTexture(TheAurorian2.id(ModelledItem.this.textureName));
                    this.renderer = new GeoItemRenderer<>(model).useAlternateGuiLighting();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (this.idleAnimation != null) {
            controllers.add(new AnimationController<ModelledItem>(
                    "idle", state -> state.setAndContinue(this.idleAnimation)));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
