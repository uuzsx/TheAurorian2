"""Approved furniture ramps. Preserve authored pixels, UVs and non-wood materials."""
import json
from pathlib import Path
import numpy as np
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
PALETTES = json.loads((Path(__file__).with_name('furniture_palettes.json')).read_text(encoding='utf-8'))

def ramp(codes, count):
    colors = [np.array(list(bytes.fromhex(c.lstrip('#'))), dtype=float) for c in codes]
    result = []
    for index in range(count):
        t = index * (len(colors) - 1) / max(1, count - 1)
        a = int(t); b = min(a + 1, len(colors) - 1)
        result.append(np.rint(colors[a] * (1 - (t-a)) + colors[b] * (t-a)).astype('uint8'))
    return result

def apply_furniture_palette(image, wood, kind='wood'):
    """Apply after the importer transfers source colors to its original plank ramp."""
    palette = PALETTES[wood]
    if palette.get('original_materials', False):
        return image.convert('RGBA')
    data = np.array(image.convert('RGBA'))
    def region(bounds, codes, allowed=None):
        view = data[bounds]; rgb = view[:, :, :3]; original = rgb.copy()
        colors = sorted(set(map(tuple, original[view[:, :, 3] > 0])), key=lambda c: (sum(map(int,c)), c))
        if allowed is not None: colors = [c for c in colors if c in allowed]
        for color, target in zip(colors, ramp(codes, len(colors))):
            rgb[np.all(original == color, axis=2)] = target
    if kind == 'crate':
        region((slice(0,16),slice(0,16)), palette['wood'])
        region((slice(0,16),slice(16,32)), [palette['trim'][i] for i in palette['crate_trim_indices']])
        dark = ['#'+''.join(f'{round(v*.26):02x}' for v in bytes.fromhex(c[1:])) for c in palette['wood']]
        region((slice(0,16),slice(32,48)), dark)
    else:
        if kind == 'carpenter':
            allowed = {(112,90,121),(122,126,158),(141,149,165),(163,173,194),(188,209,226)}
        else:
            plank = ROOT/'src/main/resources/assets/theaurorian2/textures/block'/ (palette['plank']+'.png')
            allowed = set(Image.open(plank).convert('RGB').getdata())
        region((slice(None),slice(None)), palette['wood'], allowed)
    if kind == 'storage_barrel':
        # Authored iron hoop ramp in the Blacksmith atlas; keep other materials intact.
        original = data[:, :, :3].copy()
        for color, index in zip(((44,41,41),(59,54,54),(70,64,64)), palette['storage_trim_indices']):
            data[:, :, :3][np.all(original == color, axis=2)] = tuple(bytes.fromhex(palette['trim'][index][1:]))
    if kind == 'storage_barrel' and palette['cool_labels']:
        # Source paper labels share this atlas; choose a cool parchment ramp.
        original = data[:, :, :3].copy()
        colors = sorted({tuple(c[:3]) for c in data.reshape(-1,4)
                         if c[3] and int(c[0]) > int(c[1]) > int(c[2]) and int(c[0]) > 90},
                        key=lambda c: sum(map(int,c)))
        for color, target in zip(colors, ramp(['#718994','#99b0b7','#c4d6d6','#e7eeee'],len(colors))):
            data[:, :, :3][np.all(original == color, axis=2)] = target
    return Image.fromarray(data)

def chest_palette(original, wood):
    palette = PALETTES[wood]
    brown = [(52,28,39),(96,44,44),(136,75,43),(190,119,43),(222,158,65)]
    metal = [(9,10,20),(16,20,31),(21,29,40),(32,46,55),(57,74,80),(87,114,119),(129,151,150),(168,181,178),(199,207,204)]
    mapping = {source:tuple(bytes.fromhex(target[1:])) for source,target in zip(brown+metal,palette['wood']+palette['trim'])}
    image = original.convert('RGBA');result = Image.new('RGBA', image.size)
    result.putdata([(*mapping.get(p[:3],p[:3]),p[3]) for p in image.getdata()])
    return result
