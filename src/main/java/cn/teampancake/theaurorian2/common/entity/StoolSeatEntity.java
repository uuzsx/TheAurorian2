package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.block.AurorianStoolBlock;
import cn.teampancake.theaurorian2.common.block.AurorianChairBlock;
import cn.teampancake.theaurorian2.common.block.AurorianBenchBlock;
import cn.teampancake.theaurorian2.common.block.PairedFurnitureBlock;
import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Invisible, immobile mount that exists only while a stool, chair or bench seat is occupied. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class StoolSeatEntity extends Entity {
    public StoolSeatEntity(EntityType<? extends StoolSeatEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && (!isVehicle() || !getFirstPassenger().isAlive()
                || !hasSeatBlock())) {
            // Dismount while the mount is still alive so vanilla uses our safe locations.
            ejectPassengers();
            discard();
        }
    }

    private boolean hasSeatBlock() {
        var state = level().getBlockState(blockPosition());
        return state.getBlock() instanceof AurorianStoolBlock
                || state.getBlock() instanceof AurorianBenchBlock
                || state.getBlock() instanceof AurorianChairBlock && !state.getValue(PairedFurnitureBlock.SECOND);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getVehicle() instanceof StoolSeatEntity seat) {
            // This event runs before PlayerList saves the disconnecting player's position.
            event.getEntity().stopRiding();
            seat.discard();
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && getPassengers().isEmpty();
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return position().add(0, AurorianStoolBlock.seatHeight(level().getBlockState(blockPosition())), 0);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Vec3 safe = DismountHelper.findSafeDismountLocation(
                    passenger.getType(), level(), blockPosition().relative(direction), true);
            if (safe != null) return safe;
        }
        Vec3 above = DismountHelper.findSafeDismountLocation(
                passenger.getType(), level(), blockPosition().above(), true);
        return above != null ? above : getPassengerRidingPosition(passenger);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {}

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}
}
