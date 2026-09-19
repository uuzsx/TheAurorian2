"""Bake approved Blockbench keys into vanilla animation definitions (no runtime parsing)."""
import json, sys
from pathlib import Path
root=Path(__file__).resolve().parents[1]
animal=sys.argv[1]
assert animal in {'pig','sheep','rabbit'}
source=root/f'exports/animal_animation_previews/aurorian_{animal}_preview.bbmodel'
model=json.loads(source.read_text(encoding='utf-8-sig'))
names={a['name'] for a in model['animations'] if not a['name'].startswith('preview_')}
assert len(names)==6
def f(v):
    s=format(float(v),'.9g')
    return s+('F' if '.' in s or 'e' in s else '.0F')
lines=['package cn.teampancake.theaurorian2.client.animation;', '',
 'import net.minecraft.client.animation.*;', '',
 '/** Generated from the approved animal Blockbench animations by scripts/export_animal_animations.py. */',
 f'public final class Aurorian{animal.title()}Animations {{',f'    private Aurorian{animal.title()}Animations() {{}}']
found=set()
for a in model['animations']:
    if a['name'] not in names: continue
    found.add(a['name'])
    lines.append('    public static final AnimationDefinition '+a['name'].upper()+' = AnimationDefinition.Builder.withLength('+f(a['length'])+')'+('.looping()' if a['loop']=='loop' else ''))
    for animator in a['animators'].values():
        grouped={}
        for k in animator.get('keyframes',[]): grouped.setdefault(k['channel'],[]).append(k)
        for channel,keys in grouped.items():
            target={'rotation':'ROTATION','position':'POSITION','scale':'SCALE'}[channel]
            lines.append('        .addAnimation("'+animator['name']+'", new AnimationChannel(AnimationChannel.Targets.'+target+',')
            for i,k in enumerate(sorted(keys,key=lambda k:k['time'])):
                assert k['interpolation']=='catmullrom' and len(k['data_points'])==1
                v=[float(k['data_points'][0][axis]) for axis in 'xyz']
                # Java ModelPart reflects the Blockbench Y axis: Rx/Rz change sign.
                if channel=='rotation': v=[-v[0],v[1],-v[2]]
                fn={'rotation':'degreeVec','position':'posVec','scale':'scaleVec'}[channel]
                lines.append('            new Keyframe('+f(k['time'])+', KeyframeAnimations.'+fn+'('+', '.join(map(f,v))+'), AnimationChannel.Interpolations.CATMULLROM)'+(',' if i<len(keys)-1 else '))'))
    lines.append('        .build();')
assert found==names
lines.append('}')
dest=root/f'src/main/java/cn/teampancake/theaurorian2/client/animation/Aurorian{animal.title()}Animations.java'
dest.parent.mkdir(parents=True,exist_ok=True)
dest.write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('Exported all 6 approved animations without changing keyframe times or values beyond axis conversion.')
