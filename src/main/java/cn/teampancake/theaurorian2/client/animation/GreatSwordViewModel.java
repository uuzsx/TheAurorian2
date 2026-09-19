package cn.teampancake.theaurorian2.client.animation;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** A camera-space adapter for the imported Blockbench rig, using native render submissions.
 * No ModelEngine billboard/FOV tricks, world entities or per-frame resource parsing are needed.
 * The actual sword is submitted through its existing item renderer, including its texture/foil.
 */
public final class GreatSwordViewModel extends SimplePreparableReloadListener<GreatSwordViewModel.Model> {
    private static final Identifier RESOURCE = TheAurorian2.id("animations/item/moonsilver_great_sword_view.json");
    private static final float RAD = (float) (Math.PI / 180);
    private static @Nullable Model loaded;

    public static @Nullable Model get() { return loaded; }

    @Override
    protected Model prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (var reader = manager.openAsReader(RESOURCE)) {
            return new Model(new Gson().fromJson(reader, Data.class));
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Cannot load moonsilver great sword first-person animations", exception);
        }
    }

    @Override
    protected void apply(Model model, ResourceManager manager, ProfilerFiller profiler) {
        loaded = model;
        GreatSwordView.reset();
    }

    record Data(int format_version, Bone[] bones, Map<String, Clip> animations) {}
    record Bone(String name, int parent, float[] pivot, float[] rotation, boolean weapon, Cube[] cubes) {}
    record Cube(float[] from, float[] to, float[] origin, float[] rotation, float inflate,
                boolean sleeve, Map<String, Face> faces) {}
    record Face(float[] uv, int rotation, String material) {}
    record Key(float time, float[] value, String interpolation) {}
    record Channel(int bone, String channel, Key[] frames) {}
    record Cue(float time, String sound) {}
    record Clip(float length, Channel[] channels, Cue[] sounds) {}
    private record Mesh(boolean sleeve, float[] vertices, float[] leftVertices,
                        float[] slimVertices, float[] leftSlimVertices) {}

    public static final class Model {
        final Data data;
        final float[][] pose;
        private final List<Mesh>[] meshes;
        private final Matrix4f[] matrices;
        private final Quaternionf rotation = new Quaternionf();
        private final ItemStackRenderState sword = new ItemStackRenderState();

        @SuppressWarnings("unchecked")
        Model(Data data) {
            if (data.format_version != 1 || data.bones.length > 64) throw new IllegalArgumentException("Invalid view rig");
            this.data = data;
            this.pose = new float[data.bones.length][9];
            this.meshes = (List<Mesh>[]) new List<?>[data.bones.length];
            this.matrices = new Matrix4f[data.bones.length];
            for (int i = 0; i < data.bones.length; i++) {
                Bone bone = data.bones[i];
                if (bone.parent >= i) throw new IllegalArgumentException("Invalid bone hierarchy");
                matrices[i] = new Matrix4f();
                meshes[i] = new ArrayList<>();
                for (Cube cube : bone.cubes) {
                    for (Face face : cube.faces.values()) {
                        if (!face.material.equals("skin")) throw new IllegalArgumentException("View rig only accepts player skin faces");
                    }
                    float[] vertices = bake(cube, bone.pivot, false, false);
                    meshes[i].add(new Mesh(cube.sleeve, vertices,
                            bake(cube, bone.pivot, true, false),
                            bake(cube, bone.pivot, false, true),
                            bake(cube, bone.pivot, true, true)));
                }
            }
            for (Clip clip : data.animations.values()) {
                if (!(clip.length > 0)) throw new IllegalArgumentException("Invalid clip duration");
                for (Channel channel : clip.channels) {
                    if (channel.bone < 0 || channel.bone >= pose.length || channel.frames.length == 0)
                        throw new IllegalArgumentException("Invalid animation channel");
                    float last = -1;
                    for (Key key : channel.frames) {
                        if (key.time < last || key.value.length != 3) throw new IllegalArgumentException("Invalid keyframe");
                        for (float v : key.value) if (!Float.isFinite(v)) throw new IllegalArgumentException("Nonfinite keyframe");
                        last = key.time;
                    }
                }
            }
        }

        void sample(String clipName, float time, float[][] output) {
            for (float[] bone : output) {
                java.util.Arrays.fill(bone, 0);
                bone[6] = bone[7] = bone[8] = 1;
            }
            Clip clip = data.animations.get(clipName);
            for (Channel channel : clip.channels) {
                int offset = switch (channel.channel) {
                    case "position" -> 0;
                    case "rotation" -> 3;
                    case "scale" -> 6;
                    default -> throw new IllegalArgumentException("Unknown channel " + channel.channel);
                };
                Key[] keys = channel.frames;
                int next = 0;
                while (next < keys.length && keys[next].time <= time) next++;
                for (int axis = 0; axis < 3; axis++) {
                    float value;
                    if (next == 0) value = keys[0].value[axis];
                    else if (next == keys.length) value = keys[next - 1].value[axis];
                    else {
                        Key a = keys[next - 1], b = keys[next];
                        float t = (time - a.time) / (b.time - a.time);
                        if (a.interpolation.equals("step")) value = a.value[axis];
                        else if (a.interpolation.equals("catmullrom") || b.interpolation.equals("catmullrom")) {
                            // Blockbench/Three.js uniform Catmull-Rom; retain unwrapped rotations for full spins.
                            float p0 = keys[Math.max(0, next - 2)].value[axis], p1 = a.value[axis];
                            float p2 = b.value[axis], p3 = keys[Math.min(keys.length - 1, next + 1)].value[axis];
                            value = .5F * ((2*p1) + (-p0+p2)*t + (2*p0-5*p1+4*p2-p3)*t*t + (-p0+3*p1-3*p2+p3)*t*t*t);
                        } else value = a.value[axis] + t * (b.value[axis] - a.value[axis]);
                    }
                    output[channel.bone][offset + axis] = value;
                }
            }
        }

        void render(RenderHandEvent event) {
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null) return;
            boolean left = player.getMainArm() == HumanoidArm.LEFT;
            boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
            boolean sleeve = player.isModelPartShown(left ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE);
            RenderType skin = RenderTypes.entityCutout(player.getSkin().body().texturePath());
            PoseStack stack = event.getPoseStack();
            stack.pushPose();
            // Camera-space framing, independent of camera pitch, window size and server render distances.
            // Withdraw and lower the entire rig so less forearm is exposed in every animation.
            stack.translate(0, -1.7, -.44);
            stack.scale((left ? -1 : 1) / 16F, 1 / 16F, 1 / 16F);
            for (int i = 0; i < data.bones.length; i++) {
                Bone bone = data.bones[i];
                float[] p = pose[i];
                Matrix4f matrix = matrices[i];
                if (bone.parent < 0) matrix.identity(); else matrix.set(matrices[bone.parent]);
                float[] parent = bone.parent < 0 ? null : data.bones[bone.parent].pivot;
                matrix.translate(bone.pivot[0] - (parent == null ? 0 : parent[0]) + p[0],
                        bone.pivot[1] - (parent == null ? 0 : parent[1]) + p[1],
                        bone.pivot[2] - (parent == null ? 0 : parent[2]) + p[2]);
                rotation.rotationZYX((bone.rotation[2] + p[5]) * RAD,
                        (bone.rotation[1] + p[4]) * RAD, (bone.rotation[0] + p[3]) * RAD);
                matrix.rotate(rotation).scale(p[6], p[7], p[8]);
                if (Math.abs(matrix.determinant3x3()) < 1E-8F) continue;
                stack.pushPose();
                stack.mulPose(matrix);
                if (bone.weapon) {
                    // Keep the approved greatsword asset intact; seat its grip in the imported hand socket.
                    stack.translate(0, .79, 0);
                    stack.mulPose(Axis.YP.rotationDegrees(90));
                    stack.scale(.6F, .6F, .6F);
                    stack.translate(0, -5, 0);
                    stack.scale(16, 16, 16);
                    mc.getItemModelResolver().updateForLiving(sword, event.getItemStack(), ItemDisplayContext.NONE, player);
                    sword.submit(stack, event.getSubmitNodeCollector(), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0);
                } else {
                    for (Mesh mesh : meshes[i]) {
                        if (mesh.sleeve && !sleeve || player.isInvisible()) continue;
                        float[] vertices = slim ? (left ? mesh.leftSlimVertices : mesh.slimVertices)
                                : (left ? mesh.leftVertices : mesh.vertices);
                        submit(event, stack, skin, vertices, left, event.getPackedLight());
                    }
                }
                stack.popPose();
            }
            stack.popPose();
        }

        private static void submit(RenderHandEvent event, PoseStack stack, RenderType type,
                float[] vertices, boolean mirrored, int light) {
            event.getSubmitNodeCollector().submitCustomGeometry(stack, type, (pose, buffer) -> {
                for (int q = 0; q < vertices.length; q += 32) {
                    for (int v = 0; v < 4; v++) {
                        int i = q + (mirrored ? 3 - v : v) * 8;
                        buffer.addVertex(pose, vertices[i], vertices[i+1], vertices[i+2]).setColor(-1)
                                .setUv(vertices[i+3], vertices[i+4])
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                                .setNormal(pose, vertices[i+5], vertices[i+6], vertices[i+7]);
                    }
                }
            });
        }
    }

    private static float[] bake(Cube cube, float[] boneOrigin, boolean left, boolean slim) {
        float x0 = cube.from[0] - cube.inflate, y0 = cube.from[1] - cube.inflate, z0 = cube.from[2] - cube.inflate;
        float x1 = cube.to[0] + cube.inflate, y1 = cube.to[1] + cube.inflate, z1 = cube.to[2] + cube.inflate;
        if (slim) { x0 += .625F; x1 -= .625F; }
        float[] origin = cube.origin == null ? boneOrigin : cube.origin;
        Matrix4f transform = new Matrix4f().translate(origin[0], origin[1], origin[2]);
        if (cube.rotation != null) transform.rotate(new Quaternionf().rotationZYX(
                cube.rotation[2] * RAD, cube.rotation[1] * RAD, cube.rotation[0] * RAD));
        transform.translate(-origin[0], -origin[1], -origin[2]);
        List<Float> output = new ArrayList<>();
        for (var entry : cube.faces.entrySet()) {
            String side = entry.getKey();
            String uvSide = left ? switch (side) { case "east" -> "west"; case "west" -> "east"; default -> side; } : side;
            Face face = cube.faces.get(uvSide);
            // CCW outside winding, same UV orientation as Blockbench's cube faces.
            float[][] corners = switch (side) {
                case "north" -> new float[][]{{x1,y0,z0},{x0,y0,z0},{x0,y1,z0},{x1,y1,z0}};
                case "south" -> new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
                case "east" -> new float[][]{{x1,y0,z1},{x1,y0,z0},{x1,y1,z0},{x1,y1,z1}};
                case "west" -> new float[][]{{x0,y0,z0},{x0,y0,z1},{x0,y1,z1},{x0,y1,z0}};
                case "up" -> new float[][]{{x1,y1,z1},{x1,y1,z0},{x0,y1,z0},{x0,y1,z1}};
                case "down" -> new float[][]{{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},{x0,y0,z0}};
                default -> throw new IllegalArgumentException("Unknown face " + side);
            };
            Vector3f a = new Vector3f(corners[1]).sub(new Vector3f(corners[0]));
            Vector3f b = new Vector3f(corners[2]).sub(new Vector3f(corners[0]));
            Vector3f normal = a.cross(b).normalize();
            transform.transformDirection(normal);
            float u0 = face.uv[0], v0 = face.uv[1], u1 = face.uv[2], v1 = face.uv[3];
            if (face.material.equals("skin")) {
                if (slim) {
                    if (uvSide.equals("south") || uvSide.equals("west")
                            || uvSide.equals("down") && Math.min(u0, u1) >= 48) { u0--; u1--; }
                    if (!uvSide.equals("east") && !uvSide.equals("west")) {
                        if (u1 > u0) u1--; else u0--;
                    }
                }
                if (left) {
                    float du = cube.sleeve ? 8 : -8, dv = cube.sleeve ? 16 : 32;
                    u0 += du; u1 += du; v0 += dv; v1 += dv;
                }
            }
            float[][] uv = {{u1,v1},{u0,v1},{u0,v0},{u1,v0}};
            for (int vertex = 0; vertex < 4; vertex++) {
                Vector3f position = transform.transformPosition(new Vector3f(corners[vertex]));
                // A reflected hand needs its left-arm UV island, not a mirrored right-arm skin.
                int uvVertex = left ? (side.equals("up") || side.equals("down") ? 3 - vertex : vertex ^ 1) : vertex;
                float[] tex = uv[(uvVertex + face.rotation / 90) % 4];
                for (float value : new float[]{position.x - boneOrigin[0], position.y - boneOrigin[1],
                        position.z - boneOrigin[2], tex[0] / 64, tex[1] / 64, normal.x, normal.y, normal.z}) output.add(value);
            }
        }
        float[] result = new float[output.size()];
        for (int i = 0; i < result.length; i++) result[i] = output.get(i);
        return result;
    }
}
