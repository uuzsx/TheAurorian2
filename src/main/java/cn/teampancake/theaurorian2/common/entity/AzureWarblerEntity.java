package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import cn.teampancake.theaurorian2.common.entity.ai.WarblerCowPerchGoal;
import cn.teampancake.theaurorian2.common.entity.ai.WarblerFrightenedFlightGoal;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** Vanilla parrot behavior and shoulder persistence, with lavender-seed taming. */
public final class AzureWarblerEntity extends Parrot implements GeoEntity {
    public static final TagKey<Block> SPAWNABLE_ON = TagKey.create(
            Registries.BLOCK, TheAurorian2.id("azure_warbler_spawnable_on"));
    public static final SpawnPlacementType SPAWN_PLACEMENT = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> type) {
            // Leaves allow exactly vanilla parrots/ocelots. Reuse the parrot's
            // clearance/support checks here without changing global leaf rules.
            return SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, EntityType.PARROT);
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader level, BlockPos pos) {
            return SpawnPlacementTypes.ON_GROUND.adjustSpawnPosition(level, pos);
        }
    };
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("move.fly");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    private static final RawAnimation PARTY = RawAnimation.begin().thenLoop("party");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private int cowPerchTicks, cowPerchCooldown, frightenedTicks;
    private Vec3 frightOrigin = Vec3.ZERO;

    public AzureWarblerEntity(EntityType<? extends AzureWarblerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(-1, new WarblerFrightenedFlightGoal(this));
        goalSelector.addGoal(1, new WarblerCowPerchGoal(this));
    }

    public boolean isPerchedOnCow() {
        return getVehicle() instanceof AurorianCowEntity;
    }

    public boolean isFrightened() {
        return frightenedTicks > 0;
    }

    public Vec3 frightOrigin() {
        return frightOrigin;
    }

    public boolean canSeekCow() {
        return isAlive() && !isTame() && !isPassenger() && !isLeashed() && !isOrderedToSit()
                && !isFrightened() && cowPerchCooldown == 0 && hurtTime == 0 && !isOnFire();
    }

    public boolean perchOnCow(AurorianCowEntity cow) {
        if (level().isClientSide() || !canSeekCow() || !cow.canAcceptWarbler()) return false;
        var perch = cow.getPassengerRidingPosition(this);
        var bounds = getDimensions(getPose()).makeBoundingBox(perch);
        if (!level().noBlockCollision(this, bounds) || level().containsAnyLiquid(bounds)) return false;
        if (!startRiding(cow)) return false;
        cowPerchTicks = 300 + random.nextInt(301);
        navigation.stop();
        setDeltaMovement(Vec3.ZERO);
        cow.positionRider(this);
        alignWithCow(cow);
        return true;
    }

    @Override
    public boolean canControlVehicle() {
        return !isPerchedOnCow() && super.canControlVehicle();
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        return vehicle instanceof AurorianCowEntity ? Vec3.ZERO : super.getVehicleAttachmentPoint(vehicle);
    }

    @Override
    public boolean canSitOnShoulder() {
        return !isFrightened() && !isPassenger() && super.canSitOnShoulder();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (cowPerchCooldown > 0) cowPerchCooldown--;
            if (frightenedTicks > 0) frightenedTicks--;
            if (getVehicle() instanceof AurorianCowEntity cow) {
                if (!cow.isSafeForWarbler() || hurtTime > 0 || isOnFire()) {
                    startleFrom(cow.position());
                } else if (isTame() || isLeashed() || isOrderedToSit()) {
                    leaveCow(false, cow.position());
                } else {
                    if (cowPerchTicks <= 0) cowPerchTicks = 300 + random.nextInt(301);
                    if (!isNoAi() && --cowPerchTicks == 0) leaveCow(false, cow.position());
                }
            }
        }
        if (getVehicle() instanceof AurorianCowEntity cow) alignWithCow(cow);
    }

    private void alignWithCow(AurorianCowEntity cow) {
        setYRot(cow.yBodyRot);
        setYBodyRot(cow.yBodyRot);
        setYHeadRot(cow.yBodyRot);
    }

    public void startleFrom(Vec3 source) {
        if (level().isClientSide() || !isAlive()) return;
        frightOrigin = source;
        frightenedTicks = 120;
        cowPerchCooldown = 600 + random.nextInt(401);
        leaveCow(true, source);
    }

    private void leaveCow(boolean frightened, Vec3 source) {
        if (isPerchedOnCow()) stopRiding();
        if (isPassenger()) return; // Respect another mod cancelling a dismount.
        cowPerchTicks = 0;
        if (!frightened) cowPerchCooldown = 400 + random.nextInt(401);
        navigation.stop();
        double angle = Math.atan2(getZ() - source.z, getX() - source.x);
        if (position().subtract(source).horizontalDistanceSqr() < 0.01) angle = random.nextDouble() * Math.PI * 2;
        angle += (random.nextDouble() - 0.5) * 0.8;
        double speed = frightened ? 0.42 : 0.18;
        setOnGround(false);
        setDeltaMovement(Math.cos(angle) * speed, frightened ? 0.4 : 0.22, Math.sin(angle) * speed);
        needsSync = true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (cowPerchTicks > 0) output.putInt("CowPerchTicks", cowPerchTicks);
        if (cowPerchCooldown > 0) output.putInt("CowPerchCooldown", cowPerchCooldown);
        if (frightenedTicks > 0) output.putInt("CowFrightenedTicks", frightenedTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        cowPerchTicks = Mth.clamp(input.getIntOr("CowPerchTicks", 0), 0, 600);
        cowPerchCooldown = Mth.clamp(input.getIntOr("CowPerchCooldown", 0), 0, 1000);
        frightenedTicks = Mth.clamp(input.getIntOr("CowFrightenedTicks", 0), 0, 120);
        frightOrigin = position();
    }

    public static boolean checkSpawnRules(EntityType<AzureWarblerEntity> type, LevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isTame()) {
            var stack = player.getItemInHand(hand);
            if (stack.is(ModLegacyItems.LAVENDER_SEEDS.get())) {
                usePlayerItem(player, hand, stack);
                if (!isSilent()) {
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.PARROT_EAT,
                            getSoundSource(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
                }
                if (!level().isClientSide()) {
                    if (random.nextInt(10) == 0 && !EventHooks.onAnimalTame(this, player)) {
                        tame(player);
                        level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            // Parrot's taming branch uses a fixed tag, not isFood(). Keep this
            // rejection local to the warbler so vanilla parrots keep their food.
            if (stack.is(ItemTags.PARROT_FOOD)) return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AzureWarblerEntity>("movement", 4, state -> {
            if (isPerchedOnCow()) return state.setAndContinue(IDLE);
            if (isPartyParrot()) return state.setAndContinue(PARTY);
            if (isInSittingPose()) return state.setAndContinue(SIT);
            if (isFlying()) return state.setAndContinue(FLY);
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
