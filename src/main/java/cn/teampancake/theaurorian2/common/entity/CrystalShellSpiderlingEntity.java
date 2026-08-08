package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class CrystalShellSpiderlingEntity extends AbstractSpiderlingEntity {

    private static final int HEAL_INTERVAL_TICKS = 20;
    private static final double HEAL_RADIUS = 16.0;
    private static final float HEAL_AMOUNT = 1.0F;

    public CrystalShellSpiderlingEntity(EntityType<? extends CrystalShellSpiderlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 4;
        this.updateAttackDamage(level.getDifficulty(), 6.0, 8.0, 10.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSpiderlingAttributes(24.0, 6.0, 6.0, 0.26);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.updateAttackDamage(level.getDifficulty(), 6.0, 8.0, 10.0);
        super.customServerAiStep(level);
        if (this.tickCount % HEAL_INTERVAL_TICKS == 0) {
            this.healNearbySpiders(level);
        }
    }

    private void healNearbySpiders(ServerLevel level) {
        double radiusSqr = Mth.square(HEAL_RADIUS);
        AABB searchArea = this.getBoundingBox().inflate(HEAL_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                candidate -> isSpider(candidate)
                        && candidate.isAlive()
                        && candidate.distanceToSqr(this) <= radiusSqr)) {
            target.heal(HEAL_AMOUNT);
        }
    }

    private static boolean isSpider(LivingEntity entity) {
        return entity instanceof Spider
                || entity instanceof SpiderMotherEntity
                || entity instanceof AbstractSpiderlingEntity;
    }
}
