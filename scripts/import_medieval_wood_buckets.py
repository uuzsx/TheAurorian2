"""Import ShizuArt seau/seau_remplie verbatim, replacing only their brown wood ramp."""
import copy
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from import_medieval_wood_furniture import ROOT, A, SOURCE, WOODS, bbmodel
import export_wood_chests as renderer

OUT = ROOT / 'exports/wood_buckets'

def main():
    models = [json.loads((SOURCE / f'models/sdb_medievale/{name}.json').read_text(encoding='utf8'))
              for name in ('seau', 'seau_remplie')]
    original = Image.open(SOURCE / 'textures/sdb_medievale/baquet2.png').convert('RGBA')
    assert original.size == (128, 128)
    assert len(models[0]['elements']) == 8 and len(models[1]['elements']) == 9
    assert models[1]['elements'][:1] + models[1]['elements'][2:] == models[0]['elements']
    colors = set()
    for model in models:
        for element in model['elements']:
            assert not element.get('rotation', {}).get('angle', 0)
            for face in element['faces'].values():
                assert face['texture'] == '#0' and face.get('rotation', 0) in (0, 90, 180, 270)
                assert all(0 <= value <= 16 for value in face['uv'])
                u, v, U, V = face['uv']
                for y in range(math.floor(min(v, V)*8), math.ceil(max(v, V)*8)):
                    for x in range(math.floor(min(u, U)*8), math.ceil(max(u, U)*8)):
                        r, g, b, alpha = original.getpixel((x, y))
                        # Brown wood only; retain the original neutral iron bands and blue water.
                        if alpha and r > g > b: colors.add((r, g, b))
    colors = sorted(colors, key=lambda c: (sum(c), c))
    written = []
    def write(path, data):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, ensure_ascii=False, separators=(',', ':')), encoding='utf8')
        written.append(str(path))
    sheet = Image.new('RGB', (1400, 1600), '#111a24')
    draw = ImageDraw.Draw(sheet)
    font = lambda n: ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', n)
    draw.text((32, 20), '五种木材 · 中世纪木桶', font=font(34), fill='#e6eef6')
    draw.text((34, 76), '右键取水 / 倒水 · 潜行右键摆放 · 原模型与木纹 / 对应木板采色', font=font(21), fill='#a2b9c7')
    for row, (key, label, plank) in enumerate(WOODS):
        name = key + '_bucket'
        palette = sorted(set(Image.open(A / f'textures/block/{plank}.png').convert('RGB').get_flattened_data()), key=lambda c: (sum(c), c))
        mapping = {c: palette[round(i * (len(palette)-1)/(len(colors)-1))] for i, c in enumerate(colors)}
        atlas = Image.new('RGBA', original.size)
        atlas.putdata([(*mapping.get(p[:3], p[:3]), p[3]) for p in original.get_flattened_data()])
        folder = OUT / key
        folder.mkdir(parents=True, exist_ok=True)
        atlas.save(folder / 'medieval_furniture.png')
        atlas.save(A / f'textures/block/{name}.png')
        png = (folder / 'medieval_furniture.png').read_bytes()
        y = 125 + row * 290
        draw.rounded_rectangle((20, y, 1380, y+275), 18, fill='#1c2835')
        draw.text((42, y+22), label, font=font(28), fill='#e6eef6')
        for filled, source in enumerate(models):
            model_name = name + ('_water' if filled else '')
            model = copy.deepcopy(source)
            model.update(parent='minecraft:block/block', render_type='minecraft:cutout',
                         textures={'0': f'theaurorian2:block/{name}', 'particle': f'theaurorian2:block/{plank}'})
            model.pop('groups', None)
            assert model['elements'] == source['elements'] and model['display'] == source['display']
            write(A / f'models/block/{model_name}.json', model)
            write(A / f'items/{model_name}.json', {'model': {'type': 'minecraft:model', 'model': f'theaurorian2:block/{model_name}'}})
            bb = bbmodel(source['elements'], label + ('木桶（装水）' if filled else '木桶（空）'), png)
            for cube, element in zip(bb['elements'], source['elements']):
                if 'rotation' in element: cube['origin'] = copy.deepcopy(element['rotation']['origin'])
            bb['display'] = copy.deepcopy(source['display'])
            write(folder / f'{model_name}.bbmodel', bb)
            image = renderer.render(bb, atlas, 420, 255)
            sheet.paste(image, (220 + filled*570, y+6), image)
            draw.text((620 + filled*570, y+228), '装水' if filled else '空桶', font=font(20), fill='#a2b9c7')
        variants = {}
        for facing, rotation in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
            for filled in (False, True):
                variants[f'facing={facing},filled={str(filled).lower()}'] = {'model': f'theaurorian2:block/{name}' + ('_water' if filled else ''), 'y': rotation}
        write(A / f'blockstates/{name}.json', {'variants': variants})
        entries = [{'type': 'minecraft:item', 'name': f'theaurorian2:{name}_water', 'conditions': [{'condition': 'minecraft:block_state_property', 'block': f'theaurorian2:{name}', 'properties': {'filled': 'true'}}]},
                   {'type': 'minecraft:item', 'name': f'theaurorian2:{name}'}]
        write(ROOT / f'src/main/resources/data/theaurorian2/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}], 'entries': [{'type': 'minecraft:alternatives', 'children': entries}]}]})
        write(ROOT / f'src/main/resources/data/theaurorian2/recipe/{name}.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['P P', ' P '], 'key': {'P': f'theaurorian2:{plank}'}, 'result': {'id': f'theaurorian2:{name}', 'count': 1}})
    for locale in ('zh_cn', 'en_us'):
        path = A / f'lang/{locale}.json'
        data = json.loads(path.read_text(encoding='utf8'))
        for key, label, _ in WOODS:
            english = key.replace('_', ' ').title()
            data[f'block.theaurorian2.{key}_bucket'] = label + '桶' if locale == 'zh_cn' else english + ' Bucket'
            data[f'item.theaurorian2.{key}_bucket_water'] = label + '桶（装水）' if locale == 'zh_cn' else english + ' Water Bucket'
        write(path, data)
    path = ROOT / 'src/main/resources/data/minecraft/tags/block/mineable/axe.json'
    data = json.loads(path.read_text(encoding='utf8'))
    for key, _, _ in WOODS:
        value = f'theaurorian2:{key}_bucket'
        if value not in data['values']: data['values'].append(value)
    write(path, data)
    sheet.save(OUT / 'overview.png')
    (OUT / 'generated-files.txt').write_text('\n'.join(written), encoding='utf8')
    (OUT / 'README.txt').write_text('原模型：ShizuArt seau / seau_remplie（用户提供的中世纪浴室家具包）。\n保留原几何、反向内表面、透明像素、UV、展示变换和水面。木色采自对应木板，保留原深色桶箍与蓝色水面。\n普通右键取水、倒水；潜行右键放置。空桶堆叠16，装水桶堆叠1；打掉保留水状态。\n用对应木板按铁桶形状合成。仅装水，不装岩浆或细雪。原始授权继续适用。\n', encoding='utf8')

if __name__ == '__main__': main()
