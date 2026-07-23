package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluidTypes {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, TheAurorian2.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> MOON_DEW = FLUID_TYPES.register(
            "moon_dew",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.theaurorian2.moon_dew")
                    .canExtinguish(true)
                    .canHydrate(true)
                    .supportsBoating(true)
                    .isWaterLike(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    private ModFluidTypes() {
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}
