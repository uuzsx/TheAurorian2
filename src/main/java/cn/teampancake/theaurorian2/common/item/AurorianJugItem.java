package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.block.WaterVesselBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;

/** A bottle-volume jug: one cauldron layer, never a placeable water source. */
public final class AurorianJugItem extends WaterVesselItem {
    public AurorianJugItem(WaterVesselBlock block, boolean filled, Properties properties) {
        super(block, filled, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        BlockState state = level.getBlockState(pos);
        boolean waterCauldron = state.is(Blocks.WATER_CAULDRON);
        if (filled) {
            if (!state.is(Blocks.CAULDRON) && !(waterCauldron && state.getValue(LayeredCauldronBlock.LEVEL) < 3)) {
                return InteractionResult.PASS;
            }
        } else if (!waterCauldron) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (filled) {
                level.setBlockAndUpdate(pos, waterCauldron ? state.cycle(LayeredCauldronBlock.LEVEL)
                        : Blocks.WATER_CAULDRON.defaultBlockState());
            } else {
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            }
            player.setItemInHand(context.getHand(), ItemUtils.createFilledResult(context.getItemInHand(), player,
                    new ItemStack(vessel.vesselItem(!filled))));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null, pos, filled ? SoundEvents.BOTTLE_EMPTY : SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
            level.gameEvent(player, filled ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (filled || player.isShiftKeyDown()) return InteractionResult.PASS;
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos) || !level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        // Like a glass bottle, filling samples water without draining the source or waterlogged block.
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1, 1);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(player.getItemInHand(hand), player,
                new ItemStack(vessel.vesselItem(true))));
    }
}
