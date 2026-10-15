param(
    [string]$TextureSource = 'C:/Users/84446/Downloads/5',
    [string]$RootTextureSource = '',
    [string]$WaterGrassTextureSource = 'C:/Users/84446/Downloads/2.png',
    [string]$LilyPadTextureSource = 'C:/Users/84446/Downloads/1 (1).png'
)
$ErrorActionPreference = 'Stop'
$WarningPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
. "$PSScriptRoot/starblight_glow_models.ps1"
$resourceRoot = Join-Path (Split-Path $PSScriptRoot) 'src/main/resources'
$assetRoot = "$resourceRoot/assets/theaurorian2"
$dataRoot = "$resourceRoot/data/theaurorian2"
function Read-Json($path) { Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable }
function Asset($path, $value) { Write-Json "$assetRoot/$path.json" $value }
function Write-Data($path, $value) { Write-Json "$dataRoot/$path.json" $value }
function State($name, $properties = @{}) {
    $value = @{Name="theaurorian2:$name"}
    if ($properties.Count) { $value.Properties=$properties }
    return $value
}
function Provider($name, $properties = @{}) { @{type='minecraft:simple_state_provider';state=(State $name $properties)} }
function Tag($namespace, $kind, $name, $values) {
    $path = "$resourceRoot/data/$namespace/tags/$kind/$name.json"
    $value = if (Test-Path $path) { Read-Json $path } else { @{replace=$false;values=@()} }
    $value.values = @($value.values | Where-Object { $_ -ne 'theaurorian2:starblight_dirt' })
    $value.values = @($value.values) + @($values | Where-Object { $_ -notin $value.values })
    Write-Json $path $value
}
function Self-Loot($name) {
    @{type='minecraft:block';pools=@(@{rolls=1;bonus_rolls=0;conditions=@(@{condition='minecraft:survives_explosion'});
        entries=@(@{type='minecraft:item';name="theaurorian2:$name"})})}
}
function Model($name, $parent, $textures, $cutout=$false) {
    $value=@{parent="minecraft:block/$parent";textures=$textures}
    if ($cutout) { $value.render_type='minecraft:cutout' }
    Asset "models/block/$name" $value
}
function Plain($name) { Asset "blockstates/$name" @{variants=@{''=@{model="theaurorian2:block/$name"}}} }
function Pillar($name) {
    Asset "blockstates/$name" @{variants=@{
        'axis=y'=@{model="theaurorian2:block/$name"};
        'axis=x'=@{model="theaurorian2:block/$name";x=90;y=90};
        'axis=z'=@{model="theaurorian2:block/$name";x=90}}}
}

# Preserve the supplied artwork; the mushroom source needs the user's corrected 90-degree clockwise rotation.
$textures=@{flower='starblight_flower';grass1='starblight_tuft';grass2='starblight_shoot';
    grass_side='starblight_soil_side';grass_top='starblight_soil_top';leaves='starblight_leaves';
    log='starblight_log';log_top='starblight_log_top';mushroom='starblight_shelf_fungus';
    plank='starblight_planks';sapling='starblight_sapling';stripped_log='stripped_starblight_log';
    stripped_log_top='stripped_starblight_log_top';roots='starblight_roots'}
New-Item -ItemType Directory -Force "$assetRoot/textures/block" | Out-Null
foreach ($entry in $textures.GetEnumerator()) {
    $target="$assetRoot/textures/block/$($entry.Value).png"
    $source=Join-Path $TextureSource "$($entry.Key).png"
    if ($entry.Key -eq 'roots') {
        if ($RootTextureSource) { $source=$RootTextureSource }
        elseif (-not (Test-Path -LiteralPath $source) -and (Test-Path -LiteralPath $target)) { $source=$target }
    }
    $bytes=[System.IO.File]::ReadAllBytes($source)
    if ($bytes.Length -lt 33 -or $bytes[0] -ne 137 -or $bytes[1] -ne 80 -or
        $bytes[19] -ne 16 -or $bytes[23] -ne 16) { throw "Expected supplied 16x16 PNG: $source" }
    if ($entry.Key -eq 'mushroom') {
        Add-Type -AssemblyName System.Drawing
        $original=[System.Drawing.Bitmap]::new($source)
        $rotated=$original.Clone()
        try {
            $rotated.RotateFlip([System.Drawing.RotateFlipType]::Rotate90FlipNone)
            $rotated.Save($target,[System.Drawing.Imaging.ImageFormat]::Png)
            $saved=[System.Drawing.Bitmap]::new($target)
            try {
                for ($y=0; $y -lt 16; $y++) {
                    for ($x=0; $x -lt 16; $x++) {
                        if ($original.GetPixel($x,$y).ToArgb() -ne $saved.GetPixel(15-$y,$x).ToArgb()) {
                            throw "Mushroom pixel rotation mismatch at $x,$y"
                        }
                    }
                }
            } finally { $saved.Dispose() }
        } finally { $rotated.Dispose(); $original.Dispose() }
        continue
    }
    if ([System.IO.Path]::GetFullPath($source) -ne [System.IO.Path]::GetFullPath($target)) {
        Copy-Item -LiteralPath $source -Destination $target
    }
    if ((Get-FileHash $source).Hash -ne (Get-FileHash $target).Hash) { throw "Texture copy mismatch: $source" }
}
# Aquatic artwork is copied byte-for-byte, without biome tinting.
foreach ($aquatic in @(@('starblight_water_grass',$WaterGrassTextureSource),@('starblight_lily_pad',$LilyPadTextureSource))) {
    $target="$assetRoot/textures/block/$($aquatic[0]).png"
    $source=$aquatic[1]
    if (-not (Test-Path -LiteralPath $source) -and (Test-Path -LiteralPath $target)) { $source=$target }
    Add-Type -AssemblyName System.Drawing
    $bitmap=[System.Drawing.Bitmap]::new($source)
    try { if ($bitmap.Width -ne 16 -or $bitmap.Height -ne 16) { throw "Expected 16x16 aquatic texture: $source" } }
    finally { $bitmap.Dispose() }
    if ([System.IO.Path]::GetFullPath($source) -ne [System.IO.Path]::GetFullPath($target)) {
        Copy-Item -LiteralPath $source -Destination $target
    }
    if ((Get-FileHash $source).Hash -ne (Get-FileHash $target).Hash) { throw "Texture copy mismatch: $source" }
}
$names=@('starblight_soil','starblight_log','stripped_starblight_log',
    'starblight_wood','stripped_starblight_wood','starblight_planks','starblight_stairs','starblight_slab',
    'starblight_fence','starblight_fence_gate','starblight_leaves','starblight_sapling',
    'starblight_roots','muddy_starblight_roots','starblight_tuft','starblight_shoot','starblight_flower','starblight_shelf_fungus',
    'starblight_water_grass','starblight_lily_pad')
foreach ($name in $names) {
    Plain $name
    Write-Data "loot_table/blocks/$name" (Self-Loot $name)
    Asset "items/$name" @{model=@{type='minecraft:model';model="theaurorian2:block/$name"}}
}
foreach ($name in @('starblight_log','stripped_starblight_log','starblight_wood','stripped_starblight_wood')) {
    $texture=if ($name.StartsWith('stripped')) {'stripped_starblight_log'} else {'starblight_log'}
    $end=if ($name.EndsWith('_wood')) {$texture} else {"${texture}_top"}
    Model $name 'cube_column' @{side="theaurorian2:block/$texture";end="theaurorian2:block/$end"}
    Pillar $name
}
Model 'starblight_planks' 'cube_all' @{all='theaurorian2:block/starblight_planks'}
Model 'starblight_leaves' 'cube_all' @{all='theaurorian2:block/starblight_leaves'} $true
foreach ($name in @('starblight_sapling','starblight_tuft','starblight_shoot','starblight_flower','starblight_water_grass')) {
    if ($name -eq 'starblight_flower') {
        Asset "models/block/$name" (New-StarblightGlowCrossModel "theaurorian2:block/$name" -CentreOnly)
    } else { Model $name 'cross' @{cross="theaurorian2:block/$name"} $true }
    Asset "models/item/$name" @{parent='minecraft:item/generated';textures=@{layer0="theaurorian2:block/$name"}}
    Asset "items/$name" @{model=@{type='minecraft:model';model="theaurorian2:item/$name"}}
}
# Vanilla lily pad geometry, with no tint index so the supplied purple remains intact.
Asset 'models/block/starblight_lily_pad' @{ambientocclusion=$false;render_type='minecraft:cutout';
    textures=@{particle='theaurorian2:block/starblight_lily_pad';texture='theaurorian2:block/starblight_lily_pad'};
    elements=@(@{from=@(0,0.25,0);to=@(16,0.25,16);faces=@{
        up=@{uv=@(0,0,16,16);texture='#texture'};down=@{uv=@(0,16,16,0);texture='#texture'}}})}
Asset 'models/item/starblight_lily_pad' @{parent='minecraft:item/generated';textures=@{layer0='theaurorian2:block/starblight_lily_pad'}}
Asset 'items/starblight_lily_pad' @{model=@{type='minecraft:model';model='theaurorian2:item/starblight_lily_pad'}}
$grassLoot=Read-Json "$dataRoot/loot_table/blocks/aurorian_water_grass.json"
$grassLoot.pools[0].entries[0].name='theaurorian2:starblight_water_grass'
$grassLoot.random_sequence='theaurorian2:blocks/starblight_water_grass'
Write-Data 'loot_table/blocks/starblight_water_grass' $grassLoot
# Ordinary Aurorian dirt backs the ground cover and the mud-coated roots.
$faces=@{}
foreach ($face in @('down','up','north','south','west','east')) {
    $faces[$face]=@{texture='#soil';uv=@(0,0,16,16);cullface=$face}
}
$soilFaces=@{}
foreach ($face in @('down','up','north','south','west','east')) {
    $soilFaces[$face]=@{texture=if ($face -eq 'up') {'#top'} elseif ($face -eq 'down') {'#bottom'} else {'#side'};
        uv=@(0,0,16,16);cullface=$face}
}
Asset 'models/block/starblight_soil' @{parent='minecraft:block/block';
    textures=@{top='theaurorian2:block/starblight_soil_top';side='theaurorian2:block/starblight_soil_side';bottom='theaurorian2:block/aurorian_dirt';
        particle='theaurorian2:block/starblight_soil_side'};elements=@(@{from=@(0,0,0);to=@(16,16,16);faces=$soilFaces})}
# Use vanilla's open, double-sided mangrove root mesh with the supplied root artwork.
Asset 'models/block/starblight_roots' @{parent='minecraft:block/mangrove_roots';render_type='minecraft:cutout';
    textures=@{side='theaurorian2:block/starblight_roots';top='theaurorian2:block/starblight_roots';
        particle='theaurorian2:block/starblight_roots'}}
$muddyRootFaces=@{}
foreach ($face in @('down','up','north','south','west','east')) {
    $muddyRootFaces[$face]=@{texture='#root';uv=@(0,0,16,16);cullface=$face}
}
Asset 'models/block/muddy_starblight_roots' @{parent='minecraft:block/block';render_type='minecraft:cutout';
    textures=@{soil='theaurorian2:block/aurorian_dirt';root='theaurorian2:block/starblight_roots';
        particle='theaurorian2:block/aurorian_dirt'};elements=@(
            @{from=@(0,0,0);to=@(16,16,16);faces=$faces},
            @{from=@(0,0,0);to=@(16,16,16);faces=$muddyRootFaces})}
Pillar 'muddy_starblight_roots'
Asset 'models/block/starblight_shelf_fungus' @{parent='minecraft:block/cross';render_type='minecraft:cutout';
    textures=@{cross='theaurorian2:block/starblight_shelf_fungus'}}
$fungusVariants=@{}
foreach ($facing in @('north','east','south','west')) {
    $rotation=@{north=0;east=90;south=180;west=270}[$facing]
    foreach ($variant in 0..4) {
        $fungusVariants["facing=$facing,variant=$variant,wall=true"]=@{model='theaurorian2:block/starblight_shelf_fungus';x=90;y=$rotation}
        $fungusVariants["facing=$facing,variant=$variant,wall=false"]=@{model='theaurorian2:block/starblight_shelf_fungus';y=$rotation}
    }
}
Asset 'blockstates/starblight_shelf_fungus' @{variants=$fungusVariants}
Asset 'models/item/starblight_shelf_fungus' @{parent='minecraft:item/generated';textures=@{layer0='theaurorian2:block/starblight_shelf_fungus'}}
Asset 'items/starblight_shelf_fungus' @{model=@{type='minecraft:model';model='theaurorian2:item/starblight_shelf_fungus'}}
foreach ($kind in @('stairs','slab','fence','fence_gate')) {
    $old="silent_wood_$kind"; $new="starblight_$kind"
    foreach ($file in Get-ChildItem "$assetRoot/models/block/${old}*.json") {
        $text=(Get-Content $file.FullName -Raw).Replace('silent_tree_planks','starblight_planks').Replace($old,$new)
        Asset "models/block/$($file.BaseName.Replace($old,$new))" ($text | ConvertFrom-Json -AsHashtable)
    }
    foreach ($category in @('blockstates','models/item','items')) {
        $text=(Get-Content "$assetRoot/$category/$old.json" -Raw).Replace($old,$new)
        Asset "$category/$new" ($text | ConvertFrom-Json -AsHashtable)
    }
    $loot=(Get-Content "$dataRoot/loot_table/blocks/$old.json" -Raw).Replace($old,$new)
    Write-Data "loot_table/blocks/$new" ($loot | ConvertFrom-Json -AsHashtable)
    $recipe=(Get-Content "$dataRoot/recipe/$old.json" -Raw).Replace('silent_tree_planks','starblight_planks').Replace($old,$new).Replace('theaurorian2:silent_wood_stick','minecraft:stick')
    Write-Data "recipe/$new" ($recipe | ConvertFrom-Json -AsHashtable)
}
$leafLoot=(Get-Content "$dataRoot/loot_table/blocks/silent_tree_leaves.json" -Raw).Replace('silent_tree_leaves','starblight_leaves').Replace('silent_tree_sapling','starblight_sapling').Replace('theaurorian2:silent_wood_stick','minecraft:stick')
Write-Data 'loot_table/blocks/starblight_leaves' ($leafLoot | ConvertFrom-Json -AsHashtable)
Write-Data 'loot_table/blocks/starblight_soil' @{type='minecraft:block';pools=@(@{rolls=1;bonus_rolls=0;
    entries=@(@{type='minecraft:alternatives';children=@(
        @{type='minecraft:item';name='theaurorian2:starblight_soil';conditions=@(@{condition='minecraft:match_tool';predicate=@{predicates=@{'minecraft:enchantments'=@(@{enchantments='minecraft:silk_touch';levels=@{min=1}})}}})},
        @{type='minecraft:item';name='theaurorian2:aurorian_dirt'})});conditions=@(@{condition='minecraft:survives_explosion'})})}
Write-Data 'recipe/starblight_planks' @{type='minecraft:crafting_shapeless';category='building';ingredients=@('#theaurorian2:starblight_logs');result=@{id='theaurorian2:starblight_planks';count=4}}
foreach ($prefix in @('','stripped_')) {
    Write-Data "recipe/${prefix}starblight_wood" @{type='minecraft:crafting_shaped';category='building';
        key=@{'#'="theaurorian2:${prefix}starblight_log"};pattern=@('##','##');result=@{id="theaurorian2:${prefix}starblight_wood";count=3}}
}

$logs=@('starblight_log','stripped_starblight_log','starblight_wood','stripped_starblight_wood') | ForEach-Object {"theaurorian2:$_"}
foreach ($kind in @('block','item')) {
    Tag 'theaurorian2' $kind 'starblight_logs' $logs
    Tag 'minecraft' $kind 'logs' @('#theaurorian2:starblight_logs')
    Tag 'minecraft' $kind 'logs_that_burn' @('#theaurorian2:starblight_logs')
    foreach ($entry in @(@('planks','starblight_planks'),@('leaves','starblight_leaves'),@('saplings','starblight_sapling'),
        @('wooden_stairs','starblight_stairs'),@('wooden_slabs','starblight_slab'),@('wooden_fences','starblight_fence'),@('fence_gates','starblight_fence_gate'))) {
        Tag 'minecraft' $kind $entry[0] @("theaurorian2:$($entry[1])")
    }
}
$ground=@('theaurorian2:starblight_soil','theaurorian2:aurorian_dirt')
Tag 'theaurorian2' 'block' 'starblight_fungus_supports' @('#theaurorian2:starblight_logs',
    'theaurorian2:starblight_soil','theaurorian2:starblight_roots','theaurorian2:muddy_starblight_roots')
Tag 'minecraft' 'block' 'dirt' $ground
Tag 'minecraft' 'block' 'mineable/shovel' ($ground + @('theaurorian2:muddy_starblight_roots'))
Tag 'minecraft' 'block' 'mineable/hoe' @('theaurorian2:starblight_leaves')
Tag 'minecraft' 'block' 'mineable/axe' ($logs + @('theaurorian2:starblight_planks','theaurorian2:starblight_roots',
    'theaurorian2:starblight_stairs','theaurorian2:starblight_slab','theaurorian2:starblight_fence','theaurorian2:starblight_fence_gate'))
Tag 'minecraft' 'block' 'overworld_carver_replaceables' $ground
Tag 'minecraft' 'block' 'cannot_replace_below_tree_trunk' @('theaurorian2:starblight_roots','theaurorian2:muddy_starblight_roots')
# This swamp has no natural frogs. Remove only this generator's former substrate additions.
$frogTag="$resourceRoot/data/minecraft/tags/block/frogs_spawnable_on.json"
if (Test-Path $frogTag) {
    $tag=Get-Content $frogTag -Raw | ConvertFrom-Json -AsHashtable
    $tag.values=@($tag.values | Where-Object {$_ -notin @('theaurorian2:starblight_soil',
        'theaurorian2:aurorian_dirt','theaurorian2:starblight_dirt','theaurorian2:muddy_starblight_roots')})
    if ($tag.values.Count -eq 0) { Remove-Item -LiteralPath $frogTag }
    else { Write-Json $frogTag $tag }
}
$flora=@('theaurorian2:starblight_tuft','theaurorian2:starblight_shoot','theaurorian2:starblight_flower','theaurorian2:starblight_sapling','theaurorian2:starblight_leaves')
Tag 'minecraft' 'block' 'replaceable_by_trees' $flora
Tag 'minecraft' 'block' 'replaceable_by_trees' @('theaurorian2:starblight_water_grass')
Tag 'minecraft' 'block' 'replaceable' @('theaurorian2:starblight_water_grass')
Tag 'theaurorian2' 'block' 'starblight_logs_can_grow_through' $flora
Tag 'theaurorian2' 'block' 'starblight_roots_can_grow_through' ($flora + $ground + @('theaurorian2:starblight_roots','theaurorian2:muddy_starblight_roots'))

function Tree($tall) {
    $trunk=@{type='minecraft:upwards_branching_trunk_placer';base_height=7;height_rand_a=2;height_rand_b=3;
        extra_branch_steps=@{type='minecraft:uniform';min_inclusive=1;max_inclusive=4};
        extra_branch_length=@{type='minecraft:uniform';min_inclusive=0;max_inclusive=1};
        place_branch_per_log_probability=0.35;can_grow_through='#theaurorian2:starblight_logs_can_grow_through'}
    $offset=@{type='minecraft:uniform';min_inclusive=3;max_inclusive=4}
    if ($tall) { $trunk.base_height=10;$trunk.height_rand_b=4;$trunk.extra_branch_steps.max_inclusive=6;$offset.min_inclusive=4;$offset.max_inclusive=5 }
    @{type='minecraft:tree';config=@{
        trunk_provider=(Provider 'starblight_log' @{axis='y'});trunk_placer=$trunk;
        foliage_provider=(Provider 'starblight_leaves' @{distance='7';persistent='false';waterlogged='false'});
        foliage_placer=@{type='minecraft:random_spread_foliage_placer';radius=3;offset=0;foliage_height=2;leaf_placement_attempts=70};
        root_placer=@{type='minecraft:mangrove_root_placer';trunk_offset_y=$offset;root_provider=(Provider 'starblight_roots' @{waterlogged='false'});
            mangrove_root_placement=@{can_grow_through='#theaurorian2:starblight_roots_can_grow_through';muddy_roots_in=($ground + @('theaurorian2:muddy_starblight_roots'));
                muddy_roots_provider=(Provider 'muddy_starblight_roots' @{axis='y'});max_root_width=8;max_root_length=15;random_skew_chance=0.2}};
        minimum_size=@{type='minecraft:two_layers_feature_size';limit=if ($tall) {3} else {2};lower_size=0;upper_size=2};
        below_trunk_provider=@{type='minecraft:rule_based_state_provider';rules=@(@{
            if_true=@{type='minecraft:not';predicate=@{type='minecraft:matching_block_tag';tag='minecraft:cannot_replace_below_tree_trunk'}};
            then=(Provider 'aurorian_dirt')})};
        decorators=@(@{type='theaurorian2:starblight_leaf_support'},
            @{type='theaurorian2:starblight_trunk_fungus';chance=0.55});ignore_vines=$true}}
}
Write-Data 'worldgen/configured_feature/short_starblight_tree' (Tree $false)
Write-Data 'worldgen/configured_feature/tall_starblight_tree' (Tree $true)
foreach ($size in @('short','tall')) { Write-Data "worldgen/placed_feature/${size}_starblight_tree" @{feature="theaurorian2:${size}_starblight_tree";placement=@()} }
Write-Data 'worldgen/configured_feature/starblight_tree' @{type='minecraft:random_selector';config=@{
    features=@(@{feature='theaurorian2:tall_starblight_tree';chance=0.35});default='theaurorian2:short_starblight_tree'}}
Write-Data 'worldgen/placed_feature/trees_starblight_swamp' @{feature='theaurorian2:starblight_tree';placement=@(
    @{type='minecraft:count';count=9},@{type='minecraft:in_square'},
    @{type='minecraft:surface_water_depth_filter';max_water_depth=5},
    @{type='minecraft:heightmap';heightmap='OCEAN_FLOOR'},@{type='minecraft:biome'})}
foreach ($plant in @('starblight_tuft','starblight_shoot','starblight_flower')) {
    Write-Data "worldgen/configured_feature/patch_$plant" @{type='minecraft:simple_block';config=@{to_place=(Provider $plant)}}
    $patch=Read-Json "$dataRoot/worldgen/placed_feature/patch_silent_forest_grass.json"
    $patch.feature="theaurorian2:patch_$plant"
    $patch.placement[0].count=if ($plant -eq 'starblight_tuft') {3} elseif ($plant -eq 'starblight_shoot') {2} else {1}
    $patch.placement[-1].predicate.predicates[1].blocks='theaurorian2:starblight_soil'
    Write-Data "worldgen/placed_feature/patch_$plant" $patch
}
Write-Data 'worldgen/configured_feature/patch_starblight_fungus' @{type='minecraft:simple_block';config=@{
    to_place=(Provider 'starblight_shelf_fungus' @{facing='north';variant='0';wall='false'})}}
Write-Data 'worldgen/placed_feature/patch_starblight_fungus' @{feature='theaurorian2:patch_starblight_fungus';placement=@(
    @{type='minecraft:count';count=1},@{type='minecraft:in_square'},
    @{type='minecraft:heightmap';heightmap='WORLD_SURFACE_WG'},@{type='minecraft:count';count=8},
    @{type='minecraft:random_offset';xz_spread=@{type='minecraft:trapezoid';min=-7;max=7;plateau=0};
        y_spread=@{type='minecraft:trapezoid';min=-3;max=3;plateau=0}},
    @{type='minecraft:biome'},@{type='minecraft:block_predicate_filter';predicate=@{type='minecraft:all_of';predicates=@(
        @{type='minecraft:matching_block_tag';tag='minecraft:air'},
        @{type='minecraft:would_survive';state=(State 'starblight_shelf_fungus' @{facing='north';variant='0';wall='false'})})}})}
# Keep all random positions ahead of the final biome/survival filters.
foreach ($aquatic in @('starblight_water_grass','starblight_lily_pad')) {
    Write-Data "worldgen/configured_feature/patch_$aquatic" @{type='minecraft:simple_block';config=@{to_place=(Provider $aquatic)}}
    $placement=@(@{type='minecraft:count';count=if ($aquatic -eq 'starblight_water_grass') {48} else {6}},
        @{type='minecraft:in_square'})
    if ($aquatic -eq 'starblight_water_grass') { $placement+=@(@{type='minecraft:surface_water_depth_filter';max_water_depth=4}) }
    $placement+=@(@{type='minecraft:heightmap';heightmap=if ($aquatic -eq 'starblight_water_grass') {'OCEAN_FLOOR_WG'} else {'WORLD_SURFACE_WG'}},
        @{type='minecraft:biome'},@{type='minecraft:block_predicate_filter';predicate=@{type='minecraft:all_of';predicates=@(
            $(if ($aquatic -eq 'starblight_water_grass') { @{type='minecraft:all_of';predicates=@(
                    @{type='minecraft:matching_blocks';blocks='minecraft:water'},
                    @{type='minecraft:matching_fluids';fluids='minecraft:water'})} }
                else { @{type='minecraft:matching_block_tag';tag='minecraft:air'} }),
            @{type='minecraft:would_survive';state=(State $aquatic)})}})
    Write-Data "worldgen/placed_feature/patch_$aquatic" @{feature="theaurorian2:patch_$aquatic";placement=$placement}
}
$biome=Read-Json "$dataRoot/worldgen/biome/silent_wood_forest.json"
$biome.temperature=0.8;$biome.downfall=0.9
$biome.attributes['minecraft:visual/water_fog_color']='#282039'
$biome.attributes['minecraft:visual/water_fog_end_distance']=@{modifier='multiply';argument=0.85}
$biome.attributes['theaurorian2:local_fog_strength']=1.0
$biome.attributes['theaurorian2:local_fog_color']='#614578'
$biome.attributes['theaurorian2:local_fog_start_distance']=8.0
$biome.attributes['theaurorian2:local_fog_end_distance']=56.0
$biome.attributes['minecraft:gameplay/increased_fire_burnout']=$true
$biome.attributes['minecraft:audio/background_music']=@{default=@{sound='minecraft:music.overworld.swamp';min_delay=12000;max_delay=24000}}
$biome.effects=@{water_color='#594875';grass_color='#775a96';foliage_color='#88cbd8'}
$biome.features[6]=@($biome.features[6] | Where-Object {$_ -notmatch ':disk_'})
$biome.features[9]=@('theaurorian2:trees_starblight_swamp','theaurorian2:patch_starblight_tuft','theaurorian2:patch_starblight_shoot','theaurorian2:patch_starblight_flower','theaurorian2:patch_starblight_fungus',
    'theaurorian2:patch_starblight_water_grass','theaurorian2:patch_starblight_lily_pad')
$biome.spawners.creature=@()
Write-Data 'worldgen/biome/starblight_swamp' $biome
foreach ($tag in @('is_aurorian','has_surface_fish')) { Tag 'theaurorian2' 'worldgen/biome' $tag @('theaurorian2:starblight_swamp') }
Tag 'c' 'worldgen/biome' 'is_swamp' @('theaurorian2:starblight_swamp')
# Recheck the destination after the existing patch's random offset, so neighboring water plants cannot spill in.
$waterPatch=Read-Json "$dataRoot/worldgen/placed_feature/aurorian_water_surface_plants.json"
if ($waterPatch.placement[-1].type -ne 'minecraft:biome') {
    $waterPatch.placement+=@(@{type='minecraft:biome'})
    Write-Data 'worldgen/placed_feature/aurorian_water_surface_plants' $waterPatch
}

# Normal vanilla wetland climate selection; no rare-island or random rarity gate.
$dimension=Read-Json "$dataRoot/dimension/the_aurorian.json"
$dimension.generator.biome_source.delegate.replacements['minecraft:swamp']=if (Test-Path -LiteralPath "$dataRoot/worldgen/biome/aurorian_swamp.json") {'theaurorian2:aurorian_swamp'} else {'theaurorian2:starblight_swamp'}
$dimension.generator.biome_source.delegate.replacements['minecraft:mangrove_swamp']='theaurorian2:starblight_swamp'
Write-Data 'dimension/the_aurorian' $dimension
$preset=Read-Json "$dataRoot/worldgen/world_preset/aurorian.json"
$source=$preset.dimensions['minecraft:overworld'].generator.biome_source.delegate
if ($source.type -eq 'theaurorian2:aurorian_climate') { $source=$source.land }
foreach ($depth in @(0,1)) {
    $entry=@{biome='theaurorian2:starblight_swamp';parameters=@{
        temperature=@(0.3,2);humidity=@(0.2,2);continentalness=@(-0.19,0.3);erosion=@(0.45,2);weirdness=@(-2,0.9);depth=$depth;offset=0}}
    if ($source.biomes) {
        if (-not @($source.biomes | Where-Object {$_.biome -eq 'theaurorian2:starblight_swamp' -and $_.parameters.depth -eq $depth}).Count) { $source.biomes+=@($entry) }
    } else { throw 'Unexpected Aurorian world preset biome source' }
}
Write-Data 'worldgen/world_preset/aurorian' $preset

function Block-Rule($name) { @{type='minecraft:block';result_state=(State $name)} }
function If-Rule($condition,$rule) { @{type='minecraft:condition';if_true=$condition;then_run=$rule} }
foreach ($settings in @('the_aurorian','the_aurorian_lake_islands')) {
    $noise=Read-Json "$dataRoot/worldgen/noise_settings/$settings.json"
    $wetSurface=If-Rule @{type='minecraft:water';offset=0;surface_depth_multiplier=0;add_stone_depth=$false} (Block-Rule 'starblight_soil')
    $pond=If-Rule @{type='minecraft:y_above';anchor=@{absolute=60};surface_depth_multiplier=0;add_stone_depth=$false} (
        If-Rule @{type='minecraft:not';invert=@{type='minecraft:y_above';anchor=@{absolute=63};surface_depth_multiplier=0;add_stone_depth=$false}} (
        If-Rule @{type='minecraft:noise_threshold';noise='minecraft:surface_swamp';min_threshold=0;max_threshold=[double]::MaxValue} @{type='minecraft:block';result_state=@{Name='minecraft:water';Properties=@{level='0'}}}))
    $floor=If-Rule @{type='minecraft:stone_depth';offset=0;surface_type='floor';add_surface_depth=$false;secondary_depth_range=0} @{type='minecraft:sequence';sequence=@($pond,$wetSurface,(Block-Rule 'aurorian_dirt'))}
    $under=If-Rule @{type='minecraft:stone_depth';offset=0;surface_type='floor';add_surface_depth=$true;secondary_depth_range=0} (Block-Rule 'aurorian_dirt')
    $surface=If-Rule @{type='minecraft:biome';biome_is=@('theaurorian2:starblight_swamp')} (
        If-Rule @{type='minecraft:above_preliminary_surface'} @{type='minecraft:sequence';sequence=@($floor,$under)})
    $others=@($noise.surface_rule.sequence | Where-Object { $_.if_true.biome_is -notcontains 'theaurorian2:starblight_swamp' })
    $noise.surface_rule.sequence=@($others[0],$surface)+@($others | Select-Object -Skip 1)
    Write-Data "worldgen/noise_settings/$settings" $noise
}
$zh=@{starblight_swamp='星蚀沼泽';starblight_soil='星蚀覆土';
    starblight_log='星蚀原木';stripped_starblight_log='去皮星蚀原木';starblight_wood='星蚀木';
    stripped_starblight_wood='去皮星蚀木';starblight_planks='星蚀木板';starblight_stairs='星蚀木楼梯';
    starblight_slab='星蚀木台阶';starblight_fence='星蚀木栅栏';starblight_fence_gate='星蚀木栅栏门';
    starblight_leaves='星蚀树叶';starblight_sapling='星蚀树苗';starblight_roots='星蚀树根';
    muddy_starblight_roots='沾泥的星蚀树根';starblight_tuft='辉芽簇';starblight_shoot='蚀辉枝';
    starblight_flower='荧星花';starblight_shelf_fungus='星斑菌';starblight_water_grass='星蚀水草';starblight_lily_pad='星蚀睡莲'}
foreach ($locale in @('zh_cn','en_us')) {
    $lang=Read-Json "$assetRoot/lang/$locale.json"
    $lang.Remove('block.theaurorian2.starblight_dirt') | Out-Null
    foreach ($name in $zh.Keys) {
        $key=if ($name -eq 'starblight_swamp') {'biome'} else {'block'}
        $english=(Get-Culture 'en-US').TextInfo.ToTitleCase($name.Replace('_',' '))
        $lang["$key.theaurorian2.$name"]=if ($locale -eq 'zh_cn') {$zh[$name]} else {$english}
    }
    Asset "lang/$locale" $lang
}
# The old ID is now a block/item registry alias, so its independent assets are obsolete.
foreach ($relative in @('assets/theaurorian2/blockstates/starblight_dirt.json',
        'assets/theaurorian2/models/block/starblight_dirt.json','assets/theaurorian2/models/item/starblight_dirt.json',
        'assets/theaurorian2/items/starblight_dirt.json','data/theaurorian2/loot_table/blocks/starblight_dirt.json')) {
    $legacyPath=[System.IO.Path]::GetFullPath((Join-Path $resourceRoot $relative))
    $rootPath=[System.IO.Path]::GetFullPath($resourceRoot)+[System.IO.Path]::DirectorySeparatorChar
    if (-not $legacyPath.StartsWith($rootPath,[System.StringComparison]::OrdinalIgnoreCase)) { throw 'Legacy asset path escaped resources' }
    if (Test-Path -LiteralPath $legacyPath) { Remove-Item -LiteralPath $legacyPath }
}
Write-Output 'Generated Starblight Swamp: 20 blocks, shared Aurorian dirt, taller mangrove trees, aquatic grass and lily pads.'
