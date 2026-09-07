package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AurorianChairBlock extends PairedFurnitureBlock {
    public static final MapCodec<AurorianChairBlock> CODEC = simpleCodec(AurorianChairBlock::new);
    private static final Map<Direction, VoxelShape> LOWER = Shapes.rotateHorizontal(Shapes.or(
            AurorianStoolBlock.collisionShape(), box(1, 12, 13, 3, 16, 15), box(13, 12, 13, 15, 16, 15),
            box(3, 12, 13, 13, 14, 15), box(4, 14, 13.5, 6, 16, 14.5),
            box(7, 14, 13.5, 9, 16, 14.5), box(10, 14, 13.5, 12, 16, 14.5)));
    private static final Map<Direction, VoxelShape> UPPER = Shapes.rotateHorizontal(Shapes.or(
            box(1, 0, 13, 3, 8, 15), box(13, 0, 13, 15, 8, 15), box(3, 6, 13, 13, 8, 15),
            box(4, 0, 13.5, 6, 6, 14.5), box(7, 0, 13.5, 9, 6, 14.5), box(10, 0, 13.5, 12, 6, 14.5)));

    public AurorianChairBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected Direction extensionDirection(BlockState state) { return Direction.UP; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(SECOND) ? UPPER : LOWER).get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        BlockPos base = state.getValue(SECOND) ? pos.below() : pos;
        BlockState lower = level.getBlockState(base);
        if (!lower.is(this) || lower.getValue(SECOND)) return InteractionResult.PASS;
        return AurorianStoolBlock.trySit(level, base, player);
    }
}
