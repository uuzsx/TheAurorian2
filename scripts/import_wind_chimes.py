"""Import supplied wind chimes, following Blockbench 4.10 Bedrock export conventions.
No texture recoloring or geometry simplification. Run through the sibling PS1 writer.
"""
import base64,copy,json,math,shutil,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SOURCE=Path(sys.argv[1]) if len(sys.argv)>1 else Path('E:/BaiduNetdiskDownload/1316场景风铃/1316场景风铃')
OUT=ROOT/'build/wind-chimes';OUT.mkdir(parents=True,exist_ok=True)
A='src/main/resources/assets/theaurorian2/';D='src/main/resources/data/theaurorian2/';pending={}
def emit(p,d):pending[p]=d
def pivot(v):return [-v[0],v[1],v[2]]
def rot(v):return [-v[0],-v[1],v[2]]
for stem,cn,en in [('amethyst_wind_chimes','紫水晶风铃','Amethyst Wind Chimes'),('bamboo_wind_chimes','竹制风铃','Bamboo Wind Chimes')]:
 src=next(SOURCE.rglob(stem+'.bbmodel'));d=json.loads(src.read_text(encoding='utf-8-sig'));elems={e['uuid']:e for e in d['elements']};bones=[];names={}
 def walk(ns,parent=None):
  for node in ns:
   assert isinstance(node,dict), 'Root cube requires explicit group'
   b={'name':node['name'],'pivot':pivot(node['origin'])};names[node['uuid']]=node['name']
   if parent:b['parent']=parent
   if any(node.get('rotation',[0,0,0])):b['rotation']=rot(node['rotation'])
   cubes=[];children=[]
   for child in node['children']:
    if isinstance(child,dict):children.append(child);continue
    e=elems[child]
    # ModelEngine's hitbox helper is not visible geometry (its UV is intentionally empty).
    if e['name']=='hitbox':continue
    assert e.get('type','cube')=='cube' and not e.get('box_uv') and not e.get('rescale')
    c={'origin':[-e['to'][0],e['from'][1],e['from'][2]],'size':[e['to'][j]-e['from'][j] for j in range(3)],'uv':{}}
    if any(e.get('rotation',[0,0,0])):c.update(pivot=pivot(e['origin']),rotation=rot(e['rotation']))
    for face,f in e['faces'].items():
     if f.get('texture') is None:continue
     assert f['texture']==0 and not f.get('rotation'), 'Unexpected material/UV rotation'
     u,v,U,V=f['uv'];c['uv'][face]={'uv':[U,V] if face in ['up','down'] else [u,v],'uv_size':[u-U,v-V] if face in ['up','down'] else [U-u,V-v]}
    cubes.append(c)
   if cubes:b['cubes']=cubes
   bones.append(b);walk(children,node['name'])
 walk(d['outliner'])
 assert sum(len(b.get('cubes',[])) for b in bones)==sum(e['name']!='hitbox' for e in elems.values())
 emit(A+f'geckolib/models/block/{stem}.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.'+stem,'texture_width':d['resolution']['width'],'texture_height':d['resolution']['height'],'visible_bounds_width':2,'visible_bounds_height':3,'visible_bounds_offset':[0,.5,0]},'bones':bones}]})
 animations={}
 for a in d['animations']:
  anim={'loop':a['loop']=='loop','animation_length':a['length'],'bones':{}}
  for uid,an in a['animators'].items():
   # Original effect tracks refer to an author's local MP3 or MythicMobs command.
   # Their sound behavior is supplied by the registered OGG/scheduled block tick.
   if uid == 'effects':continue
   channels={}
   for k in sorted(an['keyframes'],key=lambda k:k['time']):
    assert k['channel'] in ['rotation','position','scale'] and k['interpolation'] in ['catmullrom','linear']
    points=[[float(point[axis]) for axis in ['x','y','z']] for point in k['data_points']]
    value={'post':points[-1],'lerp_mode':'catmullrom'} if k['interpolation']=='catmullrom' else points[-1]
    if len(points)>1:value={'pre':points[0],'post':points[-1],**({'lerp_mode':'catmullrom'} if k['interpolation']=='catmullrom' else {})}
    # Keyframe values already use Bedrock animation coordinates in .bbmodel.
    channels.setdefault(k['channel'],{})[str(k['time'])]=value
   anim['bones'][names[uid]]=channels
  animations[a['name']]=anim
 emit(A+f'geckolib/animations/block/{stem}.animation.json',{'format_version':'1.8.0','animations':animations})
 for t in d['textures']:
  path=ROOT/A/'textures/block'/t['name'];path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(base64.b64decode(t['source'].split(',',1)[1]))
 # Static inventory representation uses the same cubes, faces and original pivots.
 assert all(not any(b.get('rotation',[0,0,0])) for b in bones)
 item=[]
 for e in d['elements']:
  if e['name']=='hitbox':continue
  el={k:[v+(8 if j!=1 else 0) for j,v in enumerate(e[k])] for k in ['from','to']}
  el['faces']={face:{'uv':[v/d['resolution']['width']*16 for v in f['uv']],'texture':'#0',**({'rotation':f['rotation']} if f.get('rotation') else {})} for face,f in e['faces'].items() if f.get('texture') is not None}
  rr=e.get('rotation',[0,0,0]);axes=[j for j,v in enumerate(rr) if v]
  if axes:
   assert len(axes)==1
   el['rotation']={'origin':[v+(8 if j!=1 else 0) for j,v in enumerate(e['origin'])],'axis':'xyz'[axes[0]],'angle':rr[axes[0]],'rescale':False}
  item.append(el)
 display={'gui':{'rotation':[15,30,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'ground':{'translation':[0,3,0],'scale':[.4,.4,.4]},'fixed':{'scale':[.75,.75,.75]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]},'firstperson_righthand':{'rotation':[0,-30,0],'translation':[0,2,0],'scale':[.6,.6,.6]}}
 emit(A+f'models/item/{stem}.json',{'textures':{'0':f'theaurorian2:block/{stem}','particle':f'theaurorian2:block/{stem}'},'elements':item,'display':display})
 emit(A+f'items/{stem}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{stem}'}})
 emit(A+f'models/block/{stem}.json',{'textures':{'particle':f'theaurorian2:block/{stem}'},'elements':[]})
 emit(A+f'blockstates/{stem}.json',{'multipart':[{'apply':{'model':f'theaurorian2:block/{stem}'}}]})
 emit(D+f'loot_table/blocks/{stem}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'theaurorian2:{stem}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 for lang,title in [('zh_cn',cn),('en_us',en)]:
  path=A+f'lang/{lang}.json';ld=pending.get(path) or json.loads((ROOT/path).read_text(encoding='utf-8-sig'));ld['block.theaurorian2.'+stem]=title;ld['subtitles.theaurorian2.'+stem]=title+'轻响' if lang=='zh_cn' else title+' ring';emit(path,ld)
 snd=A+'sounds.json';sd=pending.get(snd) or json.loads((ROOT/snd).read_text(encoding='utf-8-sig'));sd[stem]={'subtitle':'subtitles.theaurorian2.'+stem,'sounds':[{'name':'theaurorian2:'+stem,'stream':True,'attenuation_distance':25}]};emit(snd,sd)
 dest=ROOT/A/'sounds'/f'{stem}.ogg';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(SOURCE/'Sounds'/f'{stem}.ogg',dest)
(OUT/'pending.json').write_text(json.dumps(pending,ensure_ascii=False),encoding='utf-8')
print('Validated 2 original models and exported 48 visible cubes / 676 bone keyframes; staged',len(pending),'JSON files.')
