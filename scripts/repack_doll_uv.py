"""Pack referenced doll UV rectangles without resampling or changing geometry."""
import argparse,base64,copy,io,json
from pathlib import Path
from PIL import Image
p=argparse.ArgumentParser();p.add_argument('source',type=Path);p.add_argument('pending',type=Path);p.add_argument('--size',type=int,default=64);args=p.parse_args()
assert args.size>0
size=args.size
m=json.loads(args.source.read_text(encoding='utf-8-sig'));old=copy.deepcopy(m)
im=Image.open(io.BytesIO(base64.b64decode(m['textures'][0]['source'].split(',')[1]))).convert('RGBA')
rects=set()
for e in m['elements']:
 for f in e['faces'].values():
  if f.get('texture') is None:continue
  assert f['texture']==0
  u,v,U,V=f['uv'];assert all(float(n).is_integer() for n in [u,v,U,V])
  r=tuple(map(int,(min(u,U),min(v,V),max(u,U),max(v,V))));assert r[2]>r[0] and r[3]>r[1];rects.add(r)
atlas=Image.new('RGBA',(size,size));locations={};x=y=row=0
for r in sorted(rects,key=lambda r:(-(r[3]-r[1]),-(r[2]-r[0]),r)):
 a=im.crop(r);w,h=a.size
 if x+w+1>size:x=0;y+=row+1;row=0
 assert y+h<=size,'Atlas overflow'
 atlas.paste(a,(x,y));locations[r]=(x,y);x+=w+1;row=max(row,h)
for e in m['elements']:
 for f in e['faces'].values():
  if f.get('texture') is None:continue
  u,v,U,V=f['uv'];r=tuple(map(int,(min(u,U),min(v,V),max(u,U),max(v,V))));x,y=locations[r]
  f['uv']=[u-r[0]+x,v-r[1]+y,U-r[0]+x,V-r[1]+y]
  assert atlas.crop((x,y,x+r[2]-r[0],y+r[3]-r[1])).tobytes()==im.crop(r).tobytes()
for a,b in zip(old['elements'],m['elements']):
 aa=copy.deepcopy(a);bb=copy.deepcopy(b)
 for e in [aa,bb]:
  for f in e['faces'].values():f.pop('uv',None)
 assert aa==bb
assert old['animations']==m['animations'] and old['groups']==m['groups'] and old['outliner']==m['outliner']
m['resolution']={'width':size,'height':size};t=m['textures'][0]
for key in ['width','height','uv_width','uv_height']:t[key]=size
for key in ['path','relative_path']:t.pop(key,None)
s=io.BytesIO();atlas.save(s,format='PNG');t['source']='data:image/png;base64,'+base64.b64encode(s.getvalue()).decode();t['name']='skin.png'
args.pending.write_text(json.dumps(m,ensure_ascii=False),encoding='utf-8');atlas.save(args.source.parent/'skin.png')
print(args.source.name,len(rects),'rectangles; exact pixel colors and UV direction retained; geometry and animation unchanged')
