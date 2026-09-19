package cn.teampancake.theaurorian2.client.model;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.model.DefaultedBlockGeoModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Keeps authored, culled doll planes lit with their actual transformed face normals. */
public final class DollGeoModel<T extends GeoAnimatable> extends DefaultedBlockGeoModel<T> {
    private @Nullable BakedGeoModel preparedModel;

    public DollGeoModel(Identifier resource) { super(resource); }

    @Override
    public BakedGeoModel getBakedModel(Identifier resource) {
        BakedGeoModel model = super.getBakedModel(resource);
        if (model != preparedModel) {
            for (GeoBone bone : model.topLevelBones()) preparePlanes(bone);
            preparedModel = model;
        }
        return model;
    }

    private static void preparePlanes(GeoBone bone) {
        if (bone instanceof CuboidGeoBone cuboid) {
            for (int i = 0; i < cuboid.cubes.length; i++) {
                GeoCube cube = cuboid.cubes[i];
                Vec3 size = cube.size();
                if (size.x() == 0 || size.y() == 0 || size.z() == 0) {
                    // GeckoLib 5.5.2's fixInvertedFlatCube flips negative components AFTER
                    // transforming normals, making tilted planes light from the wrong side.
                    // Size is only a flat-lighting hint after baking: retain the exact baked
                    // quads (vertices, UVs, winding and normals), pivot and rotation. This
                    // does NOT add thickness or separate the coplanar blink surfaces.
                    cuboid.cubes[i] = new GeoCube(cube.quads(), cube.pivot(), cube.rotation(),
                            new Vec3(size.x() == 0 ? 1 : size.x(),
                                    size.y() == 0 ? 1 : size.y(), size.z() == 0 ? 1 : size.z()));
                }
            }
        }
        for (GeoBone child : bone.children()) preparePlanes(child);
    }
}
