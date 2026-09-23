package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.enchantment.RoundaboutThrow;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Does not save: the persistent owner ledger is the only authority for the real item. */
public final class ReturningAxeEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(ReturningAxeEntity.class, EntityDataSerializers.ITEM_STACK);
    private boolean returning;
    private double traveled;
    private double range = 8;
    private float attackDamage = 6;

    public ReturningAxeEntity(EntityType<? extends ReturningAxeEntity> type, Level level) { super(type, level); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(ITEM, ItemStack.EMPTY); }

    public void launch(ServerPlayer owner, ItemStack stack, int range, float damage) {
        this.setOwner(owner);
        this.entityData.set(ITEM, stack.copy());
        this.range = range;
        this.attackDamage = damage;
        this.setPos(owner.getEyePosition().add(owner.getLookAngle().scale(0.45)));
        this.setDeltaMovement(owner.getLookAngle().scale(0.9));
    }

    @Override
    public ItemStack getItem() { return this.entityData.get(ITEM); }
    @Override
    public ItemStack getWeaponItem() { return getItem(); }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            if (!(getOwner() instanceof ServerPlayer player) || !player.isAlive() || player.level() != level
                    || !player.hasData(ModAttachments.RETURNING_AXE)
                    || !player.getData(ModAttachments.RETURNING_AXE).flightId.equals(getUUID().toString())) {
                discard();
                return;
            }
            if (returning) {
                Vec3 difference = player.getEyePosition().add(0, -0.4, 0).subtract(position());
                if (difference.lengthSqr() < 1.5 || tickCount > 100) {
                    RoundaboutThrow.recover(player);
                    discard();
                    return;
                }
                setDeltaMovement(difference.normalize().scale(1.1));
            } else {
                HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                if (hit.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hit)) {
                    if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity victim) {
                        var source = damageSources().thrown(this, player);
                        float damage = EnchantmentHelper.modifyDamage(level, getItem(), victim, source, attackDamage);
                        if (victim.hurtServer(level, source, damage)) EnchantmentHelper.doPostAttackEffects(level, victim, source);
                    }
                    returning = true;
                    setPos(hit.getLocation());
                }
                traveled += getDeltaMovement().length();
                if (traveled >= range) returning = true;
            }
            // Crossing into unloaded chunks never loads them. The owner's ledger restores the item.
            if (!level.hasChunkAt(net.minecraft.core.BlockPos.containing(position().add(getDeltaMovement())))) {
                RoundaboutThrow.recover(player);
                discard();
                return;
            }
        }
        setPos(position().add(getDeltaMovement()));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == getOwner() || !super.canHitEntity(entity)) return false;
        if (getOwner() instanceof ServerPlayer player) {
            if (player.isAlliedTo(entity)) return false;
            if (entity instanceof net.minecraft.world.entity.player.Player other && !player.canHarmPlayer(other)) return false;
        }
        return true;
    }
}
