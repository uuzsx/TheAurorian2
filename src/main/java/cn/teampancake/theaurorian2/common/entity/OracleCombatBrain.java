package cn.teampancake.theaurorian2.common.entity;

import static cn.teampancake.theaurorian2.common.entity.RisingClockOracleEntity.*;

import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Decisions run between casts. The owner and its mirror share control and opening reservations. */
final class OracleCombatBrain {
    static final int CONTROL_RESPITE = 30, PARTNER_STAGGER = 8;
    private static final int[] ATTACKS = {LIGHTNING, BLAST, FLAME, GRAVITY, CHRONO, REPULSOR, SHIELD};
    private final RisingClockOracleEntity owner;
    private final long[] readyAt = new long[COFFIN];
    private final int[] weights = new int[COFFIN];
    private int lastSpell = IDLE, previousSpell = IDLE, nextSpell = IDLE, finalSpell = IDLE;
    private UUID planTarget;
    private long planExpires, controlReadyAt, partnerReadyAt, hurtAt = Long.MIN_VALUE;
    private long localReadyAt;
    private float recentDamage;

    OracleCombatBrain(RisingClockOracleEntity owner) { this.owner = owner; }
    private OracleCombatBrain coordinator() { return owner.combatLeader().combatBrain(); }
    boolean canStart(long now) { return now >= localReadyAt && now >= coordinator().partnerReadyAt; }
    void delayStart(int ticks) { localReadyAt=owner.level().getGameTime()+ticks; }
    private boolean pressured(long now) { return recentDamage >= 8 && now-hurtAt <= 40; }

    void hurt(float damage) {
        long now = owner.level().getGameTime();
        if (hurtAt == Long.MIN_VALUE || now-hurtAt > 40) recentDamage = 0;
        recentDamage = Math.min(40,recentDamage+damage);
        hurtAt = now;
    }
    void controlApplied(int remainingTicks) {
        var shared = coordinator();
        shared.controlReadyAt = Math.max(shared.controlReadyAt,owner.level().getGameTime()+remainingTicks+CONTROL_RESPITE);
    }
    void clearPlan() { nextSpell=finalSpell=IDLE; planTarget=null; }

    int choose(ServerLevel level, LivingEntity target, boolean wardReady) {
        long now = level.getGameTime();
        if (!canStart(now)) return IDLE;
        boolean enhanced = !owner.isMirror() && owner.rage()==MAX_RAGE;
        boolean visible = owner.hasLineOfSight(target);
        double distance = owner.distanceTo(target);
        double flameDistance = owner.spellOrigin().distanceTo(target.getBoundingBox().getCenter());
        boolean underControl = target.hasEffect(ModMobEffects.STUN) || target.hasEffect(ModMobEffects.PARALYSIS);
        // Existing control from this encounter or another source must end before another setup.
        var stun=target.getEffect(ModMobEffects.STUN);
        var paralysis=target.getEffect(ModMobEffects.PARALYSIS);
        int controlRemaining=Math.max(stun==null?0:stun.getDuration(),paralysis==null?0:paralysis.getDuration());
        if (controlRemaining>0) controlApplied(controlRemaining);
        boolean controlReady = now >= coordinator().controlReadyAt && !underControl;
        var floor = target.getOnPos();
        var ground = level.getBlockState(floor);
        boolean grounded = target.onGround() && !target.isPassenger() && !ground.hasBlockEntity()
                && ground.getDestroySpeed(level,floor)>=0 && ground.getFluidState().isEmpty()
                && ground.isCollisionShapeFullBlock(level,floor);
        int cluster=0, footprint=0, aligned=0;
        Vec3 origin = owner.spellOrigin(), aim = target.getBoundingBox().getCenter().subtract(origin).normalize();
        // A seven-block neighborhood is sufficient for tactical bonuses. A lone target needs none.
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(7),
                e -> e!=target && owner.validTarget(e) && target.distanceToSqr(e)<=49)) {
            cluster = Math.min(4,cluster+1);
            if (other.onGround() && Math.abs(other.getX()-target.getX()) <= (enhanced?2.5:1.5)
                    && Math.abs(other.getZ()-target.getZ()) <= (enhanced?2.5:1.5)
                    && Math.abs(other.getY()-target.getY())<3) footprint=Math.min(4,footprint+1);
            Vec3 delta = other.getBoundingBox().getCenter().subtract(origin);
            double along = delta.dot(aim), radius = (enhanced?1.3:.65)+other.getBbWidth()*.5;
            if (along>=0 && along<=64 && delta.lengthSqr()-along*along <= radius*radius) aligned=Math.min(4,aligned+1);
        }
        java.util.Arrays.fill(weights,0);
        boolean moving = target.getDeltaMovement().horizontalDistanceSqr()>.015;
        weights[LIGHTNING] = (distance>26?42:24)+cluster*7+(moving?8:0);
        if (visible) {
            if (distance<=26) weights[BLAST]=30+cluster*6+(underControl?12:0);
            if (flameDistance<=OracleArcaneEntity.FLAME_RANGE) weights[FLAME]=36+(moving?10:0);
            weights[REPULSOR]=(distance>26?40:24)+aligned*16+(underControl?24:0)
                    +(enhanced && target.getMaxHealth()>=80?20:0);
        }
        if (grounded && controlReady) weights[GRAVITY]=18+footprint*12;
        if (controlReady && (enhanced || visible && distance<=26)) weights[CHRONO]=16+cluster*8;
        if (wardReady && owner.wardCount()==0) weights[SHIELD]=pressured(now)?100:owner.getHealth()<owner.getMaxHealth()*.4F?28:8;
        if (owner.rage()>=6 && !enhanced) { weights[FLAME]+=weights[FLAME]>0?8:0;weights[REPULSOR]+=weights[REPULSOR]>0?6:0; }
        for (int spell : ATTACKS) {
            if (weights[spell]==0) continue;
            if (now<readyAt[spell] || enhanced && spell==FLAME) weights[spell]=0;
            else {
                if (spell==lastSpell) weights[spell]=Math.max(1,weights[spell]/5);
                else if (spell==previousSpell) weights[spell]=Math.max(1,weights[spell]/2);
            }
        }
        if (planTarget==null || !planTarget.equals(target.getUUID()) || now>planExpires) clearPlan();
        if (weights[SHIELD]>0 && pressured(now)) { clearPlan();return SHIELD; }
        if (nextSpell!=IDLE) {
            int planned = nextSpell;
            if (planned==FLAME && weights[FLAME]==0 && weights[BLAST]>0) planned=BLAST;
            if (weights[planned]>0) { nextSpell=planned;return planned; }
            clearPlan();
        }
        var partner = owner.combatPartner();
        if (partner!=null && partner.action()>IDLE && partner.action()<COFFIN && weights[partner.action()]>0)
            weights[partner.action()]=Math.max(1,weights[partner.action()]/3);
        int total=0;
        for (int spell:ATTACKS) total+=weights[spell];
        if (total==0) return IDLE;
        int choice=owner.getRandom().nextInt(total);
        for (int spell:ATTACKS) {choice-=weights[spell];if(choice<0)return spell;}
        return IDLE;
    }

    void started(int spell, LivingEntity target, boolean enhanced) {
        long now=owner.level().getGameTime();
        var shared=coordinator();
        shared.partnerReadyAt=now+PARTNER_STAGGER;
        int extra=switch(spell) {
            case LIGHTNING->16;case BLAST->16;case FLAME->32;case GRAVITY->50;
            case CHRONO->150;case REPULSOR->40;default->0;
        };
        readyAt[spell]=now+castDuration(spell,enhanced)+extra;
        if (spell==GRAVITY) controlApplied(OracleSpellEntity.IMPACT+(enhanced?20:0));
        if (spell==CHRONO) controlApplied(CHANNEL_WINDUP+OracleArcaneEntity.CLOCK_CONTROL_START+OracleArcaneEntity.CLOCK_CONTROL_DURATION);
        previousSpell=lastSpell;lastSpell=spell;
        if (nextSpell==spell && target.getUUID().equals(planTarget)) {
            nextSpell=finalSpell;finalSpell=IDLE;return;
        }
        clearPlan();
        planTarget=target.getUUID();planExpires=now+200;
        // Short plans are revalidated after each action, including range, cover and a lost target.
        switch(spell) {
            case CHRONO->{nextSpell=LIGHTNING;finalSpell=REPULSOR;}
            case GRAVITY->nextSpell=BLAST;
            case GLIDE->nextSpell=FLAME;
            case SHIELD->nextSpell=owner.distanceTo(target)<14?FLAME:BLAST;
            case LIGHTNING->{if(owner.getRandom().nextFloat()<.7F)nextSpell=BLAST;}
            default->{ }
        }
    }

    void save(ValueOutput out) {
        long now=owner.level().getGameTime();
        int[] remaining=new int[readyAt.length];
        for(int i=0;i<remaining.length;i++)remaining[i]=(int)Math.clamp(readyAt[i]-now,0,240);
        out.putIntArray("SpellCooldowns",remaining);
        out.putInt("ControlRecovery",(int)Math.clamp(controlReadyAt-now,0,240));
        out.putInt("LastSpell",lastSpell);out.putInt("PreviousSpell",previousSpell);
    }
    void load(ValueInput in) {
        long now=owner.level().getGameTime();
        int[] remaining=in.getIntArray("SpellCooldowns").orElseGet(()->new int[0]);
        for(int i=0;i<Math.min(remaining.length,readyAt.length);i++)readyAt[i]=now+Mth.clamp(remaining[i],0,240);
        controlReadyAt=now+Mth.clamp(in.getIntOr("ControlRecovery",0),0,240);
        lastSpell=Mth.clamp(in.getIntOr("LastSpell",IDLE),IDLE,REPULSOR);
        previousSpell=Mth.clamp(in.getIntOr("PreviousSpell",IDLE),IDLE,REPULSOR);
        clearPlan();
    }
}
