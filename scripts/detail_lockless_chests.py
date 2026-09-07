"""Add a horizontal wooden front inlay; keep double-chest seam faces hidden."""
import copy
import json
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'exports/aurorian_lockless_chests'

def cube(name, lo, hi, tile):
    u, v = tile
    sizes = [b-a for a,b in zip(lo,hi)]
    faces = {}
    for face in ('north','south','west','east','up','down'):
        width = sizes[2] if face in ('west','east') else sizes[0]
        height = sizes[2] if face in ('up','down') else sizes[1]
        faces[face] = {'uv':[u,v,u+min(16,width*2),v+min(16,height*2)],'texture':0}
    return {'name':name,'type':'cube','uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,name)),
            'from':lo,'to':hi,'origin':[(a+b)/2 for a,b in zip(lo,hi)],
            'rotation':[0,0,0],'box_uv':False,'autouv':0,'faces':faces,'color':0}

def ornaments(double):
    center = 16 if double else 8
    half = 4 if double else 2.4
    pieces = [cube('正面横向木饰板',[center-half,5.1,14.51],[center+half,7.25,14.82],(16,0)),
              cube('木饰板内嵌木纹',[center-half+.4,5.45,14.82],[center+half-.4,6.9,14.87],(0,0))]
    for side in (-1,1):
        x=center+side*(half-.22)
        for y in (5.5,6.55):
            pieces.append(cube(f'饰板铆钉_{side}_{y}',[x-.12,y,14.82],[x+.12,y+.24,14.94],(0,16)))
    for i,(y,width) in enumerate(((5.7,1.8),(6.1,2.6),(6.5,1.8))):
        pieces.append(cube(f'饰板浅刻槽_{i}',[center-width/2,y,14.87],[center+width/2,y+.12,14.90],(16,0)))
    return pieces

for file in ROOT.glob('*.bbmodel'):
    model=json.loads(file.read_text(encoding='utf8'))
    double='double' in file.name
    which='left' if '_left_' in file.name else 'right' if '_right_' in file.name else None
    for original in ornaments(double):
        for side in ('left','right') if double else ('single',):
            if which and which!=side: continue
            e=copy.deepcopy(original)
            if double:
                lo=max(e['from'][0],0 if side=='left' else 16)
                hi=min(e['to'][0],16 if side=='left' else 32)
                if hi<=lo: continue
                a,b=e['from'][0],e['to'][0]
                e['from'][0],e['to'][0]=lo,hi
                if lo==16 and a<16:e['faces'].pop('west',None)
                if hi==16 and b>16:e['faces'].pop('east',None)
                for name,face in e['faces'].items():
                    if name in ('west','east'):continue
                    u,v,U,V=face['uv'];start,end=(lo-a)/(b-a),(hi-a)/(b-a)
                    if name=='north':start,end=1-end,1-start
                    face['uv']=[u+(U-u)*start,v,u+(U-u)*end,V]
                if which=='right':
                    for key in ('from','to','origin'):e[key][0]-=16
            e['name']+='_'+side
            e['uuid']=str(uuid.uuid5(uuid.NAMESPACE_URL,'lockless-inlay/'+e['name']))
            model['elements'].append(e)
            group=next(g for g in model['outliner'] if g['name']==('body_'+side if double else 'body'))
            group['children'].append(e['uuid'])
    file.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf8')
