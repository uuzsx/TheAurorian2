package cn.teampancake.theaurorian2.client.sound;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

/** Client-only owner for the one-shot ritual track and its vanilla music suppression. */
public final class PurificationRitualMusic {

    private static final Identifier SOUND_ID = TheAurorian2.id("purification_ritual");
    private static @Nullable SimpleSoundInstance currentMusic;
    private static @Nullable ResourceKey<Level> originDimension;
    private static boolean pausedForGamePause;

    private PurificationRitualMusic() {
    }

    public static void handle(boolean playing) {
        if (playing) {
            start();
        } else {
            stop();
        }
    }

    public static boolean suppressesBackgroundMusic() {
        return currentMusic != null && isInOriginDimension();
    }

    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (currentMusic == null) {
            return;
        }
        if (!isInOriginDimension()) {
            stop();
            return;
        }

        boolean gamePaused = minecraft.isPaused();
        if (gamePaused == pausedForGamePause) {
            return;
        }
        if (gamePaused) {
            // Vanilla leaves MUSIC running on the pause screen; include the ritual track in the pause.
            minecraft.getSoundManager().pauseAllExcept(SoundSource.UI);
        } else {
            minecraft.getSoundManager().resume();
        }
        pausedForGamePause = gamePaused;
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stop();
    }

    private static void start() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        stop();
        originDimension = minecraft.level.dimension();
        minecraft.getMusicManager().stopPlaying();
        currentMusic = new SimpleSoundInstance(
                SOUND_ID,
                SoundSource.MUSIC,
                1.0F,
                1.0F,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true);
        SoundEngine.PlayResult result = minecraft.getSoundManager().play(currentMusic);
        if (result == SoundEngine.PlayResult.NOT_STARTED) {
            TheAurorian2.LOGGER.warn("Unable to start purification ritual music: {}", SOUND_ID);
            currentMusic = null;
            originDimension = null;
            pausedForGamePause = false;
        } else if (minecraft.isPaused()) {
            minecraft.getSoundManager().pauseAllExcept(SoundSource.UI);
            pausedForGamePause = true;
        }
    }

    private static void stop() {
        if (currentMusic != null) {
            Minecraft.getInstance().getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
        originDimension = null;
        pausedForGamePause = false;
    }

    private static boolean isInOriginDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        return originDimension != null
                && minecraft.level != null
                && originDimension.equals(minecraft.level.dimension());
    }
}
