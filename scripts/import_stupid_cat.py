"""Export the approved doll without changing its pixels, poses or animation timing.

Run via import_stupid_cat.ps1. The editable source stays local under exports/skin_doll/final.
"""
import argparse
import base64
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--source', default='exports/skin_doll/final/stupid_cat.bbmodel')
parser.add_argument('--id', default='stupid_cat')
parser.add_argument('--zh', default='笨蛋锚猫')
parser.add_argument('--en', default='Stupid Cat')
args = parser.parse_args()
SOURCE = ROOT / args.source
model = json.loads(SOURCE.read_text(encoding='utf-8-sig'))
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
assert animations['wave_hello']['animation_length'] == 3.2
base = 'src/main/resources/assets/theaurorian2/'
pending = {
base+'geckolib/models/block/stupid_cat.geo.json': {'format_version':'1.12.0','minecraft:geometry':[{
    'description':{'identifier':'geometry.stupid_cat','texture_width':model['resolution']['width'],'texture_height':model['resolution']['height'],
        'visible_bounds_width':2,'visible_bounds_height':2,'visible_bounds_offset':[0,.5,0]},'bones':bones}]},
base+'geckolib/animations/block/stupid_cat.animation.json':{'format_version':'1.8.0','animations':animations},
base+'models/block/stupid_cat.json':{'textures':{'particle':'theaurorian2:block/stupid_cat'},'elements':[]},
base+'blockstates/stupid_cat.json':{'multipart':[{'apply':{'model':'theaurorian2:block/stupid_cat'}}]},
base+'items/stupid_cat.json':{'model':{'type':'minecraft:special','base':'theaurorian2:item/stupid_cat','model':{'type':'geckolib:geckolib'}}},
base+'models/item/stupid_cat.json':{'gui_light':'front','textures':{'particle':'theaurorian2:block/stupid_cat'},'display':{
    'gui':{'rotation':[15,150,0],'translation':[0,-7,0],'scale':[.85,.85,.85]},
    'head':{'rotation':[0,0,0],'translation':[0,6.4,0],'scale':[1,1,1]},
    'ground':{'translation':[0,2,0],'scale':[.5,.5,.5]},
    'fixed':{'scale':[.75,.75,.75]},
    'thirdperson_righthand':{'rotation':[70,45,0],'scale':[.5,.5,.5]},
    'firstperson_righthand':{'rotation':[0,135,0],'scale':[.65,.65,.65]}}},
'src/main/resources/data/theaurorian2/loot_table/blocks/stupid_cat.json':{'type':'minecraft:block','pools':[{
    'rolls':1,'entries':[{'type':'minecraft:item','name':'theaurorian2:stupid_cat'}],
    'conditions':[{'condition':'minecraft:survives_explosion'}]}]}}
# Resource identifiers vary; geometry, UVs and animations remain untouched.
pending = {path.replace('stupid_cat', args.id): json.loads(json.dumps(value).replace('stupid_cat', args.id)) for path, value in pending.items()}
for lang, label in [('zh_cn',args.zh),('en_us',args.en)]:
    path = base+f'lang/{lang}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    data['block.theaurorian2.' + args.id] = label
    pending[path] = data
texture = base64.b64decode(model['textures'][0]['source'].split(',')[1])
(ROOT/base/f'textures/block/{args.id}.png').write_bytes(texture)
(ROOT/'build/stupid-cat-resources.json').write_text(json.dumps(pending, ensure_ascii=False),encoding='utf8')
print(f'Validated and exported {len(elements)} cubes, {len(bones)} bones, two animations; texture bytes unchanged.')
