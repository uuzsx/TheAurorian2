package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.client.sound.PurificationRitualMusic;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    // MusicManager has no public pause API; skipping its selection tick prevents a second world track.
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void theaurorian2$suspendWorldMusicForRitual(CallbackInfo ci) {
        if (PurificationRitualMusic.suppressesBackgroundMusic()) {
            ci.cancel();
        }
    }
}
