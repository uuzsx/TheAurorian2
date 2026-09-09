"""Import Magic Store barrel_1/barrel_3 without changing geometry or UVs."""
import base64
import copy
import io
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import export_wood_chests as renderer

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path('E:/BaiduNetdiskDownload/1225中世纪铁匠家具/magic_store_mcmodel_blacksmith')
OUT = ROOT / 'exports/wood_storage_barrels'
A = ROOT / 'src/main/resources/assets/theaurorian2'
D = ROOT / 'src/main/resources/data'
WOODS = [('silent_wood','谧木','silent_tree'), ('weeping_willow','垂柳木','weeping_willow'),
         ('curtain_wood','幽帘木','curtain_tree'), ('cursed_frost_wood','咒霜木','cursed_frost_tree'),
         ('filthy_wood','污秽木','filthy_tree')]
KINDS = [('storage_barrel',1,'直立储物桶'), ('horizontal_barrel',3,'横放储物桶')]

def palette(path):
    return sorted(set(Image.open(path).convert('RGB').getdata()), key=lambda c:(sum(c),c))

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    written=[]
    def write(path,data):
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
        written.append(str(path))
    source_tex=Image.open(SOURCE/'Texture Files/blacksmith.png').convert('RGBA')
    assert source_tex.size==(256,256)
    models={k:json.loads((SOURCE/'Json Files'/f'blacksmith_barrel_{i}.json').read_text(encoding='utf-8-sig')) for k,i,_ in KINDS}
    originals={k:json.loads((SOURCE/'Blockbench'/f'blacksmith_barrel_{i}.bbmodel').read_text(encoding='utf-8-sig')) for k,i,_ in KINDS}
    sampled=set()
    for m in models.values():
        assert len(m['elements'])==11
        for e in m['elements']:
            assert not e.get('rotation',{}).get('angle',0)
            for f in e['faces'].values():
                if f['texture']=='#missing': continue  # Source Blockbench marks these internal faces untextured.
                assert f['texture']=='#0'
                u,v,U,V=f['uv']; assert all(0<=n<=16 for n in f['uv'])
                for x in range(math.floor(min(u,U)*16),math.ceil(max(u,U)*16)):
                    for y in range(math.floor(min(v,V)*16),math.ceil(max(v,V)*16)):sampled.add(source_tex.getpixel((x,y))[:3])
    wood=sorted([c for c in sampled if c[0]>c[1]>c[2]],key=sum)
    trim=sorted(sampled-set(wood),key=sum)
    assert len(wood)==5 and len(trim)==3
    # Two exact source parents; wood variants only override the texture reference.
    for kind,_,_ in KINDS:
        model=copy.deepcopy(models[kind]);model['parent']='minecraft:block/block'
        model['textures']={'0':'theaurorian2:block/silent_wood_storage_barrel','particle':'#0'}
        assert model['elements']==models[kind]['elements'] and model['display']==models[kind]['display']
        for e in model['elements']:
            e['faces']={direction:f for direction,f in e['faces'].items() if f['texture']!='#missing'}
        write(A/f'models/block/storage_barrel/{kind}.json',model)
    sheet=Image.new('RGB',(1560,1700),'#111923');draw=ImageDraw.Draw(sheet)
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((35,24),'极光五木 · 直立与横放储物桶',font=font(38),fill='#edf3f8')
    draw.text((37,83),'原 1 / 3 号模型 · 对应木板配色 / 原贴图箍带 · 原版木桶的 27 格储物',font=font(23),fill='#a6bac9')
    ids=[]
    for row,(key,label,stem) in enumerate(WOODS):
        plank=palette(A/f'textures/block/{stem}_planks.png')
        mapping={c:plank[round(i*(len(plank)-1)/(len(wood)-1))] for i,c in enumerate(wood)}
        tex=Image.new('RGBA',source_tex.size);tex.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in source_tex.getdata()])
        assert tex.getchannel('A').tobytes()==source_tex.getchannel('A').tobytes()
        folder=OUT/key;folder.mkdir(exist_ok=True)
        tex.save(folder/'barrel.png');tex.save(A/f'textures/block/{key}_storage_barrel.png')
        stream=io.BytesIO();tex.save(stream,format='PNG');embedded='data:image/png;base64,'+base64.b64encode(stream.getvalue()).decode()
        y=135+row*306;draw.rounded_rectangle((22,y,1538,y+289),18,fill='#1f2c3a')
        draw.text((45,y+17),label,font=font(29),fill='#e8eff5')
        for column,(kind,_,title) in enumerate(KINDS):
            name=f'{key}_{kind}';ids.append('theaurorian2:'+name)
            write(A/f'models/block/{name}.json',{'parent':f'theaurorian2:block/storage_barrel/{kind}','textures':{'0':f'theaurorian2:block/{key}_storage_barrel','particle':'#0'}})
            write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:block/{name}'}})
            # OPEN is vanilla container state; the supplied mesh remains the original closed mesh.
            angles={'west':0,'north':90,'east':180,'south':270,'up':90,'down':90} if column else {d:0 for d in ['north','east','south','west','up','down']}
            write(A/f'blockstates/{name}.json',{'variants':{f'facing={d}':{'model':f'theaurorian2:block/{name}','y':a} for d,a in angles.items()}})
            bb=copy.deepcopy(originals[kind]);bb['name']=label+title
            assert len(bb['textures'])==1
            bb['textures'][0].update(source=embedded,name='barrel.png',path='barrel.png',relative_path='barrel.png')
            assert bb['elements']==originals[kind]['elements'] and bb['outliner']==originals[kind]['outliner']
            write(folder/f'{name}.bbmodel',bb)
            preview=copy.deepcopy(bb)
            for e in preview['elements']:
                e.setdefault('rotation',[0,0,0]);e.setdefault('origin',[0,0,0])
                e['faces']={direction:f for direction,f in e['faces'].items() if f.get('texture') is not None}
            preview['outliner']=[{'name':'root','origin':[0,0,0],'children':[e['uuid'] for e in preview['elements']]}]
            image=renderer.render(preview,tex,545,249,camera_angles=(20,-30,0))
            sheet.paste(image,(210+column*655,y+10),image)
            draw.text((400+column*655,y+255),title,font=font(21),fill='#acbdcb')
            write(D/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],
                'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}','functions':[{'function':'minecraft:copy_components','source':'block_entity','include':['minecraft:custom_name']}]}]}]})
        upright=f'{key}_storage_barrel';horizontal=f'{key}_horizontal_barrel'
        write(D/f'theaurorian2/recipe/{upright}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['PPP','S S','PPP'],
            'key':{'P':f'theaurorian2:{stem}_planks','S':f'theaurorian2:{key}_slab'},'result':{'id':f'theaurorian2:{upright}','count':1}})
        for target,source in [(horizontal,upright),(upright,horizontal)]:
            write(D/f'theaurorian2/recipe/{target}_from_{source}.json',{'type':'minecraft:crafting_shapeless','category':'misc',
                'ingredients':[f'theaurorian2:{source}'],'result':{'id':f'theaurorian2:{target}','count':1}})
    for locale in ['zh_cn','en_us']:
        path=A/f'lang/{locale}.json';data=json.loads(path.read_text(encoding='utf-8'))
        for key,label,_ in WOODS:
            for kind,_,title in KINDS:
                data[f'block.theaurorian2.{key}_{kind}']=label+title if locale=='zh_cn' else key.replace('_',' ').title()+(' Storage Barrel' if kind=='storage_barrel' else ' Horizontal Storage Barrel')
        write(path,data)
    for namespace,kind,tag in [('minecraft','block','mineable/axe'),('c','block','barrels/wooden'),('c','item','barrels/wooden'),('minecraft','item','furnace_fuels')]:
        path=D/f'{namespace}/tags/{kind}/{tag}.json';data=json.loads(path.read_text(encoding='utf-8')) if path.exists() else {'replace':False,'values':[]}
        data['values']+= [i for i in ids if i not in data['values']];write(path,data)
    path=D/'neoforge/data_maps/item/furnace_fuels.json';data=json.loads(path.read_text(encoding='utf-8'))
    data['values'].update({i:{'burn_time':300} for i in ids});write(path,data)
    sheet.save(OUT/'overview.png')
    (OUT/'generated-files.txt').write_text('\n'.join(written)+'\n',encoding='utf-8')
    (OUT/'README.txt').write_text('来源：用户提供的 Magic Store / atcpybd 中世纪铁匠家具 barrel_1、barrel_3。\n保留原几何、坐标、UV、像素纹理和展示变换；桶身映射五种木板配色，箍带完整保留原贴图。\n直立款六块对应木板和两块半砖合成，空物品形态可与横放款一比一互换。\n直接复用原版木桶方块实体：27 格、漏斗、比较器、名称、战利品表、开关声音及库存存档。\n直立款保持直立；横放款按水平朝向放置。原模型没有开盖动画，OPEN 状态不改变几何。\n继承素材原授权，项目许可证不重新授权这些素材。\n',encoding='utf-8')

if __name__=='__main__': main()
