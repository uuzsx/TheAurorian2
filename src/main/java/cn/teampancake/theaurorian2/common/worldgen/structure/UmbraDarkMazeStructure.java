package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.registry.ModStructures;
import cn.teampancake.theaurorian2.common.worldgen.LakeIslandBiomeSource;
import cn.teampancake.theaurorian2.common.worldgen.LakeIslandLayout;
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
        if (context.biomeSource() instanceof LakeIslandBiomeSource source) {
            LakeIslandLayout layout = source.layout(context.randomState().sampler());
            int step = 8;
            int radius = ((UmbraDarkMazePiece.FOOTPRINT / 2 + 16 + step - 1) / step) * step;
            // Check the whole footprint in two dimensions: a forest or underground
            // start biome must not let this wide maze extend beneath the lake or island.
            for (int x = centerX - radius; x <= centerX + radius; x += step) {
                for (int z = centerZ - radius; z <= centerZ + radius; z += step) {
                    LakeIslandLayout.Zone zone = layout.sample(x, z).zone();
                    if (zone == LakeIslandLayout.Zone.ISLAND || zone == LakeIslandLayout.Zone.LAKE) {
                        return Optional.empty();
                    }
                }
            }
        }
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
