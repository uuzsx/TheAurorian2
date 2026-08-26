package cn.teampancake.theaurorian2.common.world;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import cn.teampancake.theaurorian2.common.inventory.AccessoryEffects;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class MoonShieldSystem {

    public static final float MAX_SHIELD = MoonShieldData.MAX_SHIELD;
    private static final float NORMAL_RECOVERY = 1.0F;
    private static final float MOON_NIGHT_RECOVERY = 2.0F;
    private static final int HIT_RECOVERY_DELAY = 100;
    private static final int BREAK_RECOVERY_DELAY = 100;
    private static final int CRIMSON_RECOVERY_DELAY = 20;
    public static final int BREAK_TRANSITION_FRAME_TICKS = 2;
    public static final int BREAK_TRANSITION_FRAME_COUNT = 4;
    public static final int BREAK_TRANSITION_TICKS =
            BREAK_TRANSITION_FRAME_TICKS * BREAK_TRANSITION_FRAME_COUNT;
    private static final long DAY_TICKS = 24_000L;
    private static final long MOON_NIGHT_START = 12_000L;
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> BOSSES =
            TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "bosses"));

    private MoonShieldSystem() {
    }

    public static boolean isPurified(Player player) {
        return player.getData(ModAttachments.MOON_SHIELD).purified();
    }

    public static boolean isCrimson(Player player) {
        MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
        return data.purified() && data.crimson();
    }

    public static boolean purify(ServerPlayer player) {
        if (isPurified(player)) {
            return false;
        }

        player.setData(ModAttachments.MOON_SHIELD, MoonShieldData.active());
        player.setData(ModAttachments.MOON_SHIELD_RECOVERY_AT, 0L);
        AurorianBlessingEffects.reconcilePlayer(player);
        AccessoryEffects.reconcile(player, player.getData(ModAttachments.ACCESSORY_INVENTORY));
        playPurificationEffects(player);
        return true;
    }

    public static void reconcileMode(ServerPlayer player, boolean crimson, int crimsonLevel) {
        MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
        if (!data.purified()) {
            return;
        }

        int level = crimson ? Math.clamp(crimsonLevel, 0, MoonShieldData.MAX_CRIMSON_LEVEL) : 0;
        MoonShieldData updated = data.withMode(crimson, level);
        if (!crimson && updated.shield() > MoonShieldData.MAX_SHIELD) {
            updated = updated.withShield(MoonShieldData.MAX_SHIELD);
        }
        if (!updated.equals(data)) {
            player.setData(ModAttachments.MOON_SHIELD, updated);
            if (!data.crimson() && updated.crimson() && updated.shield() > 0.0F) {
                player.setData(
                        ModAttachments.MOON_SHIELD_RECOVERY_AT,
                        player.level().getGameTime() + CRIMSON_RECOVERY_DELAY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getNewDamage() <= 0.0F
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
        if (!data.purified()) {
            return;
        }

        if (data.crimson()) {
            return;
        }

        float shieldBefore = data.shield();
        long gameTime = player.level().getGameTime();
        if (shieldBefore <= 0.0F) {
            long recoveryAt = Math.max(
                    player.getData(ModAttachments.MOON_SHIELD_RECOVERY_AT),
                    gameTime + HIT_RECOVERY_DELAY);
            player.setData(ModAttachments.MOON_SHIELD_RECOVERY_AT, recoveryAt);
            return;
        }

        float shieldAfter = Math.max(0.0F, shieldBefore - event.getNewDamage());
        long recoveryDelay = shieldAfter <= 0.0F
                ? moonShieldBreakRecoveryDelay(player) + BREAK_TRANSITION_TICKS * 2L
                : HIT_RECOVERY_DELAY;
        player.setData(
                ModAttachments.MOON_SHIELD_RECOVERY_AT,
                gameTime + recoveryDelay);

        player.setData(ModAttachments.MOON_SHIELD, data.withShield(shieldAfter));
        event.setNewDamage(Math.max(0.0F, event.getNewDamage() - shieldBefore));
        if (shieldAfter <= 0.0F) {
            playBreakEffects(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
            if (data.purified() && data.crimson()) {
                player.setData(
                        ModAttachments.MOON_SHIELD_RECOVERY_AT,
                        player.level().getGameTime() + CRIMSON_RECOVERY_DELAY);
            }
        }

        if (!isValidCrimsonAttackTarget(event.getEntity())) {
            return;
        }

        ServerPlayer attacker = creditedPlayer(event.getSource());
        if (attacker == null || !isCrimson(attacker)) {
            return;
        }

        MoonShieldData data = attacker.getData(ModAttachments.MOON_SHIELD);
        if (data.shield() >= data.maxShield()) {
            return;
        }

        int chance = AccessoryEffects.gloomyPauldronsCrimsonChance(attacker);
        if (chance <= 0 || attacker.getRandom().nextInt(100) >= chance) {
            return;
        }

        attacker.setData(
                ModAttachments.MOON_SHIELD,
                data.withShield(Math.min(data.maxShield(), data.shield() + 1.0F)));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player
                || victim instanceof SpiderMotherEntity
                || victim.getType().builtInRegistryHolder().is(BOSSES)) {
            return;
        }

        ServerPlayer player = creditedPlayer(event.getSource());
        if (player == null || !isCrimson(player)) {
            return;
        }

        MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
        float reward = Math.max(0.0F, victim.getMaxHealth() * 0.1F);
        if (reward <= 0.0F || data.shield() >= data.maxShield()) {
            return;
        }
        player.setData(
                ModAttachments.MOON_SHIELD,
                data.withShield(Math.min(data.maxShield(), data.shield() + reward)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MoonShieldData data = player.getData(ModAttachments.MOON_SHIELD);
        if (!data.purified()) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (data.crimson()) {
            if (gameTime < player.getData(ModAttachments.MOON_SHIELD_RECOVERY_AT)
                    || data.shield() <= 0.0F
                    || player.getHealth() >= player.getMaxHealth()) {
                return;
            }
            float recovery = crimsonRecovery(data.crimsonLevel());
            float healthMissing = player.getMaxHealth() - player.getHealth();
            float amount = Math.min(recovery, Math.min(data.shield(), healthMissing));
            if (amount <= 0.0F) {
                return;
            }
            float healthBefore = player.getHealth();
            player.heal(amount);
            float healed = Math.max(0.0F, player.getHealth() - healthBefore);
            if (healed > 0.0F) {
                player.setData(
                        ModAttachments.MOON_SHIELD,
                        data.withShield(data.shield() - healed));
                player.setData(ModAttachments.MOON_SHIELD_RECOVERY_AT, gameTime + 20L);
            }
            return;
        }

        long recoveryAt = player.getData(ModAttachments.MOON_SHIELD_RECOVERY_AT);
        if (data.shield() >= MAX_SHIELD
                || gameTime < recoveryAt
                || Math.floorMod(gameTime - recoveryAt, 20L) != 0L) {
            return;
        }

        float recovery = moonShieldRecovery(serverPlayer, isAurorianMoonNight(player));
        player.setData(ModAttachments.MOON_SHIELD, data.withShield(data.shield() + recovery));
    }

    public static int moonShieldBreakRecoveryDelay(ServerPlayer player) {
        int reductionPercent = Math.clamp(AccessoryEffects.gloomyPauldronsMoonPercent(player), 0, 100);
        return Math.max(0, Math.round(BREAK_RECOVERY_DELAY * (1.0F - reductionPercent / 100.0F)));
    }

    public static float moonShieldRecovery(ServerPlayer player, boolean moonNight) {
        float base = moonNight ? MOON_NIGHT_RECOVERY : NORMAL_RECOVERY;
        int percent = Math.max(0, AccessoryEffects.gloomyPauldronsMoonPercent(player));
        return base * (1.0F + percent / 100.0F);
    }

    public static float crimsonRecovery(int level) {
        return 1.0F + Math.clamp(level, 0, MoonShieldData.MAX_CRIMSON_LEVEL) * 0.5F;
    }

    private static ServerPlayer creditedPlayer(net.minecraft.world.damagesource.DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer player) {
            return player;
        }
        if (attacker instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof ServerPlayer player) {
            return player;
        }
        if (direct instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    private static boolean isValidCrimsonAttackTarget(LivingEntity target) {
        return !(target instanceof Player);
    }

    private static boolean isAurorianMoonNight(Player player) {
        return player.level().dimensionTypeRegistration().is(TheAurorian2.AURORIAN_DIMENSION_TYPE)
                && Math.floorMod(player.level().getDefaultClockTime(), DAY_TICKS) >= MOON_NIGHT_START;
    }

    private static void playPurificationEffects(ServerPlayer player) {
        ServerLevel level = player.level();
        level.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                36,
                0.55D,
                0.9D,
                0.55D,
                0.035D);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                1.0F,
                1.35F);
    }

    private static void playBreakEffects(ServerPlayer player) {
        ServerLevel level = player.level();
        level.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                24,
                0.55D,
                0.8D,
                0.55D,
                0.08D);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.9F,
                1.15F);
    }
}
