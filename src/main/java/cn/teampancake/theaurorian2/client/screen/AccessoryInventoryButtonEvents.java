package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.inventory.AccessoryMenuExtension;
import cn.teampancake.theaurorian2.common.inventory.AccessoryEnhancements;
import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.inventory.AccessorySlot;
import cn.teampancake.theaurorian2.common.network.RotateArtifactPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class AccessoryInventoryButtonEvents {

    private static final int SLOT_BORDER_SIZE = 18;
    private static final int BORDER_PERIMETER = 68;
    private static final int LINE_LENGTH = 10;
    private static final long LINE_STEP_MILLIS = 55L;
    private static final float APPEAR_MILLIS = 120.0F;
    private static final float DISAPPEAR_MILLIS = 150.0F;

    private static final WidgetSprites BUTTON_SPRITE =
            new WidgetSprites(TheAurorian2.id("accessory_inventory_button"));
    private static final Identifier ACCESSORY_PANEL =
            TheAurorian2.id("textures/gui/container/accessories.png");
    private static boolean open;
    private static int animatedArtifactSlot = -1;
    private static float lineVisibility;
    private static boolean rotateKeyHeld;
    private static long lastAnimationNanos = System.nanoTime();

    private AccessoryInventoryButtonEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }

        ImageButton button = new ImageButton(
                screen.getLeftPos() + 60,
                screen.getTopPos() + 6,
                16,
                16,
                BUTTON_SPRITE,
                ignored -> setOpen(screen, !open),
                Component.translatable("gui.theaurorian2.open_accessories"));
        button.setTooltip(Tooltip.create(Component.translatable("gui.theaurorian2.open_accessories")));
        event.addListener(button);
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_R) {
            return;
        }
        if (rotateKeyHeld) {
            return;
        }
        rotateKeyHeld = true;
        if (!(event.getScreen() instanceof InventoryScreen screen)
                || !(screen.getHoveredSlot() instanceof AccessorySlot slot)
                || !slot.isActive()
                || !AccessoryEnhancements.isArtifact(slot.getItem())) {
            return;
        }

        event.setCanceled(true);
        ClientPacketDistributor.sendToServer(
                new RotateArtifactPayload(slot.getContainerSlot()));
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        if (event.getKeyCode() == GLFW.GLFW_KEY_R) {
            rotateKeyHeld = false;
        }
    }

    public static void extractAccessoryLayer(
            InventoryScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!open) {
            return;
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ACCESSORY_PANEL,
                screen.getLeftPos() + screen.getImageWidth() + 4,
                screen.getTopPos(),
                0.0F,
                0.0F,
                128,
                94,
                256,
                256);

        AccessoryInventory inventory =
                ((AccessoryMenuExtension) screen.getMenu()).theaurorian2$getAccessoryInventory();
        int[] enhancementLevels = AccessoryEnhancements.calculate(inventory);
        int panelX = screen.getLeftPos() + screen.getImageWidth() + 4;
        int panelY = screen.getTopPos();
        int hoveredArtifactSlot = findHoveredArtifact(
                inventory, panelX, panelY, mouseX, mouseY);
        updateLineVisibility(hoveredArtifactSlot);
        for (int slot = 0; slot < enhancementLevels.length; slot++) {
            int level = enhancementLevels[slot];
            if (level <= 0) {
                continue;
            }
            int x = panelX + 10 + slot % AccessoryEnhancements.COLUMNS * 18;
            int y = panelY + 11 + slot / AccessoryEnhancements.COLUMNS * 18;
            if (lineVisibility > 0.0F
                    && animatedArtifactSlot >= 0
                    && AccessoryEnhancements.enhances(
                            inventory.getItem(animatedArtifactSlot), animatedArtifactSlot, slot)) {
                renderOrbitingLines(graphics, x, y, lineVisibility);
            }
            renderEnhancementLevel(graphics, x, y, level);
        }
    }

    private static int findHoveredArtifact(
            AccessoryInventory inventory, int panelX, int panelY, int mouseX, int mouseY) {
        for (int slot = 0; slot < AccessoryInventory.SLOT_COUNT; slot++) {
            if (!AccessoryEnhancements.isArtifact(inventory.getItem(slot))) {
                continue;
            }
            int x = panelX + 10 + slot % AccessoryEnhancements.COLUMNS * 18;
            int y = panelY + 11 + slot / AccessoryEnhancements.COLUMNS * 18;
            if (mouseX >= x && mouseX < x + SLOT_BORDER_SIZE
                    && mouseY >= y && mouseY < y + SLOT_BORDER_SIZE) {
                return slot;
            }
        }
        return -1;
    }

    private static void updateLineVisibility(int hoveredArtifactSlot) {
        long now = System.nanoTime();
        float elapsedMillis = Math.min(50.0F, (now - lastAnimationNanos) / 1_000_000.0F);
        lastAnimationNanos = now;
        if (hoveredArtifactSlot >= 0) {
            if (animatedArtifactSlot != hoveredArtifactSlot) {
                animatedArtifactSlot = hoveredArtifactSlot;
                lineVisibility = 0.0F;
            }
            lineVisibility = Math.min(1.0F, lineVisibility + elapsedMillis / APPEAR_MILLIS);
        } else {
            lineVisibility = Math.max(0.0F, lineVisibility - elapsedMillis / DISAPPEAR_MILLIS);
            if (lineVisibility <= 0.0F) {
                animatedArtifactSlot = -1;
            }
        }
    }

    private static void renderOrbitingLines(
            GuiGraphicsExtractor graphics, int x, int y, float visibility) {
        long elapsedMillis = System.nanoTime() / 1_000_000L;
        int phase = (int) ((elapsedMillis / LINE_STEP_MILLIS) % BORDER_PERIMETER);
        int visibleLength = Math.max(1, Math.round(LINE_LENGTH * visibility));
        renderLineSegment(graphics, x, y, phase, visibleLength, visibility);
        renderLineSegment(
                graphics,
                x,
                y,
                (phase + BORDER_PERIMETER / 2) % BORDER_PERIMETER,
                visibleLength,
                visibility);
    }

    private static void renderLineSegment(
            GuiGraphicsExtractor graphics, int x, int y, int start, int visibleLength, float visibility) {
        for (int offset = 0; offset < visibleLength; offset++) {
            int position = (start + offset) % BORDER_PERIMETER;
            int pixelX;
            int pixelY;
            if (position < SLOT_BORDER_SIZE) {
                pixelX = x + position;
                pixelY = y;
            } else if (position < SLOT_BORDER_SIZE + SLOT_BORDER_SIZE - 1) {
                pixelX = x + SLOT_BORDER_SIZE - 1;
                pixelY = y + position - SLOT_BORDER_SIZE + 1;
            } else if (position < SLOT_BORDER_SIZE + (SLOT_BORDER_SIZE - 1) * 2) {
                pixelX = x + SLOT_BORDER_SIZE - 2
                        - (position - (SLOT_BORDER_SIZE + SLOT_BORDER_SIZE - 1));
                pixelY = y + SLOT_BORDER_SIZE - 1;
            } else {
                pixelX = x;
                pixelY = y + SLOT_BORDER_SIZE - 2
                        - (position - (SLOT_BORDER_SIZE + (SLOT_BORDER_SIZE - 1) * 2));
            }
            float gradient = 1.0F - offset / (float) Math.max(1, LINE_LENGTH - 1);
            int shade = Math.round(112.0F + 143.0F * gradient);
            int alpha = Math.round(255.0F * visibility);
            int color = alpha << 24 | shade << 16 | shade << 8 | shade;
            graphics.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, color);
        }
    }

    private static void renderEnhancementLevel(
            GuiGraphicsExtractor graphics, int x, int y, int level) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 2.0F, y + 2.0F);
        graphics.pose().scale(0.4F, 0.4F);
        graphics.text(Minecraft.getInstance().font, "+" + level, 0, 0, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            setOpen(screen, false);
            animatedArtifactSlot = -1;
            lineVisibility = 0.0F;
            rotateKeyHeld = false;
            lastAnimationNanos = System.nanoTime();
        }
    }

    private static void setOpen(InventoryScreen screen, boolean value) {
        open = value;
        ((AccessoryMenuExtension) screen.getMenu()).theaurorian2$setAccessoriesOpen(value);
    }
}
