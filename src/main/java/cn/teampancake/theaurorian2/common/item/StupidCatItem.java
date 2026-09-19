package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import cn.teampancake.theaurorian2.client.model.DollGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;

public final class StupidCatItem extends BlockItem implements GeoItem {
    private final Identifier modelId;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public StupidCatItem(Properties properties) {
        this(ModBlocks.STUPID_CAT.get(), "stupid_cat", properties);
    }
    public StupidCatItem(net.minecraft.world.level.block.Block block, String modelName, Properties properties) {
        super(block, properties.useBlockDescriptionPrefix().component(DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.HEAD).setDamageOnHurt(false).build()));
        this.modelId = TheAurorian2.id(modelName);
    }
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<StupidCatItem> renderer;
            @Override
            public GeoItemRenderer<StupidCatItem> getGeoItemRenderer() {
                if (renderer == null) renderer = new GeoItemRenderer<StupidCatItem>(
                        new DollGeoModel<>(modelId)) {
                    @Override
                    public RenderType getRenderType(GeoRenderState state, Identifier texture) {
                        return RenderTypes.entityCutoutCull(texture);
                    }
                };
                return renderer;
            }
        });
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
