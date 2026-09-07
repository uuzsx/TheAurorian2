import json,copy,uuid,base64,ast,math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent;S=R.parent/'aurorian_wood_chest'
b=json.loads((S/'aurorian_wood_chest.bbmodel').read_text(encoding='utf-8'))
atlas=Image.open(S/'aurorian_chest.png').convert('RGB')
wood=atlas.crop((0,0,16,16))
# Dedicated repeating roof strip: keep one texture pixel per model unit along both axes.
atlas.paste(wood,(0,32));atlas.paste(wood,(16,32))
atlas.save(R/'aurorian_chest.png');tex=np.array(atlas)
b['textures'][0]['source']='data:image/png;base64,'+base64.b64encode((R/'aurorian_chest.png').read_bytes()).decode()
source=(R.parent/'aurorian_wood_tools_v2/build_models.py').read_text(encoding='utf-8-sig')
for n in ast.parse(source).body:
 if isinstance(n,ast.FunctionDef) and n.name in ('rot','render'):
  s=ast.get_source_segment(source,n)
  if n.name=='render':s=s.replace('angles=(12,-30,-28))','angles=(12,-30,-28),fit=40,center=(16,7.5,8))').replace('scale=size/30','scale=size/fit').replace('[8,10,8]','center').replace('0,31','0,63')
  exec(s)
UID=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian_double_wood_chest/'+s))
lidids=set(next(g for g in b['outliner']if g['name']=='lid')['children'])
E=[];groups={k:[]for k in ['body_left','lid_left','body_right','lid_right']}
for old in b['elements']:
 e=copy.deepcopy(old);lo=e['from'];hi=e['to'];islock=any(s in e['name']for s in ['锁扣','钥匙孔'])
 if islock:
  lo[0]+=8;hi[0]+=8;e['origin'][0]+=8
 elif lo[0]>=8:
  lo[0]+=16;hi[0]+=16;e['origin'][0]+=16
 elif hi[0]>8:
  hi[0]+=16;e['origin'][0]+=8
 phase='lid'if old['uuid']in lidids else 'body'
 for side in ['left','right']:
  L=max(lo[0],0 if side=='left'else 16);H=min(hi[0],16 if side=='left'else 32)
  if H<=L:continue
  part=copy.deepcopy(e);part['from'][0]=L;part['to'][0]=H;part['name']+='_'+side;part['uuid']=UID(part['name'])
  if L==16 and lo[0]<16:part['faces'].pop('west',None)
  if H==16 and hi[0]>16:part['faces'].pop('east',None)
  # Keep texture density on enlarged wooden panels; crop center lock UV instead.
  for face,f in part['faces'].items():
   if face not in ['north','south','up','down']:continue
   u,v,U,V=e['faces'][face]['uv']
   if islock:
    start=(L-lo[0])/(hi[0]-lo[0]);end=(H-lo[0])/(hi[0]-lo[0])
    if face=='north':start,end=1-end,1-start
    f['uv']=[u+(U-u)*start,v,u+(U-u)*end,V]
   else:
    density=3 if u==16 and v==0 else 1
    f['uv']=[u,v,u+min(16,(H-L)*density),V]
  # Wooden roof faces share a continuous U origin across the center join.
  if phase=='lid' and old['name'] in ['拱盖前段','拱盖后段','拱盖顶板']:
   f=part['faces']['up']
   f['uv']=[L-1.25,32+lo[2]-1.25,H-1.25,32+hi[2]-1.25]
  if phase=='lid' and old['name']=='盖内木板':
   part['faces']['down']['uv']=[L-2,32,H-2,44]
  E.append(part);groups[phase+'_'+side].append(part['uuid'])

def make_model(elements,which=None):
 out=copy.deepcopy(b);out['name']='极光木大宝箱'+(' · '+which if which else '');out['model_identifier']='aurorian_double_wood_chest'+('_'+which if which else '')
 out['elements']=copy.deepcopy(elements);out['outliner']=[];out['animations']=[];animators={}
 for name,ids in groups.items():
  side=name.split('_')[1];phase=name.split('_')[0]
  if which and side!=which:continue
  center=8 if which or side=='left'else 24
  out['outliner'].append({'name':name,'uuid':UID(name),'origin':[center,10,1.6]if phase=='lid'else[center,0,8],'rotation':[0,0,0],'children':ids,'isOpen':True,'visibility':True,'export':True})
  if phase=='lid':
   original=next(iter(b['animations'][0]['animators'].values()))
   a=copy.deepcopy(original);a['name']=name
   for i,k in enumerate(a['keyframes']):k['uuid']=UID(name+'frame'+str(i))
   animators[UID(name)]=a
 out['animations']=[{'uuid':UID('open'+str(which)),'name':'animation.aurorian_double_wood_chest.open','loop':'hold','length':.55,'animators':animators}]
 if which=='right':
  for e in out['elements']:
   for k in ['from','to','origin']:e[k][0]-=16
 return out
full=make_model(E)
(R/'aurorian_double_wood_chest.bbmodel').write_text(json.dumps(full,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
for side in ['left','right']:
 ids=set(groups['body_'+side]+groups['lid_'+side]);out=make_model([e for e in E if e['uuid']in ids],side)
 (R/f'aurorian_double_wood_chest_{side}.bbmodel').write_text(json.dumps(out,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
facespec={'north':([1,0,3,2],[0,0,-1]),'south':([4,5,6,7],[0,0,1]),'west':([0,4,7,3],[-1,0,0]),'east':([5,1,2,6],[1,0,0]),'up':([7,6,2,3],[0,1,0]),'down':([0,1,5,4],[0,-1,0])}
lidset=set(groups['lid_left']+groups['lid_right'])
def surfaces(angle):
 out=[]
 for e in E:
  x,y,z=e['from'];X,Y,Z=e['to'];origin=e['origin'];a=e['rotation'];vs=[[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]]
  vs=[rot(np.array(v)-origin,a)+origin for v in vs]
  if e['uuid']in lidset:vs=[rot(v-[16,10,1.6],[angle,0,0])+[16,10,1.6]for v in vs]
  for fn,f in e['faces'].items():
   ids,n=facespec[fn];n=rot(n,a)
   if e['uuid']in lidset:n=rot(n,[angle,0,0])
   u,v,U,V=f['uv'];out.append({'v':[vs[i].tolist()for i in ids],'n':n.tolist(),'uv':[[u,V],[U,V],[U,v],[u,v]]})
 return {'surfaces':out}
closed=surfaces(0);opened=surfaces(-100)
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1800,1400),'#111a24');d=ImageDraw.Draw(sheet)
d.text((55,30),'极光木大宝箱 · 双箱合并版',font=font(44),fill='#ecf4ff')
d.text((58,100),'双格宽度 / 连通箱体 / 连续箱盖 / 中央铁锁 / 左右拆分模型',font=font(24),fill='#9fb6c9')
d.rounded_rectangle([35,160,1765,830],24,fill='#1e2a38',outline='#34495e',width=2)
im=render(closed,1150,(22,-25,0),fit=40);im=im.crop((0,260,1150,900));sheet.paste(im,(310,175),im)
d.text((65,775),'闭合 · 中间不设隔板和重复锁扣',font=font(24),fill='#c3d6e9')
for x,m,ang,label in [(35,opened,(14,-25,0),'开盖 · 一个连通的大空间'),(915,closed,(18,145,0),'背面 · 两侧把手与铰链')]:
 d.rounded_rectangle([x,850,x+850,1360],24,fill='#1e2a38',outline='#34495e',width=2)
 im=render(m,780,ang,fit=42,center=(16,11,8)if m is opened else(16,7.5,8));im=im.crop((0,120,780,610));sheet.paste(im,(x+35,865),im)
 d.text((x+30,1310),label,font=font(22),fill='#c3d6e9')
sheet.save(R/'overview.png')
# Verify two local halves reconstruct the combined geometry exactly.
left=json.loads((R/'aurorian_double_wood_chest_left.bbmodel').read_text(encoding='utf-8'))
right=json.loads((R/'aurorian_double_wood_chest_right.bbmodel').read_text(encoding='utf-8'))
for e in right['elements']:
 for k in ['from','to','origin']:e[k][0]+=16
assert {e['uuid']:e for e in left['elements']+right['elements']}=={e['uuid']:e for e in E}
assert len(E)==len({e['uuid']for e in E})
assert len(full['animations'][0]['animators'])==2
for e in E:
 assert all(b>a for a,b in zip(e['from'],e['to']))
 for f in e['faces'].values():
  u,v,U,V=f['uv'];assert 0<=u<U<=64 and 0<=v<V<=64
print(len(E),'cubes; both local halves reconstruct whole; UVs and synchronized lid animation verified')
