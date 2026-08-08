package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SpiderSilkProjectileEntity extends ThrowableItemProjectile {

    private static final int MAX_LIFETIME_TICKS = 50;
    private boolean resolved;

    public SpiderSilkProjectileEntity(EntityType<? extends SpiderSilkProjectileEntity> type, Level level) {
        super(type, level);
    }

    public static void shoot(ServerLevel level, SpiderMotherEntity owner, LivingEntity target) {
        SpiderSilkProjectileEntity silk = ModEntities.SPIDER_SILK.get().create(level, EntitySpawnReason.TRIGGERED);
        if (silk == null) {
            return;
        }

        silk.setOwner(owner);
        silk.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        Vec3 direction = target.getBoundingBox().getCenter().subtract(silk.position());
        silk.shoot(direction.x, direction.y, direction.z, 1.5F, 0.5F);
        level.addFreshEntity(silk);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            if (this.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 1, 0.02, 0.02, 0.02, 0.0);
            }
            if (this.tickCount > MAX_LIFETIME_TICKS && this.isAlive()) {
                this.resolve(false);
                this.discard();
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STRING;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity hit = hitResult.getEntity();
        Entity owner = this.getOwner();
        boolean validTarget = hit instanceof IronGolem
                || hit instanceof Player player && !player.isCreative() && !player.isSpectator();
        if (this.level() instanceof ServerLevel level
                && owner instanceof SpiderMotherEntity mother
                && hit instanceof LivingEntity living
                && validTarget
                && living.hurtServer(level, this.damageSources().thrown(this, owner), 4.0F)) {
            mother.bindWithSilk(living);
            this.resolve(true);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            this.resolve(false);
            level.sendParticles(
                    ParticleTypes.POOF,
                    hitResult.getLocation().x,
                    hitResult.getLocation().y,
                    hitResult.getLocation().z,
                    8,
                    0.2,
                    0.2,
                    0.2,
                    0.02);
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof SpiderMotherEntity)
                && !(entity instanceof AbstractSpiderlingEntity)
                && !(entity instanceof SpiderEggEntity)
                && super.canHitEntity(entity);
    }

    private void resolve(boolean hitTarget) {
        if (this.resolved) {
            return;
        }
        this.resolved = true;
        if (this.getOwner() instanceof SpiderMotherEntity mother) {
            mother.onSilkResolved(hitTarget);
        }
    }
}
