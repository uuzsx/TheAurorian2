package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class SpiderlingEntity extends AbstractSpiderlingEntity {

    public SpiderlingEntity(EntityType<? extends SpiderlingEntity> type, Level level) {
        super(type, level);
        this.updateAttackDamage(level.getDifficulty(), 8.0, 10.0, 12.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSpiderlingAttributes(28.0, 8.0, 8.0, 0.35);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.updateAttackDamage(level.getDifficulty(), 8.0, 10.0, 12.0);
        super.customServerAiStep(level);
    }
}
