"""Import ShizuArt's supplied stool/table without changing their shape or UVs.

Only the long table's X origin is shifted +16 to fit the existing paired block.
Signed cuboids, transparent cutouts, face rotations and authored grain survive.
PowerShell entry points validate and compact generated JSON with Write-Json.
"""
import base64
import copy
import json
import math
import uuid
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
import export_wood_chests as renderer

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/theaurorian2'
SOURCE=Path('E:/BaiduNetdiskDownload/1685中世纪浴室家具/EN/For ItemsAdder/shizuart_furnitures')
OUT=ROOT/'exports/medieval_wood_furniture'
WOODS=[('silent_wood','谧木','silent_tree_planks'),('weeping_willow','垂柳木','weeping_willow_planks'),('curtain_wood','幽帘木','curtain_tree_planks'),('cursed_frost_wood','咒霜木','cursed_frost_tree_planks'),('filthy_wood','污秽木','filthy_tree_planks')]

def load_sources():
    models={k:json.loads((SOURCE/f'models/sdb_medievale/{v}.json').read_text(encoding='utf8')) for k,v in [('stool','tabouret'),('long_table','table')]}
    texture=Image.open(SOURCE/'textures/sdb_medievale/baquet2.png').convert('RGBA')
    assert texture.size==(128,128)
    colors=set()
    for key,m in models.items():
        assert len(m['elements'])==(6 if key=='stool' else 5)
        for e in m['elements']:
            assert all(a!=b for a,b in zip(e['from'],e['to']))
            assert not e.get('rotation',{}).get('angle',0)
            for f in e['faces'].values():
                assert f['texture']=='#0' and f.get('rotation',0) in (0,90,180,270)
                u,v,U,V=f['uv'];assert all(0<=n<=16 for n in f['uv'])
                for y in range(math.floor(min(v,V)*8),math.ceil(max(v,V)*8)):
                    for x in range(math.floor(min(u,U)*8),math.ceil(max(u,U)*8)):
                        c=texture.getpixel((x,y))
                        if c[3]:colors.add(c[:3])
    return models,texture,sorted(colors,key=lambda c:(sum(c),c))

def shift(elements,dx):
    result=copy.deepcopy(elements)
    for e in result:
        for name in ('from','to'):e[name][0]+=dx
        if 'rotation' in e:e['rotation']['origin'][0]+=dx
    return result

def half_table(elements,second):
    low=16 if second else 0;high=low+16;result=[]
    for original in elements:
        a,b=original['from'][0],original['to'][0]
        if max(a,b)<=low or min(a,b)>=high:continue
        e=copy.deepcopy(original)
        if min(a,b)<low or max(a,b)>high:
            # Only the positive-width tabletop crosses the two-block seam.
            assert a<b and original is elements[0]
            l,h=max(a,low),min(b,high);t,T=(l-a)/(b-a),(h-a)/(b-a)
            e['from'][0]=l;e['to'][0]=h
            for face,f in e['faces'].items():
                if face in ('north','south','up','down'):
                    assert not f.get('rotation',0)
                    u,v,U,V=f['uv'];p,q=(1-T,1-t) if face=='north' else (t,T)
                    f['uv']=[u+(U-u)*p,v,u+(U-u)*q,V]
            e['faces'].pop('west' if second else 'east',None)
        result.extend(shift([e],-low))
    return result

def block_model(elements,key,plank):
    return {'parent':'minecraft:block/block','render_type':'minecraft:cutout',
            'textures':{'0':f'theaurorian2:block/{key}_medieval_furniture','particle':f'theaurorian2:block/{plank}'},
            'elements':copy.deepcopy(elements)}

def bbmodel(elements,label,png):
    cubes=[]
    for i,e in enumerate(elements):
        q=copy.deepcopy(e)
        q.update(name=f'原模型部件{i}',uuid=str(uuid.uuid5(uuid.NAMESPACE_URL,f'aurorian/medieval/{label}/{i}')),
                 type='cube',box_uv=False,origin=[8,0,8],rotation=[0,0,0])
        for f in q['faces'].values():f['texture']=0;f['uv']=[v*8 for v in f['uv']]
        cubes.append(q)
    return {'meta':{'format_version':'4.10','model_format':'java_block','box_uv':False},
            'name':label,'credit':'Made with Blockbench by ShizuArt; Aurorian plank palette adaptation',
            'resolution':{'width':128,'height':128},'elements':cubes,
            'outliner':[{'name':'body','origin':[8,0,8],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,label+'/body')),'children':[e['uuid'] for e in cubes]}],
            'textures':[{'name':'medieval_furniture.png','id':'0','width':128,'height':128,'uv_width':128,'uv_height':128,'source':'data:image/png;base64,'+base64.b64encode(png).decode()}]}

def main(kinds=('stool','long_table')):
    OUT.mkdir(parents=True,exist_ok=True);models,original,source_colors=load_sources();written=[]
    def write(path,data):
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8');written.append(str(path))
    for key,label,plank in WOODS:
        wood=Image.open(A/f'textures/block/{plank}.png').convert('RGB')
        palette=sorted(set(wood.getdata()),key=lambda c:(sum(c),c));assert len(palette)==7
        mapping={c:palette[round(i*6/(len(source_colors)-1))] for i,c in enumerate(source_colors)}
        atlas=Image.new('RGBA',original.size)
        atlas.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in original.getdata()])
        texpath=A/f'textures/block/{key}_medieval_furniture.png';atlas.save(texpath);png=texpath.read_bytes()
        for kind in kinds:
            name=key+'_'+kind;folder=ROOT/('exports/wood_stools/'+name if kind=='stool' else 'exports/wood_furniture/'+key)
            folder.mkdir(parents=True,exist_ok=True);atlas.save(folder/'medieval_furniture.png')
            elements=shift(models[kind]['elements'],16 if kind=='long_table' else 0)
            m=bbmodel(elements,label+('凳子' if kind=='stool' else '长桌'),png);write(folder/(name+'.bbmodel'),m)
            if kind=='stool':
                block=block_model(elements,key,plank)
                block['display']={'gui':models[kind]['display']['gui']}
                write(A/f'models/block/{name}.json',block)
            else:
                for second in (False,True):write(A/f'models/block/{name}_{str(second).lower()}.json',block_model(half_table(elements,second),key,plank))
                item=block_model(shift(elements,-8),key,plank)
                item['display']={'gui':{'rotation':[30,225,0],'scale':[.4,.4,.4]},'ground':{'translation':[0,4,0],'scale':[.2,.2,.2]},'fixed':{'rotation':[0,180,0],'scale':[.4,.4,.4]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.3,.3,.3]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[.35,.35,.35]}}
                write(A/f'models/item/{name}.json',item)
    (OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
    if kinds==('stool',):(ROOT/'exports/wood_stools/generated-files.txt').write_text('\n'.join(written),encoding='utf8')
    (OUT/'README.txt').write_text('中世纪木凳与长桌，原作者 ShizuArt，来源为用户提供的浴室家具包 tabouret / table。\n保留原有木纹、透明像素、反向内表面及面UV旋转；只将木色替换为对应木板的七色色阶。\n凳子15×10×15，坐面高⅝格；长桌32×16×16，高1格，模型X平移16以匹配原有双格放置。\n当前凳子、长桌的Blockbench工程分别位于 exports/wood_stools 与 exports/wood_furniture。\n旧木凳纹理仍供靠背椅使用。模型原始授权继续适用，参见 THIRD_PARTY_NOTICES.md。\n',encoding='utf8')
    if 'stool' in kinds:
        (ROOT/'exports/wood_stools/README.txt').write_text('五种木凳采用 ShizuArt 中世纪 tabouret 模型，保留木纹与镂空，各自使用对应木板的原色。\n尺寸15×10×15，坐面高⅝格；右键坐下，Shift起身；配方与注册ID保持原有设置。\nmedieval_furniture.png 为当前模型贴图；原 stool.png 是靠背椅仍在使用的旧座面材质参考。\n',encoding='utf8')
    render_previews(kinds)
    print('Imported '+', '.join(kinds)+' for five woods; signed geometry, alpha and rotated UVs retained.')
    return written

def render_previews(kinds):
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    sheet=Image.new('RGB',(1500,1550),'#111a24');d=ImageDraw.Draw(sheet)
    d.text((30,20),'五种木材 · 中世纪木凳与长桌',font=font(32),fill='#e6eef6')
    d.text((32,72),'沿用原模型、木纹与镂空 / 对应木板采色 / 木凳可坐，长桌向右占两格',font=font(20),fill='#a2b9c7')
    stools=Image.new('RGB',(1550,650),'#111a24');sd=ImageDraw.Draw(stools)
    sd.text((30,22),'五种木材 · 中世纪木凳',font=font(32),fill='#e6eef6')
    for row,(key,label,plank) in enumerate(WOODS):
        y=115+row*280;d.rounded_rectangle((20,y,1480,y+260),16,fill='#1c2835');d.text((35,y+18),label,font=font(26),fill='#e6eef6')
        for kind,x,w in [('stool',190,330),('long_table',565,885)]:
            folder=ROOT/('exports/wood_stools/'+key+'_stool' if kind=='stool' else 'exports/wood_furniture/'+key)
            path=folder/(key+'_'+kind+'.bbmodel')
            m=json.loads(path.read_text(encoding='utf8'))
            tex=Image.open(folder/m['textures'][0]['name']).convert('RGBA')
            im=renderer.render(m,tex,w,235);sheet.paste(im,(x,y+12),im)
            if kind=='stool':
                im=renderer.render(m,tex,300,325);stools.paste(im,(10+row*308,130),im)
                sd.text((30+row*308,490),label+'凳子',font=font(24),fill='#e6eef6')
    sheet.save(OUT/'overview.png')
    if 'long_table' in kinds:sheet.save(ROOT/'exports/wood_furniture/overview.png')
    if 'stool' in kinds:stools.save(ROOT/'exports/wood_stools/overview.png')

if __name__=='__main__':main()
