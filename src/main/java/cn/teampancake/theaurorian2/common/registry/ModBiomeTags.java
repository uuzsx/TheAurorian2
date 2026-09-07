package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomeTags {

    public static final TagKey<Biome> HAS_SILENT_TREE_LEAF_LITTER =
            TagKey.create(Registries.BIOME, TheAurorian2.id("has_silent_tree_leaf_litter"));

    private ModBiomeTags() {
    }
}
