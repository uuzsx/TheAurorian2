"""Lossless model atlas baking: replace per-pixel geometry with cuboids and seven face panels."""
import argparse,base64,copy,io,json,math,re,uuid
from pathlib import Path
from PIL import Image
parser=argparse.ArgumentParser()
parser.add_argument('source',type=Path)
parser.add_argument('pending',type=Path)
parser.add_argument('--template',type=Path,default=Path('exports/skin_doll/final/stupid_cat.bbmodel'))
args=parser.parse_args()
out=args.source.parent
old=json.loads(args.source.read_text(encoding='utf-8-sig'))
assert len(old['elements'])>19, 'Already consolidated'
ref=json.loads(args.template.read_text(encoding='utf-8-sig'))
texture=Image.open(io.BytesIO(base64.b64decode(old['textures'][0]['source'].split(',')[1]))).convert('RGBA')
byname={e['name']:e for e in old['elements']};atlas=Image.new('RGBA',(128,128));cursor=[0,0,0]
def pack(im):
 w,h=im.size
 if cursor[0]+w+1>128:cursor[0]=0;cursor[1]+=cursor[2]+1;cursor[2]=0
 x,y=cursor[:2];assert y+h<=128
 atlas.paste(im,(x,y));cursor[0]+=w+1;cursor[2]=max(cursor[2],h)
 return [x,y,x+w,y+h]
def sample(uv):
 u,v,U,V=uv;return texture.getpixel((int((u+U)/2),int((v+V)/2)))
def face_image(template,face):
 uv=template['faces'][face]['uv'];name=template['name']
 if name=='head_base':
  if face=='north':
   im=Image.new('RGBA',(8,8))
   for y in range(8):
    for x in range(8):
     e=byname.get(f'face_pixel_{y}_{x}') or byname[f'eye_pixel_{y}_{x}'];im.putpixel((x,y),sample(e['faces']['north']['uv']))
   return im
  u,v,U,V=map(int,uv);im=texture.crop((min(u,U),min(v,V),max(u,U),max(v,V)))
  if U<u:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
  if V<v:im=im.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  return im
 size=[template['to'][i]-template['from'][i] for i in range(3)]
 a,b=(0,1) if face in ['north','south'] else (2,1) if face in ['east','west'] else (0,2)
 im=Image.new('RGBA',(round(size[a]),round(size[b])))
 # Derive pixel placement from geometry, not stale tile names: approved arm
 # front/back corrections moved planes while retaining their original tile indices.
 for e in old['elements']:
  if not e['name'].startswith(name+'_'):continue
  f=e['faces'].get(face)
  if not f or f.get('texture') is None:continue
  i=round((e['from'][a]-template['from'][a])/size[a]*im.width)
  j=round((e['from'][b]-template['from'][b])/size[b]*im.height)
  if face in ['north','east']:i=im.width-1-i
  if face in ['north','south','east','west','down']:j=im.height-1-j
  assert 0<=i<im.width and 0<=j<im.height,(name,face,i,j)
  im.putpixel((i,j),sample(f['uv']))
 return im
new=[];groups=copy.deepcopy(old['groups']);nodes={g['name']:{'uuid':g['uuid'],'children':[]} for g in groups}
for template in ref['elements'][:12]:
 e=copy.deepcopy(template);name=e['name'];owner=name.removesuffix('_base').removesuffix('_outer')
 if '_arm_' in name:e['rotation']=byname[name.replace('_outer','_base')+'_north_0_0']['rotation'][:]
 for face in e['faces']:
  im=face_image(template,face)
  e['faces'][face]={'uv':pack(im),'texture':None if name=='head_base' and face=='north' else 0}
 new.append(e);nodes[owner]['children'].append(e['uuid'])
# Split only around animated eyes. Face panels remain exactly coplanar, as approved.
front=face_image(ref['elements'][0],'north')
rects=[(0,0,8,4),(0,7,8,8),(0,4,1,7),(3,4,5,7),(7,4,8,7),(1,4,3,7),(5,4,7,7)]
for i,(x,y,X,Y) in enumerate(rects):
 eye=i>=5;eid=str(uuid.uuid4());owner='eyelids' if eye else 'head'
 faces={k:{'uv':[0,0,0,0],'texture':None} for k in ['north','south','east','west','up','down']}
 faces['north']={'uv':pack(front.crop((x,y,X,Y))),'texture':0}
 if eye:
  closed=Image.new('RGBA',(X-x,Y-y))
  for j in range(Y-y):
   for k in range(X-x):closed.putpixel((k,j),sample(byname[f'eye_pixel_{y+j}_{x+k}']['faces']['south']['uv']))
  faces['south']={'uv':pack(closed.transpose(Image.Transpose.FLIP_LEFT_RIGHT)),'texture':0}
 e={'name':'eye_panel_'+str(i-5) if eye else 'face_panel_'+str(i),'uuid':eid,'type':'cube','box_uv':False,'from':[4-X,15.4-Y,-4],'to':[4-x,15.4-y,-4],'origin':next(g['origin'] for g in groups if g['name']==owner),'rotation':[0,0,0],'faces':faces}
 new.append(e);nodes[owner]['children'].append(eid)
nodes['head']['children'].append(nodes['eyelids'])
m=copy.deepcopy(old);m['elements']=new;m['outliner']=[nodes[g['name']] for g in groups if g['name']!='eyelids'];m['resolution']={'width':128,'height':128}
stream=io.BytesIO();atlas.save(stream,format='PNG');atlas.save(out/'skin.png')
m['textures']=[{'name':'skin.png','id':'0','uuid':str(uuid.uuid4()),'width':128,'height':128,'uv_width':128,'uv_height':128,'source':'data:image/png;base64,'+base64.b64encode(stream.getvalue()).decode()}]
assert m['animations']==old['animations'] and m['groups']==old['groups']
assert len(new)==19
args.pending.write_text(json.dumps(m,separators=(',',':')),encoding='utf8')
print('629 -> 19 elements: 12 body/outer cuboids, 5 static face panels, 2 animated eye panels. Existing bones/animation tracks unchanged.')
