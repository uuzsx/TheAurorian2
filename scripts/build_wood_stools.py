"""Original stools with a one-block footprint and three-quarter-block height."""
import base64, copy, importlib.util, json, uuid
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/theaurorian2'
DATA=ROOT/'src/main/resources/data'
OUT=ROOT/'exports/wood_stools';OUT.mkdir(parents=True,exist_ok=True)
written=[]
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(value,ensure_ascii=False,separators=(',',':')),encoding='utf8')
 written.append(str(path))
WOODS=[('silent_wood','谧木','Silent Wood','silent_tree_planks'),('weeping_willow','垂柳木','Weeping Willow','weeping_willow_planks'),('curtain_wood','幽帘木','Curtain Wood','curtain_tree_planks'),('cursed_frost_wood','咒霜木','Cursed Frost Wood','cursed_frost_tree_planks'),('filthy_wood','污秽木','Filthy Wood','filthy_tree_planks')]
elements=[]
def cube(name,lo,hi,tile=0):
 w,h,d=[b-a for a,b in zip(lo,hi)];faces={}
 for face in ('north','south','east','west','up','down'):
  width=d if face in ('east','west') else w
  height=d if face in ('up','down') else h
  # Original pixels, without filtering or procedural noise.
  faces[face]={'uv':[tile*16,0,tile*16+width,height],'texture':0}
 elements.append({'name':name,'from':lo,'to':hi,'origin':[8,8,8],'rotation':[0,0,0],'faces':faces,
  'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/stool/'+name)),'type':'cube','box_uv':False})

# Dimensions match AurorianStoolBlock's stable, cached collision shape.
cube('坐面底托',[0,9,0],[16,10.1,16],1)
for z in (0,15):cube(f'坐面前后包边{z}',[0,10.1,z],[16,12,z+1],1)
for x in (0,15):cube(f'坐面左右包边{x}',[x,10.1,1],[x+1,12,15],1)
cube('坐面板底',[1,10.1,1],[15,11.72,15],0)
for i in range(4):
 x=1+i*3.5
 cube('坐面木板'+str(i),[x,11.72,1],[x+3.4,12,15],0)
for x in (1,12):
 for z in (1,12):
  cube(f'凳腿{x}_{z}',[x,0,z],[x+3,9,z+3],0)
  # Fitted end collars keep the exact same footprint as each leg.
  cube(f'脚套{x}_{z}',[x-.002,0,z-.002],[x+3.002,1.2,z+3.002],1)
  cube(f'腿榫{x}_{z}',[x-.002,7.4,z-.002],[x+3.002,9,z+3.002],1)
for z in (1.5,12.5):cube(f'前后横撑{z}',[4,3,z],[12,5,z+2],1)
for x in (1.5,12.5):cube(f'左右横撑{x}',[x,3,4],[x+2,5,12],1)

spec=importlib.util.spec_from_file_location('render_stool',ROOT/'scripts/export_wood_chests.py');r=importlib.util.module_from_spec(spec);spec.loader.exec_module(r)
sheet=Image.new('RGB',(1550,700),'#111a24');draw=ImageDraw.Draw(sheet)
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
draw.text((30,22),'五种木材 · 方木凳',font=font(34),fill='#e6eef6')
draw.text((32,76),'统一模型与木纹 / 一格宽 × ¾ 格高 / 右键坐下，Shift 起身',font=font(21),fill='#a2b9c7')
zh=json.loads((ASSETS/'lang/zh_cn.json').read_text(encoding='utf-8-sig'))
en=json.loads((ASSETS/'lang/en_us.json').read_text(encoding='utf-8-sig'))
template=Image.open(ASSETS/'textures/block/silent_tree_planks.png').convert('RGB')
source_colors=sorted(set(template.getdata()),key=sum)
for row,(key,label,english,plank) in enumerate(WOODS):
 name=key+'_stool';folder=OUT/name;folder.mkdir(exist_ok=True)
 plank_image=Image.open(ASSETS/f'textures/block/{plank}.png').convert('RGB')
 colors=sorted(set(plank_image.getdata()),key=sum);assert len(colors)==len(source_colors)==7
 mapping=dict(zip(source_colors,colors));wood=Image.new('RGB',(16,16));wood.putdata([mapping[c] for c in template.getdata()])
 atlas=Image.new('RGB',(64,64),(38,39,43));atlas.paste(wood,(0,0));atlas.paste(wood.point(lambda v:round(v*.64)),(16,0))
 atlas.save(folder/'stool.png');atlas.save(ASSETS/f'textures/block/{name}.png')
 model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':label+'凳子','resolution':{'width':64,'height':64},'elements':copy.deepcopy(elements),
  'outliner':[{'name':'body','origin':[8,0,8],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/stool/body')),'children':[e['uuid'] for e in elements]}],
  'textures':[{'name':'stool.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode((folder/'stool.png').read_bytes()).decode()}]}
 write(folder/(name+'.bbmodel'),model)
 block={'parent':'minecraft:block/block','textures':{'particle':f'theaurorian2:block/{plank}','stool':f'theaurorian2:block/{name}'},'elements':[]}
 for e in elements:
  q={k:e[k] for k in ('from','to')};q['faces']={n:{'uv':[v/4 for v in f['uv']],'texture':'#stool'} for n,f in e['faces'].items()};block['elements'].append(q)
 write(ASSETS/f'models/block/{name}.json',block)
 write(ASSETS/f'blockstates/{name}.json',{'multipart':[{'apply':{'model':f'theaurorian2:block/{name}'}}]})
 write(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:block/{name}'}})
 write(DATA/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}'}]}]})
 write(DATA/f'theaurorian2/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['PPP','S S','S S'],'key':{'P':f'theaurorian2:{plank}','S':'#c:rods/wooden'},'result':{'id':f'theaurorian2:{name}','count':1}})
 zh['block.theaurorian2.'+name]=label+'凳子';en['block.theaurorian2.'+name]=english+' Stool'
 x=10+row*308;im=r.render(model,atlas,305,320);sheet.paste(im,(x,145),im)
 draw.text((x+45,485),label+'凳子',font=font(24),fill='#e6eef6');sheet.paste(wood.resize((64,64),Image.Resampling.NEAREST),(x+110,540))
write(ASSETS/'lang/zh_cn.json',zh);write(ASSETS/'lang/en_us.json',en)
tag=DATA/'minecraft/tags/block/mineable/axe.json';data=json.loads(tag.read_text(encoding='utf-8-sig'))
for key,*_ in WOODS:
 name='theaurorian2:'+key+'_stool'
 if name not in data['values']:data['values'].append(name)
write(tag,data)
sheet.save(OUT/'overview.png')
(OUT/'README.txt').write_text('五种木凳共用同一模型与木纹，仅按各自木板换色。\n尺寸 16×12×16 模型单位；右键坐下，Shift 起身，一张凳子限一位玩家。\n坐骑实体仅在坐下时创建，空置或凳子消失时移除，不独立保存；退出游戏前自动离座。\n配方：顶部三块对应木板，下面两行各在左右放木棍。\n',encoding='utf8')
(OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
print(f'Five stools generated; {len(elements)} cubes; identical indexed grain.')
