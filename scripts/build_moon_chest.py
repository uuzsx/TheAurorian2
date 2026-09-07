"""Build an original animated moonstone reliquary as an editable Blockbench model."""
import ast, base64, copy, importlib.util, json, math, uuid
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'exports/moon_chest';OUT.mkdir(exist_ok=True)
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'theaurorian2/moon_chest/'+name))
ingot=Image.open(ROOT/'src/main/resources/assets/theaurorian2/textures/item/moonstone_ingot.png').convert('RGBA')
palette=sorted({p[:3] for p in ingot.getdata() if p[3]},key=sum)
atlas=Image.new('RGB',(64,64),palette[0])
materials={'shadow':0,'dark':1,'steel':2,'silver':3,'pearl':4,'ivory':5,'light':6}
for material,index in materials.items():
 u=(index%4)*16;v=(index//4)*16
 for y in range(16):
  for x in range(16):
   shade=index
   if material not in ('light','shadow'):
    if x==0 or y==0:shade=min(6,index+1)
    elif x==15 or y==15:shade=max(0,index-1)
   atlas.putpixel((u+x,v+y),palette[shade])
# Three native 16px inlay designs, using only the ingot palette.
materials.update({'star_inlay':7,'moon_inlay':8,'lace_inlay':9})
for name,tile in [('star_inlay',7),('moon_inlay',8),('lace_inlay',9)]:
 u=(tile%4)*16;v=(tile//4)*16
 for y in range(16):
  for x in range(16):
   dx=abs(x-7.5);dy=abs(y-7.5);shade=2
   if x in (0,15) or y in (0,15):shade=5
   elif x in (1,14) or y in (1,14):shade=3
   elif name=='star_inlay':
    if dx+dy<6 or (min(dx,dy)<1 and max(dx,dy)<6):shade=5
    if dx+dy<2:shade=6
    if 4.5<dx+dy<6.5 and min(dx,dy)>1:shade=4
   elif name=='moon_inlay':
    outer=(x-7)**2+(y-7.5)**2<=24
    inner=(x-9.5)**2+(y-6.5)**2<=19
    if outer and not inner:shade=6
   else:
    shade=2  # Quiet recessed field; ornament belongs to the central medallions.
   atlas.putpixel((u+x,v+y),palette[shade])
atlas.save(OUT/'moon_chest.png')
elements=[];groups={}
def group(name,origin):
 groups[name]={'name':name,'uuid':uid(name),'origin':origin,'rotation':[0,0,0],'children':[],'isOpen':True,'visibility':True,'export':True}
group('body',[8,0,8]);group('lid_left',[-3,15,8]);group('lid_right',[19,15,8])
group('seal',[8,11.5,18.6]);group('moon_halo',[8,16,-1]);group('offering',[8,3.5,8])
def cube(name,lo,hi,mat='ivory',bone='body',rotation=(0,0,0),origin=None):
 size=[b-a for a,b in zip(lo,hi)];assert min(size)>0
 tile=materials[mat];u=(tile%4)*16;v=(tile//4)*16;faces={}
 for face in ('north','south','west','east','up','down'):
  w=size[2] if face in ('west','east') else size[0];h=size[2] if face in ('up','down') else size[1]
  faces[face]={'uv':([u,v,u+16,v+16] if mat.endswith('_inlay') else [u+.25,v+.25,u+.25+min(15,w),v+.25+min(15,h)]),'texture':0}
 e={'name':name,'type':'cube','uuid':uid(name),'from':lo,'to':hi,'origin':origin or [(a+b)/2 for a,b in zip(lo,hi)],'rotation':list(rotation),'box_uv':False,'autouv':0,'faces':faces,'color':0}
 elements.append(e);groups[bone]['children'].append(e['uuid'])
def bar(name,a,b,width,depth,mat,bone='body'):
 # A thin rectangle in the XY plane, used for actual curved metalwork.
 center=[(a[i]+b[i])/2 for i in range(3)];length=math.hypot(b[0]-a[0],b[1]-a[1])
 angle=math.degrees(math.atan2(b[1]-a[1],b[0]-a[0]))
 cube(name,[center[0]-length/2,center[1]-width/2,center[2]-depth/2],[center[0]+length/2,center[1]+width/2,center[2]+depth/2],mat,bone,(0,0,angle),center)
def arc(name,center,radius,start,end,segments,mat,bone,width=.4):
 for i in range(segments):
  a,b=map(math.radians,(start+(end-start)*i/segments,start+(end-start)*(i+1)/segments))
  p=[center[0]+radius*math.cos(a),center[1]+radius*math.sin(a),center[2]]
  q=[center[0]+radius*math.cos(b),center[1]+radius*math.sin(b),center[2]]
  bar(name+str(i),p,q,width,.45,mat,bone)

# Stepped altar plinth and hollow, chamfered-looking silver body.
cube('底座暗托',[-4,.25,-2],[20,1.2,18],'dark')
cube('底座银边',[-4.25,1.2,-2.25],[20.25,1.6,18.25],'pearl')
cube('阶梯台基',[-3.5,1.6,-1.5],[19.5,2.8,17.5],'ivory')
cube('箱体底板',[-2,2.8,0],[18,4,16],'steel')
cube('内衬',[-1,4,1],[17,4.2,15],'dark')
for side,z in [('前',16),('后',-1)]:
 cube(side+'壁',[-2,3.3,z],[18,14.7,z+1],'ivory')
 cube(side+'嵌银边',[-2.15,4,z-.12],[18.15,4.5,z+1.12],'silver')
 cube(side+'顶冠边',[-2.3,14.2,z-.2],[18.3,15,z+1.2],'pearl')
 # Large patterned relief replaces the repetitive vertical grilles.
 cube(side+'镂纹衬板',[-1,5.6,z-.15],[17,13.5,z+1.15],'lace_inlay')
 for x in [1,15]:
  cube(side+f'菱形嵌银{x}',[x-1.85,7.2,z-.28],[x+1.85,10.9,z+1.28],'silver',rotation=(0,0,45))
  cube(side+f'月星徽章{x}',[x-1.35,7.7,z-.34],[x+1.35,10.4,z+1.34],'star_inlay')
for side,x in [('左',-3),('右',18)]:
 cube(side+'壁',[x,3.3,0],[x+1,14.7,16],'ivory')
 cube(side+'月纹侧板',[x-.13,5.7,2],[x+1.13,13,14],'lace_inlay')
 cube(side+'侧面菱框',[x-.25,6.3,5],[x+1.25,12.3,11],'silver',rotation=(45,0,0))
 cube(side+'月相镶嵌',[x-.32,7.1,5.8],[x+1.32,11.5,10.2],'moon_inlay')
# Four architectural corner buttresses, with stepped finials.
for x in (-3,18):
 for z in (-1,16):
  key=f'{x}_{z}'
  cube('角柱'+key,[x-.6,2,z-.6],[x+1.6,15.5,z+1.6],'pearl')
  cube('角柱暗嵌'+key,[x-.68,4,z-.68],[x+1.68,4.5,z+1.68],'steel')
  cube('柱冠'+key,[x-.95,14,z-.95],[x+1.95,14.6,z+1.95],'ivory')
  cube('柱顶菱晶'+key,[x-.35,15.4,z-.35],[x+1.35,17.1,z+1.35],'silver',rotation=(0,45,0))
# Two new wings instead of the wooden chest's rear-hinged lid.
for side,lo,hi in [('left',-3,8),('right',8,19)]:
 bone='lid_'+side
 cube(side+'盖底',[lo,15.15,-1],[hi,16.1,17],'steel',bone)
 cube(side+'盖身',[lo+.15,16.1,-.7],[hi-.15,17.6,16.7],'ivory',bone)
 cube(side+'顶面银框',[lo+.65,17.6,.25],[hi-.65,17.95,15.75],'silver',bone)
 cube(side+'顶面珠光',[lo+1.05,17.95,.65],[hi-1.05,18.15,15.35],'pearl',bone)
 # A diamond medallion, moon-phase field and pearl corners replace ladder ribs.
 center=(lo+hi)/2
 cube(side+'顶盖织纹',[lo+1.1,18.15,.75],[hi-1.1,18.22,15.25],'lace_inlay',bone)
 cube(side+'顶盖菱银框',[center-2.6,18.22,5.4],[center+2.6,18.5,10.6],'ivory',bone,(0,45,0))
 cube(side+'八芒星嵌饰',[center-2.05,18.5,5.95],[center+2.05,18.58,10.05],'star_inlay',bone)
 cube(side+'盖内嵌',[lo+1,15.04,.7],[hi-1,15.14,15.3],'lace_inlay',bone)
 cube(side+'内盖月徽',[center-2,14.96,6],[center+2,15.04,10],'moon_inlay',bone)
# Crescent seal is clearly decorative, without a conventional keyhole.
arc('封印月牙',[8,11.5,18.45],2.7,48,312,15,'pearl','seal',.7)
arc('月牙内光',[8.35,11.5,18.76],2.15,55,305,13,'light','seal',.22)
cube('封印月晶',[7.4,10.9,18.45],[8.6,12.1,19.25],'silver','seal',(0,0,45))
for x,y in [(8,15.1),(8,7.9),(4.5,11.5),(11.5,11.5)]:
 cube(f'封印星芒{x}_{y}',[x-.2,y-.55,18.4],[x+.2,y+.55,18.8],'light','seal')
# Moon halo is a physical segmented model, revealed only during opening.
arc('月轮外环',[8,16,-1],7.8,0,360,32,'pearl','moon_halo',.48)
arc('月轮内环',[8,16,-.7],6.9,0,360,32,'silver','moon_halo',.2)
for i in range(8):
 a=i*math.pi/4;x=8+8.2*math.cos(a);y=16+8.2*math.sin(a)
 cube('月轮星标'+str(i),[x-.32,y-.65,-1.3],[x+.32,y+.65,-.65],'light','moon_halo',(0,0,i*45))
cube('升降内台',[2,4.25,3],[14,4.8,13],'silver','offering')
cube('内台珠边',[2.2,4.8,3.2],[13.8,5,12.8],'pearl','offering')
cube('内台暗面',[3,5,4],[13,5.2,12],'dark','offering')
cube('月晶底托',[6.7,5.2,6.7],[9.3,5.7,9.3],'ivory','offering',(0,45,0))
cube('中心月晶',[7.1,5.7,7.1],[8.9,8.8,8.9],'pearl','offering',(0,45,0))
cube('月晶上尖',[7.5,8.8,7.5],[8.5,9.5,8.5],'light','offering',(0,45,0))
cube('月晶顶光',[7.8,9.5,7.8],[8.2,10,8.2],'light','offering',(0,45,0))
for x,z in [(5.3,8),(10.7,8),(8,5.3),(8,10.7)]:
 cube(f'伴月碎晶{x}_{z}',[x-.25,6.1,z-.25],[x+.25,7.2,z+.25],'silver','offering',(0,45,0))

# Each channel has explicit keyframes; preview evaluates these same channels.
tracks={
 'seal':{'position':[(0,[0,0,0]),(.45,[0,1.6,1.6]),(.9,[0,3.1,2]),(2.6,[0,3.1,2])],
         'rotation':[(0,[0,0,0]),(.45,[0,0,0]),(.9,[0,180,0]),(2.6,[0,180,0])]},
 'lid_left':{'rotation':[(0,[0,0,0]),(.45,[0,0,0]),(1.7,[0,0,68]),(2.6,[0,0,68])]},
 'lid_right':{'rotation':[(0,[0,0,0]),(.55,[0,0,0]),(1.8,[0,0,-68]),(2.6,[0,0,-68])]},
 'moon_halo':{'scale':[(0,[0,0,0]),(.9,[0,0,0]),(1.8,[1,1,1]),(2.6,[1,1,1])],
              'position':[(0,[0,0,0]),(.9,[0,0,0]),(2.2,[0,10,-1]),(2.6,[0,10,-1])],
              'rotation':[(0,[0,0,-45]),(.9,[0,0,-45]),(2.6,[0,0,0])]},
 'offering':{'position':[(0,[0,0,0]),(1.25,[0,0,0]),(2.6,[0,9,0])]}}
def animation(name,reverse=False):
 animators={}
 for bone,channels in tracks.items():
  frames=[]
  for channel,keys in channels.items():
   for i,(t,value) in enumerate(keys):
    frames.append({'channel':channel,'data_points':[dict(zip(('x','y','z'),map(str,value)))],
      'uuid':uid(name+bone+channel+str(i)),'time':round(2.6-t,4) if reverse else t,'color':-1,'interpolation':'linear'})
  animators[uid(bone)]={'name':bone,'type':'bone','keyframes':sorted(frames,key=lambda k:k['time'])}
 return {'uuid':uid(name),'name':'animation.moon_chest.'+name,'loop':'hold','length':2.6,'animators':animators}
model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':'皎月箱',
 'model_identifier':'moon_chest','resolution':{'width':64,'height':64},'elements':elements,'outliner':list(groups.values()),
 'textures':[{'name':'moon_chest.png','id':'0','uuid':uid('texture'),'width':64,'height':64,'uv_width':64,'uv_height':64,
 'source':'data:image/png;base64,'+base64.b64encode((OUT/'moon_chest.png').read_bytes()).decode()}],
 'animations':[animation('open'),animation('close',True)]}
(OUT/'moon_chest.bbmodel').write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf8')

# Render actual model geometry and its exported animation, not concept imagery.
spec=importlib.util.spec_from_file_location('chest_render',ROOT/'scripts/export_wood_chests.py')
renderer=importlib.util.module_from_spec(spec);spec.loader.exec_module(renderer)
def channel(keys,t):
 for (a,av),(b,bv) in zip(keys,keys[1:]):
  if a<=t<=b:
   f=(t-a)/(b-a);return np.array(av)*(1-f)+np.array(bv)*f
 return np.array(keys[-1][1] if t>=keys[-1][0] else keys[0][1])
def posed_surfaces(m,opened=False):
 t=float(opened)
 for e in elements:
  bone=next(g for g in groups.values() if e['uuid'] in g['children']);tr=tracks.get(bone['name'],{})
  pos=channel(tr['position'],t) if 'position' in tr else np.zeros(3)
  scale=channel(tr['scale'],t) if 'scale' in tr else np.ones(3)
  if max(scale)<.001:continue
  rot=renderer.rotation(channel(tr['rotation'],t)) if 'rotation' in tr else np.eye(3)
  er=renderer.rotation(e['rotation']);pivot=np.array(bone['origin']);o=np.array(e['origin'])
  x,y,z=e['from'];X,Y,Z=e['to']
  vs=np.array([[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]])
  vs=(((vs-o)@er.T+o-pivot)*scale)@rot.T+pivot+pos
  for face,f in e['faces'].items():
   ids,n=renderer.FACES[face];u,v,U,V=f['uv']
   yield vs[ids],rot@er@n,np.array([[u,V],[U,V],[U,v],[u,v]]),0
renderer.surfaces=posed_surfaces
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1500,1080),'#101823');d=ImageDraw.Draw(sheet)
d.text((38,24),'皎月箱 · 月轮圣匣',font=font(40),fill='#f0ecdc')
d.text((40,86),'皎月石锭七色 / 月牙与星徽 / 银白轮廓 / 简约留白',font=font(22),fill='#b5bfc0')
for i,(t,label) in enumerate([(0,'静置 · 封印'),(.7,'解印 · 双翼初展'),(1.6,'展开 · 月轮升起'),(2.6,'开启 · 内台呈现')]):
 x=25+(i%2)*745;y=140+(i//2)*460
 d.rounded_rectangle((x,y,x+720,y+438),18,fill='#202a38')
 im=renderer.render(model,atlas,695,378,t);sheet.paste(im,(x+12,y+7),im)
 d.text((x+25,y+393),label,font=font(24),fill='#e5e7e8')
sheet.save(OUT/'overview.png')
# Fixed framing across time prevents the preview camera from pumping on open.
source=__import__('inspect').getsource(renderer.render)
source=source.replace("positions = np.concatenate([v @ camera.T for v,n,uv,slot in scene])", "positions = fixed_bounds")
namespace=renderer.__dict__
namespace['fixed_bounds']=np.concatenate([v@renderer.rotation((18,-24,0)).T for t in np.linspace(0,2.6,12) for v,n,uv,slot in posed_surfaces(model,t)])
exec(source,namespace)
frames=[]
times=[0]*6+list(np.linspace(0,2.6,32))+[2.6]*10+list(np.linspace(2.6,0,32))+[0]*6
for t in times:
 frame=Image.new('RGB',(720,650),'#101823');fd=ImageDraw.Draw(frame)
 fd.text((25,18),'皎月箱 · 开合动画',font=font(27),fill='#f0ecdc')
 im=renderer.render(model,atlas,700,555,t);frame.paste(im,(10,65),im)
 frames.append(frame)
frames[0].save(OUT/'opening.gif',save_all=True,append_images=frames[1:],duration=85,loop=0,optimize=False)
assert len(elements)==len({e['uuid'] for e in elements})
assert {e['uuid'] for e in elements}=={u for g in groups.values() for u in g['children']}
assert set(atlas.getdata())<=set(palette)
(OUT/'README.txt').write_text('皎月箱：Boss 房间宝箱模型提案，尚未接入游戏。\n采用皎月石锭原有七色，64×64 像素材质。\n全新阶梯台基、对开翼盖、月牙封印、升降内台、可展开月轮。\nBlockbench 内含 open 和 close 两段 2.6 秒动画；GIF 为同一几何及关键帧的实际渲染。\n月轮在开箱动画 0 秒缩放为零；编辑器静态模型会显示完整月轮，播放 open 动画可见关闭状态。\n设计预览不设定具体 Boss 归属、不增加剧情或掉落规则。\n',encoding='utf8')
print(f'{len(elements)} cubes, {len(groups)} animated/body groups; seven source colors; open/close and GIF exported')
