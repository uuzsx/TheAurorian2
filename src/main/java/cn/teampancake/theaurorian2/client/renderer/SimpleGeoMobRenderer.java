package cn.teampancake.theaurorian2.client.renderer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class SimpleGeoMobRenderer<T extends LivingEntity & GeoAnimatable>
        extends GeoEntityRenderer<T, LivingEntityRenderState> {

    public SimpleGeoMobRenderer(
            EntityRendererProvider.Context context,
            Identifier asset,
            float shadowRadius) {
        super(context, new DefaultedEntityGeoModel<>(asset));
        this.shadowRadius = shadowRadius;
    }

    @Override
    public LivingEntityRenderState createRenderState(T entity, Void relatedObject) {
        return new LivingEntityRenderState();
    }
}
