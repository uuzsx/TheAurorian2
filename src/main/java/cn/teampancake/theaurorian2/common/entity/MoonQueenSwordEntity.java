package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Save migration only: removes swords left by the discarded first-phase experiment.
 * Retaining the published ID lets old saves load; nothing creates this entity anymore. */
public final class MoonQueenSwordEntity extends Entity {
    public MoonQueenSwordEntity(EntityType<? extends MoonQueenSwordEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    @Override public void tick() { super.tick(); if (!level().isClientSide()) discard(); }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }
    @Override protected void addAdditionalSaveData(ValueOutput output) { }
    @Override protected void readAdditionalSaveData(ValueInput input) { }
}