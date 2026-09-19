"""Import the supplied blue teleport VFX without repainting or simplifying its rig.

The real player's renderer replaces the pack's sample skin rig. Authored effect
geometry (including inverted hulls), pivots, UVs and both timelines are retained.
Run the PS1 wrapper to write compact, validated project JSON.
"""
import base64
import json
import struct
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1])
FFMPEG = sys.argv[2] if len(sys.argv) > 2 else 'ffmpeg'
A = 'src/main/resources/assets/theaurorian2/'
PREFIX = 'plugins/MythicMobs/packs/TeleportAnimation/'
with zipfile.ZipFile(SOURCE) as archive:
    model = json.loads(archive.read(PREFIX + 'models/ac_vfx_teleport_charge_blue.bbmodel'))
    sound = archive.read(PREFIX + 'assets/merge/assets/minecraft/sounds/tugkandeman/tp2.ogg')

elements = {e['uuid']: e for e in model['elements']}
bones, names, used = [], {}, set()
def pivot(v): return [-v[0], v[1], v[2]]
def rotation(v): return [-v[0], -v[1], v[2]]
def walk(nodes, parent=None):
    for node in nodes:
        names[node['uuid']] = node['name']
        bone = {'name': node['name'], 'pivot': pivot(node['origin'])}
        if parent: bone['parent'] = parent
        if any(node.get('rotation', [0, 0, 0])): bone['rotation'] = rotation(node['rotation'])
        cubes, children = [], []
        for child in node['children']:
            if isinstance(child, dict):
                children.append(child)
                continue
            e = elements[child]
            assert child not in used and e['type'] == 'cube' and not e.get('rescale')
            used.add(child)
            cube = {'origin': [-e['to'][0], e['from'][1], e['from'][2]],
                    'size': [e['to'][i] - e['from'][i] for i in range(3)], 'uv': {}}
            if any(e.get('rotation', [0, 0, 0])):
                cube.update(pivot=pivot(e['origin']), rotation=rotation(e['rotation']))
            if e.get('inflate'): cube['inflate'] = e['inflate']
            for face, f in e['faces'].items():
                if f.get('texture') is None: continue
                assert f['texture'] == 1 and not f.get('rotation')
                u, v, U, V = f['uv']
                cube['uv'][face] = {'uv': [U, V] if face in ('up', 'down') else [u, v],
                    'uv_size': [u-U, v-V] if face in ('up', 'down') else [U-u, V-v]}
            cubes.append(cube)
        if cubes: bone['cubes'] = cubes
        bones.append(bone)
        walk(children, bone['name'])

# Only the VFX tree: sample Steve/skin rig and invisible plugin hitbox are not
# effect geometry. Actual avatars retain their own skin layers, slim arms/armor.
walk([model['outliner'][0]])
assert len(used) == 79 and len(bones) == 81
animations = {}
for anim in model['animations']:
    channels_by_bone = {}
    for uid, animator in anim['animators'].items():
        if uid not in names: continue
        channels = {}
        for k in sorted(animator['keyframes'], key=lambda k: k['time']):
            assert k['channel'] in ('rotation', 'position', 'scale')
            assert k['interpolation'] in ('linear', 'catmullrom')
            points = [[float(p[axis]) for axis in 'xyz'] for p in k['data_points']]
            value = points[-1]
            if k['interpolation'] == 'catmullrom': value = {'post': points[-1], 'lerp_mode': 'catmullrom'}
            if len(points) > 1: value = {'pre': points[0], 'post': points[-1], 'lerp_mode': k['interpolation']}
            channels.setdefault(k['channel'], {})[str(k['time'])] = value
        channels_by_bone[names[uid]] = channels
    animations[anim['name']] = {'loop': 'hold_on_last_frame', 'animation_length': anim['length'], 'bones': channels_by_bone}

pending = {
    A+'geckolib/models/entity/world_scroll_teleport.geo.json': {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.world_scroll_teleport', 'texture_width': 128, 'texture_height': 128,
            'visible_bounds_width': 16, 'visible_bounds_height': 2002, 'visible_bounds_offset': [0, 1000, 0]}, 'bones': bones}]},
    A+'geckolib/animations/entity/world_scroll_teleport.animation.json': {'format_version': '1.8.0', 'animations': animations}
}
png = base64.b64decode(model['textures'][1]['source'].split(',')[1])
assert png == base64.b64decode(model['textures'][2]['source'].split(',')[1])
assert struct.unpack('>II', png[16:24]) == (128, 128)
# These two source atlases are byte-identical; the renderer uses one emissive pass.
dest = ROOT/A/'textures/entity/world_scroll_teleport.png'
dest.parent.mkdir(parents=True, exist_ok=True)
dest.write_bytes(png)
# Match WorldScrollTeleportEntity.animationSeconds, preserving the cue's pitch.
# A fixed playback pitch cannot follow the fast arrival head and normal-speed tail.
# Conversion happens only during import; the game plays ordinary preloaded OGGs.
with tempfile.TemporaryDirectory(prefix='aurorian-teleport-audio-') as temp:
    original = Path(temp)/'original.ogg'
    original.write_bytes(sound)
    for name, split, head_length, tempo in [
        ('world_scroll_teleport', 2.65, 3, 'atempo=0.8833333333333333'),
        ('world_scroll_teleport_arrival', 3, 1, 'atempo=1.5,atempo=2')
    ]:
        dest = ROOT/A/f'sounds/effect/{name}.ogg'
        dest.parent.mkdir(parents=True, exist_ok=True)
        filters = (f'[0:a]asplit=2[a][b];[a]atrim=0:{split},asetpts=PTS-STARTPTS,'
                   f'{tempo},apad,atrim=duration={head_length}[head];'
                   f'[b]atrim={split}:5,asetpts=PTS-STARTPTS[tail];'
                   '[head][tail]concat=n=2:v=0:a=1,apad,'
                   f'atrim=end_sample={round((head_length + 5 - split) * 44100)},asetpts=N/SR/TB[out]')
        subprocess.run([FFMPEG, '-hide_banner', '-loglevel', 'error', '-y', '-i', str(original),
                        '-filter_complex', filters, '-map', '[out]', '-ac', '1', '-ar', '44100',
                        '-c:a', 'libvorbis', '-q:a', '5', str(dest)], check=True)
sounds = json.loads((ROOT/A/'sounds.json').read_text(encoding='utf-8-sig'))
for name in ('world_scroll_teleport', 'world_scroll_teleport_arrival'):
    sounds[name] = {'subtitle': 'subtitles.theaurorian2.world_scroll_teleport',
        'sounds': [{'name': f'theaurorian2:effect/{name}', 'attenuation_distance': 24, 'preload': True}]}
pending[A+'sounds.json'] = sounds
for lang, title, subtitle in [('zh_cn', '卷轴传送光效', '传送能量涌动'),
                              ('en_us', 'World Scroll Teleport Effect', 'Teleport energy surges')]:
    path = A+f'lang/{lang}.json'
    data = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
    data['entity.theaurorian2.world_scroll_teleport'] = title
    data['subtitles.theaurorian2.world_scroll_teleport'] = subtitle
    pending[path] = data
path = 'src/main/resources/theaurorian2.mixins.json'
mixins = json.loads((ROOT/path).read_text(encoding='utf-8-sig'))
if 'WorldScrollPlayerRendererMixin' not in mixins['client']:
    mixins['client'].append('WorldScrollPlayerRendererMixin')
pending[path] = mixins
(ROOT/'build').mkdir(exist_ok=True)
(ROOT/'build/world-scroll-teleport-resources.json').write_text(json.dumps(pending, ensure_ascii=False), encoding='utf-8')
print(f'Validated {len(used)} VFX cubes, {len(bones)} bones, two original timelines, original PNG and retimed mono OGG cues.')
