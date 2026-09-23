package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.registry.ModBiomeTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceSystem.class)
public abstract class AurorianFrozenOceanSurfaceMixin {
    // Vanilla hardcodes its two frozen-ocean IDs here; no surface-rule or event
    // can enable this iceberg/ice-shelf pass for a custom biome. Extend only those
    // ID checks, retaining the original algorithm and all other biome checks.
    @WrapOperation(method = "buildSurface", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z"))
    private boolean theaurorian2$frozenOceanSurface(Holder<Biome> biome, ResourceKey<Biome> key,
                                                   Operation<Boolean> original) {
        return original.call(biome, key)
                || ((key.equals(Biomes.FROZEN_OCEAN) || key.equals(Biomes.DEEP_FROZEN_OCEAN))
                && biome.is(ModBiomeTags.HAS_FROZEN_OCEAN_SURFACE));
    }
}
