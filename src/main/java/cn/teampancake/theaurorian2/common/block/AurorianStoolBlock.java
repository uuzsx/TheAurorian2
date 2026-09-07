package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.entity.StoolSeatEntity;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianStoolBlock extends Block {
    public static final MapCodec<AurorianStoolBlock> CODEC = simpleCodec(AurorianStoolBlock::new);
    public static final double SEAT_HEIGHT = 0.75;
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 9, 0, 16, 12, 16),
            box(1, 0, 1, 4, 9, 4), box(12, 0, 1, 15, 9, 4),
            box(1, 0, 12, 4, 9, 15), box(12, 0, 12, 15, 9, 15),
            box(4, 3, 1.5, 12, 5, 3.5), box(4, 3, 12.5, 12, 5, 14.5),
            box(1.5, 3, 4, 3.5, 5, 12), box(12.5, 3, 4, 14.5, 5, 12));

    public AurorianStoolBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static VoxelShape collisionShape() { return SHAPE; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        return trySit(level, pos, player);
    }

    public static InteractionResult trySit(Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown() || player.isSpectator() || player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        if (!serverLevel.getEntitiesOfClass(StoolSeatEntity.class, new AABB(pos),
                seat -> seat.isAlive() && seat.blockPosition().equals(pos) && seat.isVehicle()).isEmpty()) {
            return InteractionResult.FAIL;
        }
        StoolSeatEntity seat = new StoolSeatEntity(ModEntities.STOOL_SEAT.get(), level);
        seat.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        double top = pos.getY() + SEAT_HEIGHT + player.getBbHeight() - player.getVehicleAttachmentPoint(seat).y;
        double radius = player.getBbWidth() / 2.0;
        if (!level.noCollision(player, new AABB(seat.getX() - radius, pos.getY() + SEAT_HEIGHT + 0.001,
                seat.getZ() - radius, seat.getX() + radius, top, seat.getZ() + radius))) {
            return InteractionResult.FAIL;
        }
        if (!serverLevel.addFreshEntity(seat)) return InteractionResult.FAIL;
        // A freshly validated empty stool may be used again immediately after Shift.
        if (!player.startRiding(seat, true, true)) {
            seat.discard();
            return InteractionResult.FAIL;
        }
        seat.positionRider(player);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof AurorianChairBlock && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            float yaw = state.getValue(PairedFurnitureBlock.FACING).toYRot();
            player.setYHeadRot(yaw);
            serverPlayer.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, player.getXRot());
        }
        return InteractionResult.SUCCESS;
    }
}
