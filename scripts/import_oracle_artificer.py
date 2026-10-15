"""Import the Oracle's selected Artificer VFX rigs, including signed hulls.

The PS1 wrapper writes validated compact JSON. Texture pixels, face rotations,
pivots and animation timing are retained. Identical emissive copies need no
second pass; the two blast textures are packed side by side without resampling.
"""
import base64
import bisect
import io
import json
import sys
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = 'src/main/resources/assets/theaurorian2/'
MODELS = {
    'vfx_tinkering_shield': 'oracle_arcane_shield',
    'vfx_arcane_blast': 'oracle_arcane_blast',
    'vfx_arcane_blast_trail': 'oracle_arcane_trail',
    'vfx_arcane_hit': 'oracle_arcane_hit',
    'vfx_clockwork': 'oracle_arcane_clock',
    'vfx_arcane_repulsor': 'oracle_arcane_repulsor',
}
pending = {}

def vec(point):
    return [float(point[a]) for a in 'xyz']

def sample(keys, time):
    if time <= keys[0]['time']: return vec(keys[0]['data_points'][0])
    if time >= keys[-1]['time']: return vec(keys[-1]['data_points'][-1])
    i = bisect.bisect_right([k['time'] for k in keys], time) - 1
    a, b = keys[i:i+2]
    u = (time-a['time'])/(b['time']-a['time'])
    p1, p2 = vec(a['data_points'][-1]), vec(b['data_points'][0])
    if a['interpolation'] == 'catmullrom' or b['interpolation'] == 'catmullrom':
        p0, p3 = vec(keys[max(0,i-1)]['data_points'][-1]), vec(keys[min(len(keys)-1,i+2)]['data_points'][0])
        return [.5*((2*y)+(-x+z)*u+(2*x-5*y+4*z-w)*u*u+(-x+3*y-3*z+w)*u*u*u)
                for x,y,z,w in zip(p0,p1,p2,p3)]
    return [x+(y-x)*u for x,y in zip(p1,p2)]

with zipfile.ZipFile(sys.argv[1]) as archive:
    for source, name in MODELS.items():
        matches = [p for p in archive.namelist() if p.endswith('/ModelEngine/blueprints/'+source+'.bbmodel') or p == 'ModelEngine/blueprints/'+source+'.bbmodel']
        assert len(matches) == 1, (source, matches)
        model = json.loads(archive.read(matches[0]))
        assert model['meta']['model_format'] == 'free'
        textures, offsets, width = [], {}, 0
        for i,t in enumerate(model['textures']):
            if t['name'].endswith('_e'): continue
            png = base64.b64decode(t['source'].split(',')[1])
            assert png == base64.b64decode(model['textures'][i+1]['source'].split(',')[1])
            image = Image.open(io.BytesIO(png)).convert('RGBA')
            assert image.size == (t['uv_width'],t['uv_height'])
            offsets[i] = width
            textures.append((png,image)); width += image.width
        height = max(image.height for _,image in textures)
        texture_name = 'oracle_arcane_trail' if source == 'vfx_arcane_hit' else name
        dest = ROOT/ASSETS/f'textures/entity/{texture_name}.png'
        dest.parent.mkdir(parents=True,exist_ok=True)
        if len(textures) == 1:
            if source == 'vfx_arcane_hit': assert dest.read_bytes() == textures[0][0]
            else: dest.write_bytes(textures[0][0])
        else:
            atlas = Image.new('RGBA',(width,height)); x = 0
            for _,image in textures: atlas.paste(image,(x,0)); x += image.width
            atlas.save(dest)
        elements = {e['uuid']:e for e in model['elements']}
        bones, used, names = [], set(), {}
        pivot = lambda p: [-p[0],p[1],p[2]]
        rotation = lambda p: [-p[0],-p[1],p[2]]
        def walk(nodes,parent=None):
            for node in nodes:
                assert isinstance(node,dict)
                names[node['uuid']] = node['name']
                bone = {'name':node['name'],'pivot':pivot(node['origin'])}
                if parent: bone['parent'] = parent
                if any(node.get('rotation',[0,0,0])): bone['rotation'] = rotation(node['rotation'])
                cubes, children = [], []
                for child in node['children']:
                    if isinstance(child,dict): children.append(child); continue
                    e = elements[child]
                    assert child not in used and e['type'] == 'cube' and e.get('export',True) and not e.get('rescale')
                    used.add(child)
                    cube = {'origin':[-e['to'][0],e['from'][1],e['from'][2]],
                            'size':[b-a for a,b in zip(e['from'],e['to'])], 'uv':{}}
                    if any(e.get('rotation',[0,0,0])): cube.update(pivot=pivot(e['origin']),rotation=rotation(e['rotation']))
                    if e.get('inflate'): cube['inflate'] = e['inflate']
                    for face,f in e['faces'].items():
                        if f.get('texture') is None: continue
                        u,v,U,V = f['uv']; u += offsets[f['texture']]; U += offsets[f['texture']]
                        mapping = {'uv':[U,V] if face in ('up','down') else [u,v],
                                   'uv_size':[u-U,v-V] if face in ('up','down') else [U-u,V-v]}
                        if f.get('rotation'): mapping['uv_rotation'] = f['rotation']
                        cube['uv'][face] = mapping
                    cubes.append(cube)
                if cubes: bone['cubes'] = cubes
                bones.append(bone); walk(children,bone['name'])
        walk(model['outliner'])
        assert used == elements.keys() and len(set(names.values())) == len(names)
        animations = {}
        for clip in model['animations']:
            animated = {}
            for uid,animator in clip['animators'].items():
                assert uid in names
                channels = {}
                for k in animator['keyframes']:
                    assert k['channel'] in ('position','rotation','scale') and k['interpolation'] in ('linear','catmullrom')
                    assert len(k['data_points']) == 1
                    channels.setdefault(k['channel'],[]).append(k)
                for channel,keys in channels.items():
                    keys.sort(key=lambda k:k['time'])
                    times = {k['time'] for k in keys}
                    if any(k['interpolation']=='catmullrom' for k in keys):
                        # Dense linear bake avoids GeckoLib's different Catmull-Rom endpoint convention.
                        times.update(i/240 for i in range(int(clip['length']*240)+1))
                    values = {}
                    for time in sorted(times):
                        value = sample(keys,time)
                        if channel == 'position': value[0] *= -1
                        values[f'{time:.8f}'.rstrip('0').rstrip('.') or '0'] = value
                    animated.setdefault(names[uid],{})[channel] = values
            animations[clip['name']] = {'loop':True if clip['loop']=='loop' else 'hold_on_last_frame',
                                       'animation_length':clip['length'],'bones':animated}
        pending[ASSETS+f'geckolib/models/entity/{name}.geo.json'] = {'format_version':'1.12.0','minecraft:geometry':[{
            'description':{'identifier':'geometry.'+name,'texture_width':width,'texture_height':height,
                           'visible_bounds_width':12,'visible_bounds_height':12,'visible_bounds_offset':[0,2,0]},'bones':bones}]}
        pending[ASSETS+f'geckolib/animations/entity/{name}.animation.json'] = {'format_version':'1.8.0','animations':animations}
        negative = sum(any(v<0 for v in c['size']) for b in bones for c in b.get('cubes',[]))
        print(f'{name}: {len(used)} cubes, {negative} signed hulls, {len(animations)} clips')
for locale in ['zh_cn','en_us']:
    path = ASSETS+f'lang/{locale}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    data['entity.theaurorian2.oracle_arcane'] = '神官奥术' if locale=='zh_cn' else 'Oracle Arcana'
    for key,cn,en in [('shield','奥术护盾','Arcane Ward'),('blast','奥术爆弹','Arcane Blast'),
                      ('flame','奥术焰流','Arcane Flame'),('glide','焰迹滑行','Flame Glide'),
                      ('chrono','时序灾变','Chrono Cataclysm'),('repulsor','奥术光束炮','Arcane Repulsor')]:
        data['boss.theaurorian2.oracle.'+key] = cn if locale=='zh_cn' else en
    pending[path] = data
(ROOT/'build/oracle-artificer-resources.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
