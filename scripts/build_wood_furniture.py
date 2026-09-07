"""Two-cell tables and backed chairs sharing the approved stool grain/palettes."""
import base64, copy, importlib.util, json, uuid
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/theaurorian2';D=ROOT/'src/main/resources/data'
OUT=ROOT/'exports/wood_furniture';OUT.mkdir(parents=True,exist_ok=True)
written=[]
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8');written.append(str(path))
WOODS=[('silent_wood','谧木','Silent Wood','silent_tree_planks'),('weeping_willow','垂柳木','Weeping Willow','weeping_willow_planks'),('curtain_wood','幽帘木','Curtain Wood','curtain_tree_planks'),('cursed_frost_wood','咒霜木','Cursed Frost Wood','cursed_frost_tree_planks'),('filthy_wood','污秽木','Filthy Wood','filthy_tree_planks')]
def cube(es,name,lo,hi,tile=0):
 sizes=[b-a for a,b in zip(lo,hi)];faces={}
 for face in ('north','south','east','west','up','down'):
  w=sizes[2] if face in ('east','west') else sizes[0];h=sizes[2] if face in ('up','down') else sizes[1]
  assert max(w,h)<=16
  faces[face]={'uv':[tile*16,0,tile*16+w,h],'texture':0}
 es.append({'name':name,'from':lo,'to':hi,'origin':[8,8,8],'rotation':[0,0,0],'faces':faces,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/furniture/'+name)),'type':'cube','box_uv':False})

chair=copy.deepcopy(json.loads((ROOT/'exports/wood_stools/silent_wood_stool/silent_wood_stool.bbmodel').read_text(encoding='utf8'))['elements'])
for x in (1,13):cube(chair,f'靠背立柱{x}',[x,12,13],[x+2,24,15],1)
cube(chair,'靠背上横梁',[3,22,13],[13,24,15],1)
cube(chair,'靠背下横梁',[3,12,13],[13,14,15],1)
for x in (4,7,10):cube(chair,f'靠背木板{x}',[x,14,13.5],[x+2,22,14.5],0)
table=[]
for i in range(2):
 x=i*16
 cube(table,f'桌面底托{i}',[x,15,0],[x+16,16.1,16],1)
 for z in (0,15):cube(table,f'桌面长边{i}_{z}',[x,16.1,z],[x+16,18,z+1],1)
 lo=1 if i==0 else 16;hi=16 if i==0 else 31
 cube(table,f'桌面板底{i}',[lo,16.1,1],[hi,17.72,15],0)
 for j in range(4):
  z=1+j*3.5;cube(table,f'连续桌板{i}_{j}',[lo,17.72,z],[hi,18,z+3.4],0)
 for z in (1,13):cube(table,f'桌下长撑{i}_{z}',[x,12.5,z-.01 if z==1 else z],[x+16,15,z+2.01 if z==13 else z+2],1)
for x in (0,31):cube(table,f'桌面短边{x}',[x,16.1,1],[x+1,18,15],1)
for x in (1,28):
 for z in (1,12):
  cube(table,f'桌腿{x}_{z}',[x,0,z],[x+3,15,z+3],0)
  cube(table,f'桌脚套{x}_{z}',[x-.002,0,z-.002],[x+3.002,1.2,z+3.002],1)
 cube(table,f'桌下短撑{x}',[x,12.5,3],[x+2,15,13],1)

def section(es,axis,second):
 result=[];low=16 if second else 0;high=32 if second else 16
 for source in es:
  a=source['from'][axis];b=source['to'][axis]
  if b<=low or a>=high:continue
  e=copy.deepcopy(source);l=max(a,low);h=min(b,high);t=(l-a)/(b-a);T=(h-a)/(b-a)
  e['from'][axis]=l;e['to'][axis]=h
  for face,f in e['faces'].items():
   u,v,U,V=f['uv']
   if axis==0 and face in ('north','south','up','down'):
    p,q=(1-T,1-t) if face=='north' else (t,T);f['uv']=[u+(U-u)*p,v,u+(U-u)*q,V]
   if axis==1 and face in ('north','south','east','west'):f['uv']=[u,v+(V-v)*(1-T),U,v+(V-v)*(1-t)]
  if l==16:e['faces'].pop('west' if axis==0 else 'down',None)
  if h==16:e['faces'].pop('east' if axis==0 else 'up',None)
  for k in ('from','to','origin'):e[k][axis]-=low
  result.append(e)
 return result

def block_model(es,texture,plank,shift=(0,0,0)):
 result={'parent':'minecraft:block/block','textures':{'particle':f'theaurorian2:block/{plank}','furniture':texture},'elements':[]}
 for e in es:
  result['elements'].append({'from':[v+d for v,d in zip(e['from'],shift)],'to':[v+d for v,d in zip(e['to'],shift)],'faces':{n:{'uv':[v/4 for v in f['uv']],'texture':'#furniture'} for n,f in e['faces'].items()}})
 return result

spec=importlib.util.spec_from_file_location('r',ROOT/'scripts/export_wood_chests.py');r=importlib.util.module_from_spec(spec);spec.loader.exec_module(r)
sheet=Image.new('RGB',(1550,1740),'#111a24');draw=ImageDraw.Draw(sheet);font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
draw.text((30,20),'五种木材 · 双格长桌与靠背椅',font=font(34),fill='#e6eef6')
draw.text((32,76),'统一木纹与配色体系 / 长桌 2×1 格，高 1⅛ 格 / 椅子坐面 ¾ 格高，右键乘坐',font=font(21),fill='#a2b9c7')
zh=json.loads((A/'lang/zh_cn.json').read_text(encoding='utf8'));en=json.loads((A/'lang/en_us.json').read_text(encoding='utf8'))
for row,(key,label,english,plank) in enumerate(WOODS):
 folder=OUT/key;folder.mkdir(exist_ok=True);texture=f'theaurorian2:block/{key}_stool'
 texpath=A/f'textures/block/{key}_stool.png';atlas=Image.open(texpath).convert('RGB');atlas.save(folder/'furniture.png')
 y=120+row*320;draw.rounded_rectangle((20,y,1530,y+300),16,fill='#1c2835');draw.text((40,y+20),label,font=font(28),fill='#e6eef6')
 sheet.paste(atlas.crop((0,0,16,16)).resize((70,70),Image.Resampling.NEAREST),(60,y+85))
 for kind,es,axis in [('long_table',table,0),('chair',chair,1)]:
  name=key+'_'+kind;model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':label+('长桌' if kind=='long_table' else '靠背椅'),'resolution':{'width':64,'height':64},'elements':es,
   'outliner':[{'name':'body','origin':[8,0,8],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,name+'/body')),'children':[e['uuid'] for e in es]}],
   'textures':[{'name':'furniture.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode(texpath.read_bytes()).decode()}]}
  write(folder/(name+'.bbmodel'),model)
  for second in (False,True):write(A/f'models/block/{name}_{str(second).lower()}.json',block_model(section(es,axis,second),texture,plank))
  variants={}
  for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
   for second in ('false','true'):variants[f'facing={facing},second={second}']={'model':f'theaurorian2:block/{name}_{second}','y':angle}
  write(A/f'blockstates/{name}.json',{'variants':variants})
  item=block_model(es,texture,plank,(-8,0,0) if kind=='long_table' else (0,-4,0))
  item['display']={'gui':{'rotation':[30,225,0],'scale':[.4,.4,.4]},'ground':{'translation':[0,4,0],'scale':[.2,.2,.2]},'fixed':{'rotation':[0,180,0],'scale':[.4,.4,.4]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.3,.3,.3]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[.35,.35,.35]}}
  write(A/f'models/item/{name}.json',item);write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{name}'}})
  write(D/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':f'theaurorian2:{name}','properties':{'second':'false'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}'}]}]})
  write(D/f'theaurorian2/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['PPP','SSS','P P'] if kind=='long_table' else ['P  ','PPP','S S'],'key':{'P':f'theaurorian2:{plank}','S':'#c:rods/wooden'},'result':{'id':f'theaurorian2:{name}','count':1}})
  zh['block.theaurorian2.'+name]=label+('长桌' if kind=='long_table' else '靠背椅');en['block.theaurorian2.'+name]=english+(' Long Table' if kind=='long_table' else ' Chair')
  # Chair's canonical front is north; show that front in the software preview.
  preview=copy.deepcopy(model)
  if kind=='chair':
   for e in preview['elements']:
    for coord in (0,2):e['from'][coord],e['to'][coord]=16-e['to'][coord],16-e['from'][coord]
    swapped={}
    for n,f in e['faces'].items():
     n={'north':'south','south':'north','east':'west','west':'east'}.get(n,n)
     if n in ('up','down'):u,v,U,V=f['uv'];f['uv']=[U,V,u,v]
     swapped[n]=f
    e['faces']=swapped
  im=r.render(preview,atlas,820 if kind=='long_table' else 420,275);sheet.paste(im,(200 if kind=='long_table' else 1060,y+12),im)
write(A/'lang/zh_cn.json',zh);write(A/'lang/en_us.json',en)
tag=D/'minecraft/tags/block/mineable/axe.json';data=json.loads(tag.read_text(encoding='utf8'))
for key,*_ in WOODS:
 for kind in ('long_table','chair'):
  name='theaurorian2:'+key+'_'+kind
  if name not in data['values']:data['values'].append(name)
write(tag,data);sheet.save(OUT/'overview.png')
(OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
(OUT/'README.txt').write_text('五种木材的长桌与靠背椅，共用矮凳的模型纹理资源，木纹一致，仅木材配色不同。\n长桌：32×18×16，放置在点击格和玩家右手边的一格；椅子：16×24×16，坐面高12，靠背占用上方格的一部分。\n拆任意部分整体移除，生存掉一件家具，创造不掉落。椅子右键坐下，Shift起身。\n',encoding='utf8')
print('Five tables and five chairs generated; per-cell models preserve UVs; shared stool textures.')
