"""Import bird.zip, preserving the supplied geometry, UVs and embedded flight.

Run via import_azure_warbler.ps1 to use the validated project JSON writer.
The standalone animation JSON is empty; the actual flight is in the bbmodel.
"""
import argparse
import copy
import json
import struct
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
parser.add_argument('--raw', type=Path, default=Path('C:/Users/84446/Downloads/raw_bird.png'))
parser.add_argument('--cooked', type=Path, default=Path('C:/Users/84446/Downloads/cooked_bird.png'))
args = parser.parse_args()
with zipfile.ZipFile(args.source) as archive:
    model = json.loads(archive.read('bird.bbmodel'))
    texture = archive.read('bird.png')
    json.loads(archive.read('bird.animation.json'))
assert model['meta']['model_format'] == 'geckolib_model'
assert struct.unpack('>II', texture[16:24]) == (32, 32)
assert model['resolution'] == {'width': 32, 'height': 32}
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
            assert all(value >= 0 for value in size)  # Wings/tail/feet intentionally contain planes.
            cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]], 'size': size, 'uv': {}}
            if any(e.get('rotation', [0, 0, 0])):
                cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
            for face, f in e['faces'].items():
                if f.get('texture') is None:
                    continue
                assert f['texture'] == 0 and not f.get('rotation')
                u, v, U, V = f['uv']
                assert all(0 <= value <= 32 for value in (u, v, U, V))
                cube['uv'][face] = {'uv': [U, V] if face in ('up', 'down') else [u, v],
                                    'uv_size': [u-U, v-V] if face in ('up', 'down') else [U-u, V-v]}
            cubes.append(cube)
        if cubes:
            bone['cubes'] = cubes
        bones.append(bone)
        walk(children, bone['name'])

walk(model['outliner'])
assert seen == elements.keys()
assert len({b['name'] for b in bones}) == len(groups)
flight = next(a for a in model['animations'] if a['name'] == 'move.fly')
tracks = {}
for uid, animator in flight['animators'].items():
    channels = {}
    for frame in animator.get('keyframes', []):
        assert frame['time'] == 0 and frame['interpolation'] == 'linear'
        assert frame['channel'] in ('rotation', 'position') and len(frame['data_points']) == 1
        values = []
        for index, axis in enumerate('xyz'):
            value = frame['data_points'][0][axis]
            try:
                value = float(value)
            except ValueError:
                assert 'query.anim_time' in value and ('math.sin' in value or 'math.cos' in value)
            # Blockbench's GeckoLib exporter checkAndPatchKeyframeValues:
            # rotation X/Y and position X switch handedness, including Molang.
            if index == 0 or (index == 1 and frame['channel'] == 'rotation'):
                value = -value if isinstance(value, float) else f'-({value})'
            values.append(value)
        assert frame['channel'] not in channels
        channels[frame['channel']] = values
    if channels:
        tracks[groups[uid]['name']] = channels

idle = {'body': {'rotation': ['math.sin(query.anim_time*90)*1.2', 0, 0]},
        'tail2': {'rotation': ['math.sin(query.anim_time*90)*2', 0, 0]}}
animations = {'move.fly': {'loop': True, 'animation_length': 2, 'bones': tracks},
              'idle': {'loop': True, 'animation_length': 4, 'bones': idle},
              'sit': {'loop': True, 'animation_length': 4, 'bones': dict(idle, root={'position': [0, -0.25, 0]})},
              'walk': {'loop': True, 'animation_length': 1, 'bones': {
                  'root': {'position': [0, 'math.abs(math.sin(query.anim_time*360))*0.18', 0]},
                  'leg_left2': {'rotation': ['math.sin(query.anim_time*360)*22', 0, 0]},
                  'leg_right2': {'rotation': ['-math.sin(query.anim_time*360)*22', 0, 0]},
                  'tail2': {'rotation': [0, 'math.sin(query.anim_time*360)*3', 0]}}},
              'party': {'loop': True, 'animation_length': 2, 'bones': {
                  'root': {'rotation': [0, 0, 'math.sin(query.anim_time*360)*8'],
                           'position': [0, 'math.abs(math.sin(query.anim_time*360))*0.4', 0]},
                  'head2': {'rotation': [0, 'math.sin(query.anim_time*180)*12', 0]},
                  'tail2': {'rotation': [0, 'math.sin(query.anim_time*180)*10', 0]}}}}

assets = 'src/main/resources/assets/theaurorian2/'
data = 'src/main/resources/data/'
pending = {
    assets+'geckolib/models/entity/azure_warbler.geo.json': {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.azure_warbler', 'texture_width': 32, 'texture_height': 32,
                        'visible_bounds_width': 3, 'visible_bounds_height': 3, 'visible_bounds_offset': [0, 0.5, 0]},
        'bones': bones}]},
    assets+'geckolib/animations/entity/azure_warbler.animation.json': {'format_version': '1.8.0', 'animations': animations},
    assets+'items/azure_warbler_spawn_egg.json': {'model': {'type': 'minecraft:model', 'model': 'minecraft:item/parrot_spawn_egg'}},
    data+'theaurorian2/tags/worldgen/biome/has_azure_warbler.json': {'replace': False, 'values': [
        'theaurorian2:'+name+suffix for name in ('silent_wood_forest', 'curtain_tree_forest') for suffix in ('', '_hills')]},
    data+'theaurorian2/tags/block/azure_warbler_spawnable_on.json': {'replace': False, 'values': [
        '#minecraft:parrots_spawnable_on', '#theaurorian2:is_aurorian_grass_block']},
    data+'theaurorian2/neoforge/biome_modifier/azure_warbler_spawns.json': {
        'type': 'neoforge:add_spawns', 'biomes': '#theaurorian2:has_azure_warbler',
        'spawners': {'type': 'theaurorian2:azure_warbler', 'weight': 4, 'minCount': 1, 'maxCount': 3}},
}

for cooked, source in [(False, args.raw), (True, args.cooked)]:
    name = 'cooked_bird' if cooked else 'raw_bird'
    png = source.read_bytes()
    assert struct.unpack('>II', png[16:24]) == (16, 16)
    (ROOT/assets/f'textures/item/{name}.png').write_bytes(png)
    pending[assets+f'items/{name}.json'] = {'model': {'type': 'minecraft:model', 'model': f'theaurorian2:item/{name}'}}
    pending[assets+f'models/item/{name}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'theaurorian2:item/{name}'}}

for kind, ticks in [('smelting', 200), ('smoking', 100), ('campfire_cooking', 600)]:
    recipe_id = f'theaurorian2:cooked_bird_from_{kind}'
    pending[data+f'theaurorian2/recipe/cooked_bird_from_{kind}.json'] = {
        'type': 'minecraft:'+kind, 'category': 'food', 'cookingtime': ticks, 'experience': 0.35,
        'ingredient': 'theaurorian2:raw_bird', 'result': {'id': 'theaurorian2:cooked_bird'}}
    pending[data+f'theaurorian2/advancement/recipes/food/cooked_bird_from_{kind}.json'] = {
        'parent': 'minecraft:recipes/root', 'criteria': {
            'has_raw_bird': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'theaurorian2:raw_bird'}]}},
            'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': recipe_id}}},
        'requirements': [['has_raw_bird', 'has_the_recipe']], 'rewards': {'recipes': [recipe_id]}}

loot = json.loads((ROOT/data/'theaurorian2/loot_table/entities/frostfin.json').read_text(encoding='utf-8-sig'))
loot['random_sequence'] = 'theaurorian2:entities/azure_warbler'
loot['pools'][0]['entries'][0]['name'] = 'theaurorian2:raw_bird'
looting = copy.deepcopy(loot['pools'][0]['entries'][0]['functions'][-1])
loot['pools'].append({'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:feather', 'functions': [
    {'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}}, looting]}]})
pending[data+'theaurorian2/loot_table/entities/azure_warbler.json'] = loot
for language, names in [('zh_cn', ('青翎雀', '青翎雀刷怪蛋', '生鸟肉', '熟鸟肉')),
                        ('en_us', ('Azure Warbler', 'Azure Warbler Spawn Egg', 'Raw Bird Meat', 'Cooked Bird Meat'))]:
    path = assets+f'lang/{language}.json'
    lang = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    lang.update(dict(zip(('entity.theaurorian2.azure_warbler', 'item.theaurorian2.azure_warbler_spawn_egg',
                          'item.theaurorian2.raw_bird', 'item.theaurorian2.cooked_bird'), names)))
    pending[path] = lang

(ROOT/assets/'textures/entity/azure_warbler.png').write_bytes(texture)
(ROOT/'build/azure-warbler-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
print(f'Validated {len(elements)} cubes, {len(bones)} bones, embedded flight + 4 land/perch animations; original PNGs preserved.')
