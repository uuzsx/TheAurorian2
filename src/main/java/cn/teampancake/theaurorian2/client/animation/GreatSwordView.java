package cn.teampancake.theaurorian2.client.animation;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Local presentation only. Vanilla attacks, damage, reach and server inventory remain authoritative.
 * Inspect consumes an otherwise unused right click; interactable blocks still get first refusal.
 */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class GreatSwordView {
    private static GreatSwordViewModel.Model model;
    private static float[][] previous;
    private static String clip = "idle";
    private static double started, nextInspect, lastSwing = -100;
    private static float speed = 1, lastCueTime = -1, oldAttack;
    private static int combo, slot = -1;
    private static Object level;
    private static boolean active;

    private GreatSwordView() {}

    public static void reset() {
        active = false;
        model = null;
        previous = null;
        level = null;
        slot = -1;
        combo = 0;
        nextInspect = 0;
        lastSwing = -100;
        oldAttack = 0;
    }

    private static boolean holding() {
        var player = Minecraft.getInstance().player;
        return player != null && player.isAlive() && !player.isSpectator()
                && player.getMainHandItem().is(ModLegacyItems.MOONSILVER_GREAT_SWORD.get());
    }

    private static double now() {
        var mc = Minecraft.getInstance();
        return mc.level == null ? 0 : mc.level.getGameTime();
    }

    private static boolean ensureActive() {
        var mc = Minecraft.getInstance();
        if (!holding() || GreatSwordViewModel.get() == null) return false;
        if (model != GreatSwordViewModel.get() || level != mc.level) reset();
        if (!active || slot != mc.player.getInventory().getSelectedSlot()) {
            model = GreatSwordViewModel.get();
            previous = new float[model.pose.length][9];
            model.sample("equip", 0, model.pose);
            clip = "equip";
            started = now();
            speed = .5F; // The original pack plays its 0.5-second equip clip at half speed.
            copyPose();
            level = mc.level;
            slot = mc.player.getInventory().getSelectedSlot();
            active = true;
            lastCueTime = -1;
            oldAttack = mc.player.getAttackAnim(0);
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ensureActive()) {
            if (active) reset();
            return;
        }
        var mc = Minecraft.getInstance();
        float attack = mc.player.getAttackAnim(0);
        // Also cover vanilla's repeated block-breaking swings, without changing their behavior.
        if (mc.screen == null && mc.player.swingingArm == InteractionHand.MAIN_HAND
                && attack > 0 && (oldAttack == 0 || attack < oldAttack) && now() - lastSwing >= 6) swing();
        oldAttack = attack;
        float elapsed = (float) ((now() - started) / 20 * speed);
        var animation = model.data.animations().get(clip);
        for (var cue : animation.sounds()) {
            if (cue.time() > lastCueTime && cue.time() <= elapsed) {
                sound(cue.sound().equals("swing") ? SoundEvents.BREEZE_CHARGE : SoundEvents.AMETHYST_BLOCK_RESONATE,
                        .5F, cue.sound().equals("swing") ? .65F : .8F);
            }
        }
        lastCueTime = elapsed;
        boolean looping = clip.equals("idle") || clip.equals("walk");
        if (looping || elapsed >= animation.length()) {
            String movement = mc.player.onGround() && mc.player.getDeltaMovement().horizontalDistanceSqr() > .001
                    ? "walk" : "idle";
            if (!clip.equals(movement)) start(movement, 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && Minecraft.getInstance().screen == null && ensureActive()) swing();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void inspect(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || event.getEntity() != Minecraft.getInstance().player
                || event.getHand() != InteractionHand.MAIN_HAND || !ensureActive()) return;
        // This animation does not modify game state and needs no custom server packet or ticking entity.
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
        if (now() < nextInspect || clip.startsWith("swing") || clip.startsWith("emote")) return;
        nextInspect = now() + 40;
        start("emote_" + (event.getEntity().getRandom().nextInt(3) + 1), 1);
        sound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), .5F, .75F);
    }

    private static void swing() {
        if (now() - lastSwing < 2) return;
        combo = now() - lastSwing > 30 ? 1 : combo % 3 + 1;
        lastSwing = now();
        start("swing_" + combo, 1);
        sound(SoundEvents.TRIDENT_THROW.value(), .35F, .8F);
    }

    private static void sound(SoundEvent sound, float volume, float pitch) {
        var mc = Minecraft.getInstance();
        if (mc.player != null && mc.options.getCameraType().isFirstPerson())
            mc.player.playSound(sound, volume, pitch);
    }

    private static void start(String next, float rate) {
        evaluate(now());
        copyPose();
        clip = next;
        started = now();
        speed = rate;
        lastCueTime = -1;
    }

    private static void copyPose() {
        for (int i = 0; i < previous.length; i++) System.arraycopy(model.pose[i], 0, previous[i], 0, 9);
    }

    private static void evaluate(double time) {
        float seconds = (float) ((time - started) / 20 * speed);
        var animation = model.data.animations().get(clip);
        if (clip.equals("idle") || clip.equals("walk")) seconds %= animation.length();
        model.sample(clip, seconds, model.pose);
        float blend = Math.clamp((float) (time - started) / 2, 0, 1);
        blend = blend * blend * (3 - 2 * blend);
        for (int i = 0; i < model.pose.length; i++) {
            for (int j = 0; j < 9; j++) {
                float from = previous[i][j], to = model.pose[i][j];
                if (j >= 3 && j < 6) from += Math.round((to - from) / 360) * 360;
                model.pose[i][j] = from + (to - from) * blend;
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getItemStack().is(ModLegacyItems.MOONSILVER_GREAT_SWORD.get()) || !ensureActive()) return;
        // Only replace this weapon's first-person pass. GUI, ground, offhand and other players use the original model.
        event.setCanceled(true);
        double time = now() + event.getPartialTick();
        evaluate(time);
        model.render(event);
    }
}
