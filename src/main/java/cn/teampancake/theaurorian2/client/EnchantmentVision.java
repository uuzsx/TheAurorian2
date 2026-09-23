package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentRules;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Viewer-local vision: never changes another player's glow flags or global gamma settings. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class EnchantmentVision {
    private static final Set<Integer> VISIBLE = new HashSet<>();
    private static ClientLevel lastLevel;
    private static float aurora;
    private EnchantmentVision() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (lastLevel != minecraft.level || minecraft.player == null) {
            VISIBLE.clear();
            aurora = 0;
            lastLevel = minecraft.level;
        }
        var player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        boolean impaired = player.hasEffect(MobEffects.BLINDNESS) || player.hasEffect(MobEffects.DARKNESS);
        float target = !impaired && EnchantmentRules.level(player, EquipmentSlot.HEAD, ModEnchantments.AURORA) > 0
                ? 0.35F * Mth.clamp((8 - player.level().getMaxLocalRawBrightness(player.blockPosition())) / 8.0F, 0, 1) : 0;
        aurora = Mth.lerp(0.15F, aurora, target);
        boolean moonlight = !impaired && player.level().dimensionTypeRegistration().is(TheAurorian2.AURORIAN_DIMENSION_TYPE)
                && Math.floorMod(player.level().getDefaultClockTime(), 24000) >= 12000
                && EnchantmentRules.level(player, EquipmentSlot.HEAD, ModEnchantments.MOONLIGHT) > 0;
        if (!moonlight) {
            VISIBLE.clear();
            return;
        }
        if (player.tickCount % 5 != 0) return;
        VISIBLE.clear();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator() && !entity.isInvisibleTo(player))) {
            Vec3 direction = entity.getBoundingBox().getCenter().subtract(eye);
            if (direction.lengthSqr() <= 400 && direction.normalize().dot(look) >= 0.5 && player.hasLineOfSight(entity)) VISIBLE.add(entity.getId());
        }
    }

    public static boolean highlighted(Entity entity) { return entity.level() == lastLevel && VISIBLE.contains(entity.getId()); }
    public static float auroraIntensity() { return aurora; }
}
