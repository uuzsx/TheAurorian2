package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CloudberryBushBlock extends SweetBerryBushBlock {

    public CloudberryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.AURORIAN_GRASS_BLOCK.get())
                || state.is(ModBlocks.LIGHT_AURORIAN_GRASS_BLOCK.get())
                || state.is(ModBlocks.AURORIAN_DIRT.get());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean hasEnoughLight = level.getBrightness(LightLayer.BLOCK, pos) >= 7
                || level.getBrightness(LightLayer.SKY, pos) >= 7;
        if (hasEnoughLight && random.nextInt(5) == 0) {
            int age = state.getValue(AGE);
            BlockState nextState = state.setValue(AGE, age == MAX_AGE ? 0 : age + 1);
            level.setBlock(pos, nextState, UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(nextState));
        }
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ModBlocks.CLOUDBERRY.get());
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (entity instanceof LivingEntity && !entity.is(EntityType.FOX) && !entity.is(EntityType.BEE)) {
            entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75F, 0.8F));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(AGE) != MAX_AGE) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            int count = 2 + serverLevel.getRandom().nextInt(2);
            popResource(serverLevel, pos, new ItemStack(ModBlocks.CLOUDBERRY.get(), count));
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + serverLevel.getRandom().nextFloat() * 0.4F);
            BlockState harvestedState = state.setValue(AGE, 0);
            serverLevel.setBlock(pos, harvestedState, UPDATE_CLIENTS);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, harvestedState));
        }
        return InteractionResult.SUCCESS;
    }
}
