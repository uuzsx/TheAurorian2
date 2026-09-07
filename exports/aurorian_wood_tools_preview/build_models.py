import base64, json, math, uuid, shutil
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parent
SRC=ROOT.parents[1]/"src/main/resources/assets/theaurorian2/textures/item"
NAMES={"pickaxe":"极光木镐", "axe":"极光木斧", "hoe":"极光木锄", "shovel":"极光木铲"}
FONT="C:/Windows/Fonts/msyh.ttc"
def font(size): return ImageFont.truetype(FONT,size)
models={}
for key,name in NAMES.items():
    source=SRC/f"silent_wood_{key}.png"
    tex=Image.open(source).convert("RGBA")
    shutil.copy2(source,ROOT/source.name)
    elements=[]; groups={"工具头":[],"蓝色连接处":[],"握柄":[]}; boxes=[]
    for y in range(16):
      for x in range(16):
        rgba=tex.getpixel((x,y))
        if rgba[3]==0: continue
        r,g,b,a=rgba
        handle=y>=8 and x<=8
        blue=b>r*1.3 and b>g*1.05 and (b-r)>45
        group="蓝色连接处" if blue else "握柄" if handle else "工具头"
        adjacent=sum(0<=x+dx<16 and 0<=y+dy<16 and tex.getpixel((x+dx,y+dy))[3]>0 for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)))
        depth=2.2 if handle else 3.2
        if blue: depth=3.8
        if adjacent<=2: depth-=0.6
        if group=="工具头" and adjacent==4: depth+=0.4
        ident=str(uuid.uuid5(uuid.NAMESPACE_URL,f"aurorian-tools/{key}/{x}/{y}"))
        lo=[x,15-y,8-depth/2]; hi=[x+1,16-y,8+depth/2]
        faces={f:{"uv":[x,y,x+1,y+1],"texture":0} for f in ["north","east","south","west","up","down"]}
        element={"name":f"{group}_{x}_{y}","box_uv":False,"rescale":False,"locked":False,"from":lo,"to":hi,"autouv":0,"color":0,"origin":[8,8,8],"faces":faces,"type":"cube","uuid":ident}
        elements.append(element);groups[group].append(ident);boxes.append({"from":lo,"to":hi,"color":list(rgba[:3])})
    outliner=[{"name":g,"origin":[8,8,8],"rotation":[0,0,0],"uuid":str(uuid.uuid5(uuid.NAMESPACE_URL,key+g)),"children":ids,"export":True,"isOpen":True,"visibility":True} for g,ids in groups.items() if ids]
    data={"meta":{"format_version":"4.10","model_format":"java_block","box_uv":False},"name":name,"model_identifier":f"silent_wood_{key}_preview","visible_box":[1,1,0],"resolution":{"width":16,"height":16},"elements":elements,"outliner":outliner,"textures":[{"path":str(ROOT/source.name),"name":source.name,"folder":"item","namespace":"theaurorian2","id":"0","uuid":str(uuid.uuid5(uuid.NAMESPACE_URL,key+"texture")),"width":16,"height":16,"uv_width":16,"uv_height":16,"particle":True,"use_as_default":True,"render_mode":"default","render_sides":"double","source":"data:image/png;base64,"+base64.b64encode(source.read_bytes()).decode()}],"display":{"gui":{"rotation":[15,-30,0],"translation":[0,0,0],"scale":[1,1,1]},"thirdperson_righthand":{"rotation":[0,-90,55],"translation":[0,4,0.5],"scale":[0.85,0.85,0.85]},"firstperson_righthand":{"rotation":[0,-90,25],"translation":[1.13,3.2,1.13],"scale":[0.68,0.68,0.68]}}}
    (ROOT/f"silent_wood_{key}.bbmodel").write_text(json.dumps(data,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
    assert len({e['uuid'] for e in elements})==len(elements)
    assert len(elements)==sum(tex.getpixel((x,y))[3]>0 for x in range(16) for y in range(16))
    models[key]={"name":name,"boxes":boxes,"cubes":len(elements)}
(ROOT/"geometry.json").write_text(json.dumps(models,ensure_ascii=False),encoding="utf-8")
# Render the actual model cuboids. No illustrative geometry is added.
FACES=[([0,3,2,1],(0,0,-1)),([4,5,6,7],(0,0,1)),([0,4,7,3],(-1,0,0)),([1,2,6,5],(1,0,0)),([3,7,6,2],(0,1,0)),([0,1,5,4],(0,-1,0))]
def render(model,size,ax=12,ay=-32):
    a,b=math.radians(ax),math.radians(ay)
    def rot(v):
      x,y,z=v; xx=x*math.cos(b)+z*math.sin(b); zz=-x*math.sin(b)+z*math.cos(b)
      return xx,y*math.cos(a)-zz*math.sin(a),y*math.sin(a)+zz*math.cos(a)
    polys=[]; scale=size/21
    for box in model['boxes']:
      x0,y0,z0=box['from'];x1,y1,z1=box['to']
      vs=[(x0,y0,z0),(x1,y0,z0),(x1,y1,z0),(x0,y1,z0),(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)]
      vs=[rot((x-8,y-8,z-8)) for x,y,z in vs]
      for ids,n in FACES:
        rn=rot(n)
        if rn[2]<=0:continue
        pts=[(size/2+vs[i][0]*scale,size/2-vs[i][1]*scale) for i in ids]
        shade=.63+.37*max(0,sum(rn[i]*(-.35,.55,.76)[i] for i in range(3)))
        color=tuple(min(255,int(c*shade)) for c in box['color'])
        polys.append((sum(vs[i][2] for i in ids)/4,pts,color))
    im=Image.new('RGBA',(size,size),(0,0,0,0));d=ImageDraw.Draw(im)
    for _,pts,c in sorted(polys,key=lambda q:q[0]):d.polygon(pts,fill=c)
    return im
W,H=1800,1500
sheet=Image.new('RGB',(W,H),'#101720');d=ImageDraw.Draw(sheet)
d.text((65,40),'极光木制工具 · 立体模型预览',font=font(43),fill='#edf4ff')
d.text((68,110),'保留原贴图轮廓与颜色  /  分层工具头、连接处、握柄  /  尚未接入游戏',font=font(23),fill='#9aaec3')
for i,(key,model) in enumerate(models.items()):
    x=50+(i%2)*880;y=180+(i//2)*645
    d.rounded_rectangle((x,y,x+850,y+615),24,fill='#1b2531',outline='#344458',width=2)
    d.text((x+30,y+25),model['name'],font=font(32),fill='#e5efff')
    hero=render(model,560);sheet.paste(hero,(x+5,y+65),hero)
    side=render(model,270,12,-68);sheet.paste(side,(x+565,y+200),side)
    d.text((x+630,y+475),'侧面体积',font=font(20),fill='#94a9bf')
    d.text((x+30,y+570),f"{model['cubes']} 个可编辑方块  ·  原始 16×16 贴图",font=font(19),fill='#94a9bf')
    hero.save(ROOT/f"{key}_preview.png")
sheet.save(ROOT/'overview.png')
print({k:v['cubes'] for k,v in models.items()})
