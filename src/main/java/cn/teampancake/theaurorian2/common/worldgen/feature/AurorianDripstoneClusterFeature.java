package cn.teampancake.theaurorian2.common.worldgen.feature;

import net.minecraft.world.level.levelgen.feature.DripstoneClusterFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public final class AurorianDripstoneClusterFeature extends DripstoneClusterFeature {

    public AurorianDripstoneClusterFeature() {
        super(DripstoneClusterConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> context) {
        boolean placed = super.place(context);
        if (placed) {
            AurorianDripstoneFeatureSupport.replaceInBox(context.level(), context.origin(), 12, 18);
        }
        return placed;
    }
}
