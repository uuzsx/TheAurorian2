package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible support cells that give the centered three-by-three base real collision coverage. */
public final class PurificationAltarBasePartBlock extends Block {

    public static final MapCodec<PurificationAltarBasePartBlock> CODEC =
            simpleCodec(PurificationAltarBasePartBlock::new);
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 2);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 2);
    private static final VoxelShape[][] COLLISION_SHAPES = {
        {
            Shapes.or(
                    box(1, 0, 1, 9, 12, 9),
                    box(8, 0, 8, 16, 16, 16)),
            box(8, 0, 0, 16, 16, 16),
            Shapes.or(
                    box(1, 0, 7, 9, 12, 15),
                    box(8, 0, 0, 16, 16, 8))
        },
        {
            box(0, 0, 8, 16, 16, 16),
            Shapes.block(),
            box(0, 0, 0, 16, 16, 8)
        },
        {
            Shapes.or(
                    box(7, 0, 1, 15, 12, 9),
                    box(0, 0, 8, 8, 16, 16)),
            box(0, 0, 0, 8, 16, 16),
            Shapes.or(
                    box(7, 0, 7, 15, 12, 15),
                    box(0, 0, 0, 8, 16, 8))
        }
    };

    public PurificationAltarBasePartBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OFFSET_X, 1).setValue(OFFSET_Z, 1));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[
                state.getValue(OFFSET_X)][state.getValue(OFFSET_Z)];
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[
                state.getValue(OFFSET_X)][state.getValue(OFFSET_Z)];
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // This block is only an invisible collision cell; the visible master owns the particles.
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            // Breaking any footprint cell removes the master and its other hidden cells.
            // Creative players must not receive a survival-style base drop from a part.
            level.destroyBlock(masterPos(pos, state), !player.getAbilities().instabuild, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos masterPos = masterPos(pos, state);
        if (level.getBlockState(masterPos).is(ModBlocks.PURIFICATION_ALTAR_BASE.get())) {
            level.destroyBlock(masterPos, true, null);
        }
    }

    public static BlockPos masterPos(BlockPos partPos, BlockState state) {
        return partPos.offset(1 - state.getValue(OFFSET_X), 0, 1 - state.getValue(OFFSET_Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Z);
    }

    private static VoxelShape box(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ) {
        return Shapes.box(
                minX / 16.0D, minY / 16.0D, minZ / 16.0D,
                maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
    }
}
