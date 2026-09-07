package cn.teampancake.theaurorian2.client.model;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

/** Bakes the approved Blockbench geometry once when the renderer is rebuilt. */
public final class AurorianChestModel extends Model<Float> {
    private final ModelPart lid;

    public AurorianChestModel(String variant) {
        super(load(variant), RenderTypes::entityCutoutCull);
        this.lid = root().getChild("lid");
    }

    @Override
    public void setupAnim(Float open) {
        super.setupAnim(open);
        this.lid.xRot = -open * (float) Math.toRadians(100);
    }

    private static ModelPart load(String variant) {
        var id = TheAurorian2.id("models/chest/" + variant + ".json");
        try (var reader = Minecraft.getInstance().getResourceManager().openAsReader(id)) {
            JsonObject data = GsonHelper.parse(reader);
            Map<String, ModelPart> groups = new LinkedHashMap<>();
            for (var entry : data.entrySet()) {
                JsonObject group = entry.getValue().getAsJsonObject();
                float[] pivot = vector(group.getAsJsonArray("origin"));
                Map<String, ModelPart> children = new LinkedHashMap<>();
                for (var value : group.getAsJsonArray("elements")) {
                    JsonObject element = value.getAsJsonObject();
                    float[] from = vector(element.getAsJsonArray("from"));
                    float[] to = vector(element.getAsJsonArray("to"));
                    float[] origin = vector(element.getAsJsonArray("origin"));
                    float[] rotation = vector(element.getAsJsonArray("rotation"));
                    ModelPart.Cube cube = new ModelPart.Cube(0, 0,
                            from[0] - origin[0], from[1] - origin[1], from[2] - origin[2],
                            to[0] - from[0], to[1] - from[1], to[2] - from[2],
                            0, 0, 0, false, 64, 64,
                            faces(element.getAsJsonObject("faces")));
                    // Cube's polygon array is public: replace its box UVs with exact per-face UVs.
                    int index = 0;
                    for (var face : element.getAsJsonObject("faces").entrySet()) {
                        cube.polygons[index++] = polygon(face.getKey(),
                                face.getValue().getAsJsonArray(), cube);
                    }
                    ModelPart part = new ModelPart(List.of(cube), Map.of());
                    part.setPos(origin[0] - pivot[0], origin[1] - pivot[1], origin[2] - pivot[2]);
                    part.setRotation((float) Math.toRadians(rotation[0]),
                            (float) Math.toRadians(rotation[1]), (float) Math.toRadians(rotation[2]));
                    part.setInitialPose(part.storePose());
                    children.put(Integer.toString(children.size()), part);
                }
                ModelPart part = new ModelPart(List.of(), children);
                part.setPos(pivot[0], pivot[1], pivot[2]);
                part.setInitialPose(part.storePose());
                groups.put(entry.getKey(), part);
            }
            return new ModelPart(List.of(), groups);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to bake Aurorian chest " + id, exception);
        }
    }

    private static Set<Direction> faces(JsonObject faces) {
        Set<Direction> result = java.util.EnumSet.noneOf(Direction.class);
        for (String name : faces.keySet()) result.add(Direction.valueOf(name.toUpperCase(java.util.Locale.ROOT)));
        return result;
    }

    private static float[] vector(JsonArray array) {
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static ModelPart.Polygon polygon(String face, JsonArray uv, ModelPart.Cube cube) {
        float x = cube.minX, y = cube.minY, z = cube.minZ;
        float X = cube.maxX, Y = cube.maxY, Z = cube.maxZ;
        float[][] points = {{x,y,z},{X,y,z},{X,Y,z},{x,Y,z},{x,y,Z},{X,y,Z},{X,Y,Z},{x,Y,Z}};
        int[] indices = switch (face) {
            case "north" -> new int[]{1,0,3,2};
            case "south" -> new int[]{4,5,6,7};
            case "west" -> new int[]{0,4,7,3};
            case "east" -> new int[]{5,1,2,6};
            case "up" -> new int[]{7,6,2,3};
            case "down" -> new int[]{0,1,5,4};
            default -> throw new IllegalArgumentException(face);
        };
        float u = uv.get(0).getAsFloat() / 64, v = uv.get(1).getAsFloat() / 64;
        float U = uv.get(2).getAsFloat() / 64, V = uv.get(3).getAsFloat() / 64;
        float[][] coords = {{u,V},{U,V},{U,v},{u,v}};
        ModelPart.Vertex[] vertices = new ModelPart.Vertex[4];
        for (int i = 0; i < 4; i++) {
            float[] point = points[indices[i]];
            vertices[i] = new ModelPart.Vertex(point[0], point[1], point[2], coords[i][0], coords[i][1]);
        }
        return new ModelPart.Polygon(vertices,
                Direction.valueOf(face.toUpperCase(java.util.Locale.ROOT)).getUnitVec3f());
    }
}
