package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.WorldScrollTeleportEntity;
import cn.teampancake.theaurorian2.common.registry.ModSoundEvents;
import cn.teampancake.theaurorian2.common.network.WorldScrollReadyPayload;
import cn.teampancake.theaurorian2.mixin.ClientInputAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class WorldScrollTeleportClient {
    public static final ContextKey<Float> PLAYER_WIDTH = new ContextKey<>(TheAurorian2.id("teleport_player_width"));

    @SubscribeEvent public static void registerRenderData(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                WorldScrollTeleportEntity effect = avatar instanceof Player player ? WorldScrollTeleportEntity.active(player) : null;
                state.setRenderData(PLAYER_WIDTH, effect == null ? 1F : effect.playerWidth(state.partialTick));
            }
        });
    }

    @SubscribeEvent public static void hideCollapsedAvatar(RenderPlayerEvent.Pre<?> event) {
        Float width = event.getRenderState().getRenderData(PLAYER_WIDTH);
        if (width != null && width <= 0) event.setCanceled(true);
    }

    @SubscribeEvent public static void arrivalInput(MovementInputUpdateEvent event) {
        WorldScrollTeleportEntity effect = WorldScrollTeleportEntity.active(event.getEntity());
        if (effect == null || !effect.isMaterializing()) return;
        event.getInput().keyPresses = Input.EMPTY;
        ((ClientInputAccessor) event.getInput()).theaurorian2$setMoveVector(Vec2.ZERO);
    }

    @SubscribeEvent public static void playEffectSound(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof WorldScrollTeleportEntity effect) || !effect.level().isClientSide()) return;
        Minecraft mc = Minecraft.getInstance();
        if (effect.isWaitingForView() && mc.player != null && mc.screen == null && mc.getOverlay() == null
                && mc.player.connection.hasClientLoaded() && WorldScrollTeleportEntity.active(mc.player) == effect
                && effect.markReadySent()) {
            ClientPacketDistributor.sendToServer(new WorldScrollReadyPayload(effect.getId()));
        }
        if (effect.startSoundOnce()) mc.getSoundManager().play(new TeleportSound(effect));
    }

    private static final class TeleportSound extends AbstractTickableSoundInstance {
        // Client-only cue selection: both sounds are tied to the existing VFX entity,
        // so arrival needs neither a new synchronized sound event nor another packet.
        private static final SoundEvent ARRIVAL_SOUND = SoundEvent.createVariableRangeEvent(
                TheAurorian2.id("world_scroll_teleport_arrival"));
        private final WorldScrollTeleportEntity effect;

        private TeleportSound(WorldScrollTeleportEntity effect) {
            super(effect.isArrival() ? ARRIVAL_SOUND : ModSoundEvents.WORLD_SCROLL_TELEPORT.get(),
                    SoundSource.PLAYERS, RandomSource.create());
            this.effect = effect;
            tick();
        }

        @Override public boolean canPlaySound() { return !isStopped(); }

        @Override public void tick() {
            float animationTime = effect.animationSeconds(0);
            float opacity = effect.opacity(0);
            if (effect.isRemoved() || Minecraft.getInstance().level != effect.level()
                    || animationTime >= 5F || opacity <= 0F) {
                stop();
                return;
            }
            x = effect.getX();
            y = effect.getY();
            z = effect.getZ();
            // The beam vanishes at source time 3.1; only fading fragments remain
            // until 5.0. Follow that timeline, even if an entity removal arrives late.
            float tail = Mth.clamp((5F - animationTime) / 1.9F, 0F, 1F);
            volume = 0.8F * opacity * tail * tail;
        }
    }
}
