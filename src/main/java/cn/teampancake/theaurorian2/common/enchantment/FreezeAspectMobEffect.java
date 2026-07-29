package cn.teampancake.theaurorian2.common.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class FreezeAspectMobEffect extends MobEffect {

    private static final int TICKS_PER_LEVEL = 4 * 20;

    public FreezeAspectMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x80E3EC);
    }

    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        refreshFrozenVisual(mob);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int remainingTicks, int amplifier) {
        int fullDuration = (amplifier + 1) * TICKS_PER_LEVEL;
        return remainingTicks < fullDuration && remainingTicks % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplifier) {
        if (mob.canFreeze()) {
            refreshFrozenVisual(mob);
            mob.hurtServer(level, mob.damageSources().freeze(), 1.0F);
        }
        return true;
    }

    private static void refreshFrozenVisual(LivingEntity mob) {
        if (!mob.canFreeze()) {
            return;
        }

        int visualFreezeTicks = Math.max(0, mob.getTicksRequiredToFreeze() - 1);
        mob.setTicksFrozen(Math.max(mob.getTicksFrozen(), visualFreezeTicks));
    }
}
