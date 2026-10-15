package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.CrystalWormEntity;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.loading.math.MolangQueries;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class CrystalWormRenderer extends SimpleGeoMobRenderer<CrystalWormEntity> {
    public CrystalWormRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("crystal_worm"), .2F);
    }
    public static void registerAnimationQueries() {
        MolangQueries.setActorVariable("query.theaurorian2_worm_pitch", actor -> {
            if (!(actor.animatable() instanceof CrystalWormEntity worm) || !worm.isAlive()
                    || worm.phase() == CrystalWormEntity.Phase.BURROW_IN || worm.phase() == CrystalWormEntity.Phase.BURROW_OUT) return 0;
            return Mth.lerp(actor.partialTick(), worm.xRotO, worm.getXRot());
        });
    }
    @Override protected float getDeathMaxRotation(GeoRenderState state) { return 0; }
    @Override protected AABB getBoundingBoxForCulling(CrystalWormEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(.6, 2, .6);
    }
}
