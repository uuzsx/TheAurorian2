package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public abstract class AurorianOreVeinMixin {
    // Vanilla hardcodes vein materials without a registry/configuration hook.
    // Wrap its result only for Aurorian stone settings; retain every density,
    // random draw, height limit and raw-ore block in the native vein algorithm.
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"))
    private NoiseChunk.BlockStateFiller theaurorian2$veinMaterials(
            DensityFunction toggle, DensityFunction ridged, DensityFunction gap,
            PositionalRandomFactory random, Operation<NoiseChunk.BlockStateFiller> original,
            @Local(argsOnly = true) NoiseGeneratorSettings settings) {
        var nativeVein = original.call(toggle, ridged, gap, random);
        if (!settings.defaultBlock().is(ModBlocks.AURORIAN_STONE.get())) {
            return nativeVein;
        }
        var copper = ModBlocks.AURORIAN_COPPER_ORE.get().defaultBlockState();
        var iron = ModBlocks.EROSIVE_AURORIAN_IRON_ORE.get().defaultBlockState();
        var granite = ModBlocks.AURORIAN_GRANITE.get().defaultBlockState();
        var tuff = ModBlocks.AURORIAN_ANDESITE.get().defaultBlockState();
        return context -> {
            var state = nativeVein.calculate(context);
            if (state == null) return null;
            if (state.is(Blocks.COPPER_ORE)) return copper;
            if (state.is(Blocks.DEEPSLATE_IRON_ORE)) return iron;
            if (state.is(Blocks.GRANITE)) return granite;
            if (state.is(Blocks.TUFF)) return tuff;
            return state;
        };
    }
}
