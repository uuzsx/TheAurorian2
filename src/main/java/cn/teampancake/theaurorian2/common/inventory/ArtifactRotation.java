package cn.teampancake.theaurorian2.common.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ArtifactRotation {

    private static final String ROTATION_KEY = "theaurorian2_artifact_rotation";
    private static final int QUARTER_TURNS = 4;

    private ArtifactRotation() {
    }

    public static int quarterTurns(ItemStack stack) {
        if (!AccessoryEnhancements.isArtifact(stack)) {
            return 0;
        }
        return Math.floorMod(
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag()
                        .getIntOr(ROTATION_KEY, 0),
                QUARTER_TURNS);
    }

    public static void rotateClockwise(ItemStack stack) {
        if (!AccessoryEnhancements.isArtifact(stack)) {
            return;
        }
        int nextRotation = (quarterTurns(stack) + 1) % QUARTER_TURNS;
        CustomData.update(
                DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(ROTATION_KEY, nextRotation));
    }

    public static int rotatedRowOffset(int rowOffset, int columnOffset, int quarterTurns) {
        return switch (Math.floorMod(quarterTurns, QUARTER_TURNS)) {
            case 1 -> columnOffset;
            case 2 -> -rowOffset;
            case 3 -> -columnOffset;
            default -> rowOffset;
        };
    }

    public static int rotatedColumnOffset(int rowOffset, int columnOffset, int quarterTurns) {
        return switch (Math.floorMod(quarterTurns, QUARTER_TURNS)) {
            case 1 -> -rowOffset;
            case 2 -> -columnOffset;
            case 3 -> rowOffset;
            default -> columnOffset;
        };
    }
}
