package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class AurorianGrassBlock extends SpreadingSnowyBlock implements BonemealableBlock {

    public static final MapCodec<AurorianGrassBlock> CODEC = simpleCodec(AurorianGrassBlock::new);
    private static final ResourceKey<Block> AURORIAN_DIRT = ResourceKey.create(
            Registries.BLOCK, TheAurorian2.id("aurorian_dirt"));

    public AurorianGrassBlock(BlockBehaviour.Properties properties) {
        super(properties, AURORIAN_DIRT);
    }

    @Override
    protected MapCodec<? extends SpreadingSnowyBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir() && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        BlockState shortGrass = state.is(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get())
                ? ModBlocks.AURORIAN_GRASS_LIGHT.get().defaultBlockState()
                : ModBlocks.AURORIAN_GRASS.get().defaultBlockState();

        placementAttempts:
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos testPos = above;

            for (int step = 0; step < attempt / 16; step++) {
                testPos = testPos.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);
                if (!level.getBlockState(testPos.below()).is(this)
                        || level.getBlockState(testPos).isCollisionShapeFullBlock(level, testPos)) {
                    continue placementAttempts;
                }
            }

            BlockState testState = level.getBlockState(testPos);
            if (testState.is(shortGrass.getBlock()) && random.nextInt(10) == 0) {
                BonemealableBlock grass = (BonemealableBlock) shortGrass.getBlock();
                if (grass.isValidBonemealTarget(level, testPos, testState)) {
                    grass.performBonemeal(level, random, testPos, testState);
                }
            }

            if (testState.isAir() && !level.isOutsideBuildHeight(testPos)) {
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> flowers = level.getBiome(testPos)
                            .value()
                            .getGenerationSettings()
                            .getBoneMealFeatures();
                    if (!flowers.isEmpty()) {
                        Util.getRandom(flowers, random)
                                .place(level, level.getChunkSource().getGenerator(), random, testPos);
                    }
                } else if (shortGrass.canSurvive(level, testPos)) {
                    level.setBlock(testPos, shortGrass, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}
