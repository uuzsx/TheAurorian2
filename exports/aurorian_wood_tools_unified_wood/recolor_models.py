import json,base64,uuid,copy,ast,math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent
BASE=Path('C:/Users/84446/Downloads/adventurer_tools_v8/Oraxen/pack')
NAMES={'pickaxe':'极光木镐','axe':'极光木斧','hoe':'极光木锄','shovel':'极光木铲'}
source=(R.parent/'aurorian_wood_tools_v2/build_models.py').read_text(encoding='utf-8-sig'); tree=ast.parse(source)
for n in tree.body:
 if isinstance(n,ast.FunctionDef) and n.name in ('rot','render'):
  s=ast.get_source_segment(source,n)
  if n.name=='render':
   s=s.replace('scale=size/30','scale=size/36').replace('[8,10,8]','[8,4.5,8]').replace('0,31','0,63').replace('tex[V,U]*shade','tex[V,U,:3]*shade').replace('rgb=np.clip','mask &= tex[V,U,3]>127\n  rgb=np.clip')
  exec(s)
facespec={'north':([1,0,3,2],[0,0,-1]),'south':([4,5,6,7],[0,0,1]),'west':([0,4,7,3],[-1,0,0]),'east':([5,1,2,6],[1,0,0]),'up':([3,7,6,2],[0,1,0]),'down':([0,1,5,4],[0,-1,0])}
def ramp(t,stops):
 t=max(0,min(1,t)); i=min(len(stops)-2,int(t*(len(stops)-1))); f=t*(len(stops)-1)-i
 return tuple(round(a*(1-f)+b*f)for a,b in zip(stops[i],stops[i+1]))
models={};textures={};originals={};checks=[]
for key,name in NAMES.items():
 modelpath=BASE/f'models/akaleaf/adventurer_tools/adventurer_{key}.json'
 src=json.loads(modelpath.read_text()); old=Image.open(BASE/f'textures/akaleaf/adventurer_tools/adventurer_{key}.png').convert('RGBA'); new=old.copy()
 assert old.size==(64,64)
 handle=set()
 for e in src['elements']:
  if e['to'][1]-e['from'][1]>15:
   for f in e['faces'].values():
    u,v,U,V=[q*4 for q in f['uv']]
    handle.update((x,y)for x in range(math.floor(min(u,U)),math.ceil(max(u,U))) for y in range(math.floor(min(v,V)),math.ceil(max(v,V))))
 # Match both wood surfaces to the same palette from the original Aurorian sprite.
 sprite=Image.open(R.parents[1]/f'src/main/resources/assets/theaurorian2/textures/item/silent_wood_{key}.png').convert('RGBA')
 woodcolors=[tuple(p[:3]) for p in sprite.getdata() if p[3] and not (p[2]-p[0]>45)]
 woodcolors.sort(key=lambda c:.2126*c[0]+.7152*c[1]+.0722*c[2])
 populations={True:[],False:[]}
 for sy in range(64):
  for sx in range(64):
   cr,cg,cb,ca=old.getpixel((sx,sy))
   if ca and ((sx,sy) not in handle or (cr>cb*1.2 and cr>cg*1.1)):
    populations[cr>cb*1.2 and cr>cg*1.1].append(.2126*cr+.7152*cg+.0722*cb)
 for a in populations: populations[a].sort()
 for y in range(64):
  for x in range(64):
   r,g,b,a=old.getpixel((x,y))
   if a==0:continue
   lum=.2126*r+.7152*g+.0722*b
   warm=r>b*1.2 and r>g*1.1
   if not warm and (x,y) in handle:
    rgb=ramp((lum-65)/190,[(33,44,109),(35,116,180),(78,194,222)])
   else:
    values=populations[warm]
    q=(sum(v<lum for v in values)+.5*sum(v==lum for v in values))/len(values)
    rgb=woodcolors[min(len(woodcolors)-1,int((.08+.84*q)*len(woodcolors)))]
   new.putpixel((x,y),(*rgb,a))
 assert np.array_equal(np.array(old)[:,:,3],np.array(new)[:,:,3])
 filename=f'silent_wood_{key}_recolor.png';new.save(R/filename)
 # Preview-only game-format JSON: preserve every geometry/UV/display/group field.
 exported=copy.deepcopy(src)
 exported['textures']={k:('theaurorian2:item/silent_wood_'+key+'_recolor' if not v.startswith('#') else v)for k,v in src['textures'].items()}
 (R/f'silent_wood_{key}.json').write_text(json.dumps(exported,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
 assert exported['elements']==src['elements'] and exported.get('display')==src.get('display')
 elems=[];surfs=[];ids=[]
 for i,e in enumerate(src['elements']):
  uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian-reference-recolor/'+key+'/'+str(i)));ids.append(uid)
  rotation=e.get('rotation',{});angles=[0,0,0];angles['xyz'.index(rotation.get('axis','y'))]=rotation.get('angle',0);pivot=rotation.get('origin',[8,8,8])
  ef={}
  for fn,f in e['faces'].items():
   ef[fn]={**f,'uv':[q*4 for q in f['uv']],'texture':0}
  elem={'name':('握柄' if e['to'][1]-e['from'][1]>15 else '原模型部件 '+str(i+1)),'uuid':uid,'type':'cube','from':e['from'],'to':e['to'],'origin':pivot,'rotation':angles,'rescale':rotation.get('rescale',False),'box_uv':False,'autouv':0,'faces':ef}
  for flag in ('shade','light_emission'):
   if flag in e:elem[flag]=e[flag]
  elems.append(elem)
  x,y,z=e['from'];X,Y,Z=e['to'];verts=[[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]]
  verts=[(rot(np.array(v)-pivot,angles)+pivot).tolist()for v in verts]
  for fn,f in ef.items():
   indexes,n=facespec[fn];u,v,U,V=f['uv'];uv=[[u,V],[U,V],[U,v],[u,v]]
   turn=f.get('rotation',0)//90
   if turn:uv=uv[-turn:]+uv[:-turn]
   surfs.append({'v':[verts[j]for j in indexes],'n':rot(n,angles).tolist(),'uv':uv})
 bb={'meta':{'format_version':'4.10','model_format':'java_block','box_uv':False},'name':name+' · 原模型改色','resolution':{'width':64,'height':64},'elements':elems,'outliner':[{'name':name,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,key+'/group')),'origin':[8,8,8],'children':ids,'isOpen':True,'visibility':True,'export':True}],'textures':[{'name':filename,'id':'0','uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,key+'/texture')),'width':64,'height':64,'uv_width':64,'uv_height':64,'particle':True,'source':'data:image/png;base64,'+base64.b64encode((R/filename).read_bytes()).decode()}],'display':copy.deepcopy(src.get('display',{}))}
 (R/f'silent_wood_{key}.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
 for i,e in enumerate(elems):
  assert e['from']==src['elements'][i]['from'] and e['to']==src['elements'][i]['to']
  for fn,f in e['faces'].items():assert [v/4 for v in f['uv']]==src['elements'][i]['faces'][fn]['uv']
 models[key]={'name':name,'surfaces':surfs,'count':len(elems)};textures[key]=np.array(new);originals[key]=np.array(old)
 checks.append(f'{key}: {len(elems)} elements; geometry, UV, transforms, alpha unchanged')
(R/'geometry.json').write_text(json.dumps(models,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1900,1540),'#111a24');d=ImageDraw.Draw(sheet)
d.text((60,32),'极光木制工具 · 统一木色版',font=font(44),fill='#ecf4ff')
d.text((62,102),'直接使用 Adventurer Tools 模型 · 保留造型和 UV · 工具头与握柄共用原贴图木色 · 未接入游戏',font=font(22),fill='#9fb6c9')
for i,(key,m) in enumerate(models.items()):
 x=45+(i%2)*935;y=160+(i//2)*680
 d.rounded_rectangle([x,y,x+895,y+650],22,fill='#1d2a38',outline='#364b60',width=2)
 d.text((x+28,y+22),m['name'],font=font(31),fill='#edf4ff')
 tex=textures[key];hero=render(m,650,(8,-25,-28));sheet.paste(hero,(x-10,y+38),hero);hero.save(R/f'{key}_preview.png')
 side=render(m,275,(8,-67,-10));sheet.paste(side,(x+604,y+280),side)
 d.text((x+665,y+535),'改色后侧面',font=font(18),fill='#98afc4')
 tex=originals[key];ref=render(m,230,(8,-25,-28));sheet.paste(ref,(x+615,y+50),ref)
 d.text((x+675,y+255),'原配色',font=font(18),fill='#98afc4')
sheet.save(R/'overview.png')
(R/'validation.txt').write_text('\n'.join(checks),encoding='utf-8');print('\n'.join(checks))
