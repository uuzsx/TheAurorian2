package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.world.SpiderBroodData;
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
        if (this.level() instanceof ServerLevel level) {
            this.hatchAge++;
            if (this.hatchAge >= HATCH_TIME_TICKS - 20 && this.hatchAge % 5 == 0) {
                level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + .5, this.getZ(), 3, .2, .25, .2, 0);
                if (this.hatchAge == HATCH_TIME_TICKS - 20) this.playSound(net.minecraft.sounds.SoundEvents.SPIDER_AMBIENT, .6F, 1.4F);
            }
            if (this.hatchAge >= HATCH_TIME_TICKS) this.hatch(level);
        }
    }

    public void setMother(@Nullable UUID motherId) {
        this.motherId = motherId;
        if (this.isAddedToLevel() && this.level() instanceof ServerLevel level) {
            if (motherId == null) {
                SpiderBroodData.get(level).remove(this.getUUID());
            } else {
                SpiderBroodData.get(level).add(this.getUUID(), motherId);
            }
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // Also imports offspring from saves created before the brood ledger existed.
        if (this.motherId != null && this.level() instanceof ServerLevel level) {
            SpiderBroodData.get(level).add(this.getUUID(), this.motherId);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        if (this.level() instanceof ServerLevel level
                && this.getRemovalReason() != null && this.getRemovalReason().shouldDestroy()) {
            SpiderBroodData.get(level).remove(this.getUUID());
        }
        super.onRemovedFromLevel();
    }

    public boolean belongsTo(SpiderMotherEntity mother) {
        return this.motherId != null && this.motherId.equals(mother.getUUID());
    }

    private void hatch(ServerLevel level) {
        AbstractSpiderlingEntity spiderling = this.createSpiderling(level);
        if (spiderling == null) {
            this.discard();
            return;
        }

        Vec3 spawn = SpiderSummonPlacement.find(level, spiderling, this.position());
        if (spawn == null) {
            this.hatchAge = HATCH_TIME_TICKS - 20;
            return;
        }
        spiderling.setMother(this.motherId);
        spiderling.snapTo(spawn.x, spawn.y, spawn.z, this.getYRot(), 0.0F);
        // The egg already reserves a brood slot. Replace it only after a successful spawn.
        if (!level.addFreshEntity(spiderling)) {
            this.hatchAge = HATCH_TIME_TICKS - 20;
            return;
        }
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
