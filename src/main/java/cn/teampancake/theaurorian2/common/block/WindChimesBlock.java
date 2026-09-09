package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.WindChimesBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Ceiling decoration. Audio uses scheduled block ticks, not an always-ticking block entity. */
public final class WindChimesBlock extends BaseEntityBlock {
    public static final MapCodec<WindChimesBlock> CODEC = simpleCodec(WindChimesBlock::new);
    private static final VoxelShape SHAPE = box(5, -2, 5, 11, 16, 11);

    public WindChimesBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WindChimesBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                && context.getLevel().getFluidState(context.getClickedPos()).isEmpty()
                ? defaultBlockState() : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == Direction.UP && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean amethyst = state.is(ModBlocks.AMETHYST_WIND_CHIMES.get());
        if (level.getNearestPlayer(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 25, false) != null) {
            level.playSound(null, pos, (amethyst ? ModSoundEvents.AMETHYST_WIND_CHIMES
                    : ModSoundEvents.BAMBOO_WIND_CHIMES).get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        // Supplied audio lasts 60.13 / 29.33 seconds; leave a small gap to avoid overlap.
        level.scheduleTick(pos, this, amethyst ? 1220 : 600);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) == 0) level.addParticle(ParticleTypes.WAX_OFF,
                pos.getX() + .5, pos.getY() + .4, pos.getZ() + .5,
                (random.nextDouble() - .5) * .03, .015, (random.nextDouble() - .5) * .03);
    }
}
