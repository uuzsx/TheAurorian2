"""Append one texel row to seated legs, preserving existing texel density and art."""
import argparse,base64,copy,io,json,math
from pathlib import Path
from PIL import Image
p=argparse.ArgumentParser();p.add_argument('source',type=Path);p.add_argument('pending',type=Path);args=p.parse_args()
m=json.loads(args.source.read_text(encoding='utf-8-sig'));old=copy.deepcopy(m)
img=Image.open(io.BytesIO(base64.b64decode(m['textures'][0]['source'].split(',')[1]))).convert('RGBA')
w=max(128,img.width);h=2**math.ceil(math.log2(img.height+8));atlas=Image.new('RGBA',(w,h));atlas.paste(img,(0,0));x=0;y=img.height
for e in m['elements']:
 if e['name'] not in ['left_leg_base','left_leg_outer','right_leg_base','right_leg_outer']:continue
 assert abs(e['to'][1]-e['from'][1])<4.3,'Already extended or unexpected model'
 oldheight=e['to'][1]-e['from'][1];n=abs(e['faces']['north']['uv'][3]-e['faces']['north']['uv'][1]);assert n==4
 e['from'][1]-=oldheight/n
 for face in ['north','east','south','west']:
  f=e['faces'][face];u,v,U,V=map(int,f['uv']);a=img.crop((min(u,U),min(v,V),max(u,U),max(v,V)))
  if U<u:a=a.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
  if V<v:a=a.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  assert a.height==n
  b=Image.new('RGBA',(a.width,a.height+1));b.paste(a,(0,0));b.paste(a.crop((0,a.height-1,a.width,a.height)),(0,a.height))
  assert x+b.width<=w;atlas.paste(b,(x,y));f['uv']=[x,y,x+b.width,y+b.height];x+=b.width+1
  assert b.crop((0,0,a.width,a.height)).tobytes()==a.tobytes()
assert old['animations']==m['animations'] and old['groups']==m['groups'] and old['outliner']==m['outliner']
assert atlas.crop((0,0,img.width,img.height)).tobytes()==img.tobytes()
m['resolution']={'width':w,'height':h};t=m['textures'][0]
for k,v in [('width',w),('height',h),('uv_width',w),('uv_height',h)]:t[k]=v
t['name']='skin.png'
for k in ['path','relative_path']:t.pop(k,None)
s=io.BytesIO();atlas.save(s,format='PNG');t['source']='data:image/png;base64,'+base64.b64encode(s.getvalue()).decode()
args.pending.write_text(json.dumps(m,ensure_ascii=False),encoding='utf-8');atlas.save(args.source.parent/'skin.png')
print(args.source.name,'one row appended; old artwork, pixel pitch, pivots, animations preserved')
