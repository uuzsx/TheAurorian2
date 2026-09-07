package cn.teampancake.theaurorian2.common.worldgen.feature;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.HugeRedMushroomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

/**
 * Uses the vanilla red mushroom shape while giving the indigo mushroom an extra
 * three to five blocks of stem height.
 */
public final class IndigoMushroomTreeFeature extends HugeRedMushroomFeature {

    public IndigoMushroomTreeFeature() {
        super(HugeMushroomFeatureConfiguration.CODEC);
    }

    @Override
    protected int getTreeHeight(RandomSource random) {
        return super.getTreeHeight(random) + 3 + random.nextInt(3);
    }
}
