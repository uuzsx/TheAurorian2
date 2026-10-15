"""Export the approved BB 4.9 oracle without modifying its geometry or authored clips.

Adds encounter-timed variants. Final JSON is written by the companion PowerShell script.
"""
import base64
import copy
import json
from pathlib import Path
import sys
from export_blue_tail_wolf import evaluate, tracks, number

ROOT = Path(__file__).resolve().parents[1]


def bake_track(sample, knots, tolerance):
    times, maximum_error = [], 0.
    def flatten(a, b, depth=0):
        nonlocal maximum_error
        va, vb = sample(a), sample(b)
        error = max(abs((1-u)*x+u*y-z) for u in (.25,.5,.75)
                    for x,y,z in zip(va,vb,sample(a+(b-a)*u)))
        if error > tolerance and depth < 12:
            flatten(a,(a+b)/2,depth+1); flatten((a+b)/2,b,depth+1)
        else:
            assert error <= tolerance
            maximum_error = max(maximum_error,error)
            times.append(a)
    for a,b in zip(knots,knots[1:]): flatten(a,b)
    times.append(knots[-1])
    values = [[float(number(v)) for v in sample(t)] for t in times]
    track = values[0] if all(v == values[0] for v in values) else {number(t):v for t,v in zip(times,values)}
    return track, maximum_error


def export(source):
    model = json.loads(Path(source).read_text(encoding='utf-8-sig'))
    assert model['meta']['format_version'] == '4.9'
    assert model['meta']['model_format'] == 'geckolib_model'
    assert model['resolution'] == {'width': 128, 'height': 128}
    elements = {e['uuid']: e for e in model['elements']}
    bones, seen = [], set()
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
    assert seen == elements.keys() and len(bones) == 28 and len(elements) == 64
    names = {b['name'] for b in bones}
    assert len(names) == len(bones)
    animations, errors = {}, {'rotation': 0., 'position': 0.}
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
                if channel == 'position':
                    value[0] = -value[0]
                return value
            tolerance = .035 if channel == 'rotation' else .001
            knots = sorted({0, length, *(k['time'] for k in keys)})
            track,error = bake_track(sample,knots,tolerance)
            errors[channel] = max(errors[channel],error)
            animation['bones'].setdefault(bone, {})[channel] = track
        animations[name] = animation
    assert len(animations) == 11

    # Distinct gravity choreography: present staff, sustain the channel, lift for the slam, recover.
    # All body poses come from the approved clips. The six-tick blend keeps the hand/staff attached.
    gravity = {'loop':'hold_on_last_frame','animation_length':5.8,'bones':{}}
    source_tracks = {c['name']:tracks(c) for c in model['animations'] if not c['name'].startswith('preview.')}
    pairs = set().union(*(t.keys() for n,t in source_tracks.items() if n in ('channel_start','channel_loop','cast_skyfall')))
    for bone,channel in sorted(pairs):
        def pose(name,t):
            keys=source_tracks[name].get((bone,channel))
            value=evaluate(keys,t) if keys else [0.,0.,0.]
            if channel=='position': value[0]=-value[0]
            return value
        def sample(t):
            if t<=1.5: return pose('channel_start',t*.8)
            if t<=3.8: return pose('channel_loop',(t-1.5)*2.4/2.3)
            if t<4.1:
                a,b=pose('channel_loop',2.4),pose('cast_skyfall',1.95)
                u=(t-3.8)/.3; u=u*u*(3-2*u)
                return [x+(y-x)*u for x,y in zip(a,b)]
            if t<=4.6: return pose('cast_skyfall',1.95+(t-4.1)*.5)
            return pose('cast_skyfall',2.2+(t-4.6)*1.4/1.2)
        tolerance=.035 if channel=='rotation' else .001
        # Include source key boundaries and composition boundaries before adaptive flattening.
        knots={0.,1.5,3.8,4.1,4.6,5.8}
        for name,mapping in [('channel_start',lambda t:t/.8),('channel_loop',lambda t:1.5+t*2.3/2.4),
                             ('cast_skyfall',lambda t:4.1+(t-1.95)*2 if t<=2.2 else 4.6+(t-2.2)*1.2/1.4)]:
            for key in source_tracks[name].get((bone,channel),[]):
                if name!='cast_skyfall' or key['time']>=1.95: knots.add(round(mapping(key['time']),8))
        track,error = bake_track(sample,sorted(knots),tolerance)
        errors[channel] = max(errors[channel],error)
        gravity['bones'].setdefault(bone,{})[channel] = track
    animations['cast_gravity'] = gravity
    animations['boss_awaken'] = copy.deepcopy(animations['cast_ward'])
    death = {'animation_length':1., 'loop':'hold_on_last_frame', 'bones':{}}
    for bone, channels in animations['idle_hover']['bones'].items():
        death['bones'][bone] = {}
        for channel, values in channels.items():
            start = next(iter(values.values())) if isinstance(values,dict) else values
            end = start.copy()
            if bone == 'root' and channel == 'position': end[1] -= 14
            if bone == 'body' and channel == 'rotation': end[0] += 18
            if bone.startswith('wing_') and channel == 'rotation': end[2] += 16 if 'right' in bone else -16
            death['bones'][bone][channel] = {'0':start,'1':end}
    animations['boss_death'] = death
    assets = 'src/main/resources/assets/theaurorian2/'
    data = 'src/main/resources/data/theaurorian2/'
    entity = 'rising_clock_oracle'
    pending = {
        assets+f'geckolib/models/entity/{entity}.geo.json': {'format_version':'1.12.0','minecraft:geometry':[{
            'description':{'identifier':'geometry.'+entity,'texture_width':128,'texture_height':128,
                           'visible_bounds_width':8,'visible_bounds_height':9,'visible_bounds_offset':[0,3,0]},'bones':bones}]},
        assets+f'geckolib/animations/entity/{entity}.animation.json': {'format_version':'1.8.0','animations':animations},
        assets+f'items/{entity}_spawn_egg.json': {'model':{'type':'minecraft:model','model':'minecraft:item/evoker_spawn_egg'}},
        assets+'blockstates/oracle_displaced_terrain.json': {'multipart':[{'apply':{'model':'theaurorian2:block/oracle_displaced_terrain'}}]},
        assets+'models/block/oracle_displaced_terrain.json': {'textures':{'particle':'minecraft:block/stone'},'elements':[]},
        data+f'loot_table/entities/{entity}.json': {'type':'minecraft:entity','random_sequence':'theaurorian2:entities/'+entity,
            'pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:amethyst_shard',
                'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':4,'max':8}}]}]}]},
    }
    for locale,label in [('zh_cn','起翼之钟刻神官'),('en_us','Rising Clock Oracle')]:
        path = assets+f'lang/{locale}.json'
        lang = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
        lang['entity.theaurorian2.'+entity] = label
        lang['entity.theaurorian2.oracle_spell'] = '钟刻术式' if locale=='zh_cn' else 'Oracle Spell'
        lang['block.theaurorian2.oracle_displaced_terrain'] = '浮空地形占位' if locale=='zh_cn' else 'Displaced Terrain'
        lang['item.theaurorian2.'+entity+'_spawn_egg'] = label+('刷怪蛋' if locale=='zh_cn' else ' Spawn Egg')
        for key,cn,en in [('lightning','落雷裁击','Thunderfall'),('gravity','失重坠落','Gravity Collapse'),('awaken','钟翼展开','The Clock Unfolds')]:
            lang['boss.theaurorian2.oracle.'+key] = cn if locale=='zh_cn' else en
        pending[path] = lang
    (ROOT/assets/f'textures/entity/{entity}.png').write_bytes(base64.b64decode(model['textures'][0]['source'].split(',')[1]))
    (ROOT/'build/oracle-resources.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
    print(f'Validated {len(elements)} cubes, {len(bones)} bones, {len(animations)} clips; maximum bake errors {errors}')


if __name__ == '__main__':
    export(sys.argv[1])
