package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.OracleDisplacedTerrainBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A short-lived terrain reservation. Its saved block entity owns the original block. */
public final class OracleDisplacedTerrainBlock extends BaseEntityBlock implements LiquidBlockContainer {
    public static final MapCodec<OracleDisplacedTerrainBlock> CODEC = simpleCodec(OracleDisplacedTerrainBlock::new);
    public static final BooleanProperty LIFTED = BooleanProperty.create("lifted");
    public OracleDisplacedTerrainBlock(Properties properties) {
        super(properties);
        // Old saved reservations had no property and were already airborne.
        registerDefaultState(stateDefinition.any().setValue(LIFTED,true));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(LIFTED); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OracleDisplacedTerrainBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LIFTED) ? Shapes.empty() : Shapes.block();
    }
    @Override protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) { return Shapes.block(); }
    @Override public boolean canPlaceLiquid(net.minecraft.world.entity.LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, net.minecraft.world.level.material.Fluid fluid) { return false; }
    @Override public boolean placeLiquid(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state, net.minecraft.world.level.material.FluidState fluid) { return false; }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof OracleDisplacedTerrainBlockEntity reservation) reservation.restore(level);
    }
}
