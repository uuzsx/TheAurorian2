package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.OracleSpellLights;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge exposes no API for transient entity light sources. Add emission only
 * at registered client spell cells, preserving vanilla propagation and other light. */
@Mixin(BlockLightEngine.class)
public abstract class OracleSpellLightMixin {
    @Unique private ClientLevel theaurorian2$spellLevel;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;Lnet/minecraft/world/level/lighting/BlockLightSectionStorage;)V", at = @At("RETURN"))
    private void theaurorian2$rememberClientLevel(LightChunkGetter source, BlockLightSectionStorage storage, CallbackInfo ci) {
        if (source.getLevel() instanceof ClientLevel client) theaurorian2$spellLevel = client;
    }

    @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true)
    private void theaurorian2$spellLight(long position, BlockState state, CallbackInfoReturnable<Integer> result) {
        if (theaurorian2$spellLevel == null) return;
        int light = OracleSpellLights.emission(theaurorian2$spellLevel, position);
        if (light > result.getReturnValueI()) result.setReturnValue(light);
    }
}
