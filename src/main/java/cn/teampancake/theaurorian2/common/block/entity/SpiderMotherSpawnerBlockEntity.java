package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class SpiderMotherSpawnerBlockEntity extends BlockEntity {

    private boolean armed;
    private @Nullable BlockPos barrierCenter;

    public SpiderMotherSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIDER_MOTHER_SPAWNER.get(), pos, state);
    }

    public void setBarrierCenter(BlockPos barrierCenter) {
        this.barrierCenter = barrierCenter.immutable();
        this.setChanged();
    }

    public void arm(ServerLevel level) {
        if (!this.armed) {
            this.armed = true;
            this.setChanged();
        }
        this.trySpawn(level);
    }

    public void trySpawn(ServerLevel level) {
        if (!this.armed || this.isRemoved()) {
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 20);
            return;
        }
        SpiderMotherEntity spiderMother = ModEntities.SPIDER_MOTHER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (spiderMother == null) {
            level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 20);
            return;
        }
        spiderMother.setArenaBarrier(this.barrierCenter);
        spiderMother.snapTo(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 1.0D,
                this.worldPosition.getZ() + 0.5D,
                spiderMother.getRandom().nextFloat() * 360.0F,
                0.0F);
        spiderMother.finalizeSpawn(
                level, level.getCurrentDifficultyAt(this.worldPosition), EntitySpawnReason.TRIGGERED, null);
        if (level.addFreshEntity(spiderMother)) {
            level.removeBlock(this.worldPosition, false);
        } else {
            level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 20);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Armed", this.armed);
        if (this.barrierCenter != null) {
            output.putInt("BarrierX", this.barrierCenter.getX());
            output.putInt("BarrierY", this.barrierCenter.getY());
            output.putInt("BarrierZ", this.barrierCenter.getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.armed = input.getBooleanOr("Armed", false);
        if (input.getInt("BarrierX").isPresent()
                && input.getInt("BarrierY").isPresent()
                && input.getInt("BarrierZ").isPresent()) {
            this.barrierCenter = new BlockPos(
                    input.getIntOr("BarrierX", 0),
                    input.getIntOr("BarrierY", 0),
                    input.getIntOr("BarrierZ", 0));
        } else {
            this.barrierCenter = null;
        }
    }
}
