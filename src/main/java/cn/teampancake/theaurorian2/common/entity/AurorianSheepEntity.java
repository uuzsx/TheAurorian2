package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class AurorianSheepEntity extends Sheep {
    public AurorianSheepEntity(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof BreedGoal || goal instanceof TemptGoal);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void shear(ServerLevel level, SoundSource soundSource, ItemStack tool) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1.0F, 1.0F);
        ItemStack wool = new ItemStack(
                ModStructureBlocks.blocksById().get("mysterium_wool").get().asItem(),
                this.getRandom().nextInt(3) + 1);
        ItemEntity dropped = this.spawnAtLocation(level, wool, 1.0F);
        if (dropped != null) {
            dropped.setDeltaMovement(dropped.getDeltaMovement().add(
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                    this.random.nextFloat() * 0.05F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
        this.setSheared(true);
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
