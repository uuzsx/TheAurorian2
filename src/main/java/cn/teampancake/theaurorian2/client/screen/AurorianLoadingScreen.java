package cn.teampancake.theaurorian2.client.screen;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** First-arrival artwork with real client-side reception progress for the local 3x3 chunks. */
public final class AurorianLoadingScreen extends LevelLoadingScreen {
    private static final Identifier BACKGROUND = TheAurorian2.id("textures/gui/aurorian_first_arrival.png");
    private static final int IMAGE_WIDTH = 1672;
    private static final int IMAGE_HEIGHT = 941;
    private static final int TOTAL_CHUNKS = 9;
    private static final long MINIMUM_VISIBLE_MS = 1_000L;
    private static final long NEIGHBOR_WAIT_LIMIT_MS = 30_000L;
    private static final Component TIP = Component.translatable("gui.theaurorian2.loading.tip.1");
    private static final Component[] LOADING_TEXT = {
        Component.translatable("gui.theaurorian2.loading.world", ""),
        Component.translatable("gui.theaurorian2.loading.world", "."),
        Component.translatable("gui.theaurorian2.loading.world", ".."),
        Component.translatable("gui.theaurorian2.loading.world", "...")
    };
    private final long startedAt = Util.getMillis();
    private long firstShownAt = -1L;
    private int loadedChunks;
    private Component chunkText = Component.translatable("gui.theaurorian2.loading.chunks", 0, TOTAL_CHUNKS);

    public AurorianLoadingScreen(LevelLoadTracker tracker, Reason reason) {
        super(tracker, reason);
    }

    public static LevelLoadingScreen create(LevelLoadTracker tracker, Reason reason) {
        // The transition factory runs before the new LocalPlayer replaces the old
        // one, so this reads the source player's server-synchronized visit record.
        var player = Minecraft.getInstance().player;
        return player != null && !player.getData(ModAttachments.AURORIAN_TRAVEL).enteredAurorian()
                ? new AurorianLoadingScreen(tracker, reason) : new LevelLoadingScreen(tracker, reason);
    }

    @Override public void tick() {
        int received = 0;
        if (minecraft.level != null && minecraft.player != null
                && minecraft.player.level() == minecraft.level
                && minecraft.level.dimension().equals(TheAurorian2.AURORIAN_LEVEL)) {
            ChunkPos center = minecraft.player.chunkPosition();
            var chunks = minecraft.level.getChunkSource();
            // No generation, placeholders, global totals or work after this screen
            // closes. An actual cached FULL chunk is one completed unit.
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (chunks.getChunk(center.x() + x, center.z() + z, ChunkStatus.FULL, false) != null) received++;
                }
            }
        }
        if (loadedChunks != received) {
            loadedChunks = received;
            chunkText = Component.translatable("gui.theaurorian2.loading.chunks", received, TOTAL_CHUNKS);
        }
        super.tick(); // Vanilla still controls server readiness and center-chunk mesh readiness.
    }

    @Override public void onClose() {
        long now = Util.getMillis();
        // Start the minimum display time at the first rendered frame, not during
        // construction/loading. Fast loads may show real 9/9 while this finishes.
        if (firstShownAt < 0L || now - firstShownAt < MINIMUM_VISIBLE_MS) return;
        // Wait for the advertised neighborhood, but keep an escape for a stalled
        // chunk stream. A timeout never fabricates completed chunks or 100%.
        if (loadedChunks < TOTAL_CHUNKS && now - startedAt < NEIGHBOR_WAIT_LIMIT_MS) return;
        super.onClose();
    }

    @Override protected void updateNarratedWidget(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, LOADING_TEXT[0].copy().append(" ").append(chunkText));
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float scale = Math.max((float) width / IMAGE_WIDTH, (float) height / IMAGE_HEIGHT);
        float uSpan = width / (IMAGE_WIDTH * scale);
        float vSpan = height / (IMAGE_HEIGHT * scale);
        // Fill the viewport with an aspect-preserving, centered crop; no letterboxing.
        graphics.blit(BACKGROUND, 0, 0, width, height,
                (1F - uSpan) * 0.5F, (1F + uSpan) * 0.5F,
                (1F - vSpan) * 0.5F, (1F + vSpan) * 0.5F);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        if (firstShownAt < 0L) firstShownAt = now;
        extractBackground(graphics, mouseX, mouseY, partialTick);
        // Keep the controls inside the viewport independently of the background crop.
        float scale = Math.min((float) width / IMAGE_WIDTH, (float) height / IMAGE_HEIGHT);
        int imageWidth = Math.round(IMAGE_WIDTH * scale);
        int imageHeight = Math.round(IMAGE_HEIGHT * scale);
        int top = (height - imageHeight) / 2;
        int center = width / 2;
        int lineY = top + Math.round(imageHeight * 0.82F);
        int barY = top + Math.round(imageHeight * 0.885F);
        int countY = top + Math.round(imageHeight * 0.925F);
        int barWidth = Math.round(imageWidth * 0.32F);
        int left = center - barWidth / 2;
        int barHeight = Math.max(2, Math.round(imageHeight * 0.004F));
        int filledWidth = barWidth * loadedChunks / TOTAL_CHUNKS;
        Component loading = LOADING_TEXT[(int) ((now - startedAt) / 400L % LOADING_TEXT.length)];
        graphics.text(font, loading, center - font.width(LOADING_TEXT[3]) / 2, lineY, 0xFFD4E7FF, false);
        graphics.fill(left - 1, barY - 1, left + barWidth + 1, barY + barHeight + 1, 0xFF526387);
        graphics.fill(left, barY, left + barWidth, barY + barHeight, 0xFF111626);
        if (filledWidth > 0) {
            graphics.fill(left, barY, left + filledWidth, barY + barHeight, 0xFF7AA6FF);
            graphics.fill(left, barY, left + filledWidth, barY + 1, 0xFFC2DFFF);
            graphics.fill(left + filledWidth - 1, barY - 1, left + filledWidth + 1, barY + barHeight + 1, 0xFFE4F3FF);
        }
        graphics.text(font, chunkText, center - font.width(chunkText) / 2, countY, 0xFF8FA9CC, false);
        graphics.pose().pushMatrix();
        graphics.pose().translate(width - 10F, height - 10F);
        graphics.pose().scale(0.75F, 0.75F);
        float pulse = (float) (0.5 - 0.5 * Math.cos((now - firstShownAt) % 2_400L * (Math.PI * 2.0 / 2_400.0)));
        int tipColor = ARGB.srgbLerp(pulse, 0xFF8FA9CC, 0xFFC2DFFF);
        graphics.text(font, TIP, -font.width(TIP), -font.lineHeight, tipColor, false);
        graphics.pose().popMatrix();
    }
}
