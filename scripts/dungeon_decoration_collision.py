"""Offline collision baking from Java-model surfaces, including transparent pixels.

Face order and UV rotation match vanilla FaceInfo/CuboidFace. The half-pixel
grid (1/32 block) follows tilted surfaces; it never fills a whole prop's AABB.
Only baked bitsets are read by the mod, once during registration.
"""
import gzip
import math
import struct
import numpy as np

RESOLUTION = 2  # voxels per model pixel

def rotate(points, rotation):
    if not rotation or not rotation.get('angle'):
        return points
    origin=np.array(rotation['origin']);v=points-origin
    a=math.radians(rotation['angle']);c,s=math.cos(a),math.sin(a)
    axis='xyz'.index(rotation['axis']);j,k=(axis+1)%3,(axis+2)%3
    if rotation.get('rescale'):
        v[:,j]/=c;v[:,k]/=c
    result=v.copy();result[:,j]=v[:,j]*c-v[:,k]*s;result[:,k]=v[:,j]*s+v[:,k]*c
    return result+origin

def face_quads(element):
    x,y,z=element['from'];X,Y,Z=element['to']
    return {
        'north':[(X,Y,z),(X,y,z),(x,y,z),(x,Y,z)],
        'south':[(x,Y,Z),(x,y,Z),(X,y,Z),(X,Y,Z)],
        'east':[(X,Y,Z),(X,y,Z),(X,y,z),(X,Y,z)],
        'west':[(x,Y,z),(x,y,z),(x,y,Z),(x,Y,Z)],
        'up':[(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],
        'down':[(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)],
    }

def bake_surface(elements, texture, reverse=False):
    alpha=np.asarray(texture)[:,:,3];height,width=alpha.shape
    occupied=set()
    for element in elements:
        quads=face_quads(element)
        dimensions=np.array(element['to'])-np.array(element['from'])
        inverted=np.prod(dimensions)<0
        for direction,face in element['faces'].items():
            p=rotate(np.array(quads[direction],dtype=float),element.get('rotation'))
            if reverse:
                p[:,0]=16-p[:,0];p[:,2]=16-p[:,2]
            a=p[3]-p[0];b=p[1]-p[0];normal=np.cross(b,a);length=np.linalg.norm(normal)
            if length<1e-9:continue
            normal/=length
            if inverted:normal=-normal
            ns=max(1,math.ceil(np.linalg.norm(a)*8-1e-8));nt=max(1,math.ceil(np.linalg.norm(b)*8-1e-8))
            s,t=np.meshgrid((np.arange(ns)+.5)/ns,(np.arange(nt)+.5)/nt,indexing='ij')
            s=s.ravel();t=t.ravel()
            u,v,U,V=face['uv'];uv=np.array([(u,v),(u,V),(U,V),(U,v)])
            uv=np.roll(uv,-face.get('rotation',0)//90,axis=0)
            coords=uv[0]+s[:,None]*(uv[3]-uv[0])+t[:,None]*(uv[1]-uv[0])
            tx=np.clip(np.floor(coords[:,0]*width/16).astype(int),0,width-1)
            ty=np.clip(np.floor(coords[:,1]*height/16).astype(int),0,height-1)
            visible=alpha[ty,tx]>=26
            points=p[0]+s[visible,None]*a+t[visible,None]*b-normal*1e-7
            # A horizontal zero-thickness surface supports feet at its actual Y,
            # not half a pixel above it. Vertical planes retain a thin wall.
            if 0 in dimensions and abs(normal[1])>.999:
                points[:,1]=p[0,1]-1e-7
            voxels=np.floor(np.round(points*RESOLUTION,9)).astype(int)
            occupied.update(map(tuple,np.unique(voxels,axis=0).tolist()))
    return occupied

def partition(voxels,cells):
    result=[set() for _ in cells];scale=16*RESOLUTION
    cell_ids={tuple(c):i for i,c in enumerate(cells)}
    for voxel in voxels:
        # Head penetration on lying A is intentional. Buried surfaces must not
        # create an extra occupied cell below its ground support.
        if min(c[1] for c in cells)>=0 and voxel[1]<0:continue
        cell=tuple(v//scale for v in voxel);part=cell_ids.get(cell)
        if part is None:
            center=[(v+.5)/scale for v in voxel]
            part=min(range(len(cells)),key=lambda i:sum(max(c-p,0,p-c-1)**2 for p,c in zip(center,cells[i])))
        result[part].add(tuple(v-c*scale for v,c in zip(voxel,cells[part])))
    return result

def write_shapes(path,layouts):
    data=bytearray(struct.pack('>IHH',0x41555244,1,len(layouts)))
    stats=[]
    for name,parts in layouts.items():
        encoded=name.encode('ascii');data.extend(struct.pack('>H',len(encoded)));data.extend(encoded)
        data.extend(struct.pack('>H',len(parts)))
        for voxels in parts:
            if voxels:
                low=[min(v[i] for v in voxels) for i in range(3)]
                dims=[max(v[i] for v in voxels)+1-low[i] for i in range(3)]
            else:low=[0,0,0];dims=[1,1,1]
            bits=bytearray((math.prod(dims)+7)//8)
            for v in voxels:
                x,y,z=[p-q for p,q in zip(v,low)];index=(x*dims[1]+y)*dims[2]+z
                bits[index//8]|=1<<(index%8)
            data.extend(struct.pack('>6hI',*low,*dims,len(bits)));data.extend(bits)
        stats.append({'layout':name,'voxels':sum(map(len,parts)),'parts':len(parts)})
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(gzip.compress(bytes(data),mtime=0))
    return stats
