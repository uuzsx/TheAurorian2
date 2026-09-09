package cn.teampancake.theaurorian2.client.sound;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.GlobalPos;
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
    private static final Set<GlobalPos> activeAltars = new HashSet<>();

    private PurificationRitualMusic() {
    }

    public static void handle(GlobalPos altar, boolean playing) {
        var level = Minecraft.getInstance().level;
        if (level == null || !altar.dimension().equals(level.dimension())) return;
        if (originDimension != null && !originDimension.equals(level.dimension())) stop();
        if (playing) {
            boolean wasEmpty = activeAltars.isEmpty();
            if (activeAltars.add(altar) && wasEmpty) start();
        } else if (activeAltars.remove(altar) && activeAltars.isEmpty()) {
            stop();
        }
    }

    public static boolean suppressesBackgroundMusic() {
        return currentMusic != null && isInOriginDimension()
                && Minecraft.getInstance().getSoundManager().isActive(currentMusic);
    }

    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (originDimension != null && !isInOriginDimension()) {
            stop();
            return;
        }
        if (currentMusic == null) return;

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
        activeAltars.clear();
    }

    private static boolean isInOriginDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        return originDimension != null
                && minecraft.level != null
                && originDimension.equals(minecraft.level.dimension());
    }
}
