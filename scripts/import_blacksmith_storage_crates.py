"""Three Magic Store box arrangements, five woods, vanilla barrel storage."""
import base64
import copy
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from import_blacksmith_storage_barrels import ROOT, SOURCE, A, D, WOODS
import export_wood_chests as renderer

OUT=ROOT/'exports/wood_storage_crates'
TITLES=['盖板款','斜盖款','双层款']

def main():
    OUT.mkdir(parents=True,exist_ok=True);written=[]
    def write(path,data):
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf-8');written.append(str(path))
    models={i:json.loads((SOURCE/'Json Files'/f'blacksmith_box_{i}.json').read_text(encoding='utf-8-sig')) for i in [1,2,3]}
    originals={i:json.loads((SOURCE/'Blockbench'/f'blacksmith_box_{i}.bbmodel').read_text(encoding='utf-8-sig')) for i in [1,2,3]}
    for i,m in models.items():
        assert len(m['elements'])=={1:10,2:10,3:19}[i]
        assert all(f.get('texture')=='#0' for e in m['elements'] for f in e['faces'].values())
        parent=copy.deepcopy(m);parent['parent']='minecraft:block/block';parent['render_type']='minecraft:cutout'
        parent['textures']={'0':'theaurorian2:block/silent_wood_storage_barrel','particle':'#0'}
        assert parent['elements']==m['elements'] and parent['display']==m['display']
        write(A/f'models/block/storage_crate/box_{i}.json',parent)
    sheet=Image.new('RGB',(1800,1670),'#111923');draw=ImageDraw.Draw(sheet);font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((35,24),'极光五木 · 三款储物板条箱',font=font(38),fill='#ecf2f8')
    draw.text((37,82),'原 box_1 / box_2 / box_3 模型 · 对应木板配色 · 单层 27 格 / 双层 54 格储物',font=font(23),fill='#a7bac8')
    ids=[]
    for row,(wood,label,stem) in enumerate(WOODS):
        folder=OUT/wood;folder.mkdir(exist_ok=True)
        # Both furniture sets use the same original blacksmith atlas and identical five-color
        # plank mapping. Share the runtime texture instead of writing five duplicate atlases.
        texture=A/f'textures/block/{wood}_storage_barrel.png'
        atlas=Image.open(texture).convert('RGBA');assert atlas.size==(256,256)
        (folder/'blacksmith.png').write_bytes(texture.read_bytes())
        embedded='data:image/png;base64,'+base64.b64encode(texture.read_bytes()).decode()
        y=130+row*300;draw.rounded_rectangle((22,y,1778,y+282),17,fill='#1f2c3a')
        draw.text((42,y+15),label,font=font(27),fill='#edf3fa')
        for i in [1,2,3]:
            name=f'{wood}_storage_crate_{i}';ids.append('theaurorian2:'+name)
            write(A/f'models/block/{name}.json',{'parent':f'theaurorian2:block/storage_crate/box_{i}','textures':{'0':f'theaurorian2:block/{wood}_storage_barrel','particle':'#0'}})
            write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:block/{name}'}})
            write(A/f'blockstates/{name}.json',{'variants':{f'facing={direction}':{'model':f'theaurorian2:block/{name}','y':rotation} for direction,rotation in [('north',180),('east',270),('south',0),('west',90),('up',180),('down',180)]}})
            bb=copy.deepcopy(originals[i]);bb['name']=label+'储物板条箱（'+TITLES[i-1]+'）';assert len(bb['textures'])==1
            bb['textures'][0].update(source=embedded,name='blacksmith.png',path='blacksmith.png',relative_path='blacksmith.png')
            assert bb['elements']==originals[i]['elements'] and bb['outliner']==originals[i]['outliner']
            write(folder/f'{name}.bbmodel',bb)
            preview=copy.deepcopy(bb)
            for e in preview['elements']:e.setdefault('rotation',[0,0,0]);e.setdefault('origin',[0,0,0])
            preview['outliner']=[{'name':'root','origin':[0,0,0],'children':[e['uuid'] for e in preview['elements']]}]
            im=renderer.render(preview,atlas,510,228,camera_angles=(23,-30,0));x=140+(i-1)*535;sheet.paste(im,(x,y+16),im)
            draw.text((x+165,y+245),f'{i}  {TITLES[i-1]}',font=font(21),fill='#b6c7d5')
            write(D/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],
                'entries':[{'type':'minecraft:item','name':'theaurorian2:'+name,'functions':[{'function':'minecraft:copy_components','source':'block_entity','include':['minecraft:custom_name']}]}]}]})
            next_name=f'{wood}_storage_crate_{i%3+1}'
            write(D/f'theaurorian2/recipe/{next_name}_from_{i}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['theaurorian2:'+name], 'result':{'id':'theaurorian2:'+next_name,'count':1}})
        write(D/f'theaurorian2/recipe/{wood}_storage_crate_1.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['PPP','P P','SSS'],
            'key':{'P':f'theaurorian2:{stem}_planks','S':f'theaurorian2:{wood}_slab'},'result':{'id':f'theaurorian2:{wood}_storage_crate_1','count':1}})
    for locale in ['zh_cn','en_us']:
        p=A/f'lang/{locale}.json';data=json.loads(p.read_text(encoding='utf-8'))
        for wood,label,_ in WOODS:
            for i in [1,2,3]:data[f'block.theaurorian2.{wood}_storage_crate_{i}']=label+'储物板条箱（'+TITLES[i-1]+'）' if locale=='zh_cn' else wood.replace('_',' ').title()+' Storage Crate ('+['Loose Lid','Leaning Lid','Stacked'][i-1]+')'
        write(p,data)
    for namespace,kind,tag in [('minecraft','block','mineable/axe'),('c','block','barrels/wooden'),('c','item','barrels/wooden'),('minecraft','item','furnace_fuels')]:
        p=D/f'{namespace}/tags/{kind}/{tag}.json';data=json.loads(p.read_text(encoding='utf-8'));data['values'] += [n for n in ids if n not in data['values']];write(p,data)
    p=D/'neoforge/data_maps/item/furnace_fuels.json';data=json.loads(p.read_text(encoding='utf-8'));data['values'].update({n:{'burn_time':300} for n in ids});write(p,data)
    sheet.save(OUT/'overview.png');(OUT/'generated-files.txt').write_text('\n'.join(written)+'\n',encoding='utf-8')
    (OUT/'README.txt').write_text('Magic Store / atcpybd 原 box_1、box_2、box_3，保留模型顶点、枢轴、旋转、UV、原标签和展示变换。\n木质部分使用极光五种木板配色，与储物桶共享原铁匠包图集；纸标签和深色装饰保留原色。\n盖板款和斜盖款为27格，双层款为54格；原斜盖为固定造型，没有额外开盖动画。\n五块对应木板和三块对应半砖合成盖板款；在工作台中单独放入物品按1→2→3→1循环转换。\n新增storage_crate注册名，不替换旧的破坏掉落金苹果的crate方块。\n旋转盖板的碰撞按像素网格预计算，仅初始化时生成，原渲染几何不变。\n原素材授权继续适用，详见THIRD_PARTY_NOTICES.md。\n',encoding='utf-8')

if __name__=='__main__':main()
