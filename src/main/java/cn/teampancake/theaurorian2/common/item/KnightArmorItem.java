package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.KnightArmorItemRenderer;
import cn.teampancake.theaurorian2.client.renderer.KnightArmorRenderer;
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

public final class KnightArmorItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String textureName;
    private final ArmorType armorType;

    public KnightArmorItem(Properties properties, String textureName, ArmorType armorType) {
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
            private GeoArmorRenderer<?, ?> armorRenderer;
            private GeoItemRenderer<KnightArmorItem> itemRenderer;

            @Override
            public GeoItemRenderer<KnightArmorItem> getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new KnightArmorItemRenderer(KnightArmorItem.this);
                }

                return this.itemRenderer;
            }

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.armorRenderer == null) {
                    this.armorRenderer = new KnightArmorRenderer(KnightArmorItem.this);
                }

                return this.armorRenderer;
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
