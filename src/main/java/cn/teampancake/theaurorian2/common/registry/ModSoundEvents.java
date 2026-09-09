package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TheAurorian2.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> AMETHYST_WIND_CHIMES =
            SOUNDS.register("amethyst_wind_chimes", () -> SoundEvent.createFixedRangeEvent(
                    TheAurorian2.id("amethyst_wind_chimes"), 25));
    public static final DeferredHolder<SoundEvent, SoundEvent> BAMBOO_WIND_CHIMES =
            SOUNDS.register("bamboo_wind_chimes", () -> SoundEvent.createFixedRangeEvent(
                    TheAurorian2.id("bamboo_wind_chimes"), 25));
    private ModSoundEvents() {}
    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
