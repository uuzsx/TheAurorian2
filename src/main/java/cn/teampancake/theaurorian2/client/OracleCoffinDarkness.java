package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.OracleCoffinEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Tracks only live coffin effects. A GUI backdrop remains black with Iris, F1 or third-person cameras. */
@EventBusSubscriber(modid=TheAurorian2.MOD_ID,value=Dist.CLIENT)
public final class OracleCoffinDarkness {
    private static final Set<OracleCoffinEntity> COFFINS=new HashSet<>();
    private OracleCoffinDarkness() {}
    @SubscribeEvent public static void join(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide() && event.getEntity() instanceof OracleCoffinEntity coffin) COFFINS.add(coffin);
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide() && event.getEntity() instanceof OracleCoffinEntity coffin) COFFINS.remove(coffin);
    }
    @SubscribeEvent public static void unload(LevelEvent.Unload event) {
        if(event.getLevel().isClientSide()) COFFINS.clear();
    }
    @SubscribeEvent public static void beforeTick(EntityTickEvent.Pre event) { hold(event.getEntity()); }
    @SubscribeEvent public static void afterTick(EntityTickEvent.Post event) { hold(event.getEntity()); }
    private static void hold(net.minecraft.world.entity.Entity entity) {
        var player=Minecraft.getInstance().player;
        if(player==null || entity!=player) return;
        for(var coffin:COFFINS) if(coffin.level()==player.level() && coffin.restrains(player)) {
            player.setDeltaMovement(Vec3.ZERO); player.fallDistance=0; player.setPos(coffin.victimAnchor());
            return;
        }
    }
    public static boolean obscured() {
        var player=Minecraft.getInstance().player;
        if(player==null) return false;
        for(var coffin:COFFINS) if(coffin.obscures(player)) return true;
        return false;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void render(RenderGuiEvent.Pre event) {
        if(!obscured()) return;
        var graphics=event.getGuiGraphics();
        graphics.fill(0,0,graphics.guiWidth(),graphics.guiHeight(),0xFF000000);
    }
}
