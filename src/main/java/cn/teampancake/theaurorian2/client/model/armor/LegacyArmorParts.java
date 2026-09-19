package cn.teampancake.theaurorian2.client.model.armor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.world.item.equipment.ArmorType;

/** Splits the original atlases at existing cube boundaries, only while baking models. */
final class LegacyArmorParts {
    private LegacyArmorParts() {
    }

    static CubeListBuilder body(ArmorType slot, CubeListBuilder source) {
        // Every imported torso starts with its inner and outer chest cubes; the rest is waist armor.
        List<CubeDefinition> cubes = source.getCubes();
        return selected(switch (slot) {
            case CHESTPLATE -> cubes.subList(0, 2);
            case LEGGINGS -> cubes.subList(2, cubes.size());
            default -> List.of();
        });
    }

    static CubeListBuilder leg(ArmorType slot, CubeListBuilder source, int... leggings) {
        List<CubeDefinition> cubes = source.getCubes();
        List<CubeDefinition> retained = new ArrayList<>();
        for (int index = 0; index < cubes.size(); index++) {
            boolean belongsToLeggings = false;
            for (int selected : leggings) {
                if (selected == index) {
                    belongsToLeggings = true;
                    break;
                }
            }
            if (slot == (belongsToLeggings ? ArmorType.LEGGINGS : ArmorType.BOOTS)) {
                retained.add(cubes.get(index));
            }
        }
        return selected(retained);
    }

    private static CubeListBuilder selected(List<CubeDefinition> cubes) {
        List<CubeDefinition> retained = List.copyOf(cubes);
        return new CubeListBuilder() {
            @Override
            public List<CubeDefinition> getCubes() {
                return retained;
            }
        };
    }
}
