"""Import the approved Reqium first-person rig; the sword mesh stays an existing item.

Usage: python scripts/import_greatsword_viewmodel.py path/to/ec-reqium_netherite_sword.bbmodel
Only the arm/sword rig and motion keyframes are imported, without attack effects or server-pack mechanics.
"""
import json
from pathlib import Path
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/theaurorian2'


def main():
    model = json.loads(Path(sys.argv[1]).read_text(encoding='utf-8-sig'))
    groups = {g['uuid']: g for g in model['groups']}
    elements = {e['uuid']: e for e in model['elements']}
    bones, indices = [], {}
    excluded_effects = set()

    def exclude_effect_branch(node):
        excluded_effects.add(node['uuid'])
        for child in node['children']:
            if not isinstance(child, str):
                exclude_effect_branch(child)

    def visit(nodes, parent=-1):
        for node in nodes:
            if isinstance(node, str):
                continue
            group = groups[node['uuid']]
            if group['name'].startswith('ef_') or group['name'] == 'all_ef_mini_slash':
                exclude_effect_branch(node)
                continue
            if group['name'] == 'hitbox' or group['name'].startswith('armor['):
                continue
            index = len(bones)
            indices[group['uuid']] = index
            bone = dict(name=group['name'], parent=parent, pivot=group['origin'],
                        rotation=group.get('rotation', [0, 0, 0]), cubes=[])
            # ModelEngine's world-space billboard is replaced by a native camera-space pass.
            if group['name'] == 'hi_orbit':
                bone['rotation'] = [0, 0, 0]
            bone['weapon'] = group['name'] == 'reqium_netherite_sword'
            bones.append(bone)
            if not bone['weapon']:
                for uuid in node['children']:
                    if not isinstance(uuid, str):
                        continue
                    e = elements[uuid]
                    cube = {k: e[k] for k in ('from', 'to', 'origin', 'rotation', 'inflate') if k in e}
                    cube['sleeve'] = e['name'].startswith('hat_')
                    cube['faces'] = {}
                    for side, face in e['faces'].items():
                        if 'texture' in face and face['texture'] is None:
                            continue
                        assert face.get('texture') is None
                        cube['faces'][side] = dict(uv=face['uv'], rotation=face.get('rotation', 0), material='skin')
                    bone['cubes'].append(cube)
            visit(node['children'], index)

    visit(model['outliner'])
    clips = {}
    frame_count = 0
    for animation in model['animations']:
        channels, sounds = [], []
        for uuid, animator in animation['animators'].items():
            if animator['type'] == 'effect':
                for frame in animator.get('keyframes', []):
                    script = frame['data_points'][0]['script']
                    sounds.append(dict(time=frame['time'], sound='swing' if script.endswith('emote_swing') else 'resonate'))
                continue
            if uuid not in indices:
                assert uuid in excluded_effects or not animator.get('keyframes')
                continue
            assert not animator.get('rotation_global') and not animator.get('quaternion_interpolation')
            for channel in ('rotation', 'position', 'scale'):
                frames = []
                for frame in sorted(animator.get('keyframes', []), key=lambda f: f['time']):
                    if frame['channel'] != channel:
                        continue
                    assert len(frame['data_points']) == 1
                    assert frame['interpolation'] in ('linear', 'catmullrom', 'step')
                    value = [float(frame['data_points'][0][axis]) for axis in 'xyz']
                    frames.append(dict(time=frame['time'], value=value, interpolation=frame['interpolation']))
                if frames:
                    frame_count += len(frames)
                    channels.append(dict(bone=indices[uuid], channel=channel, frames=frames))
        clips[animation['name']] = dict(length=animation['length'], channels=channels, sounds=sorted(sounds, key=lambda s: s['time']))

    result = dict(format_version=1, bones=bones, animations=clips)
    # Repository JSON writer validates and produces the canonical compact representation.
    with tempfile.TemporaryDirectory() as directory:
        raw = Path(directory) / 'import.json'
        raw.write_text(json.dumps(result), encoding='utf-8')
        target = ASSETS / 'animations/item/moonsilver_great_sword_view.json'
        def quote(path):
            return "'" + str(path).replace("'", "''") + "'"
        command = f". {quote(ROOT / 'scripts/json_utils.ps1')}; Write-Json {quote(target)} (Get-Content -LiteralPath {quote(raw)} -Raw | ConvertFrom-Json)"
        subprocess.run(['powershell', '-NoProfile', '-Command', command], check=True)
    print(f'Imported {len(bones)} bones, {len(clips)} animations and {frame_count} motion keys; attack effects excluded.')


if __name__ == '__main__':
    main()
