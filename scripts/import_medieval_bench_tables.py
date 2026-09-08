"""Import the supplied banc/petite_table, reusing the existing five plank atlases."""
import copy
import json
import math
import uuid
from PIL import Image, ImageDraw, ImageFont
from import_medieval_wood_furniture import ROOT, A, SOURCE, WOODS, load_sources, shift, half_table, block_model, bbmodel
import export_wood_chests as renderer

OUT = ROOT / 'exports/medieval_bench_tables'

def main():
    models = {kind: json.loads((SOURCE / f'models/sdb_medievale/{name}.json').read_text(encoding='utf8'))
              for kind, name in [('bench', 'banc'), ('small_table', 'petite_table')]}
    _, original, wood_colors = load_sources()
    for model in models.values():
        assert len(model['elements']) == 5
        for element in model['elements']:
            assert all(a != b for a, b in zip(element['from'], element['to']))
            assert not element.get('rotation', {}).get('angle', 0)
            for face in element['faces'].values():
                assert face['texture'] == '#0' and face.get('rotation', 0) in (0, 90, 180, 270)
                assert all(0 <= n <= 16 for n in face['uv'])
                u, v, U, V = face['uv']
                for y in range(math.floor(min(v,V)*8), math.ceil(max(v,V)*8)):
                    for x in range(math.floor(min(u,U)*8), math.ceil(max(u,U)*8)):
                        color = original.getpixel((x,y))
                        assert not color[3] or color[:3] in wood_colors
    written = []
    def write(path, data):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, ensure_ascii=False, separators=(',', ':')), encoding='utf8')
        written.append(str(path))
    sheet = Image.new('RGB', (1500, 1550), '#111a24')
    draw = ImageDraw.Draw(sheet)
    font = lambda n: ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', n)
    draw.text((32, 22), '五种木材 · 中世纪长凳与小方桌', font=font(34), fill='#e6eef6')
    draw.text((34, 78), '原模型与木纹 / 对应木板配色 · 长凳双人座、向右占两格 · 小方桌一格', font=font(21), fill='#a2b9c7')
    for row, (key, label, plank) in enumerate(WOODS):
        texture = A / f'textures/block/{key}_medieval_furniture.png'
        atlas = Image.open(texture).convert('RGBA')
        assert atlas.size == original.size and atlas.getchannel('A').tobytes() == original.getchannel('A').tobytes()
        palette = sorted(set(Image.open(A / f'textures/block/{plank}.png').convert('RGB').get_flattened_data()), key=lambda c: (sum(c), c))
        mapping = {c: palette[round(i*6/(len(wood_colors)-1))] for i,c in enumerate(wood_colors)}
        assert list(atlas.get_flattened_data()) == [(*mapping.get(p[:3], p[:3]), p[3]) for p in original.get_flattened_data()]
        folder = OUT / key
        folder.mkdir(parents=True, exist_ok=True)
        (folder / 'medieval_furniture.png').write_bytes(texture.read_bytes())
        y = 120 + row*280
        draw.rounded_rectangle((20,y,1480,y+262), 18, fill='#1c2835')
        draw.text((40,y+18), label, font=font(26), fill='#e6eef6')
        for kind, title in [('bench', '长凳'), ('small_table', '小方桌')]:
            name = key + '_' + kind
            source = models[kind]
            variants = {}
            if kind == 'bench':
                elements = shift(source['elements'], 16)
                for second in (False, True):
                    write(A / f'models/block/{name}_{str(second).lower()}.json', block_model(half_table(elements, second), key, plank))
            else:
                model = copy.deepcopy(source)
                model.pop('groups', None)
                model.update(block_model(source['elements'], key, plank))
                write(A / f'models/block/{name}.json', model)
            for facing, rotation in [('north',0), ('east',90), ('south',180), ('west',270)]:
                if kind == 'bench':
                    for second in (False, True):
                        variants[f'facing={facing},second={str(second).lower()}'] = {'model': f'theaurorian2:block/{name}_{str(second).lower()}', 'y': rotation}
                else:
                    variants[f'facing={facing}'] = {'model': f'theaurorian2:block/{name}', 'y': rotation}
            write(A / f'blockstates/{name}.json', {'variants': variants})
            if kind == 'bench':
                item = copy.deepcopy(source)
                item.pop('groups', None)
                item.update(block_model(source['elements'], key, plank))
                write(A / f'models/item/{name}.json', item)
            write(A / f'items/{name}.json', {'model': {'type':'minecraft:model', 'model':f'theaurorian2:{"item" if kind == "bench" else "block"}/{name}'}})
            export = bbmodel(source['elements'], label+title, texture.read_bytes())
            preview = copy.deepcopy(export)
            export['display'] = copy.deepcopy(source['display'])
            if 'gui_light' in source: export['gui_light'] = source['gui_light']
            # Keep the authored hierarchy and pivots in the editable Blockbench project.
            def group(node, path):
                if isinstance(node, int): return export['elements'][node]['uuid']
                result = copy.deepcopy(node)
                result['uuid'] = str(uuid.uuid5(uuid.NAMESPACE_URL, name + '/' + path))
                result['children'] = [group(child, path + '/' + str(i)) for i,child in enumerate(node['children'])]
                return result
            export['outliner'] = [group(g, str(i)) for i,g in enumerate(source['groups'])]
            write(folder / f'{name}.bbmodel', export)
            image = renderer.render(preview, atlas, 650 if kind == 'bench' else 410, 220)
            sheet.paste(image, (185 if kind == 'bench' else 960, y+25), image)
            draw.text((475 if kind == 'bench' else 1090,y+228),title,font=font(19),fill='#a2b9c7')
            conditions = [{'condition':'minecraft:survives_explosion'}]
            if kind == 'bench': conditions.insert(0, {'condition':'minecraft:block_state_property','block':f'theaurorian2:{name}','properties':{'second':'false'}})
            write(ROOT / f'src/main/resources/data/theaurorian2/loot_table/blocks/{name}.json', {'type':'minecraft:block','pools':[{'rolls':1,'conditions':conditions,'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}'}]}]})
            write(ROOT / f'src/main/resources/data/theaurorian2/recipe/{name}.json', {'type':'minecraft:crafting_shaped','category':'building','pattern':['PPP','P P'] if kind == 'bench' else ['PP','SS','SS'],'key':{'P':f'theaurorian2:{plank}', **({'S':'#c:rods/wooden'} if kind == 'small_table' else {})},'result':{'id':f'theaurorian2:{name}','count':1}})
    for locale in ('zh_cn', 'en_us'):
        path = A / f'lang/{locale}.json'
        data = json.loads(path.read_text(encoding='utf8'))
        for key,label,_ in WOODS:
            for kind,title,english in [('bench','长凳','Bench'),('small_table','小方桌','Small Table')]:
                data[f'block.theaurorian2.{key}_{kind}'] = label+title if locale == 'zh_cn' else key.replace('_',' ').title()+' '+english
        write(path,data)
    path = ROOT / 'src/main/resources/data/minecraft/tags/block/mineable/axe.json'
    data = json.loads(path.read_text(encoding='utf8'))
    for key,_,_ in WOODS:
        for kind in models:
            value = f'theaurorian2:{key}_{kind}'
            if value not in data['values']: data['values'].append(value)
    write(path,data)
    sheet.save(OUT / 'overview.png')
    (OUT / 'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
    (OUT / 'README.txt').write_text('原作者 ShizuArt，使用用户提供的中世纪浴室家具包 banc / petite_table。\n保留原几何、反向内表面、UV、透明像素、木纹和物品展示参数。共用现有五种木材 medieval_furniture 贴图。\n长凳32×7×12，放置向右占两格，两个独立座位，右键坐、Shift起身；破坏任意半边只掉落一件。\n小方桌16×16×16，占一格。配方：长凳上三块木板、下两端木板；小方桌上两块木板、下面两排木棍。\n原始授权继续适用，见 THIRD_PARTY_NOTICES.md。\n',encoding='utf8')

if __name__ == '__main__': main()
