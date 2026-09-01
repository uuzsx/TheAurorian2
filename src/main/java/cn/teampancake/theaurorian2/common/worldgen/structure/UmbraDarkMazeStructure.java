package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class UmbraDarkMazeStructure extends Structure {

    public static final MapCodec<UmbraDarkMazeStructure> CODEC = simpleCodec(UmbraDarkMazeStructure::new);

    public UmbraDarkMazeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX,
                centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState());
        int minBaseY = context.heightAccessor().getMinY() + 8;
        int maxBaseY = context.heightAccessor().getMaxY() - UmbraDarkMazePiece.STRUCTURE_HEIGHT - 8;
        int baseY = Mth.clamp(surfaceY - 34, minBaseY, maxBaseY);
        long layoutSeed = context.random().nextLong();
        BlockPos origin = new BlockPos(
                centerX - UmbraDarkMazePiece.FOOTPRINT / 2,
                baseY,
                centerZ - UmbraDarkMazePiece.FOOTPRINT / 2);
        return Optional.of(new GenerationStub(
                origin, builder -> builder.addPiece(new UmbraDarkMazePiece(origin, layoutSeed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.UMBRA_DARK_MAZE.get();
    }
}
