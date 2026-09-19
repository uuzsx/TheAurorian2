package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class AurorianRabbitEntity extends Rabbit {
    private static final EntityDataAccessor<Boolean> PANICKING = SynchedEntityData.defineId(AurorianRabbitEntity.class, EntityDataSerializers.BOOLEAN);
    private @Nullable PanicGoal panicGoal;
    public final AnimalVisualState visualState = new AnimalVisualState(92, 90);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PANICKING, false);
    }

    public boolean isPanicking() { return entityData.get(PANICKING); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) visualState.tick(this, isPanicking());
        else entityData.set(PANICKING, panicGoal != null && panicGoal.isRunning());
    }

    public AurorianRabbitEntity(EntityType<? extends Rabbit> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof BreedGoal || goal instanceof TemptGoal);
        for (var wrapped : goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof PanicGoal goal) panicGoal = goal;
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable Rabbit getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
