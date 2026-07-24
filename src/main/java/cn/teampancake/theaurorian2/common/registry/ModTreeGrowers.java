package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ModTreeGrowers {

    private static final ResourceKey<ConfiguredFeature<?, ?>> SILENT_TREE_FEATURE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, TheAurorian2.id("silent_tree"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> CURTAIN_TREE_FEATURE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, TheAurorian2.id("curtain_tree"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> CURSED_FROST_TREE_FEATURE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, TheAurorian2.id("cursed_frost_tree"));

    public static final TreeGrower SILENT_TREE = new TreeGrower(
            "theaurorian2:silent_tree", Optional.empty(), Optional.of(SILENT_TREE_FEATURE), Optional.empty());
    public static final TreeGrower CURTAIN_TREE = new TreeGrower(
            "theaurorian2:curtain_tree", Optional.empty(), Optional.of(CURTAIN_TREE_FEATURE), Optional.empty());
    public static final TreeGrower CURSED_FROST_TREE = new TreeGrower(
            "theaurorian2:cursed_frost_tree", Optional.empty(), Optional.of(CURSED_FROST_TREE_FEATURE), Optional.empty());

    private ModTreeGrowers() {
    }
}
