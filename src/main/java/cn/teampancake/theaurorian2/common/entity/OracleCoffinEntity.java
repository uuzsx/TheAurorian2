package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** A temporary hollow cage with six thin multipart colliders and one anchored execution target.
 * It changes no terrain, and releases its temporary controls on shatter or removal. */
public final class OracleCoffinEntity extends Entity {
    public static final int GROW_START = 8, ROOF_START = 32, SEAL = 40;
    public static final int SEALED_HOLD = 40, FINAL_STRIKE = SEAL + SEALED_HOLD;
    public static final int DISSOLVE_TICKS = 24, DURATION = FINAL_STRIKE + 10;
    public static final double HEIGHT = 36, HALF_WIDTH = 9.5;
    private static final List<Holder<MobEffect>> CONTROL=List.of(ModMobEffects.STUN,ModMobEffects.PARALYSIS);
    private static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, TheAurorian2.id("oracle_black_coffin"));
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(OracleCoffinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISSOLVE = SynchedEntityData.defineId(OracleCoffinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(OracleCoffinEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> VICTIM = SynchedEntityData.defineId(OracleCoffinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3fc> ANCHOR = SynchedEntityData.defineId(OracleCoffinEntity.class, EntityDataSerializers.VECTOR3);
    private RisingClockOracleEntity owner;
    private LivingEntity victim;
    private Vec3 victimAnchor;
    private final MobEffectInstance[] previousEffects=new MobEffectInstance[CONTROL.size()],appliedEffects=new MobEffectInstance[CONTROL.size()];
    private final Map<LivingEntity,Darkness> darkness=new HashMap<>();
    private record Darkness(MobEffectInstance previous,MobEffectInstance applied,long start,int duration) {}
    private long capturedAt;
    private int appliedDuration;
    private boolean released;
    private final Wall[] walls = new Wall[6];

    public OracleCoffinEntity(EntityType<? extends OracleCoffinEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
        for (int i=0;i<walls.length;i++) walls[i]=new Wall(this);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(OWNER, -1);
        data.define(START, 0L); data.define(DISSOLVE, FINAL_STRIKE); data.define(VICTIM,-1); data.define(ANCHOR,new Vector3f());
    }
    public float age(float partial) { return Math.max(0, level().getGameTime() - entityData.get(START)) + partial; }
    public int dissolveAt() { return entityData.get(DISSOLVE); }
    public double halfWidth() { return HALF_WIDTH; }
    public double height() { return HEIGHT; }
    public boolean channeling() { return !isRemoved() && age(0) < dissolveAt(); }
    public boolean sealed() { return !isRemoved() && age(0) >= SEAL && age(0) < dissolveAt(); }
    public boolean restrains(LivingEntity target) {
        return !isRemoved() && target.isAlive() && !target.isSpectator() && target.getId()==entityData.get(VICTIM) && age(0)<dissolveAt();
    }
    public Vec3 victimAnchor() {
        Vector3fc offset=entityData.get(ANCHOR);
        return position().add(offset.x(),offset.y(),offset.z());
    }
    public boolean obscures(LivingEntity target) {
        return target.level()==level() && sealed() && insideLiving(target);
    }
    public AABB volume() { return new AABB(getX()-halfWidth(), getY(), getZ()-halfWidth(), getX()+halfWidth(), getY()+height(), getZ()+halfWidth()); }
    private RisingClockOracleEntity caster() {
        if (owner == null && level().getEntity(entityData.get(OWNER)) instanceof RisingClockOracleEntity oracle) owner = oracle;
        return owner;
    }
    public static OracleCoffinEntity create(ServerLevel level, RisingClockOracleEntity caster, Vec3 origin, LivingEntity victim, Vec3 anchor, int age) {
        OracleCoffinEntity coffin = new OracleCoffinEntity(ModEntities.ORACLE_COFFIN.get(), level);
        coffin.owner = caster;
        coffin.victim=victim; coffin.victimAnchor=anchor;
        coffin.entityData.set(OWNER, caster.getId()); coffin.entityData.set(START, level.getGameTime()-age);
        coffin.entityData.set(VICTIM,victim==null?-1:victim.getId());
        Vec3 offset=anchor.subtract(origin);
        coffin.entityData.set(ANCHOR,new Vector3f((float)offset.x,(float)offset.y,(float)offset.z));
        coffin.setPos(origin);
        coffin.refreshBounds();
        if (!level.addFreshEntity(coffin)) return null;
        coffin.captureVictim();
        if(coffin.sealed()) coffin.updateDarkness(coffin.occupants(level));
        if(age==0) {
            coffin.sound(level, SoundEvents.BELL_RESONATE, .9F, .5F);
            coffin.sound(level, SoundEvents.BEACON_DEACTIVATE, .9F, .6F);
        }
        return coffin;
    }
    void resumeVictim(LivingEntity target) {
        if(victim!=null || released || !channeling()) return;
        victim=target; entityData.set(VICTIM,target.getId()); captureVictim();
    }
    private void captureVictim() {
        if(victim==null || !victim.isAlive()) return;
        capturedAt=level().getGameTime(); appliedDuration=Math.max(1,FINAL_STRIKE-(int)age(0)+1);
        for(int i=0;i<CONTROL.size();i++) {
            var previous=victim.getEffect(CONTROL.get(i));
            previousEffects[i]=previous==null?null:new MobEffectInstance(previous);
            var effect=new MobEffectInstance(CONTROL.get(i),appliedDuration,0,false,false,true);
            if(victim.addEffect(effect,owner)) appliedEffects[i]=victim.getEffect(CONTROL.get(i));
        }
        holdVictim();
    }
    private void holdVictim() {
        if(victim==null || victim.level()!=level() || !restrains(victim)) return;
        if(victim instanceof Mob mob) mob.getNavigation().stop();
        victim.setDeltaMovement(Vec3.ZERO); victim.fallDistance=0;
        if(victim.position().distanceToSqr(victimAnchor)>1.0E-8) victim.setPos(victimAnchor);
    }
    void releaseVictim() {
        if(released) return;
        released=true;
        for(var entry:darkness.entrySet()) releaseDarkness(entry.getKey(),entry.getValue());
        darkness.clear();
        if(victim==null) return;
        int elapsed=(int)Math.max(0,level().getGameTime()-capturedAt);
        for(int i=0;i<CONTROL.size();i++) releaseEffect(victim,CONTROL.get(i),previousEffects[i],appliedEffects[i],appliedDuration,elapsed);
    }
    private static void releaseEffect(LivingEntity living,Holder<MobEffect> type,MobEffectInstance previous,MobEffectInstance applied,int duration,int elapsed) {
        var current=living.getEffect(type);
        // Do not remove a stronger/longer effect added by another source during this cage.
        if(current==null || current!=applied || current.getAmplifier()!=0 || current.getDuration()>duration-elapsed+2) return;
        living.removeEffect(type);
        if(previous!=null && (previous.isInfiniteDuration() || previous.getDuration()>elapsed)) {
            int remaining=previous.isInfiniteDuration()?-1:previous.getDuration()-elapsed;
            living.addEffect(new MobEffectInstance(previous.getEffect(),remaining,previous.getAmplifier(),previous.isAmbient(),previous.isVisible(),previous.showIcon()));
        }
    }
    private void releaseDarkness(LivingEntity living,Darkness effect) {
        releaseEffect(living,MobEffects.DARKNESS,effect.previous(),effect.applied(),effect.duration(),(int)(level().getGameTime()-effect.start()));
    }
    private void updateDarkness(List<LivingEntity> occupants) {
        var iterator=darkness.entrySet().iterator();
        while(iterator.hasNext()) {
            var entry=iterator.next();
            if(entry.getKey().level()!=level() || !insideLiving(entry.getKey())) {
                releaseDarkness(entry.getKey(),entry.getValue()); iterator.remove();
            }
        }
        int duration=Math.max(1,FINAL_STRIKE-(int)age(0)+1);
        for(var living:occupants) if(!darkness.containsKey(living) || !living.hasEffect(MobEffects.DARKNESS)) {
            var previous=living.getEffect(MobEffects.DARKNESS);
            previous=previous==null?null:new MobEffectInstance(previous);
            boolean applied=living.addEffect(new MobEffectInstance(MobEffects.DARKNESS,duration,0,false,false,true),owner);
            darkness.put(living,new Darkness(previous,applied?living.getEffect(MobEffects.DARKNESS):null,level().getGameTime(),duration));
        }
    }
    private void refreshBounds() {
        if (walls==null || walls[0]==null) return;
        AABB b = volume(); double t = .12;
        setBoundingBox(b);
        for (int i=0;i<walls.length;i++) {
            AABB shape=switch(i+1) {
            case 1 -> new AABB(b.minX-t,b.minY,b.minZ-t,b.minX,b.maxY,b.maxZ+t);
            case 2 -> new AABB(b.maxX,b.minY,b.minZ-t,b.maxX+t,b.maxY,b.maxZ+t);
            case 3 -> new AABB(b.minX,b.minY,b.minZ-t,b.maxX,b.maxY,b.minZ);
            case 4 -> new AABB(b.minX,b.minY,b.maxZ,b.maxX,b.maxY,b.maxZ+t);
            case 5 -> new AABB(b.minX,b.maxY,b.minZ,b.maxX,b.maxY+t,b.maxZ);
                default -> new AABB(b.minX,b.minY-.12,b.minZ,b.maxX,b.minY,b.maxZ);
            };
            walls[i].setPos(shape.getCenter()); walls[i].setBoundingBox(shape);
        }
    }
    @Override public boolean isMultipartEntity() { return true; }
    @Override public Wall[] getParts() { return walls; }
    @Override public void setId(int id) {
        super.setId(id);
        if (walls!=null) for (int i=0;i<walls.length;i++) if (walls[i]!=null) walls[i].setId(id+i+1);
    }
    @Override public void tick() {
        super.tick(); if (tickCount==1) refreshBounds();
        if (!(level() instanceof ServerLevel server)) return;
        int age=(int)age(0);
        if (age<dissolveAt() && (caster()==null || !owner.isFinalCasting() || owner.isRemoved() || owner.level()!=server)) cancel();
        if (age>=dissolveAt()+DISSOLVE_TICKS) { discard(); return; }
        if (age==FINAL_STRIKE && dissolveAt()==FINAL_STRIKE) finishStrike(server);
        if (age>=dissolveAt()) return;
        holdVictim();
        // Only the two-second sealed phase scans its local volume, five times per second.
        if(sealed() && (age-SEAL)%4==0) {
            var occupants=occupants(server);
            updateDarkness(occupants);
            if(age==SEAL+20) hurt(server,occupants,0,.1F);
        }
        if (age==GROW_START) sound(server, SoundEvents.BEACON_AMBIENT, 1.1F, .5F);
        if (age==SEAL) sound(server, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), .9F, .6F);
        if (age==SEAL+7) sound(server, SoundEvents.NOTE_BLOCK_HAT.value(), .8F, .5F);
    }
    private void finishStrike(ServerLevel server) {
        if (isRemoved() || dissolveAt()!=FINAL_STRIKE || (int)age(0)!=FINAL_STRIKE || caster()==null || !owner.isFinalCasting() || owner.isRemoved()) return;
        var occupants=occupants(server);
        // Settle the second full second before the separate simultaneous fracture hit.
        hurt(server,occupants,0,.1F);
        float damage=switch(server.getDifficulty()) { case PEACEFUL -> 0; case EASY -> 30; case NORMAL -> 35; case HARD -> 40; };
        hurt(server,occupants,damage,0);
        releaseVictim();
        sound(server, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.2F, .65F);
        sound(server, SoundEvents.GLASS_BREAK, 1.8F, .55F);
        sound(server, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 1F, .5F);
        owner.finishFinalCoffin();
    }
    private List<LivingEntity> occupants(ServerLevel server) {
        return server.getEntitiesOfClass(LivingEntity.class,volume(),this::insideLiving);
    }
    private boolean insideLiving(LivingEntity living) {
        return living!=caster() && living.isAlive() && !living.isSpectator()
                && getBoundingBox().contains(living.getBoundingBox().getCenter());
    }
    private void hurt(ServerLevel server, List<LivingEntity> victims, float amount,float maximumHealthFraction) {
        if (server.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL) return;
        DamageSource source=new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE),this,owner);
        for (LivingEntity victim:victims) if(victim.isAlive()) victim.hurtServer(server,source,amount+victim.getMaxHealth()*maximumHealthFraction);
    }
    public void cancel() {
        if (!level().isClientSide() && !isRemoved() && age(0)<dissolveAt()) {
            entityData.set(DISSOLVE,(int)age(0)); releaseVictim();
        }
    }
    @Override public void remove(RemovalReason reason) { if(!level().isClientSide()) releaseVictim(); super.remove(reason); }
    private void sound(ServerLevel level, net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
        level.playSound(null,getX(),getY()+1,getZ(),event,SoundSource.HOSTILE,volume,pitch);
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance<160*160; }
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
    @Override protected void readAdditionalSaveData(ValueInput input) {}
    /** NeoForge includes these shapes across section boundaries without widening global queries. */
    public static final class Wall extends PartEntity<OracleCoffinEntity> {
        private Wall(OracleCoffinEntity parent) { super(parent); noPhysics=true; setNoGravity(true); }
        @Override public boolean canBeCollidedWith(Entity other) {
            OracleCoffinEntity parent=getParent(); RisingClockOracleEntity caster=parent.caster();
            return parent.sealed() && caster!=null && caster.isFinalCasting() && other!=caster
                    && other instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
        }
        @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
        @Override protected void defineSynchedData(SynchedEntityData.Builder data) {}
        @Override protected void addAdditionalSaveData(ValueOutput output) {}
        @Override protected void readAdditionalSaveData(ValueInput input) {}
        @Override public boolean shouldBeSaved() { return false; }
    }
}
