package cn.teampancake.theaurorian2.common.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class FrostbiteMobEffect extends AurorianMobEffect {

    private static final int DAMAGE_INTERVAL = 3 * 20;

    public FrostbiteMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xA7C6FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int remainingTicks, int amplifier) {
        return remainingTicks % DAMAGE_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.canFreeze()) {
            int visualFreezeTicks = Math.max(0, entity.getTicksRequiredToFreeze() - 1);
            entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), visualFreezeTicks));
            entity.hurtServer(level, entity.damageSources().freeze(), 1.0F);
        }
        return true;
    }
}
