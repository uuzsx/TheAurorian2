package cn.teampancake.theaurorian2.common.entity;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class DamageNumberEntity extends Entity {

    private static final int LIFETIME_TICKS = 16;
    private static final double RISE_PER_TICK = 0.035;

    public DamageNumberEntity(EntityType<? extends DamageNumberEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setDamage(float damage) {
        this.setCustomName(Component.literal(formatDamage(damage)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        this.setCustomNameVisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= LIFETIME_TICKS) {
            this.discard();
            return;
        }

        this.setPos(this.getX(), this.getY() + RISE_PER_TICK, this.getZ());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    public static String formatDamage(float damage) {
        if (damage >= 10_000.0F) {
            return String.format(Locale.ROOT, "%.2e", damage);
        }

        int rounded = Math.round(damage);
        if (Math.abs(damage - rounded) < 0.01F) {
            return Integer.toString(rounded);
        }

        return String.format(Locale.ROOT, "%.1f", damage);
    }
}
