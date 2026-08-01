package cn.teampancake.theaurorian2.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record SmallRuinConfiguration(Identifier template) implements FeatureConfiguration {

    public static final Codec<SmallRuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(SmallRuinConfiguration::template)
    ).apply(instance, SmallRuinConfiguration::new));
}
