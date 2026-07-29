package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.ArcherArmorRenderer;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ArcherArmorItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String textureName;

    public ArcherArmorItem(Properties properties, String textureName) {
        super(properties);
        this.textureName = textureName;
    }

    public String textureName() {
        return this.textureName;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?, ?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.renderer == null) {
                    this.renderer = new ArcherArmorRenderer(ArcherArmorItem.this);
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
