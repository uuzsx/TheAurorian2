package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomeTags {
    public static final TagKey<Biome> IS_AURORIAN_CAVE =
            TagKey.create(Registries.BIOME, TheAurorian2.id("is_aurorian_cave"));
    public static final TagKey<Biome> HAS_FROZEN_OCEAN_SURFACE =
            TagKey.create(Registries.BIOME, TheAurorian2.id("has_frozen_ocean_surface"));

    public static final TagKey<Biome> HAS_SILENT_TREE_LEAF_LITTER =
            TagKey.create(Registries.BIOME, TheAurorian2.id("has_silent_tree_leaf_litter"));
    public static final TagKey<Biome> HAS_FOREST_ALLAYS =
            TagKey.create(Registries.BIOME, TheAurorian2.id("has_forest_allays"));

    private ModBiomeTags() {
    }
}
