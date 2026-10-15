package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Grass-style growth on shared Aurorian dirt, with a closed set of surface flora. */
public final class StarblightSoilBlock extends SpreadingSnowyBlock implements BonemealableBlock {

    public static final MapCodec<StarblightSoilBlock> CODEC = simpleCodec(StarblightSoilBlock::new);
    private static final ResourceKey<Block> DIRT = ResourceKey.create(
            Registries.BLOCK, TheAurorian2.id("aurorian_dirt"));

    public StarblightSoilBlock(BlockBehaviour.Properties properties) {
        super(properties, DIRT);
    }

    @Override
    protected MapCodec<? extends SpreadingSnowyBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AurorianSoilSpread.tick(state, level, pos, random, true);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.isInsideBuildHeight(pos.above()) && level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        placementAttempts:
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos target = pos.above();
            for (int step = 0; step < attempt / 16; step++) {
                target = target.offset(random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!level.getBlockState(target.below()).is(this)
                        || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
                    continue placementAttempts;
                }
            }
            if (!level.isInsideBuildHeight(target) || !level.getBlockState(target).isAir()) {
                continue;
            }
            int choice = random.nextInt(6);
            BlockState plant = (choice < 3 ? ModBlocks.STARBLIGHT_TUFT.get()
                    : choice < 5 ? ModBlocks.STARBLIGHT_SHOOT.get()
                    : ModBlocks.STARBLIGHT_FLOWER.get()).defaultBlockState();
            if (plant.canSurvive(level, target)) {
                level.setBlock(target, plant, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}
