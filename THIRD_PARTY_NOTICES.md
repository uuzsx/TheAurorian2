# Third-Party Notices

## Original Aurorian runestone dungeon

The nine runestone dungeon templates come from the user-designated
`TheAurorian-NeoForge-1.21/TheAurorian-NeoForge-1.21` source. Architectural
block positions, materials and state properties are retained with updated
namespaces. Export-only air, jigsaw connectors and obsolete default gate
block-entity data are removed. Deterministic shared-center fragments preserve
the complete large building within vanilla's chunk-reference limits; the new
terrain foundation follows the original ground-contact silhouette.
Existing asset terms continue to apply.

## Original Aurorian armor

The legacy armor meshes and textures come from the user-designated
`TheAurorian-NeoForge-1.21/TheAurorian-NeoForge-1.21` source. The eight Java
mesh definitions preserve their geometry-builder arguments; the empty hat node
is nested under the head for the current humanoid API. Existing waist, skirt,
trouser and boot cubes are assigned to their matching equipment slots without
changing their geometry, UVs or pivots. The holy knight uses its
original GeckoLib geometry and atlas, with a torso attachment for its continuous
robe. Original animation data is retained as a reference; its static test poses
are not applied over player movement. Existing asset terms continue to apply.

## Assassin armor expansion

The five assassin armor sets use the project owner's supplied assassin armor
expansion (`2326刺客铠甲扩展版`), with each distinct `raw` Blockbench geometry
and its corresponding original standalone PNG. Geometry, per-face UVs, cube
rotations, inflation and authored pivots are retained through the GeckoLib
coordinate conversion. A parent attachment binds the mask to the player's neck;
item display transforms frame each equipment piece separately. Original asset
terms continue to apply; the project license does not relicense these assets.

## Archer Armor & Weapons Pack ranged weapons

The twelve ranger bows and crossbows use the project owner's supplied
Archer Armor & Weapons Pack, from its `raw models` directory. Original geometry,
UVs, colors, hand transforms and draw/load state models are retained. Inventory
transforms are centered and crossbow icons resized to fit their slots. Resource
identifiers are namespaced for The Aurorian 2. These assets remain subject to their original
terms; the project license does not relicense them.

## Magic Store blacksmith storage crates

The fifteen storage crates use `blacksmith_box_1`, `blacksmith_box_2` and
`blacksmith_box_3` from the project owner's supplied Magic Store blacksmith pack
by atcpybd. Geometry, rotations, pivots, UVs and display transforms are retained.
Wood colors use the approved cool Aurorian furniture ramps; labels retain
the original colors and iron details use coordinated chest trim colors. The crates share the blacksmith
texture atlas with the storage barrels. Original asset terms continue to apply;
the project license does not relicense these assets.

## Magic Store blacksmith storage barrels

The ten wood storage barrels use `blacksmith_barrel_1` and `blacksmith_barrel_3`
from the project owner's supplied Magic Store blacksmith pack, authored by atcpybd.
Original geometry, pivots, UVs, display transforms and pixel detail are retained;
the wood color ramp uses approved cool Aurorian colors, while the hoops retain
their pixel layout and use the corresponding chest trim colors.
Untextured internal faces marked `#missing` by the JSON exporter are omitted from
runtime models, matching their untextured Blockbench source faces.
These assets remain subject to their original terms; the project license does
not relicense them.

The project skeleton contains files derived from the NeoForged MDK template.
Those template portions are licensed as follows:

## NeoForged MDK

MIT License

Copyright (c) 2023 NeoForged project

This license applies to the template files as supplied by github.com/NeoForged/MDK

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Adventurer Tools models and recolored textures

The models for the Aurorian wooden and stone pickaxes, axes, hoes, and shovels originate from
Adventurer Tools v8 by Akaleaf, supplied by the project owner from MCModels Marketplace.
The corresponding textures are recolored adaptations. Model geometry, UVs, and
item display transforms are retained from the supplied assets.

Original creator: Akaleaf
- Marketplace: mcmodels.net
- Discord: akaleaf#4723 / discord.gg/jpbHGee
- X: x.com/akaleafwastaken

These assets remain subject to their original asset license; the project license
and the NeoForged MDK MIT notice above do not relicense them.

## ShizuArt medieval water buckets

The five wooden water buckets use the project owner's supplied ShizuArt medieval
bathroom models `seau` and `seau_remplie`. Their geometry, signed inner surfaces,
UVs, transparent cutouts, water plane and item display transforms are retained.
The brown wood ramp in `baquet2` is recolored from each matching Aurorian plank;
original dark hoops and blue water remain. These assets remain subject to their
original terms; the project license does not relicense them.

## ShizuArt medieval ceramic jug

The Aurorian Jug uses `cruche_terre_cuite` from the project owner's supplied
ShizuArt medieval bathroom pack. The ceramic colors are sampled from the Aurorian
brick item texture. Original geometry, UVs, pivots, handle planes, water alpha and
display transforms are retained; the empty variant hides only the water plane.
These assets remain subject to their original terms; the project license does
not relicense them.

## ShizuArt medieval long mirrors

The five wooden long mirrors use the supplied ShizuArt `long_miroir` model.
Only the frame's brown color ramp is mapped to the matching Aurorian plank palette;
the original blue mirror, transparency and texture resolution are retained.
The model is split vertically with matching UVs for two-block wall placement.
Original geometry, reversed inner surfaces and item display transforms are retained.
These assets remain subject to their original terms; the project license does not relicense them.

## ShizuArt medieval bathroom seats and tables

The five wood stool, bench, small-table and long-table variants use the project owner's supplied
ShizuArt medieval bathroom pack models `tabouret`, `banc`, `petite_table` and `table`, with the shared
`baquet2` texture recolored from each Aurorian plank palette. The source files
credit "Made with Blockbench by ShizuArt". Geometry, signed inner surfaces,
transparent cutouts and face UV rotations are retained. The long table and bench are translated
and split at the block seam with matching UVs for two-block placement. The bench
and small table retain the source inventory transforms; long-table inventory presentation
is adapted to the mod's block items. These assets remain subject to their original terms;
the project license does not relicense them.

## Magic Store carpenter workbench

The carpenter workbench uses the project owner's supplied Magic Store / atcpybd
`blacksmith_table_1_2x1` model. Original tools, paper, geometry, pivots, UVs and item
display transforms are retained. The world model is translated and its tabletop
is split at the two-block seam with continuous UVs. Wood and handle color ramps come from the
Aurorian crafting-table model texture; metal heads and paper retain their original
pixels, and original pixel resolution and alpha remain.
These assets remain subject to their original terms; the project license does
not relicense them.

## Scene Wind Chimes

The amethyst and bamboo wind chimes use the project owner's supplied `1316场景风铃`
Blockbench models, embedded textures and OGG audio. The original geometry, bone
hierarchy, UV mapping, animation keyframes and texture/audio bytes are preserved.
The source pack's author-local MP3 reference and MythicMobs timeline command are
replaced with namespaced in-game sound events. These third-party assets remain
subject to their original terms; the project license does not relicense them.
## Crates Pack v2 level 1 wood chests

The five wood chests use the project owner's supplied Crates Pack v2
`crate_lvl1.bbmodel` geometry and embedded 128x128 texture. The authored wood and metal palettes are replaced with the approved Aurorian
furniture ramps; pixel placement and transparency remain unchanged. The visible source model is uniformly fitted and rotated for
block placement. Only the single chest is imported, and the existing chest
renderer animates the lid to the source's 105-degree opening angle. No key items
are imported. These assets remain subject to their original terms; the project
license does not relicense them.

## Furniture color adaptation

The wood furniture now uses five authored Aurorian color ramps instead of direct
plank sampling. Source geometry, UVs and pixel layouts are retained. Water bucket
hoops keep their original colors; mirror surfaces, water, carpenter paper and metal
tool heads are unchanged. The level-1 chests, crate frames and storage barrel
hoops use coordinated trim ramps. Willow and cursed frost use the approved
light cool palette, including their building blocks, signs and wood cores.
Original log bark is retained. Curtain wood uses its original plank and furniture
colors; its current chest geometry retains original-source metal colors. Silent and filthy
furniture coordinate with the unchanged plank colors. Paper labels on the two
light storage crate variants use cool parchment colors.

## Reqium sword first-person animations

The Moonsilver Great Sword adapts the project owner's supplied EliteCreatures
"The Reqium Netherite Sword" arm/sword rig and nine motion animation clips.
The source sword is replaced by the existing Aurorian greatsword;
the arm uses the current player's skin. A native camera-space render pass replaces
ModelEngine's world-space billboard, and vanilla sound events replace MythicMobs
timeline commands. Attack effect bones/textures and the pack's damage/AI configuration
are not imported. These
third-party assets remain subject to their original terms and are not relicensed
by this project's license.

## ShizuArt dungeon skeleton decorations

The fourteen dungeon props use the project owner's supplied ShizuArt
`2036地下城骷髅` pack (ItemsAdder `dungeon_skeletons_props` models and texture).
The original texture bytes, geometry, signed inner faces, UVs, rotations and
item display transforms are retained. World geometry and pivots are translated
into occupied block cells without rescaling; the overhanging skeleton retains
its lowered head and arms. Original plugin configuration is replaced by native
placement and loot rules. These assets remain subject to their original terms;
the project license does not relicense them.

## World Scroll teleport animation

The World Scroll uses the blue model, two animation clips and tp2.ogg from the
project owner's supplied 1731 teleport animation pack (TugkanDeMan asset namespace).
Geometry, UVs, inverted hulls and texture pixels are retained. The sample player
and hitbox are omitted; the actual player's skin and equipment are rendered.
Java controls cancellation, cross-dimension travel and retimes the two clips.
The supplied sound is retimed into departure and arrival cues to match those
timelines without shifting pitch; their tails fade with the remaining particles.
The identical diffuse/emissive texture is rendered with a single emissive pass.
These supplied assets remain subject to their original terms and are not
relicensed by this project's license.
