package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AurorianTableBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class AurorianTableBlock extends BaseEntityBlock {

    public static final MapCodec<AurorianTableBlock> CODEC = simpleCodec(AurorianTableBlock::new);
    private static final BlockPos[] PART_OFFSETS = {
        BlockPos.ZERO,
        new BlockPos(1, 0, 0),
        new BlockPos(0, 0, 1),
        new BlockPos(1, 0, 1)
    };
    private static final ModelBox[] COLLISION_BOXES = {
        new ModelBox(5.0, 7.0, 5.0, 27.0, 9.05, 27.0),
        new ModelBox(15.0, 0.0, 7.0, 17.0, 7.0, 13.0),
        new ModelBox(19.0, 0.0, 15.0, 25.0, 7.0, 17.0),
        new ModelBox(15.0, 0.0, 19.0, 17.0, 7.0, 25.0),
        new ModelBox(7.0, 0.0, 15.0, 13.0, 7.0, 17.0)
    };
    private static final VoxelShape[] SHAPES = createShapes();

    public AurorianTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AurorianTableBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[0];
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[0];
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos masterPos = context.getClickedPos();
        for (int part = 1; part < PART_OFFSETS.length; part++) {
            BlockPos partPos = masterPos.offset(PART_OFFSETS[part]);
            if (!level.isInWorldBounds(partPos)
                    || !level.getWorldBorder().isWithinBounds(partPos)
                    || !level.getBlockState(partPos).canBeReplaced(context)) {
                return null;
            }
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }

        BlockState partState = ModBlocks.WOOD_TABLE_PART.get().defaultBlockState()
                .setValue(AurorianTablePartBlock.VARIANT, tableVariant());
        for (int part = 1; part < PART_OFFSETS.length; part++) {
            level.setBlock(
                    pos.offset(PART_OFFSETS[part]),
                    partState.setValue(AurorianTablePartBlock.PART, part),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AurorianTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        if (!table.getDisplayedItem().isEmpty()) {
            return rotateDisplayedItem(level, pos, state, player, table);
        }
        if (itemStack.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            table.setDisplayedItem(itemStack.copyWithCount(1));
            itemStack.consume(1, player);
            level.playSound(null, displayCenter(pos), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AurorianTableBlockEntity table
                && !table.getDisplayedItem().isEmpty()) {
            return rotateDisplayedItem(level, pos, state, player, table);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult rotateDisplayedItem(
            Level level, BlockPos pos, BlockState state, Player player, AurorianTableBlockEntity table) {
        if (!level.isClientSide()) {
            table.rotateDisplayedItem();
            level.playSound(null, displayCenter(pos), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.preventsBlockDrops()) {
            dropDisplayedItem(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof AurorianTableBlockEntity table && !table.getDisplayedItem().isEmpty()) {
            drops.add(table.getDisplayedItem().copy());
        }
        return drops;
    }

    void dropDisplayedItem(Level level, BlockPos pos) {
        if (level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof AurorianTableBlockEntity table
                && !table.getDisplayedItem().isEmpty()) {
            popResource(level, displayCenter(pos), table.takeDisplayedItem());
            level.playSound(null, displayCenter(pos), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(level.getBlockState(pos)));
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        for (int part = 1; part < PART_OFFSETS.length; part++) {
            BlockPos partPos = pos.offset(PART_OFFSETS[part]);
            BlockState partState = level.getBlockState(partPos);
            if (partState.is(ModBlocks.WOOD_TABLE_PART.get())
                    && partState.getValue(AurorianTablePartBlock.PART) == part) {
                level.setBlock(
                        partPos,
                        Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
    }

    int tableVariant() {
        if (this == ModBlocks.CURTAIN_WOOD_TABLE.get()) {
            return 1;
        }
        return this == ModBlocks.CURSED_FROST_WOOD_TABLE.get() ? 2 : 0;
    }

    static BlockPos masterPos(BlockPos partPos, int part) {
        return partPos.subtract(PART_OFFSETS[part]);
    }

    static VoxelShape collisionShape(int part) {
        return SHAPES[part];
    }

    private static BlockPos displayCenter(BlockPos pos) {
        return pos.offset(1, 1, 1);
    }

    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[PART_OFFSETS.length];
        for (int part = 0; part < PART_OFFSETS.length; part++) {
            BlockPos offset = PART_OFFSETS[part];
            double cellMinX = offset.getX() * 16.0;
            double cellMinZ = offset.getZ() * 16.0;
            VoxelShape shape = Shapes.empty();
            for (ModelBox box : COLLISION_BOXES) {
                double minX = Math.max(box.minX(), cellMinX);
                double minZ = Math.max(box.minZ(), cellMinZ);
                double maxX = Math.min(box.maxX(), cellMinX + 16.0);
                double maxZ = Math.min(box.maxZ(), cellMinZ + 16.0);
                if (minX >= maxX || minZ >= maxZ) {
                    continue;
                }
                shape = Shapes.or(shape, Block.box(
                        minX - cellMinX,
                        box.minY(),
                        minZ - cellMinZ,
                        maxX - cellMinX,
                        box.maxY(),
                        maxZ - cellMinZ));
            }
            shapes[part] = shape.optimize();
        }
        return shapes;
    }

    private record ModelBox(
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
    }
}
