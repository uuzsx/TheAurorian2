package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.SpiderMotherSpawnerBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class SpiderMotherSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<SpiderMotherSpawnerBlock> CODEC = simpleCodec(SpiderMotherSpawnerBlock::new);
    private static final int SEARCH_RADIUS = 40;
    private static final int SEARCH_DOWN = 8;
    private static final int SEARCH_UP = 2;

    public SpiderMotherSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiderMotherSpawnerBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SpiderMotherSpawnerBlockEntity spawner) {
            spawner.trySpawn(level);
        }
    }

    public static boolean activateNearest(ServerLevel level, BlockPos gatePos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 0; distance <= SEARCH_RADIUS; distance++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int dy = -SEARCH_DOWN; dy <= SEARCH_UP; dy++) {
                    cursor.set(
                            gatePos.getX() + direction.getStepX() * distance,
                            gatePos.getY() + dy,
                            gatePos.getZ() + direction.getStepZ() * distance);
                    if (!level.hasChunkAt(cursor)
                            || !level.getBlockState(cursor).is(ModStructureBlocks.SPIDER_MOTHER_SPAWNER.get())) {
                        continue;
                    }
                    if (level.getBlockEntity(cursor) instanceof SpiderMotherSpawnerBlockEntity spawner) {
                        spawner.arm(level);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
