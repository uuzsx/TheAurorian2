"""Sample log-side colors for furniture trim, preserving the authored grain."""
from pathlib import Path

from PIL import Image


TEXTURES = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/theaurorian2/textures/block'


def bark_trim(image, plank):
    """Transfer the source's ordered color steps to its wood's actual bark palette."""
    log = TEXTURES / (plank.removesuffix('_planks') + '_log.png')
    with Image.open(log) as bark:
        colors = sorted(set(bark.convert('RGB').getdata()), key=lambda c: (sum(c), c))
    if plank == 'filthy_tree_planks':
        # Sample the purple bark itself, not the sparse green surface growth.
        colors = [color for color in colors if color[2] > color[0] > color[1]]
    if len(colors) < 2:
        raise ValueError(f'No usable bark color ramp in {log}')
    source = image.convert('RGB')
    original = sorted(set(source.getdata()), key=lambda c: (sum(c), c))
    mapping = {
        color: colors[round(index * (len(colors) - 1) / max(1, len(original) - 1))]
        for index, color in enumerate(original)
    }
    result = Image.new('RGB', source.size)
    result.putdata([mapping[color] for color in source.getdata()])
    return result
