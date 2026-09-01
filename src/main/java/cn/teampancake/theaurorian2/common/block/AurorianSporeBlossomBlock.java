package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianSporeBlossomBlock extends SporeBlossomBlock {

    public AurorianSporeBlossomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int plantX = pos.getX();
        int plantY = pos.getY();
        int plantZ = pos.getZ();
        level.addParticle(
                ModParticles.BLUE_FALLING_SPORE_BLOSSOM.get(),
                plantX + random.nextDouble(),
                plantY + 0.7,
                plantZ + random.nextDouble(),
                0.0,
                0.0,
                0.0);
        BlockPos.MutableBlockPos ambientPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 14; i++) {
            ambientPos.set(
                    plantX + Mth.nextInt(random, -10, 10),
                    plantY - random.nextInt(10),
                    plantZ + Mth.nextInt(random, -10, 10));
            if (!level.getBlockState(ambientPos).isCollisionShapeFullBlock(level, ambientPos)) {
                level.addParticle(
                        ModParticles.BLUE_SPORE_BLOSSOM_AIR.get(),
                        ambientPos.getX() + random.nextDouble(),
                        ambientPos.getY() + random.nextDouble(),
                        ambientPos.getZ() + random.nextDouble(),
                        0.0,
                        0.0,
                        0.0);
            }
        }
    }
}
