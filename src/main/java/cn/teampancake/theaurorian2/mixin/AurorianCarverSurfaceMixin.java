package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModBlockTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldCarver.class)
public abstract class AurorianCarverSurfaceMixin {
    // Replaceable tags allow carving, but vanilla's exposed-soil repair tests
    // literal grass/dirt blocks. Extend only these tests for our materials so
    // the native surface rule restores the appropriate grass at cave openings.
    @WrapOperation(method = "carveBlock", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean theaurorian2$exposedSoil(BlockState state, Object block, Operation<Boolean> original) {
        return original.call(state, block)
                || (block == Blocks.GRASS_BLOCK && state.is(ModBlockTags.AURORIAN_GRASS_BLOCKS))
                || (block == Blocks.DIRT && state.is(ModBlocks.AURORIAN_DIRT.get()));
    }
}
