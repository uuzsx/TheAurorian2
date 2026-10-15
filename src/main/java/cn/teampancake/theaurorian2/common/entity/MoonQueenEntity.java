package cn.teampancake.theaurorian2.common.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

/** Egg-only animation preview. No attack, retaliation, targets or story progression. */
public final class MoonQueenEntity extends PathfinderMob implements GeoEntity {
    public record Preview(String name, double seconds, boolean loop, RawAnimation animation) {
        private Preview(String name, double seconds, boolean loop) {
            this(name, seconds, loop, loop ? RawAnimation.begin().thenLoop("rebuilt." + name)
                    : RawAnimation.begin().thenPlayAndHold("rebuilt." + name));
        }
        public int durationTicks() { return loop ? Math.max(40, (int)Math.ceil(seconds * 40)) : (int)Math.ceil(seconds * 20); }
    }
    public static final Preview[] PREVIEWS = {
            new Preview("idle",4,true), new Preview("walk",1.6,true),
            new Preview("hit1",1.2,false), new Preview("hit2",1.2,false), new Preview("hit3",1.2,false),
            new Preview("run1",0.8,true), new Preview("run2",0.72,true), new Preview("run3",0.9,true),
            new Preview("sword_left_slash",1.85,false), new Preview("sword_round_right_slash",2,false),
            new Preview("sword_uppercut",1.85,false), new Preview("sword_smash",2.15,false), new Preview("sword_right_slash",1.65,false),
            new Preview("scythe_jump_smash",2.7,false), new Preview("scythe_roundhouse",2.25,false), new Preview("scythe_right_slash",1.95,false),
            new Preview("sword_side_kick",2.3,false), new Preview("sword_step_slash",2,false), new Preview("sword_step_to_left_slash",1.9,false),
            new Preview("sword_horizontal_slash",1.9,false), new Preview("scythe_left_slash",2,false), new Preview("sword_thrust",1.7,false),
            new Preview("sword_thrust_to_kick",2.6,false), new Preview("sword_lionclaw",3.1,false),
            new Preview("parryleft",1.5,false), new Preview("parryright",1.5,false), new Preview("defencebreak",3.2,false),
            new Preview("execute",2.5,false), new Preview("death",2.4,false)};
    private static final EntityDataAccessor<Integer> PREVIEW = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PREVIEW_START = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.LONG);
    private int lastPreview = -1;
    public enum Motion { IDLE, START, WALK, STOP }
    private static final Motion[] MOTIONS = Motion.values();

    public static final float MODEL_SCALE = .65F;
    private static final int START_TICKS = 18, STOP_TICKS = 16;
    private static final double WALK_PERIOD = 1.33333;
    // Two steps, measured from the original foot travel (~15 pixels per step).
    private static final double WALK_STRIDE = 1.92;
    private static final EntityDataAccessor<Integer> MOTION = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> IDLE_STYLE = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STOP_FOOT = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> MOTION_START = SynchedEntityData.defineId(MoonQueenEntity.class, EntityDataSerializers.LONG);
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation BLINK = RawAnimation.begin().thenLoop("misc.blink");
    private static final RawAnimation[] IDLES = {
            RawAnimation.begin().thenLoop("idle.sword_low"),
            RawAnimation.begin().thenLoop("idle.sword_shoulder")};
    private static final RawAnimation[] STARTS = {
            RawAnimation.begin().thenPlayAndHold("transition.low_to_walk"),
            RawAnimation.begin().thenPlayAndHold("transition.shoulder_to_walk")};
    private static final RawAnimation[][] STOPS = {
            {RawAnimation.begin().thenPlayAndHold("transition.walk_to_low_left"),
             RawAnimation.begin().thenPlayAndHold("transition.walk_to_low_right"),
             RawAnimation.begin().thenPlayAndHold("transition.walk_to_low_cycle_end")},
            {RawAnimation.begin().thenPlayAndHold("transition.walk_to_shoulder_left"),
             RawAnimation.begin().thenPlayAndHold("transition.walk_to_shoulder_right"),
             RawAnimation.begin().thenPlayAndHold("transition.walk_to_shoulder_cycle_end")}};
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private int idleTicksRemaining = 160;
    private double walkDistance, previousWalkDistance;

    public MoonQueenEntity(EntityType<? extends MoonQueenEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        entityData.set(MOTION_START, level.getGameTime());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, .16).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOTION, Motion.IDLE.ordinal());
        builder.define(IDLE_STYLE, 0);
        builder.define(STOP_FOOT, 0);
        builder.define(MOTION_START, 0L);
        builder.define(PREVIEW, -1); builder.define(PREVIEW_START, 0L);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new QuietWanderGoal());
    }

    @Override public boolean canAttack(LivingEntity target) { return false; }
    @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
    public Motion motion() { return MOTIONS[entityData.get(MOTION)]; }
    public int idleStyle() { return entityData.get(IDLE_STYLE); }
    public long animationStateStamp() {
        return previewIndex() < 0 ? entityData.get(MOTION_START) * 64 + entityData.get(MOTION)
                : entityData.get(PREVIEW_START) * 64 + 32 + previewIndex();
    }
    public int previewIndex() { return entityData.get(PREVIEW); }
    public double previewAge(float partial) { return Math.max(0, level().getGameTime() - entityData.get(PREVIEW_START) + partial); }
    public void beginPreview(int index) {
        if (index < 0 || index >= PREVIEWS.length) throw new IllegalArgumentException("Unknown queen animation");
        navigation.stop(); changeMotion(Motion.IDLE); idleTicksRemaining = 160; lastPreview = index;
        entityData.set(PREVIEW_START, level().getGameTime()); entityData.set(PREVIEW, index);
    }
    public void stopPreview() {
        entityData.set(PREVIEW, -1); navigation.stop(); idleTicksRemaining = 160;
        entityData.set(MOTION_START, level().getGameTime()); changeMotion(Motion.IDLE);
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(hand).isEmpty()) return super.mobInteract(player, hand);
        if (!level().isClientSide()) {
            if (player.isShiftKeyDown()) stopPreview();
            else {
                beginPreview((lastPreview + 1) % PREVIEWS.length);
                player.sendOverlayMessage(Component.translatable("message.theaurorian2.queen_animation", previewIndex()+1, PREVIEWS.length,
                        Component.translatable("animation.theaurorian2.queen." + PREVIEWS[previewIndex()].name())));
            }
        }
        return InteractionResult.SUCCESS;
    }
    public double motionAge(float partialTick) {
        return Math.max(0, level().getGameTime() - entityData.get(MOTION_START) + partialTick);
    }

    private void changeMotion(Motion next) {
        entityData.set(MOTION_START, level().getGameTime());
        entityData.set(MOTION, next.ordinal());
        if (next == Motion.WALK) walkDistance = previousWalkDistance = 0;
    }

    private void stopWalking() {
        double phase = walkDistance / WALK_STRIDE * WALK_PERIOD % WALK_PERIOD;
        int foot = phase < WALK_PERIOD / 4 ? 0 : phase < WALK_PERIOD * .75 ? 1 : 2;
        entityData.set(STOP_FOOT, foot);
        // Alternate the approved resting poses after each stroll, never revive the rejected high pose.
        entityData.set(IDLE_STYLE, 1 - idleStyle());
        navigation.stop();
        changeMotion(Motion.STOP);
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (MOTION.equals(key) && motion() == Motion.WALK) walkDistance = previousWalkDistance = 0;
    }

    @Override protected void updateWalkAnimation(float distance) {
        super.updateWalkAnimation(distance);
        if (level().isClientSide()) {
            previousWalkDistance = walkDistance;
            if (motion() == Motion.WALK && onGround() && distance >= 0 && distance <= 1.5)
                walkDistance += distance / (getScale() * MODEL_SCALE);
        }
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel)) return;
        if (previewIndex() >= 0) {
            navigation.stop();
            if (previewAge(0) >= PREVIEWS[previewIndex()].durationTicks()) stopPreview();
            return;
        }
        if (motion() == Motion.WALK && onGround()) {
            double distance = Math.hypot(getX() - xo, getZ() - zo);
            if (distance <= 1.5) walkDistance += distance / (getScale() * MODEL_SCALE);
        }
        if (isNoAi() || !isAlive()) {
            if (motion() != Motion.IDLE) {
                navigation.stop();
                changeMotion(Motion.IDLE);
            }
            return;
        }
        if (motion() == Motion.IDLE && idleTicksRemaining > 0) idleTicksRemaining--;
        if (motion() == Motion.STOP && motionAge(0) >= STOP_TICKS) {
            idleTicksRemaining = (idleStyle() == 0 ? 120 : 160) + random.nextInt(61);
            changeMotion(Motion.IDLE);
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Seeking authored time skips GeckoLib's fixed transition timeline.
        // The renderer blends the actual last pose/velocity instead of holding
        // a preset left/right foot pose for four ticks before playing the stop.
        controllers.add(new AnimationController<MoonQueenEntity>("locomotion", 0, state -> {
            if (previewIndex() >= 0) {
                Preview preview = PREVIEWS[previewIndex()];
                double time = previewAge(state.renderState().getPartialTick()) / 20.;
                state.setAnimation(preview.animation());
                state.controller().setAnimationTime(preview.loop() ? time % preview.seconds() : Math.min(time, preview.seconds()));
                return PlayState.CONTINUE;
            }
            Motion motion = motion();
            double elapsed = motionAge(state.renderState().getPartialTick()) / 20;
            RawAnimation clip = switch (motion) {
                case IDLE -> IDLES[idleStyle()];
                case START -> STARTS[idleStyle()];
                case WALK -> WALK;
                case STOP -> STOPS[idleStyle()][entityData.get(STOP_FOOT)];
            };
            state.setAnimation(clip);
            double time = switch (motion) {
                case IDLE -> elapsed % (idleStyle() == 0 ? 6 : 8);
                case START -> Math.min(.9, elapsed);
                case STOP -> Math.min(.8, elapsed);
                case WALK -> (Mth.lerp(state.renderState().getPartialTick(), previousWalkDistance, walkDistance)
                        / WALK_STRIDE * WALK_PERIOD) % WALK_PERIOD;
            };
            state.controller().setAnimationTime(time);
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<MoonQueenEntity>("blink", 0, state -> {
            state.setAnimation(BLINK);
            state.controller().setAnimationTime(((tickCount + state.renderState().getPartialTick() + getId() * 13) % 80) / 20);
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("IdleStyle", idleStyle());
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getIntOr("QueenCombatVersion", 0) > 0) {
            // Restore the old passive preview and preserve its current health fraction.
            float fraction = getHealth() / getMaxHealth();
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.16);
            getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16);
            for (var attribute : java.util.List.of(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE)) {
                var instance = getAttribute(attribute); if (instance != null) instance.setBaseValue(0);
            }
            setHealth(Math.max(1, getMaxHealth() * fraction));
        }
        entityData.set(PREVIEW, -1);
        entityData.set(IDLE_STYLE, Mth.clamp(input.getIntOr("IdleStyle", 0), 0, 1));
        idleTicksRemaining = (idleStyle() == 0 ? 120 : 160) + random.nextInt(61);
        // Paths are transient: a save loaded mid-step resumes in a stable resting pose.
        changeMotion(Motion.IDLE);
    }

    private final class QuietWanderGoal extends Goal {
        private Path path;
        private int walkingTicks;

        private QuietWanderGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override public boolean canUse() {
            if (previewIndex() >= 0 || motion() != Motion.IDLE || idleTicksRemaining > 0 || !onGround() || isInWater()) return false;
            // At most three bounded vanilla ground-position attempts, once per resting period.
            for (int attempt = 0; attempt < 3; attempt++) {
                var destination = LandRandomPos.getPos(MoonQueenEntity.this, 10, 3);
                if (destination == null || destination.distanceToSqr(position()) < 9) continue;
                path = navigation.createPath(destination.x, destination.y, destination.z, 0);
                if (path != null && path.canReach()) return true;
            }
            idleTicksRemaining = 40;
            return false;
        }

        @Override public void start() { walkingTicks = 0; changeMotion(Motion.START); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public boolean canContinueToUse() {
            return previewIndex() < 0 && !isInWater() && isAlive() && (motion() == Motion.START || motion() == Motion.WALK);
        }
        @Override public void tick() {
            if (motion() == Motion.START) {
                double age = motionAge(0);
                // Shift weight before moving; finish the step into the real walking speed.
                if (age >= 9 && navigation.isDone()) navigation.moveTo(path, .15);
                if (age >= 9) navigation.setSpeedModifier(.15 + .85 * Math.clamp((age - 9) / 9, 0, 1));
                if (age >= START_TICKS) {
                    navigation.setSpeedModifier(1);
                    changeMotion(Motion.WALK);
                }
            } else if (motion() == Motion.WALK) {
                walkingTicks++;
                if (navigation.isDone() || walkingTicks > 180) stopWalking();
            }
        }
        @Override public void stop() {
            if (motion() == Motion.START || motion() == Motion.WALK) stopWalking();
            path = null;
        }
    }
}
