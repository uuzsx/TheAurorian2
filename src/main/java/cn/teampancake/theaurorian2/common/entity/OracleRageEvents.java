package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class OracleRageEvents {
    private OracleRageEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforeWardDamage(LivingDamageEvent.Pre event) {
        // This stage runs after immunity, vanilla hurt cooldowns and damage
        // reductions. An ignored/zero hit must not spend a shield; an accepted
        // hit is fully blocked, including the hit that breaks the last shield.
        if (event.getEntity() instanceof RisingClockOracleEntity oracle
                && oracle.absorbWardHit(event.getSource(),event.getNewDamage())) event.setNewDamage(0);
    }

    @SubscribeEvent
    public static void afterSpellDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof RisingClockOracleEntity oracle)) return;
        var direct = event.getSource().getDirectEntity();
        if (!(direct instanceof OracleSpellEntity || direct instanceof OracleArcaneEntity || direct instanceof OracleCoffinEntity)) return;
        // Absorption still receives a hit; immunity, blocked damage and the gravity spell's
        // separate vanilla fall damage must not count as additional spell hits.
        if (event.getHealthDamage() > 0 || event.getReduction(DamageContainer.Reduction.ABSORPTION) > 0)
            oracle.recordSpellHit(event.getEntity());
    }
}
