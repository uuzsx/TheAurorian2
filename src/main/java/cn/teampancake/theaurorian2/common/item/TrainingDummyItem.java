package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.entity.TrainingDummyEntity;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TrainingDummyItem extends Item {

    public TrainingDummyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = new BlockPlaceContext(context).getClickedPos();
        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        AABB bounds = ModEntities.TRAINING_DUMMY.get().getDimensions().makeBoundingBox(spawnPos);
        if (!level.noCollision(null, bounds) || !level.getEntities(null, bounds).isEmpty()) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (level instanceof ServerLevel serverLevel) {
            Consumer<TrainingDummyEntity> config = EntityType.createDefaultStackConfig(serverLevel, stack, player);
            TrainingDummyEntity dummy = ModEntities.TRAINING_DUMMY.get().create(
                    serverLevel, config, pos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if (dummy == null) {
                return InteractionResult.FAIL;
            }

            float yRot = Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            dummy.snapTo(dummy.getX(), dummy.getY(), dummy.getZ(), yRot, 0.0F);
            dummy.setYHeadRot(yRot);
            dummy.setYBodyRot(yRot);
            serverLevel.addFreshEntity(dummy);
            level.playSound(
                    null, dummy.getX(), dummy.getY(), dummy.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            dummy.gameEvent(GameEvent.ENTITY_PLACE, player);
        }

        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
