package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class SilentTreeFeature extends Feature<NoneFeatureConfiguration> {

    public SilentTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int height = 12 + random.nextInt(4);
        CurtainTreeFeature.TreePlan plan = CurtainTreeFeature.createTreePlan(
                origin, height, random, ModBlocks.SILENT_TREE_LOG.get());
        return CustomTreeFeatureSupport.place(
                context.level(), origin, plan.logs(), plan.leaves(),
                ModBlocks.SILENT_TREE_LEAVES.get(), ModBlocks.SILENT_TREE_SAPLING.get(),
                ModBlocks.FRUITING_SILENT_TREE_LEAVES.get(), 0.05F, random);
    }
}
