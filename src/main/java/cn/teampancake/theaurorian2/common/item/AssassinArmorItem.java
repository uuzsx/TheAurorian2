package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.client.renderer.AssassinArmorRenderer;
import cn.teampancake.theaurorian2.client.renderer.AssassinArmorItemRenderer;
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

public final class AssassinArmorItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;
    private final ArmorType armorType;

    public AssassinArmorItem(Properties properties, String modelName, ArmorType armorType) {
        super(properties);
        this.modelName = modelName;
        this.armorType = armorType;
    }

    public String modelName() {
        return this.modelName;
    }

    public ArmorType armorType() {
        return this.armorType;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?, ?> armorRenderer;
            private GeoItemRenderer<AssassinArmorItem> itemRenderer;

            @Override
            public GeoItemRenderer<AssassinArmorItem> getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new AssassinArmorItemRenderer(AssassinArmorItem.this);
                }
                return this.itemRenderer;
            }

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack stack, EquipmentSlot slot) {
                if (this.armorRenderer == null) {
                    this.armorRenderer = new AssassinArmorRenderer(AssassinArmorItem.this);
                }
                return this.armorRenderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // These source models have no nonempty animation tracks; armor follows the wearer's pose.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
