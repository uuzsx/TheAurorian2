"""Import the owner's ShizuArt dungeon props as static, multipart block models.

No rescaling, UV edits or winding changes. Whole source elements are assigned to
nearby occupied cells and translated with their pivots. The item keeps the source
geometry/display transforms. Run import_dungeon_decorations.ps1 to write JSON.
"""
import copy
import itertools
import json
import math
import shutil
from pathlib import Path
from PIL import Image
from dungeon_decoration_collision import bake_surface, partition, write_shapes

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path('E:/BaiduNetdiskDownload/2036地下城骷髅/2036地下城骷髅/ItemsAdder/contents/shizuart_furnitures')
ASSETS = ROOT / 'src/main/resources/assets/theaurorian2'
DATA = ROOT / 'src/main/resources/data'
OUT = ROOT / 'build/dungeon-props'
# enum, registry, source suffix, Chinese, English, cage, hanging, translation, occupied cells
PROPS = [
    ('HANGING_CAGE','dungeon_hanging_cage','jail','悬挂空笼','Hanging Cage',True,True,(0,-16,0),[(0,0,0),(0,-1,0),(0,-2,0)]),
    ('HANGING_CAGED_SKELETON','dungeon_hanging_caged_skeleton','jail_skeleton','悬挂笼中骷髅','Hanging Caged Skeleton',True,True,(0,-16,0),[(0,0,0),(0,-1,0),(0,-2,0)]),
    ('CAGE','dungeon_cage','jail2','落地空笼','Standing Cage',True,False,(0,8,0),[(0,0,0),(0,1,0),(0,2,0)]),
    ('CAGED_SKELETON','dungeon_caged_skeleton','jail_skeleton2','笼中骷髅','Caged Skeleton',True,False,(0,8,0),[(0,0,0),(0,1,0),(0,2,0)]),
    ('BROKEN_CAGE','dungeon_broken_cage','broken_jail','破损牢笼','Broken Cage',True,False,(14,0.801,2.25),[(0,0,0),(1,0,0),(0,0,1),(1,0,1)]),
    ('SEATED_SKELETON','dungeon_seated_skeleton','seat_skeleton','坐姿骷髅','Seated Skeleton',False,False,(0,0.08203,0),[(0,0,0),(0,1,0)]),
    ('CHAINED_SKELETON','dungeon_chained_skeleton','chained_skeleton','锁链骷髅','Chained Skeleton',False,False,(0,0,0),[(0,0,0),(0,1,0)]),
    ('IMPALED_SKELETON','dungeon_impaled_skeleton','impaled_skeleton','穿刺骷髅','Impaled Skeleton',False,False,(0,0,0),[(0,0,0),(0,1,0)]),
    ('LYING_SKELETON','dungeon_lying_skeleton','lay_skeleton','躺卧骷髅 A','Lying Skeleton A',False,False,(0,-0.08239,0),[(0,0,0),(0,0,1)]),
    ('OVERHANGING_SKELETON','dungeon_overhanging_skeleton','lay_skeleton2','躺卧骷髅 B（垂首）','Lying Skeleton B (Overhanging)',False,False,(0,0,0),[(0,0,0),(0,-1,-1)]),
    ('SKULL','dungeon_skull','skeleton_head','单个骷髅头','Skull and Bones',False,False,(0,0.01933,0),[(0,0,0)]),
    ('SKULL_PILE','dungeon_skull_pile','skeleton_heads','骷髅头堆','Skull Pile',False,False,(0,0.01933,0),[(0,0,0)]),
    ('BONES','dungeon_bones','bones','散落骨头','Scattered Bones',False,False,(0,0,0),[(0,0,0)]),
    ('RIB_CAGE','dungeon_rib_cage','rib_cage','肋骨架','Rib Cage',False,False,(0,-0.08239,0),[(0,0,0)]),
]

# Keep old occupied cells readable. Newly placed wide bones sit across the seam
# of two columns, rather than spilling out of both sides of one central column.
ALIGNMENT = {
    'SEATED_SKELETON': ((8,0,0),[(0,0,0),(0,1,0),(1,0,0),(1,1,0)]),
    'CHAINED_SKELETON': ((8,0,0),[(0,0,0),(0,1,0),(1,0,0),(1,1,0)]),
    'LYING_SKELETON': ((8,0,0),[(0,0,0),(0,0,1),(1,0,0),(1,0,1)]),
    'OVERHANGING_SKELETON': ((8,0,-1.5),[(0,0,0),(0,-1,-1),(1,0,0),(1,-1,-1)]),
    'SKULL': ((8,0,0),[(0,0,0),(1,0,0)]),
    'SKULL_PILE': ((8,0,8),[(0,0,0),(1,0,0),(0,0,1),(1,0,1)]),
}

def translated(element, delta):
    e = copy.deepcopy(element)
    for k in ('from','to'):
        e[k] = [round(v+d,8) for v,d in zip(e[k],delta)]
    if 'rotation' in e:
        e['rotation']['origin'] = [round(v+d,8) for v,d in zip(e['rotation']['origin'],delta)]
    return e

def bounds(elements):
    points = []
    for e in elements:
        r=e.get('rotation',{}); o=r.get('origin',[0,0,0]); a=math.radians(r.get('angle',0))
        axis='xyz'.index(r.get('axis','y')); j,k=(axis+1)%3,(axis+2)%3
        for p in itertools.product(*zip(e['from'],e['to'])):
            v=[p[i]-o[i] for i in range(3)]
            v[j],v[k]=v[j]*math.cos(a)-v[k]*math.sin(a),v[j]*math.sin(a)+v[k]*math.cos(a)
            points.append([v[i]+o[i] for i in range(3)])
    return [[f(p[i] for p in points) for i in range(3)] for f in (min,max)]

def model(elements):
    return {'parent':'minecraft:block/block','render_type':'minecraft:cutout','gui_light':'front',
            'textures':{'0':'theaurorian2:block/dungeon_skeletons_props','particle':'minecraft:block/bone_block_side'},
            'elements':elements}

def main():
    OUT.mkdir(parents=True,exist_ok=True); writes={}; definitions=[]; manifest=[]; layouts={}; surfaces={}
    texture=Image.open(SOURCE/'textures/dungeon_skeletons_props/dungeon_skeletons_props.png').convert('RGBA')
    def write(path,data): writes[str(path)]=data
    def build_layout(name, original, delta, cells, reverse, aligned, moved_surface=None):
        parts=[[] for _ in cells];assignments=[]
        shifted=[translated(e,delta) for e in original]
        for i,e in enumerate(shifted):
            assert all(f['texture']=='#0' and f.get('rotation',0) in (0,90,180,270) for f in e['faces'].values())
            assert e.get('rotation',{}).get('angle',0) in (-45,-22.5,0,22.5,45)
            lo,hi=bounds([e]);center=[(a+b)/2 for a,b in zip(lo,hi)];valid=[]
            for part,cell in enumerate(cells):
                local=translated(e,[-16*v for v in cell])
                if all(-16<=v<=32 for key in ('from','to') for v in local[key]):
                    valid.append((sum((v-(c*16+8))**2 for v,c in zip(center,cell)),part,local))
            assert valid,(name,i)
            _,part,local=min(valid,key=lambda v:v[0]);parts[part].append(local);assignments.append(part)
            restored=translated(local,[16*v-d for v,d in zip(cells[part],delta)])
            for key in ('from','to'):
                assert all(abs(a-b)<1e-6 for a,b in zip(restored[key],original[i][key]))
                restored[key]=original[i][key]
            if 'rotation' in restored:
                assert all(abs(a-b)<1e-6 for a,b in zip(restored['rotation']['origin'],original[i]['rotation']['origin']))
                restored['rotation']['origin']=original[i]['rotation']['origin']
            assert restored==original[i]
        for part,elements in enumerate(parts):
            block=model(elements);block['textures']['particle']='minecraft:block/iron_block' if cage else 'minecraft:block/bone_block_side'
            write(ASSETS/f'models/block/{name}{"_aligned" if aligned else ""}_{part}.json',block)
        voxels=bake_surface(shifted,texture,reverse) if moved_surface is None else moved_surface
        if not aligned:surfaces[name]=voxels
        layouts[name+('/aligned' if aligned else '/legacy')]=partition(voxels,cells)
        return {'cells':cells,'translation':delta,'assignments':assignments,'bounds':bounds(shifted),'surface_voxels':len(voxels)}

    write(ASSETS/'models/block/dungeon_decoration_empty.json',model([]))
    for enum,name,suffix,zh,en,cage,hanging,delta,cells in PROPS:
        source=json.loads((SOURCE/f'models/dungeon_skeletons_props/skeletons_{suffix}.json').read_text(encoding='utf8'))
        original=source['elements'];reverse=enum=='HANGING_CAGED_SKELETON'
        legacy=build_layout(name,original,delta,cells,reverse,False)
        aligned=None
        if enum in ALIGNMENT:
            offset,aligned_cells=ALIGNMENT[enum]
            # Integral grid shifts must preserve every collision voxel exactly.
            moved={tuple(v+int(d*2) for v,d in zip(voxel,offset)) for voxel in surfaces[name]}
            aligned=build_layout(name,original,tuple(a+b for a,b in zip(delta,offset)),aligned_cells,reverse,True,moved)
            assert aligned['bounds'][0][0]>=0 and aligned['bounds'][1][0]<=32,(name,'two-column alignment')
        variants={}
        part_count=len(aligned['cells']) if aligned else len(cells)
        for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
            for is_aligned in ([False,True] if aligned else [False]):
                for part in range(part_count):
                    key=f'facing={facing}'+(f',part={part}' if part_count>1 else '')
                    if aligned:key+=',aligned='+str(is_aligned).lower()
                    target=f'{name}{"_aligned" if is_aligned else ""}_{part}'
                    if not is_aligned and part>=len(cells):target='dungeon_decoration_empty'
                    rotation=(y+(180 if reverse else 0))%360
                    variants[key]={'model':f'theaurorian2:block/{target}',**({'y':rotation} if rotation else {})}
        write(ASSETS/f'blockstates/{name}.json',{'variants':variants})
        item=model(original);item['display']=source['display'];item['textures']['particle']='minecraft:block/iron_block' if cage else 'minecraft:block/bone_block_side'
        write(ASSETS/f'models/item/{name}.json',item)
        write(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'theaurorian2:item/{name}'}})
        write(DATA/f'theaurorian2/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[
            {'type':'minecraft:item','name':f'theaurorian2:{name}','conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},
            {'type':'minecraft:item','name':'minecraft:iron_ingot' if cage else 'minecraft:bone','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':5}}]}]}],
            'conditions':[{'condition':'minecraft:survives_explosion'}]}],'random_sequence':f'theaurorian2:blocks/{name}'})
        ints=', '.join(str(v) for c in cells for v in c)
        aligned_arg='offsets('+', '.join(str(v) for c in aligned['cells'] for v in c)+')' if aligned else 'null'
        definitions.append(f'    {enum}("{name}", {str(cage).lower()}, {str(hanging).lower()}, offsets({ints}), {aligned_arg})')
        manifest.append({'id':name,'source':f'skeletons_{suffix}','elements':len(original),'world_yaw':180 if reverse else 0,'legacy':legacy,'aligned':aligned})
        print('Baked '+name,flush=True)
    for lang,index in [('zh_cn',3),('en_us',4)]:
        path=ASSETS/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf8'))
        for p in PROPS:data['block.theaurorian2.'+p[1]]=p[index]
        write(path,data)
    ids=['theaurorian2:'+p[1] for p in PROPS]
    write(DATA/'theaurorian2/tags/block/dungeon_decorations.json',{'replace':False,'values':ids})
    write(DATA/'theaurorian2/tags/item/dungeon_decorations.json',{'replace':False,'values':ids})
    path=DATA/'minecraft/tags/block/mineable/pickaxe.json';data=json.loads(path.read_text(encoding='utf8'))
    data['values'] += [v for v in ids if v not in data['values']];write(path,data)
    target=ASSETS/'textures/block/dungeon_skeletons_props.png'
    shutil.copyfile(SOURCE/'textures/dungeon_skeletons_props/dungeon_skeletons_props.png',target)
    template=(ROOT/'scripts/dungeon_decoration_type.java.template').read_text(encoding='utf8')
    (ROOT/'src/main/java/cn/teampancake/theaurorian2/common/block/DungeonDecorationType.java').write_text(template.replace('@@DEFINITIONS@@',',\n'.join(definitions)+';'),encoding='utf8')
    (OUT/'generated.staging').write_text(json.dumps(writes,ensure_ascii=False),encoding='utf8')
    (OUT/'manifest.staging').write_text(json.dumps(manifest,ensure_ascii=False),encoding='utf8')
    stats=write_shapes(ASSETS/'shapes/dungeon_decorations.bin.gz',layouts)
    (OUT/'collision-stats.staging').write_text(json.dumps(stats),encoding='utf8')
    print(f'Validated 14 props / {sum(p["elements"] for p in manifest)} original elements; {len(writes)} JSON files staged.')

if __name__=='__main__':main()
