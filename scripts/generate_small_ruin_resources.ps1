. "$PSScriptRoot/json_utils.ps1"

$root = Split-Path $PSScriptRoot -Parent
$assets = Join-Path $root 'src/main/resources/assets/theaurorian2'
$data = Join-Path $root 'src/main/resources/data'

function Write-BlockItem($name, $model = $name) {
    Write-Json (Join-Path $assets "items/$name.json") @{
        model = @{type = 'minecraft:model'; model = "theaurorian2:block/$model"}
    }
}

function Write-CubeBlock($name) {
    Write-Json (Join-Path $assets "blockstates/$name.json") @{
        multipart = @(@{apply = @{model = "theaurorian2:block/$name"}})
    }
    Write-Json (Join-Path $assets "models/block/$name.json") @{
        parent = 'minecraft:block/cube_all'
        textures = @{all = "theaurorian2:block/$name"}
    }
    Write-BlockItem $name
}

function Write-DropSelf($name) {
    Write-Json (Join-Path $data "theaurorian2/loot_table/blocks/$name.json") @{
        type = 'minecraft:block'
        pools = @(@{
            bonus_rolls = 0.0
            entries = @(@{type = 'minecraft:item'; name = "theaurorian2:$name"})
            rolls = 1.0
        })
    }
}

function Merge-Tag($path, $values) {
    if (Test-Path -LiteralPath $path) {
        $tag = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json
        $merged = @($tag.values) + @($values)
    } else {
        $merged = @($values)
    }
    Write-Json $path @{
        replace = $false
        values = @($merged | Select-Object -Unique)
    }
}

Write-CubeBlock 'aurorian_peridotite'
Write-CubeBlock 'aurorian_portal_frame_bricks'
Write-DropSelf 'aurorian_peridotite'
Write-DropSelf 'aurorian_portal_frame_bricks'

$cobblestoneTexture = 'theaurorian2:block/aurorian_cobblestone'
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_stairs.json') @{
    parent = 'minecraft:block/stairs'
    textures = @{bottom = $cobblestoneTexture; side = $cobblestoneTexture; top = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_stairs_inner.json') @{
    parent = 'minecraft:block/inner_stairs'
    textures = @{bottom = $cobblestoneTexture; side = $cobblestoneTexture; top = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_stairs_outer.json') @{
    parent = 'minecraft:block/outer_stairs'
    textures = @{bottom = $cobblestoneTexture; side = $cobblestoneTexture; top = $cobblestoneTexture}
}
$stairsState = Get-Content -Raw -LiteralPath (Join-Path $assets 'blockstates/aurorian_brick_stairs.json') | ConvertFrom-Json
foreach ($variant in $stairsState.variants.PSObject.Properties) {
    $variant.Value.model = ([string]$variant.Value.model).Replace(
        'minecraft:block/brick_stairs',
        'theaurorian2:block/aurorian_cobblestone_stairs')
}
Write-Json (Join-Path $assets 'blockstates/aurorian_cobblestone_stairs.json') $stairsState
Write-BlockItem 'aurorian_cobblestone_stairs'
Write-DropSelf 'aurorian_cobblestone_stairs'

Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_slab.json') @{
    parent = 'minecraft:block/slab'
    textures = @{bottom = $cobblestoneTexture; side = $cobblestoneTexture; top = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_slab_top.json') @{
    parent = 'minecraft:block/slab_top'
    textures = @{bottom = $cobblestoneTexture; side = $cobblestoneTexture; top = $cobblestoneTexture}
}
$slabState = Get-Content -Raw -LiteralPath (Join-Path $assets 'blockstates/aurorian_brick_slab.json') | ConvertFrom-Json
foreach ($variant in $slabState.variants.PSObject.Properties) {
    $variant.Value.model = ([string]$variant.Value.model).Replace(
        'minecraft:block/brick_slab_top',
        'theaurorian2:block/aurorian_cobblestone_slab_top').Replace(
        'minecraft:block/brick_slab',
        'theaurorian2:block/aurorian_cobblestone_slab').Replace(
        'minecraft:block/bricks',
        'theaurorian2:block/aurorian_cobblestone')
}
Write-Json (Join-Path $assets 'blockstates/aurorian_cobblestone_slab.json') $slabState
Write-BlockItem 'aurorian_cobblestone_slab'
Write-Json (Join-Path $data 'theaurorian2/loot_table/blocks/aurorian_cobblestone_slab.json') @{
    type = 'minecraft:block'
    pools = @(@{
        bonus_rolls = 0.0
        entries = @(@{
            type = 'minecraft:item'
            functions = @(@{
                add = $false
                conditions = @(@{
                    block = 'theaurorian2:aurorian_cobblestone_slab'
                    condition = 'minecraft:block_state_property'
                    properties = @{type = 'double'}
                })
                count = 2.0
                function = 'minecraft:set_count'
            })
            name = 'theaurorian2:aurorian_cobblestone_slab'
        })
        rolls = 1.0
    })
}

Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_wall_post.json') @{
    parent = 'minecraft:block/template_wall_post'
    textures = @{wall = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_wall_side.json') @{
    parent = 'minecraft:block/template_wall_side'
    textures = @{wall = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_wall_side_tall.json') @{
    parent = 'minecraft:block/template_wall_side_tall'
    textures = @{wall = $cobblestoneTexture}
}
Write-Json (Join-Path $assets 'models/block/aurorian_cobblestone_wall_inventory.json') @{
    parent = 'minecraft:block/wall_inventory'
    textures = @{wall = $cobblestoneTexture}
}
$wallState = Get-Content -Raw -LiteralPath (Join-Path $assets 'blockstates/aurorian_brick_wall.json') | ConvertFrom-Json
foreach ($part in $wallState.multipart) {
    $part.apply.model = ([string]$part.apply.model).Replace(
        'minecraft:block/brick_wall',
        'theaurorian2:block/aurorian_cobblestone_wall')
}
Write-Json (Join-Path $assets 'blockstates/aurorian_cobblestone_wall.json') $wallState
Write-BlockItem 'aurorian_cobblestone_wall' 'aurorian_cobblestone_wall_inventory'
Write-DropSelf 'aurorian_cobblestone_wall'

$crystals = @(
    'cerulean_cluster',
    'large_cerulean_bud',
    'medium_cerulean_bud',
    'small_cerulean_bud',
    'moonstone_cluster',
    'large_moonstone_bud',
    'medium_moonstone_bud',
    'small_moonstone_bud'
)
foreach ($name in $crystals) {
    $variants = [ordered]@{}
    $variants['facing=down'] = @{model = "theaurorian2:block/$name"; x = 180}
    $variants['facing=east'] = @{model = "theaurorian2:block/$name"; x = 90; y = 90}
    $variants['facing=north'] = @{model = "theaurorian2:block/$name"; x = 90}
    $variants['facing=south'] = @{model = "theaurorian2:block/$name"; x = 90; y = 180}
    $variants['facing=up'] = @{model = "theaurorian2:block/$name"}
    $variants['facing=west'] = @{model = "theaurorian2:block/$name"; x = 90; y = 270}
    Write-Json (Join-Path $assets "blockstates/$name.json") @{variants = $variants}
    Write-Json (Join-Path $assets "models/block/$name.json") @{
        parent = 'minecraft:block/cross'
        render_type = 'minecraft:cutout'
        textures = @{cross = "theaurorian2:block/$name"}
    }
    Write-BlockItem $name
    Write-DropSelf $name
}

$smallRuinFeatures = @()
foreach ($number in 1..22) {
    $name = 'small_ruin_{0:D2}' -f $number
    Write-Json (Join-Path $data "theaurorian2/worldgen/configured_feature/$name.json") @{
        type = 'theaurorian2:small_ruin'
        config = @{template = "theaurorian2:ruins/small_ruins/$name"}
    }
    $smallRuinFeatures += @{feature = "theaurorian2:$name"; placement = @()}
}
Write-Json (Join-Path $data 'theaurorian2/worldgen/configured_feature/small_ruins.json') @{
    type = 'minecraft:simple_random_selector'
    config = @{features = $smallRuinFeatures}
}
Write-Json (Join-Path $data 'theaurorian2/worldgen/placed_feature/small_ruins.json') @{
    feature = 'theaurorian2:small_ruins'
    placement = @(
        @{type = 'minecraft:rarity_filter'; chance = 150},
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:heightmap'; heightmap = 'WORLD_SURFACE_WG'},
        @{type = 'minecraft:biome'}
    )
}
$mediumRuinNames = @(
    'aurorian_forest_memory_loop',
    'aurorian_forest_remains',
    'aurorian_forest_ruined_portal',
    'aurorian_forest_shattered_forest_pillar',
    'aurorian_forest_shattered_pillar',
    'aurorian_forest_shattered_wreath',
    'aurorian_forest_spring'
)
$mediumRuinFeatures = @()
foreach ($name in $mediumRuinNames) {
    Write-Json (Join-Path $data "theaurorian2/worldgen/configured_feature/$name.json") @{
        type = 'theaurorian2:small_ruin'
        config = @{template = "theaurorian2:ruins/medium_ruins/$name"}
    }
    $mediumRuinFeatures += @{feature = "theaurorian2:$name"; placement = @()}
}
Write-Json (Join-Path $data 'theaurorian2/worldgen/configured_feature/medium_ruins.json') @{
    type = 'minecraft:simple_random_selector'
    config = @{features = $mediumRuinFeatures}
}
Write-Json (Join-Path $data 'theaurorian2/worldgen/placed_feature/medium_ruins.json') @{
    feature = 'theaurorian2:medium_ruins'
    placement = @(
        @{type = 'minecraft:rarity_filter'; chance = 150},
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:heightmap'; heightmap = 'WORLD_SURFACE_WG'},
        @{type = 'minecraft:biome'}
    )
}
Write-Json (Join-Path $data 'theaurorian2/tags/worldgen/biome/has_forest_ruins.json') @{
    replace = $false
    values = @(
        'theaurorian2:silent_wood_forest',
        'theaurorian2:curtain_tree_forest'
    )
}
Write-Json (Join-Path $data 'theaurorian2/neoforge/biome_modifier/add_small_ruins.json') @{
    type = 'neoforge:add_features'
    biomes = '#theaurorian2:has_forest_ruins'
    features = 'theaurorian2:small_ruins'
    step = 'surface_structures'
}
Write-Json (Join-Path $data 'theaurorian2/neoforge/biome_modifier/add_medium_ruins.json') @{
    type = 'neoforge:add_features'
    biomes = '#theaurorian2:has_forest_ruins'
    features = 'theaurorian2:medium_ruins'
    step = 'surface_structures'
}

$pickaxeBlocks = @(
    'theaurorian2:aurorian_cobblestone_stairs',
    'theaurorian2:aurorian_cobblestone_slab',
    'theaurorian2:aurorian_cobblestone_wall',
    'theaurorian2:aurorian_peridotite',
    'theaurorian2:aurorian_portal_frame_bricks'
) + ($crystals | ForEach-Object {"theaurorian2:$_"})
Merge-Tag (Join-Path $data 'minecraft/tags/block/mineable/pickaxe.json') $pickaxeBlocks
Merge-Tag (Join-Path $data 'minecraft/tags/block/walls.json') @('theaurorian2:aurorian_cobblestone_wall')
