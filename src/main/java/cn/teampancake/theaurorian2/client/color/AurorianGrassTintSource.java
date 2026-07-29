package cn.teampancake.theaurorian2.client.color;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public final class AurorianGrassTintSource implements BlockTintSource {

    public static final AurorianGrassTintSource INSTANCE = new AurorianGrassTintSource();
    public static final ColorResolver COLOR_RESOLVER = AurorianGrassTintSource::getBiomeColor;

    private AurorianGrassTintSource() {
    }

    @Override
    public int color(BlockState state) {
        return AurorianGrassColor.getDefaultColor();
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockTint(pos, COLOR_RESOLVER);
    }

    @Override
    public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return -1;
    }

    private static int getBiomeColor(Biome biome, double x, double z) {
        var grassColorOverride = biome.getModifiedSpecialEffects().grassColorOverride();
        if (grassColorOverride.isPresent()) {
            return grassColorOverride.get();
        }

        Biome.ClimateSettings climate = biome.getModifiedClimateSettings();
        return AurorianGrassColor.get(climate.temperature(), climate.downfall());
    }
}
