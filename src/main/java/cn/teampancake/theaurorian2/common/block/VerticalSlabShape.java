package cn.teampancake.theaurorian2.common.block;

import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum VerticalSlabShape implements StringRepresentable {
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    FULL(null);

    private final @Nullable Direction direction;

    VerticalSlabShape(@Nullable Direction direction) {
        this.direction = direction;
    }

    public @Nullable Direction direction() {
        return this.direction;
    }

    public static VerticalSlabShape fromDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> FULL;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
