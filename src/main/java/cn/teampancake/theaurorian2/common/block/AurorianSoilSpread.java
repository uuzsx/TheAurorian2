package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/** Native grass tick rules with destination checks for the two covers sharing Aurorian dirt.
 * SpreadingSnowyBlock's propagation helpers are private, so this small bounded tick is shared
 * by our blocks instead of changing vanilla grass or intercepting global block updates.
 */
final class AurorianSoilSpread {
    private static final Identifier SWAMP = TheAurorian2.id("starblight_swamp");
    private static final Identifier FLOWER_ISLAND = TheAurorian2.id("equinox_flower_island");

    private AurorianSoilSpread() {
    }

    static boolean isStarblight(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(SWAMP);
    }

    static void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, boolean starblight) {
        if (!canStayAlive(state, level, pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.AURORIAN_DIRT.get().defaultBlockState());
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }
        boolean sourceSwamp = starblight && isStarblight(level, pos);
        BlockState cover = state.getBlock().defaultBlockState();
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (!level.getBlockState(target).is(ModBlocks.AURORIAN_DIRT.get())) {
                continue;
            }
            var biome = level.getBiome(target);
            if (biome.is(SWAMP) != sourceSwamp || (starblight && biome.is(FLOWER_ISLAND))) {
                continue;
            }
            BlockPos above = target.above();
            if (canStayAlive(cover, level, target) && !level.getFluidState(above).is(FluidTags.WATER)) {
                level.setBlockAndUpdate(target, cover.setValue(SnowyBlock.SNOWY, level.getBlockState(above).is(BlockTags.SNOW)));
            }
        }
    }

    private static boolean canStayAlive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(Blocks.SNOW) && above.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (above.getFluidState().isFull()) {
            return false;
        }
        return LightEngine.getLightBlockInto(state, above, Direction.UP, above.getLightDampening()) < 15;
    }
}
