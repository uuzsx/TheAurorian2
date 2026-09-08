"""Build the existing backed chairs, then import the approved medieval tables."""
import base64, copy, json, uuid
from pathlib import Path
from PIL import Image
from wood_bark_palette import bark_trim
import import_medieval_wood_furniture as medieval
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

chair=[]
# The backed chair retains its original seat, independently of imported stools.
cube(chair,'坐面底托',[0,9,0],[16,10.1,16],1)
for z in (0,15):cube(chair,f'坐面前后包边{z}',[0,10.1,z],[16,12,z+1],1)
for x in (0,15):cube(chair,f'坐面左右包边{x}',[x,10.1,1],[x+1,12,15],1)
cube(chair,'坐面板底',[1,10.1,1],[15,11.72,15],0)
for i in range(4):
 x=1+i*3.5
 cube(chair,'坐面木板'+str(i),[x,11.72,1],[x+3.4,12,15],0)
for x in (1,12):
 for z in (1,12):
  cube(chair,f'凳腿{x}_{z}',[x,0,z],[x+3,9,z+3],0)
  # Fitted end collars keep the exact same footprint as each leg.
  cube(chair,f'脚套{x}_{z}',[x-.002,0,z-.002],[x+3.002,1.2,z+3.002],1)
  cube(chair,f'腿榫{x}_{z}',[x-.002,7.4,z-.002],[x+3.002,9,z+3.002],1)
for z in (1.5,12.5):cube(chair,f'前后横撑{z}',[4,3,z],[12,5,z+2],1)
for x in (1.5,12.5):cube(chair,f'左右横撑{x}',[x,3,4],[x+2,5,12],1)

for e in chair:e["uuid"]=str(uuid.uuid5(uuid.NAMESPACE_URL,"aurorian/stool/"+e["name"]))
for x in (1,13):cube(chair,f'靠背立柱{x}',[x,12,13],[x+2,24,15],1)
cube(chair,'靠背上横梁',[3,22,13],[13,24,15],1)
cube(chair,'靠背下横梁',[3,12,13],[13,14,15],1)
for x in (4,7,10):cube(chair,f'靠背木板{x}',[x,14,13.5],[x+2,22,14.5],0)
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

zh=json.loads((A/'lang/zh_cn.json').read_text(encoding='utf8'));en=json.loads((A/'lang/en_us.json').read_text(encoding='utf8'))
for row,(key,label,english,plank) in enumerate(WOODS):
 folder=OUT/key;folder.mkdir(exist_ok=True);texture=f'theaurorian2:block/{key}_stool'
 texpath=A/f'textures/block/{key}_stool.png'
 template=Image.open(A/'textures/block/silent_tree_planks.png').convert('RGB')
 wood_source=Image.open(A/f'textures/block/{plank}.png').convert('RGB')
 mapping=dict(zip(sorted(set(template.getdata()),key=sum),sorted(set(wood_source.getdata()),key=sum)))
 wood=Image.new('RGB',(16,16));wood.putdata([mapping[c] for c in template.getdata()])
 atlas=Image.new('RGB',(64,64),(38,39,43));atlas.paste(wood,(0,0));atlas.paste(bark_trim(wood,plank),(16,0))
 atlas.save(texpath);atlas.save(folder/'furniture.png')
 for kind,es,axis in [('chair',chair,1)]:
  name=key+'_'+kind;model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':label+'靠背椅','resolution':{'width':64,'height':64},'elements':es,
   'outliner':[{'name':'body','origin':[8,0,8],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,name+'/body')),'children':[e['uuid'] for e in es]}],
   'textures':[{'name':'furniture.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode(texpath.read_bytes()).decode()}]}
  write(folder/(name+'.bbmodel'),model)
  for second in (False,True):write(A/f'models/block/{name}_{str(second).lower()}.json',block_model(section(es,axis,second),texture,plank))
  variants={}
  for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
   for second in ('false','true'):variants[f'facing={facing},second={second}']={'model':f'theaurorian2:block/{name}_{second}','y':angle}
  write(A/f'blockstates/{name}.json',{'variants':variants})
  item=block_model(es,texture,plank,(0,-4,0))
  item['display']={'gui':{'rotation':[30,225,0],'scale':[.4,.4,.4]},'ground':{'translation':[0,4,0],'scale':[.2,.2,.2]},'fixed':{'rotation':[0,180,0],'scale':[.4,.4,.4]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.3,.3,.3]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[.35,.35,.35]}}
  write(A/f'models/item/{name}.json',item);write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{name}'}})
  write(D/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':f'theaurorian2:{name}','properties':{'second':'false'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}'}]}]})
  write(D/f'theaurorian2/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['P  ','PPP','S S'],'key':{'P':f'theaurorian2:{plank}','S':'#c:rods/wooden'},'result':{'id':f'theaurorian2:{name}','count':1}})
  zh['block.theaurorian2.'+name]=label+'靠背椅';en['block.theaurorian2.'+name]=english+' Chair'
write(A/'lang/zh_cn.json',zh);write(A/'lang/en_us.json',en)
tag=D/'minecraft/tags/block/mineable/axe.json';data=json.loads(tag.read_text(encoding='utf8'))
for key,*_ in WOODS:
 for kind in ('chair',):
  name='theaurorian2:'+key+'_'+kind
  if name not in data['values']:data['values'].append(name)
write(tag,data)
written.extend(medieval.main(('long_table',)))
(OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
(OUT/'README.txt').write_text('五种木材的长桌采用 ShizuArt 中世纪 table 模型，原木纹按对应木板采色，32×16×16，高1格，放置在点击格与玩家右侧一格。\n靠背椅维持原模型、木板与树皮材质，坐面高12，右键乘坐。\n长桌当前使用 medieval_furniture.png；furniture.png 仍用于靠背椅。\n重新生成：scripts/build_wood_furniture.ps1；只重新导入木凳和长桌：scripts/import_medieval_wood_furniture.ps1。\n',encoding='utf8')
print('Backed chairs preserved; medieval long tables imported.')
