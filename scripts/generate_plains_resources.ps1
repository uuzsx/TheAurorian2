$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$assets = Join-Path $root 'src/main/resources/assets/theaurorian2'
$data = Join-Path $root 'src/main/resources/data/theaurorian2'

function Write-Json($path, $value) {
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    $value | ConvertTo-Json -Depth 40 | Set-Content -LiteralPath $path -Encoding utf8
}

$singlePlants = @('aurorian_grass','aurorian_grass_light','petunia_plant','nebula_blossom_cluster','moon_frost_flower','void_candle_flower')
foreach ($name in $singlePlants) {
    Write-Json (Join-Path $assets "blockstates/$name.json") @{variants=@{''=@{model="theaurorian2:block/$name"}}}
    Write-Json (Join-Path $assets "models/block/$name.json") @{parent='minecraft:block/cross';render_type='minecraft:cutout';textures=@{cross="theaurorian2:block/$name"}}
    Write-Json (Join-Path $assets "models/item/$name.json") @{parent='minecraft:item/generated';textures=@{layer0="theaurorian2:block/$name"}}
    Write-Json (Join-Path $assets "items/$name.json") @{model=@{type='minecraft:model';model="theaurorian2:item/$name"}}
}

$tall='tall_aurorian_grass'
Write-Json (Join-Path $assets "blockstates/$tall.json") @{variants=@{'half=lower'=@{model="theaurorian2:block/${tall}_lower"};'half=upper'=@{model="theaurorian2:block/${tall}_upper"}}}
foreach ($half in @('lower','upper')) {
    Write-Json (Join-Path $assets "models/block/${tall}_$half.json") @{parent='minecraft:block/tinted_cross';render_type='minecraft:cutout';textures=@{cross="theaurorian2:block/${tall}_$half"}}
}
Write-Json (Join-Path $assets "models/item/$tall.json") @{parent='minecraft:item/generated';textures=@{layer0="theaurorian2:block/${tall}_upper"}}
Write-Json (Join-Path $assets "items/$tall.json") @{model=@{type='minecraft:model';model="theaurorian2:item/$tall"}}

function Simple-Loot($name, $drop) {
    Write-Json (Join-Path $data "loot_table/blocks/$name.json") @{
        type='minecraft:block'; pools=@(@{rolls=1.0;bonus_rolls=0.0;conditions=@(@{condition='minecraft:survives_explosion'});entries=@(@{type='minecraft:item';name="theaurorian2:$drop"})});random_sequence="theaurorian2:blocks/$name"
    }
}
function Shears-Loot($name, $drop) {
    $shears = @{condition='minecraft:match_tool';predicate=@{items='minecraft:shears'}}
    $silkTouch = @{
        condition='minecraft:match_tool'
        predicate=@{predicates=@{
            'minecraft:enchantments'=@(@{enchantments='minecraft:silk_touch';levels=@{min=1}})
        }}
    }
    $entry = @{
        type='minecraft:item'
        name="theaurorian2:$drop"
        conditions=@(@{condition='minecraft:any_of';terms=@($shears,$silkTouch)})
    }
    $loot = @{
        type='minecraft:block'
        pools=@(@{rolls=1.0;bonus_rolls=0.0;entries=@($entry)})
        random_sequence="theaurorian2:blocks/$name"
    }
    Write-Json (Join-Path $data "loot_table/blocks/$name.json") $loot
}
foreach ($name in @('petunia_plant','nebula_blossom_cluster','moon_frost_flower','void_candle_flower')) { Simple-Loot $name $name }
Shears-Loot 'aurorian_grass' 'aurorian_grass'
Shears-Loot 'aurorian_grass_light' 'aurorian_grass_light'
Shears-Loot 'tall_aurorian_grass' 'aurorian_grass'

function Plant-Feature($provider) {
    @{type='minecraft:simple_block';config=@{to_place=$provider}}
}
function State($name, $weight, $properties=$null) {
    $data=@{Name="theaurorian2:$name"};if($null-ne$properties){$data.Properties=$properties};@{data=$data;weight=$weight}
}
$grassProvider=@{type='minecraft:weighted_state_provider';entries=@((State 'aurorian_grass' 31),(State 'aurorian_grass_light' 1))}
$tallGrassProvider=@{type='minecraft:simple_state_provider';state=@{Name='theaurorian2:tall_aurorian_grass';Properties=@{half='lower'}}}
$flowerProvider=@{
    type='minecraft:noise_threshold_provider'
    default_state=@{Name='theaurorian2:petunia_plant'}
    high_chance=0.33333334
    high_states=@(
        @{Name='theaurorian2:nebula_blossom_cluster'},
        @{Name='theaurorian2:moon_frost_flower'},
        @{Name='theaurorian2:void_candle_flower'},
        @{Name='theaurorian2:petunia_plant'}
    )
    low_states=@(
        @{Name='theaurorian2:moon_frost_flower'},
        @{Name='theaurorian2:void_candle_flower'},
        @{Name='theaurorian2:nebula_blossom_cluster'},
        @{Name='theaurorian2:petunia_plant'}
    )
    noise=@{firstOctave=0;amplitudes=@(1.0)}
    scale=0.005
    seed=2345
    threshold=-0.8
}
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_aurorian_grass.json') (Plant-Feature $grassProvider)
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_tall_aurorian_grass.json') (Plant-Feature $tallGrassProvider)
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_aurorian_flowers.json') (Plant-Feature $flowerProvider)

function Offset($horizontal,$vertical) {
    @{
        type='minecraft:random_offset'
        xz_spread=@{type='minecraft:trapezoid';min=-$horizontal;max=$horizontal;plateau=0}
        y_spread=@{type='minecraft:trapezoid';min=-$vertical;max=$vertical;plateau=0}
    }
}
$airFilter=@{type='minecraft:block_predicate_filter';predicate=@{type='minecraft:matching_block_tag';tag='minecraft:air'}}
$grassPlacement=@(
    @{type='minecraft:noise_threshold_count';noise_level=-0.8;below_noise=5;above_noise=10},
    @{type='minecraft:in_square'},@{type='minecraft:heightmap';heightmap='WORLD_SURFACE_WG'},@{type='minecraft:biome'},
    @{type='minecraft:count';count=32},(Offset 7 3),$airFilter
)
$tallGrassPlacement=@(
    @{type='minecraft:noise_threshold_count';noise_level=-0.8;below_noise=0;above_noise=7},@{type='minecraft:rarity_filter';chance=32},
    @{type='minecraft:in_square'},@{type='minecraft:heightmap';heightmap='MOTION_BLOCKING'},@{type='minecraft:biome'},
    @{type='minecraft:count';count=96},(Offset 7 3),$airFilter
)
$flowerPlacement=@(
    @{type='minecraft:noise_threshold_count';noise_level=-0.8;below_noise=15;above_noise=4},@{type='minecraft:rarity_filter';chance=32},
    @{type='minecraft:in_square'},@{type='minecraft:heightmap';heightmap='MOTION_BLOCKING'},@{type='minecraft:biome'},
    @{type='minecraft:count';count=64},(Offset 6 2),$airFilter
)
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_aurorian_grass.json') @{feature='theaurorian2:patch_aurorian_grass';placement=$grassPlacement}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_tall_aurorian_grass.json') @{feature='theaurorian2:patch_tall_aurorian_grass';placement=$tallGrassPlacement}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_aurorian_flowers.json') @{feature='theaurorian2:patch_aurorian_flowers';placement=$flowerPlacement}

$dirtTag = Join-Path $root 'src/main/resources/data/minecraft/tags/block/dirt.json'
Write-Json $dirtTag @{replace=$false;values=@('theaurorian2:aurorian_dirt','theaurorian2:aurorian_grass_block')}

$biomePath=Join-Path $data 'worldgen/biome/aurorian_plains.json'
$biome=Get-Content -Raw -LiteralPath $biomePath | ConvertFrom-Json
$features=@($biome.features);while($features.Count-lt 11){$features+=,@()}
$features[9]=@('theaurorian2:patch_tall_aurorian_grass','theaurorian2:patch_aurorian_flowers','theaurorian2:patch_aurorian_grass')
$biome.features=$features
Write-Json $biomePath $biome
