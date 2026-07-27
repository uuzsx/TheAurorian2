package cn.teampancake.theaurorian2.common.worldgen.feature;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;

public final class AurorianLargeDripstoneFeature extends LargeDripstoneFeature {

    public AurorianLargeDripstoneFeature() {
        super(LargeDripstoneConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> context) {
        boolean placed = super.place(context);
        if (placed) {
            AurorianDripstoneFeatureSupport.replaceLargeDripstone(context.level(), context.origin());
        }
        return placed;
    }
}
