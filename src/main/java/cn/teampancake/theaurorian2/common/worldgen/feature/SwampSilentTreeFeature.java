package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * HugeFungusFeature's warped stem/cap rules, with four extra blocks below the cap.
 * Vanilla keeps its height and cap builders private and uses non-decaying wart
 * blocks. Plan the same outline here so ordinary silent leaves can receive real
 * connections and normal decay distances before any blocks enter the world.
 */
public final class SwampSilentTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int EXTRA_HEIGHT = 4;

    public SwampSilentTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        TreePlan plan = createTreePlan(context.origin(), context.random());
        for (BlockPos leaf : plan.leaves()) {
            if (context.level().isOutsideBuildHeight(leaf)
                    || !TreeFeature.validTreePos(context.level(), leaf)
                    && !context.level().getBlockState(leaf).is(BlockTags.LEAVES)) {
                return false;
            }
        }
        return CustomTreeFeatureSupport.place(
                context.level(), context.origin(), plan.logs(), plan.leaves(),
                ModBlocks.SILENT_TREE_LEAVES.get(), ModBlocks.SILENT_TREE_SAPLING.get(),
                ModBlocks.FRUITING_SILENT_TREE_LEAVES.get(), 0.05F, context.random());
    }

    static TreePlan createTreePlan(BlockPos origin, RandomSource random) {
        int fungusHeight = 4 + random.nextInt(10);
        if (random.nextInt(12) == 0) {
            fungusHeight *= 2;
        }
        int height = fungusHeight + EXTRA_HEIGHT;
        boolean huge = random.nextFloat() < 0.06F;
        Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new LinkedHashSet<>();
        BlockState log = ModBlocks.SILENT_TREE_LOG.get().defaultBlockState();
        int stemRadius = huge ? 1 : 0;
        for (int x = -stemRadius; x <= stemRadius; x++) {
            for (int z = -stemRadius; z <= stemRadius; z++) {
                boolean corner = huge && Math.abs(x) == stemRadius && Math.abs(z) == stemRadius;
                for (int y = 0; y < fungusHeight; y++) {
                    if (!corner || random.nextFloat() < 0.1F) {
                        logs.put(origin.offset(x, y + EXTRA_HEIGHT, z), log);
                    }
                }
                for (int y = 0; y < EXTRA_HEIGHT; y++) {
                    if (!corner || logs.containsKey(origin.offset(x, y + EXTRA_HEIGHT, z))) {
                        logs.put(origin.offset(x, y, z), log);
                    }
                }
            }
        }

        // Keep native cap proportions; the extra height only lifts the cap.
        int capHeight = Math.min(random.nextInt(1 + fungusHeight / 3) + 5, fungusHeight);
        int capStart = height - capHeight;
        for (int y = capStart; y <= height; y++) {
            int radius = y < height - random.nextInt(3) ? 2 : 1;
            if (capHeight > 8 && y < capStart + 4) {
                radius = 3;
            }
            if (huge) {
                radius++;
            }
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (logs.containsKey(pos)) {
                        continue;
                    }
                    boolean edgeX = Math.abs(x) == radius;
                    boolean edgeZ = Math.abs(z) == radius;
                    boolean inside = !edgeX && !edgeZ && y != height;
                    if (y < capStart + 3) {
                        if (!inside && (leaves.contains(pos.below()) || random.nextFloat() < 0.15F)) {
                            leaves.add(pos);
                        }
                    } else if (inside ? placeCapBlock(random, 0.1F, 0.2F)
                            : edgeX && edgeZ ? placeCapBlock(random, 0.01F, 0.7F)
                            : placeCapBlock(random, 0.0005F, 0.98F)) {
                        leaves.add(pos);
                    }
                }
            }
        }

        // Wart caps can have disconnected pixels; living leaves cannot. Interior
        // connections retain every native cap pixel and stay inside its footprint.
        for (BlockPos leaf : List.copyOf(leaves)) {
            BlockPos cursor = new BlockPos(origin.getX(), Math.min(leaf.getY(), origin.getY() + height - 1), origin.getZ());
            while (cursor.distManhattan(leaf) > 6) {
                cursor = stepToward(cursor, leaf);
                logs.put(cursor, log);
            }
            while (cursor.distManhattan(leaf) > 1) {
                cursor = stepToward(cursor, leaf);
                if (!logs.containsKey(cursor)) {
                    leaves.add(cursor);
                }
            }
        }
        leaves.removeAll(logs.keySet());
        return new TreePlan(logs, leaves);
    }

    private static boolean placeCapBlock(RandomSource random, float decorChance, float capChance) {
        if (random.nextFloat() < decorChance) {
            return true;
        }
        if (random.nextFloat() < capChance) {
            random.nextFloat(); // Vanilla also samples its zero warped-vine chance.
            return true;
        }
        return false;
    }

    private static BlockPos stepToward(BlockPos from, BlockPos target) {
        int x = target.getX() - from.getX();
        int z = target.getZ() - from.getZ();
        if (x != 0 && Math.abs(x) >= Math.abs(z)) {
            return from.offset(Integer.signum(x), 0, 0);
        }
        if (z != 0) {
            return from.offset(0, 0, Integer.signum(z));
        }
        return from.offset(0, Integer.signum(target.getY() - from.getY()), 0);
    }

    record TreePlan(Map<BlockPos, BlockState> logs, Set<BlockPos> leaves) {}
}
