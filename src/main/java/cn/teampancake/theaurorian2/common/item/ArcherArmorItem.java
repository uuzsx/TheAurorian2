package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.ArcherArmorRenderer;
import cn.teampancake.theaurorian2.client.renderer.ArcherArmorItemRenderer;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

public final class ArcherArmorItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String textureName;
    private final ArmorType armorType;

    public ArcherArmorItem(Properties properties, String textureName, ArmorType armorType) {
        super(properties);
        this.textureName = textureName;
        this.armorType = armorType;
        GeoItem.registerSyncedAnimatable(this);
    }

    public String textureName() {
        return this.textureName;
    }

    public ArmorType armorType() {
        return this.armorType;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?, ?> renderer;
            private GeoItemRenderer<ArcherArmorItem> itemRenderer;

            @Override
            public GeoItemRenderer<ArcherArmorItem> getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new ArcherArmorItemRenderer(ArcherArmorItem.this);
                }
                return this.itemRenderer;
            }

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
