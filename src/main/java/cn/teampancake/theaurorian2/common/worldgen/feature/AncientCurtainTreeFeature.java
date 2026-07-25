package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class AncientCurtainTreeFeature extends Feature<NoneFeatureConfiguration> {

    public AncientCurtainTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        for (BlockPos origin : AncientTreeFeatureSupport.findPlacementCandidates(
                context.level(), context.origin(), random)) {
            int height = 30 + random.nextInt(9);
            TreePlan plan = createTreePlan(origin, height, random);
            if (!AncientTreeFeatureSupport.extendTrunkToTerrain(
                    context.level(), origin, plan.logs(), logState(Direction.Axis.Y))) {
                continue;
            }
            if (CustomTreeFeatureSupport.place(
                    context.level(), origin, plan.logs(), plan.leaves(),
                    ModBlocks.CURTAIN_TREE_LEAVES.get(), ModBlocks.CURTAIN_TREE_SAPLING.get())) {
                WallMushroomPlacement.placeOnAncientTree(
                        context.level(), origin, plan.logs(), random);
                return true;
            }
        }
        return false;
    }

    static TreePlan createTreePlan(BlockPos origin, int height, RandomSource random) {
        AncientSilentTreeFeature.TreePlan silentPlan =
                AncientSilentTreeFeature.createTreePlan(origin, height, random);
        Map<BlockPos, BlockState> curtainLogs = new LinkedHashMap<>();
        silentPlan.logs().forEach((pos, state) -> curtainLogs.put(
                pos,
                logState(state.getValue(RotatedPillarBlock.AXIS))));
        return new TreePlan(curtainLogs, new HashSet<>(silentPlan.leaves()));
    }

    private static BlockState logState(Direction.Axis axis) {
        return ModBlocks.CURTAIN_TREE_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
    }

    record TreePlan(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves) {
    }
}
