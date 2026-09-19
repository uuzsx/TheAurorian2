import json,base64
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
model=json.loads((ROOT/'exports/astrology_table_refined/astrology_table_refined_v8.bbmodel').read_text(encoding='utf-8-sig'))
elements = {e['uuid']: e for e in model['elements']}
groups = {g['uuid']: g for g in model['groups']}
names = {uid: g['name'] for uid, g in groups.items()}
bones = []
def pivot(v): return [-v[0], v[1], v[2]]
def rotation(v): return [-v[0], -v[1], v[2]]
def walk(nodes, parent=None):
    for node in nodes:
        g = groups[node['uuid']]
        b = {'name': g['name'], 'pivot': pivot(g['origin'])}
        if parent: b['parent'] = parent
        if any(g.get('rotation', [0, 0, 0])): b['rotation'] = rotation(g['rotation'])
        cubes, children = [], []
        for child in node['children']:
            if isinstance(child, dict):
                children.append(child)
                continue
            e = elements[child]
            assert e.get('type', 'cube') == 'cube'
            cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]],
                    'size': [e['to'][i]-e['from'][i] for i in range(3)], 'uv': {}}
            if any(e.get('rotation', [0, 0, 0])):
                cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
            for face, f in e['faces'].items():
                if f.get('texture') is None: continue
                assert f['texture'] == 0 and not f.get('rotation')
                u,v,U,V = f['uv']
                cube['uv'][face] = {'uv': [U,V] if face in ('up','down') else [u,v],
                    'uv_size': [u-U,v-V] if face in ('up','down') else [U-u,V-v]}
            cubes.append(cube)
        if cubes: b['cubes'] = cubes
        bones.append(b)
        walk(children, b['name'])
walk(model['outliner'])
assert sum(len(b.get('cubes', [])) for b in bones) == len(elements)
animations = {}
for anim in model['animations']:
    exported = {'loop': anim['loop']=='loop', 'animation_length': anim['length'], 'bones': {}}
    for uid, animator in anim['animators'].items():
        if not animator.get('keyframes'): continue
        assert not animator.get('rotation_global') and not animator.get('quaternion_interpolation')
        channels = {}
        previous = {}
        for k in sorted(animator['keyframes'], key=lambda k:k['time']):
            channel = k['channel']
            value = [float(k['data_points'][0][axis]) for axis in 'xyz']
            if k['interpolation']=='step':
                # Bedrock discontinuous keyframes hold the previous value until this instant.
                encoded = {'pre': previous.get(channel, value), 'post': value}
            elif k['interpolation']=='catmullrom':
                encoded = {'post': value, 'lerp_mode': 'catmullrom'}
            else: encoded = value
            channels.setdefault(channel, {})[str(k['time'])] = encoded
            previous[channel] = value
        exported['bones'][names[uid]] = channels
    animations[anim['name']] = exported

base='src/main/resources/assets/theaurorian2/'
pending={
base+'geckolib/models/block/astrology_table.geo.json':{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.astrology_table','texture_width':48,'texture_height':48,'visible_bounds_width':3,'visible_bounds_height':2.5,'visible_bounds_offset':[0,.75,0]},'bones':bones}]},
base+'geckolib/animations/block/astrology_table.animation.json':{'format_version':'1.8.0','animations':animations}}
assert set(animations)=={'misc.idle'} and animations['misc.idle']['animation_length']==12
texture=base64.b64decode(model['textures'][0]['source'].split(',')[1])
assert texture==(ROOT/base/'textures/block/astrology_table.png').read_bytes(), 'Texture changed: review before replacing'
(ROOT/'build/astrology-resources.json').write_text(json.dumps(pending),encoding='utf-8')
print(f'Validated {len(elements)} cubes, {len(bones)} bones, original texture; exported approved animation.')
