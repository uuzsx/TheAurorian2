package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.LakeIslandBiomeSource;
import cn.teampancake.theaurorian2.common.worldgen.LakeIslandDensity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, TheAurorian2.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<LakeIslandBiomeSource>> LAKE_ISLAND_BIOME_SOURCE =
            BIOME_SOURCES.register("lake_island", () -> LakeIslandBiomeSource.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<LakeIslandDensity>> LAKE_ISLAND_DENSITY =
            DENSITY_FUNCTION_TYPES.register("lake_island", () -> LakeIslandDensity.CODEC);

    private ModWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        BIOME_SOURCES.register(modEventBus);
        DENSITY_FUNCTION_TYPES.register(modEventBus);
    }
}
