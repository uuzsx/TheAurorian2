package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.block.WallMushroomBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;

public final class AurorianMineshaftStructure extends MineshaftStructure {

    public static final MapCodec<AurorianMineshaftStructure> CODEC =
            simpleCodec(AurorianMineshaftStructure::new);

    public AurorianMineshaftStructure(StructureSettings settings) {
        super(settings, Type.NORMAL);
    }

    @Override
    public void afterPlace(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBounds,
            ChunkPos chunkPos,
            PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, chunkBounds, chunkPos, pieces);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox pieceBounds = piece.getBoundingBox();
            int minX = Math.max(pieceBounds.minX() - 1, chunkBounds.minX());
            int minY = Math.max(pieceBounds.minY() - 21, chunkBounds.minY());
            int minZ = Math.max(pieceBounds.minZ() - 1, chunkBounds.minZ());
            int maxX = Math.min(pieceBounds.maxX() + 1, chunkBounds.maxX());
            int maxY = Math.min(pieceBounds.maxY() + 1, chunkBounds.maxY());
            int maxZ = Math.min(pieceBounds.maxZ() + 1, chunkBounds.maxZ());
            if (minX > maxX || minY > maxY || minZ > maxZ) {
                continue;
            }

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        cursor.set(x, y, z);
                        BlockState current = level.getBlockState(cursor);
                        if (current.is(Blocks.SPAWNER)) {
                            level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                            level.setBlock(cursor, Blocks.SPAWNER.defaultBlockState(), 2);
                            continue;
                        }
                        if ((current.is(Blocks.BROWN_MUSHROOM)
                                        || current.is(Blocks.RED_MUSHROOM)
                                        || current.getBlock() instanceof WallMushroomBlock)
                                && !current.canSurvive(level, cursor)) {
                            level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }
                        BlockState mapped = mapMineshaftMaterial(current);
                        if (mapped != current) {
                            level.setBlock(cursor, mapped, 2);
                        }
                    }
                }
            }
        }

    }

    private static BlockState mapMineshaftMaterial(BlockState state) {
        if (state.is(Blocks.OAK_LOG)) {
            return ModBlocks.SILENT_TREE_LOG.get()
                    .defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        if (state.is(Blocks.OAK_PLANKS)) {
            return ModBlocks.SILENT_WOOD.planks().get().defaultBlockState();
        }
        if (state.is(Blocks.OAK_FENCE)) {
            return ModBlocks.SILENT_WOOD.fence().get()
                    .defaultBlockState()
                    .setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.NORTH))
                    .setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.EAST))
                    .setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.SOUTH))
                    .setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.WEST))
                    .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        }
        if (state.is(Blocks.RAIL)) {
            return ModBlocks.AURORIAN_RAIL.get()
                    .defaultBlockState()
                    .setValue(RailBlock.SHAPE, state.getValue(RailBlock.SHAPE))
                    .setValue(BaseRailBlock.WATERLOGGED, state.getValue(BaseRailBlock.WATERLOGGED));
        }
        if (state.is(Blocks.TORCH)) {
            return ModBlocks.SILENT_WOOD_TORCH.get().defaultBlockState();
        }
        if (state.is(Blocks.WALL_TORCH)) {
            return ModBlocks.SILENT_WOOD_WALL_TORCH.get()
                    .defaultBlockState()
                    .setValue(
                            BlockStateProperties.HORIZONTAL_FACING,
                            state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return state;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AURORIAN_MINESHAFT.get();
    }
}
