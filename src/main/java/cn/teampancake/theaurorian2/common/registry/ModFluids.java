package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.fluid.MoonDewFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, TheAurorian2.MOD_ID);

    public static final DeferredHolder<Fluid, FlowingFluid> MOON_DEW = FLUIDS.register(
            "moon_dew", MoonDewFluid.Source::new);
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOON_DEW = FLUIDS.register(
            "flowing_moon_dew", MoonDewFluid.Flowing::new);

    private ModFluids() {
    }

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}
