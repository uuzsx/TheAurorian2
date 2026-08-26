package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;

/** Zombie variant used only by the purification ritual. */
public final class PurificationRitualZombieEntity extends Zombie {

    private BlockPos altarPos = BlockPos.ZERO;
    private long ritualId;
    private int playerAggroTicks;

    public PurificationRitualZombieEntity(EntityType<? extends PurificationRitualZombieEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setCanPickUpLoot(false);
        this.setPersistenceRequired();
    }

    public void configureRitual(BlockPos altarPos, long ritualId) {
        this.altarPos = altarPos.immutable();
        this.ritualId = ritualId;
        this.setCanBreakDoors(false);
        this.setPersistenceRequired();
    }

    public boolean belongsTo(BlockPos pos, long id) {
        return this.ritualId == id && this.altarPos.equals(pos);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new RitualAltarAttackGoal(this));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt) {
            Player attacker = source.getEntity() instanceof Player player
                    ? player
                    : source.getDirectEntity() instanceof Projectile projectile
                            && projectile.getOwner() instanceof Player player
                            ? player
                            : null;
            if (attacker != null) {
                this.setTarget(attacker);
                this.playerAggroTicks = 5 * 20;
            }
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.playerAggroTicks > 0) {
            this.playerAggroTicks--;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    private static final class RitualAltarAttackGoal extends Goal {

        private final PurificationRitualZombieEntity zombie;
        private int attackDelay;
        private int attackCooldown;

        private RitualAltarAttackGoal(PurificationRitualZombieEntity zombie) {
            this.zombie = zombie;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.shouldAttackAltar();
        }

        @Override
        public boolean canContinueToUse() {
            return this.shouldAttackAltar();
        }

        @Override
        public void start() {
            this.attackDelay = 0;
            this.attackCooldown = 0;
        }

        @Override
        public void stop() {
            this.zombie.getNavigation().stop();
            this.attackDelay = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            PurificationAltarBlockEntity altar = this.getAltar();
            if (altar == null) {
                return;
            }

            double distance = this.zombie.distanceToSqr(altar.getBlockPos().getCenter());
            if (distance > 3.0D) {
                this.attackDelay = 0;
                this.zombie.getNavigation().moveTo(
                        altar.getBlockPos().getX() + 0.5D,
                        altar.getBlockPos().getY(),
                        altar.getBlockPos().getZ() + 0.5D,
                        1.05D);
                return;
            }

            this.zombie.getNavigation().stop();
            this.zombie.getLookControl().setLookAt(
                    altar.getBlockPos().getX() + 0.5D,
                    altar.getBlockPos().getY() + 0.8D,
                    altar.getBlockPos().getZ() + 0.5D);
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            if (this.attackDelay > 0) {
                this.attackDelay--;
                if (this.attackDelay == 0) {
                    altar.tryDamageShield(this.zombie);
                    this.attackCooldown = 20;
                }
            } else if (this.attackCooldown <= 0) {
                this.zombie.swing(InteractionHand.MAIN_HAND, true);
                this.attackDelay = 10;
            }
        }

        private boolean shouldAttackAltar() {
            PurificationAltarBlockEntity altar = this.getAltar();
            if (altar == null || altar.getShieldCount() <= 0) {
                return false;
            }

            if (this.zombie.playerAggroTicks > 0
                    || this.zombie.getLastHurtByPlayerMemoryTime() > 0) {
                return false;
            }

            Player target = this.zombie.getTarget() instanceof Player player && player.isAlive()
                    ? player
                    : null;
            if (target == null) {
                target = this.zombie.level().getNearestPlayer(this.zombie, 32.0D);
            }
            if (target == null) {
                return true;
            }

            double altarDistance = this.zombie.distanceToSqr(altar.getBlockPos().getCenter());
            double playerDistance = this.zombie.distanceToSqr(target);
            return altarDistance + 4.0D < playerDistance;
        }

        private PurificationAltarBlockEntity getAltar() {
            return this.zombie.level().getBlockEntity(this.zombie.altarPos)
                    instanceof PurificationAltarBlockEntity altar
                    && altar.isRitualActive()
                    && altar.belongsToRitual(this.zombie.ritualId)
                    ? altar
                    : null;
        }
    }
}
