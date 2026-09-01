package cn.teampancake.theaurorian2.common.worldgen.structure;

import cn.teampancake.theaurorian2.common.block.HorizontalEntityBlock;
import cn.teampancake.theaurorian2.common.block.SpiderMotherBarrierBlock;
import cn.teampancake.theaurorian2.common.block.entity.SpiderMotherSpawnerBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class UmbraDarkMazePiece extends StructurePiece {

    public static final int FOOTPRINT = 147;
    public static final int STRUCTURE_HEIGHT = 22;
    private static final int GRID_SIZE = 9;
    private static final int ROOM_SIZE = 11;
    private static final int CORRIDOR_LENGTH = 6;
    private static final int CELL_PITCH = ROOM_SIZE + CORRIDOR_LENGTH;
    private static final int CORRIDOR_HALF_WIDTH = 2;
    private static final int SECOND_FLOOR_Y = 0;
    private static final int SECOND_FLOOR_CEILING_Y = 8;
    private static final int FIRST_FLOOR_Y = 9;
    private static final int FIRST_FLOOR_CEILING_Y = 17;
    private static final int FIRST_FLOOR_ROOMS = 40;
    private static final int SECOND_FLOOR_ROOMS = 45;
    private static final int BOSS_GRID_SIZE = 5;
    private static final int BOSS_ARENA_DIAMETER = 71;
    private static final int BOSS_BOUNDS_WIDTH = (BOSS_GRID_SIZE - 1) * CELL_PITCH + ROOM_SIZE;
    private static final int BOSS_ARENA_DEPTH = 4;
    private static final int BOSS_ARENA_CAGES = 3;
    private static final int LOWEST_LOCAL_Y = SECOND_FLOOR_Y - BOSS_ARENA_DEPTH;
    private static final long FIRST_FLOOR_LAYOUT_SALT = 0x4f1bbcdc6762c63bL;
    private static final long SECOND_FLOOR_LAYOUT_SALT = 0x9e3779b97f4a7c15L;

    private final BlockPos origin;
    private final long layoutSeed;

    public UmbraDarkMazePiece(BlockPos origin, long layoutSeed) {
        super(ModStructures.UMBRA_DARK_MAZE_PIECE.get(), 0, createBoundingBox(origin));
        this.origin = origin;
        this.layoutSeed = layoutSeed;
    }

    public UmbraDarkMazePiece(CompoundTag tag) {
        super(ModStructures.UMBRA_DARK_MAZE_PIECE.get(), tag);
        this.origin = new BlockPos(
                tag.getIntOr("OriginX", this.boundingBox.minX()),
                tag.getIntOr("OriginY", this.boundingBox.minY()),
                tag.getIntOr("OriginZ", this.boundingBox.minZ()));
        this.layoutSeed = tag.getLongOr("LayoutSeed", 0L);
    }

    private static BoundingBox createBoundingBox(BlockPos origin) {
        return new BoundingBox(
                origin.getX(),
                origin.getY() + LOWEST_LOCAL_Y,
                origin.getZ(),
                origin.getX() + FOOTPRINT - 1,
                origin.getY() + FIRST_FLOOR_CEILING_Y,
                origin.getZ() + FOOTPRINT - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.origin.getX());
        tag.putInt("OriginY", this.origin.getY());
        tag.putInt("OriginZ", this.origin.getZ());
        tag.putLong("LayoutSeed", this.layoutSeed);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos referencePos) {
        MazePlan plan = MazePlan.create(this.layoutSeed);
        Palette firstFloorPalette = Palette.umbra();
        Palette secondFloorPalette = Palette.darkStone();
        ArenaPalette arenaPalette = ArenaPalette.darkStone();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = Math.max(this.origin.getX(), chunkBox.minX());
        int maxX = Math.min(this.origin.getX() + FOOTPRINT - 1, chunkBox.maxX());
        int minZ = Math.max(this.origin.getZ(), chunkBox.minZ());
        int maxZ = Math.min(this.origin.getZ() + FOOTPRINT - 1, chunkBox.maxZ());

        for (int worldX = minX; worldX <= maxX; worldX++) {
            int localX = worldX - this.origin.getX();
            for (int worldZ = minZ; worldZ <= maxZ; worldZ++) {
                int localZ = worldZ - this.origin.getZ();
                for (int localY = LOWEST_LOCAL_Y; localY <= FIRST_FLOOR_CEILING_Y; localY++) {
                    BlockState state = this.stateAt(
                            plan, firstFloorPalette, secondFloorPalette, arenaPalette, localX, localY, localZ);
                    if (state != null) {
                        level.setBlock(cursor.set(worldX, this.origin.getY() + localY, worldZ), state, 2);
                    }
                }
            }
        }

        this.buildStaircase(
                level, chunkBox, plan.stairA(), false, firstFloorPalette, secondFloorPalette);
        this.buildStaircase(
                level, chunkBox, plan.stairB(), true, firstFloorPalette, secondFloorPalette);
        this.decorateDarkStoneFloor(level, chunkBox, plan);
        this.placeFirstFloorUrns(level, chunkBox, plan);
        this.placeTreasureRoomLoot(level, chunkBox, plan);
        this.configureSpiderMotherSpawner(level, chunkBox, plan);
    }

    private @Nullable BlockState stateAt(
            MazePlan plan,
            Palette firstFloor,
            Palette secondFloor,
            ArenaPalette arenaPalette,
            int localX,
            int localY,
            int localZ) {
        if (localY <= SECOND_FLOOR_CEILING_Y) {
            return this.layerState(
                    plan.secondFloor(),
                    secondFloor,
                    arenaPalette,
                    localX,
                    localY,
                    localZ,
                    SECOND_FLOOR_Y,
                    SECOND_FLOOR_CEILING_Y);
        }
        return this.layerState(
                plan.firstFloor(),
                firstFloor,
                arenaPalette,
                localX,
                localY,
                localZ,
                FIRST_FLOOR_Y,
                FIRST_FLOOR_CEILING_Y);
    }

    private @Nullable BlockState layerState(
            LayerPlan plan,
            Palette palette,
            ArenaPalette arenaPalette,
            int localX,
            int localY,
            int localZ,
            int floorY,
            int ceilingY) {
        if (!plan.covers(localX, localZ)) {
            return null;
        }
        if (plan.inBossArena(localX, localZ)) {
            int arenaDepth = plan.bossArenaDepth(localX, localZ);
            int arenaFloorY = floorY - arenaDepth;
            if (localY < arenaFloorY) {
                return arenaPalette.foundation();
            }
            if (plan.isBossArenaCenter(localX, localZ) && localY == arenaFloorY + 1) {
                return ModStructureBlocks.SPIDER_MOTHER_SPAWNER.get().defaultBlockState();
            }
            BlockState cageState = plan.bossArenaCageState(
                    localX, localY, localZ, ceilingY, arenaPalette);
            if (cageState != null) {
                return cageState;
            }
            if (plan.isBossArenaPillar(localX, localZ)) {
                return arenaPalette.pillar();
            }
            if (localY == arenaFloorY) {
                return plan.isBossArenaFloorLamp(localX, localZ, arenaDepth)
                        ? arenaPalette.lamp()
                        : arenaPalette.floor(arenaDepth);
            }
            if (localY == ceilingY) {
                return palette.ceiling(plan, localX, localZ, this.layoutSeed);
            }
            if (plan.isBossGate(localX, localY, localZ, floorY, ceilingY)) {
                return plan.isBossGateKeyhole(localX, localY, localZ, floorY)
                        ? ModStructureBlocks.DARK_STONE_GATE_KEYHOLE.get().defaultBlockState()
                        : ModStructureBlocks.DARK_STONE_GATE.get().defaultBlockState();
            }
            if (plan.isBoundary(localX, localZ)) {
                return arenaPalette.wall();
            }
            if (plan.isBossArenaCobweb(
                    localX, localY, localZ, arenaFloorY, ceilingY, arenaDepth, this.layoutSeed)) {
                return Blocks.COBWEB.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (localY < floorY) {
            return null;
        }
        if (localY == floorY) {
            return palette.floor(localX, localZ, this.layoutSeed);
        }
        if (localY == ceilingY) {
            return palette.ceiling(plan, localX, localZ, this.layoutSeed);
        }
        if (plan.isBossGate(localX, localY, localZ, floorY, ceilingY)) {
            return plan.isBossGateKeyhole(localX, localY, localZ, floorY)
                    ? ModStructureBlocks.DARK_STONE_GATE_KEYHOLE.get().defaultBlockState()
                    : ModStructureBlocks.DARK_STONE_GATE.get().defaultBlockState();
        }
        if (plan.isBoundary(localX, localZ)) {
            return palette.wall(localX, localY, localZ, this.layoutSeed);
        }
        return Blocks.AIR.defaultBlockState();
    }

    private void buildStaircase(
            WorldGenLevel level,
            BoundingBox chunkBox,
            Cell room,
            boolean reverse,
            Palette firstFloorPalette,
            Palette secondFloorPalette) {
        int roomMinX = this.origin.getX() + room.x() * CELL_PITCH;
        int roomMinZ = this.origin.getZ() + room.z() * CELL_PITCH;
        Direction facing = reverse ? Direction.EAST : Direction.WEST;
        int centerZ = roomMinZ + ROOM_SIZE / 2;

        for (int step = 0; step <= FIRST_FLOOR_Y - SECOND_FLOOR_Y; step++) {
            int xOffset = reverse ? ROOM_SIZE - 2 - step : 1 + step;
            int worldX = roomMinX + xOffset;
            int stepY = this.origin.getY() + SECOND_FLOOR_Y + 1 + step;
            Palette palette = stepY < this.origin.getY() + FIRST_FLOOR_Y
                    ? secondFloorPalette
                    : firstFloorPalette;
            BlockState stair = palette.stair().setValue(StairBlock.FACING, facing);
            for (int dz = -1; dz <= 1; dz++) {
                int worldZ = centerZ + dz;
                this.placeIfInside(level, chunkBox, worldX, stepY, worldZ, stair);
                for (int clearance = 1; clearance <= 3; clearance++) {
                    this.placeIfInside(
                            level, chunkBox, worldX, stepY + clearance, worldZ, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void placeIfInside(
            WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos position = new BlockPos(x, y, z);
        if (chunkBox.isInside(position)) {
            level.setBlock(position, state, 2);
        }
    }

    private void placeFirstFloorUrns(WorldGenLevel level, BoundingBox chunkBox, MazePlan plan) {
        BlockState urn = ModStructureBlocks.URN.get().defaultBlockState();
        for (Cell room : plan.firstFloor().activeRooms()) {
            if (room.equals(plan.stairA()) || room.equals(plan.stairB())) {
                continue;
            }
            RandomSource decoration = RandomSource.create(
                    Palette.hash(this.layoutSeed ^ 0x632be59bd9b4e019L, room.x(), FIRST_FLOOR_Y, room.z()));
            if (decoration.nextFloat() >= 0.52F) {
                continue;
            }

            int roomMinX = this.origin.getX() + room.x() * CELL_PITCH;
            int roomMinZ = this.origin.getZ() + room.z() * CELL_PITCH;
            int[][] corners = {
                {1, 1},
                {ROOM_SIZE - 2, 1},
                {1, ROOM_SIZE - 2},
                {ROOM_SIZE - 2, ROOM_SIZE - 2}
            };
            int firstCorner = decoration.nextInt(corners.length);
            this.placeIfInside(
                    level,
                    chunkBox,
                    roomMinX + corners[firstCorner][0],
                    this.origin.getY() + FIRST_FLOOR_Y + 1,
                    roomMinZ + corners[firstCorner][1],
                    urn);
            if (decoration.nextFloat() < 0.28F) {
                int secondCorner = (firstCorner + 1 + decoration.nextInt(corners.length - 1)) % corners.length;
                this.placeIfInside(
                        level,
                        chunkBox,
                        roomMinX + corners[secondCorner][0],
                        this.origin.getY() + FIRST_FLOOR_Y + 1,
                        roomMinZ + corners[secondCorner][1],
                        urn);
            }
        }
    }

    private void decorateDarkStoneFloor(WorldGenLevel level, BoundingBox chunkBox, MazePlan plan) {
        LayerPlan floor = plan.secondFloor();
        for (Cell room : floor.activeRooms()) {
            if (floor.isBossCell(room)
                    || floor.isBossApproachRoom(room)
                    || floor.isTreasureRoom(room)
                    || floor.isPrisonCell(room)
                    || room.equals(plan.stairA())
                    || room.equals(plan.stairB())) {
                continue;
            }
            this.decorateDarkStoneRoom(level, chunkBox, floor, room);
        }
        this.decoratePrisonHall(level, chunkBox, floor);
        this.decorateDarkStoneCorridors(level, chunkBox, floor, plan.stairA(), plan.stairB());
    }

    private void decorateDarkStoneRoom(
            WorldGenLevel level, BoundingBox chunkBox, LayerPlan floor, Cell room) {
        int roomMinX = this.origin.getX() + room.x() * CELL_PITCH;
        int roomMinZ = this.origin.getZ() + room.z() * CELL_PITCH;
        int floorY = this.origin.getY() + SECOND_FLOOR_Y;
        int ceilingY = this.origin.getY() + SECOND_FLOOR_CEILING_Y;
        BlockState pillar = block("dark_stone_pillar");
        BlockState base = block("smooth_dark_stone_bricks");
        BlockState cornice = block("chiseled_dark_stone_bricks");

        int[][] corners = {{0, 0}, {ROOM_SIZE - 1, 0}, {0, ROOM_SIZE - 1}, {ROOM_SIZE - 1, ROOM_SIZE - 1}};
        for (int[] corner : corners) {
            for (int y = floorY + 1; y < ceilingY; y++) {
                this.placeIfInside(level, chunkBox, roomMinX + corner[0], y, roomMinZ + corner[1], pillar);
            }
        }
        for (int offset = 1; offset < ROOM_SIZE - 1; offset++) {
            this.replaceWallDecoration(level, chunkBox, roomMinX + offset, floorY + 1, roomMinZ, base);
            this.replaceWallDecoration(
                    level, chunkBox, roomMinX + offset, floorY + 1, roomMinZ + ROOM_SIZE - 1, base);
            this.replaceWallDecoration(level, chunkBox, roomMinX, floorY + 1, roomMinZ + offset, base);
            this.replaceWallDecoration(
                    level, chunkBox, roomMinX + ROOM_SIZE - 1, floorY + 1, roomMinZ + offset, base);
            this.replaceWallDecoration(level, chunkBox, roomMinX + offset, ceilingY - 1, roomMinZ, cornice);
            this.replaceWallDecoration(
                    level, chunkBox, roomMinX + offset, ceilingY - 1, roomMinZ + ROOM_SIZE - 1, cornice);
            this.replaceWallDecoration(level, chunkBox, roomMinX, ceilingY - 1, roomMinZ + offset, cornice);
            this.replaceWallDecoration(
                    level, chunkBox, roomMinX + ROOM_SIZE - 1, ceilingY - 1, roomMinZ + offset, cornice);
        }

        int variant = (int) Math.floorMod(
                Palette.hash(this.layoutSeed ^ 0x1f83d9abfb41bd6bL, room.x(), SECOND_FLOOR_Y, room.z()), 5);
        switch (variant) {
            case 0 -> this.decorateCryptRoom(level, chunkBox, roomMinX, floorY, roomMinZ, room);
            case 1 -> this.decorateStorageRoom(level, chunkBox, roomMinX, floorY, roomMinZ, room);
            case 2 -> this.decorateNestRoom(level, chunkBox, roomMinX, floorY, ceilingY, roomMinZ, room);
            case 3 -> this.decorateCollapsedRoom(level, chunkBox, roomMinX, floorY, roomMinZ, room);
            default -> this.decoratePillarRoom(level, chunkBox, roomMinX, floorY, ceilingY, roomMinZ);
        }
    }

    private void replaceWallDecoration(
            WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState replacement) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos) && !level.getBlockState(pos).isAir()) {
            level.setBlock(pos, replacement, 2);
        }
    }

    private void decorateCryptRoom(
            WorldGenLevel level, BoundingBox chunkBox, int minX, int floorY, int minZ, Cell room) {
        BlockState tomb = block("smooth_dark_stone_bricks");
        int[][] tombs = {{2, 2}, {7, 6}};
        for (int[] tombOrigin : tombs) {
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    this.placeIfInside(
                            level, chunkBox, minX + tombOrigin[0] + dx, floorY + 1, minZ + tombOrigin[1] + dz, tomb);
                }
            }
        }
        BlockState skull = Blocks.SKELETON_SKULL.defaultBlockState().setValue(
                SkullBlock.ROTATION,
                (int) Math.floorMod(Palette.hash(this.layoutSeed, room.x(), 5, room.z()), SkullBlock.MAX + 1));
        this.placeIfInside(level, chunkBox, minX + 2, floorY + 2, minZ + 3, skull);
    }

    private void decorateStorageRoom(
            WorldGenLevel level, BoundingBox chunkBox, int minX, int floorY, int minZ, Cell room) {
        int[][] positions = {{1, 1}, {2, 1}, {1, 2}, {9, 1}, {8, 1}, {9, 2}, {1, 9}, {2, 9}, {9, 9}};
        RandomSource random = RandomSource.create(
                Palette.hash(this.layoutSeed ^ 0x5be0cd19137e2179L, room.x(), SECOND_FLOOR_Y, room.z()));
        int count = 3 + random.nextInt(4);
        for (int index = 0; index < count; index++) {
            int selected = index + random.nextInt(positions.length - index);
            int[] value = positions[index];
            positions[index] = positions[selected];
            positions[selected] = value;
            this.placeIfInside(
                    level, chunkBox, minX + positions[index][0], floorY + 1, minZ + positions[index][1],
                    ModStructureBlocks.URN.get().defaultBlockState());
        }
    }

    private void decorateNestRoom(
            WorldGenLevel level, BoundingBox chunkBox, int minX, int floorY, int ceilingY, int minZ, Cell room) {
        int[][] floorWebs = {{1, 1}, {9, 1}, {1, 9}, {9, 9}};
        int start = (int) Math.floorMod(Palette.hash(this.layoutSeed, room.x(), 6, room.z()), floorWebs.length);
        for (int index = 0; index < 3; index++) {
            int[] offset = floorWebs[(start + index) % floorWebs.length];
            this.placeIfInside(level, chunkBox, minX + offset[0], floorY + 1, minZ + offset[1],
                    Blocks.COBWEB.defaultBlockState());
        }
        int[][] ceilingWebs = {{2, 2}, {8, 2}, {2, 8}, {8, 8}};
        for (int index = 0; index < 2; index++) {
            int[] offset = ceilingWebs[(start + index * 2) % ceilingWebs.length];
            this.placeIfInside(level, chunkBox, minX + offset[0], ceilingY - 1, minZ + offset[1],
                    Blocks.COBWEB.defaultBlockState());
        }
        this.placeIfInside(level, chunkBox, minX + 2, ceilingY - 1, minZ + 5, Blocks.IRON_CHAIN.defaultBlockState());
        this.placeIfInside(level, chunkBox, minX + 2, ceilingY - 2, minZ + 5, Blocks.IRON_CHAIN.defaultBlockState());
    }

    private void decorateCollapsedRoom(
            WorldGenLevel level, BoundingBox chunkBox, int minX, int floorY, int minZ, Cell room) {
        boolean mirror = (Palette.hash(this.layoutSeed, room.x(), 7, room.z()) & 1L) != 0L;
        int baseX = minX + (mirror ? ROOM_SIZE - 3 : 2);
        int step = mirror ? -1 : 1;
        BlockState bricks = block("dark_stone_bricks");
        BlockState slab = block("dark_stone_brick_slab");
        BlockState stair = block("dark_stone_brick_stairs").setValue(
                StairBlock.FACING, mirror ? Direction.WEST : Direction.EAST);
        int[][] blocks = {{0, 0}, {step, 0}, {0, 1}, {step, 1}};
        for (int[] offset : blocks) {
            this.placeIfInside(level, chunkBox, baseX + offset[0], floorY + 1, minZ + 2 + offset[1], bricks);
        }
        this.placeIfInside(level, chunkBox, baseX, floorY + 2, minZ + 2, slab);
        this.placeIfInside(level, chunkBox, baseX + step * 2, floorY + 1, minZ + 3, stair);
    }

    private void decoratePillarRoom(
            WorldGenLevel level, BoundingBox chunkBox, int minX, int floorY, int ceilingY, int minZ) {
        BlockState pillar = block("dark_stone_pillar");
        int[][] positions = {{2, 2}, {8, 2}, {2, 8}, {8, 8}};
        for (int[] position : positions) {
            for (int y = floorY + 1; y < ceilingY; y++) {
                this.placeIfInside(level, chunkBox, minX + position[0], y, minZ + position[1], pillar);
            }
        }
    }

    private void decoratePrisonHall(WorldGenLevel level, BoundingBox chunkBox, LayerPlan floor) {
        PrisonHall hall = floor.prisonHall();
        if (hall == null) {
            return;
        }
        int minX = this.origin.getX() + hall.start().x() * CELL_PITCH;
        int minZ = this.origin.getZ() + hall.start().z() * CELL_PITCH;
        int floorY = this.origin.getY() + SECOND_FLOOR_Y;
        int ceilingY = this.origin.getY() + SECOND_FLOOR_CEILING_Y;
        int hallLength = ROOM_SIZE * 3 + CORRIDOR_LENGTH * 2;
        BlockState wall = block("dark_stone_bricks");
        BlockState bars = block("dark_stone_bars");

        for (int partition = 1; partition <= 41; partition += 8) {
            for (int side = 0; side < 2; side++) {
                int crossStart = side == 0 ? 0 : 7;
                int crossEnd = side == 0 ? 3 : ROOM_SIZE - 1;
                for (int cross = crossStart; cross <= crossEnd; cross++) {
                    for (int y = floorY + 1; y < ceilingY; y++) {
                        this.placePrisonHallBlock(
                                level, chunkBox, hall, minX, minZ, partition, cross, y, wall);
                    }
                }
                BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
                int lanternCross = side == 0 ? 4 : 6;
                this.placePrisonHallBlock(
                        level, chunkBox, hall, minX, minZ, partition, lanternCross, ceilingY - 1, lantern);
            }
        }

        for (int cell = 0; cell < 5; cell++) {
            int start = 2 + cell * 8;
            int end = Math.min(start + 6, hallLength - 3);
            for (int side = 0; side < 2; side++) {
                int front = side == 0 ? 3 : 7;
                for (int along = start; along <= end; along++) {
                    BlockState connectedBars = prisonBarState(bars, hall.axis());
                    for (int y = floorY + 1; y < ceilingY; y++) {
                        this.placePrisonHallBlock(
                                level, chunkBox, hall, minX, minZ, along, front, y, connectedBars);
                    }
                }
                long cellSeed = Palette.hash(
                        this.layoutSeed ^ 0xa4093822299f31d0L, hall.start().x() + cell, side, hall.start().z());
                int skullAlong = -1;
                int skullCross = -1;
                if (Math.floorMod(cellSeed, 3) == 0) {
                    skullAlong = start + Math.floorMod((int) (cellSeed >>> 8), end - start + 1);
                    skullCross = side == 0
                            ? 1 + Math.floorMod((int) (cellSeed >>> 16), 2)
                            : 8 + Math.floorMod((int) (cellSeed >>> 16), 2);
                    BlockState skull = Blocks.SKELETON_SKULL.defaultBlockState().setValue(
                            SkullBlock.ROTATION, Math.floorMod((int) (cellSeed >>> 24), SkullBlock.MAX + 1));
                    this.placePrisonHallBlock(
                            level, chunkBox, hall, minX, minZ, skullAlong, skullCross, floorY + 1, skull);
                }
                if (Math.floorMod(cellSeed >>> 32, 4) == 0) {
                    int skeletonAlong = start + 1 + Math.floorMod((int) (cellSeed >>> 40), Math.max(1, end - start - 1));
                    int skeletonCross = side == 0 ? 2 : 8;
                    if (skeletonAlong == skullAlong && skeletonCross == skullCross) {
                        skeletonAlong = skeletonAlong < end - 1 ? skeletonAlong + 1 : skeletonAlong - 1;
                    }
                    this.spawnPrisonSkeleton(
                            level, chunkBox, hall, minX, minZ, skeletonAlong, skeletonCross, floorY + 1, cellSeed);
                }
            }
        }
    }

    private void spawnPrisonSkeleton(
            WorldGenLevel level,
            BoundingBox chunkBox,
            PrisonHall hall,
            int minX,
            int minZ,
            int along,
            int cross,
            int y,
            long seed) {
        int x = hall.axis() == Direction.Axis.X ? minX + along : minX + cross;
        int z = hall.axis() == Direction.Axis.Z ? minZ + along : minZ + cross;
        BlockPos pos = new BlockPos(x, y, z);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        Skeleton skeleton = EntityType.SKELETON.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (skeleton == null) {
            return;
        }
        skeleton.snapTo(x + 0.5D, y, z + 0.5D, (float) Math.floorMod(seed, 360L), 0.0F);
        skeleton.finalizeSpawn(
                level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        skeleton.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        skeleton.setPersistenceRequired();
        level.addFreshEntity(skeleton);
    }

    private static BlockState prisonBarState(BlockState state, Direction.Axis hallAxis) {
        if (hallAxis == Direction.Axis.X) {
            return state.setValue(CrossCollisionBlock.WEST, true)
                    .setValue(CrossCollisionBlock.EAST, true);
        }
        return state.setValue(CrossCollisionBlock.NORTH, true)
                .setValue(CrossCollisionBlock.SOUTH, true);
    }

    private void placePrisonHallBlock(
            WorldGenLevel level,
            BoundingBox chunkBox,
            PrisonHall hall,
            int minX,
            int minZ,
            int along,
            int cross,
            int y,
            BlockState state) {
        int x = hall.axis() == Direction.Axis.X ? minX + along : minX + cross;
        int z = hall.axis() == Direction.Axis.Z ? minZ + along : minZ + cross;
        this.placeIfInside(level, chunkBox, x, y, z, state);
    }

    private void decorateDarkStoneCorridors(
            WorldGenLevel level, BoundingBox chunkBox, LayerPlan floor, Cell stairA, Cell stairB) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int z = 0; z < GRID_SIZE; z++) {
                Cell room = new Cell(x, z);
                if (!floor.active[x][z]
                        || floor.isBossCell(room)
                        || floor.isTreasureRoom(room)
                        || floor.isPrisonCell(room)
                        || room.equals(stairA)
                        || room.equals(stairB)) {
                    continue;
                }
                if (x + 1 < GRID_SIZE && floor.east[x][z]) {
                    Cell other = new Cell(x + 1, z);
                    if (this.canDecorateCorridor(floor, other, stairA, stairB)) {
                        int archX = this.origin.getX() + x * CELL_PITCH + ROOM_SIZE + CORRIDOR_LENGTH / 2;
                        int centerZ = this.origin.getZ() + z * CELL_PITCH + ROOM_SIZE / 2;
                        this.buildCorridorArch(level, chunkBox, archX, centerZ, Direction.Axis.X);
                    }
                }
                if (z + 1 < GRID_SIZE && floor.south[x][z]) {
                    Cell other = new Cell(x, z + 1);
                    if (this.canDecorateCorridor(floor, other, stairA, stairB)) {
                        int centerX = this.origin.getX() + x * CELL_PITCH + ROOM_SIZE / 2;
                        int archZ = this.origin.getZ() + z * CELL_PITCH + ROOM_SIZE + CORRIDOR_LENGTH / 2;
                        this.buildCorridorArch(level, chunkBox, centerX, archZ, Direction.Axis.Z);
                    }
                }
            }
        }
    }

    private boolean canDecorateCorridor(LayerPlan floor, Cell room, Cell stairA, Cell stairB) {
        return floor.active[room.x()][room.z()]
                && !floor.isBossCell(room)
                && !floor.isTreasureRoom(room)
                && !floor.isPrisonCell(room)
                && !room.equals(stairA)
                && !room.equals(stairB);
    }

    private void buildCorridorArch(
            WorldGenLevel level, BoundingBox chunkBox, int centerX, int centerZ, Direction.Axis axis) {
        int floorY = this.origin.getY() + SECOND_FLOOR_Y;
        int ceilingY = this.origin.getY() + SECOND_FLOOR_CEILING_Y;
        BlockState pillar = block("dark_stone_pillar");
        BlockState beam = block("chiseled_dark_stone_bricks");
        for (int offset : new int[] {-CORRIDOR_HALF_WIDTH, CORRIDOR_HALF_WIDTH}) {
            int x = centerX + (axis == Direction.Axis.Z ? offset : 0);
            int z = centerZ + (axis == Direction.Axis.X ? offset : 0);
            for (int y = floorY + 1; y <= floorY + 3; y++) {
                this.placeIfInside(level, chunkBox, x, y, z, pillar);
            }
        }
        for (int offset = -CORRIDOR_HALF_WIDTH; offset <= CORRIDOR_HALF_WIDTH; offset++) {
            int x = centerX + (axis == Direction.Axis.Z ? offset : 0);
            int z = centerZ + (axis == Direction.Axis.X ? offset : 0);
            this.placeIfInside(level, chunkBox, x, ceilingY - 1, z, beam);
        }
    }

    private void placeTreasureRoomLoot(WorldGenLevel level, BoundingBox chunkBox, MazePlan plan) {
        BossEntrance treasureEntrance = plan.secondFloor().treasureEntrance();
        Cell room = treasureEntrance.outside();
        Direction outward = LayerPlan.directionFromInside(treasureEntrance);
        int roomMinX = this.origin.getX() + room.x() * CELL_PITCH;
        int roomMinZ = this.origin.getZ() + room.z() * CELL_PITCH;
        int floorY = this.origin.getY() + SECOND_FLOOR_Y;
        RandomSource decoration = RandomSource.create(Palette.hash(
                this.layoutSeed ^ 0x510e527fade682d1L, room.x(), SECOND_FLOOR_Y, room.z()));
        Set<BlockPos> occupied = new HashSet<>();

        BlockPos lampPos = new BlockPos(roomMinX + ROOM_SIZE / 2, floorY, roomMinZ + ROOM_SIZE / 2);
        BlockPos pedestalPos = lampPos.above();
        this.placeIfInside(
                level, chunkBox, lampPos.getX(), lampPos.getY(), lampPos.getZ(),
                block("dark_stone_lamp"));
        this.placeIfInside(
                level,
                chunkBox,
                pedestalPos.getX(),
                pedestalPos.getY(),
                pedestalPos.getZ(),
                ModStructureBlocks.CRYSTALLINE_SWORD_PEDESTAL.get()
                        .defaultBlockState()
                        .setValue(HorizontalEntityBlock.FACING, outward.getOpposite()));
        occupied.add(pedestalPos);

        int[] offsets = {3, ROOM_SIZE - 4};
        for (int offset : offsets) {
            int worldX = switch (outward) {
                case WEST -> roomMinX + 1;
                case EAST -> roomMinX + ROOM_SIZE - 2;
                default -> roomMinX + offset;
            };
            int worldZ = switch (outward) {
                case NORTH -> roomMinZ + 1;
                case SOUTH -> roomMinZ + ROOM_SIZE - 2;
                default -> roomMinZ + offset;
            };
            BlockPos pos = new BlockPos(worldX, floorY + 1, worldZ);
            occupied.add(pos);
            if (!chunkBox.isInside(pos)) {
                continue;
            }
            BlockState chestState = StructurePiece.reorient(
                    level, pos, ModBlocks.AURORIAN_CHEST.get().defaultBlockState());
            level.setBlock(pos, chestState, 2);
            if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
                container.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
                container.setLootTableSeed(Palette.hash(
                        this.layoutSeed ^ 0xa54ff53a5f1d36f1L, worldX, pos.getY(), worldZ));
            }
        }

        int[][] pileCenters = {{2, 2}, {ROOM_SIZE - 3, 2}, {2, ROOM_SIZE - 3}, {ROOM_SIZE - 3, ROOM_SIZE - 3}};
        shuffleOffsets(pileCenters, decoration);
        for (int[] pileCenter : pileCenters) {
            if (this.placeTreasureOrePile(
                    level,
                    chunkBox,
                    roomMinX,
                    floorY,
                    roomMinZ,
                    pileCenter[0],
                    pileCenter[1],
                    decoration,
                    occupied,
                    ModBlocks.GEODE_ORE.get().defaultBlockState())) {
                break;
            }
        }

        int[][] urnOffsets = {
            {1, 1}, {2, 1}, {1, 2},
            {ROOM_SIZE - 2, 1}, {ROOM_SIZE - 3, 1}, {ROOM_SIZE - 2, 2},
            {1, ROOM_SIZE - 2}, {2, ROOM_SIZE - 2}, {1, ROOM_SIZE - 3},
            {ROOM_SIZE - 2, ROOM_SIZE - 2},
            {ROOM_SIZE - 3, ROOM_SIZE - 2},
            {ROOM_SIZE - 2, ROOM_SIZE - 3}
        };
        shuffleOffsets(urnOffsets, decoration);
        int urnCount = 3 + decoration.nextInt(3);
        int placedUrns = 0;
        for (int index = 0; index < urnOffsets.length && placedUrns < urnCount; index++) {
            BlockPos pos = new BlockPos(
                    roomMinX + urnOffsets[index][0], floorY + 1, roomMinZ + urnOffsets[index][1]);
            if (!occupied.add(pos)) {
                continue;
            }
            this.placeIfInside(
                    level, chunkBox, pos.getX(), pos.getY(), pos.getZ(),
                    ModStructureBlocks.URN.get().defaultBlockState());
            placedUrns++;
        }
    }

    private void configureSpiderMotherSpawner(WorldGenLevel level, BoundingBox chunkBox, MazePlan plan) {
        LayerPlan secondFloor = plan.secondFloor();
        BlockPos spawnerPos = new BlockPos(
                this.origin.getX() + secondFloor.bossCenterX(),
                this.origin.getY() + SECOND_FLOOR_Y - BOSS_ARENA_DEPTH + 1,
                this.origin.getZ() + secondFloor.bossCenterZ());
        BlockPos barrierCenter = this.placeTreasureBarrier(level, chunkBox, secondFloor);
        if (chunkBox.isInside(spawnerPos)
                && level.getBlockEntity(spawnerPos) instanceof SpiderMotherSpawnerBlockEntity spawner) {
            spawner.setBarrierCenter(barrierCenter);
        }
    }

    private BlockPos placeTreasureBarrier(
            WorldGenLevel level, BoundingBox chunkBox, LayerPlan secondFloor) {
        BossEntrance entrance = secondFloor.treasureEntrance();
        Direction outward = LayerPlan.directionFromInside(entrance);
        Cell room = entrance.outside();
        int roomMinX = this.origin.getX() + room.x() * CELL_PITCH;
        int roomMinZ = this.origin.getZ() + room.z() * CELL_PITCH;
        int centerX = switch (outward) {
            case WEST -> roomMinX + ROOM_SIZE - 1;
            case EAST -> roomMinX;
            default -> roomMinX + ROOM_SIZE / 2;
        };
        int centerZ = switch (outward) {
            case NORTH -> roomMinZ + ROOM_SIZE - 1;
            case SOUTH -> roomMinZ;
            default -> roomMinZ + ROOM_SIZE / 2;
        };
        BlockState barrier = ModStructureBlocks.SPIDER_MOTHER_BARRIER.get()
                .defaultBlockState()
                .setValue(SpiderMotherBarrierBlock.FACING, outward);
        for (int offset = -CORRIDOR_HALF_WIDTH; offset <= CORRIDOR_HALF_WIDTH; offset++) {
            int x = centerX + (outward.getAxis() == Direction.Axis.Z ? offset : 0);
            int z = centerZ + (outward.getAxis() == Direction.Axis.X ? offset : 0);
            for (int localY = SECOND_FLOOR_Y + 1; localY < SECOND_FLOOR_CEILING_Y; localY++) {
                this.placeIfInside(
                        level, chunkBox, x, this.origin.getY() + localY, z, barrier);
            }
        }
        return new BlockPos(
                centerX,
                this.origin.getY() + (SECOND_FLOOR_Y + SECOND_FLOOR_CEILING_Y) / 2,
                centerZ);
    }

    private boolean placeTreasureOrePile(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int roomMinX,
            int floorY,
            int roomMinZ,
            int baseX,
            int baseZ,
            RandomSource random,
            Set<BlockPos> occupied,
            BlockState ore) {
        int[][] shape = {
            {0, 0, 0}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 1}, {-1, 0, 1},
            {0, 1, 0}, {1, 0, -1}, {-1, 0, -1}, {1, 1, 0}, {0, 1, 1}, {-1, 1, 0}
        };
        int rotation = random.nextInt(4);
        int size = 8 + random.nextInt(6);
        List<BlockPos> positions = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            int dx = shape[index][0];
            int dz = shape[index][2];
            int rotatedX = switch (rotation) {
                case 1 -> -dz;
                case 2 -> -dx;
                case 3 -> dz;
                default -> dx;
            };
            int rotatedZ = switch (rotation) {
                case 1 -> dx;
                case 2 -> -dz;
                case 3 -> -dx;
                default -> dz;
            };
            BlockPos pos = new BlockPos(
                    roomMinX + baseX + rotatedX,
                    floorY + 1 + shape[index][1],
                    roomMinZ + baseZ + rotatedZ);
            if (occupied.contains(pos)) {
                return false;
            }
            positions.add(pos);
        }
        occupied.addAll(positions);
        for (BlockPos pos : positions) {
            if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, ore, 2);
            }
        }
        return true;
    }

    private static void shuffleOffsets(int[][] offsets, RandomSource random) {
        for (int index = offsets.length - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            int[] value = offsets[index];
            offsets[index] = offsets[swapIndex];
            offsets[swapIndex] = value;
        }
    }

    private static BlockState block(String id) {
        return ModStructureBlocks.blocksById().get(id).get().defaultBlockState();
    }

    private record Cell(int x, int z) {
    }

    private record BossEntrance(Cell inside, Cell outside) {
    }

    private record ArenaCage(int centerX, int centerZ, int chainLength, int skullRotation) {
    }

    private record PrisonHall(Cell start, Direction.Axis axis) {
        private List<Cell> cells() {
            List<Cell> cells = new ArrayList<>(3);
            for (int offset = 0; offset < 3; offset++) {
                cells.add(this.axis == Direction.Axis.X
                        ? new Cell(this.start.x() + offset, this.start.z())
                        : new Cell(this.start.x(), this.start.z() + offset));
            }
            return cells;
        }

        private Cell first() {
            return this.start;
        }

        private Cell last() {
            return this.axis == Direction.Axis.X
                    ? new Cell(this.start.x() + 2, this.start.z())
                    : new Cell(this.start.x(), this.start.z() + 2);
        }

        private Cell before() {
            return this.axis == Direction.Axis.X
                    ? new Cell(this.start.x() - 1, this.start.z())
                    : new Cell(this.start.x(), this.start.z() - 1);
        }

        private Cell after() {
            return this.axis == Direction.Axis.X
                    ? new Cell(this.start.x() + 3, this.start.z())
                    : new Cell(this.start.x(), this.start.z() + 3);
        }
    }

    private record MazePlan(LayerPlan firstFloor, LayerPlan secondFloor, Cell stairA, Cell stairB) {
        private static MazePlan create(long seed) {
            RandomSource selector = RandomSource.create(seed);
            int bossMinX = 1 + selector.nextInt(GRID_SIZE - BOSS_GRID_SIZE - 1);
            int bossMinZ = 1 + selector.nextInt(GRID_SIZE - BOSS_GRID_SIZE - 1);
            Cell root = new Cell(GRID_SIZE / 2, GRID_SIZE / 2);

            LayerPlan firstFloor = LayerPlan.generate(
                    seed ^ FIRST_FLOOR_LAYOUT_SALT,
                    FIRST_FLOOR_ROOMS,
                    branchingAnchors(selector, root),
                    -1,
                    -1);
            List<Cell> stairs = firstFloor.farthestPairOutside(bossMinX, bossMinZ);
            Cell stairA = stairs.getFirst();
            Cell stairB = stairs.getLast();

            Cell bossCenter = new Cell(bossMinX + BOSS_GRID_SIZE / 2, bossMinZ + BOSS_GRID_SIZE / 2);
            List<Cell> secondAnchors = branchingAnchors(selector, bossCenter);
            secondAnchors.add(stairA);
            secondAnchors.add(stairB);
            LayerPlan secondFloor = LayerPlan.generate(
                    seed ^ SECOND_FLOOR_LAYOUT_SALT,
                    SECOND_FLOOR_ROOMS,
                    secondAnchors,
                    bossMinX,
                    bossMinZ);
            return new MazePlan(firstFloor, secondFloor, stairA, stairB);
        }

        private static List<Cell> branchingAnchors(RandomSource random, Cell root) {
            List<Cell> anchors = new ArrayList<>();
            anchors.add(root);
            anchors.add(new Cell(random.nextInt(2), 1 + random.nextInt(GRID_SIZE - 2)));
            anchors.add(new Cell(GRID_SIZE - 1 - random.nextInt(2), 1 + random.nextInt(GRID_SIZE - 2)));
            anchors.add(new Cell(1 + random.nextInt(GRID_SIZE - 2), random.nextBoolean() ? 0 : GRID_SIZE - 1));
            return anchors;
        }
    }

    private static final class LayerPlan {
        private final boolean[][] active = new boolean[GRID_SIZE][GRID_SIZE];
        private final boolean[][] east = new boolean[GRID_SIZE - 1][GRID_SIZE];
        private final boolean[][] south = new boolean[GRID_SIZE][GRID_SIZE - 1];
        private final int bossMinX;
        private final int bossMinZ;
        private final List<BossEntrance> bossEntrances = new ArrayList<>();
        private final List<ArenaCage> bossCages = new ArrayList<>(BOSS_ARENA_CAGES);
        private BossEntrance treasureEntrance;
        private PrisonHall prisonHall;

        private LayerPlan(int bossMinX, int bossMinZ) {
            this.bossMinX = bossMinX;
            this.bossMinZ = bossMinZ;
        }

        private static LayerPlan generate(
                long seed, int targetRooms, List<Cell> anchors, int bossMinX, int bossMinZ) {
            LayerPlan plan = new LayerPlan(bossMinX, bossMinZ);
            RandomSource random = RandomSource.create(seed);
            Cell root = anchors.getFirst();
            if (bossMinX >= 0) {
                for (int z = bossMinZ; z < bossMinZ + BOSS_GRID_SIZE; z++) {
                    for (int x = bossMinX; x < bossMinX + BOSS_GRID_SIZE; x++) {
                        plan.activate(new Cell(x, z));
                        if (x + 1 < bossMinX + BOSS_GRID_SIZE) {
                            plan.east[x][z] = true;
                        }
                        if (z + 1 < bossMinZ + BOSS_GRID_SIZE) {
                            plan.south[x][z] = true;
                        }
                    }
                }
                plan.chooseBossEntrances(random);
                plan.choosePrisonHall(random, List.of(anchors.get(anchors.size() - 2), anchors.getLast()));
                plan.chooseBossCages(RandomSource.create(seed ^ 0x3c6ef372fe94f82bL));
                plan.activate(plan.treasureEntrance.outside());
                plan.connect(plan.treasureEntrance.inside(), plan.treasureEntrance.outside());
                Cell outsideRoot = plan.bossEntrances.getFirst().outside();
                if (plan.prisonHall != null) {
                    for (Cell cell : plan.prisonHall.cells()) {
                        plan.activate(cell);
                    }
                    List<Cell> prisonCells = plan.prisonHall.cells();
                    plan.connect(prisonCells.get(0), prisonCells.get(1));
                    plan.connect(prisonCells.get(1), prisonCells.get(2));
                    plan.activate(plan.prisonHall.before());
                    plan.activate(plan.prisonHall.after());
                    plan.connect(plan.prisonHall.before(), plan.prisonHall.first());
                    plan.connect(plan.prisonHall.last(), plan.prisonHall.after());
                    plan.connectPathOutsideBoss(outsideRoot, plan.prisonHall.before(), random);
                    plan.connectPathOutsideBoss(outsideRoot, plan.prisonHall.after(), random);
                }
                for (BossEntrance entrance : plan.bossEntrances) {
                    plan.activate(entrance.outside());
                    plan.connect(entrance.inside(), entrance.outside());
                    plan.connectPathOutsideBoss(outsideRoot, entrance.outside(), random);
                }
                for (Cell anchor : anchors) {
                    if (!plan.isBossCell(anchor)
                            && !plan.isTreasureRoom(anchor)
                            && !plan.isPrisonCell(anchor)) {
                        plan.connectPathOutsideBoss(outsideRoot, anchor, random);
                    }
                }
            } else {
                plan.activate(root);
                for (int index = 1; index < anchors.size(); index++) {
                    plan.connectPath(root, anchors.get(index), random);
                }
            }

            while (plan.roomCount() < targetRooms) {
                List<Cell> endpoints = plan.growthCells(true);
                List<Cell> growth = !endpoints.isEmpty() && random.nextFloat() < 0.75F
                        ? endpoints
                        : plan.growthCells(false);
                if (growth.isEmpty()) {
                    break;
                }
                Cell parent = growth.get(random.nextInt(growth.size()));
                List<Cell> available = plan.inactiveNeighbors(parent);
                Cell child = available.get(random.nextInt(available.size()));
                plan.activate(child);
                plan.connect(parent, child);
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 0; z < GRID_SIZE; z++) {
                    if (x + 1 < GRID_SIZE
                            && plan.active[x][z]
                            && plan.active[x + 1][z]
                            && !plan.east[x][z]
                            && plan.isAllowedConnection(new Cell(x, z), new Cell(x + 1, z))
                            && random.nextFloat() < 0.05F) {
                        plan.east[x][z] = true;
                    }
                    if (z + 1 < GRID_SIZE
                            && plan.active[x][z]
                            && plan.active[x][z + 1]
                            && !plan.south[x][z]
                            && plan.isAllowedConnection(new Cell(x, z), new Cell(x, z + 1))
                            && random.nextFloat() < 0.05F) {
                        plan.south[x][z] = true;
                    }
                }
            }
            return plan;
        }

        private void chooseBossEntrances(RandomSource random) {
            List<Direction> sides = new ArrayList<>(List.of(
                    Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
            for (int index = sides.size() - 1; index > 0; index--) {
                int swapIndex = random.nextInt(index + 1);
                Direction side = sides.get(index);
                sides.set(index, sides.get(swapIndex));
                sides.set(swapIndex, side);
            }
            for (int index = 0; index < 3; index++) {
                Direction side = sides.get(index);
                this.bossEntrances.add(this.createBossEntrance(side));
            }
            this.treasureEntrance = this.createBossEntrance(sides.getLast());
        }

        private BossEntrance createBossEntrance(Direction side) {
            int offset = BOSS_GRID_SIZE / 2;
            Cell inside = switch (side) {
                case NORTH -> new Cell(this.bossMinX + offset, this.bossMinZ);
                case SOUTH -> new Cell(
                        this.bossMinX + offset, this.bossMinZ + BOSS_GRID_SIZE - 1);
                case WEST -> new Cell(this.bossMinX, this.bossMinZ + offset);
                case EAST -> new Cell(
                        this.bossMinX + BOSS_GRID_SIZE - 1, this.bossMinZ + offset);
                default -> throw new IllegalStateException("Unexpected boss entrance direction: " + side);
            };
            return new BossEntrance(
                    inside, new Cell(inside.x() + side.getStepX(), inside.z() + side.getStepZ()));
        }

        private void connectPathOutsideBoss(Cell start, Cell target, RandomSource random) {
            if (start.equals(target)) {
                this.activate(start);
                return;
            }
            Set<Cell> excluded = this.prisonHall == null
                    ? Set.of()
                    : Set.copyOf(this.prisonHall.cells());
            if (!this.isOutsideRouteCell(start, excluded) || !this.isOutsideRouteCell(target, excluded)) {
                return;
            }
            Cell[][] previous = new Cell[GRID_SIZE][GRID_SIZE];
            boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
            ArrayDeque<Cell> pending = new ArrayDeque<>();
            pending.add(start);
            visited[start.x()][start.z()] = true;
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            int directionOffset = random.nextInt(directions.length);

            while (!pending.isEmpty() && !visited[target.x()][target.z()]) {
                Cell current = pending.removeFirst();
                for (int index = 0; index < directions.length; index++) {
                    Direction direction = directions[(index + directionOffset) % directions.length];
                    Cell next = new Cell(
                            current.x() + direction.getStepX(), current.z() + direction.getStepZ());
                    if (!this.isOutsideRouteCell(next, excluded) || visited[next.x()][next.z()]) {
                        continue;
                    }
                    visited[next.x()][next.z()] = true;
                    previous[next.x()][next.z()] = current;
                    pending.addLast(next);
                }
            }

            if (!visited[target.x()][target.z()]) {
                // A malformed future layout must not abort the entire structure's chunk generation.
                return;
            }
            Cell current = target;
            this.activate(current);
            while (!current.equals(start)) {
                Cell parent = previous[current.x()][current.z()];
                if (parent == null) {
                    return;
                }
                this.activate(parent);
                this.connect(parent, current);
                current = parent;
            }
        }

        private void connectPath(Cell start, Cell target, RandomSource random) {
            Cell current = start;
            while (!current.equals(target)) {
                boolean moveX = current.x() != target.x()
                        && (current.z() == target.z() || random.nextBoolean());
                Cell next = moveX
                        ? new Cell(current.x() + Integer.signum(target.x() - current.x()), current.z())
                        : new Cell(current.x(), current.z() + Integer.signum(target.z() - current.z()));
                this.activate(next);
                this.connect(current, next);
                current = next;
            }
        }

        private void activate(Cell cell) {
            this.active[cell.x()][cell.z()] = true;
        }

        private int roomCount() {
            int count = 0;
            for (boolean[] column : this.active) {
                for (boolean room : column) {
                    if (room) {
                        count++;
                    }
                }
            }
            return count;
        }

        private List<Cell> growthCells(boolean endpointsOnly) {
            List<Cell> result = new ArrayList<>();
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 0; z < GRID_SIZE; z++) {
                    Cell cell = new Cell(x, z);
                    if (this.active[x][z]
                            && !this.isTreasureRoom(cell)
                            && !this.isPrisonCell(cell)
                            && !this.inactiveNeighbors(cell).isEmpty()
                            && (!endpointsOnly || this.connectionCount(cell) <= 1)) {
                        result.add(cell);
                    }
                }
            }
            return result;
        }

        private List<Cell> inactiveNeighbors(Cell cell) {
            List<Cell> neighbors = new ArrayList<>(4);
            if (cell.x() > 0) this.addIfInactive(neighbors, cell, new Cell(cell.x() - 1, cell.z()));
            if (cell.x() + 1 < GRID_SIZE) {
                this.addIfInactive(neighbors, cell, new Cell(cell.x() + 1, cell.z()));
            }
            if (cell.z() > 0) this.addIfInactive(neighbors, cell, new Cell(cell.x(), cell.z() - 1));
            if (cell.z() + 1 < GRID_SIZE) {
                this.addIfInactive(neighbors, cell, new Cell(cell.x(), cell.z() + 1));
            }
            return neighbors;
        }

        private void addIfInactive(List<Cell> result, Cell source, Cell candidate) {
            if (!this.active[candidate.x()][candidate.z()] && this.isAllowedConnection(source, candidate)) {
                result.add(candidate);
            }
        }

        private boolean isAllowedConnection(Cell first, Cell second) {
            boolean firstBoss = this.isBossCell(first);
            boolean secondBoss = this.isBossCell(second);
            if (this.isPrisonConnection(first, second)) {
                return true;
            }
            if (this.isPrisonCell(first) || this.isPrisonCell(second)) {
                return false;
            }
            if (this.isTreasureConnection(first, second)) {
                return true;
            }
            if (this.isTreasureRoom(first) || this.isTreasureRoom(second)) {
                return false;
            }
            if (firstBoss == secondBoss) {
                return true;
            }
            for (BossEntrance entrance : this.bossEntrances) {
                if ((entrance.inside().equals(first) && entrance.outside().equals(second))
                        || (entrance.inside().equals(second) && entrance.outside().equals(first))) {
                    return true;
                }
            }
            return false;
        }

        private boolean isTreasureConnection(Cell first, Cell second) {
            return this.treasureEntrance != null
                    && ((this.treasureEntrance.inside().equals(first)
                                    && this.treasureEntrance.outside().equals(second))
                            || (this.treasureEntrance.inside().equals(second)
                                    && this.treasureEntrance.outside().equals(first)));
        }

        private boolean isTreasureRoom(Cell cell) {
            return this.treasureEntrance != null && this.treasureEntrance.outside().equals(cell);
        }

        private boolean isBossApproachRoom(Cell cell) {
            for (BossEntrance entrance : this.bossEntrances) {
                if (entrance.outside().equals(cell)) {
                    return true;
                }
            }
            return false;
        }

        private void choosePrisonHall(RandomSource random, List<Cell> protectedCells) {
            List<PrisonHall> candidates = new ArrayList<>();
            for (int z = 0; z < GRID_SIZE; z++) {
                for (int x = 1; x <= GRID_SIZE - 4; x++) {
                    PrisonHall candidate = new PrisonHall(new Cell(x, z), Direction.Axis.X);
                    if (this.isValidPrisonHall(candidate, protectedCells)
                            && this.isOutsideNetworkConnected(candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 1; z <= GRID_SIZE - 4; z++) {
                    PrisonHall candidate = new PrisonHall(new Cell(x, z), Direction.Axis.Z);
                    if (this.isValidPrisonHall(candidate, protectedCells)
                            && this.isOutsideNetworkConnected(candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
            if (!candidates.isEmpty()) {
                this.prisonHall = candidates.get(random.nextInt(candidates.size()));
            }
        }

        private boolean isValidPrisonHall(PrisonHall candidate, List<Cell> protectedCells) {
            for (Cell cell : candidate.cells()) {
                if (this.isBossCell(cell)
                        || this.isTreasureRoom(cell)
                        || this.isBossApproachRoom(cell)
                        || protectedCells.contains(cell)) {
                    return false;
                }
            }
            for (Cell entrance : List.of(candidate.before(), candidate.after())) {
                if (entrance.x() < 0
                        || entrance.x() >= GRID_SIZE
                        || entrance.z() < 0
                        || entrance.z() >= GRID_SIZE
                        || this.isBossCell(entrance)
                        || this.isTreasureRoom(entrance)
                        || this.isBossApproachRoom(entrance)
                        || protectedCells.contains(entrance)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Keeps a prison hall from splitting the cells that must be reachable from the boss room.
         * The hall cells and the treasure room are intentionally excluded from this network because
         * both are dead-end areas rather than general-purpose corridors.
         */
        private boolean isOutsideNetworkConnected(PrisonHall candidate) {
            Set<Cell> excluded = Set.copyOf(candidate.cells());
            Cell root = this.bossEntrances.getFirst().outside();
            if (!this.isOutsideRouteCell(root, excluded)) {
                return false;
            }

            boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
            ArrayDeque<Cell> pending = new ArrayDeque<>();
            pending.add(root);
            visited[root.x()][root.z()] = true;
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            while (!pending.isEmpty()) {
                Cell current = pending.removeFirst();
                for (Direction direction : directions) {
                    Cell next = new Cell(
                            current.x() + direction.getStepX(), current.z() + direction.getStepZ());
                    if (!this.isOutsideRouteCell(next, excluded) || visited[next.x()][next.z()]) {
                        continue;
                    }
                    visited[next.x()][next.z()] = true;
                    pending.addLast(next);
                }
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 0; z < GRID_SIZE; z++) {
                    Cell cell = new Cell(x, z);
                    if (this.isOutsideRouteCell(cell, excluded) && !visited[x][z]) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isOutsideRouteCell(Cell cell, Set<Cell> excluded) {
            return cell.x() >= 0
                    && cell.x() < GRID_SIZE
                    && cell.z() >= 0
                    && cell.z() < GRID_SIZE
                    && !this.isBossCell(cell)
                    && !this.isTreasureRoom(cell)
                    && !excluded.contains(cell);
        }

        private boolean isPrisonCell(Cell cell) {
            return this.prisonHall != null && this.prisonHall.cells().contains(cell);
        }

        private @Nullable PrisonHall prisonHall() {
            return this.prisonHall;
        }

        private boolean isPrisonConnection(Cell first, Cell second) {
            if (this.prisonHall == null) {
                return false;
            }
            List<Cell> cells = this.prisonHall.cells();
            if (cells.contains(first) && cells.contains(second)) {
                return Math.abs(first.x() - second.x()) + Math.abs(first.z() - second.z()) == 1;
            }
            return (first.equals(this.prisonHall.before()) && second.equals(this.prisonHall.first()))
                    || (second.equals(this.prisonHall.before()) && first.equals(this.prisonHall.first()))
                    || (first.equals(this.prisonHall.after()) && second.equals(this.prisonHall.last()))
                    || (second.equals(this.prisonHall.after()) && first.equals(this.prisonHall.last()));
        }

        private BossEntrance treasureEntrance() {
            if (this.treasureEntrance == null) {
                throw new IllegalStateException("Treasure entrance is unavailable on this floor");
            }
            return this.treasureEntrance;
        }

        private boolean isBossCell(Cell cell) {
            return this.bossMinX >= 0
                    && cell.x() >= this.bossMinX
                    && cell.x() < this.bossMinX + BOSS_GRID_SIZE
                    && cell.z() >= this.bossMinZ
                    && cell.z() < this.bossMinZ + BOSS_GRID_SIZE;
        }

        private int connectionCount(Cell cell) {
            int count = 0;
            if (cell.x() > 0 && this.east[cell.x() - 1][cell.z()]) count++;
            if (cell.x() + 1 < GRID_SIZE && this.east[cell.x()][cell.z()]) count++;
            if (cell.z() > 0 && this.south[cell.x()][cell.z() - 1]) count++;
            if (cell.z() + 1 < GRID_SIZE && this.south[cell.x()][cell.z()]) count++;
            return count;
        }

        private void connect(Cell first, Cell second) {
            if (first.x() != second.x()) {
                this.east[Math.min(first.x(), second.x())][first.z()] = true;
            } else {
                this.south[first.x()][Math.min(first.z(), second.z())] = true;
            }
        }

        private boolean covers(int localX, int localZ) {
            if (this.inPrisonHall(localX, localZ)) {
                return true;
            }
            if (this.inBossBounds(localX, localZ)) {
                return this.inBossArena(localX, localZ) || this.inBossEntrancePassage(localX, localZ);
            }
            return this.inActiveRoom(localX, localZ)
                    || this.inHorizontalCorridor(localX, localZ)
                    || this.inVerticalCorridor(localX, localZ);
        }

        private boolean inPrisonHall(int localX, int localZ) {
            if (this.prisonHall == null) {
                return false;
            }
            int minX = this.prisonHall.start().x() * CELL_PITCH;
            int minZ = this.prisonHall.start().z() * CELL_PITCH;
            int maxX = this.prisonHall.axis() == Direction.Axis.X
                    ? minX + (ROOM_SIZE * 3 + CORRIDOR_LENGTH * 2) - 1
                    : minX + ROOM_SIZE - 1;
            int maxZ = this.prisonHall.axis() == Direction.Axis.Z
                    ? minZ + (ROOM_SIZE * 3 + CORRIDOR_LENGTH * 2) - 1
                    : minZ + ROOM_SIZE - 1;
            return localX >= minX && localX <= maxX && localZ >= minZ && localZ <= maxZ;
        }

        private boolean isBoundary(int localX, int localZ) {
            return !this.covers(localX - 1, localZ)
                    || !this.covers(localX + 1, localZ)
                    || !this.covers(localX, localZ - 1)
                    || !this.covers(localX, localZ + 1);
        }

        private boolean inActiveRoom(int localX, int localZ) {
            if (localX < 0 || localZ < 0 || localX >= FOOTPRINT || localZ >= FOOTPRINT) {
                return false;
            }
            int cellX = localX / CELL_PITCH;
            int cellZ = localZ / CELL_PITCH;
            return cellX < GRID_SIZE
                    && cellZ < GRID_SIZE
                    && localX % CELL_PITCH < ROOM_SIZE
                    && localZ % CELL_PITCH < ROOM_SIZE
                    && this.active[cellX][cellZ];
        }

        private boolean inHorizontalCorridor(int localX, int localZ) {
            if (localX < 0 || localZ < 0 || localX >= FOOTPRINT || localZ >= FOOTPRINT) {
                return false;
            }
            int cellX = localX / CELL_PITCH;
            int cellZ = localZ / CELL_PITCH;
            int offsetX = localX % CELL_PITCH;
            int offsetZ = localZ % CELL_PITCH;
            int center = ROOM_SIZE / 2;
            return cellX < GRID_SIZE - 1
                    && cellZ < GRID_SIZE
                    && offsetX >= ROOM_SIZE - 1
                    && offsetZ >= center - CORRIDOR_HALF_WIDTH
                    && offsetZ <= center + CORRIDOR_HALF_WIDTH
                    && this.east[cellX][cellZ];
        }

        private boolean inVerticalCorridor(int localX, int localZ) {
            if (localX < 0 || localZ < 0 || localX >= FOOTPRINT || localZ >= FOOTPRINT) {
                return false;
            }
            int cellX = localX / CELL_PITCH;
            int cellZ = localZ / CELL_PITCH;
            int offsetX = localX % CELL_PITCH;
            int offsetZ = localZ % CELL_PITCH;
            int center = ROOM_SIZE / 2;
            return cellX < GRID_SIZE
                    && cellZ < GRID_SIZE - 1
                    && offsetZ >= ROOM_SIZE - 1
                    && offsetX >= center - CORRIDOR_HALF_WIDTH
                    && offsetX <= center + CORRIDOR_HALF_WIDTH
                    && this.south[cellX][cellZ];
        }

        private boolean inBossBounds(int localX, int localZ) {
            if (this.bossMinX < 0) {
                return false;
            }
            int minX = this.bossMinX * CELL_PITCH;
            int minZ = this.bossMinZ * CELL_PITCH;
            int maxX = minX + BOSS_BOUNDS_WIDTH - 1;
            int maxZ = minZ + BOSS_BOUNDS_WIDTH - 1;
            return localX >= minX && localX <= maxX && localZ >= minZ && localZ <= maxZ;
        }

        private boolean inBossArena(int localX, int localZ) {
            if (!this.inBossBounds(localX, localZ)) {
                return false;
            }
            int dx2 = localX * 2 - this.bossCenterX2();
            int dz2 = localZ * 2 - this.bossCenterZ2();
            return dx2 * dx2 + dz2 * dz2 <= BOSS_ARENA_DIAMETER * BOSS_ARENA_DIAMETER;
        }

        private boolean inBossEntrancePassage(int localX, int localZ) {
            for (BossEntrance entrance : this.bossEntrances) {
                if (this.inBossEntrancePassage(entrance, localX, localZ)) {
                    return true;
                }
            }
            return this.treasureEntrance != null
                    && this.inBossEntrancePassage(this.treasureEntrance, localX, localZ);
        }

        private boolean inBossEntrancePassage(BossEntrance entrance, int localX, int localZ) {
            Direction side = directionFromInside(entrance);
            int centerX = this.bossCenterX();
            int centerZ = this.bossCenterZ();
            return side.getAxis() == Direction.Axis.X
                            && between(localX, this.bossGateX(side), this.bossBoundsEdgeX(side))
                            && Math.abs(localZ - centerZ) <= CORRIDOR_HALF_WIDTH
                    || side.getAxis() == Direction.Axis.Z
                            && between(localZ, this.bossGateZ(side), this.bossBoundsEdgeZ(side))
                            && Math.abs(localX - centerX) <= CORRIDOR_HALF_WIDTH;
        }

        private int bossArenaDepth(int localX, int localZ) {
            int dx2 = localX * 2 - this.bossCenterX2();
            int dz2 = localZ * 2 - this.bossCenterZ2();
            int distanceSquared = dx2 * dx2 + dz2 * dz2;
            if (distanceSquared <= 32 * 32) return 4;
            if (distanceSquared <= 42 * 42) return 3;
            if (distanceSquared <= 52 * 52) return 2;
            if (distanceSquared <= 62 * 62) return 1;
            return 0;
        }

        private boolean isBossArenaFloorLamp(int localX, int localZ, int depth) {
            int dx = localX - this.bossCenterX();
            int dz = localZ - this.bossCenterZ();
            int absX = Math.abs(dx);
            int absZ = Math.abs(dz);
            return switch (depth) {
                case 0 -> isCardinal(absX, absZ, 33);
                case 1 -> isCardinal(absX, absZ, 28);
                case 2 -> isCardinal(absX, absZ, 23);
                case 3 -> isCardinal(absX, absZ, 18);
                case 4 -> (absX == 0 && absZ == 0) || isCardinal(absX, absZ, 10);
                default -> false;
            };
        }

        private boolean isBossArenaCenter(int localX, int localZ) {
            return localX == this.bossCenterX() && localZ == this.bossCenterZ();
        }

        private boolean isBossArenaPillar(int localX, int localZ) {
            int absX = Math.abs(localX - this.bossCenterX());
            int absZ = Math.abs(localZ - this.bossCenterZ());
            return absX >= 13 && absX <= 14 && absZ >= 13 && absZ <= 14;
        }

        private void chooseBossCages(RandomSource random) {
            int minX = this.bossMinX * CELL_PITCH + 2;
            int minZ = this.bossMinZ * CELL_PITCH + 2;
            int candidateWidth = BOSS_BOUNDS_WIDTH - 4;
            for (int attempt = 0; attempt < 512 && this.bossCages.size() < BOSS_ARENA_CAGES; attempt++) {
                int centerX = minX + random.nextInt(candidateWidth);
                int centerZ = minZ + random.nextInt(candidateWidth);
                if (this.isValidBossCageSite(centerX, centerZ)) {
                    this.bossCages.add(new ArenaCage(
                            centerX, centerZ, 1 + random.nextInt(3), random.nextInt(SkullBlock.MAX + 1)));
                }
            }
            if (this.bossCages.size() != BOSS_ARENA_CAGES) {
                throw new IllegalStateException("Unable to place three spider mother cages");
            }
        }

        private boolean isValidBossCageSite(int centerX, int centerZ) {
            int centerDx = centerX - this.bossCenterX();
            int centerDz = centerZ - this.bossCenterZ();
            if (centerDx * centerDx + centerDz * centerDz < 9 * 9
                    || this.isNearBossEntrance(centerX, centerZ)) {
                return false;
            }
            int depth = this.bossArenaDepth(centerX, centerZ);
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    if (!this.inBossArena(x, z)
                            || this.isBoundary(x, z)
                            || this.bossArenaDepth(x, z) != depth
                            || this.isBossArenaPillar(x, z)
                            || this.isBossArenaFloorLamp(x, z, depth)
                            || this.isNearBossEntrance(x, z)) {
                        return false;
                    }
                }
            }
            for (ArenaCage cage : this.bossCages) {
                if (Math.abs(centerX - cage.centerX()) < 8 && Math.abs(centerZ - cage.centerZ()) < 8) {
                    return false;
                }
            }
            return true;
        }

        private @Nullable BlockState bossArenaCageState(
                int localX, int localY, int localZ, int ceilingY, ArenaPalette palette) {
            for (ArenaCage cage : this.bossCages) {
                int offsetX = localX - cage.centerX();
                int offsetZ = localZ - cage.centerZ();
                int dx = Math.abs(offsetX);
                int dz = Math.abs(offsetZ);
                int roofY = ceilingY - cage.chainLength() - 1;
                int cageFloorY = roofY - 3;
                if (dx == 0
                        && dz == 0
                        && localY > roofY
                        && localY < ceilingY) {
                    return Blocks.IRON_CHAIN.defaultBlockState();
                }
                if (dx > 1 || dz > 1 || localY < cageFloorY || localY > roofY) {
                    continue;
                }
                if (localY == cageFloorY) {
                    return palette.cageFloor();
                }
                if (localY == roofY) {
                    return palette.cageRoof();
                }
                if (dx == 1 || dz == 1) {
                    return cageBarState(palette.cageBars(), offsetX, offsetZ);
                }
                return localY == cageFloorY + 1
                        ? Blocks.SKELETON_SKULL.defaultBlockState()
                                .setValue(SkullBlock.ROTATION, cage.skullRotation())
                        : Blocks.AIR.defaultBlockState();
            }
            return null;
        }

        private static BlockState cageBarState(BlockState state, int offsetX, int offsetZ) {
            return state.setValue(CrossCollisionBlock.NORTH, isCageBarOffset(offsetX, offsetZ - 1))
                    .setValue(CrossCollisionBlock.EAST, isCageBarOffset(offsetX + 1, offsetZ))
                    .setValue(CrossCollisionBlock.SOUTH, isCageBarOffset(offsetX, offsetZ + 1))
                    .setValue(CrossCollisionBlock.WEST, isCageBarOffset(offsetX - 1, offsetZ));
        }

        private static boolean isCageBarOffset(int offsetX, int offsetZ) {
            return Math.abs(offsetX) <= 1
                    && Math.abs(offsetZ) <= 1
                    && (Math.abs(offsetX) == 1 || Math.abs(offsetZ) == 1);
        }

        private boolean isBossArenaCobweb(
                int localX,
                int localY,
                int localZ,
                int arenaFloorY,
                int ceilingY,
                int arenaDepth,
                long seed) {
            if (localY <= arenaFloorY
                    || localY >= ceilingY
                    || this.isBossArenaFloorLamp(localX, localZ, arenaDepth)
                    || this.isNearBossEntrance(localX, localZ)) {
                return false;
            }

            int aboveFloor = localY - arenaFloorY;
            long positionHash = Palette.hash(seed ^ 0x6a09e667f3bcc909L, localX, localY, localZ);
            if (aboveFloor == 1 && Math.floorMod(positionHash, 97) == 0) {
                return true;
            }
            if (localY == ceilingY - 1 && Math.floorMod(positionHash, 89) == 0) {
                return true;
            }
            if (this.isAdjacentToBossArenaWall(localX, localZ)
                    && Math.floorMod(positionHash, 101) == 0) {
                return true;
            }

            long columnHash = Palette.hash(seed ^ 0xbb67ae8584caa73bL, localX, 0, localZ);
            int hangingLength = 2 + (int) Math.floorMod(columnHash, 3);
            return Math.floorMod(columnHash, 311) == 0 && localY >= ceilingY - hangingLength;
        }

        private boolean isAdjacentToBossArenaWall(int localX, int localZ) {
            return this.isBoundary(localX - 1, localZ)
                    || this.isBoundary(localX + 1, localZ)
                    || this.isBoundary(localX, localZ - 1)
                    || this.isBoundary(localX, localZ + 1);
        }

        private boolean isNearBossEntrance(int localX, int localZ) {
            for (BossEntrance entrance : this.bossEntrances) {
                if (this.isNearBossEntrance(entrance, localX, localZ)) {
                    return true;
                }
            }
            return this.treasureEntrance != null
                    && this.isNearBossEntrance(this.treasureEntrance, localX, localZ);
        }

        private boolean isNearBossEntrance(BossEntrance entrance, int localX, int localZ) {
            Direction side = directionFromInside(entrance);
            return side.getAxis() == Direction.Axis.X
                            && Math.abs(localZ - this.bossCenterZ()) <= CORRIDOR_HALF_WIDTH + 1
                            && Math.abs(localX - this.bossGateX(side)) <= 8
                    || side.getAxis() == Direction.Axis.Z
                            && Math.abs(localX - this.bossCenterX()) <= CORRIDOR_HALF_WIDTH + 1
                            && Math.abs(localZ - this.bossGateZ(side)) <= 8;
        }

        private static boolean isCardinal(int absX, int absZ, int radius) {
            return (absX == radius && absZ == 0) || (absX == 0 && absZ == radius);
        }

        private boolean isBossGate(
                int localX, int localY, int localZ, int floorY, int ceilingY) {
            if (localY <= floorY || localY >= ceilingY) {
                return false;
            }
            for (BossEntrance entrance : this.bossEntrances) {
                Direction side = directionFromInside(entrance);
                int centerX = this.bossCenterX();
                int centerZ = this.bossCenterZ();
                int gateX = this.bossGateX(side);
                int gateZ = this.bossGateZ(side);
                if (side.getAxis() == Direction.Axis.X
                        && localX == gateX
                        && Math.abs(localZ - centerZ) <= CORRIDOR_HALF_WIDTH) {
                    return true;
                }
                if (side.getAxis() == Direction.Axis.Z
                        && localZ == gateZ
                        && Math.abs(localX - centerX) <= CORRIDOR_HALF_WIDTH) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBossGateKeyhole(int localX, int localY, int localZ, int floorY) {
            if (localY != floorY + 2) {
                return false;
            }
            for (BossEntrance entrance : this.bossEntrances) {
                Direction side = directionFromInside(entrance);
                int centerX = this.bossCenterX();
                int centerZ = this.bossCenterZ();
                int gateX = this.bossGateX(side);
                int gateZ = this.bossGateZ(side);
                if ((side.getAxis() == Direction.Axis.X && localX == gateX && localZ == centerZ)
                        || (side.getAxis() == Direction.Axis.Z && localZ == gateZ && localX == centerX)) {
                    return true;
                }
            }
            return false;
        }

        private int bossCenterX() {
            return Math.floorDiv(this.bossCenterX2(), 2);
        }

        private int bossCenterZ() {
            return Math.floorDiv(this.bossCenterZ2(), 2);
        }

        private int bossCenterX2() {
            return this.bossMinX * CELL_PITCH * 2 + BOSS_BOUNDS_WIDTH - 1;
        }

        private int bossCenterZ2() {
            return this.bossMinZ * CELL_PITCH * 2 + BOSS_BOUNDS_WIDTH - 1;
        }

        private int bossGateX(Direction side) {
            return switch (side) {
                case WEST -> (this.bossCenterX2() - BOSS_ARENA_DIAMETER + 1) / 2;
                case EAST -> (this.bossCenterX2() + BOSS_ARENA_DIAMETER - 1) / 2;
                default -> this.bossCenterX();
            };
        }

        private int bossGateZ(Direction side) {
            return switch (side) {
                case NORTH -> (this.bossCenterZ2() - BOSS_ARENA_DIAMETER + 1) / 2;
                case SOUTH -> (this.bossCenterZ2() + BOSS_ARENA_DIAMETER - 1) / 2;
                default -> this.bossCenterZ();
            };
        }

        private int bossBoundsEdgeX(Direction side) {
            return side == Direction.WEST
                    ? this.bossMinX * CELL_PITCH
                    : this.bossMinX * CELL_PITCH + BOSS_BOUNDS_WIDTH - 1;
        }

        private int bossBoundsEdgeZ(Direction side) {
            return side == Direction.NORTH
                    ? this.bossMinZ * CELL_PITCH
                    : this.bossMinZ * CELL_PITCH + BOSS_BOUNDS_WIDTH - 1;
        }

        private static boolean between(int value, int first, int second) {
            return value >= Math.min(first, second) && value <= Math.max(first, second);
        }

        private static Direction directionFromInside(BossEntrance entrance) {
            int stepX = entrance.outside().x() - entrance.inside().x();
            int stepZ = entrance.outside().z() - entrance.inside().z();
            if (stepX < 0) return Direction.WEST;
            if (stepX > 0) return Direction.EAST;
            if (stepZ < 0) return Direction.NORTH;
            if (stepZ > 0) return Direction.SOUTH;
            throw new IllegalStateException("Boss entrance does not cross a room boundary");
        }

        private boolean isRoomCenter(int localX, int localZ) {
            int offsetX = Math.floorMod(localX, CELL_PITCH);
            int offsetZ = Math.floorMod(localZ, CELL_PITCH);
            return offsetX == ROOM_SIZE / 2 && offsetZ == ROOM_SIZE / 2;
        }

        private List<Cell> farthestPairOutside(int excludedMinX, int excludedMinZ) {
            List<Cell> rooms = new ArrayList<>();
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 0; z < GRID_SIZE; z++) {
                    if (this.active[x][z]
                            && (x < excludedMinX
                                    || x >= excludedMinX + BOSS_GRID_SIZE
                                    || z < excludedMinZ
                                    || z >= excludedMinZ + BOSS_GRID_SIZE)) {
                        rooms.add(new Cell(x, z));
                    }
                }
            }
            Cell first = rooms.getFirst();
            Cell second = rooms.getLast();
            int bestDistance = -1;
            for (int left = 0; left < rooms.size(); left++) {
                for (int right = left + 1; right < rooms.size(); right++) {
                    Cell a = rooms.get(left);
                    Cell b = rooms.get(right);
                    int distance = Math.abs(a.x() - b.x()) + Math.abs(a.z() - b.z());
                    if (distance > bestDistance) {
                        bestDistance = distance;
                        first = a;
                        second = b;
                    }
                }
            }
            return List.of(first, second);
        }

        private List<Cell> activeRooms() {
            List<Cell> rooms = new ArrayList<>();
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int z = 0; z < GRID_SIZE; z++) {
                    if (this.active[x][z]) {
                        rooms.add(new Cell(x, z));
                    }
                }
            }
            return rooms;
        }
    }

    private record ArenaPalette(
            BlockState wall,
            BlockState ringFloor,
            BlockState bottomFloor,
            BlockState foundation,
            BlockState lamp,
            BlockState pillar,
            BlockState cageFloor,
            BlockState cageBars,
            BlockState cageRoof) {

        private static ArenaPalette darkStone() {
            return new ArenaPalette(
                    block("dark_stone_fancy"),
                    block("dark_stone_bricks"),
                    block("dark_stone_layers"),
                    block("dark_stone_bricks"),
                    block("dark_stone_lamp"),
                    block("dark_stone_pillar"),
                    block("dark_stone_bricks"),
                    block("dark_stone_bars"),
                    block("smooth_dark_stone_bricks"));
        }

        private BlockState floor(int depth) {
            return depth == BOSS_ARENA_DEPTH ? this.bottomFloor : this.ringFloor;
        }
    }

    private record Palette(
            BlockState floor,
            BlockState floorAccent,
            BlockState wall,
            BlockState wallAccent,
            BlockState ceiling,
            BlockState pillar,
            BlockState lamp,
            BlockState stair,
            int floorAccentRate,
            int wallAccentRate,
            int ceilingAccentRate,
            boolean centerLamp) {

        private static Palette umbra() {
            return new Palette(
                    block("umbra_stone"),
                    block("umbra_stone_cracked"),
                    block("umbra_stone"),
                    block("umbra_stone_cracked"),
                    block("umbra_stone"),
                    block("umbra_stone"),
                    block("umbra_stone_cracked"),
                    block("umbra_stone_stairs"),
                    6,
                    7,
                    7,
                    false);
        }

        private static Palette darkStone() {
            return new Palette(
                    block("dark_stone_bricks"),
                    block("smooth_dark_stone_bricks"),
                    block("dark_stone_bricks"),
                    block("chiseled_dark_stone_bricks"),
                    block("smooth_dark_stone_bricks"),
                    block("dark_stone_pillar"),
                    block("dark_stone_lamp"),
                    block("dark_stone_brick_stairs"),
                    11,
                    17,
                    Integer.MAX_VALUE,
                    true);
        }

        private BlockState floor(int x, int z, long seed) {
            return Math.floorMod(hash(seed, x, 0, z), this.floorAccentRate) == 0L
                    ? this.floorAccent
                    : this.floor;
        }

        private BlockState ceiling(LayerPlan plan, int x, int z, long seed) {
            if (this.centerLamp && plan.isRoomCenter(x, z)) {
                return this.lamp;
            }
            return Math.floorMod(hash(seed ^ 0x2545f4914f6cdd1dL, x, 0, z), this.ceilingAccentRate) == 0L
                    ? this.floorAccent
                    : this.ceiling;
        }

        private BlockState wall(int x, int y, int z, long seed) {
            return Math.floorMod(hash(seed, x, y, z), this.wallAccentRate) == 0L
                    ? this.wallAccent
                    : this.wall;
        }

        private static long hash(long seed, int x, int y, int z) {
            long value = seed ^ (long) x * 341873128712L ^ (long) y * 42317861L ^ (long) z * 132897987541L;
            value ^= value >>> 33;
            value *= 0xff51afd7ed558ccdL;
            value ^= value >>> 33;
            value *= 0xc4ceb9fe1a85ec53L;
            return value ^ value >>> 33;
        }
    }
}
