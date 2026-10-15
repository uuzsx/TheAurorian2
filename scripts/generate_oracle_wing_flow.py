"""Bake a small cyclic gold/cyan surface-flow atlas from the original wing mask.

Original texture/UVs are immutable. No runtime pixel generation or custom shader is
needed, so the existing Iris-compatible emissive pipeline can display the sweep.
"""
from pathlib import Path
from PIL import Image
import math

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / 'src/main/resources/assets/theaurorian2/textures/entity/rising_clock_oracle.png'
output = source.with_name('rising_clock_oracle_wing_flow.png')
original = Image.open(source).convert('RGBA')
assert original.size == (128,128)
wing = original.crop((97,0,113,32))
frames, tile_height = 32, 34
atlas = Image.new('RGBA',(16,frames*tile_height))
for frame in range(frames):
    tile = Image.new('RGBA',wing.size)
    for y in range(32):
        for x in range(16):
            r,g,b,a = wing.getpixel((x,y))
            if not a: continue
            # Narrow, diagonal polish over the existing pixel shading. Never replace
            # the gold/cyan artwork with two flat palette colors.
            phase = ((31-y)/32 + (x%8)*.026 - frame/frames) % 1
            distance = min(phase,1-phase)
            crest = math.exp(-(distance/.034)**2)
            wake = math.exp(-((phase-.065)/.06)**2)*.16
            sheen = min(1.,crest+wake)
            # Original RGB carries every hand-painted highlight and dark pixel.
            # A low-opacity full-bright copy marks the charged state; only the
            # traveling sliver receives a small, texture-relative highlight.
            color = tuple(min(255,round(c*(1+.10*sheen)+10*sheen)) for c in (r,g,b))
            tile.putpixel((x,y),color+(round(a*(.20+.35*sheen)),))
    atlas.paste(tile,(0,frame*tile_height+1))
    # Guard rows prevent the next animation frame from bleeding into the surface.
    atlas.paste(tile.crop((0,0,16,1)),(0,frame*tile_height))
    atlas.paste(tile.crop((0,31,16,32)),(0,frame*tile_height+33))
atlas.save(output,optimize=True)
assert all(atlas.getpixel((x,f*tile_height+1+y))[3] == 0
           for f in range(frames) for y in range(32) for x in range(16)
           if wing.getpixel((x,y))[3] == 0)
print(f'Generated {output}: {atlas.size}, {frames} frames; original wing silhouette preserved.')
