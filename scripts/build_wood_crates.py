"""Original slatted crates, five plank palettes, and temporary test loot.

The PowerShell wrapper validates and compacts all generated JSON with Write-Json.
Models are ordinary baked blocks: no inventory, block entity, tick or packets.
"""
import base64, copy, importlib.util, json, math, uuid
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/theaurorian2'
DATA=ROOT/'src/main/resources/data'
OUT=ROOT/'exports/wood_crates';OUT.mkdir(parents=True,exist_ok=True)
written=[]
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(value,ensure_ascii=False,separators=(',',':')),encoding='utf8')
 written.append(str(path))

WOODS=[('silent_wood','谧木','Silent Wood','silent_tree_planks'),('weeping_willow','垂柳木','Weeping Willow','weeping_willow_planks'),('curtain_wood','幽帘木','Curtain Wood','curtain_tree_planks'),('cursed_frost_wood','咒霜木','Cursed Frost Wood','cursed_frost_tree_planks'),('filthy_wood','污秽木','Filthy Wood','filthy_tree_planks')]
elements=[]
def cube(name,lo,hi,tile=0,axis=None,angle=0):
 w,h,d=[b-a for a,b in zip(lo,hi)];faces={}
 for face in ('north','south','east','west','up','down'):
  u=tile*16;v=0
  width=d if face in ('east','west') else w
  height=d if face in ('up','down') else h
  faces[face]={'uv':[u,v,u+min(width,16),v+min(height,16)],'texture':0}
 e={'name':name,'from':lo,'to':hi,'origin':[(a+b)/2 for a,b in zip(lo,hi)],'rotation':[0,0,0],'faces':faces,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/crate/'+name)),'type':'cube','box_uv':False}
 if axis:e['rotation']['xyz'.index(axis)]=angle
 elements.append(e)

# Recessed dark backing lets thin gaps read clearly without exposing a void.
cube('暗色内衬',[1.7,1,1.7],[14.3,15,14.3],2)
for x in (0,14):
 for z in (0,14):cube(f'木角柱{x}_{z}',[x,0,z],[x+2,16,z+2],1)
for y in (0,14):
 for z in (.3,14.3):cube(f'前后框{y}_{z}',[2,y,z],[14,y+2,z+1.4],1)
 for x in (.3,14.3):cube(f'侧框{y}_{x}',[x,y,2],[x+1.4,y+2,14],1)
for i in range(3):
 y=2.3+i*4
 for z in (.8,14.2):cube(f'前后板条{i}_{z}',[2,y,z],[14,y+3.4,z+1],0)
 for x in (.8,14.2):cube(f'侧板条{i}_{x}',[x,y,2],[x+1,y+3.4,14],0)
for i in range(4):
 z=2.15+i*3
 for y in (.5,14.5):cube(f'顶底板条{i}_{y}',[2,y,z],[14,y+1,z+2.7],0)
# Reach into all four corner posts while keeping rotated bounds within 0..16.
brace_half=(16*math.sqrt(2)-1.3)/2
for sign in (-1,1):
 for z in (.25,15.05):cube(f'交叉加固正背{sign}_{z}',[8-brace_half,7.35,z+(sign+1)*.08],[8+brace_half,8.65,z+.55+(sign+1)*.08],1,'z',sign*45)
 for x in (.25,15.05):cube(f'交叉加固两侧{sign}_{x}',[x+(sign+1)*.08,7.35,8-brace_half],[x+.55+(sign+1)*.08,8.65,8+brace_half],1,'x',sign*45)
for x in (1,15):
 for y in (1,15):
  for z in (.0,15.85):cube(f'方头钉{x}_{y}_{z}',[x-.22,y-.22,z],[x+.22,y+.22,z+.15],3)

spec=importlib.util.spec_from_file_location('render_crate',ROOT/'scripts/export_wood_chests.py');r=importlib.util.module_from_spec(spec);spec.loader.exec_module(r)
sheet=Image.new('RGB',(1550,740),'#111a24');draw=ImageDraw.Draw(sheet)
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
draw.text((30,22),'五种木材 · 板条箱',font=font(34),fill='#e6eef6')
draw.text((32,76),'交叉木条加固 / 打碎掉落 1 个金苹果（临时测试）',font=font(21),fill='#a2b9c7')
zh=json.loads((ASSETS/'lang/zh_cn.json').read_text(encoding='utf-8-sig'))
en=json.loads((ASSETS/'lang/en_us.json').read_text(encoding='utf-8-sig'))
for row,(key,label,english,plank) in enumerate(WOODS):
 name=key+'_crate';folder=OUT/name;folder.mkdir(exist_ok=True)
 wood=Image.open(ASSETS/f'textures/block/{plank}.png').convert('RGB')
 if key=='filthy_wood':
  template=Image.open(ASSETS/'textures/block/silent_tree_planks.png').convert('RGB')
  original_colors=sorted(set(template.getdata()),key=sum)
  filthy_colors=sorted(set(wood.getdata()),key=sum)
  assert len(original_colors)==len(filthy_colors)==7
  mapping=dict(zip(original_colors,filthy_colors))
  wood=Image.new('RGB',template.size);wood.putdata([mapping[p] for p in template.getdata()])
 atlas=Image.new('RGB',(64,64),(38,39,43));atlas.paste(wood,(0,0))
 dark=wood.point(lambda v:round(v*.55));atlas.paste(dark,(16,0))
 inner=wood.point(lambda v:round(v*.26));atlas.paste(inner,(32,0))
 nail=Image.new('RGB',(16,16),(92,95,99));atlas.paste(nail,(48,0))
 atlas.save(folder/'crate.png');atlas.save(ASSETS/f'textures/block/{name}.png')
 model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':label+'板条箱','resolution':{'width':64,'height':64},'elements':copy.deepcopy(elements),
  'outliner':[{'name':'body','origin':[8,0,8],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/crate/body')),'children':[e['uuid'] for e in elements]}],
  'textures':[{'name':'crate.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode((folder/'crate.png').read_bytes()).decode()}]}
 write(folder/(name+'.bbmodel'),model)
 block={'parent':'minecraft:block/block','textures':{'particle':f'theaurorian2:block/{plank}','crate':f'theaurorian2:block/{name}'},'elements':[]}
 for e in elements:
  q={k:e[k] for k in ('from','to')};q['faces']={n:{'uv':[v/4 for v in f['uv']],'texture':'#crate'} for n,f in e['faces'].items()}
  for axis,angle in zip('xyz',e['rotation']):
   if angle:q['rotation']={'origin':e['origin'],'axis':axis,'angle':angle,'rescale':False}
  block['elements'].append(q)
 write(ASSETS/f'models/block/{name}.json',block)
 write(ASSETS/f'blockstates/{name}.json',{'multipart':[{'apply':{'model':f'theaurorian2:block/{name}'}}]})
 write(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:block/{name}'}})
 # Deliberately fixed test loot. Future loot belongs here, not in tick/break code.
 write(DATA/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:golden_apple'}]}]})
 write(DATA/f'theaurorian2/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['PSP','S S','PSP'],'key':{'P':f'theaurorian2:{plank}','S':'#c:rods/wooden'},'result':{'id':f'theaurorian2:{name}','count':1}})
 zh['block.theaurorian2.'+name]=label+'板条箱';en['block.theaurorian2.'+name]=english+' Crate'
 x=10+row*308
 im=r.render(model,atlas,305,320);sheet.paste(im,(x,140),im)
 draw.text((x+25,490),label+'板条箱',font=font(23),fill='#e6eef6')
 sheet.paste(wood.resize((64,64),Image.Resampling.NEAREST),(x+110,550))
write(ASSETS/'lang/zh_cn.json',zh);write(ASSETS/'lang/en_us.json',en)
tag=DATA/'minecraft/tags/block/mineable/axe.json';data=json.loads(tag.read_text(encoding='utf-8-sig'))
for key,*_ in WOODS:
 name='theaurorian2:'+key+'_crate'
 if name not in data['values']:data['values'].append(name)
write(tag,data)
sheet.save(OUT/'overview.png')
(OUT/'README.txt').write_text('五种原创板条箱模型，已注册为普通可破坏方块。\n创造、生存破坏均使用同一临时掉落：1 个普通金苹果，不掉箱子自身。\n配方：四角对应木板，四边木棍，中心留空。\n没有储物界面、方块实体或每刻更新。正式战利品以后替换各方块的 loot_table。\n',encoding='utf8')
(OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
print(f'Five crates generated; {len(elements)} cubes each; fixed golden-apple test loot.')
