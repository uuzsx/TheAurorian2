"""Port original armor meshes and assign their existing cubes to the correct equipment slots."""
import json
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
legacy = Path(sys.argv[1]) / 'src/main'
models = legacy / 'java/cn/teampancake/theaurorian/client/model/entity/armor'
target = root / 'src/main/java/cn/teampancake/theaurorian2/client/model/armor'
assets = 'src/main/resources/assets/theaurorian2/'
styles = {
    'cerulean': 'CeruleanArmorModel', 'crystal_rune': 'CrystalRuneArmorModel',
    'knight': 'KnightArmorModel', 'moonsilver': 'MoonsilverArmorModel',
    'mysterium_wool': 'MysteriumWoolArmorModel', 'spectral': 'SpectralArmorModel',
    'spiked_chestplate': 'SpikedChestplateModel', 'aurorian_slime_boots': 'AurorianSlimeBootsModel',
}
parts = {'helmet': 'head_armor', 'chestplate': 'chest_armor', 'leggings': 'leg_armor', 'boots': 'foot_armor'}
pending = {}

# These are authored waist/skirt parts, even though the legacy files parent them to the torso.
waist_children = {
    'cerulean': {'cube_r1', 'cube_r2'},
    'knight': {'body_r2', 'body_r3', 'body_r4', 'body_r5'},
    'moonsilver': {'cube_r1', 'cube_r2', 'cube_r3'},
    'crystal_rune': {'cube_r1', 'cube_r2', 'cube_r3', 'cube_r4'},
    'spectral': set(), 'mysterium_wool': set(),
}
leg_cubes = {
    'spectral': {'right_leg': [0, 1, 3], 'left_leg': [0, 2, 3]},
    'mysterium_wool': {'right_leg': [0, 1], 'left_leg': [0, 1]},
}


def partition_mesh(mesh, style):
    mesh = mesh.replace('createBodyLayer()', 'createBodyLayer(ArmorType type)', 1)

    def split_cubes(match):
        prefix, part, builder = match.group(1, 2, 3)
        if part == 'body':
            selected = f'LegacyArmorParts.body(type, {builder})'
        else:
            indices = leg_cubes.get(style, {}).get(part, [])
            suffix = ''.join(f', {index}' for index in indices)
            selected = f'LegacyArmorParts.leg(type, {builder}{suffix})'
        return prefix + selected + match.group(4)

    mesh, count = re.subn(
        r'(partDefinition\.addOrReplaceChild\("(body|right_leg|left_leg)", )'
        r'(CubeListBuilder\.create\(\).*?)(, PartPose\.[^;]+;)', split_cubes, mesh, flags=re.S)
    assert count == 3, style
    body_var = re.search(r'PartDefinition (\w+) = partDefinition\.addOrReplaceChild\("body"', mesh)
    if body_var:
        pattern = rf'        {body_var.group(1)}\.addOrReplaceChild\("([^"]+)",.*?;\n'
        found = set()

        def split_child(match):
            child = match.group(1)
            owner = 'LEGGINGS' if child in waist_children[style] else 'CHESTPLATE'
            found.add(child)
            statement = '\n'.join('    ' + line for line in match.group(0).rstrip().splitlines())
            return f'        if (type == ArmorType.{owner}) {{\n{statement}\n        }}\n'

        mesh = re.sub(pattern, split_child, mesh, flags=re.S)
        assert waist_children[style] <= found, style
    else:
        assert not waist_children[style], style
    return mesh


def geometry_tokens(source):
    tokens = re.findall(r'\.(?:texOffs|mirror)\([^)]*\)|'
                        r'\.addBox\([^;]*?new CubeDeformation\([^)]*\)\)|'
                        r'PartPose\.\w+\([^)]*\)', source)
    assert sum(token.startswith('.addBox(') for token in tokens) == source.count('.addBox(')
    return tokens
target.mkdir(parents=True, exist_ok=True)
for style, name in styles.items():
    source = (models / f'{name}.java').read_text(encoding='utf-8')
    mesh = source[source.index('    public static LayerDefinition createBodyLayer()'):source.rfind('}')]
    old_hat = 'partDefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));'
    if old_hat not in mesh:
        old_hat = 'partDefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);'
    assert mesh.count(old_hat) == 1, name
    # Since 1.21.2 HumanoidModel expects the empty hat node inside head, not at root.
    mesh = mesh.replace(old_hat, old_hat.replace('partDefinition.', 'partDefinition.getChild("head").'))
    if style in waist_children:
        mesh = partition_mesh(mesh, style)
    header = '''package cn.teampancake.theaurorian2.client.model.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Original geometry and UVs; equipment slots are separated without moving or duplicating cubes.
'''
    if style in waist_children:
        header = header.replace('import net.minecraft.client.model.geom.PartPose;',
                                'import net.minecraft.world.item.equipment.ArmorType;\n'
                                'import net.minecraft.client.model.geom.PartPose;')
    else:
        header = header.replace('// Original geometry and UVs; equipment slots are separated without moving or duplicating cubes.',
                                '// Original legacy geometry; only the empty hat node adapts to the current humanoid hierarchy.')
    output = header + f'public final class {name} {{\n    private {name}() {{\n    }}\n\n' + mesh + '}\n'
    assert geometry_tokens(source) == geometry_tokens(output), name
    (target / f'{name}.java').write_text(output, encoding='utf-8')
    tex_name = 'mysterium_armor' if style == 'mysterium_wool' else style if style in ('spiked_chestplate', 'aurorian_slime_boots') else style + '_armor'
    relative = f'textures/models/armor/{tex_name}.png'
    original = (legacy / 'resources/assets/theaurorian' / relative).read_bytes()
    existing = root / assets / relative
    assert existing.read_bytes() == original, f'Changed local atlas: {relative}'
    print(f'{style}: preserved {mesh.count(".addBox(")} cubes and original texture')

old_assets = legacy / 'resources/assets/theaurorian'
geo = json.loads((old_assets / 'geo/item/holy_knight_armor.geo.json').read_text(encoding='utf-8'))
assert len(geo['minecraft:geometry']) == 1
bones = geo['minecraft:geometry'][0]['bones']
assert sum(len(b.get('cubes', [])) for b in bones) == 95
robe = next(b for b in bones if b['name'] == 'armorLegs')
# Keep authored robe geometry/pivot intact, attaching crouch rotation at the torso.
bones.insert(bones.index(robe), {'name': 'waist_attachment', 'parent': robe['parent'], 'pivot': [0, 24, 0]})
robe['parent'] = 'waist_attachment'
pending[assets + 'geckolib/models/armor/holy_knight_armor.geo.json'] = geo
pending[assets + 'geckolib/animations/armor/holy_knight_armor.animation.json'] = json.loads(
    (old_assets / 'animations/item/holy_knight_armor.animation.json').read_text(encoding='utf-8'))
assert (root / assets / 'textures/item/holy_knight_armor.png').read_bytes() == (old_assets / 'textures/item/holy_knight_armor.png').read_bytes()
# One undyed equipment layer. IClientItemExtensions chooses the exact legacy atlas per item.
pending[assets + 'equipment/legacy_armor.json'] = {'layers': {
    kind: [{'texture': 'minecraft:iron'}] for kind in ('humanoid', 'humanoid_leggings', 'humanoid_baby')}}
for part, tag in parts.items():
    path = f'src/main/resources/data/minecraft/tags/item/{tag}.json'
    data = json.loads((root / path).read_text(encoding='utf-8'))
    ids = [f'theaurorian2:{style}_{part}' for style in list(styles)[:6] + ['holy_knight']]
    if part == 'chestplate': ids.append('theaurorian2:spiked_chestplate')
    if part == 'boots': ids.append('theaurorian2:aurorian_slime_boots')
    data['values'] = list(dict.fromkeys(data['values'] + ids))
    pending[path] = data
(root / 'build/legacy-armor-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
