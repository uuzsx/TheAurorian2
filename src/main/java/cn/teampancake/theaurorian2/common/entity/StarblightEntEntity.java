package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import java.util.ArrayList;
import java.util.function.Supplier;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class StarblightEntEntity extends Monster implements GeoEntity {
    public enum Wood {
        STARBLIGHT("starblight","starblight_log",ModBlocks.STARBLIGHT_LOG),
        SILENT_WOOD("silent_wood","silent_tree_log",ModBlocks.SILENT_TREE_LOG),
        CURTAIN_TREE("curtain_tree","curtain_tree_log",ModBlocks.CURTAIN_TREE_LOG),
        CURSED_FROST("cursed_frost","cursed_frost_tree_log",ModBlocks.CURSED_FROST_TREE_LOG);
        private final String id,logTexture;
        private final Supplier<? extends Block> log;
        private final TagKey<Biome> habitat;
        Wood(String id,String logTexture,Supplier<? extends Block> log) {
            this.id=id;this.logTexture=logTexture;this.log=log;
            habitat=TagKey.create(Registries.BIOME,TheAurorian2.id("has_"+id+"_ent"));
        }
        public String id() { return id; }
        public String logTexture() { return logTexture; }
        public BlockState disguise() { return log.get().defaultBlockState(); }
        private boolean habitat(Holder<Biome> biome) {
            return this==STARBLIGHT?biome.is(TheAurorian2.id("starblight_swamp")):biome.is(habitat);
        }
    }
    public enum Phase {
        DISGUISED(0,"misc.disguise"), WAKING(20,"misc.wake"), ACTIVE(0,"misc.idle"),
        BITE(16,"attack.bite"), LUNGE(20,"attack.lunge"), HIDING(20,"misc.hide"), HURT(9,"misc.hurt");
        final int duration;
        final RawAnimation animation;
        Phase(int duration,String name) { this.duration=duration;this.animation=RawAnimation.begin().thenPlayAndHold(name); }
    }
    private static final Phase[] PHASES=Phase.values();
    private static final EntityDataAccessor<Byte> PHASE=SynchedEntityData.defineId(StarblightEntEntity.class,EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(StarblightEntEntity.class,EntityDataSerializers.LONG);
    private static final RawAnimation IDLE=RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK=RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation DEATH=RawAnimation.begin().thenPlayAndHold("misc.death");
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    private final Wood wood;
    private int attackCooldown,lungeCooldown,unseenTicks,idleTicks;
    private boolean attackConnected,launched;

    public StarblightEntEntity(EntityType<? extends StarblightEntEntity> type,Level level) {
        super(type,level);
        wood=woodForType(type);
        xpReward=5;
        entityData.set(START,level.getGameTime());
        refreshDimensions();
    }
    public Wood wood() { return wood; }
    @Override public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,
                                                           EntitySpawnReason reason,@Nullable SpawnGroupData group) {
        SpawnGroupData result=super.finalizeSpawn(level,difficulty,reason,group);
        setYRot(level.getRandom().nextInt(4)*90F);
        alignDisguiseFacing();
        return result;
    }
    private void alignDisguiseFacing() {
        // A whole-block disguise must stay aligned to the grid, including its top grain.
        float yaw=Math.round(getYRot()/90F)*90F;
        setYRot(yaw);setYHeadRot(yaw);setXRot(0);
        yRotO=yHeadRotO=yBodyRot=yBodyRotO=yaw;xRotO=0;
    }
    private static Wood woodForType(EntityType<?> type) {
        if (type==ModEntities.SILENT_WOOD_ENT.get()) return Wood.SILENT_WOOD;
        if (type==ModEntities.CURTAIN_TREE_ENT.get()) return Wood.CURTAIN_TREE;
        if (type==ModEntities.CURSED_FROST_ENT.get()) return Wood.CURSED_FROST;
        return Wood.STARBLIGHT;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.ARMOR,2)
                .add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.22)
                .add(Attributes.FOLLOW_RANGE,16).add(Attributes.KNOCKBACK_RESISTANCE,.25);
    }
    public static boolean checkNaturalSpawn(EntityType<StarblightEntEntity> type,LevelAccessor world,
                                           EntitySpawnReason reason,BlockPos pos,RandomSource random) {
        if (reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.CHUNK_GENERATION) return true;
        Wood wood=woodForType(type);
        return world instanceof ServerLevelAccessor a && a.getLevel().dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                && world.getDifficulty()!=Difficulty.PEACEFUL
                && wood.habitat(world.getBiome(pos))
                && substrate(world.getBlockState(pos.below()))
                && openFeet(world.getBlockState(pos),wood)&&world.getBlockState(pos.above()).isAir()
                && (wood==Wood.STARBLIGHT||random.nextInt(4)==0
                    && (reason==EntitySpawnReason.CHUNK_GENERATION||woodlandSpawnHasRoom(a.getLevel(),pos)));
    }
    private static boolean woodlandSpawnHasRoom(ServerLevel level,BlockPos pos) {
        // Only natural spawn attempts query loaded entities; stop after the first match.
        // Worldgen can run off-thread and must not query the live entity manager.
        var nearby=new ArrayList<StarblightEntEntity>(1);
        Vec3 center=Vec3.atBottomCenterOf(pos);
        level.getEntities(EntityTypeTest.forClass(StarblightEntEntity.class),new AABB(pos).inflate(64),
                ent->ent.isAlive()&&ent.wood()!=Wood.STARBLIGHT&&ent.distanceToSqr(center)<64*64,nearby,1);
        return nearby.isEmpty();
    }
    private static boolean openFeet(BlockState state,Wood wood) {
        return state.isAir()||wood==Wood.CURSED_FROST&&state.is(Blocks.SNOW)&&state.getValue(SnowLayerBlock.LAYERS)==1;
    }
    private static boolean substrate(BlockState state) {
        return state.is(ModBlocks.STARBLIGHT_SOIL.get())||state.is(ModBlocks.AURORIAN_DIRT.get())||state.is(BlockTags.DIRT);
    }
    @Override protected void registerGoals() { goalSelector.addGoal(0,new FloatGoal(this)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);builder.define(PHASE,(byte)0);builder.define(START,0L);
    }
    public Phase phase() { return PHASES[Math.clamp(entityData.get(PHASE),0,PHASES.length-1)]; }
    public boolean isDisguised() { return phase()==Phase.DISGUISED&&isAlive(); }
    public float phaseTicks(float partialTick) { return Math.max(0,level().getGameTime()-entityData.get(START)+partialTick); }
    private void change(Phase phase,int elapsed) {
        entityData.set(START,level().getGameTime()-Math.max(0,elapsed));entityData.set(PHASE,(byte)phase.ordinal());
        attackConnected=false;launched=false;getNavigation().stop();refreshDimensions();
    }
    private void change(Phase phase) { change(phase,0); }
    private void wake(@Nullable LivingEntity target) {
        if (target!=null&&validTarget(target)) setTarget(target);
        int elapsed=phase()==Phase.HIDING?Math.max(0,20-(int)phaseTicks(0)):0;
        change(Phase.WAKING,elapsed);idleTicks=0;
        playSound(SoundEvents.CREAKING_ACTIVATE,.6F,1.6F);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);if (key.equals(PHASE)) refreshDimensions();
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(1,isDisguised()?1:1.5F).withEyeHeight(isDisguised()?.5F:1.05F);
    }
    @Override public boolean isPushable() { return !isDisguised()&&super.isPushable(); }
    @Override public boolean canBeCollidedWith(@Nullable Entity other) { return isDisguised(); }
    @Override public int getMaxSpawnClusterSize() { return 1; }
    private boolean validTarget(@Nullable LivingEntity target) {
        return target!=null&&target.isAlive()&&target.level()==level()&&target!=this
                && !(target instanceof StarblightEntEntity)&&distanceToSqr(target)<24*24
                && !(target instanceof Player p&&(p.isCreative()||p.isSpectator()))&&canAttack(target);
    }
    private @Nullable Player nearbyPlayer(ServerLevel level,double radius) {
        Player nearest=null;double best=radius*radius;
        for (Player player:level.players()) {
            double d=distanceToSqr(player);
            if (d<=best&&validTarget(player)&&getSensing().hasLineOfSight(player)) { nearest=player;best=d; }
        }
        return nearest;
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!isAlive()) return;
        if (attackCooldown>0) attackCooldown--;if (lungeCooldown>0) lungeCooldown--;
        Phase phase=phase();int ticks=(int)phaseTicks(0);
        if (phase==Phase.DISGUISED) {
            getNavigation().stop();setYHeadRot(getYRot());yBodyRot=getYRot();setXRot(0);
            setDeltaMovement(0,Math.min(0,getDeltaMovement().y),0);
            if (tickCount%10==0) {
                Player player=nearbyPlayer(level,5);
                if (player!=null||isInWater()||!substrate(level.getBlockState(blockPosition().below()))) wake(player);
            }
            return;
        }
        if (phase==Phase.HIDING&&tickCount%5==0) {
            Player player=nearbyPlayer(level,5);if (player!=null) { wake(player);return; }
        }
        if (phase==Phase.WAKING||phase==Phase.HIDING||phase==Phase.HURT) {
            getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);
            if (ticks>=phase.duration) change(phase==Phase.HIDING?Phase.DISGUISED:Phase.ACTIVE);
            return;
        }
        if (!validTarget(getTarget())) setTarget(null);
        if (phase==Phase.BITE||phase==Phase.LUNGE) {
            getNavigation().stop();LivingEntity target=getTarget();
            if (phase==Phase.LUNGE&&!launched&&ticks>=6) {
                launched=true;
                if (validTarget(target)&&onGround()) {
                    Vec3 direction=target.position().subtract(position()).multiply(1,0,1).normalize();
                    setDeltaMovement(direction.x*.7,.28,direction.z*.7);
                }
            }
            int impact=phase==Phase.BITE?8:10;
            if (!attackConnected&&ticks>=impact) {
                attackConnected=true;
                if (validTarget(target)&&isWithinMeleeAttackRange(target)&&getSensing().hasLineOfSight(target)) {
                    doHurtTarget(level,target);playSound(SoundEvents.WOOD_BREAK,.65F,1.35F);
                }
            }
            if (ticks>=phase.duration) change(Phase.ACTIVE);
            return;
        }
        if (tickCount%10==0&&getTarget()==null) setTarget(nearbyPlayer(level,16));
        LivingEntity target=getTarget();
        if (target!=null) {
            idleTicks=0;
            if (getSensing().hasLineOfSight(target)) unseenTicks=0;else if (++unseenTicks>100) { setTarget(null);return; }
            getLookControl().setLookAt(target,30,30);
            if (attackCooldown==0&&onGround()&&getSensing().hasLineOfSight(target)) {
                double distance=distanceToSqr(target);
                if (isWithinMeleeAttackRange(target)) { attackCooldown=30;change(Phase.BITE);return; }
                if (lungeCooldown==0&&distance>=2.2*2.2&&distance<=3.5*3.5) {
                    lungeCooldown=100;attackCooldown=36;change(Phase.LUNGE);return;
                }
            }
            if (tickCount%10==0) getNavigation().moveTo(target,1.1);
            return;
        }
        unseenTicks=0;
        if (++idleTicks>=100&&tickCount%20==0&&nearbyPlayer(level,10)==null) findDisguiseSpot(level);
    }
    private boolean disguiseSpot(ServerLevel level,BlockPos pos) {
        return level.hasChunkAt(pos)&&substrate(level.getBlockState(pos.below()))
                && openFeet(level.getBlockState(pos),wood)&&level.getBlockState(pos.above()).isAir()
                && level.noCollision(this,new AABB(pos.getX(),pos.getY(),pos.getZ(),pos.getX()+1,pos.getY()+1.5,pos.getZ()+1));
    }
    private void findDisguiseSpot(ServerLevel level) {
        BlockPos here=blockPosition();
        if (onGround()&&Math.abs(getY()-here.getY())<.05&&disguiseSpot(level,here)) {
            double x=here.getX()+.5,z=here.getZ()+.5;
            if (Math.hypot(getX()-x,getZ()-z)<=.35) {
                setPos(x,here.getY(),z);alignDisguiseFacing();setDeltaMovement(Vec3.ZERO);
                change(Phase.HIDING);return;
            }
            getNavigation().moveTo(x,here.getY(),z,.75);return;
        }
        // At most 25 loaded surface columns, only after the encounter ends.
        for (int radius=1;radius<=2;radius++) for (int dx=-radius;dx<=radius;dx++) for (int dz=-radius;dz<=radius;dz++) {
            if (Math.abs(dx)!=radius&&Math.abs(dz)!=radius) continue;
            BlockPos column=here.offset(dx,0,dz);if (!level.hasChunkAt(column)) continue;
            BlockPos pos=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column);
            if (Math.abs(pos.getY()-getY())<=2&&disguiseSpot(level,pos)) {
                getNavigation().moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,.75);return;
            }
        }
    }
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount) {
        boolean hurt=super.hurtServer(level,source,amount);
        if (hurt&&isAlive()) {
            LivingEntity attacker=source.getEntity() instanceof LivingEntity living&&validTarget(living)?living:null;
            if (phase()==Phase.DISGUISED||phase()==Phase.HIDING) wake(attacker);
            else { if (attacker!=null) setTarget(attacker);if (phase()!=Phase.WAKING) change(Phase.HURT); }
            idleTicks=0;
        }
        return hurt;
    }
    // Use vanilla's wooden creature hurt response; block.wood.hit reuses ordinary footsteps.
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.CREAKING_SWAY; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WOOD_BREAK; }
    @Override protected void playStepSound(BlockPos pos,BlockState state) { if (!isDisguised()) playSound(SoundEvents.WOOD_STEP,.2F,1.35F); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<StarblightEntEntity>("ent",0,state->{
            float partial=state.renderState().getPartialTick();RawAnimation animation;double time;
            if (!isAlive()) { animation=DEATH;time=(deathTime+partial)/20.; }
            else if (phase()==Phase.ACTIVE) {
                boolean moving=state.isMoving();animation=moving?WALK:IDLE;
                time=moving?(walkAnimation.position(partial)*.2)%.8:((tickCount+partial)/20.)%2;
            } else { animation=phase().animation;time=isDisguised()?0:phaseTicks(partial)/20.; }
            state.setAnimation(animation);state.controller().setAnimationTime(time);return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);output.putInt("EntPhase",phase().ordinal());
        output.putInt("EntPhaseTicks",(int)Math.min(phaseTicks(0),phase().duration));
        output.putInt("EntAttackCooldown",attackCooldown);output.putInt("EntLungeCooldown",lungeCooldown);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);Phase restored=PHASES[Math.clamp(input.getIntOr("EntPhase",0),0,PHASES.length-1)];
        // Never replay a saved attack against a newly acquired target.
        if (restored==Phase.BITE||restored==Phase.LUNGE||restored==Phase.HURT) restored=Phase.ACTIVE;
        change(restored,Math.clamp(input.getIntOr("EntPhaseTicks",0),0,restored.duration));
        attackCooldown=Math.clamp(input.getIntOr("EntAttackCooldown",0),0,100);
        lungeCooldown=Math.clamp(input.getIntOr("EntLungeCooldown",0),0,100);
        if (isDisguised()) alignDisguiseFacing();
    }
}
