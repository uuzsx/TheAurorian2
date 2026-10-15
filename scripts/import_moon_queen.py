"""Import the approved two-idle BB5 queen, without editing the source project.

All cubes, signed sizes, pivots and UVs are retained. The two embedded images
share a lossless atlas. Native Bezier/Catmull-Rom motion is sampled within a
bounded error for GeckoLib; the final serializer is json_utils.ps1.
"""
import base64
import bisect
import io
import json
import sys
from pathlib import Path
from PIL import Image
from import_rising_clock_oracle import bake_track

ROOT = Path(__file__).resolve().parents[1]


def value(key, point=0):
    data = key['data_points'][min(point, len(key['data_points']) - 1)]
    return [float(data[a]) for a in 'xyz']


def sample(keys, t, loop):
    if t <= keys[0]['time']:
        return value(keys[0])
    if t >= keys[-1]['time']:
        return value(keys[-1])
    i = bisect.bisect_right([k['time'] for k in keys], t) - 1
    left, right = keys[i:i + 2]
    a, b = value(left, 1), value(right)
    u = (t - left['time']) / (right['time'] - left['time'])
    if left['interpolation'] == 'step':
        return a
    if 'catmullrom' in (left['interpolation'], right['interpolation']):
        p = value(keys[i-1], 1) if i else value(keys[-2], 1) if loop and len(keys) >= 3 else a
        q = value(keys[i+2]) if i+2 < len(keys) else value(keys[1]) if loop and len(keys) >= 3 else b
        return [.5 * (2*x + (-p0+y)*u + (2*p0-5*x+4*y-p3)*u*u
                      + (-p0+3*x-3*y+p3)*u**3) for p0,x,y,p3 in zip(p,a,b,q)]
    if 'bezier' in (left['interpolation'], right['interpolation']):
        result = []
        dt = right['time'] - left['time']
        for axis,(x,y,r,l) in enumerate(zip(a,b,left['bezier_right_value'],right['bezier_left_value'])):
            h1 = min(1,max(0,left['bezier_right_time'][axis]/dt))
            h2 = min(1,max(0,1+right['bezier_left_time'][axis]/dt))
            lo,hi = 0.,1.
            for _ in range(40):
                q = (lo+hi)/2
                time = 3*(1-q)**2*q*h1+3*(1-q)*q*q*h2+q**3
                if time < u:
                    lo = q
                else:
                    hi = q
            q = (lo+hi)/2
            result.append((1-q)**3*x+3*(1-q)**2*q*(x+r)+3*(1-q)*q*q*(y+l)+q**3*y)
        return result
    assert left['interpolation'] == right['interpolation'] == 'linear'
    return [x+(y-x)*u for x,y in zip(a,b)]


def export(source):
    model = json.loads(Path(source).read_text(encoding='utf-8-sig'))
    assert model['meta']['format_version'] == '5.0' and model['meta']['model_format'] == 'free'
    groups = {g['uuid']: g for g in model['groups']}
    elements = {e['uuid']: e for e in model['elements']}
    images = [Image.open(io.BytesIO(base64.b64decode(t['source'].split(',')[1]))).convert('RGBA')
              for t in model['textures']]
    assert [i.size for i in images] == [(256,256), (192,192)]
    offsets = [0,256]
    atlas = Image.new('RGBA', (448,256))
    for offset, image in zip(offsets, images):
        atlas.paste(image, (offset,0))
        assert atlas.crop((offset,0,offset+image.width,image.height)).tobytes() == image.tobytes()
    pivot = lambda v: [-v[0],v[1],v[2]]
    rotation = lambda v: [-v[0],-v[1],v[2]]
    bones, seen = [], set()

    def walk(nodes, parent=None):
        for node in nodes:
            g = groups[node['uuid']]
            bone = {'name':g['name'], 'pivot':pivot(g['origin'])}
            if parent:
                bone['parent'] = parent
            if any(g['rotation']):
                bone['rotation'] = rotation(g['rotation'])
            if g.get('mirror_uv'):
                bone['mirror'] = True
            cubes, children = [], []
            for child in node['children']:
                if isinstance(child,dict):
                    children.append(child)
                    continue
                assert child not in seen
                seen.add(child)
                e = elements[child]
                assert e['type'] == 'cube' and e.get('export',True)
                cube = {'origin':[-e['to'][0],e['from'][1],e['from'][2]],
                        'size':[b-a for a,b in zip(e['from'],e['to'])], 'uv':{}}
                if any(e.get('rotation',[0,0,0])):
                    cube.update(pivot=pivot(e['origin']),rotation=rotation(e['rotation']))
                if 'inflate' in e:
                    cube['inflate'] = e['inflate']
                for side,face in e['faces'].items():
                    if face.get('texture') is None:
                        continue
                    index = face['texture']
                    assert index in (0,1)
                    u,v,U,V = face['uv']
                    u += offsets[index]
                    U += offsets[index]
                    cube['uv'][side] = {'uv':[U,V] if side in ('up','down') else [u,v],
                                       'uv_size':[u-U,v-V] if side in ('up','down') else [U-u,V-v]}
                    if face.get('rotation'):
                        cube['uv'][side]['uv_rotation'] = face['rotation']
                cubes.append(cube)
            if cubes:
                bone['cubes'] = cubes
            bones.append(bone)
            walk(children, g['name'])

    walk(model['outliner'])
    assert seen == elements.keys() and len(bones) == 65 and len(elements) == 245
    assert len({b['name'] for b in bones}) == len(bones)
    animations, errors = {}, {'rotation':0., 'position':0., 'scale':0.}
    eye_bones = {'LeftEyeball','RightEyeball','LeftEyebrow','RightEyebrow'}
    for clip in model['animations']:
        name = clip['name']
        if not (name in ('walk','misc.blink') or name.startswith(('idle.sword_','transition.','rebuilt.'))):
            continue
        assert 'high' not in name
        channels = {}
        for uuid,animator in clip['animators'].items():
            if uuid not in groups:
                # BB ignores orphan UUIDs from the source's previous skeleton.
                # Resolving these by display name would incorrectly animate a different bone.
                continue
            for key in animator.get('keyframes',[]):
                channels.setdefault((groups[uuid]['name'],key['channel']),[]).append(key)
        animation = {'loop':True if clip['loop']=='loop' else 'hold_on_last_frame',
                     'animation_length':clip['length'], 'bones':{}}
        for (bone,channel),keys in channels.items():
            assert channel in errors
            # A single independent blink layer runs in every locomotion state.
            # Its original eye values are preserved; idles do not blink twice.
            if bone in eye_bones and name != 'misc.blink' and channel in ('scale','position'):
                continue
            keys.sort(key=lambda k:k['time'])
            if channel == 'scale' and all(k['interpolation'] == 'step' for k in keys):
                track = {}; previous = value(keys[0])
                for key in keys:
                    current = value(key)
                    if not track or current != previous:
                        track[str(round(key['time'], 6))] = {'pre': previous, 'post': current}
                    previous = current
                animation['bones'].setdefault(bone,{})[channel] = track
                continue
            def evaluate(t):
                v = sample(keys,t,clip['loop']=='loop')
                # RenderUtil adds geometry's base rotation to the snapshot.
                # Animation must contain only BB offsets; adding bind again
                # flares the ribbons and displaces the sword arm.
                return rotation(v) if channel=='rotation' else pivot(v) if channel=='position' else v
            knots = sorted({0.,clip['length'],*(k['time'] for k in keys)})
            track,error = bake_track(evaluate,knots,.025 if channel=='rotation' else .0007)
            animation['bones'].setdefault(bone,{})[channel] = track
            errors[channel] = max(errors[channel],error)
        if name != 'misc.blink':
            # Foreign effect meshes are not imported; retain the approved queen's hidden slash planes.
            animation['bones'].setdefault('vfx_01',{})['scale'] = [0,0,0]
            animation['bones'].setdefault('bone2',{})['scale'] = [0,0,0]
        animations[name] = animation
    assert len(animations) in (12,41)
    assets = 'src/main/resources/assets/theaurorian2/'
    pending = {
        assets+'geckolib/models/entity/moon_queen.geo.json':{
            'format_version':'1.12.0', 'minecraft:geometry':[{
                'description':{'identifier':'geometry.moon_queen','texture_width':448,'texture_height':256,
                    'visible_bounds_width':8,'visible_bounds_height':8,'visible_bounds_offset':[0,2.5,0]},
                'bones':bones}]},
        assets+'geckolib/animations/entity/moon_queen.animation.json':{
            'format_version':'1.8.0','animations':animations},
        assets+'items/moon_queen_spawn_egg.json':{
            'model':{'type':'minecraft:model','model':'minecraft:item/witch_spawn_egg'}},
    }
    for locale,label in [('zh_cn','末代皎月女王·维斯佩拉'),('en_us','Last Moon Queen Vespera')]:
        path = assets+f'lang/{locale}.json'
        lang = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
        lang['entity.theaurorian2.moon_queen'] = label
        lang['item.theaurorian2.moon_queen_spawn_egg'] = label+('刷怪蛋' if locale=='zh_cn' else ' Spawn Egg')
        labels=['持剑警戒','持剑行走','正面受击','侧面受击','踉跄受击','压剑奔跑','警戒奔跑','巡行奔跑','左横斩','回身右斩','上挑斩','重劈','右横斩',
                '跃起重劈','宽幅扫斩','右斜斩','低踢接剑','踏步斩','踏步左斩','水平斩','左斜斩','突刺','突刺接低踢','腾跃重斩','左格挡','右格挡','破防踉跄','重创俯身','屈膝谢幕预览']
        for removed in ('tail_small_swipe','tail_swipe','tail_double_swipe'):
            lang.pop('animation.theaurorian2.queen.'+removed,None)
        imported=[c for c in model['animations'] if c['name'].startswith('rebuilt.')]
        if imported:
            assert len(imported)==len(labels)
            for clip,zh in zip(imported,labels):
                key=clip['name'].removeprefix('rebuilt.')
                lang['animation.theaurorian2.queen.'+key]=zh if locale=='zh_cn' else key.replace('_',' ').title()
            messages={'queen_animation':('动画预览 %s/%s：%s','Animation preview %s/%s: %s'),
                      'queen_animation_stopped':('女王动画预览已停止','Queen animation preview stopped'),
                      'queen_animation_unknown':('没有这个女王动画：%s','Unknown queen animation: %s'),
                      'queen_animation_missing':('16格内没有皎月女王，请先用刷怪蛋生成','No Moon Queen within 16 blocks. Spawn one first.')}
            for k,pair in messages.items():lang['message.theaurorian2.'+k]=pair[0 if locale=='zh_cn' else 1]
        pending[path] = lang
    texture = ROOT/assets/'textures/entity/moon_queen.png'
    texture.parent.mkdir(parents=True,exist_ok=True)
    atlas.save(texture)
    (ROOT/'build/moon-queen-resources.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
    print(f'Validated {len(bones)} bones, {len(elements)} cubes, {len(animations)} clips; max bake errors {errors}')


if __name__ == '__main__':
    export(sys.argv[1])
