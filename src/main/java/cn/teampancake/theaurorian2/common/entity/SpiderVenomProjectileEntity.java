package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SpiderVenomProjectileEntity extends ThrowableItemProjectile implements GeoEntity {

    private static final int POISON_DURATION_TICKS = 6 * 20;
    private static final float WEB_PROJECTILE_SPEED = 1.35F;
    private static final float WEB_PROJECTILE_INACCURACY = 0.5F;
    private float damage = 6.0F;
    private boolean createsPool;
    private boolean createsWebOnHit;
    private boolean poolCreated;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SpiderVenomProjectileEntity(EntityType<? extends SpiderVenomProjectileEntity> type, Level level) {
        super(type, level);
    }

    public static void shoot(ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        shoot(level, owner, target, damage, false, false);
    }

    public static void shootWebbing(
            ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        shoot(level, owner, target, damage, false, true);
    }

    public static void shootWebbing(
            ServerLevel level, LivingEntity owner, Vec3 targetPosition, float damage) {
        shoot(level, owner, targetPosition, damage, false, true);
    }

    public static void shootPool(ServerLevel level, SpiderMotherEntity owner, LivingEntity target) {
        shoot(level, owner, target, 0.0F, true, false);
    }

    private static void shoot(
            ServerLevel level,
            LivingEntity owner,
            LivingEntity target,
            float damage,
            boolean createsPool,
            boolean createsWebOnHit) {
        shoot(level, owner, target.getBoundingBox().getCenter(), damage, createsPool, createsWebOnHit);
    }

    private static void shoot(
            ServerLevel level,
            LivingEntity owner,
            Vec3 targetPosition,
            float damage,
            boolean createsPool,
            boolean createsWebOnHit) {
        SpiderVenomProjectileEntity venom = ModEntities.SPIDER_VENOM.get().create(level, EntitySpawnReason.TRIGGERED);
        if (venom == null) {
            return;
        }

        venom.setOwner(owner);
        venom.damage = damage;
        venom.createsPool = createsPool;
        venom.createsWebOnHit = createsWebOnHit;
        venom.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        Vec3 direction = targetPosition.subtract(venom.position());
        float speed = createsWebOnHit ? WEB_PROJECTILE_SPEED : 1.05F;
        float inaccuracy = createsWebOnHit ? WEB_PROJECTILE_INACCURACY : 1.5F;
        venom.shoot(direction.x, direction.y, direction.z, speed, inaccuracy);
        level.addFreshEntity(venom);
    }

    @Override
    protected double getDefaultGravity() {
        return this.createsWebOnHit ? 0.01 : super.getDefaultGravity();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STRING;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.createsPool) {
            return;
        }
        Entity hit = hitResult.getEntity();
        Entity owner = this.getOwner();
        if (!(hit instanceof LivingEntity living)
                || hit instanceof SpiderMotherEntity
                || hit instanceof AbstractSpiderlingEntity
                || hit instanceof SpiderEggEntity) {
            return;
        }

        if (this.level() instanceof ServerLevel level) {
            boolean damaged = living.hurtServer(level, this.damageSources().thrown(this, owner), this.damage);
            if (damaged) {
                int poisonDuration = this.createsWebOnHit ? 4 * 20 : POISON_DURATION_TICKS;
                living.addEffect(new MobEffectInstance(ModMobEffects.EIDOLON_POISON, poisonDuration), owner);
                if (this.createsWebOnHit) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 4 * 20), owner);
                }
            }
            if (this.createsWebOnHit) {
                this.placeWeb(level, living);
            }
        }
    }

    private void placeWeb(ServerLevel level, LivingEntity target) {
        this.placeWeb(level, target.blockPosition());
    }

    private void placeWeb(ServerLevel level, BlockHitResult hitResult) {
        this.placeWeb(level, hitResult.getBlockPos().relative(hitResult.getDirection()));
    }

    private void placeWeb(ServerLevel level, BlockPos position) {
        if (level.getGameRules().get(GameRules.MOB_GRIEFING)
                && level.getBlockState(position).canBeReplaced()) {
            level.setBlockAndUpdate(position, Blocks.COBWEB.defaultBlockState());
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            ServerLevel level = (ServerLevel)this.level();
            if (this.createsWebOnHit && hitResult instanceof BlockHitResult blockHit) {
                this.placeWeb(level, blockHit);
            }
            if (this.createsPool && !this.poolCreated && this.getOwner() instanceof SpiderMotherEntity mother) {
                this.poolCreated = true;
                mother.addVenomPool(hitResult.getLocation());
            }
            level.sendParticles(
                    ParticleTypes.WITCH,
                    this.getX(), this.getY(), this.getZ(),
                    8, 0.2, 0.2, 0.2, 0.05);
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

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Damage", this.damage);
        output.putBoolean("CreatesPool", this.createsPool);
        output.putBoolean("CreatesWebOnHit", this.createsWebOnHit);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("Damage", 6.0F);
        this.createsPool = input.getBooleanOr("CreatesPool", false);
        this.createsWebOnHit = input.getBooleanOr("CreatesWebOnHit", false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
