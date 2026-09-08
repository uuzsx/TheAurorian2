"""Preserve the supplied ceramic jug; recolor its ceramic ramp from aurorian_brick."""
import base64
import copy
import json
import uuid
from PIL import Image, ImageDraw, ImageFont
from import_medieval_wood_furniture import ROOT, A, SOURCE
import export_wood_chests as renderer

OUT = ROOT / 'exports/aurorian_jug'

def main():
    source = json.loads((SOURCE / 'models/sdb_medievale/cruche_terre_cuite.json').read_text(encoding='utf8'))
    original = Image.open(SOURCE / 'textures/sdb_medievale/cruche_terre_cuite.png').convert('RGBA')
    brick = Image.open(A / 'textures/item/aurorian_brick.png').convert('RGBA')
    assert original.size == (32,32) and len(source['elements']) == 6
    assert source['elements'][-1]['from'][1] == source['elements'][-1]['to'][1] == 9.499
    for element in source['elements']:
        assert element['rotation']['angle'] == 0
        for face in element['faces'].values():
            assert face['texture'] == '#0' and face.get('rotation',0) in (0,90,180,270)
            assert all(0 <= n <= 16 for n in face['uv'])
    ceramic = sorted({p[:3] for p in original.get_flattened_data() if p[3] and p[0] > p[1] > p[2]}, key=sum)
    palette = sorted({p[:3] for p in brick.get_flattened_data() if p[3]}, key=sum)
    assert len(ceramic) == 5 and len(palette) == 6
    mapping = {c:palette[round(i*(len(palette)-1)/(len(ceramic)-1))] for i,c in enumerate(ceramic)}
    atlas = Image.new('RGBA', original.size)
    atlas.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in original.get_flattened_data()])
    assert atlas.getchannel('A').tobytes() == original.getchannel('A').tobytes()
    OUT.mkdir(parents=True,exist_ok=True)
    atlas.save(OUT / 'aurorian_jug.png')
    atlas.save(A / 'textures/block/aurorian_jug.png')
    png = (OUT / 'aurorian_jug.png').read_bytes()
    written = []
    def write(path,data):
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8')
        written.append(str(path))
    sheet = Image.new('RGB',(1250,680),'#111a24')
    draw = ImageDraw.Draw(sheet)
    font = lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((30,22),'凝月水壶 · 中世纪陶壶原模型',font=font(32),fill='#e6eef6')
    draw.text((32,77),'凝月砖采色 / 保留像素纹理与把手 · 水瓶容量 / 炼药锅每次一层，潜行右键摆放',font=font(20),fill='#a2b9c7')
    for filled in (False,True):
        name = 'aurorian_jug' + ('_water' if filled else '')
        elements = copy.deepcopy(source['elements'] if filled else source['elements'][:-1])
        model = copy.deepcopy(source)
        model.pop('groups',None)
        model.update(parent='minecraft:block/block',render_type='minecraft:translucent' if filled else 'minecraft:cutout',
                     textures={'0':'theaurorian2:block/aurorian_jug','particle':'theaurorian2:block/aurorian_bricks'},elements=elements)
        write(A / f'models/block/{name}.json',model)
        write(A / f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:block/{name}'}})
        cubes = []
        for i,element in enumerate(elements):
            cube = copy.deepcopy(element)
            cube.update(name='水面' if i == 5 else f'陶壶部件{i}',type='cube',box_uv=False,
                        uuid=str(uuid.uuid5(uuid.NAMESPACE_URL,f'aurorian/jug/{i}')),
                        origin=copy.deepcopy(element['rotation']['origin']),rotation=[0,0,0])
            for face in cube['faces'].values():
                face['texture'] = 0
                face['uv'] = [v*2 for v in face['uv']]
            cubes.append(cube)
        bb = {'meta':{'format_version':'4.10','model_format':'java_block','box_uv':False},
              'name':'凝月水壶'+('（装水）' if filled else '（空）'),'credit':source['credit']+'; supplied ShizuArt medieval pack; Aurorian brick palette',
              'resolution':{'width':32,'height':32},'elements':cubes,'display':copy.deepcopy(source['display']),
              'gui_light':source['gui_light'],'textures':[{'name':'aurorian_jug.png','id':'0','width':32,'height':32,'uv_width':32,'uv_height':32,
              'source':'data:image/png;base64,'+base64.b64encode(png).decode()}]}
        def group(node,path):
            if isinstance(node,int):return cubes[node]['uuid'] if node<len(cubes) else None
            result=copy.deepcopy(node)
            result['uuid']=str(uuid.uuid5(uuid.NAMESPACE_URL,'aurorian/jug/'+path))
            result['children']=[value for i,child in enumerate(node['children']) if (value:=group(child,path+'/'+str(i))) is not None]
            return result
        bb['outliner']=[group(g,str(i)) for i,g in enumerate(source['groups'])]
        write(OUT / f'{name}.bbmodel',bb)
        # The preview renderer accepts a flat group; exported hierarchy above stays intact.
        preview=copy.deepcopy(bb)
        preview['outliner']=[{'name':'body','origin':[8,8,8],'children':[c['uuid'] for c in cubes]}]
        im=renderer.render(preview,atlas,520,405)
        x=30+int(filled)*610
        draw.rounded_rectangle((x,128,x+580,598),18,fill='#1c2835')
        sheet.paste(im,(x+25,140),im)
        draw.text((x+220,550),'装水' if filled else '空壶',font=font(24),fill='#e6eef6')
    for i,color in enumerate(palette):draw.rectangle((38+i*35,624,68+i*35,654),fill=color)
    draw.text((275,624),'取自凝月砖物品贴图',font=font(20),fill='#a2b9c7')
    sheet.save(OUT / 'overview.png')
    write(A / 'blockstates/aurorian_jug.json',{'variants':{
        f'facing={facing},filled={str(filled).lower()}':{'model':'theaurorian2:block/aurorian_jug'+('_water' if filled else ''),'y':rotation}
        for facing,rotation in [('north',0),('east',90),('south',180),('west',270)] for filled in (False,True)}})
    write(ROOT / 'src/main/resources/data/theaurorian2/recipe/aurorian_jug.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['B B','BBB'],
          'key':{'B':'theaurorian2:aurorian_brick'},'result':{'id':'theaurorian2:aurorian_jug','count':1}})
    write(ROOT / 'src/main/resources/data/theaurorian2/loot_table/blocks/aurorian_jug.json',{'type':'minecraft:block','pools':[{'rolls':1,
          'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:alternatives','children':[
          {'type':'minecraft:item','name':'theaurorian2:aurorian_jug_water','conditions':[{'condition':'minecraft:block_state_property','block':'theaurorian2:aurorian_jug','properties':{'filled':'true'}}]},
          {'type':'minecraft:item','name':'theaurorian2:aurorian_jug'}]}]}]})
    for locale,title,full in [('zh_cn','凝月水壶','凝月水壶（装水）'),('en_us','Aurorian Jug','Aurorian Water Jug')]:
        path=A/f'lang/{locale}.json';data=json.loads(path.read_text(encoding='utf8'))
        data['block.theaurorian2.aurorian_jug']=title;data['item.theaurorian2.aurorian_jug_water']=full
        write(path,data)
    path=ROOT/'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(path.read_text(encoding='utf8'))
    if 'theaurorian2:aurorian_jug' not in data['values']:data['values'].append('theaurorian2:aurorian_jug')
    write(path,data)
    (OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
    (OUT/'README.txt').write_text('凝月水壶使用用户提供的 ShizuArt 中世纪浴室家具包 cruche_terre_cuite 原模型。\n陶色直接取自凝月砖物品 aurorian_brick.png；保留32×32原像素密度、全部透明度、原水面蓝色、反向内壁、把手面片、UV、旋转中心和展示参数。\n空壶仅隐藏原水面，装水壶完整保留原模型，使用半透明渲染保留水面alpha。\n容量对标原版水瓶：取水不移除水源，每壶给炼药锅增减一层水，三壶注满；不向地面倒出水源，也不灌水到含水方块。Shift+右键摆放；破坏保留空/满状态，空壶叠16、满壶叠1。5块凝月砖合成1个水壶。\n原素材授权继续适用，见 THIRD_PARTY_NOTICES.md。\n',encoding='utf8')

if __name__ == '__main__':main()
