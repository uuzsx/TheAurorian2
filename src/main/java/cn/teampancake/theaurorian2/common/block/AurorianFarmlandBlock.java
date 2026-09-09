package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import org.jspecify.annotations.Nullable;

/** Vanilla farmland behavior, with soil conversion kept within Aurorian blocks. */
public final class AurorianFarmlandBlock extends FarmlandBlock {
    public static final MapCodec<FarmlandBlock> CODEC = simpleCodec(AurorianFarmlandBlock::new);

    public AurorianFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FarmlandBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? defaultBlockState() : ModBlocks.AURORIAN_DIRT.get().defaultBlockState();
    }

    // Vanilla's conversion helper is static and hardcodes minecraft:dirt. Override only its
    // callers, retaining vanilla hydration bounds, crop preservation and NeoForge hooks.
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToAurorianDirt(null, state, level, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (isNearWater(state, level, pos) || level.isRainingAt(pos.above())) {
            if (moisture < MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
            }
        } else if (moisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
        } else if (!level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND)) {
            turnToAurorianDirt(null, state, level, pos);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (level instanceof ServerLevel serverLevel && CommonHooks.onFarmlandTrample(
                serverLevel, pos, ModBlocks.AURORIAN_DIRT.get().defaultBlockState(), fallDistance, entity)) {
            turnToAurorianDirt(entity, state, level, pos);
        }
        // Calling FarmlandBlock.fallOn would also run its vanilla-dirt conversion.
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    private static void turnToAurorianDirt(@Nullable Entity source, BlockState state, Level level, BlockPos pos) {
        BlockState dirt = pushEntitiesUp(state, ModBlocks.AURORIAN_DIRT.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, dirt);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(source, dirt));
    }

    private static boolean isNearWater(BlockState state, LevelReader level, BlockPos pos) {
        for (BlockPos waterPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (state.canBeHydrated(level, pos, level.getFluidState(waterPos), waterPos)) {
                return true;
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}
