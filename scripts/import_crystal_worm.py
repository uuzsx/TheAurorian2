"""Import the approved BB5 crystal worm, preserving cubes and baking Bezier tracks.

Final JSON is written through json_utils.ps1. Authored burrow root motion drives
the server entity; all segment rotations and ordinary body motion remain.
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
                if bone == 'root':
                    if clip['name'].startswith('burrow'): return [0,0,0]
                    value[1] -= 2.2
                return value
            knots = sorted({0,clip['length'],*(k['time'] for k in keys)})
            track, error = bake_track(sample, knots, .035 if channel=='rotation' else .001)
            errors[channel] = max(errors[channel], error)
            animation['bones'].setdefault(bone,{})[channel] = track
        animations[clip['name']] = animation
    assert len(animations)==13
    animations['motion_attitude'] = {'loop':True,'animation_length':1,'bones':{
        'root':{'rotation':['query.theaurorian2_worm_pitch',0,0]}}}
    assets = 'src/main/resources/assets/theaurorian2/'
    data = 'src/main/resources/data/theaurorian2/'
    pending = {
        assets+'geckolib/models/entity/crystal_worm.geo.json': {'format_version':'1.12.0','minecraft:geometry':[{
            'description':dict(identifier='geometry.crystal_worm',texture_width=32,texture_height=32,
                visible_bounds_width=7,visible_bounds_height=5,visible_bounds_offset=[0,.5,0]),'bones':bones}]},
        assets+'geckolib/animations/entity/crystal_worm.animation.json':dict(format_version='1.8.0',animations=animations),
        assets+'items/crystal_worm_spawn_egg.json':{'model':{'type':'minecraft:model','model':'minecraft:item/silverfish_spawn_egg'}},
        data+'loot_table/entities/crystal_worm.json':{'type':'minecraft:entity','pools':[
            {'rolls':1,'entries':[{'type':'minecraft:item','name':'theaurorian2:aurorian_cobblestone'}],
             'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]},
            {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}, {'condition':'minecraft:random_chance','chance':.35}],
             'entries':[{'type':'minecraft:item','name':'theaurorian2:aurorian_crystal'}]}]},
        data+'tags/damage_type/crystal_worm_magic.json':{'replace':False,'values':['#neoforge:is_magic',
            'theaurorian2:oracle_repulsor','theaurorian2:oracle_arcane_fire','theaurorian2:oracle_empowered_blast',
            'theaurorian2:oracle_lightning','theaurorian2:oracle_gravity_slam','theaurorian2:oracle_chrono',
            'theaurorian2:oracle_black_coffin','theaurorian2:spider_mother_beam',
            'theaurorian2:enchantment_lightning','theaurorian2:enchantment_reflection']},
        data+'tags/item/crystal_worm_mining_tools.json':{'replace':False,'values':['#minecraft:pickaxes',
            'theaurorian2:crystalline_pickaxe','theaurorian2:aurorianite_pickaxe',
            'theaurorian2:moonstone_pickaxe','theaurorian2:moonsilver_pickaxe',
            'theaurorian2:umbra_pickaxe','theaurorian2:queens_chipper']},
        data+'neoforge/biome_modifier/crystal_worm_spawns.json':{'type':'neoforge:add_spawns',
            'biomes':'#theaurorian2:is_aurorian','spawners':{'type':'theaurorian2:crystal_worm','weight':2,'minCount':1,'maxCount':1}},
    }
    for locale, label in [('zh_cn','晶岩蠕虫'),('en_us','Crystal Rock Worm')]:
        path=assets+f'lang/{locale}.json'
        lang=json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
        lang['entity.theaurorian2.crystal_worm']=label
        lang['item.theaurorian2.crystal_worm_spawn_egg']=label+('刷怪蛋' if locale=='zh_cn' else ' Spawn Egg')
        pending[path]=lang
    (ROOT/assets/'textures/entity/crystal_worm.png').write_bytes(base64.b64decode(model['textures'][0]['source'].split(',')[1]))
    motion=[]
    for name in ['burrow_in','burrow_out']:
        clip=next(a for a in model['animations'] if a['name']==name)
        keys=tracks(clip)['root','position'];start=evaluate(keys,0)
        rows=[]
        for i in range(round(clip['length']*20)+1):
            p=evaluate(keys,i/20)
            assert abs(p[0]-start[0])<1e-7
            rows.append(f'        {{{(p[1]-start[1])/16:.9f}, {-(p[2]-start[2])/16:.9f}}}')
        motion.append('    private static final double[][] '+('ENTRY' if name=='burrow_in' else 'EXIT')+' = {\n'+',\n'.join(rows)+'\n    };')
    java='package cn.teampancake.theaurorian2.common.entity;\n\nimport net.minecraft.util.Mth;\nimport net.minecraft.world.phys.Vec3;\n\n/** Generated from the approved worm BB project: entity travel replaces root animation offsets. */\npublic final class CrystalWormBurrowMotion {\n    public static final int ENTRY_TICKS = 48, EXIT_TICKS = 64;\n    private CrystalWormBurrowMotion() {}\n    public static Vec3 sample(boolean entry, float ticks) {\n        double[][] table = entry ? ENTRY : EXIT;\n        double t = Mth.clamp(ticks, 0, table.length - 1);\n        int a = Math.min((int)t, table.length - 2);\n        double f = t - a;\n        return new Vec3(0, Mth.lerp(f, table[a][0], table[a+1][0]), Mth.lerp(f, table[a][1], table[a+1][1]));\n    }\n'
    (ROOT/'src/main/java/cn/teampancake/theaurorian2/common/entity/CrystalWormBurrowMotion.java').write_text(java+'\n'.join(motion)+'\n}\n',encoding='utf-8')
    (ROOT/'build/crystal-worm-resources.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
    print(f'Validated {len(elements)} cubes, {len(bones)} bones, {len(animations)} clips; bake error {errors}')

if __name__=='__main__': export(sys.argv[1])
