package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PhantomBlossomRequiemItem extends Item {

    private static final int MAX_USE_DURATION = 72_000;
    private static final int BLOOM_CHARGE_TICKS = 30;
    private static final int SENDOFF_COOLDOWN_TICKS = 10;
    private static final int BLOOM_COOLDOWN_TICKS = 400;
    private static final int MAX_SENDOFF_TARGETS = 32;
    private static final double SENDOFF_RANGE = 12.0;
    private static final double CONE_DOT_THRESHOLD = 0.8191520443;
    private static final float SENDOFF_DAMAGE = 8.0F;

    public PhantomBlossomRequiemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return MAX_USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel) || !(livingEntity instanceof Player player)) {
            return;
        }

        int usedTicks = this.getUseDuration(stack, player) - ticksRemaining;
        if (usedTicks > 0 && usedTicks % 2 == 0) {
            spawnChargingSwarm(serverLevel, player, usedTicks);
        }
        if (usedTicks == BLOOM_CHARGE_TICKS) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.65F);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.sendoff")
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.bloom")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.bloom_damage")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.mark")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.empty());
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.developer")
                .withStyle(ChatFormatting.GOLD));
        builder.accept(Component.empty());
        builder.accept(Component.translatable("item.theaurorian2.phantom_blossom_requiem.quote")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        int usedTicks = this.getUseDuration(stack, player) - remainingTime;
        if (usedTicks >= BLOOM_CHARGE_TICKS) {
            PhantomBlossomEvents.startBloomWave(serverLevel, player);
            player.getCooldowns().addCooldown(stack, BLOOM_COOLDOWN_TICKS);
        }
        return true;
    }

    public void tryCastSendoff(ServerPlayer player, ItemStack stack) {
        if (stack.getItem() != this) {
            return;
        }

        ServerLevel level = player.level();
        long gameTime = level.getGameTime();
        if (gameTime < player.getData(ModAttachments.PHANTOM_BLOSSOM_SENDOFF_READY_AT)) {
            return;
        }

        player.setData(
                ModAttachments.PHANTOM_BLOSSOM_SENDOFF_READY_AT,
                gameTime + SENDOFF_COOLDOWN_TICKS);
        castSendoff(level, player);
    }

    private static void castSendoff(ServerLevel level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        List<LivingEntity> targets = collectTargets(level, player, SENDOFF_RANGE).stream()
                .filter(target -> isInCone(eyePosition, look, target))
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .limit(MAX_SENDOFF_TARGETS)
                .toList();

        for (LivingEntity target : targets) {
            if (target.hurtServer(level, level.damageSources().playerAttack(player), SENDOFF_DAMAGE)) {
                spawnTargetButterflies(level, target);
            }
        }

        Vec3 origin = eyePosition.add(look.scale(0.9));
        for (int i = 0; i < 18; i++) {
            double angle = i * Math.PI * 2.0 / 18.0;
            Vec3 velocity = look.scale(0.14 + i % 3 * 0.025)
                    .add(Math.cos(angle) * 0.035, Math.sin(angle) * 0.025, Math.sin(angle) * 0.035);
            sendExactParticle(level,
                    i % 3 == 0
                            ? ModParticles.PHANTOM_BUTTERFLY_PINK.get()
                            : ModParticles.PHANTOM_BUTTERFLY_BLUE.get(),
                    origin, velocity);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.PLAYERS, 1.0F, 1.25F);
    }

    private static void spawnChargingSwarm(ServerLevel level, Player player, int usedTicks) {
        Vec3 facing = horizontalDirection(player);
        Vec3 right = new Vec3(-facing.z(), 0.0, facing.x());
        double charge = Math.min(1.0, usedTicks / (double) BLOOM_CHARGE_TICKS);
        int count = 2 + (int) Math.floor(charge * 3.0);
        var random = level.getRandom();

        for (int i = 0; i < count; i++) {
            double depth = 0.8 + random.nextDouble() * 2.4;
            double lateral = (random.nextDouble() - 0.5) * 3.2;
            double height = 0.3 + random.nextDouble() * 2.2;
            Vec3 position = player.position()
                    .subtract(facing.scale(depth))
                    .add(right.scale(lateral))
                    .add(0.0, height, 0.0);

            double lateralSpeed = (random.nextDouble() - 0.5) * 0.045;
            double backwardSpeed = 0.004 + random.nextDouble() * 0.012;
            double verticalSpeed = (random.nextDouble() - 0.35) * 0.028;
            Vec3 velocity = right.scale(lateralSpeed)
                    .subtract(facing.scale(backwardSpeed))
                    .add(0.0, verticalSpeed, 0.0);
            sendExactParticle(level,
                    random.nextFloat() < 0.38F
                            ? ModParticles.PHANTOM_BUTTERFLY_PINK.get()
                            : ModParticles.PHANTOM_BUTTERFLY_BLUE.get(),
                    position, velocity);
        }
    }

    static void spawnTargetButterflies(ServerLevel level, LivingEntity target) {
        Vec3 center = target.getBoundingBox().getCenter();
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2.0 / 12.0;
            double speed = 0.035 + (i % 3) * 0.012;
            Vec3 velocity = new Vec3(
                    Math.cos(angle) * speed,
                    0.035 + (i % 4) * 0.014,
                    Math.sin(angle) * speed);
            sendExactParticle(level,
                    i % 3 == 0
                            ? ModParticles.PHANTOM_BUTTERFLY_PINK.get()
                            : ModParticles.PHANTOM_BUTTERFLY_BLUE.get(),
                    center.add(0.0, (i % 3 - 1) * 0.16, 0.0), velocity);
        }
    }

    static void spawnExpansionWave(ServerLevel level, Vec3 center, double radius, int age) {
        double visualRadius = Math.min(radius, 11.7);
        double phase = age * 0.19;
        int ringCount = 12;
        for (int i = 0; i < ringCount; i++) {
            double angle = i * Math.PI * 2.0 / ringCount + phase;
            Vec3 radial = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 tangent = new Vec3(-Math.sin(angle), 0.0, Math.cos(angle));
            double ribbon = Math.sin(angle * 5.0 - age * 0.48);
            double y = center.y() - 0.45 + (i % 3) * 0.46 + ribbon * 0.24;
            Vec3 position = new Vec3(
                    center.x() + radial.x() * (visualRadius + ribbon * 0.12),
                    y,
                    center.z() + radial.z() * (visualRadius + ribbon * 0.12));
            Vec3 velocity = radial.scale(0.018)
                    .add(tangent.scale(0.025 + 0.012 * Math.cos(angle * 3.0 + phase)))
                    .add(0.0, 0.012 + (i % 2) * 0.008, 0.0);
            sendExactParticle(level,
                    (i + age) % 4 == 0
                            ? ModParticles.PHANTOM_BUTTERFLY_PINK.get()
                            : ModParticles.PHANTOM_BUTTERFLY_BLUE.get(),
                    position, velocity);

            if (i % 4 == 0) {
                double trailRadius = Math.max(0.1, visualRadius - 0.55 - (i % 2) * 0.34);
                Vec3 trailPosition = new Vec3(
                        center.x() + radial.x() * trailRadius,
                        y + 0.28,
                        center.z() + radial.z() * trailRadius);
                sendExactParticle(level,
                        i % 2 == 0
                                ? ModParticles.PHANTOM_BUTTERFLY_PINK.get()
                                : ModParticles.PHANTOM_BUTTERFLY_BLUE.get(),
                        trailPosition, tangent.scale(-0.02).add(0.0, 0.016, 0.0));
            }
        }
    }

    static void spawnCherryDeath(ServerLevel level, LivingEntity target) {
        Vec3 center = target.getBoundingBox().getCenter();
        int count = 42;
        double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < count; i++) {
            double t = (i + 0.5) / count;
            double y = 1.0 - 2.0 * t;
            double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double angle = i * goldenAngle;
            Vec3 direction = new Vec3(
                    Math.cos(angle) * horizontal,
                    y,
                    Math.sin(angle) * horizontal);
            double speed = 0.055 + (i % 5) * 0.011;
            sendExactParticle(level, ModParticles.PHANTOM_PETAL.get(),
                    center.add(direction.scale(0.22)),
                    direction.scale(speed).add(0.0, 0.045, 0.0));
        }
    }

    static void sendExactParticle(
            ServerLevel level, ParticleOptions particle, Vec3 position, Vec3 velocity) {
        level.sendParticles(particle,
                position.x(), position.y(), position.z(), 0,
                velocity.x(), velocity.y(), velocity.z(), 1.0);
    }

    static boolean isValidTarget(Player player, LivingEntity target) {
        return target != player
                && target.isAlive()
                && !target.isSpectator()
                && !player.isAlliedTo(target);
    }

    private static List<LivingEntity> collectTargets(ServerLevel level, Player player, double range) {
        AABB bounds = player.getBoundingBox().inflate(range);
        double rangeSquared = range * range;
        return level.getEntitiesOfClass(LivingEntity.class, bounds, target -> isValidTarget(player, target)
                && player.distanceToSqr(target) <= rangeSquared
                && player.hasLineOfSight(target));
    }

    private static boolean isInCone(Vec3 origin, Vec3 look, LivingEntity target) {
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
        return toTarget.lengthSqr() > 1.0E-6 && look.dot(toTarget.normalize()) >= CONE_DOT_THRESHOLD;
    }

    private static Vec3 horizontalDirection(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x(), 0.0, look.z());
        if (horizontal.lengthSqr() > 1.0E-6) {
            return horizontal.normalize();
        }
        double yaw = Math.toRadians(player.getYRot());
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }
}
