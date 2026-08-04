package cn.teampancake.theaurorian2.common.inventory;

import cn.teampancake.theaurorian2.common.registry.ModAccessoryItems;
import net.minecraft.world.item.ItemStack;

public final class AccessoryEnhancements {

    public static final int COLUMNS = 6;
    public static final int ROWS = 4;

    private static final int[][] ADVANCE_PATTERN = {
        {-1, 0, 1},
        {-2, 0, 1}
    };
    private static final int[][] CHOICE_PATTERN = {
        {-1, 0, 2},
        {0, 1, 2}
    };
    private static final int[][] DESIRE_PATTERN = {
        {-1, -1, 1},
        {-2, -2, 1},
        {-1, 1, 1},
        {-2, 2, 1}
    };

    private AccessoryEnhancements() {
    }

    public static int[] calculate(AccessoryInventory inventory) {
        int[] levels = new int[AccessoryInventory.SLOT_COUNT];
        for (int artifactSlot = 0; artifactSlot < AccessoryInventory.SLOT_COUNT; artifactSlot++) {
            int[][] pattern = pattern(inventory.getItem(artifactSlot));
            if (pattern == null) {
                continue;
            }

            int row = artifactSlot / COLUMNS;
            int column = artifactSlot % COLUMNS;
            int rotation = ArtifactRotation.quarterTurns(inventory.getItem(artifactSlot));
            for (int[] offset : pattern) {
                int targetRow = row + ArtifactRotation.rotatedRowOffset(
                        offset[0], offset[1], rotation);
                int targetColumn = column + ArtifactRotation.rotatedColumnOffset(
                        offset[0], offset[1], rotation);
                if (!isInsideGrid(targetRow, targetColumn)) {
                    continue;
                }
                int targetSlot = targetRow * COLUMNS + targetColumn;
                levels[targetSlot] += offset[2];
            }
        }
        return levels;
    }

    public static boolean isArtifact(ItemStack stack) {
        return pattern(stack) != null;
    }

    public static boolean enhances(ItemStack artifact, int artifactSlot, int targetSlot) {
        int[][] pattern = pattern(artifact);
        if (pattern == null
                || artifactSlot < 0
                || artifactSlot >= AccessoryInventory.SLOT_COUNT
                || targetSlot < 0
                || targetSlot >= AccessoryInventory.SLOT_COUNT) {
            return false;
        }

        int rowOffset = targetSlot / COLUMNS - artifactSlot / COLUMNS;
        int columnOffset = targetSlot % COLUMNS - artifactSlot % COLUMNS;
        int rotation = ArtifactRotation.quarterTurns(artifact);
        for (int[] offset : pattern) {
            int rotatedRow = ArtifactRotation.rotatedRowOffset(offset[0], offset[1], rotation);
            int rotatedColumn = ArtifactRotation.rotatedColumnOffset(offset[0], offset[1], rotation);
            if (rotatedRow == rowOffset && rotatedColumn == columnOffset) {
                return true;
            }
        }
        return false;
    }

    private static int[][] pattern(ItemStack stack) {
        if (stack.is(ModAccessoryItems.SEALED_ARTIFACT_ADVANCE.get())) {
            return ADVANCE_PATTERN;
        }
        if (stack.is(ModAccessoryItems.SEALED_ARTIFACT_CHOICE.get())) {
            return CHOICE_PATTERN;
        }
        if (stack.is(ModAccessoryItems.SEALED_ARTIFACT_DESIRE.get())) {
            return DESIRE_PATTERN;
        }
        return null;
    }

    private static boolean isInsideGrid(int row, int column) {
        return row >= 0 && row < ROWS && column >= 0 && column < COLUMNS;
    }
}
