package cn.teampancake.theaurorian2.client.hud;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.screen.HudLayoutScreen;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.dimension.DimensionType;

public final class AurorianNightHud {

    private static final ResourceKey<DimensionType> AURORIAN_DIMENSION_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE, TheAurorian2.id("the_aurorian"));
    private static final ResourceKey<WorldClock> BLESSING_CLOCK = ResourceKey.create(
            Registries.WORLD_CLOCK, TheAurorian2.id("aurorian_blessing"));
    private static final Identifier MOON_NIGHT = texture("empty");
    private static final Identifier[] AURORIAN_NIGHTS = {
        texture("exploration"),
        texture("combat"),
        texture("protection"),
        texture("mining"),
        texture("growth")
    };
    private static final long DAY_TICKS = 24_000L;
    private static final long AURORIAN_NIGHT_END = 12_000L;
    private static final long BLESSING_CYCLE_TICKS = DAY_TICKS * AURORIAN_NIGHTS.length;
    static final int HUD_WIDTH = 45;
    static final int HUD_HEIGHT = 64;
    private static boolean visible = true;

    private AurorianNightHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (!visible
                || level == null
                || minecraft.player == null
                || minecraft.player.getData(ModAttachments.MOON_SHIELD).purified()
                || minecraft.options.hideGui
                || minecraft.screen instanceof HudLayoutScreen
                || !level.dimensionTypeRegistration().is(AURORIAN_DIMENSION_TYPE)) {
            return;
        }

        long dayTime = Math.floorMod(level.getDefaultClockTime(), DAY_TICKS);
        Identifier texture = dayTime < AURORIAN_NIGHT_END ? currentBlessingTexture(level) : MOON_NIGHT;
        HudLayoutRegistry.Position position = HudLayoutRegistry.position(
                HudLayoutRegistry.AURORIAN_BLESSING,
                graphics.guiWidth(),
                graphics.guiHeight());
        renderAt(graphics, texture, position.x(), position.y());
    }

    static void renderPreview(GuiGraphicsExtractor graphics, int x, int y) {
        renderAt(graphics, AURORIAN_NIGHTS[2], x, y);
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    private static void renderAt(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0F,
                0.0F,
                HUD_WIDTH,
                HUD_HEIGHT,
                HUD_WIDTH,
                HUD_HEIGHT);
    }

    private static Identifier currentBlessingTexture(ClientLevel level) {
        Holder<WorldClock> blessingClock = level.registryAccess().getOrThrow(BLESSING_CLOCK);
        long blessingTicks = Math.floorMod(
                level.clockManager().getTotalTicks(blessingClock), BLESSING_CYCLE_TICKS);
        int blessingIndex = (int)(blessingTicks / DAY_TICKS);
        return AURORIAN_NIGHTS[blessingIndex];
    }

    private static Identifier texture(String blessing) {
        return TheAurorian2.id("textures/misc/bless/" + blessing + ".png");
    }
}
