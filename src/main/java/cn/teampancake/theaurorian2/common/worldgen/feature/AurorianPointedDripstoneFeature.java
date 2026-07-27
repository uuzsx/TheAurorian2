package cn.teampancake.theaurorian2.common.worldgen.feature;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.PointedDripstoneFeature;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public final class AurorianPointedDripstoneFeature extends PointedDripstoneFeature {

    public AurorianPointedDripstoneFeature() {
        super(PointedDripstoneConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> context) {
        boolean placed = super.place(context);
        if (placed) {
            AurorianDripstoneFeatureSupport.replaceInBox(context.level(), context.origin(), 4, 5);
        }
        return placed;
    }
}
