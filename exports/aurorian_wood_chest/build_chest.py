from pathlib import Path
import json,base64,uuid,ast,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent
W=Image.open(R.parents[1]/'src/main/resources/assets/theaurorian2/textures/block/silent_tree_planks.png').convert('RGB')
texim=Image.new('RGB',(64,64),(43,42,62));texim.paste(W,(0,0));d=ImageDraw.Draw(texim)
materials={'wood':(0,0),'frame':(16,0),'inside':(32,0),'iron':(48,0),'pale':(0,16),'shadow':(16,16),'iron_highlight':(32,16)}
colors={'frame':(61,59,83),'inside':(64,62,83),'iron':(216,216,216),'pale':(133,149,176),'shadow':(53,53,53),'iron_highlight':(255,255,255)}
for name,(u,v) in materials.items():
 if name=='wood':continue
 c=colors[name]
 for y in range(16):
  for x in range(16):
   a=8 if x in (0,1) or y==0 else -8 if x==15 or y==15 else 0
   if name=='inside':a+=((x//3+y//5)%3-1)*3
   if name=='frame':
    # Short broken grain and worn edges, including the first texel column
    # sampled by the narrow handles. Keep the original blue-grey base.
    grain=(0,-6,3,-3,5,-8,1,-4)[x%8]
    a=grain+((x*3+y*5)%7-3)
    if (y+x//3)%7 in (2,3):a-=6
    if x in (0,15):a+=7 if y%5!=3 else -3
    if y in (0,15):a+=4
    if (x*7+y*11)%31==0:a+=10
   texim.putpixel((u+x,v+y),tuple(max(0,min(255,z+a))for z in c))
texim.save(R/'aurorian_chest.png');tex=np.array(texim)
source=(R.parent/'aurorian_wood_tools_v2/build_models.py').read_text(encoding='utf-8-sig')
for n in ast.parse(source).body:
 if isinstance(n,ast.FunctionDef) and n.name in ('rot','render'):
  s=ast.get_source_segment(source,n)
  if n.name=='render':s=s.replace('angles=(12,-30,-28))','angles=(12,-30,-28),fit=24,center=(8,7.5,8))').replace('scale=size/30','scale=size/fit').replace('[8,10,8]','center').replace('0,31','0,63')
  exec(s)
facespec={'north':([1,0,3,2],[0,0,-1]),'south':([4,5,6,7],[0,0,1]),'west':([0,4,7,3],[-1,0,0]),'east':([5,1,2,6],[1,0,0]),'up':([3,7,6,2],[0,1,0]),'down':([0,1,5,4],[0,-1,0])}
E=[];groups={'body':[],'lid':[]};surfs=[]
UID=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian_wood_chest/'+s))
hinge=[8,10,1.6]
def cube(name,lo,hi,mat='wood',group='body',angles=(0,0,0),origin=None):
 pivot=origin or [(a+b)/2 for a,b in zip(lo,hi)];sz=np.array(hi)-lo;fs={}
 for fn in facespec:
  w=sz[0] if fn in ['north','south','up','down'] else sz[2];h=sz[2] if fn in ['up','down'] else sz[1];u,v=materials[mat]
  density=3 if mat=='frame' else 1
  fs[fn]={'uv':[u,v,u+min(16,w*density),v+min(16,h*density)],'texture':0}
 e={'name':name,'type':'cube','uuid':UID(name),'from':lo,'to':hi,'origin':pivot,'rotation':list(angles),'box_uv':False,'autouv':0,'faces':fs,'color':0};E.append(e);groups[group].append(e['uuid'])
 x,y,z=lo;X,Y,Z=hi;vs=[[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]];vs=[(rot(np.array(v)-pivot,angles)+pivot).tolist()for v in vs]
 for fn,(ids,n) in facespec.items():
  u,v,U,V=fs[fn]['uv'];surfs.append({'v':[vs[i]for i in ids],'n':rot(n,angles).tolist(),'uv':[[u,V],[U,V],[U,v],[u,v]],'group':group})
# Hollow body, four walls and a thick bottom.
cube('箱底',[1.5,1.25,1.5],[14.5,2.4,14.5],'frame')
cube('内底木板',[2.25,2.4,2.25],[13.75,2.65,13.75],'inside')
cube('前木壁',[1.8,2.4,13.5],[14.2,9.65,14.5])
cube('后木壁',[1.8,2.4,1.5],[14.2,9.65,2.5])
cube('左木壁',[1.5,2.4,2.5],[2.5,9.65,13.5])
cube('右木壁',[13.5,2.4,2.5],[14.5,9.65,13.5])
# Rim and foot corners.
for x in [1.25,13.45]:
 for z in [1.25,13.45]:
  cube(f'角柱_{x}_{z}',[x,1.1,z],[x+1.3,9.8,z+1.3],'frame')
  cube(f'箱脚_{x}_{z}',[x-.2,.3,z-.2],[x+1.5,1.7,z+1.5],'frame')
for y,h in [(2.25,.6),(9.1,.65)]:
 cube(f'前框_{y}',[1.35,y,14.35],[14.65,y+h,14.75],'frame')
 cube(f'后框_{y}',[1.35,y,1.25],[14.65,y+h,1.65],'frame')
 cube(f'左框_{y}',[1.25,y,1.65],[1.65,y+h,14.35],'frame')
 cube(f'右框_{y}',[14.35,y,1.65],[14.75,y+h,14.35],'frame')
# Two bands around the body; limited small pale rivets.
for x in [3.15,11.8]:
 for z in [1.2,14.5]:
  cube(f'竖包带_{x}_{z}',[x,2.5,z],[x+1.05,9.65,z+.3],'frame')
  for y in [3.05,8.25]:cube(f'铆钉_{x}_{z}_{y}',[x+.32,y,z-.06],[x+.73,y+.4,z+.36],'pale')
# Side handles formed from three bars, mounted clear of the wood wall.
for side,x in [('左',.85),('右',14.85)]:
 for z in [6.2,9.2]:cube(side+f'把手座_{z}',[x,5.55,z],[x+.3,6.8,z+.55],'frame')
 for z in [6.3,9.3]:cube(side+f'把手端_{z}',[x-.35,5.1,z],[x+.6,5.7,z+.35],'frame')
 cube(side+'把手横杆',[x-.35,4.85,6.3],[x+.6,5.35,9.65],'frame')
# Lid is a separately hinged object; shallow arch built from three continuous sections.
cube('箱盖底框',[1.3,10,1.25],[14.7,10.8,14.75],'frame','lid')
cube('盖内木板',[2,9.9,2],[14,10.3,14],'wood','lid')
cube('盖前低木段',[1.6,10.8,11.5],[14.4,11.9,14.35],'wood','lid')
cube('盖后低木段',[1.6,10.8,1.65],[14.4,11.9,4.5],'wood','lid')
cube('盖中承托',[1.6,10.8,4.5],[14.4,11.6,11.5],'wood','lid')
cube('拱盖前段',[1.55,12.1,8],[14.45,13.55,14.2],'wood','lid',(22.5,0,0),[8,12.75,8])
cube('拱盖后段',[1.55,12.1,1.8],[14.45,13.55,8],'wood','lid',(-22.5,0,0),[8,12.75,8])
cube('拱盖顶板',[1.55,13.15,6.5],[14.45,13.7,9.5],'wood','lid')
for x in [3.15,11.8]:
 cube(f'盖前包带_{x}',[x,12,8],[x+1.05,13.74,14.4],'frame','lid',(22.5,0,0),[8,12.75,8])
 cube(f'盖后包带_{x}',[x,12,1.6],[x+1.05,13.74,8],'frame','lid',(-22.5,0,0),[8,12.75,8])
 cube(f'盖顶包带_{x}',[x,13.66,6.4],[x+1.05,13.9,9.6],'frame','lid')
# Iron lock remains attached to lid while opening.
cube('锁扣背板',[6.8,7.65,14.75],[9.2,11.55,15.1],'frame','lid')
cube('铁质锁扣',[7.15,8.15,15.08],[8.85,10.85,15.45],'iron','lid')
cube('锁扣亮面',[7.48,9.6,15.44],[8.52,10.43,15.58],'iron_highlight','lid')
cube('钥匙孔上',[7.8,8.95,15.57],[8.2,9.38,15.62],'shadow','lid')
cube('钥匙孔下',[7.91,8.6,15.57],[8.09,9.08,15.62],'shadow','lid')
for x in [3.1,11.9]:cube(f'后铰链_{x}',[x,9.55,1],[x+1,10.75,1.7],'pale')
lidid=UID('lid');bodyid=UID('body')
keyframes=[]
for time,angle in [(0,0),(.12,-8),(.55,-100)]:keyframes.append({'channel':'rotation','data_points':[{'x':str(angle),'y':'0','z':'0'}],'uuid':UID('frame'+str(time)),'time':time,'color':-1,'interpolation':'linear'})
bb={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':'极光木宝箱','model_identifier':'aurorian_wood_chest','resolution':{'width':64,'height':64},'elements':E,'outliner':[{'name':'body','uuid':bodyid,'origin':[8,0,8],'rotation':[0,0,0],'children':groups['body'],'isOpen':True,'visibility':True,'export':True},{'name':'lid','uuid':lidid,'origin':hinge,'rotation':[0,0,0],'children':groups['lid'],'isOpen':True,'visibility':True,'export':True}],'textures':[{'name':'aurorian_chest.png','id':'0','uuid':UID('texture'),'width':64,'height':64,'uv_width':64,'uv_height':64,'source':'data:image/png;base64,'+base64.b64encode((R/'aurorian_chest.png').read_bytes()).decode()}],'animations':[{'uuid':UID('animation'),'name':'animation.aurorian_wood_chest.open','loop':'hold','length':.55,'animators':{lidid:{'name':'lid','type':'bone','keyframes':keyframes}}}]}
(R/'aurorian_wood_chest.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
def pose(angle):
 out=[]
 for s in surfs:
  s=dict(s)
  if s['group']=='lid':s['v']=[(rot(np.array(v)-hinge,[angle,0,0])+hinge).tolist()for v in s['v']];s['n']=rot(s['n'],[angle,0,0]).tolist()
  out.append(s)
 return {'surfaces':out}
closed=pose(0);opened=pose(-100)
(R/'geometry.json').write_text(json.dumps({'closed':closed,'opened':opened}),encoding='utf-8')
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1800,1220),'#111a24');dd=ImageDraw.Draw(sheet)
dd.text((55,34),'极光木宝箱 · 模型预览',font=font(43),fill='#edf3ff')
dd.text((58,101),'极光木板 / 拱形箱盖 / 深色包带 / 铁质锁扣 / 独立开合结构',font=font(23),fill='#9fb4c9')
dd.rounded_rectangle([35,160,1080,1175],24,fill='#1e2a38',outline='#34495e',width=2)
im=render(closed,1020,(22,-35,0));sheet.paste(im,(45,180),im);im.save(R/'closed_preview.png')
dd.text((70,1100),'闭合 · 斜前视角',font=font(25),fill='#c3d6e9')
for y,m,angles,label in [(160,opened,(16,-32,0),'开盖 · 箱体内部'),(675,closed,(16,145,0),'背面 · 铰链与侧把手')]:
 dd.rounded_rectangle([1105,y,1765,y+500],24,fill='#1e2a38',outline='#34495e',width=2)
 im=render(m,540,angles,fit=32 if m is opened else 24,center=(8,11.5,8) if m is opened else (8,7.5,8));sheet.paste(im,(1160,y-5),im)
 dd.text((1140,y+440),label,font=font(23),fill='#c3d6e9')
sheet.save(R/'overview.png')
assert len({e['uuid']for e in E})==len(E)
assert all(all(b>a for a,b in zip(e['from'],e['to']))for e in E)
assert set(groups['body']).isdisjoint(groups['lid'])
print(len(E),'cubes',len(groups['lid']),'lid cubes; validated geometry and bone assignments')
