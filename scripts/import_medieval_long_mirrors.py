"""Import the supplied two-block mirror; recolor wood only and retain the blue mirror."""
import copy
import json
import uuid
from PIL import Image, ImageDraw, ImageFont
from import_medieval_wood_furniture import ROOT, A, SOURCE, WOODS, bbmodel
import export_wood_chests as renderer

OUT = ROOT / 'exports/wood_long_mirrors'

def half(elements, upper):
    low, high = (16,32) if upper else (0,16)
    result = []
    for original in elements:
        a,b = original['from'][1],original['to'][1]
        assert a < b
        l,h = max(a,low),min(b,high)
        if l >= h: continue
        element = copy.deepcopy(original)
        element['from'][1],element['to'][1] = l-low,h-low
        for name,face in element['faces'].items():
            if name in ('north','east','south','west'):
                assert not face.get('rotation',0)
                u,v,U,V = face['uv']
                face['uv'] = [u,v+(V-v)*(b-h)/(b-a),U,v+(V-v)*(b-l)/(b-a)]
        if h < b: element['faces'].pop('up',None)
        if l > a: element['faces'].pop('down',None)
        result.append(element)
    return result

def main():
    source = json.loads((SOURCE/'models/sdb_medievale/long_miroir.json').read_text(encoding='utf8'))
    original = Image.open(SOURCE/'textures/sdb_medievale/long_miroir.png').convert('RGBA')
    assert original.size == (64,64) and len(source['elements']) == 3
    assert source['elements'][1]['from'][0] > source['elements'][1]['to'][0]
    assert source['elements'][2]['from'][2] == source['elements'][2]['to'][2] == 15
    for e in source['elements']:
        assert 'rotation' not in e
        for f in e['faces'].values():
            assert f['texture'] == '#0' and not f.get('rotation',0) and all(0<=n<=16 for n in f['uv'])
    colors = sorted({p[:3] for p in original.get_flattened_data() if p[3] and p[0]>p[1]>p[2]},key=sum)
    written = []
    def write(path,data):
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8')
        written.append(str(path))
    sheet = Image.new('RGB',(1500,820),'#111a24');draw=ImageDraw.Draw(sheet)
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((30,24),'五种木材 · 中世纪长镜',font=font(34),fill='#e6eef6')
    draw.text((32,80),'保留原模型与蓝色镜面 · 镜框按对应木板采色 · 一格宽、两格高，壁挂摆放',font=font(21),fill='#a2b9c7')
    for i,(key,label,plank) in enumerate(WOODS):
        name=key+'_long_mirror';folder=OUT/key;folder.mkdir(parents=True,exist_ok=True)
        palette=sorted(set(Image.open(A/f'textures/block/{plank}.png').convert('RGB').get_flattened_data()),key=sum)
        mapping={c:palette[round(n*(len(palette)-1)/(len(colors)-1))] for n,c in enumerate(colors)}
        atlas=Image.new('RGBA',original.size)
        atlas.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in original.get_flattened_data()])
        atlas.save(folder/'long_mirror.png');atlas.save(A/f'textures/block/{name}.png')
        base={'parent':'minecraft:block/block','render_type':'minecraft:cutout',
              'textures':{'0':f'theaurorian2:block/{name}','particle':f'theaurorian2:block/{plank}'}}
        for upper in (False,True):write(A/f'models/block/{name}_{str(upper).lower()}.json',{**base,'elements':half(source['elements'],upper)})
        item=copy.deepcopy(source);item.pop('groups',None);item.update(base)
        write(A/f'models/item/{name}.json',item)
        write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{name}'}})
        write(A/f'blockstates/{name}.json',{'variants':{f'facing={facing},second={str(upper).lower()}':
            {'model':f'theaurorian2:block/{name}_{str(upper).lower()}','y':rot}
            for facing,rot in [('north',0),('east',90),('south',180),('west',270)] for upper in (False,True)}})
        bb=bbmodel(source['elements'],label+'长镜',(folder/'long_mirror.png').read_bytes())
        bb['resolution']={'width':64,'height':64};bb['display']=copy.deepcopy(source['display']);bb['gui_light']=source['gui_light']
        tex=bb['textures'][0];tex.update(name='long_mirror.png',width=64,height=64,uv_width=64,uv_height=64)
        for cube in bb['elements']:
            cube['origin']=[8,8,8]
            for f in cube['faces'].values():f['uv']=[n/2 for n in f['uv']]
        preview=copy.deepcopy(bb)
        def group(node,path):
            if isinstance(node,int):return bb['elements'][node]['uuid']
            g=copy.deepcopy(node);g['uuid']=str(uuid.uuid5(uuid.NAMESPACE_URL,name+'/'+path))
            g['children']=[group(c,path+'/'+str(n)) for n,c in enumerate(node['children'])];return g
        bb['outliner']=[group(g,str(n)) for n,g in enumerate(source['groups'])]
        write(folder/f'{name}.bbmodel',bb)
        x=15+i*297;draw.rounded_rectangle((x,137,x+282,795),16,fill='#1c2835')
        im=renderer.render(preview,atlas,278,590,camera_angles=(-12,155,0));sheet.paste(im,(x+2,155),im)
        draw.text((x+50,748),label+'长镜',font=font(23),fill='#e6eef6')
        write(ROOT/f'src/main/resources/data/theaurorian2/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'building',
            'pattern':['PGP','PGP','PPP'],'key':{'P':f'theaurorian2:{plank}','G':'#c:glass_blocks/colorless'},'result':{'id':f'theaurorian2:{name}','count':1}})
        write(ROOT/f'src/main/resources/data/theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,
            'conditions':[{'condition':'minecraft:block_state_property','block':f'theaurorian2:{name}','properties':{'second':'false'}},{'condition':'minecraft:survives_explosion'}],
            'entries':[{'type':'minecraft:item','name':f'theaurorian2:{name}'}]}]})
    for locale in ('zh_cn','en_us'):
        path=A/f'lang/{locale}.json';data=json.loads(path.read_text(encoding='utf8'))
        for key,label,_ in WOODS:data[f'block.theaurorian2.{key}_long_mirror']=label+'长镜' if locale=='zh_cn' else key.replace('_',' ').title()+' Long Mirror'
        write(path,data)
    path=ROOT/'src/main/resources/data/minecraft/tags/block/mineable/axe.json';data=json.loads(path.read_text(encoding='utf8'))
    for key,_,_ in WOODS:
        value=f'theaurorian2:{key}_long_mirror'
        if value not in data['values']:data['values'].append(value)
    write(path,data)
    sheet.save(OUT/'overview.png')
    (OUT/'generated-files.txt').write_text('\n'.join(written),encoding='utf8')
    (OUT/'README.txt').write_text('原模型：用户提供的 ShizuArt 中世纪浴室家具包 long_miroir。\n五种镜框直接采色对应木板，保留原64×64木纹、镜面蓝色高光、透明镂空、反向内表面、零厚度镜面及物品展示参数。\n原模型16×32×2，向上占两格，保留原素材的壁挂、无实体碰撞方式；上下均需背靠完整墙面。\n拆除任意半边或失去墙面支撑，整面镜子掉落一次；创造拆除不掉落。七块对应木板和两块无色玻璃合成一面。\n原始授权继续适用，见 THIRD_PARTY_NOTICES.md。\n',encoding='utf8')

if __name__=='__main__':main()
