package cn.teampancake.theaurorian2.client.renderer.state;

import cn.teampancake.theaurorian2.common.entity.AnimalVisualState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class AurorianAnimalRenderState extends LivingEntityRenderState {
    public float panicBlend, idleBlend, idleTime, startleTime;
    public int idleVariant;
    public float jumpCompletion, headEatPositionScale, headEatAngleScale;
    public void extract(AnimalVisualState visual, float partialTick) {
        panicBlend = visual.panicBlend(partialTick);
        idleBlend = visual.idleBlend(partialTick);
        idleVariant = visual.idleVariant();
        idleTime = visual.idleTime(partialTick);
        startleTime = visual.startleTime(partialTick);
    }
}
