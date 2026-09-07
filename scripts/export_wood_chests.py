"""Material variants of the approved chest, using the project's wood textures.

Only exported model names and embedded materials change. Runtime resources are
untouched. export_wood_chests.ps1 validates/compacts JSON and packages the results.
"""
import ast
import base64
import copy
import io
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

REPO = Path(__file__).resolve().parents[1]
OUT = REPO / 'exports/aurorian_wood_chest_variants'
BLOCKS = REPO / 'src/main/resources/assets/theaurorian2/textures/block'
WOODS = (
    ('weeping_willow', '垂柳木', 'weeping_willow_planks'),
    ('curtain', '幽帘木', 'curtain_tree_planks'),
    ('cursed_frost', '咒霜木', 'cursed_frost_tree_planks'),
    ('filthy', '污秽木', 'filthy_tree_planks'),
)
SOURCES = {
    'single': REPO / 'exports/aurorian_wood_chest/aurorian_wood_chest.bbmodel',
    'double': REPO / 'exports/aurorian_double_wood_chest/aurorian_double_wood_chest.bbmodel',
    'double_left': REPO / 'exports/aurorian_double_wood_chest/aurorian_double_wood_chest_left.bbmodel',
    'double_right': REPO / 'exports/aurorian_double_wood_chest/aurorian_double_wood_chest_right.bbmodel',
}
MODELS = {key: json.loads(path.read_text(encoding='utf8')) for key, path in SOURCES.items()}


def shared_wood_grain(palette_source):
    """Keep the common chest grain pixel-for-pixel, using filthy plank colors."""
    template = Image.open(BLOCKS/'silent_tree_planks.png').convert('RGB')
    template_pixels = list(template.getdata())
    original = sorted(set(template_pixels), key=sum)
    replacement = sorted(set(palette_source.getdata()), key=sum)
    assert len(original) == len(replacement) == 7
    mapping = dict(zip(original, replacement))
    result = Image.new('RGB', template.size)
    result.putdata([mapping[pixel] for pixel in template_pixels])
    return result


def material_atlas(wood):
    # Reuse the authored, deterministic material program; retain iron and its UVs.
    script = REPO / 'exports/aurorian_wood_chest/build_chest.py'
    source = script.read_text(encoding='utf8')
    module = ast.parse(source)
    palette = {}
    materials = {}
    for node in module.body:
        if isinstance(node, ast.Assign):
            names = [t.id for t in node.targets if isinstance(t, ast.Name)]
            if 'colors' in names: palette = ast.literal_eval(node.value)
            if 'materials' in names: materials = ast.literal_eval(node.value)
    mean = np.asarray(wood, dtype=float).mean(axis=(0, 1))
    dark = mean * .46
    dark = dark.mean() + (dark-dark.mean()) * 1.5
    palette['frame'] = np.clip(dark, 0, 255).round().astype(int).tolist()
    palette['inside'] = (mean*.52).round().astype(int).tolist()
    palette['pale'] = np.clip(mean*.88+24, 0, 255).round().astype(int).tolist()
    atlas = Image.new('RGB', (64, 64), (43, 42, 62))
    atlas.paste(wood, (0, 0))
    # Execute only the native material loop; no geometry regeneration or writes.
    loop = next(n for n in module.body if isinstance(n, ast.For))
    scope = {'texim': atlas, 'materials': materials, 'colors': palette}
    exec(compile(ast.Module(body=[loop], type_ignores=[]), str(script), 'exec'), scope)
    atlas.paste(wood, (0, 32))
    atlas.paste(wood, (16, 32))
    return atlas


def rotation(angles):
    x,y,z = map(math.radians, angles)
    cx,sx,cy,sy,cz,sz = math.cos(x),math.sin(x),math.cos(y),math.sin(y),math.cos(z),math.sin(z)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]]) @ np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]) @ np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])


FACES = {
    'north': ([1,0,3,2],[0,0,-1]), 'south': ([4,5,6,7],[0,0,1]),
    'west': ([0,4,7,3],[-1,0,0]), 'east': ([5,1,2,6],[1,0,0]),
    'up': ([7,6,2,3],[0,1,0]), 'down': ([0,1,5,4],[0,-1,0]),
}


def surfaces(model, opened=False):
    groups = {key: group for group in model['outliner'] for key in group['children']}
    for e in model['elements']:
        x,y,z = e['from']; X,Y,Z = e['to']; origin = np.array(e['origin'])
        r = rotation(e['rotation']); group = groups[e['uuid']]
        hinge = np.array(group['origin'])
        lid = rotation((-100,0,0)) if opened and group['name'].startswith('lid') else np.eye(3)
        points = np.array([[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]])
        points = ((points-origin) @ r.T + origin-hinge) @ lid.T + hinge
        for name, face in e['faces'].items():
            ids, normal = FACES[name]
            u,v,U,V = face['uv']
            yield points[ids], lid @ r @ normal, np.array([[u,V],[U,V],[U,v],[u,v]]), face.get('texture', 0)


def render(model, atlas, width, height, opened=False):
    scene = list(surfaces(model, opened)); camera = rotation((18,-24,0))
    positions = np.concatenate([v @ camera.T for v,n,uv,slot in scene])
    lo,hi = positions[:,:2].min(0),positions[:,:2].max(0)
    center = (lo+hi)/2
    scale = min((width-48)/(hi[0]-lo[0]),(height-36)/(hi[1]-lo[1]))
    pixels = np.zeros((height,width,4),dtype=np.uint8)
    depth = np.full((height,width),-np.inf)
    textures = [np.asarray(a) for a in atlas] if isinstance(atlas, tuple) else [np.asarray(atlas)]
    for vertices,normal,uv,slot in scene:
        texture = textures[slot]
        uv = uv * np.array([texture.shape[1]/64, texture.shape[0]/64])
        n = camera @ normal
        if n[2] <= 0: continue
        vertices = vertices @ camera.T
        vertices[:,0] = (vertices[:,0]-center[0])*scale+width/2
        vertices[:,1] = -(vertices[:,1]-center[1])*scale+height/2
        shade = .64+.38*max(0,float(n @ np.array([-.35,.6,.72])))
        for ids in ([0,1,2],[0,2,3]):
            v=vertices[ids]; t=uv[ids]
            xmin=max(0,int(v[:,0].min())); xmax=min(width-1,int(np.ceil(v[:,0].max())))
            ymin=max(0,int(v[:,1].min())); ymax=min(height-1,int(np.ceil(v[:,1].max())))
            if xmax<xmin or ymax<ymin: continue
            X,Y=np.meshgrid(np.arange(xmin,xmax+1)+.5,np.arange(ymin,ymax+1)+.5)
            a,b,c=v
            den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
            if abs(den)<1e-9:continue
            A=((b[1]-c[1])*(X-c[0])+(c[0]-b[0])*(Y-c[1]))/den
            B=((c[1]-a[1])*(X-c[0])+(a[0]-c[0])*(Y-c[1]))/den
            C=1-A-B; Z=A*a[2]+B*b[2]+C*c[2]
            region=depth[ymin:ymax+1,xmin:xmax+1]
            mask=(A>=-1e-7)&(B>=-1e-7)&(C>=-1e-7)&(Z>region)
            U=np.clip((A*t[0,0]+B*t[1,0]+C*t[2,0]).astype(int),0,texture.shape[1]-1)
            V=np.clip((A*t[0,1]+B*t[1,1]+C*t[2,1]).astype(int),0,texture.shape[0]-1)
            target=pixels[ymin:ymax+1,xmin:xmax+1]
            target[:,:,:3][mask]=np.clip(texture[V,U]*shade,0,255).astype(np.uint8)[mask]
            target[:,:,3][mask]=255;region[mask]=Z[mask]
    return Image.fromarray(pixels)


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    sheet=Image.new('RGB',(1600,1560),'#111a24');draw=ImageDraw.Draw(sheet)
    draw.text((38,24),'极光 · 四种木材的带锁箱子',font=font(37),fill='#ecf4ff')
    draw.text((40,82),'沿用带锁造型与细节 · 对应木板配色 · 单箱 / 双箱',font=font(22),fill='#9fb6c9')
    original=Image.open(REPO/'exports/aurorian_double_wood_chest/aurorian_chest.png').convert('RGB')
    for row,(key,label,texture) in enumerate(WOODS):
        folder=OUT/key;folder.mkdir(exist_ok=True)
        wood=Image.open(BLOCKS/(texture+'.png')).convert('RGB')
        assert wood.size==(16,16)
        if key=='filthy':
            wood=shared_wood_grain(wood)
        atlas=material_atlas(wood);atlas.save(folder/'chest.png')
        for box in [(48,0,64,16),(16,16,32,32),(32,16,48,32)]:
            assert atlas.crop(box).tobytes()==original.crop(box).tobytes(), 'Iron/keyhole material changed'
        buffer=io.BytesIO();atlas.save(buffer,format='PNG')
        embedded='data:image/png;base64,'+base64.b64encode(buffer.getvalue()).decode()
        rendered_models = {}
        for variant,source in MODELS.items():
            model=copy.deepcopy(source)
            model['name']=label+('单箱' if variant=='single' else '双箱')+{'double_left':' · 左半','double_right':' · 右半'}.get(variant,'')
            model['model_identifier']=key+'_chest_'+variant
            model['textures'][0]['name']='chest.png';model['textures'][0]['source']=embedded
            assert model['elements']==source['elements'] and model['outliner']==source['outliner']
            assert model['animations']==source['animations']
            rendered_models[variant]=model
            (folder/(key+'_chest_'+variant+'.bbmodel')).write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf8')
        y=130+row*350
        draw.rounded_rectangle((25,y,1575,y+330),18,fill='#1e2a38')
        draw.text((48,y+20),label,font=font(30),fill='#e6eef6')
        draw.text((48,y+63),'带铁锁',font=font(20),fill='#99afc1')
        swatch=wood.resize((80,80),Image.Resampling.NEAREST);sheet.paste(swatch,(60,y+118))
        for variant,x,w in [('single',205,450),('double',695,835)]:
            im=render(rendered_models[variant],atlas,w,290);sheet.paste(im,(x,y+28),im)
        detail=Image.new('RGB',(1300,1120),'#111a24');d=ImageDraw.Draw(detail)
        d.text((35,22),label+'箱子 · 带锁版',font=font(34),fill='#ecf4ff')
        d.text((38,76),'保留铁色锁扣、木纹内衬、包带与把手细节',font=font(21),fill='#9fb6c9')
        for r,variant in enumerate(('single','double')):
            for c,opened in enumerate((False,True)):
                x=20+c*645; yy=120+r*495
                d.rounded_rectangle((x,yy,x+625,yy+475),16,fill='#1e2a38')
                im=render(rendered_models[variant],atlas,605,410,opened)
                detail.paste(im,(x+10,yy+10),im)
                d.text((x+24,yy+430),('单箱' if r==0 else '双箱')+(' · 开盖' if opened else ' · 闭合'),font=font(22),fill='#c3d6e9')
        detail.save(folder/'overview.png')
        material_note = '保留模型结构、转轴、UV、铁锁和已完成的细节；仅材质配色改变。\n'
        if key=='filthy':
            material_note += '木纹使用与其他箱子一致的谧木像素排列，逐色替换成污秽木板原有的七色；保持 16×16 像素密度。\n'
        (folder/'README.txt').write_text(label+'箱子：单箱、完整双箱、左半、右半四个 Blockbench 模型。\n模型使用嵌入贴图，附独立 PNG；含原有开盖动画。\n'+material_note+'当前为独立模型预览，未注册进游戏。\n',encoding='utf8')
        print(key+': 4 models; geometry/animation and original iron preserved')
    sheet.save(OUT/'overview.png')
    (OUT/'README.txt').write_text('极光其他木材带锁箱子\n垂柳、幽帘、咒霜、污秽四套；共 16 个 Blockbench 模型。\n各目录附单箱/双箱、闭合/开盖预览。贴图使用现有木板；包带、把手和内衬匹配对应木色，铁锁保持原色。\n未接入游戏，也未更改原有谧木箱或无锁版。\n',encoding='utf8')


if __name__=='__main__':
    main()
