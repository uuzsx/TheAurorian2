import base64,json,math,uuid,zipfile
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent
NAMES={'pickaxe':'极光木镐','axe':'极光木斧','hoe':'极光木锄','shovel':'极光木铲'}
# Original construction; reference pack informs proportions and articulation, not copied geometry.
PALETTE={'shaft':(109,123,143),'dark':(61,61,90),'head':(125,146,158),'edge':(166,197,202),'band':(39,141,191),'banddark':(38,69,140),'inlay':(65,186,215)}
T=Image.new('RGB',(64,32));draw=ImageDraw.Draw(T)
UV={}
for i,(mat,c) in enumerate(PALETTE.items()):
 x=(i%4)*16;y=(i//4)*16; UV[mat]=[x,y,x+16,y+16]
 for j in range(16):
  for k in range(16):
   delta=([0,-4,3,0,6,0,-3,2][k%8] if mat in ('shaft','head','edge') else [-2,0,2,0][j%4])
   if mat in ('shaft','head') and (k*7+j*3)%41==0:delta-=10
   T.putpixel((x+k,y+j),tuple(max(0,min(255,v+delta)) for v in c))
T.save(R/'aurorian_tool_materials.png')
facespec={'north':([1,0,3,2],[0,0,-1]),'south':([4,5,6,7],[0,0,1]),'west':([0,4,7,3],[-1,0,0]),'east':([5,1,2,6],[1,0,0]),'up':([3,7,6,2],[0,1,0]),'down':([0,1,5,4],[0,-1,0])}
allmodels={}
def rot(v,angles):
 v=np.array(v,dtype=float)
 for axis,a in enumerate(angles):
  a=math.radians(a);c,s=math.cos(a),math.sin(a)
  if axis==0:v=np.array([v[0],c*v[1]-s*v[2],s*v[1]+c*v[2]])
  elif axis==1:v=np.array([c*v[0]+s*v[2],v[1],-s*v[0]+c*v[2]])
  else:v=np.array([c*v[0]-s*v[1],s*v[0]+c*v[1],v[2]])
 return v
for key,name in NAMES.items():
 E=[];groups={};surf=[]
 def cube(label,lo,hi,mat,angle=0,axis='z',pivot=None,group='工具头'):
  lo=list(lo);hi=list(hi);pivot=list(pivot or [(a+b)/2 for a,b in zip(lo,hi)])
  ang=[0,0,0];ang['xyz'.index(axis)]=angle
  uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian-v2/'+key+'/'+label))
  size=np.array(hi)-np.array(lo)
  f={}
  for fn in facespec:
   # Texture density follows actual component dimensions, never source silhouette pixels.
   w=size[0] if fn in ('north','south','up','down') else size[2]
   h=size[2] if fn in ('up','down') else size[1]
   u,v,_,_=UV[mat];f[fn]={'uv':[u,v,u+min(16,w*2),v+min(16,h*2)],'texture':0}
  e={'name':label,'type':'cube','uuid':uid,'from':lo,'to':hi,'origin':pivot,'rotation':ang,'rescale':False,'box_uv':False,'autouv':0,'faces':f,'color':list(PALETTE).index(mat)}
  E.append(e);groups.setdefault(group,[]).append(uid)
  x,y,z=lo;X,Y,Z=hi
  verts=[[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]]
  verts=[(rot(np.array(q)-pivot,ang)+pivot).tolist() for q in verts]
  for fn,(ids,n) in facespec.items():
   uv=f[fn]['uv'];u,v,U,V=uv
   surf.append({'v':[verts[i] for i in ids],'n':rot(n,ang).tolist(),'uv':[[u,V],[U,V],[U,v],[u,v]],'mat':mat})
 # Slim continuous shaft, end ferrule, inset blue bands. All are functional-sized parts.
 cube('连续木柄',[7.2,0.5,7.2],[8.8,14.8,8.8],'shaft',group='握柄')
 cube('握持段',[7.05,1.7,7.05],[8.95,5.6,8.95],'dark',group='握柄')
 for i,y in enumerate([1.85,3.05,4.25,5.35]):cube('握柄束带'+str(i),[6.98,y,6.98],[9.02,y+.23,9.02],'banddark',group='握柄')
 cube('柄尾护套',[6.9,0.2,6.9],[9.1,1.1,9.1],'head',group='握柄')
 cube('柄尾浅色端面',[7.02,0,7.02],[8.98,.25,8.98],'edge',group='握柄')
 cube('颈部下套',[6.95,12.6,6.95],[9.05,14,9.05],'banddark',group='连接件')
 cube('蓝色绑带',[6.8,13.55,6.8],[9.2,14.45,9.2],'band',group='连接件')
 cube('绑带亮扣',[7.55,13.66,9.19],[8.45,14.3,9.42],'inlay',group='连接件')
 if key=='pickaxe':
  cube('中部承力木梁',[4.7,14.7,6.65],[11.3,17,9.35],'head')
  cube('中心箍',[7,14.5,6.48],[9,17.15,9.52],'dark',group='连接件')
  cube('中心蓝箍',[7.45,14.48,6.38],[8.55,17.18,9.62],'band',group='连接件')
  for side in [-1,1]:
   # Separate inward and outward parts form a bent, thinning pick arm.
   if side==-1:
    cube('左臂',[1.3,14.95,6.95],[4.9,16.7,9.05],'head',22.5,pivot=[4.9,16,8])
    cube('左尖',[-1.25,13.15,7.45],[1.75,14.15,8.55],'edge',45,pivot=[1.6,14,8])
   else:
    cube('右臂',[11.1,14.95,6.95],[14.7,16.7,9.05],'head',-22.5,pivot=[11.1,16,8])
    cube('右尖',[14.25,13.15,7.45],[17.25,14.15,8.55],'edge',-45,pivot=[14.4,14,8])
  cube('梁顶亮边',[4.72,16.95,6.78],[11.28,17.22,9.22],'edge')
 elif key=='axe':
  cube('斧眼承力块',[6.35,14.5,6.4],[9.65,18.3,9.6],'head')
  cube('后锤',[9.55,15.65,6.85],[11.9,17.6,9.15],'dark')
  cube('后锤端帽',[11.55,15.6,6.75],[12.1,17.65,9.25],'head')
  cube('斧身上肩',[3.4,16.1,6.8],[6.65,18.3,9.2],'head',-22.5,pivot=[6.4,16.3,8])
  cube('宽斧面',[1.75,12.9,7.25],[4.65,17.5,8.75],'head')
  cube('下垂斧腹',[2.25,11.75,7.45],[5.15,14.5,8.55],'head',22.5,pivot=[3.5,14.25,8])
  cube('上刃',[1.15,14.25,7.65],[1.9,17.45,8.35],'edge')
  cube('下弧刃',[1.45,11.7,7.7],[2.25,14.5,8.3],'edge',22.5,pivot=[1.9,14.25,8])
  cube('斧眼蓝箍',[7.5,14.45,6.3],[8.5,18.38,9.7],'band',group='连接件')
 elif key=='hoe':
  cube('锄头套筒',[6.65,14.2,6.6],[9.35,17.15,9.4],'head')
  cube('锄背短榫',[9.2,15.5,7.2],[11,16.9,8.8],'dark')
  cube('横向锄梁',[2.1,15,7.05],[6.8,16.6,8.95],'head',22.5,pivot=[6.6,15.7,8])
  cube('下折锄刃',[.4,12.2,6.9],[2.1,15.7,9.1],'head',22.5,pivot=[1.9,15.2,8])
  cube('薄刃端',[.1,11.55,6.72],[1.8,12.35,9.28],'edge',22.5,pivot=[1.4,12.25,8])
  cube('套筒蓝扣',[7.5,14.2,9.38],[8.5,17.2,9.63],'band',group='连接件')
 elif key=='shovel':
  cube('铲面主板',[5,15.2,7.5],[11,19,8.5],'head')
  cube('铲尖',[6.15,17.65,7.6],[9.85,21.35,8.4],'head',45,pivot=[8,19.5,8])
  cube('左铲刃',[4.7,15.5,7.7],[5.3,19,8.3],'edge')
  cube('右铲刃',[10.7,15.5,7.7],[11.3,19,8.3],'edge')
  cube('尖部左刃',[5.4,19,7.72],[8.6,19.45,8.28],'edge',45,pivot=[8,19.5,8])
  cube('尖部右刃',[7.4,19,7.72],[10.6,19.45,8.28],'edge',-45,pivot=[8,19.5,8])
  cube('铲柄套管',[6.95,13.8,6.85],[9.05,16.4,9.15],'dark',group='连接件')
  cube('铲面中央加强筋',[7.55,15.6,8.45],[8.45,19.25,8.9],'edge')
  cube('套管蓝箍',[6.8,14.5,6.7],[9.2,15.25,9.3],'band',group='连接件')
 uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,key+s))
 bb={'meta':{'format_version':'4.10','model_format':'java_block','box_uv':False},'name':name+' v2','resolution':{'width':64,'height':32},'elements':E,'outliner':[{'name':g,'uuid':uid(g),'origin':[8,8,8],'children':ids,'isOpen':True,'visibility':True,'export':True}for g,ids in groups.items()],'textures':[{'name':'aurorian_tool_materials.png','id':'0','uuid':uid('texture'),'width':64,'height':32,'uv_width':64,'uv_height':32,'source':'data:image/png;base64,'+base64.b64encode((R/'aurorian_tool_materials.png').read_bytes()).decode(),'particle':True}], 'display':{'gui':{'rotation':[12,-28,-28],'translation':[0,-1,0],'scale':[.7,.7,.7]},'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,3,0],'scale':[.65,.65,.65]},'firstperson_righthand':{'rotation':[0,-90,10],'translation':[1,1,0],'scale':[.6,.6,.6]}}}
 (R/f'silent_wood_{key}.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
 allmodels[key]={'name':name,'surfaces':surf,'count':len(E)}
 assert len(E)==len({e['uuid'] for e in E})
 assert all(all(b>a for a,b in zip(e['from'],e['to'])) for e in E)
 assert all(sum(a!=0 for a in e['rotation'])<=1 for e in E)
(R/'geometry.json').write_text(json.dumps(allmodels,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
# Orthographic textured triangle rasterizer with depth buffer; preview uses actual exported cubes.
tex=np.array(T)
def render(m,size=700,angles=(12,-30,-28)):
 bg=np.zeros((size,size,4),dtype=np.uint8);zb=np.full((size,size),-1e9)
 scale=size/30
 def triangle(v,uv,shade):
  xs=v[:,0];ys=v[:,1];xmin=max(0,int(xs.min()));xmax=min(size-1,int(np.ceil(xs.max())));ymin=max(0,int(ys.min()));ymax=min(size-1,int(np.ceil(ys.max())))
  if xmax<xmin or ymax<ymin:return
  X,Y=np.meshgrid(np.arange(xmin,xmax+1)+.5,np.arange(ymin,ymax+1)+.5)
  den=(ys[1]-ys[2])*(xs[0]-xs[2])+(xs[2]-xs[1])*(ys[0]-ys[2])
  if abs(den)<1e-9:return
  a=((ys[1]-ys[2])*(X-xs[2])+(xs[2]-xs[1])*(Y-ys[2]))/den
  b=((ys[2]-ys[0])*(X-xs[2])+(xs[0]-xs[2])*(Y-ys[2]))/den;c=1-a-b
  Z=a*v[0,2]+b*v[1,2]+c*v[2,2]; region=zb[ymin:ymax+1,xmin:xmax+1]
  mask=(a>=-1e-7)&(b>=-1e-7)&(c>=-1e-7)&(Z>region)
  U=np.clip((a*uv[0,0]+b*uv[1,0]+c*uv[2,0]).astype(int),0,63);V=np.clip((a*uv[0,1]+b*uv[1,1]+c*uv[2,1]).astype(int),0,31)
  rgb=np.clip(tex[V,U]*shade,0,255).astype(np.uint8);tile=bg[ymin:ymax+1,xmin:xmax+1]
  tile[:,:,:3][mask]=rgb[mask];tile[:,:,3][mask]=255;region[mask]=Z[mask]
 for s in m['surfaces']:
  n=rot(s['n'],angles)
  if n[2]<=0:continue
  verts=np.array([rot(np.array(v)-[8,10,8],angles) for v in s['v']]); verts[:,0]=size/2+verts[:,0]*scale;verts[:,1]=size/2-verts[:,1]*scale
  shade=.62+.4*max(0,float(np.dot(n,[-.35,.6,.72])))
  for ids in [[0,1,2],[0,2,3]]:triangle(verts[ids],np.array(s['uv'])[ids],shade)
 return Image.fromarray(bg)
font=lambda s:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',s)
sheet=Image.new('RGB',(1900,1600),'#111923');d=ImageDraw.Draw(sheet)
d.text((60,32),'极光木制工具 · 结构建模第二版',font=font(44),fill='#edf3ff')
d.text((63,100),'连续握柄 / 独立工具头 / 收薄刃口 / 蓝色连接件     ·     仅预览，未接入游戏',font=font(24),fill='#9fb2c7')
notes={'pickaxe':'分段弯折镐臂 · 收尖端部','axe':'厚斧眼 · 展开的薄斧刃','hoe':'横向锄梁 · 下折宽刃','shovel':'独立铲面 · 中央加强筋'}
for i,(k,m) in enumerate(allmodels.items()):
 x=45+(i%2)*935;y=165+(i//2)*705
 d.rounded_rectangle([x,y,x+895,y+675],radius=22,fill='#1c2836',outline='#34485c',width=2)
 d.text((x+27,y+22),m['name'],font=font(32),fill='#edf3ff');d.text((x+27,y+69),notes[k],font=font(20),fill='#93aec7')
 im=render(m,660);sheet.paste(im,(x-25,y+85),im);im.save(R/f'{k}_preview.png')
 im=render(m,285,(10,-70,-10));sheet.paste(im,(x+597,y+255),im)
 d.text((x+659,y+531),'侧面结构',font=font(20),fill='#93aec7')
 d.text((x+27,y+631),f"{m['count']} 个结构部件 · Blockbench 可编辑",font=font(19),fill='#93aec7')
sheet.save(R/'overview.png')
print({k:m['count'] for k,m in allmodels.items()})
