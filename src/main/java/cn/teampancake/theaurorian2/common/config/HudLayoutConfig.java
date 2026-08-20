package cn.teampancake.theaurorian2.common.config;

import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class HudLayoutConfig {

    private static final int MIN_OFFSET = -32_768;
    private static final int MAX_OFFSET = 32_768;
    private static final Map<Element, Entry> ENTRIES = new EnumMap<>(Element.class);
    private static final ModConfigSpec.EnumValue<MoonShieldStyle> MOON_SHIELD_STYLE;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud_layout");
        for (Element element : Element.values()) {
            builder.push(element.configPath);
            ENTRIES.put(element, new Entry(
                    builder.defineEnum("anchor", element.defaultAnchor),
                    builder.defineInRange("offset_x", 0, MIN_OFFSET, MAX_OFFSET),
                    builder.defineInRange("offset_y", 0, MIN_OFFSET, MAX_OFFSET)));
            builder.pop();
        }
        builder.pop();
        builder.push("hud_style");
        MOON_SHIELD_STYLE = builder.defineEnum("moon_shield", MoonShieldStyle.HORIZONTAL);
        builder.pop();
        SPEC = builder.build();
    }

    private HudLayoutConfig() {
    }

    public static Anchor anchor(Element element) {
        return ENTRIES.get(element).anchor.get();
    }

    public static int offsetX(Element element) {
        return ENTRIES.get(element).offsetX.getAsInt();
    }

    public static int offsetY(Element element) {
        return ENTRIES.get(element).offsetY.getAsInt();
    }

    public static void set(Element element, Anchor anchor, int offsetX, int offsetY) {
        Entry entry = ENTRIES.get(element);
        entry.anchor.set(anchor);
        entry.offsetX.set(offsetX);
        entry.offsetY.set(offsetY);
    }

    public static void reset(Element element) {
        set(element, element.defaultAnchor, 0, 0);
    }

    public static void save() {
        SPEC.save();
    }

    public static MoonShieldStyle moonShieldStyle() {
        return MOON_SHIELD_STYLE.get();
    }

    public static void setMoonShieldStyle(MoonShieldStyle style) {
        MOON_SHIELD_STYLE.set(style);
    }

    public enum MoonShieldStyle {
        HORIZONTAL,
        VERTICAL;

        public MoonShieldStyle next() {
            return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
        }
    }

    public enum Element {
        AURORIAN_BLESSING("aurorian_blessing", Anchor.TOP_LEFT),
        MOON_SHIELD("moon_shield", Anchor.BOTTOM_LEFT);

        private final String configPath;
        private final Anchor defaultAnchor;

        Element(String configPath, Anchor defaultAnchor) {
            this.configPath = configPath;
            this.defaultAnchor = defaultAnchor;
        }
    }

    public enum Anchor {
        TOP_LEFT(Horizontal.LEFT, Vertical.TOP),
        TOP_CENTER(Horizontal.CENTER, Vertical.TOP),
        TOP_RIGHT(Horizontal.RIGHT, Vertical.TOP),
        CENTER_LEFT(Horizontal.LEFT, Vertical.CENTER),
        CENTER(Horizontal.CENTER, Vertical.CENTER),
        CENTER_RIGHT(Horizontal.RIGHT, Vertical.CENTER),
        BOTTOM_LEFT(Horizontal.LEFT, Vertical.BOTTOM),
        BOTTOM_CENTER(Horizontal.CENTER, Vertical.BOTTOM),
        BOTTOM_RIGHT(Horizontal.RIGHT, Vertical.BOTTOM);

        private final Horizontal horizontal;
        private final Vertical vertical;

        Anchor(Horizontal horizontal, Vertical vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public int baseX(int screenWidth, int elementWidth) {
            return this.horizontal.base(screenWidth, elementWidth);
        }

        public int baseY(int screenHeight, int elementHeight) {
            return this.vertical.base(screenHeight, elementHeight);
        }

        public static Anchor nearest(
                int x,
                int y,
                int screenWidth,
                int screenHeight,
                int elementWidth,
                int elementHeight) {
            Horizontal horizontal = Horizontal.nearest(x, screenWidth, elementWidth);
            Vertical vertical = Vertical.nearest(y, screenHeight, elementHeight);
            for (Anchor anchor : values()) {
                if (anchor.horizontal == horizontal && anchor.vertical == vertical) {
                    return anchor;
                }
            }
            throw new IllegalStateException("Missing HUD anchor");
        }
    }

    private record Entry(
            ModConfigSpec.EnumValue<Anchor> anchor,
            ModConfigSpec.IntValue offsetX,
            ModConfigSpec.IntValue offsetY) {
    }

    private enum Horizontal {
        LEFT,
        CENTER,
        RIGHT;

        private int base(int screenWidth, int elementWidth) {
            return switch (this) {
                case LEFT -> 0;
                case CENTER -> (screenWidth - elementWidth) / 2;
                case RIGHT -> screenWidth - elementWidth;
            };
        }

        private static Horizontal nearest(int x, int screenWidth, int elementWidth) {
            Horizontal closest = LEFT;
            int closestDistance = Integer.MAX_VALUE;
            for (Horizontal candidate : values()) {
                int distance = Math.abs(x - candidate.base(screenWidth, elementWidth));
                if (distance < closestDistance) {
                    closest = candidate;
                    closestDistance = distance;
                }
            }
            return closest;
        }
    }

    private enum Vertical {
        TOP,
        CENTER,
        BOTTOM;

        private int base(int screenHeight, int elementHeight) {
            return switch (this) {
                case TOP -> 0;
                case CENTER -> (screenHeight - elementHeight) / 2;
                case BOTTOM -> screenHeight - elementHeight;
            };
        }

        private static Vertical nearest(int y, int screenHeight, int elementHeight) {
            Vertical closest = TOP;
            int closestDistance = Integer.MAX_VALUE;
            for (Vertical candidate : values()) {
                int distance = Math.abs(y - candidate.base(screenHeight, elementHeight));
                if (distance < closestDistance) {
                    closest = candidate;
                    closestDistance = distance;
                }
            }
            return closest;
        }
    }

}
