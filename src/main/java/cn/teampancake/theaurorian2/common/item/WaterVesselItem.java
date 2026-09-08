package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.block.WaterVesselBlock;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/** Bucket-volume water transport and shared sneaking placement for water vessels. */
public class WaterVesselItem extends BlockItem {
    protected final WaterVesselBlock vessel;
    protected final boolean filled;

    public WaterVesselItem(WaterVesselBlock block, boolean filled, Properties properties) {
        super(block, properties);
        this.vessel = block;
        this.filled = filled;
        block.bindItem(this, filled);
    }

    @Override public void registerBlocks(Map<Block, Item> map, Item item) {
        if (!filled) super.registerBlocks(map, item);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!filled && state.is(Blocks.WATER_CAULDRON)) {
            return CauldronInteractions.fillBucket(state, level, pos, player, context.getHand(), context.getItemInHand(),
                    new ItemStack(vessel.vesselItem(true)), s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL);
        }
        if (filled && (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)
                || state.is(Blocks.LAVA_CAULDRON) || state.is(Blocks.POWDER_SNOW_CAULDRON))) {
            if (!level.isClientSide()) {
                player.setItemInHand(context.getHand(), ItemUtils.createFilledResult(context.getItemInHand(), player,
                        new ItemStack(vessel.vesselItem(false))));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(this));
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1);
                level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null ? null : state.setValue(WaterVesselBlock.FILLED, filled);
    }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        var hit = getPlayerPOVHitResult(level, player, filled ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        BlockPos adjacent = pos.relative(hit.getDirection());
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(adjacent, hit.getDirection(), stack)) {
            return InteractionResult.FAIL;
        }
        BlockState state = level.getBlockState(pos);
        if (!filled) {
            // Check before pickupBlock: lava, powder snow and mod fluids must never be destroyed.
            if (!state.getFluidState().is(Fluids.WATER) || !state.getFluidState().isSource()
                    || !(state.getBlock() instanceof BucketPickup pickup)) return InteractionResult.FAIL;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack taken = pickup.pickupBlock(player, level, pos, state);
            if (taken.isEmpty()) return InteractionResult.FAIL;
            pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1, 1));
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            ItemStack water = new ItemStack(vessel.vesselItem(true));
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, water);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(stack, player, water));
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos target = state.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, state, Fluids.WATER) ? pos : adjacent;
        // Reuse vanilla/NeoForge placement, including waterlogging and evaporating dimensions.
        if (!((BucketItem) Items.WATER_BUCKET).emptyContents(player, level, target, hit, stack)) return InteractionResult.FAIL;
        if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, target, stack);
        player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack empty = player.hasInfiniteMaterials() ? stack : new ItemStack(vessel.vesselItem(false));
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(stack, player, empty));
    }
}
