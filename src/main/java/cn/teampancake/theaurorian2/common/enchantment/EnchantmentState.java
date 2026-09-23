package cn.teampancake.theaurorian2.common.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Only allocated for entities using stateful enchantments. No per-tick network synchronization. */
public final class EnchantmentState {
    public int experienceTenths;
    public int springPulses;
    public int springDelay;
    public int guardianTicks;
    public int heatTicks;
    public int lightningHits;
    public long lightningLastHit;
    public ItemStack lightningWeapon = ItemStack.EMPTY;
    public int criticalTarget = -1;
    public long criticalTick = -1;
    public float criticalMultiplier = 1;
    public int virtualProjectile = -1;
    public long virtualProjectileTick = -1;
    public float reflectionHealth;

    public static EnchantmentState read(ValueInput input) {
        EnchantmentState state = new EnchantmentState();
        state.experienceTenths = Math.clamp(input.getIntOr("experience_tenths", 0), 0, 9);
        state.springPulses = Math.clamp(input.getIntOr("spring_pulses", 0), 0, 5);
        state.springDelay = Math.clamp(input.getIntOr("spring_delay", 0), 0, 20);
        state.guardianTicks = Math.clamp(input.getIntOr("guardian_ticks", 0), 0, 40);
        state.heatTicks = Math.clamp(input.getIntOr("heat_ticks", 0), 0, 80);
        return state;
    }

    public void write(ValueOutput output) {
        output.putInt("experience_tenths", experienceTenths);
        output.putInt("spring_pulses", springPulses);
        output.putInt("spring_delay", springDelay);
        output.putInt("guardian_ticks", guardianTicks);
        output.putInt("heat_ticks", heatTicks);
    }
}
