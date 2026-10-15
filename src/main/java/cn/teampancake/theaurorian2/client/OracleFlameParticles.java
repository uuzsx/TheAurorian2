package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleArcaneEntity;
import cn.teampancake.theaurorian2.common.entity.OracleFlamePath;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Reuses the original blue soul-fire sprite; short trails keep each helix legible. */
@EventBusSubscriber(modid=TheAurorian2.MOD_ID,value=Dist.CLIENT)
public final class OracleFlameParticles {
    private static long budgetTick = Long.MIN_VALUE;
    private static int spawned;

    @SubscribeEvent public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof OracleArcaneEntity spell) || !spell.level().isClientSide()
                || spell.kind()!=OracleArcaneEntity.FLAME || spell.isRemoved()
                || spell.age(0)>=OracleArcaneEntity.FLAME_DURATION || spell.tickCount%2!=0) return;
        var mc = Minecraft.getInstance();
        if (mc.getCameraEntity()==null || spell.distanceToSqr(mc.getCameraEntity())>48*48) return;
        long now = spell.level().getGameTime();
        if (budgetTick!=now) { budgetTick=now; spawned=0; }
        int stride = mc.options.particles().get()==ParticleStatus.MINIMAL ? 4
                : mc.options.particles().get()==ParticleStatus.DECREASED ? 2 : 1;
        var path = spell.flamePath();
        for (int strand=0;strand<OracleFlamePath.STRANDS;strand++) {
            for (int step=strand==0 ? 2 : 1;step<=path.end(strand) && spawned<384;step+=strand==0 ? stride*2 : stride) {
                var p = path.point(strand,step);
                var particle = mc.particleEngine.createParticle(ParticleTypes.SOUL_FIRE_FLAME,p.x,p.y,p.z,0,0,0);
                if (particle!=null) {
                    particle.setParticleSpeed(0,0,0);
                    particle.setLifetime(3);
                    particle.scale(strand==0 ? .85F : 1.5F);
                    spawned++;
                }
            }
        }
    }
}
