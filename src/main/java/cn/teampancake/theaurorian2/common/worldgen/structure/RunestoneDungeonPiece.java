package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class RunestoneDungeonPiece extends TemplateStructurePiece {
    private final BlockPos anchor;
    private final int column;
    private final int row;

    public RunestoneDungeonPiece(StructureTemplateManager manager, BlockPos anchor, Rotation rotation, int column, int row) {
        super(ModStructures.RUNESTONE_DUNGEON_PIECE.get(), 0, manager, templateId(column, row),
                templateId(column, row).toString(), settings(rotation),
                RunestoneDungeonTerrain.worldPosition(anchor, rotation, column * 120, 0, row * 120));
        this.anchor = anchor;
        this.column = column;
        this.row = row;
        includeGround();
    }

    public RunestoneDungeonPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.RUNESTONE_DUNGEON_PIECE.get(), tag, context.structureTemplateManager(),
                id -> settings(Rotation.valueOf(tag.getStringOr("Rotation", "NONE"))));
        this.anchor = new BlockPos(tag.getIntOr("AnchorX", 0), tag.getIntOr("AnchorY", 0), tag.getIntOr("AnchorZ", 0));
        this.column = tag.getIntOr("Column", 1);
        this.row = tag.getIntOr("Row", 1);
        includeGround();
    }

    private static Identifier templateId(int column, int row) {
        return TheAurorian2.id("runestone_dungeon/part_" + (column * 3 + row + 1));
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
    }

    private void includeGround() {
        BoundingBox ground = RunestoneDungeonTerrain.groundBox(anchor, getRotation(), column, row);
        if (ground != null) this.boundingBox.encapsulate(ground);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("AnchorX", anchor.getX());
        tag.putInt("AnchorY", anchor.getY());
        tag.putInt("AnchorZ", anchor.getZ());
        tag.putInt("Column", column);
        tag.putInt("Row", row);
        tag.putString("Rotation", getRotation().name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        RunestoneDungeonTerrain.blend(level, chunkBox, anchor, getRotation(), column, row);
        super.postProcess(level, structureManager, generator, random, chunkBox, chunkPos, referencePos);
        includeGround();
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level,
                                     RandomSource random, BoundingBox chunkBox) {
        // The imported architectural template has no gameplay markers.
    }

    public BlockPos anchor() { return anchor; }
    public int part() { return column * 3 + row + 1; }
}
