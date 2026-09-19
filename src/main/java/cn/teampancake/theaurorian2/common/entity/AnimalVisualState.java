package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/** Client-ticked visual timing only; does not change AI, attributes or saved data. */
public final class AnimalVisualState {
    private float panicBlend, previousPanicBlend, idleBlend, previousIdleBlend;
    private int idleVariant, idleStartTick, nextIdleTick, startleTick = -1000, previousHurtTime;
    private boolean previousPanic;
    private int tickCount;
    private final int firstDuration, secondDuration;
    public AnimalVisualState(int firstDuration, int secondDuration) {
        this.firstDuration = firstDuration;
        this.secondDuration = secondDuration;
    }
    public void tick(Mob animal, boolean panic) {
        tickCount = animal.tickCount;

        if (animal.isAlive() && (animal.hurtTime > previousHurtTime || (panic && !previousPanic && tickCount - startleTick > 11))) startleTick = tickCount;
        previousHurtTime = animal.hurtTime;
        previousPanic = panic;
        previousPanicBlend = panicBlend;
        panicBlend = Mth.approach(panicBlend, panic && animal.isAlive() ? 1.0F : 0.0F, 0.2F);
        previousIdleBlend = idleBlend;
        boolean resting = animal.isAlive() && !panic && animal.walkAnimation.speed() < 0.025F && animal.onGround() && !animal.isInWater() && tickCount - startleTick > 11;
        int duration = idleVariant == 1 ? firstDuration : secondDuration;
        boolean active = idleVariant != 0 && tickCount - idleStartTick < duration;
        idleBlend = Mth.approach(idleBlend, resting && active ? 1.0F : 0.0F, 0.15F);
        if (idleVariant != 0 && (!resting || !active) && idleBlend == 0) {
            idleVariant = 0;
            nextIdleTick = tickCount + 100 + animal.getRandom().nextInt(141);
        }
        if (nextIdleTick == 0) nextIdleTick = tickCount + 60 + animal.getRandom().nextInt(141);
        if (resting && idleVariant == 0 && tickCount >= nextIdleTick) {
            idleVariant = 1 + animal.getRandom().nextInt(2);
            idleStartTick = tickCount;
        }
    }

    public float panicBlend(float partialTick) { return Mth.lerp(partialTick, previousPanicBlend, panicBlend); }
    public float idleBlend(float partialTick) { return Mth.lerp(partialTick, previousIdleBlend, idleBlend); }
    public int idleVariant() { return idleVariant; }
    public float idleTime(float partialTick) { return (tickCount - idleStartTick + partialTick) / 20.0F; }
    public float startleTime(float partialTick) { return (tickCount - startleTick + partialTick) / 20.0F; }

}
