package cn.teampancake.theaurorian2.common.effect;

import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import java.util.ArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Stable entry points for bosses, weapons, skills and commands that apply Aurorian effects. */
public final class AurorianEffects {

    private AurorianEffects() {
    }

    public static boolean apply(LivingEntity target, Holder<MobEffect> effect, int durationTicks) {
        return apply(target, effect, durationTicks, 0);
    }

    public static boolean apply(LivingEntity target, Holder<MobEffect> effect, int durationTicks, int amplifier) {
        return target.addEffect(new MobEffectInstance(effect, durationTicks, amplifier));
    }

    public static boolean isMilkResistantCurse(Holder<MobEffect> effect) {
        return effect.is(ModMobEffects.PRESSURE.getKey())
                || effect.is(ModMobEffects.CORRUPTION.getKey())
                || effect.is(ModMobEffects.FORBIDDEN_CURSE.getKey())
                || effect.is(ModMobEffects.INCANTATION.getKey())
                || effect.is(ModMobEffects.CRYSTALLIZATION.getKey());
    }

    public static void cleanseWithHoliness(LivingEntity target) {
        ArrayList<Holder<MobEffect>> harmful = new ArrayList<>();
        for (MobEffectInstance instance : target.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                harmful.add(instance.getEffect());
            }
        }

        EffectRemovalContext.run(EffectRemovalContext.Reason.HOLINESS, () -> {
            harmful.forEach(target::removeEffect);
            return null;
        });
    }
}
