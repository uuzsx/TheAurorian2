"""Import Crates Pack v2 levels 2, 3 and 4; split unlocking from viewer-driven lids.

Original PNG bytes, geometry, UVs and bone hierarchy retained.
Run import_moon_chest.ps1 to write validated, compact JSON resources.
"""
import base64
import copy
import io
import json
import sys
import math
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path('E:/BaiduNetdiskDownload/1407箱子包第二卷/Crates Pack v2/Blockbench files')
A = 'src/main/resources/assets/theaurorian2/'
D = 'src/main/resources/data/theaurorian2/'
OUT = ROOT/'build/moon-chest'
OUT.mkdir(parents=True, exist_ok=True)
pending = {}

def emit(path, value):
    pending[path] = value

VARIANTS = [(2, 'treasure_chest_2', .5), (3, 'moon_chest', .42), (4, 'treasure_chest_4', .38)]
for number, chest_stem, chest_scale in VARIANTS:
 for kind in ['crate', 'key']:
    source_name = f'{kind}_lvl{number}'
    stem = chest_stem if kind == 'crate' else chest_stem+'_key'
    model = json.loads((SOURCE/f'{source_name}.bbmodel').read_text(encoding='utf-8-sig'))
    assert len(model['textures']) == 1
    original_png = base64.b64decode(model['textures'][0]['source'].split(',')[1])
    texture = Image.open(io.BytesIO(original_png)).convert('RGBA')
    (ROOT/A/f'textures/block/{stem}.png').write_bytes(original_png)
    scale = chest_scale if kind == 'crate' else 1
    elems = {e['uuid']: e for e in model['elements']}
    bones, names = [], {}
    def pivot(v): return [-v[0]*scale, v[1]*scale, v[2]*scale]
    def rotation(v): return [-v[0], -v[1], v[2]]
    def walk(nodes, parent=None):
        for node in nodes:
            assert isinstance(node, dict)
            names[node['uuid']] = node['name']
            bone = {'name': node['name'], 'pivot': pivot(node['origin'])}
            if any(node.get('rotation', [0, 0, 0])): bone['rotation'] = rotation(node['rotation'])
            if parent: bone['parent'] = parent
            cubes, children = [], []
            for child in node['children']:
                if isinstance(child, dict):
                    children.append(child)
                    continue
                e = elems[child]
                assert e.get('type', 'cube') == 'cube' and not e.get('rescale')
                cube = {'origin': [-e['to'][0]*scale, e['from'][1]*scale, e['from'][2]*scale],
                        'size': [(e['to'][i]-e['from'][i])*scale for i in range(3)], 'uv': {}}
                if any(e.get('rotation', [0, 0, 0])):
                    cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
                for face, f in e['faces'].items():
                    if f.get('texture') is None: continue
                    assert f['texture'] == 0 and not f.get('rotation')
                    u, v, U, V = f['uv']
                    cube['uv'][face] = {'uv': [U, V] if face in ['up', 'down'] else [u, v],
                                       'uv_size': [u-U, v-V] if face in ['up', 'down'] else [U-u, V-v]}
                cubes.append(cube)
            if cubes: bone['cubes'] = cubes
            bones.append(bone)
            walk(children, node['name'])
    grouped = [node for node in model['outliner'] if isinstance(node, dict)]
    loose = [node for node in model['outliner'] if isinstance(node, str)]
    if loose:
        grouped.append({'name': 'ungrouped', 'uuid': 'ungrouped', 'origin': [0, 0, 0], 'children': loose})
    walk(grouped)
    assert sum(len(b.get('cubes', [])) for b in bones) == len(elems)
    if kind == 'crate':
        emit(A+f'geckolib/models/block/{stem}.geo.json', {'format_version': '1.12.0', 'minecraft:geometry': [{
            'description': {'identifier': 'geometry.'+stem, 'texture_width': texture.width, 'texture_height': texture.height,
                            'visible_bounds_width': 3, 'visible_bounds_height': 3, 'visible_bounds_offset': [0, .5, 0]}, 'bones': bones}]})
        animations = {}
        for anim in model['animations']:
            if anim['name'] != 'open': continue
            exported = {'loop': 'hold_on_last_frame', 'animation_length': anim['length']/2, 'bones': {}}
            for uid, animator in anim['animators'].items():
                if uid == 'effects': continue
                channels = {}
                for k in sorted(animator['keyframes'], key=lambda k: k['time']):
                    assert k['channel'] in ['rotation', 'position', 'scale']
                    points = [[float(p[axis])*(scale if k['channel']=='position' else 1) for axis in 'xyz'] for p in k['data_points']]
                    value = {'post': points[-1], 'lerp_mode': 'catmullrom'} if k['interpolation']=='catmullrom' else points[-1]
                    if len(points)>1: value = {'pre': points[0], 'post': points[-1], 'lerp_mode': k['interpolation']}
                    channels.setdefault(k['channel'], {})[str(k['time']/2)] = value
                exported['bones'][names[uid]] = channels
            animations['open'] = exported
        assert animations
        animations['closed'] = {'loop': True, 'animation_length': 1, 'bones': {
            b['name']: {'rotation': [0, 0, 0], 'position': [0, 0, 0]} for b in bones}}
        # Keep the authored lock/body motion separate from opening the lid.
        final_bones = copy.deepcopy(animations['closed']['bones'])
        for bone_name, channels in animations['open']['bones'].items():
            for channel, frames in channels.items():
                value = frames[max(frames, key=float)]
                final_bones[bone_name][channel] = value['post'] if isinstance(value, dict) else value
        lid_open = final_bones.pop('lid')
        lid_closed = animations['closed']['bones'].pop('lid')
        unlock = animations.pop('open')
        unlock['bones'].pop('lid')
        animations['unlock'] = unlock
        animations['unlocked'] = {'loop': True, 'animation_length': 1, 'bones': final_bones}
        animations['lid_open'] = {'loop': True, 'animation_length': 1, 'bones': {'lid': lid_open}}
        animations['lid_closed'] = {'loop': True, 'animation_length': 1, 'bones': {'lid': lid_closed}}
        expected_ticks = {2: 12, 3: 10, 4: 13}[number]
        assert math.ceil(animations['unlock']['animation_length']*20) == expected_ticks
        assert all('lid' not in animations[name]['bones'] for name in ['unlock', 'unlocked', 'closed'])
        emit(A+f'geckolib/animations/block/{stem}.animation.json', {'format_version': '1.8.0', 'animations': animations})
        emit(A+f'models/block/{stem}.json', {'textures': {'particle': f'theaurorian2:block/{stem}'}, 'elements': []})
        emit(A+f'blockstates/{stem}.json', {'multipart': [{'apply': {'model': f'theaurorian2:block/{stem}'}}]})
        emit(D+f'loot_table/blocks/{stem}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1,
            'entries': [{'type': 'minecraft:item', 'name': 'theaurorian2:'+stem,
                         'functions': [{'function': 'minecraft:copy_state', 'block': 'theaurorian2:'+stem, 'properties': ['unlocked']}]}],
            'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    item = []
    for e in elems.values():
        el = {k: [v*scale+(8 if i!=1 and kind=='crate' else 0) for i, v in enumerate(e[k])] for k in ['from', 'to']}
        el['faces'] = {face: {'texture': '#0', 'uv': [v/texture.width*16 for v in f['uv']]} for face, f in e['faces'].items() if f.get('texture') is not None}
        axes = [i for i, v in enumerate(e.get('rotation', [0, 0, 0])) if v]
        if axes:
            assert len(axes) == 1
            el['rotation'] = {'origin': [v*scale+(8 if i!=1 and kind=='crate' else 0) for i, v in enumerate(e['origin'])],
                              'axis': 'xyz'[axes[0]], 'angle': e['rotation'][axes[0]], 'rescale': False}
        item.append(el)
    display = {'gui': {'rotation': [20, 150, 0], 'translation': [0, 2, 0], 'scale': [1, 1, 1]},
               'ground': {'translation': [0, 3, 0], 'scale': [.5, .5, .5]},
               'fixed': {'scale': [.75, .75, .75]},
               'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
               'firstperson_righthand': {'rotation': [0, 135, 0], 'scale': [.65, .65, .65]}}
    if stem.endswith('key'): display.update(model.get('display', {}))
    if kind == 'crate':
        display['gui']['translation'] = [0, -4, 0]
        # Geo items retain level 4's rotated parent bone, which vanilla element
        # JSON cannot express faithfully.
        emit(A+f'models/item/{stem}.json', {'textures': {'particle': f'theaurorian2:block/{stem}'}, 'display': display})
        emit(A+f'items/{stem}.json', {'model': {'type': 'minecraft:special', 'base': f'theaurorian2:item/{stem}',
                                             'model': {'type': 'geckolib:geckolib'}}})
    else:
        emit(A+f'models/item/{stem}.json', {'textures': {'0': f'theaurorian2:block/{stem}', 'particle': f'theaurorian2:block/{stem}'}, 'elements': item, 'display': display})
        emit(A+f'items/{stem}.json', {'model': {'type': 'minecraft:model', 'model': f'theaurorian2:item/{stem}'}})
    editable = copy.deepcopy(model)
    buffer = io.BytesIO(); texture.save(buffer, format='PNG')
    editable['textures'][0]['source'] = 'data:image/png;base64,'+base64.b64encode(buffer.getvalue()).decode()
    emit(f'exports/moon_chest_v3/{stem}.bbmodel', editable)

for lang in ['zh_cn', 'en_us']:
    path = A+f'lang/{lang}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    for number, stem, _ in VARIANTS:
        title = ('皎月箱' if number == 3 else f'{number}号宝箱') if lang == 'zh_cn' else ('Moon Chest' if number == 3 else f'Treasure Chest {number}')
        key = ('皎月钥匙' if number == 3 else f'{number}号宝箱钥匙') if lang == 'zh_cn' else ('Moon Key' if number == 3 else f'Treasure Key {number}')
        data['block.theaurorian2.'+stem] = title
        data['item.theaurorian2.'+stem+'_key'] = key
    data['message.theaurorian2.moon_chest_locked'] = '需要对应的钥匙解锁' if lang == 'zh_cn' else 'Requires the matching key'
    data['message.theaurorian2.chest_unlocking'] = '正在解锁……' if lang == 'zh_cn' else 'Unlocking...'
    emit(path, data)
(OUT/'pending.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
print('Validated original levels 2/3/4 assets, 2x unlock motion (12/10/13 ticks), and independent lid poses.')
