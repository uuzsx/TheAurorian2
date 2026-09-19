"""Import the supplied fish.zip without changing geometry, face UVs or texture pixels.

Run through import_frostfin.ps1 so generated JSON uses the project writer.
The embedded swim track is used; the separate animation JSON contains only an empty idle.
"""
import argparse
import json
import struct
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
with zipfile.ZipFile(args.source) as archive:
    model = json.loads(archive.read('fish.bbmodel'))
    texture = archive.read('fish.png')
    json.loads(archive.read('model.animation.json'))

assert model['meta']['model_format'] == 'geckolib_model'
assert struct.unpack('>II', texture[16:24]) == (64, 64)
assert model['resolution'] == {'width': 64, 'height': 64}
elements = {e['uuid']: e for e in model['elements']}
groups = {g['uuid']: g for g in model['groups']}
assert len(elements) == len(model['elements']) and len(groups) == len(model['groups'])
bones, seen = [], set()

def pivot(v):
    return [-v[0], v[1], v[2]]

def rotation(v):
    return [-v[0], -v[1], v[2]]

def walk(nodes, parent=None):
    for node in nodes:
        group = groups[node['uuid']]
        bone = {'name': group['name'], 'pivot': pivot(group['origin'])}
        if parent:
            bone['parent'] = parent
        if any(group.get('rotation', [0, 0, 0])):
            bone['rotation'] = rotation(group['rotation'])
        cubes, children = [], []
        for child in node['children']:
            if isinstance(child, dict):
                children.append(child)
                continue
            assert child not in seen
            seen.add(child)
            e = elements[child]
            assert e['type'] == 'cube' and e.get('export', True)
            size = [e['to'][i] - e['from'][i] for i in range(3)]
            assert all(value >= 0 for value in size)
            cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]], 'size': size, 'uv': {}}
            if any(e.get('rotation', [0, 0, 0])):
                cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
            for face, f in e['faces'].items():
                if f.get('texture') is None:
                    continue
                assert f['texture'] == 0 and not f.get('rotation')
                u, v, U, V = f['uv']
                assert all(0 <= value <= 64 for value in (u, v, U, V))
                cube['uv'][face] = {'uv': [U, V] if face in ('up', 'down') else [u, v],
                                    'uv_size': [u-U, v-V] if face in ('up', 'down') else [U-u, V-v]}
            cubes.append(cube)
        if cubes:
            bone['cubes'] = cubes
        bones.append(bone)
        walk(children, bone['name'])

walk(model['outliner'])
assert seen == elements.keys()
swim = next(a for a in model['animations'] if a['name'] == 'swim')
tracks = {}
for uid, animator in swim['animators'].items():
    channels = {}
    for frame in animator.get('keyframes', []):
        assert frame['time'] == 0 and frame['interpolation'] == 'linear'
        assert frame['channel'] == 'rotation' and len(frame['data_points']) == 1
        values = []
        for axis in 'xyz':
            value = frame['data_points'][0][axis]
            try:
                value = float(value)
            except ValueError:
                assert value in ('math.sin(query.anim_time*180)*7', 'math.cos(query.anim_time*180)*3.5',
                                 'math.cos(query.anim_time*180)*17', 'math.sin(query.anim_time*180)*-15')
            values.append(value)
        channels[frame['channel']] = values
    if channels:
        tracks[groups[uid]['name']] = channels

base = 'src/main/resources/assets/theaurorian2/'
pending = {
    base+'geckolib/models/entity/frostfin.geo.json': {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.frostfin', 'texture_width': 64, 'texture_height': 64,
                        'visible_bounds_width': 5, 'visible_bounds_height': 3, 'visible_bounds_offset': [0, 0, 0.5]},
        'bones': bones}]},
    base+'geckolib/animations/entity/frostfin.animation.json': {'format_version': '1.8.0', 'animations': {
        'swim': {'loop': True, 'animation_length': 2, 'bones': tracks}}},
    base+'items/frostfin_spawn_egg.json': {'model': {'type': 'minecraft:model', 'model': 'minecraft:item/salmon_spawn_egg'}},
    base+'items/frostfin_bucket.json': {'model': {'type': 'minecraft:model', 'model': 'minecraft:item/water_bucket'}},
}
for language, name, bucket, egg in [('zh_cn', '霜鳍鱼', '霜鳍鱼桶', '霜鳍鱼刷怪蛋'),
                                   ('en_us', 'Frostfin', 'Bucket of Frostfin', 'Frostfin Spawn Egg')]:
    path = base+f'lang/{language}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    data.update({'entity.theaurorian2.frostfin': name, 'item.theaurorian2.frostfin_bucket': bucket,
                 'item.theaurorian2.frostfin_spawn_egg': egg})
    pending[path] = data

path = 'src/main/resources/data/theaurorian2/neoforge/biome_modifier/aurorian_fish_spawns.json'
data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
data['spawners'] = [s for s in data['spawners'] if s['type'] != 'theaurorian2:frostfin']
data['spawners'].append({'type': 'theaurorian2:frostfin', 'weight': 6, 'minCount': 2, 'maxCount': 4})
pending[path] = data
for tag in ('aquatic', 'axolotl_hunt_targets', 'can_breathe_under_water', 'cannot_be_pushed_onto_boats', 'not_scary_for_pufferfish'):
    path = f'src/main/resources/data/minecraft/tags/entity_type/{tag}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    assert not data.get('replace', False)
    if 'theaurorian2:frostfin' not in data['values']:
        data['values'].append('theaurorian2:frostfin')
    pending[path] = data

(ROOT/base/'textures/entity/frostfin.png').write_bytes(texture)
(ROOT/'build/frostfin-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
print(f'Validated {len(elements)} cubes, {len(bones)} bones, 4 animated bones, original 64x64 texture.')
