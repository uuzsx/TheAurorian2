"""Import the owner's animated BB 4.9 bird; finalize JSON with import_clock_raptor.ps1.

Keeps geometry/UVs/texture and bakes authored Bezier curves, excluding the demo.
The preview's vertical root translation is replaced by real entity flight.
"""
import base64
import json
from pathlib import Path
import sys
from export_blue_tail_wolf import evaluate, tracks, number

ROOT = Path(__file__).resolve().parents[1]


def export(source):
    model = json.loads(Path(source).read_text(encoding='utf-8-sig'))
    assert model['meta']['format_version'] == '4.9', 'Animation axis convention requires BB 4.9'
    assert model['resolution'] == {'width': 64, 'height': 64}
    elements = {e['uuid']: e for e in model['elements']}
    seen, bones = set(), []
    pivot = lambda v: [-v[0], v[1], v[2]]
    rotation = lambda v: [-v[0], -v[1], v[2]]

    def walk(nodes, parent=None):
        for node in nodes:
            bone = {'name': node['name'], 'pivot': pivot(node['origin'])}
            if parent:
                bone['parent'] = parent
            if any(node.get('rotation', [0, 0, 0])):
                bone['rotation'] = rotation(node['rotation'])
            children, cubes = [], []
            for child in node['children']:
                if isinstance(child, dict):
                    children.append(child)
                    continue
                assert child not in seen
                seen.add(child)
                e = elements[child]
                assert e['type'] == 'cube' and e.get('export', True)
                cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]],
                        'size': [b-a for a, b in zip(e['from'], e['to'])], 'uv': {}}
                if any(e.get('rotation', [0, 0, 0])):
                    cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
                if 'inflate' in e:
                    cube['inflate'] = e['inflate']
                for face, uv in e['faces'].items():
                    if uv.get('texture') is None:
                        continue
                    assert uv['texture'] == 0 and not uv.get('rotation')
                    u, v, U, V = uv['uv']
                    cube['uv'][face] = {'uv': [U, V] if face in ('up', 'down') else [u, v],
                                       'uv_size': [u-U, v-V] if face in ('up', 'down') else [U-u, V-v]}
                cubes.append(cube)
            if cubes:
                bone['cubes'] = cubes
            bones.append(bone)
            walk(children, bone['name'])

    walk(model['outliner'])
    assert seen == elements.keys()
    names = {b['name'] for b in bones}
    assert len(names) == len(bones)
    smooth = lambda x: (lambda u: u*u*u*(u*(u*6-15)+10))(max(0, min(1, x)))
    animations, errors = {}, {'rotation': 0, 'position': 0}
    for clip in model['animations']:
        name, length = clip['name'], clip['length']
        if name.startswith('preview.'):
            continue
        animation = {'loop': True if clip['loop'] == 'loop' else 'hold_on_last_frame',
                     'animation_length': length, 'bones': {}}
        for (bone, channel), keys in tracks(clip).items():
            assert bone in names and channel in ('rotation', 'position')

            def sample(t):
                value = evaluate(keys, t)
                # BB 4.9 stores rotation X/Y in the old Bedrock convention already.
                if channel == 'position':
                    value[0] = -value[0]
                    if bone == 'body':
                        lift = (smooth((t-.22)/.98) if name == 'takeoff' else
                                1-smooth(t/1.1) if name == 'land' else
                                1 if name in ('hover', 'fly', 'hover_to_fly', 'fly_to_hover') else 0)
                        value[1] -= 7.7 * lift
                return value

            tolerance = .035 if channel == 'rotation' else .001
            times = []

            def flatten(a, b, depth=0):
                va, vb = sample(a), sample(b)
                error = max(abs((1-u)*x+u*y-z) for u in (.25, .5, .75)
                            for x, y, z in zip(va, vb, sample(a+(b-a)*u)))
                if error > tolerance and depth < 12:
                    flatten(a, (a+b)/2, depth+1)
                    flatten((a+b)/2, b, depth+1)
                else:
                    assert error <= tolerance
                    errors[channel] = max(errors[channel], error)
                    times.append(a)
            knots = sorted({0, length, *(k['time'] for k in keys)})
            for a, b in zip(knots, knots[1:]):
                flatten(a, b)
            times.append(length)
            values = [[float(number(v)) for v in sample(t)] for t in times]
            animation['bones'].setdefault(bone, {})[channel] = (
                values[0] if all(v == values[0] for v in values) else
                {number(t): value for t, value in zip(times, values)})
        animations[name] = animation
    assert len(animations) == 8
    assets = 'src/main/resources/assets/theaurorian2/'
    data = 'src/main/resources/data/theaurorian2/'
    entity = 'clock_raptor_of_fallen'
    pending = {
        assets+f'geckolib/models/entity/{entity}.geo.json': {'format_version': '1.12.0', 'minecraft:geometry': [{
            'description': {'identifier': 'geometry.'+entity, 'texture_width': 64, 'texture_height': 64,
                            'visible_bounds_width': 5, 'visible_bounds_height': 4, 'visible_bounds_offset': [0, 1, 0]},
            'bones': bones}]},
        assets+f'geckolib/animations/entity/{entity}.animation.json': {'format_version': '1.8.0', 'animations': animations},
        assets+f'items/{entity}_spawn_egg.json': {'model': {'type': 'minecraft:model', 'model': 'minecraft:item/parrot_spawn_egg'}},
        data+'tags/worldgen/biome/has_clock_raptor.json': {'replace': False, 'values': ['theaurorian2:aurorian_desert']},
        data+'tags/block/clock_raptor_spawnable_on.json': {'replace': False, 'values': [
            'theaurorian2:bright_moon_sand', 'theaurorian2:bright_moon_sandstone']},
        data+'neoforge/biome_modifier/clock_raptor_spawns.json': {'type': 'neoforge:add_spawns',
            'biomes': '#theaurorian2:has_clock_raptor',
            'spawners': {'type': 'theaurorian2:'+entity, 'weight': 3, 'minCount': 1, 'maxCount': 2}},
    }
    loot = json.loads((ROOT/data/'loot_table/entities/azure_warbler.json').read_text(encoding='utf-8-sig'))
    loot['random_sequence'] = 'theaurorian2:entities/'+entity
    loot['pools'][0]['entries'][0]['functions'].insert(0, {'function': 'minecraft:set_count',
        'count': {'type': 'minecraft:uniform', 'min': 1, 'max': 2}})
    loot['pools'][1]['entries'][0]['functions'][0]['count'].update(min=1, max=3)
    pending[data+f'loot_table/entities/{entity}.json'] = loot
    for locale, label in [('zh_cn', '折翼之终刻鸟'), ('en_us', 'Clock Raptor of Fallen')]:
        path = assets+f'lang/{locale}.json'
        lang = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
        lang['entity.theaurorian2.'+entity] = label
        lang['item.theaurorian2.'+entity+'_spawn_egg'] = label+('刷怪蛋' if locale == 'zh_cn' else ' Spawn Egg')
        pending[path] = lang
    texture = base64.b64decode(model['textures'][0]['source'].split(',')[1])
    (ROOT/assets/f'textures/entity/{entity}.png').write_bytes(texture)
    (ROOT/'build/clock-raptor-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
    print(f'Validated {len(elements)} cubes, {len(bones)} bones, {len(animations)} clips; bake error {errors}')


if __name__ == '__main__':
    export(sys.argv[1])
