package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Original painted crystal pixels, with distinct light rhythms attached to the animated anatomy. */
public final class SpiderMotherGlowLayer extends GeoRenderLayer<SpiderMotherEntity, Void, GeoRenderState> {
    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/spider_mother.png");
    private static final int WIDTH = 256, HEIGHT = 128;
    private static final DataTicket<Float> GAIN = DataTickets.create("theaurorian2:spider_crystal_gain", Float.class);
    private static final RenderType SURFACE = RenderType.create("spider_mother_crystal_glow",
            RenderSetup.builder(OracleGlowRender.pipeline("spider_mother_crystal_glow"))
                    .withTexture("Sampler0", TEXTURE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).sortOnUpload().createRenderSetup());
    private final boolean[] crystalPixels;
    private final Part[] parts;

    public SpiderMotherGlowLayer(GeoRenderer<SpiderMotherEntity, Void, GeoRenderState> renderer, ResourceManager resources) {
        super(renderer);
        // Renderer construction runs during resource reload, never during entity ticking/render submission.
        crystalPixels = readCrystalPixels(resources);
        List<Part> anatomy = new ArrayList<>();
        anatomy.add(new Part("head", Style.EYES, 0));
        anatomy.add(new Part("body_2", Style.ABDOMEN, .2F));
        anatomy.add(new Part("body_3", Style.ABDOMEN, .4F));
        for (String side : new String[]{"right", "left"}) {
            float offset = side.equals("left") ? .25F : 0;
            anatomy.add(new Part("tooth_" + side, Style.ORNAMENT, offset));
            anatomy.add(new Part("fang_" + side, Style.ORNAMENT, offset + .35F));
            for (String joint : new String[]{"", "_2", "_3"}) {
                anatomy.add(new Part("leg_blade_" + side + joint, Style.BLADE, offset));
                for (int leg = 0; leg < 3; leg++) {
                    anatomy.add(new Part("leg_" + side + "_" + (char)('a' + leg) + joint,
                            joint.equals("_2") ? Style.ORNAMENT : Style.LIMB, offset + leg * .18F));
                }
            }
        }
        parts = anatomy.toArray(Part[]::new);
    }

    @Override public void addRenderData(SpiderMotherEntity entity, Void related, GeoRenderState state, float partialTick) {
        // Vanilla has no bloom/exposure lift; retain the same painted colors with a stronger surface contribution.
        state.addGeckolibData(GAIN, OracleGlowRender.shaderPackActive() ? 1F : 1.6F);
    }

    @Override public void addPerBoneRender(RenderPassInfo<GeoRenderState> pass,
            BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> register) {
        var state = (LivingEntityRenderState)(Object)pass.renderState();
        if (state.isInvisible || state.deathTime >= 40 || OracleGlowRender.shadowPass()) return;
        var bones = pass.model().boneLookup().get();
        // Explicit anatomy excludes all slash waves, circles and pillars from this body effect.
        for (Part part : parts) if (bones.get(part.name) instanceof CuboidGeoBone bone) {
            if (part.source != bone.cubes) part.bake(bone.cubes);
            if (part.cells.length > 0) register.accept(bone, part.renderer);
        }
    }

    private enum Style { EYES, ORNAMENT, LIMB, BLADE, ABDOMEN, TAIL }
    private record Cell(GeoQuad face, Style style, float[] progress) {}

    private final class Part {
        final String name;
        final Style style;
        final float offset;
        final PerBoneRender<GeoRenderState> renderer = this::submit;
        GeoCube[] source;
        Cell[] cells = new Cell[0];

        Part(String name, Style style, float offset) { this.name = name; this.style = style; this.offset = offset; }

        void submit(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector collector) {
            bone.translateAwayFromPivotPoint(pass.poseStack());
            float age = (float)pass.renderState().getAnimatableAge();
            float life = 1 - Mth.clamp(((LivingEntityRenderState)(Object)pass.renderState()).deathTime / 40F, 0, 1);
            float gain = pass.getGeckolibData(GAIN);
            Cell[] mesh = cells;
            collector.order(1).submitCustomGeometry(pass.poseStack(), SURFACE, (pose, buffer) -> {
                if (OracleGlowRender.shadowPass()) return;
                for (Cell cell : mesh) {
                    GeoQuad face = cell.face();
                    GeoVertex[] vertices = face.vertices();
                    for (int i = 0; i < vertices.length; i++) {
                        GeoVertex v = vertices[i];
                        int color = (int)(255 * life * gain * strength(cell.style(), age, cell.progress()[i], offset)) << 24 | 0xFFFFFF;
                        buffer.addVertex(pose, v.posX(), v.posY(), v.posZ()).setColor(color).setUv(v.texU(), v.texV())
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setNormal(pose, face.normalX(), face.normalY(), face.normalZ());
                    }
                }
            });
        }

        void bake(GeoCube[] cubes) {
            List<Cell> result = new ArrayList<>();
            var transform = new PoseStack();
            float[] low = new float[Style.values().length], top = new float[low.length];
            java.util.Arrays.fill(low, Float.POSITIVE_INFINITY);
            java.util.Arrays.fill(top, Float.NEGATIVE_INFINITY);
            for (int index = 0; index < cubes.length; index++) {
                GeoCube cube = cubes[index];
                Style surface = name.equals("body_3") && index == 6 ? Style.TAIL : style;
                transform.pushPose();
                cube.translateToPivotPoint(transform); cube.rotate(transform); cube.translateAwayFromPivotPoint(transform);
                var pose = transform.last();
                for (GeoQuad face : cube.quads()) {
                    if (face == null) continue;
                    GeoVertex[] v = face.vertices();
                    Vector3f edgeA = new Vector3f(v[1].posX() - v[0].posX(), v[1].posY() - v[0].posY(), v[1].posZ() - v[0].posZ());
                    Vector3f edgeB = new Vector3f(v[3].posX() - v[0].posX(), v[3].posY() - v[0].posY(), v[3].posZ() - v[0].posZ());
                    if (edgeA.cross(edgeB).lengthSquared() < 1e-10F) continue;
                    int columns = Math.max(1, Math.round(Math.max(Math.abs(v[1].texU() - v[0].texU()) * WIDTH,
                            Math.abs(v[1].texV() - v[0].texV()) * HEIGHT)));
                    int rows = Math.max(1, Math.round(Math.max(Math.abs(v[3].texU() - v[0].texU()) * WIDTH,
                            Math.abs(v[3].texV() - v[0].texV()) * HEIGHT)));
                    columns = Math.min(columns, WIDTH); rows = Math.min(rows, HEIGHT);
                    Vector3f normal = pose.normal().transform(new Vector3f(face.normalX(), face.normalY(), face.normalZ()));
                    for (int row = 0; row < rows; row++) for (int col = 0; col < columns;) {
                        if (!painted(v, (col + .5F) / columns, (row + .5F) / rows)) { col++; continue; }
                        int end = col + 1;
                        // Merge adjacent painted texels, with short spans so flow still interpolates smoothly.
                        while (end < columns && end - col < 4 && painted(v, (end + .5F) / columns, (row + .5F) / rows)) end++;
                        float a = col / (float)columns, b = end / (float)columns;
                        float c = row / (float)rows, d = (row + 1F) / rows;
                        GeoVertex[] strip = { point(v, a, c), point(v, b, c), point(v, b, d), point(v, a, d) };
                        for (int i = 0; i < strip.length; i++) {
                            GeoVertex p = strip[i];
                            var pos = pose.pose().transformPosition(new Vector3f(p.posX(), p.posY(), p.posZ()));
                            strip[i] = new GeoVertex(pos.x, pos.y, pos.z, p.texU(), p.texV());
                            float coordinate = coordinate(surface, strip[i]);
                            low[surface.ordinal()] = Math.min(low[surface.ordinal()], coordinate);
                            top[surface.ordinal()] = Math.max(top[surface.ordinal()], coordinate);
                        }
                        result.add(new Cell(new GeoQuad(strip, normal.x, normal.y, normal.z, face.direction()), surface, new float[4]));
                        col = end;
                    }
                }
                transform.popPose();
            }
            for (Cell cell : result) for (int i = 0; i < 4; i++) {
                int s = cell.style().ordinal();
                cell.progress()[i] = (coordinate(cell.style(), cell.face().vertices()[i]) - low[s]) / Math.max(.01F, top[s] - low[s]);
            }
            cells = result.toArray(Cell[]::new);
            source = cubes;
            TheAurorian2.LOGGER.debug("Baked SpiderMother glow {}: {} masked quads", name, cells.length);
        }
    }

    private boolean painted(GeoVertex[] v, float a, float b) {
        float u = Mth.lerp(b, Mth.lerp(a, v[0].texU(), v[1].texU()), Mth.lerp(a, v[3].texU(), v[2].texU()));
        float t = Mth.lerp(b, Mth.lerp(a, v[0].texV(), v[1].texV()), Mth.lerp(a, v[3].texV(), v[2].texV()));
        int x = Mth.floor(u * WIDTH), y = Mth.floor(t * HEIGHT);
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && crystalPixels[y * WIDTH + x];
    }

    private static GeoVertex point(GeoVertex[] v, float a, float b) {
        return mix(mix(v[0], v[1], a), mix(v[3], v[2], a), b);
    }
    private static GeoVertex mix(GeoVertex a, GeoVertex b, float t) {
        return new GeoVertex(Mth.lerp(t, a.posX(), b.posX()), Mth.lerp(t, a.posY(), b.posY()), Mth.lerp(t, a.posZ(), b.posZ()),
                Mth.lerp(t, a.texU(), b.texU()), Mth.lerp(t, a.texV(), b.texV()));
    }
    private static float coordinate(Style style, GeoVertex v) {
        return style == Style.ABDOMEN || style == Style.TAIL ? v.posZ() : -v.posY();
    }
    private static float strength(Style style, float age, float progress, float offset) {
        return switch (style) {
            case EYES -> .46F;
            case ORNAMENT -> .28F + .10F * (.5F + .5F * Mth.sin(age * ((float)Math.PI * 2 / 108) + offset * 6));
            case LIMB -> .24F + .22F * sweep(age, progress, offset, 76, .22F);
            case BLADE -> .28F + .27F * sweep(age, progress, offset, 54, .15F);
            case ABDOMEN -> .22F + .18F * sweep(age, progress, offset, 132, .30F);
            case TAIL -> .25F + .24F * sweep(age, progress, offset + .25F, 94, .20F);
        };
    }
    private static float sweep(float age, float progress, float offset, float period, float width) {
        float phase = age / period + offset;
        float center = (phase - Mth.floor(phase)) * 1.6F - .3F;
        float crest = Mth.clamp(1 - Math.abs(progress - center) / width, 0, 1);
        return crest * crest * (3 - 2 * crest);
    }

    private static boolean[] readCrystalPixels(ResourceManager resources) {
        boolean[] mask = new boolean[WIDTH * HEIGHT];
        try (var stream = resources.getResourceOrThrow(TEXTURE).open(); var image = NativeImage.read(stream)) {
            for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
                int pixel = image.getPixel(Math.min(image.getWidth() - 1, (int)((x + .5) * image.getWidth() / WIDTH)),
                        Math.min(image.getHeight() - 1, (int)((y + .5) * image.getHeight() / HEIGHT)));
                int red = pixel >>> 16 & 255, green = pixel >>> 8 & 255, blue = pixel & 255;
                boolean cyan = green >= 96 && blue >= 64 && green > red * 1.35 && blue > red * 1.10;
                boolean indigo = blue >= 96 && blue > red * 1.28 && (blue > green * 1.18 || green > red * 1.25);
                boolean paleTip = Math.min(red, Math.min(green, blue)) >= 175 && blue >= red - 6 && green >= red - 6;
                mask[y * WIDTH + x] = pixel >>> 24 > 32 && (cyan || indigo || paleTip);
            }
        } catch (IOException e) {
            TheAurorian2.LOGGER.warn("Cannot read SpiderMother crystal glow mask", e);
        }
        return mask;
    }
}
