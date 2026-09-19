package cn.teampancake.theaurorian2.common.entity.ai;

import cn.teampancake.theaurorian2.common.entity.AzureWarblerEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;

public final class WarblerFrightenedFlightGoal extends Goal {
    private final AzureWarblerEntity bird;
    private int nextTurnTick;

    public WarblerFrightenedFlightGoal(AzureWarblerEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return bird.isAlive() && bird.isFrightened() && !bird.isPassenger();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        nextTurnTick = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (bird.tickCount < nextTurnTick) return;
        nextTurnTick = bird.tickCount + 12 + bird.getRandom().nextInt(13);
        var away = bird.position().subtract(bird.frightOrigin());
        double angle = Math.atan2(away.z, away.x) + (bird.getRandom().nextDouble() - 0.5) * 2.0;
        var target = AirAndWaterRandomPos.getPos(bird, 8, 4, 1, Math.cos(angle), Math.sin(angle), Math.PI / 2);
        if (target != null && bird.level().getFluidState(BlockPos.containing(target)).isEmpty()) {
            bird.getNavigation().moveTo(target.x, target.y, target.z, 1.6);
        }
    }

    @Override
    public void stop() {
        bird.getNavigation().stop();
    }
}
