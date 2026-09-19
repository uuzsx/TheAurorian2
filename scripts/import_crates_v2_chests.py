"""Import Crates Pack v2 level 1; retain authored pixel layout and source geometry.

Fit uniformly into one block and face south like the existing chest renderer.
Only single-chest geometry is imported.
Run import_crates_v2_chests.ps1 to validate and compact generated JSON.
"""
import ast,base64,copy,io,json,math
from pathlib import Path
import numpy as np
from furniture_palette import chest_palette
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
SRC=Path('E:/BaiduNetdiskDownload/1407箱子包第二卷/Crates Pack v2/Blockbench files/crate_lvl1.bbmodel')
OUT=ROOT/'exports/crates_v2_wood_chests';OUT.mkdir(parents=True,exist_ok=True)
ASSETS=ROOT/'src/main/resources/assets/theaurorian2'
m=json.loads(SRC.read_text(encoding='utf-8-sig'))
original=Image.open(io.BytesIO(base64.b64decode(m['textures'][0]['source'].split(',')[1]))).convert('RGBA')
assert original.size==(128,128)
for path,names in [('scripts/export_wood_chests.py',('rotation','render'))]:
 tree=ast.parse((ROOT/path).read_text(encoding='utf-8-sig'))
 nodes=[n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name in names or isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='FACES' for t in n.targets)]
 exec(compile(ast.Module(body=nodes,type_ignores=[]),path,'exec'))
def surfaces(model, opened=False):
    elements = {e['uuid']: e for e in model['elements']}

    def walk(entries, parent):
        for node in entries:
            if isinstance(node, str):
                e = elements[node]
                if e.get('visibility', True) == False:
                    continue
                x, y, z = e['from']
                X, Y, Z = e['to']
                q = np.array(e.get('origin', [0, 0, 0]))
                R = rotation(e.get('rotation', [0, 0, 0]))
                p = np.array([[x, y, z], [X, y, z], [X, Y, z], [x, Y, z], [x, y, Z], [X, y, Z], [X, Y, Z], [x, Y, Z]], float)
                p = (p - q) @ R.T + q
                p = (np.c_[p, np.ones(8)] @ parent.T)[:, :3]
                for name, f in e['faces'].items():
                    if f.get('texture') is None:
                        continue
                    idx, _ = FACES[name]
                    v = p[idx]
                    n = np.cross(v[1] - v[0], v[2] - v[0])
                    length = np.linalg.norm(n)
                    if length < 1e-09:
                        continue
                    u, vv, U, V = f['uv']
                    uv = np.roll(np.array([[u, V], [U, V], [U, vv], [u, vv]]), -f.get('rotation', 0) // 90, axis=0)
                    yield (v, n / length, uv, int(f['texture']))
            else:
                if node.get('visibility', True) == False:
                    continue
                R = rotation(node.get('rotation', [0, 0, 0]))
                q = np.array(node.get('origin', [0, 0, 0]))
                m = np.eye(4)
                m[:3, :3] = R
                m[:3, 3] = q - R @ q
                yield from walk(node['children'], parent @ m)
    yield from walk(model['outliner'], np.eye(4))

# Visible authored geometry only: the optional padlock group is hidden by the author.
visible={};
def collect(nodes,bone='body'):
 for n in nodes:
  if isinstance(n,str):visible[n]=bone
  elif n.get('visibility',True):collect(n['children'],'lid' if n['name']=='lid' else bone)
collect(m['outliner'])
rawverts=np.concatenate([s[0] for s in surfaces(m)])
S=14/np.ptp(rawverts,axis=0)[0];miny=rawverts[:,1].min()
def point(p):return [8-p[0]*S,(p[1]-miny)*S,8-p[2]*S]
elements=[]
for i,e in enumerate(m['elements']):
 if e['uuid'] not in visible:continue
 assert not any(f.get('rotation',0) for f in e['faces'].values())
 assert not any(e.get('rotation',[0,0,0])[1:])
 a,b=point(e['from']),point(e['to'])
 q={'name':f'source_{i}','from':[min(x,y) for x,y in zip(a,b)],'to':[max(x,y) for x,y in zip(a,b)],'origin':point(e.get('origin',[0,0,0])),'rotation':[-e.get('rotation',[0,0,0])[0],0,0],'faces':{},'bone':visible[e['uuid']]}
 for f,v in e['faces'].items():
  if v.get('texture') is None:continue
  name={'north':'south','south':'north','west':'east','east':'west'}.get(f,f)
  uv=[x/2 for x in v['uv']]
  if f in ('up','down'):uv=[uv[2],uv[3],uv[0],uv[1]]
  q['faces'][name]={'uv':uv,'texture':0}
 elements.append(q)

def model(es):
 groups=[{'name':b,'origin':point([0,9,8]) if b=='lid' else [8,0,8],'children':[]} for b in ('body','lid')]
 cubes=[]
 for i,e in enumerate(es):
  q=copy.deepcopy(e);b=q.pop('bone');q['uuid']=str(i);q['type']='cube';groups[b=='lid']['children'].append(str(i));cubes.append(q)
 return {'resolution':{'width':64,'height':64},'elements':cubes,'outliner':groups}
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
for variant,es in {'single':elements}.items():
 d=model(es);data={g['name']:{'origin':g['origin'],'elements':[{k:copy.deepcopy(e[k]) for k in ('from','to','origin','rotation','faces')} for e in d['elements'] if e['uuid'] in g['children']]} for g in d['outliner']}
 for g in data.values():
  for e in g['elements']:e['faces']={n:f['uv'] for n,f in e['faces'].items()}
 write(ASSETS/f'models/chest/{variant}.json',data)
WOODS=[('aurorian_chest','谧木','silent_tree_planks'),('weeping_willow_chest','垂柳木','weeping_willow_planks'),('curtain_wood_chest','幽帘木','curtain_tree_planks'),('cursed_frost_wood_chest','咒霜木','cursed_frost_tree_planks'),('filthy_wood_chest','污秽木','filthy_tree_planks')]
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1000,1700),'#111d29');draw=ImageDraw.Draw(sheet)
draw.text((30,22),'极光五种木箱 · 1号箱子模型',font=font(37),fill='#e7f0f7')
draw.text((32,78),'采用已确认的木材与包带配色 · 仅单箱',font=font(23),fill='#aabcce')
baseitem=json.loads((ASSETS/'models/item/aurorian_chest.json').read_text())
for row,(key,label,plank) in enumerate(WOODS):
 wood=Image.open(ASSETS/f'textures/block/{plank}.png').convert('RGB')
 wood_key='silent_wood' if key=='aurorian_chest' else key.removesuffix('_chest')
 tex=chest_palette(original,wood_key)
 tex.save(ASSETS/f'textures/entity/chest/{key}.png');tex.save(OUT/f'{key}.png')
 item=copy.deepcopy(baseitem);item['textures']={'particle':f'theaurorian2:block/{plank}','chest':f'theaurorian2:block/{key}_model'};item['elements']=[]
 for e in elements:
  q={k:e[k] for k in ('from','to')};q['faces']={n:{'uv':[v/4 for v in f['uv']],'texture':'#chest'} for n,f in e['faces'].items()}
  if e['rotation'][0]:q['rotation']={'axis':'x','angle':e['rotation'][0],'origin':e['origin'],'rescale':False}
  item['elements'].append(q)
 write(ASSETS/f'models/item/{key}.json',item)
 y=135+row*306;draw.text((28,y+30),label+'箱子',font=font(27),fill='#e7f0f7');sheet.paste(wood.resize((72,72),Image.Resampling.NEAREST),(54,y+83))
 for es,x,w in [(elements,280,630)]:
  im=render(model(es),(tex,),w,280,camera_angles=(20,-30,0));sheet.paste(im,(x,y),im)
sheet.save(OUT/'overview.png')
print(f'Imported {len(elements)} visible source elements; approved wood and hardware ramps applied to all five textures.')
