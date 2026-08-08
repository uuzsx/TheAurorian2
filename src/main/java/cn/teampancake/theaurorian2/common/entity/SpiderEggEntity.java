package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SpiderEggEntity extends Monster implements GeoEntity {

    public static final int HATCH_TIME_TICKS = 4 * 20;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable UUID motherId;
    private int hatchAge;

    public SpiderEggEntity(EntityType<? extends SpiderEggEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && ++this.hatchAge >= HATCH_TIME_TICKS) {
            this.hatch(level);
        }
    }

    public void setMother(@Nullable UUID motherId) {
        this.motherId = motherId;
    }

    public boolean belongsTo(SpiderMotherEntity mother) {
        return this.motherId != null && this.motherId.equals(mother.getUUID());
    }

    private void hatch(ServerLevel level) {
        if (this.motherId != null && countOwnedSpiderlings(level, this.motherId, this.position()) >= SpiderMotherEntity.MAX_SPIDERLINGS) {
            this.discard();
            return;
        }

        AbstractSpiderlingEntity spiderling = this.createSpiderling(level);
        if (spiderling == null) {
            this.discard();
            return;
        }

        spiderling.setMother(this.motherId);
        spiderling.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        level.addFreshEntity(spiderling);
        level.sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + 0.2, this.getZ(),
                12, 0.3, 0.2, 0.3, 0.04);
        this.discard();
    }

    private @Nullable AbstractSpiderlingEntity createSpiderling(ServerLevel level) {
        return (this.random.nextBoolean()
                        ? ModEntities.SPIDERLING_CRYSTAL_SHELL
                        : ModEntities.SPIDERLING)
                .get()
                .create(level, EntitySpawnReason.TRIGGERED);
    }

    public static int countOwnedSpiderlings(ServerLevel level, UUID motherId, Vec3 center) {
        AABB searchArea = new AABB(center, center).inflate(32.0);
        return level.getEntitiesOfClass(
                AbstractSpiderlingEntity.class,
                searchArea,
                spiderling -> motherId.equals(spiderling.getMotherId())).size();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (attacker instanceof SpiderMotherEntity || attacker instanceof AbstractSpiderlingEntity) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void travel(Vec3 input) {
        double x = this.getX();
        double z = this.getZ();
        super.travel(Vec3.ZERO);
        this.setPos(x, this.getY(), z);
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
    }

    @Override
    public void knockback(double power, double x, double z) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("HatchAge", this.hatchAge);
        if (this.motherId != null) {
            output.putString("Mother", this.motherId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.hatchAge = input.getIntOr("HatchAge", 0);
        String encodedMother = input.getStringOr("Mother", "");
        if (!encodedMother.isEmpty()) {
            try {
                this.motherId = UUID.fromString(encodedMother);
            } catch (IllegalArgumentException ignored) {
                this.motherId = null;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
