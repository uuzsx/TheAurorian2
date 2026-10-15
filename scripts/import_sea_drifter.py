"""Import the approved BB5 sky-ray, preserving cubes and baking Bezier tracks.

Final JSON is written through json_utils.ps1. The preview's eight-pixel root
lift is replaced by real entity flight; all secondary floating motion remains.
"""
import base64
import json
import sys
from pathlib import Path
from export_blue_tail_wolf import evaluate, tracks
from import_rising_clock_oracle import bake_track

ROOT = Path(__file__).resolve().parents[1]

def export(source):
    model = json.loads(Path(source).read_text(encoding='utf-8-sig'))
    assert model['meta']['format_version'] == '5.0'
    assert model['meta']['model_format'] == 'geckolib_model'
    elements = {e['uuid']: e for e in model['elements']}
    groups = {g['uuid']: g for g in model['groups']}
    bones, seen = [], set()
    def pivot(v): return [-v[0], v[1], v[2]]
    def rotation(v): return [-v[0], -v[1], v[2]]
    def walk(nodes, parent=None):
        for node in nodes:
            group = groups[node['uuid']]
            bone = dict(name=group['name'], pivot=pivot(group['origin']))
            if parent: bone['parent'] = parent
            if any(group.get('rotation', [0,0,0])): bone['rotation'] = rotation(group['rotation'])
            children, cubes = [], []
            for child in node['children']:
                if isinstance(child, dict): children.append(child); continue
                assert child not in seen
                seen.add(child)
                e = elements[child]
                assert e['type'] == 'cube' and e.get('export', True)
                cube = dict(origin=[-e['to'][0], e['from'][1], e['from'][2]],
                            size=[b-a for a,b in zip(e['from'], e['to'])], uv={})
                if any(e.get('rotation', [0,0,0])): cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
                if 'inflate' in e: cube['inflate'] = e['inflate']
                for face, uv in e['faces'].items():
                    if uv.get('texture') is None: continue
                    assert uv['texture'] == 0
                    u,v,U,V = uv['uv']
                    cube['uv'][face] = dict(uv=[U,V] if face in ('up','down') else [u,v],
                        uv_size=[u-U,v-V] if face in ('up','down') else [U-u,V-v])
                    if uv.get('rotation'): cube['uv'][face]['uv_rotation'] = uv['rotation']
                cubes.append(cube)
            if cubes: bone['cubes'] = cubes
            bones.append(bone)
            walk(children, bone['name'])
    walk(model['outliner'])
    assert seen == elements.keys()
    assert len({b['name'] for b in bones}) == len(bones)
    animations, errors = {}, {'rotation':0, 'position':0}
    for clip in model['animations']:
        if clip['name'].startswith('preview.'): continue
        animation = dict(loop=True if clip['loop']=='loop' else False, animation_length=clip['length'], bones={})
        for (bone, channel), keys in tracks(clip).items():
            assert channel in errors
            def sample(t):
                value = evaluate(keys, t)
                if channel == 'rotation': return rotation(value) # BB5 native axes -> Bedrock axes.
                value[0] = -value[0]
                if bone == 'root': value[1] -= 8
                return value
            knots = sorted({0,clip['length'],*(k['time'] for k in keys)})
            track, error = bake_track(sample, knots, .035 if channel=='rotation' else .001)
            errors[channel] = max(errors[channel], error)
            animation['bones'].setdefault(bone,{})[channel] = track
        animations[clip['name']] = animation
    assert len(animations)==11
    animations['flight_attitude'] = {'loop':True,'animation_length':1,'bones':{
        'root':{'rotation':['query.theaurorian2_sea_pitch',0,'query.theaurorian2_sea_bank']}}}
    assets = 'src/main/resources/assets/theaurorian2/'
    data = 'src/main/resources/data/theaurorian2/'
    pending = {
        assets+'geckolib/models/entity/sea_drifter.geo.json': {'format_version':'1.12.0','minecraft:geometry':[{
            'description':dict(identifier='geometry.sea_drifter',texture_width=64,texture_height=64,
                visible_bounds_width=6,visible_bounds_height=4,visible_bounds_offset=[0,.5,.8]),'bones':bones}]},
        assets+'geckolib/animations/entity/sea_drifter.animation.json':dict(format_version='1.8.0',animations=animations),
        assets+'items/sea_drifter_spawn_egg.json':{'model':{'type':'minecraft:model','model':'minecraft:item/dolphin_spawn_egg'}},
        data+'loot_table/entities/sea_drifter.json':{'type':'minecraft:entity','pools':[]},
        data+'tags/entity_type/sea_drifter_prey.json':{'replace':False,'values':[
            'theaurorian2:moon_fish','theaurorian2:aurorian_winged_fish','theaurorian2:frostfin']},
        data+'tags/worldgen/biome/has_sea_drifter.json':{'replace':False,'values':[
            'theaurorian2:aurorian_'+b for b in ['ocean','deep_ocean','cold_ocean','deep_cold_ocean',
                'lukewarm_ocean','deep_lukewarm_ocean','warm_ocean','frozen_ocean','deep_frozen_ocean']]},
        data+'neoforge/biome_modifier/sea_drifter_spawns.json':{'type':'neoforge:add_spawns',
            'biomes':'#theaurorian2:has_sea_drifter','spawners':{'type':'theaurorian2:sea_drifter','weight':2,'minCount':3,'maxCount':4}},
    }
    for locale, label in [('zh_cn','海巡鳐'),('en_us','Sea Drifter')]:
        path=assets+f'lang/{locale}.json'
        lang=json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
        lang['entity.theaurorian2.sea_drifter']=label
        lang['item.theaurorian2.sea_drifter_spawn_egg']=label+('刷怪蛋' if locale=='zh_cn' else ' Spawn Egg')
        pending[path]=lang
    (ROOT/assets/'textures/entity/sea_drifter.png').write_bytes(base64.b64decode(model['textures'][0]['source'].split(',')[1]))
    (ROOT/'build/sea-drifter-resources.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
    print(f'Validated {len(elements)} cubes, {len(bones)} bones, {len(animations)} clips; bake error {errors}')

if __name__=='__main__': export(sys.argv[1])
