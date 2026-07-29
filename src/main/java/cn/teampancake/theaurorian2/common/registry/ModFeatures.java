package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.feature.AncientCurtainTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AncientSilentTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AncientStumpFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianGrassRockFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianSeagrassFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianDripstoneClusterFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianLargeDripstoneFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianPointedDripstoneFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.CurtainTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.CursedFrostTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.FallenLogFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.SilentTreeFeature;
import cn.teampancake.theaurorian2.common.worldgen.feature.WallMushroomPatchFeature;
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
    public static final DeferredHolder<Feature<?>, AncientSilentTreeFeature> ANCIENT_SILENT_TREE =
            FEATURES.register("ancient_silent_tree", AncientSilentTreeFeature::new);
    public static final DeferredHolder<Feature<?>, AncientCurtainTreeFeature> ANCIENT_CURTAIN_TREE =
            FEATURES.register("ancient_curtain_tree", AncientCurtainTreeFeature::new);
    public static final DeferredHolder<Feature<?>, AncientStumpFeature> ANCIENT_STUMP =
            FEATURES.register("ancient_stump", AncientStumpFeature::new);
    public static final DeferredHolder<Feature<?>, WallMushroomPatchFeature> WALL_MUSHROOM_PATCH =
            FEATURES.register("wall_mushroom_patch", WallMushroomPatchFeature::new);
    public static final DeferredHolder<Feature<?>, FallenLogFeature> FALLEN_LOG =
            FEATURES.register("fallen_log", FallenLogFeature::new);
    public static final DeferredHolder<Feature<?>, AurorianSeagrassFeature> AURORIAN_SEAGRASS =
            FEATURES.register("aurorian_seagrass", AurorianSeagrassFeature::new);
    public static final DeferredHolder<Feature<?>, AurorianGrassRockFeature> AURORIAN_GRASS_ROCK =
            FEATURES.register("aurorian_grass_rock", AurorianGrassRockFeature::new);
    public static final DeferredHolder<Feature<?>, AurorianDripstoneClusterFeature> AURORIAN_DRIPSTONE_CLUSTER =
            FEATURES.register("aurorian_dripstone_cluster", AurorianDripstoneClusterFeature::new);
    public static final DeferredHolder<Feature<?>, AurorianLargeDripstoneFeature> AURORIAN_LARGE_DRIPSTONE =
            FEATURES.register("aurorian_large_dripstone", AurorianLargeDripstoneFeature::new);
    public static final DeferredHolder<Feature<?>, AurorianPointedDripstoneFeature> AURORIAN_POINTED_DRIPSTONE =
            FEATURES.register("aurorian_pointed_dripstone", AurorianPointedDripstoneFeature::new);

    private ModFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
