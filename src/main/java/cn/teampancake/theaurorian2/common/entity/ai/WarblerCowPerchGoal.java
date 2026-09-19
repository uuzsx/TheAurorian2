package cn.teampancake.theaurorian2.common.entity.ai;

import cn.teampancake.theaurorian2.common.entity.AurorianCowEntity;
import cn.teampancake.theaurorian2.common.entity.AzureWarblerEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

public final class WarblerCowPerchGoal extends Goal {
    private final AzureWarblerEntity bird;
    private @Nullable AurorianCowEntity target;
    private int nextSearchTick, approachDeadline, nextPathTick;

    public WarblerCowPerchGoal(AzureWarblerEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (bird.isPerchedOnCow()) return true;
        if (!bird.canSeekCow() || bird.tickCount < nextSearchTick) return false;
        nextSearchTick = bird.tickCount + 160 + bird.getRandom().nextInt(81);
        if (bird.getRandom().nextInt(4) != 0) return false;
        target = null;
        double nearest = 144;
        // One bounded local query per successful roll, not a per-tick herd scan.
        for (var cow : bird.level().getEntitiesOfClass(AurorianCowEntity.class,
                bird.getBoundingBox().inflate(12, 6, 12), AurorianCowEntity::canAcceptWarbler)) {
            double distance = bird.distanceToSqr(cow);
            if (distance < nearest) {
                target = cow;
                nearest = distance;
            }
        }
        return target != null;
    }

    @Override
    public void start() {
        approachDeadline = bird.tickCount + 240;
        nextPathTick = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return bird.isPerchedOnCow() || (bird.canSeekCow() && target != null && target.canAcceptWarbler()
                && bird.distanceToSqr(target) < 256 && bird.tickCount < approachDeadline);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (bird.isPerchedOnCow()) {
            bird.getNavigation().stop();
            return;
        }
        if (target == null) return;
        var perch = target.getPassengerRidingPosition(bird);
        var delta = perch.subtract(bird.position());
        if (delta.horizontalDistanceSqr() < 0.7 * 0.7 && delta.y < 0.25 && delta.y > -1.25) {
            bird.getNavigation().stop();
            bird.getMoveControl().setWantedPosition(perch.x, perch.y, perch.z, 0.7);
            if (delta.lengthSqr() < 0.35 * 0.35) bird.perchOnCow(target);
        } else if (bird.tickCount >= nextPathTick) {
            nextPathTick = bird.tickCount + 10;
            bird.getNavigation().moveTo(perch.x, perch.y + 0.7, perch.z, 1.0);
        }
    }

    @Override
    public void stop() {
        target = null;
        bird.getNavigation().stop();
        nextSearchTick = bird.tickCount + 160 + bird.getRandom().nextInt(81);
    }
}
