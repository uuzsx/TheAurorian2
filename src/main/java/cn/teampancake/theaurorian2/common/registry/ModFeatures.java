package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.feature.CurtainTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.CursedFrostTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.FallenLogFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.SilentTreeFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<Feature<?>, CurtainTreeFeature> CURTAIN_TREE =
            FEATURES.register("curtain_tree", CurtainTreeFeature::new);
    public static final DeferredHolder<Feature<?>, SilentTreeFeature> SILENT_TREE =
            FEATURES.register("silent_tree", SilentTreeFeature::new);
    public static final DeferredHolder<Feature<?>, CursedFrostTreeFeature> CURSED_FROST_TREE =
            FEATURES.register("cursed_frost_tree", CursedFrostTreeFeature::new);
    public static final DeferredHolder<Feature<?>, FallenLogFeature> FALLEN_LOG =
            FEATURES.register("fallen_log", FallenLogFeature::new);

    private ModFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
