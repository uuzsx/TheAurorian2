"""Adapt the supplied Wooden Pack box to the existing animated wood chests.

Uniform block fitting and a 180-degree facing correction preserve source shape.
The solid base is hollowed without changing its exterior. Double-box extensions
repeat the original UVs instead of stretching the texture or corner hardware.
Runtime UVs use the existing 64-unit convention with a 256x128 atlas: wood on the left, shared dark straps on the right.
"""
import base64, copy, importlib.util, inspect, json, math, uuid
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
SOURCE=Path('C:/Users/84446/Downloads/Wooden-Pack-mvaynq/Wooden Pack')
ASSETS=ROOT/'src/main/resources/assets/theaurorian2'
OUT=ROOT/'exports/wooden_pack_chests'; OUT.mkdir(parents=True,exist_ok=True)
source=json.loads((SOURCE/'assets/minecraft/models/custom/elitecreatures/wooden_pack/box_wood.json').read_text())
original=Image.open(SOURCE/'assets/minecraft/textures/custom/elitecreatures/wooden_pack/box_wood.png').convert('RGB')
assert original.size==(128,128) and len(source['elements'])==30
assert all(not f.get('rotation',0) for e in source['elements'] for f in e['faces'].values())
S=14/19.5
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/wooden-pack/'+name))
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8')

def clip(e,axis,low,high):
 """Crop a cuboid and interpolate its per-face UVs, retaining pixel density."""
 q=copy.deepcopy(e); a=e['from'][axis]; b=e['to'][axis]
 low=max(a,low);high=min(b,high)
 assert high>low
 q['from'][axis]=low;q['to'][axis]=high
 for name,f in q['faces'].items():
  u,v,U,V=f['uv'];t=(low-a)/(b-a);T=(high-a)/(b-a)
  if axis==0 and name in ('north','south','up','down'):
   if name=='north':t,T=1-T,1-t
   f['uv']=[u+(U-u)*t,v,u+(U-u)*T,V]
  elif axis==1 and name in ('north','south','east','west'):
   f['uv']=[u,v+(V-v)*(1-T),U,v+(V-v)*(1-t)]
  elif axis==2:
   if name in ('east','west'):
    if name=='east':t,T=1-T,1-t
    f['uv']=[u+(U-u)*t,v,u+(U-u)*T,V]
   elif name in ('up','down'):
    if name=='up':t,T=1-T,1-t
    f['uv']=[u,v+(V-v)*t,U,v+(V-v)*T]
 return q

elements=[]
lid_ids={1,2,13,14,15,16,27,28,29}
for i,e in enumerate(source['elements']):
 assert all(a<b for a,b in zip(e['from'],e['to']))
 def point(p):return [8-(p[0]-8)*S,p[1]*S,8-(p[2]-8)*S]
 a,b=point(e['from']),point(e['to']);rot=e.get('rotation',{})
 q={'name':f'source_{i}','from':[min(x,y) for x,y in zip(a,b)],'to':[max(x,y) for x,y in zip(a,b)],
    'origin':point(rot.get('origin',[8,0,8])),'rotation':[0,0,0],'faces':{},'bone':'lid' if i in lid_ids else 'body'}
 if rot.get('angle',0):q['rotation']['xyz'.index(rot['axis'])]=-rot['angle']
 for face,f in e['faces'].items():
  name={'north':'south','south':'north','east':'west','west':'east'}.get(face,face)
  uv=[v*(2 if axis%2==0 else 4) for axis,v in enumerate(f['uv'])]
  if i>=3:uv[0]+=32;uv[2]+=32
  if face in ('up','down'):uv=[uv[2],uv[3],uv[0],uv[1]]
  q['faces'][name]={'uv':uv,'texture':0}
 if i==1:
  q['faces']['down']['uv']=[0,48,4,56]
 if i==0:
  # Keep every exterior surface, add a bottom and four inner-facing walls.
  x,y,z=q['from'];X,Y,Z=q['to'];t=.8
  pieces=[clip(q,1,y,y+t),clip(q,0,x,x+t),clip(q,0,X-t,X)]
  mid=clip(q,0,x+t,X-t)
  pieces.extend([clip(mid,2,z,z+t),clip(mid,2,Z-t,Z)])
  pieces[0]['faces']['up']['uv']=[0,48,4,56]
  for j,p in enumerate(pieces):p['name']+=f'_shell{j}'
  elements.extend(pieces)
 else:elements.append(q)

def shifted(e,dx):
 q=copy.deepcopy(e)
 for k in ('from','to','origin'):q[k][0]+=dx
 return q

double=[]
for e in elements:
 a,b=e['from'][0],e['to'][0]
 if e['name']=='source_27':double.append(shifted(e,8));continue
 if b<=8:double.append(copy.deepcopy(e));continue
 if a>=8:double.append(shifted(e,16));continue
 # Keep both ends, tile additional width through the middle.
 left=clip(e,0,a,8);left['faces'].pop('east',None);double.append(left)
 right=shifted(clip(e,0,8,b),16);right['faces'].pop('west',None);double.append(right)
 x=8
 while x<24-1e-8:
  width=min(b-a,24-x);part=shifted(clip(e,0,a,a+width),x-a)
  part['faces'].pop('east',None);part['faces'].pop('west',None)
  double.append(part);x+=width

halves={}
for name,low,high,offset in [('left',0,16,0),('right',16,32,-16)]:
 half=[]
 for e in double:
  if e['name']=='source_27' and name=='right':continue
  if e['to'][0]<=low or e['from'][0]>=high:continue
  q=clip(e,0,low,high) if e['rotation']==[0,0,0] else copy.deepcopy(e)
  if q['from'][0]==16:q['faces'].pop('west',None)
  if q['to'][0]==16:q['faces'].pop('east',None)
  half.append(shifted(q,offset))
 halves[name]=half

def model(es,texture,label):
 groups=[{'name':n,'uuid':uid(n),'origin':[8,9.75*S,8-8.25*S] if n=='lid' else [8,0,8],'children':[]} for n in ('body','lid')]
 cubes=[]
 for i,e in enumerate(es):
  q=copy.deepcopy(e);bone=q.pop('bone');q.update(uuid=uid(str(i)),type='cube',box_uv=False)
  groups[bone=='lid']['children'].append(q['uuid']);cubes.append(q)
 return {'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':label,'resolution':{'width':64,'height':64},'elements':cubes,'outliner':groups,
  'textures':[{'name':'chest.png','id':'0','uuid':uid('texture'),'width':256,'height':128,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode(texture).decode()}]}

WOODS=[('aurorian_chest','谧木','silent_tree_planks'),('weeping_willow_chest','垂柳木','weeping_willow_planks'),('curtain_wood_chest','幽帘木','curtain_tree_planks'),('cursed_frost_wood_chest','咒霜木','cursed_frost_tree_planks'),('filthy_wood_chest','污秽木','filthy_tree_planks')]
spec=importlib.util.spec_from_file_location('render_chest',ROOT/'scripts/export_wood_chests.py');renderer=importlib.util.module_from_spec(spec);spec.loader.exec_module(renderer)
sheet=Image.new('RGB',(1500,1670),'#111a24');draw=ImageDraw.Draw(sheet)
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
draw.text((30,20),'五种木箱 · Wooden Pack 模型',font=font(32),fill='#e6eef6')
src_colors=sorted(set(original.getdata()),key=lambda c:sum(c))
for row,(key,label,plank) in enumerate(WOODS):
 wood=Image.open(ASSETS/f'textures/block/{plank}.png').convert('RGB')
 colors=sorted(set(wood.getdata()),key=sum)
 # Monotonic palette transfer preserves all authored pixel locations and grain.
 mapping={c:colors[round(i*(len(colors)-1)/(len(src_colors)-1))] for i,c in enumerate(src_colors)}
 base=Image.new('RGB',original.size);base.putdata([mapping[c] for c in original.getdata()])
 dark_colors=[(42,43,49),(49,50,57),(56,58,64),(64,66,72),(73,75,82),(83,85,92),(95,97,104)]
 dark_map={c:dark_colors[round(i*6/(len(src_colors)-1))] for i,c in enumerate(src_colors)}
 bands=Image.new('RGB',original.size);bands.putdata([dark_map[c] for c in original.getdata()])
 tex=Image.new('RGB',(256,128));tex.paste(base,(0,0));tex.paste(bands,(128,0))
 tex.paste(wood,(0,96))
 folder=OUT/key;folder.mkdir(exist_ok=True);tex.save(folder/'chest.png')
 tex.save(ASSETS/f'textures/entity/chest/{key}.png')
 for variant,es in {'single':elements,'double':double,**halves}.items():
  m=model(es,(folder/'chest.png').read_bytes(),label+'箱子 '+variant)
  write(folder/(variant+'.bbmodel'),m)
  if row==0 and variant!='double':
   data={g['name']:{'origin':g['origin'],'elements':[{k:copy.deepcopy(e[k]) for k in ('from','to','origin','rotation','faces')} for e in m['elements'] if e['uuid'] in g['children']]} for g in m['outliner']}
   for g in data.values():
    for e in g['elements']:e['faces']={n:f['uv'] for n,f in e['faces'].items()}
   write(ASSETS/f'models/chest/{variant}.json',data)
  if variant in ('single','double'):
   im=renderer.render(m,tex,430 if variant=='single' else 710,265)
   sheet.paste(im,(210 if variant=='single' else 710,100+row*310),im)
  if variant=='single':
   item=json.loads((ASSETS/'models/item/aurorian_chest.json').read_text())
   item['elements']=[];item['textures']={'particle':f'theaurorian2:block/{plank}','chest':f'theaurorian2:block/{key}_model'}
   for e in m['elements']:
    q={k:e[k] for k in ('from','to')};q['faces']={n:{'uv':[v/4 for v in f['uv']],'texture':'#chest'} for n,f in e['faces'].items()}
    for axis,angle in zip('xyz',e['rotation']):
     if angle:q['rotation']={'axis':axis,'angle':angle,'origin':e['origin'],'rescale':False}
    item['elements'].append(q)
   write(ASSETS/f'models/item/{key}.json',item)
 y=110+row*310;draw.text((30,y),label+'箱子',font=font(26),fill='#e6eef6');sheet.paste(wood.resize((80,80),Image.Resampling.NEAREST),(50,y+55))
sheet.save(OUT/'overview.png')
# Open views expose both the lid lining and the textured floor.
exec(inspect.getsource(renderer.render).replace('rotation((18,-24,0))','rotation((42,-24,0))'),renderer.__dict__)
opened_sheet=Image.new('RGB',(1300,1550),'#111a24')
for row,(key,label,plank) in enumerate(WOODS):
 folder=OUT/key;tex=Image.open(folder/'chest.png')
 ImageDraw.Draw(opened_sheet).text((24,row*310+8),label+'箱子 · 内部木纹',font=font(23),fill='#e6eef6')
 for variant,x,w in [('single',20,460),('double',500,780)]:
  m=json.loads((folder/(variant+'.bbmodel')).read_text(encoding='utf8'))
  im=renderer.render(m,tex,w,265,True);opened_sheet.paste(im,(x,row*310+40),im)
opened_sheet.save(OUT/'opened.png')
print('Imported five palettes; single, double and split models; source UVs repeated for extended spans.')
