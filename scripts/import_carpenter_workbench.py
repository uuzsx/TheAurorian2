"""Import blacksmith table 1 using the existing Aurorian crafting-table palette."""
import base64
import copy
import itertools
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from import_blacksmith_storage_barrels import ROOT, SOURCE, A, D
from import_medieval_wood_furniture import shift
import export_wood_chests as renderer

OUT = ROOT / 'exports/carpenter_workbench'
NAME = 'carpenter_workbench'

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    written = []
    def write(path, value):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value, ensure_ascii=False, separators=(',', ':')), encoding='utf8')
        written.append(str(path))
    source = json.loads((SOURCE/'Json Files/blacksmith_table_1_2x1.json').read_text(encoding='utf-8-sig'))
    bb = json.loads((SOURCE/'Blockbench/blacksmith_table_1_2x1.bbmodel').read_text(encoding='utf-8-sig'))
    original = Image.open(SOURCE/'Texture Files/blacksmith.png').convert('RGBA')
    reference = Image.open(A/'textures/block/aurorian_crafting_table_model.png').convert('RGBA')
    assert len(source['elements']) == len(bb['elements']) == 18 and original.size == (256, 256)
    def colors(indices):
        found = set()
        for i in indices:
            for f in source['elements'][i]['faces'].values():
                assert f['texture'] == '#0'
                u,v,U,V = f['uv']
                for x in range(math.floor(min(u,U)*16), math.ceil(max(u,U)*16)):
                    for y in range(math.floor(min(v,V)*16), math.ceil(max(v,V)*16)):
                        found.add(original.getpixel((x,y))[:3])
        return sorted(found, key=sum)
    wood_ramp = [(112,90,121),(122,126,158),(141,149,165),(163,173,194),(188,209,226)]
    ramps = [(range(10), wood_ramp), ([15,16], wood_ramp)]
    ref_colors = set(reference.getdata())
    mapping = {}
    for indices, ramp in ramps:
        source_colors = colors(indices)
        assert all((*c,255) in ref_colors for c in ramp)
        mapping.update({c:ramp[round(i*(len(ramp)-1)/(len(source_colors)-1))] for i,c in enumerate(source_colors)})
    # The original metal heads and paper must remain byte-for-byte unchanged.
    assert not set(colors([10,11,12,13,14,17])).intersection(mapping)
    atlas = Image.new('RGBA', original.size)
    atlas.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in original.getdata()])
    assert atlas.getchannel('A').tobytes() == original.getchannel('A').tobytes()
    texture = A/f'textures/block/{NAME}.png';atlas.save(texture)
    atlas.save(OUT/f'{NAME}.png')
    model = copy.deepcopy(source)
    model.update(parent='minecraft:block/block', render_type='minecraft:cutout',
                 textures={'0':f'theaurorian2:block/{NAME}','particle':'theaurorian2:block/aurorian_crafting_table_particle'})
    write(A/f'models/item/{NAME}.json', model)
    elements = shift(source['elements'], 16)
    for second in (False,True):
        low = 16 if second else 0; high = low+16; half = []
        for index, original_element in enumerate(elements):
            a,b = original_element['from'][0],original_element['to'][0]
            # Keep this narrow cross-brace whole, including its rotated UVs, on the first half.
            if index == 9:
                if not second: half.append(copy.deepcopy(original_element))
                continue
            if b <= low or a >= high: continue
            e = copy.deepcopy(original_element)
            if a < low or b > high:
                assert index in (4,5,6) and not e.get('rotation',{}).get('angle',0)
                left,right = max(a,low),min(b,high)
                t,T = (left-a)/(b-a),(right-a)/(b-a)
                e['from'][0] = left;e['to'][0] = right
                for direction,face in e['faces'].items():
                    if direction in ('north','south','up','down'):
                        assert not face.get('rotation',0)
                        u,v,U,V=face['uv'];p,q=(1-T,1-t) if direction=='north' else (t,T)
                        face['uv']=[u+(U-u)*p,v,u+(U-u)*q,V]
                e['faces'].pop('west' if second else 'east',None)
            half.extend(shift([e],-low))
        part = {k:copy.deepcopy(v) for k,v in model.items() if k not in ('elements','display')}
        part['elements'] = half
        write(A/f'models/block/{NAME}_{str(second).lower()}.json',part)
    write(A/f'blockstates/{NAME}.json', {'variants':{
        f'facing={direction},second={second}':{'model':f'theaurorian2:block/{NAME}_{second}','y':angle}
        for direction,angle in [('north',0),('east',90),('south',180),('west',270)] for second in ('false','true')}})
    write(A/f'items/{NAME}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{NAME}'}})
    loot = json.loads((D/'theaurorian2/loot_table/blocks/silent_wood_long_table.json').read_text(encoding='utf8'))
    loot = json.loads(json.dumps(loot).replace('silent_wood_long_table',NAME))
    write(D/f'theaurorian2/loot_table/blocks/{NAME}.json',loot)
    for locale,title in [('zh_cn','木匠工作台'),('en_us',"Carpenter's Workbench")]:
        path=A/f'lang/{locale}.json';data=json.loads(path.read_text(encoding='utf8'))
        data[f'block.theaurorian2.{NAME}']=title;write(path,data)
    path=D/'minecraft/tags/block/mineable/axe.json';data=json.loads(path.read_text(encoding='utf8'))
    if f'theaurorian2:{NAME}' not in data['values']: data['values'].append(f'theaurorian2:{NAME}')
    write(path,data)
    bb['name']='木匠工作台'
    bb['textures'][0].update(source='data:image/png;base64,'+base64.b64encode(texture.read_bytes()).decode(),
                            name=f'{NAME}.png',path=f'{NAME}.png',relative_path=f'{NAME}.png')
    write(OUT/f'{NAME}.bbmodel',bb)
    preview=copy.deepcopy(bb)
    for e in preview['elements']: e.setdefault('rotation',[0,0,0]);e.setdefault('origin',[0,0,0])
    preview['outliner']=[{'name':'root','origin':[0,0,0],'children':[e['uuid'] for e in preview['elements']]}]
    sheet=Image.new('RGB',(1400,760),'#111923');draw=ImageDraw.Draw(sheet)
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((32,22),'木匠工作台 · 原工作台 1 模型',font=font(34),fill='#edf3fa')
    draw.text((34,78),'极光工作台采色 · 保留工具与图纸 · 两格摆放',font=font(22),fill='#a7bac8')
    for x,angles in [(15,(24,-30,0)),(710,(24,145,0))]:
        im=renderer.render(preview,atlas,665,550,camera_angles=angles);sheet.paste(im,(x,145),im)
    sheet.save(OUT/'overview.png')
    # Offline bounds for the cached collision shape; never parse model resources in-game.
    bounds=[]
    for e in elements:
        if any(a==b for a,b in zip(e['from'],e['to'])): continue
        points=list(itertools.product(*zip(e['from'],e['to'])))
        rot=e.get('rotation',{});angle=math.radians(rot.get('angle',0));ox,oy,oz=rot.get('origin',[0,0,0])
        assert rot.get('axis','y')=='y'
        points=[(ox+math.cos(angle)*(x-ox)+math.sin(angle)*(z-oz),y,oz-math.sin(angle)*(x-ox)+math.cos(angle)*(z-oz)) for x,y,z in points]
        bounds.append([min(p[i] for p in points) for i in range(3)]+[max(p[i] for p in points) for i in range(3)])
    (OUT/'collision-bounds.txt').write_text(',\n'.join('box('+', '.join(f'{n:.6f}'.rstrip('0').rstrip('.') for n in b)+')' for b in bounds),encoding='utf8')
    (OUT/'generated-files.txt').write_text('\n'.join(written)+'\n',encoding='utf8')

if __name__=='__main__': main()
