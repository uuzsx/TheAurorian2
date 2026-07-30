package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.FireplaceBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStats;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class FireplaceBlock extends AbstractFurnaceBlock {

    public static final MapCodec<FireplaceBlock> CODEC = simpleCodec(FireplaceBlock::new);
    private static final PartOffset[] PARTS = {
        new PartOffset(0, 0, 0),
        new PartOffset(-1, 0, 0),
        new PartOffset(1, 0, 0),
        new PartOffset(-1, 1, 0),
        new PartOffset(0, 1, 0),
        new PartOffset(1, 1, 0),
        new PartOffset(-1, 0, 1),
        new PartOffset(0, 0, 1),
        new PartOffset(1, 0, 1),
        new PartOffset(-1, 1, 1),
        new PartOffset(0, 1, 1),
        new PartOffset(1, 1, 1)
    };
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    private static final ModelBox[] COLLISION_BOXES = {
        new ModelBox(0.0, 22.0, 16.0, 16.0, 27.0, 32.0),
        new ModelBox(0.0, 16.0, 16.0, 16.0, 22.0, 25.0),
        new ModelBox(0.0, 0.0, 16.0, 16.0, 3.0, 29.0),
        new ModelBox(-8.0, 0.0, 7.0, 24.0, 3.0, 16.0),
        new ModelBox(0.0, 0.0, 29.0, 16.0, 22.0, 32.0),
        new ModelBox(-4.0, 1.0, 12.0, 20.0, 9.0, 16.0),
        new ModelBox(-10.0, 27.0, 12.0, 26.0, 32.0, 32.0),
        new ModelBox(-8.0, 0.0, 16.0, 0.0, 27.0, 32.0),
        new ModelBox(-8.0, 3.0, 14.0, -2.0, 27.0, 16.0),
        new ModelBox(16.0, 0.0, 16.0, 24.0, 27.0, 32.0),
        new ModelBox(18.0, 3.0, 14.0, 24.0, 27.0, 16.0)
    };
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(
            Block.box(0.0, 22.0, 16.0, 16.0, 27.0, 32.0),
            Block.box(0.0, 16.0, 16.0, 16.0, 22.0, 25.0),
            Block.box(0.0, 0.0, 16.0, 16.0, 3.0, 29.0),
            Block.box(-8.0, 0.0, 7.0, 24.0, 3.0, 16.0),
            Block.box(0.0, 0.0, 29.0, 16.0, 22.0, 32.0),
            Block.box(-4.0, 1.0, 12.0, 20.0, 9.0, 16.0),
            Block.box(-10.0, 27.0, 12.0, 26.0, 32.0, 32.0),
            Block.box(-8.0, 0.0, 16.0, 0.0, 27.0, 32.0),
            Block.box(-8.0, 3.0, 14.0, -2.0, 27.0, 16.0),
            Block.box(16.0, 0.0, 16.0, 24.0, 27.0, 32.0),
            Block.box(18.0, 3.0, 14.0, 24.0, 27.0, 16.0)));
    private static final Map<Direction, VoxelShape[]> COLLISION_SHAPES = createCollisionShapes();

    public FireplaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FireplaceBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShape(state.getValue(FACING), 0);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        Level level = context.getLevel();
        BlockPos masterPos = context.getClickedPos();
        Direction facing = state.getValue(FACING);
        for (int part = 1; part < PARTS.length; part++) {
            BlockPos partPos = partPos(masterPos, facing, part);
            if (!level.isInWorldBounds(partPos)
                    || !level.getWorldBorder().isWithinBounds(partPos)
                    || !level.getBlockState(partPos).canBeReplaced(context)) {
                return null;
            }
        }

        return state;
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockState partState = ModBlocks.FIREPLACE_PART.get().defaultBlockState().setValue(
                FireplacePartBlock.FACING, facing);
        for (int part = 1; part < PARTS.length; part++) {
            level.setBlock(
                    partPos(pos, facing, part),
                    partState.setValue(FireplacePartBlock.PART, part),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        Direction facing = state.getValue(FACING);
        for (int part = 1; part < PARTS.length; part++) {
            BlockPos partPos = partPos(pos, facing, part);
            BlockState partState = level.getBlockState(partPos);
            if (partState.is(ModBlocks.FIREPLACE_PART.get())
                    && partState.getValue(FireplacePartBlock.FACING) == facing
                    && partState.getValue(FireplacePartBlock.PART) == part) {
                level.setBlock(
                        partPos,
                        Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireplaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.FIREPLACE.get(),
                (tickLevel, pos, tickState, fireplace) ->
                        AbstractFurnaceBlockEntity.serverTick(serverLevel, pos, tickState, fireplace));
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof FireplaceBlockEntity fireplace) {
            player.openMenu(fireplace);
            player.awardStat(ModStats.INTERACT_WITH_FIREPLACE.get());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        Direction back = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + back.getStepX();
        double y = pos.getY() + 2.1;
        double z = pos.getZ() + 0.5 + back.getStepZ();
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.03, 0.0);
    }

    static BlockPos partPos(BlockPos masterPos, Direction facing, int part) {
        PartOffset offset = PARTS[part];
        return masterPos.relative(facing.getClockWise(), offset.side())
                .relative(facing.getOpposite(), offset.depth())
                .above(offset.height());
    }

    static BlockPos masterPos(BlockPos partPos, Direction facing, int part) {
        PartOffset offset = PARTS[part];
        return partPos.relative(facing.getClockWise(), -offset.side())
                .relative(facing.getOpposite(), -offset.depth())
                .below(offset.height());
    }

    static VoxelShape collisionShape(Direction facing, int part) {
        return COLLISION_SHAPES.get(facing)[part];
    }

    private static Map<Direction, VoxelShape[]> createCollisionShapes() {
        Map<Direction, VoxelShape[]> shapes = new EnumMap<>(Direction.class);
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            shapes.put(direction, new VoxelShape[PARTS.length]);
        }

        for (int part = 0; part < PARTS.length; part++) {
            PartOffset offset = PARTS[part];
            double cellMinX = offset.side() * 16.0;
            double cellMinY = offset.height() * 16.0;
            double cellMinZ = offset.depth() * 16.0;
            VoxelShape northShape = Shapes.empty();
            for (ModelBox box : COLLISION_BOXES) {
                double minX = Math.max(box.minX(), cellMinX);
                double minY = Math.max(box.minY(), cellMinY);
                double minZ = Math.max(box.minZ(), cellMinZ);
                double maxX = Math.min(box.maxX(), cellMinX + 16.0);
                double maxY = Math.min(box.maxY(), cellMinY + 16.0);
                double maxZ = Math.min(box.maxZ(), cellMinZ + 16.0);
                if (minX >= maxX || minY >= maxY || minZ >= maxZ) {
                    continue;
                }
                northShape = Shapes.or(northShape, Block.box(
                        minX - cellMinX,
                        minY - cellMinY,
                        minZ - cellMinZ,
                        maxX - cellMinX,
                        maxY - cellMinY,
                        maxZ - cellMinZ));
            }

            Map<Direction, VoxelShape> rotatedShapes = Shapes.rotateHorizontal(northShape.optimize());
            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                shapes.get(direction)[part] = rotatedShapes.get(direction);
            }
        }
        return shapes;
    }

    private record PartOffset(int side, int depth, int height) {
    }

    private record ModelBox(
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
    }
}
