package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class AurorianPigEntity extends Pig {
    private static final EntityDataAccessor<Boolean> PANICKING = SynchedEntityData.defineId(AurorianPigEntity.class, EntityDataSerializers.BOOLEAN);
    private @Nullable PanicGoal panicGoal;
    public final AnimalVisualState visualState = new AnimalVisualState(112, 68);

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

    public AurorianPigEntity(EntityType<? extends Pig> type, Level level) {
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).is(Items.SADDLE) ? InteractionResult.PASS : super.mobInteract(player, hand);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.SADDLE && super.canUseSlot(slot);
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.SADDLE && super.canDispenserEquipIntoSlot(slot);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public @Nullable Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
