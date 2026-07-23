package cn.teampancake.theaurorian2.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ColoredParticleLeavesBlock extends TintedParticleLeavesBlock {

    private final int fallingLeafColor;

    public ColoredParticleLeavesBlock(
            float leafParticleChance, int fallingLeafColor, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
        this.fallingLeafColor = fallingLeafColor;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ColorParticleOption particle = ColorParticleOption.create(
                ParticleTypes.TINTED_LEAVES, this.fallingLeafColor);
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }
}
