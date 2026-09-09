package cn.teampancake.theaurorian2.client.sound;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.WindChimesBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.jspecify.annotations.Nullable;

/** Only active chimes are monitored, so removing one stops its long audio clip immediately. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class WindChimesSound implements TickableSoundInstance {
    private final SoundInstance original;
    private final ClientLevel level;
    private final WindChimesBlockEntity source;
    private boolean stopped;

    private WindChimesSound(SoundInstance original, ClientLevel level, WindChimesBlockEntity source) {
        this.original = original;
        this.level = level;
        this.source = source;
    }

    @SubscribeEvent
    public static void onSound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || sound instanceof WindChimesSound) return;
        Identifier id = sound.getIdentifier();
        if (!id.equals(ModSoundEvents.AMETHYST_WIND_CHIMES.getId())
                && !id.equals(ModSoundEvents.BAMBOO_WIND_CHIMES.getId())) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(BlockPos.containing(sound.getX(), sound.getY(), sound.getZ()))
                instanceof WindChimesBlockEntity source) {
            event.setSound(new WindChimesSound(sound, level, source));
        }
    }

    @Override public void tick() {
        stopped = Minecraft.getInstance().level != level || source.isRemoved()
                || !level.hasChunkAt(source.getBlockPos()) || level.getBlockEntity(source.getBlockPos()) != source;
    }
    @Override public boolean isStopped() { return stopped; }
    @Override public Identifier getIdentifier() { return original.getIdentifier(); }
    @Override public @Nullable WeighedSoundEvents resolve(SoundManager manager) { return original.resolve(manager); }
    @Override public @Nullable Sound getSound() { return original.getSound(); }
    @Override public SoundSource getSource() { return original.getSource(); }
    @Override public boolean isLooping() { return original.isLooping(); }
    @Override public boolean isRelative() { return original.isRelative(); }
    @Override public int getDelay() { return original.getDelay(); }
    @Override public float getVolume() { return original.getVolume(); }
    @Override public float getPitch() { return original.getPitch(); }
    @Override public double getX() { return original.getX(); }
    @Override public double getY() { return original.getY(); }
    @Override public double getZ() { return original.getZ(); }
    @Override public Attenuation getAttenuation() { return original.getAttenuation(); }
}
