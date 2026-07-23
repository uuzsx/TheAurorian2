package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class AurorianSkyIslandStructure extends Structure {

    public static final MapCodec<AurorianSkyIslandStructure> CODEC = simpleCodec(AurorianSkyIslandStructure::new);

    public AurorianSkyIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int groundY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX,
                centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState());
        if (groundY > 170) {
            return Optional.empty();
        }

        int topY = 216 + context.random().nextInt(7);
        long shapeSeed = context.random().nextLong();
        BlockPos position = new BlockPos(centerX, topY, centerZ);
        return Optional.of(new GenerationStub(
                position,
                builder -> builder.addPiece(new AurorianSkyIslandPiece(centerX, centerZ, groundY, topY, shapeSeed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AURORIAN_SKY_ISLAND_GROUP.get();
    }
}
