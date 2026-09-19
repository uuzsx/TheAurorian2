"""Import all five authored armor variants. Run through import_assassin_armor.ps1."""
import argparse
import itertools
import json
import math
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
RAW = args.source / 'raw'
ASSETS = 'src/main/resources/assets/theaurorian2/'
DATA = 'src/main/resources/data/'
VARIANTS = (
    ('mistveil_assassin', '雾隐刺客', 'Mistveil Assassin', 'assassin_armor'),
    ('gloamgold_assassin', '暮金刺客', 'Gloamgold Assassin', 'assassin_armor_tier2'),
    ('startrace_assassin', '星痕刺客', 'Startrace Assassin', 'assassin_armor_tier3'),
    ('embershade_assassin', '烬影刺客', 'Embershade Assassin', 'assassin_armor_tier4'),
    ('frostmoon_assassin', '寒月刺客', 'Frostmoon Assassin', 'assassin_armor_t5'),
)
PARTS = {
    'helmet': ('头盔', 'Hood', ('head',), 'head_armor'),
    'chestplate': ('护甲', 'Chestguard', ('body', 'left_arm', 'right_arm'), 'chest_armor'),
    'leggings': ('护腿', 'Leggings', ('left_leg', 'right_leg'), 'leg_armor'),
    'boots': ('长靴', 'Boots', ('left_feet', 'right_feet'), 'foot_armor'),
}
pending = {}
textures = []
tags = {}
languages = {lang: json.loads((ROOT / ASSETS / 'lang' / f'{lang}.json').read_text(encoding='utf-8'))
             for lang in ('zh_cn', 'en_us')}


def rotate(v, angles, order):
    v = list(v)
    for axis in order:
        angle = math.radians(angles[axis])
        a, b = ((1, 2), (2, 0), (0, 1))[axis]
        c, s = math.cos(angle), math.sin(angle)
        v[a], v[b] = c*v[a]-s*v[b], s*v[a]+c*v[b]
    return v


def corners(e):
    inflate = e.get('inflate', 0)
    origin = e['origin']
    for p in itertools.product(*[(a-inflate, b+inflate) for a, b in zip(e['from'], e['to'])]):
        p = rotate([a-b for a, b in zip(p, origin)], e.get('rotation', [0, 0, 0]), (0, 1, 2))
        yield [p[i]+origin[i] for i in range(3)]


def item_transform(points, rotation, scale, translation=(0, 0, 0), fit=False):
    # GeoItemRenderer adds (0.5, 0.51, 0.5) after the item model's (-0.5, -0.5, -0.5).
    rotated = [rotate([p[0], p[1]+0.16, p[2]], rotation, (2, 1, 0)) for p in points]
    low = [min(p[i] for p in rotated) for i in range(3)]
    high = [max(p[i] for p in rotated) for i in range(3)]
    if fit:
        scale = min(scale, 13/max(high[i]-low[i] for i in (0, 1)))
    return {'rotation': rotation, 'translation': [round(translation[i]-scale*(low[i]+high[i])/2, 6) for i in range(3)],
            'scale': [round(scale, 6)]*3}


for variant, zh, en, source in VARIANTS:
    model = json.loads((RAW / f'{source}.bbmodel').read_text(encoding='utf-8-sig'))
    assert model['meta']['model_format'] == 'free'
    assert len(model['textures']) == 1
    tex = model['textures'][0]
    png = (RAW / tex['name']).read_bytes()
    # The pack's standalone atlases differ slightly from its embedded copies.
    # Use the supplied raw PNGs consistently, retaining their bytes and UV resolution.
    width, height = struct.unpack('>II', png[16:24])
    assert (width, height) == (tex['uv_width'], tex['uv_height'])
    assert not any(a.get('keyframes') for anim in model.get('animations', []) for a in anim.get('animators', {}).values())
    elements = {e['uuid']: e for e in model['elements']}
    groups = {g['uuid']: g for g in model['groups']}
    assert len(elements) == len(model['elements']) and len(groups) == len(model['groups'])
    bones, visited, segment_points = [], set(), {}

    def walk(node, parent=None):
        group = groups[node['uuid']]
        assert not any(group.get('rotation', [])) and group.get('visibility', True)
        name = group['name']
        bone = {'name': name, 'pivot': [-group['origin'][0], *group['origin'][1:]]}
        if name == 'head':
            # Preserve the source's head pivot (28), but attach it to the player's neck pivot (24).
            bones.append({'name': 'head_attachment', 'parent': parent, 'pivot': [0, 24, 0]})
            parent = 'head_attachment'
        if parent:
            bone['parent'] = parent
        bones.append(bone)
        for child in node['children']:
            if isinstance(child, dict):
                walk(child, name)
                continue
            assert child not in visited
            visited.add(child)
            e = elements[child]
            assert e['type'] == 'cube' and e.get('export', True) and e.get('visibility', True)
            size = [b-a for a, b in zip(e['from'], e['to'])]
            assert min(size) >= 0 and not e.get('rescale', False)
            cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]], 'size': size, 'uv': {}}
            if any(e.get('rotation', [])):
                cube['pivot'] = [-e['origin'][0], *e['origin'][1:]]
                cube['rotation'] = [-e['rotation'][0], -e['rotation'][1], e['rotation'][2]]
            if e.get('inflate'):
                cube['inflate'] = e['inflate']
            for side, face in e['faces'].items():
                if face.get('texture') is None:
                    continue
                assert face['texture'] == 0 and not face.get('rotation', 0)
                u, v, U, V = face['uv']
                assert all(0 <= x <= width for x in (u, U)) and all(0 <= y <= height for y in (v, V))
                cube['uv'][side] = {'uv': [U, V] if side in ('up', 'down') else [u, v],
                                    'uv_size': [u-U, v-V] if side in ('up', 'down') else [U-u, V-v]}
            bone.setdefault('cubes', []).append(cube)
            segment_points.setdefault(name, []).extend(corners(e))

    for node in model['outliner']:
        walk(node)
    assert visited == elements.keys()
    assert set(segment_points) == {p for part in PARTS.values() for p in part[2]}
    pending[ASSETS + f'geckolib/models/armor/{variant}.geo.json'] = {
        'format_version': '1.12.0', 'minecraft:geometry': [{'description': {
            'identifier': 'geometry.' + variant, 'texture_width': width, 'texture_height': height,
            'visible_bounds_width': 4, 'visible_bounds_height': 4, 'visible_bounds_offset': [0, 1.5, 0]}, 'bones': bones}]}
    textures.append((ASSETS + f'textures/armor/{variant}.png', png))
    for part, (zh_part, en_part, segments, tag) in PARTS.items():
        item = f'{variant}_{part}'
        points = [point for segment in segments for point in segment_points[segment]]
        gui = item_transform(points, [25, 225, 0], 1.1, fit=True)
        display = {
            'gui': gui,
            'ground': item_transform(points, [0, 0, 0], 0.25, (0, 3, 0)),
            'fixed': item_transform(points, [0, 180, 0], 0.5),
            'thirdperson_righthand': item_transform(points, [75, 45, 0], 0.375, (0, 2.5, 0)),
            'thirdperson_lefthand': item_transform(points, [75, 225, 0], 0.375, (0, 2.5, 0)),
            'firstperson_righthand': item_transform(points, [0, 45, 0], 0.4),
            'firstperson_lefthand': item_transform(points, [0, 225, 0], 0.4),
        }
        pending[ASSETS + f'models/item/{item}.json'] = {'gui_light': 'front',
            'textures': {'particle': f'theaurorian2:armor/{variant}'}, 'display': display}
        pending[ASSETS + f'items/{item}.json'] = {'model': {'type': 'minecraft:special',
            'base': 'theaurorian2:item/' + item, 'model': {'type': 'geckolib:geckolib'}}}
        languages['zh_cn'][f'item.theaurorian2.{item}'] = zh + zh_part
        languages['en_us'][f'item.theaurorian2.{item}'] = en + ' ' + en_part
        tags.setdefault(tag, []).append('theaurorian2:' + item)
    print(f'{variant}: {len(elements)} cubes, {width}x{height}, original UV/rotation/inflation retained')

for tag, ids in tags.items():
    path = DATA + f'minecraft/tags/item/{tag}.json'
    contents = json.loads((ROOT / path).read_text(encoding='utf-8')) if (ROOT / path).exists() else {'replace': False, 'values': []}
    contents['values'] = list(dict.fromkeys(contents['values'] + ids))
    pending[path] = contents
for lang, entries in languages.items():
    pending[ASSETS + f'lang/{lang}.json'] = entries
atlas_path = 'src/main/resources/assets/minecraft/atlases/items.json'
atlas = json.loads((ROOT / atlas_path).read_text(encoding='utf-8')) if (ROOT / atlas_path).exists() else {'sources': []}
for variant, *_ in VARIANTS:
    source = {'type': 'minecraft:single', 'resource': f'theaurorian2:armor/{variant}'}
    if source not in atlas['sources']:
        atlas['sources'].append(source)
pending[atlas_path] = atlas
for path, png in textures:
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(png)
(ROOT / 'build/assassin-armor-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
