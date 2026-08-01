param(
    [string]$LegacyRoot = 'D:\TheAurorian',
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

. (Join-Path $PSScriptRoot 'json_utils.ps1')
Add-Type -AssemblyName System.Web.Extensions
$jsonSerializer = New-Object System.Web.Script.Serialization.JavaScriptSerializer
$jsonSerializer.MaxJsonLength = [int]::MaxValue
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$generatedAssets = Join-Path $LegacyRoot 'src\generated\resources\assets\theaurorian'
$mainAssets = Join-Path $LegacyRoot 'src\main\resources\assets\theaurorian'
$generatedData = Join-Path $LegacyRoot 'src\generated\resources\data\theaurorian'
$targetAssets = Join-Path $ProjectRoot 'src\main\resources\assets\theaurorian2'
$targetData = Join-Path $ProjectRoot 'src\main\resources\data\theaurorian2'

$blockIds = @(
    'aurorian_stone_bricks',
    'aurorian_stone_stairs', 'aurorian_stone_slab', 'aurorian_stone_wall',
    'aurorian_stone_brick_stairs', 'aurorian_stone_brick_slab', 'aurorian_stone_brick_wall',
    'aurorian_andesite_slab', 'aurorian_andesite_wall',
    'aurorian_diorite_stairs', 'aurorian_diorite_slab', 'aurorian_diorite_wall',
    'aurorian_granite_wall',
    'aurorian_peridotite_stairs', 'aurorian_peridotite_slab', 'aurorian_peridotite_wall',
    'smooth_aurorian_peridotite', 'smooth_aurorian_peridotite_stairs',
    'smooth_aurorian_peridotite_slab', 'smooth_aurorian_peridotite_wall',
    'moon_sand', 'bright_moon_sand', 'bright_moon_sandstone', 'cut_moon_sandstone',
    'aurorian_glass', 'moon_glass', 'moon_glass_pane',
    'rune_stone', 'rune_stone_stairs', 'rune_stone_wall',
    'smooth_rune_stone', 'smooth_rune_stone_stairs', 'smooth_rune_stone_slab', 'smooth_rune_stone_wall',
    'chiseled_rune_stone', 'chiseled_rune_stone_slab', 'chiseled_rune_stone_wall',
    'transparent_rune_stone', 'rune_stone_lamp', 'rune_crystal', 'rune_stone_bars',
    'rune_stone_gate', 'rune_stone_gate_keyhole',
    'moon_temple_pillar', 'moon_temple_bricks', 'moon_temple_brick_stairs',
    'moon_temple_brick_slab', 'moon_temple_brick_wall',
    'smooth_moon_temple_bricks', 'smooth_moon_temple_brick_stairs',
    'smooth_moon_temple_brick_slab', 'smooth_moon_temple_brick_wall',
    'chiseled_moon_temple_bricks', 'moon_temple_lamp', 'moon_temple_bars', 'moon_temple_gate',
    'vertical_aurorian_andesite_slab', 'vertical_aurorian_andesite_stairs',
    'vertical_aurorian_diorite_slab', 'vertical_aurorian_diorite_stairs',
    'vertical_aurorian_stone_slab', 'vertical_aurorian_stone_stairs',
    'vertical_aurorian_stone_brick_slab', 'vertical_aurorian_stone_brick_stairs',
    'vertical_chiseled_moon_temple_brick_slab',
    'vertical_moon_temple_brick_slab', 'vertical_moon_temple_brick_stairs',
    'weeping_willow_log', 'stripped_weeping_willow_log',
    'weeping_willow_wood', 'stripped_weeping_willow_wood', 'weeping_willow_planks',
    'weeping_willow_stairs', 'weeping_willow_slab', 'weeping_willow_fence',
    'weeping_willow_fence_gate', 'weeping_willow_door', 'weeping_willow_trapdoor',
    'weeping_willow_pressure_plate', 'weeping_willow_button', 'weeping_willow_leaves',
    'vertical_weeping_willow_slab', 'vertical_weeping_willow_stairs',
    'vertical_silent_wood_slab', 'vertical_silent_wood_stairs',
    'silent_wood_sign', 'silent_wood_wall_sign',
    'silent_wood_hanging_sign', 'silent_wood_wall_hanging_sign',
    'weeping_willow_wood_sign', 'weeping_willow_wood_wall_sign',
    'weeping_willow_wood_hanging_sign', 'weeping_willow_wood_wall_hanging_sign',
    'silent_campfire', 'aurorian_furnace_chimney',
    'indigo_mushroom_block', 'equinox_flower',
    'potted_aurorian_grass', 'potted_equinox_flower', 'potted_lavender_plant',
    'potted_moon_frost_flower', 'potted_nebula_blossom_cluster', 'potted_petunia_plant',
    'potted_silent_tree_sapling', 'potted_void_candle_flower'
)

$registrySource = [System.IO.File]::ReadAllText((Join-Path $ProjectRoot 'src\main\java\cn\teampancake\theaurorian2\common\registry\ModStructureBlocks.java'))
$legacyCatalogIds = @()
$catalogPattern = 'private static final List<String> LEGACY_[A-Z0-9_]+_IDS\s*=\s*List\.of\((.*?)\);'
foreach ($catalogMatch in [regex]::Matches($registrySource, $catalogPattern, 'Singleline')) {
    foreach ($idMatch in [regex]::Matches($catalogMatch.Groups[1].Value, '"([a-z0-9_]+)"')) {
        $legacyCatalogIds += $idMatch.Groups[1].Value
    }
}
$legacyCatalogIds = @($legacyCatalogIds | Sort-Object -Unique)
if ($legacyCatalogIds.Count -ne 198) {
    throw "Expected 198 deferred legacy block IDs in ModStructureBlocks, found $($legacyCatalogIds.Count)"
}
$blockIds = @($blockIds + $legacyCatalogIds | Sort-Object -Unique)

$hiddenBlockIds = @(
    'silent_wood_wall_sign', 'silent_wood_wall_hanging_sign',
    'weeping_willow_wood_wall_sign', 'weeping_willow_wood_wall_hanging_sign',
    'potted_aurorian_grass', 'potted_equinox_flower', 'potted_lavender_plant',
    'potted_moon_frost_flower', 'potted_nebula_blossom_cluster', 'potted_petunia_plant',
    'potted_silent_tree_sapling', 'potted_void_candle_flower',
    'aurorian_farm_tile', 'aurorian_portal', 'mystical_barrier',
    'lavender_crop', 'silk_berry_crop',
    'moon_wall_torch',
    'molten_cerulean', 'molten_moonsilver', 'molten_moonstone',
    'potted_aurorian_grass_light', 'potted_cursed_frost_tree_sapling',
    'potted_curtain_tree_sapling', 'potted_wick_grass',
    'curtain_wood_wall_sign', 'curtain_wood_wall_hanging_sign',
    'cursed_frost_wood_wall_sign', 'cursed_frost_wood_wall_hanging_sign'
)

$modelCache = @{}
$copiedTextures = @{}

function Write-LegacyJson([string]$path, [string]$jsonText) {
    $value = $jsonSerializer.DeserializeObject($jsonText)
    $compact = $jsonSerializer.Serialize($value)
    $null = $jsonSerializer.DeserializeObject($compact)
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    [System.IO.File]::WriteAllText($path, $compact + [Environment]::NewLine, $utf8NoBom)
}

function Find-LegacyFile([string]$relativePath) {
    foreach ($root in @($generatedAssets, $mainAssets)) {
        $candidate = Join-Path $root $relativePath
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }
    return $null
}

function Copy-Texture([string]$kind, [string]$resourcePath) {
    $key = "$kind/$resourcePath"
    if ($copiedTextures.ContainsKey($key)) {
        return
    }
    $copiedTextures[$key] = $true
    $source = Find-LegacyFile "textures/$kind/$resourcePath.png"
    if (-not $source) {
        return
    }
    $target = Join-Path $targetAssets "textures/$kind/$resourcePath.png"
    if (-not (Test-Path -LiteralPath $target)) {
        New-Item -ItemType Directory -Force -Path (Split-Path $target) | Out-Null
        Copy-Item -LiteralPath $source -Destination $target
    }
    $metadata = "$source.mcmeta"
    $targetMetadata = "$target.mcmeta"
    if ((Test-Path -LiteralPath $metadata) -and -not (Test-Path -LiteralPath $targetMetadata)) {
        Copy-Item -LiteralPath $metadata -Destination $targetMetadata
    }
}

function Import-Model([string]$kind, [string]$resourcePath) {
    $key = "$kind/$resourcePath"
    if ($modelCache.ContainsKey($key)) {
        return
    }
    $modelCache[$key] = $true
    $source = Find-LegacyFile "models/$kind/$resourcePath.json"
    if (-not $source) {
        return
    }

    $raw = [System.IO.File]::ReadAllText($source)
    $matches = [regex]::Matches($raw, 'theaurorian:(block|item)/([a-z0-9_./-]+)')
    foreach ($match in $matches) {
        $referenceKind = $match.Groups[1].Value
        $referencePath = $match.Groups[2].Value
        Import-Model $referenceKind $referencePath
        Copy-Texture $referenceKind $referencePath
    }

    Write-LegacyJson (Join-Path $targetAssets "models/$kind/$resourcePath.json") ($raw.Replace('theaurorian:', 'theaurorian2:'))
}

function Import-Blockstate([string]$id) {
    $source = Find-LegacyFile "blockstates/$id.json"
    if (-not $source) {
        throw "Missing legacy blockstate for '$id'"
    }
    $raw = [System.IO.File]::ReadAllText($source)
    foreach ($match in [regex]::Matches($raw, 'theaurorian:block/([a-z0-9_./-]+)')) {
        Import-Model 'block' $match.Groups[1].Value
    }
    Write-LegacyJson (Join-Path $targetAssets "blockstates/$id.json") ($raw.Replace('theaurorian:', 'theaurorian2:'))
}

function Import-LootTable([string]$id) {
    $source = Join-Path $generatedData "loot_table/blocks/$id.json"
    if (Test-Path -LiteralPath $source) {
        $raw = [System.IO.File]::ReadAllText($source)
        Write-LegacyJson (Join-Path $targetData "loot_table/blocks/$id.json") ($raw.Replace('theaurorian:', 'theaurorian2:'))
        return
    }

    if ($hiddenBlockIds -contains $id) {
        $loot = [ordered]@{ type = 'minecraft:block'; pools = @() }
    } else {
        $loot = [ordered]@{
            type = 'minecraft:block'
            pools = @([ordered]@{
                rolls = 1
                bonus_rolls = 0
                entries = @([ordered]@{ type = 'minecraft:item'; name = "theaurorian2:$id" })
                conditions = @([ordered]@{ condition = 'minecraft:survives_explosion' })
            })
            random_sequence = "theaurorian2:blocks/$id"
        }
    }
    Write-Json (Join-Path $targetData "loot_table/blocks/$id.json") $loot
}

function Add-TagValues([string]$scope, [string]$tagPath, [string[]]$values) {
    $path = Join-Path $ProjectRoot "src/main/resources/data/minecraft/tags/$scope/$tagPath.json"
    $existing = @()
    $replace = $false
    if (Test-Path -LiteralPath $path) {
        $json = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json
        $existing = @($json.values)
        $replace = [bool]$json.replace
    }
    $merged = @($existing + $values | Where-Object { $_ } | Sort-Object -Unique)
    Write-Json $path ([ordered]@{ replace = $replace; values = $merged })
}

foreach ($id in $blockIds) {
    Import-Blockstate $id
    Import-LootTable $id
    if ($hiddenBlockIds -notcontains $id) {
        $itemModel = Find-LegacyFile "models/item/$id.json"
        if ($itemModel) {
            Import-Model 'item' $id
            $model = "theaurorian2:item/$id"
        } else {
            $model = "theaurorian2:block/$id"
        }
        Write-Json (Join-Path $targetAssets "items/$id.json") ([ordered]@{
            model = [ordered]@{ type = 'minecraft:model'; model = $model }
        })
    }
}

# The legacy laser crystal shipped with a blockstate only. Reuse the rune crystal model
# until the dedicated mechanic and artwork are ported.
$laserCrystalModel = Join-Path $targetAssets 'models/block/laser_crystal.json'
if (-not (Test-Path -LiteralPath $laserCrystalModel)) {
    Write-Json $laserCrystalModel ([ordered]@{ parent = 'theaurorian2:block/rune_crystal' })
}

# The legacy data generator omitted this table because the willow had no sapling item.
# Keep vanilla leaf harvesting and stick drops without inventing a replacement sapling.
$weepingWillowLeavesLoot = @'
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "theaurorian2:weeping_willow_leaves",
          "conditions": [
            {
              "condition": "minecraft:any_of",
              "terms": [
                {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "predicates": {
                      "minecraft:enchantments": [
                        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}
                      ]
                    }
                  }
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "theaurorian2:silent_wood_stick",
          "conditions": [
            {
              "condition": "minecraft:table_bonus",
              "enchantment": "minecraft:fortune",
              "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]
            }
          ],
          "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}, "add": false},
            {"function": "minecraft:explosion_decay"}
          ]
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:inverted",
          "term": {
            "condition": "minecraft:any_of",
            "terms": [
              {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "predicates": {
                    "minecraft:enchantments": [
                      {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}
                    ]
                  }
                }
              }
            ]
          }
        }
      ]
    }
  ],
  "random_sequence": "theaurorian2:blocks/weeping_willow_leaves"
}
'@
Write-LegacyJson (Join-Path $targetData 'loot_table/blocks/weeping_willow_leaves.json') $weepingWillowLeavesLoot

foreach ($texture in @(
    'entity/signs/silent', 'entity/signs/weeping_willow',
    'entity/signs/curtain', 'entity/signs/cursed_frost',
    'entity/signs/hanging/silent', 'entity/signs/hanging/weeping_willow',
    'entity/signs/hanging/curtain', 'entity/signs/hanging/cursed_frost'
)) {
    $source = Find-LegacyFile "textures/$texture.png"
    if ($source) {
        $target = Join-Path $targetAssets "textures/$texture.png"
        New-Item -ItemType Directory -Force -Path (Split-Path $target) | Out-Null
        if (-not (Test-Path -LiteralPath $target)) {
            Copy-Item -LiteralPath $source -Destination $target
        }
    }
}

foreach ($locale in @('en_us', 'zh_cn')) {
    $legacyLanguage = $jsonSerializer.DeserializeObject(
        [System.IO.File]::ReadAllText((Join-Path $generatedAssets "lang/$locale.json"), [System.Text.Encoding]::UTF8))
    $targetLanguagePath = Join-Path $targetAssets "lang/$locale.json"
    $targetLanguage = $jsonSerializer.DeserializeObject(
        [System.IO.File]::ReadAllText($targetLanguagePath, [System.Text.Encoding]::UTF8))
    $language = [ordered]@{}
    foreach ($entry in $targetLanguage.GetEnumerator()) {
        $language[$entry.Key] = $entry.Value
    }
    foreach ($id in $blockIds) {
        foreach ($prefix in @('block', 'item')) {
            $oldKey = "$prefix.theaurorian.$id"
            if ($legacyLanguage.ContainsKey($oldKey)) {
                $language["$prefix.theaurorian2.$id"] = $legacyLanguage[$oldKey]
            }
        }
    }
    if ($locale -eq 'zh_cn') {
        $language['block.theaurorian2.laser_crystal'] = [Text.Encoding]::UTF8.GetString(
            [Convert]::FromBase64String('5r+A5YWJ5rC05pm2'))
    } else {
        $language['block.theaurorian2.laser_crystal'] = 'Laser Crystal'
    }
    Write-Json $targetLanguagePath $language
}

$stoneIds = @($blockIds | Where-Object {
    $_ -match 'stone|andesite|diorite|granite|peridotite|temple|rune|glass|chimney'
}) | ForEach-Object { "theaurorian2:$_" }
$stoneIds += @(
    'theaurorian2:aurorian_barrier_stone', 'theaurorian2:aurorian_coal_block',
    'theaurorian2:cerulean_block', 'theaurorian2:moonsilver_block', 'theaurorian2:moonstone_block',
    'theaurorian2:moon_gem', 'theaurorian2:moonlight_forge', 'theaurorian2:scrapper',
    'theaurorian2:crystalline_sword_pedestal', 'theaurorian2:laser_crystal', 'theaurorian2:urn',
    'theaurorian2:filthy_ice', 'theaurorian2:large_filthy_ice_spike',
    'theaurorian2:medium_filthy_ice_spike', 'theaurorian2:small_filthy_ice_spike',
    'theaurorian2:indigo_mushroom_crystal'
)
Add-TagValues 'block' 'mineable/pickaxe' $stoneIds
Add-TagValues 'block' 'mineable/shovel' @(
    'theaurorian2:aurorian_farm_tile', 'theaurorian2:red_aurorian_grass_block'
)

$woodIds = @(
    'weeping_willow_log', 'stripped_weeping_willow_log', 'weeping_willow_wood',
    'stripped_weeping_willow_wood', 'weeping_willow_planks', 'weeping_willow_stairs',
    'weeping_willow_slab', 'weeping_willow_fence', 'weeping_willow_fence_gate',
    'weeping_willow_door', 'weeping_willow_trapdoor', 'weeping_willow_pressure_plate',
    'weeping_willow_button', 'vertical_weeping_willow_slab', 'vertical_weeping_willow_stairs',
    'vertical_silent_wood_slab', 'vertical_silent_wood_stairs',
    'silent_wood_sign', 'silent_wood_wall_sign', 'silent_wood_hanging_sign', 'silent_wood_wall_hanging_sign',
    'weeping_willow_wood_sign', 'weeping_willow_wood_wall_sign',
    'weeping_willow_wood_hanging_sign', 'weeping_willow_wood_wall_hanging_sign',
    'curtain_wood_sign', 'curtain_wood_wall_sign',
    'curtain_wood_hanging_sign', 'curtain_wood_wall_hanging_sign',
    'cursed_frost_wood_sign', 'cursed_frost_wood_wall_sign',
    'cursed_frost_wood_hanging_sign', 'cursed_frost_wood_wall_hanging_sign',
    'sacrifice_table', 'silent_campfire'
) | ForEach-Object { "theaurorian2:$_" }
Add-TagValues 'block' 'mineable/axe' $woodIds
Add-TagValues 'block' 'leaves' @('theaurorian2:weeping_willow_leaves')
Add-TagValues 'block' 'mineable/hoe' @('theaurorian2:weeping_willow_leaves')
Add-TagValues 'block' 'walls' @($blockIds | Where-Object { $_ -like '*_wall' } | ForEach-Object { "theaurorian2:$_" })
Add-TagValues 'block' 'wool' @('theaurorian2:mysterium_wool')
Add-TagValues 'block' 'crops' @('theaurorian2:lavender_crop', 'theaurorian2:silk_berry_crop')
Add-TagValues 'block' 'planks' @('theaurorian2:weeping_willow_planks')
Add-TagValues 'block' 'logs' @(
    'theaurorian2:weeping_willow_log', 'theaurorian2:stripped_weeping_willow_log',
    'theaurorian2:weeping_willow_wood', 'theaurorian2:stripped_weeping_willow_wood'
)
Add-TagValues 'block' 'logs_that_burn' @(
    'theaurorian2:weeping_willow_log', 'theaurorian2:stripped_weeping_willow_log',
    'theaurorian2:weeping_willow_wood', 'theaurorian2:stripped_weeping_willow_wood'
)
Add-TagValues 'block' 'wooden_fences' @('theaurorian2:weeping_willow_fence')
Add-TagValues 'block' 'fence_gates' @('theaurorian2:weeping_willow_fence_gate')
Add-TagValues 'block' 'wooden_doors' @('theaurorian2:weeping_willow_door')
Add-TagValues 'block' 'wooden_trapdoors' @('theaurorian2:weeping_willow_trapdoor')
Add-TagValues 'block' 'wooden_pressure_plates' @('theaurorian2:weeping_willow_pressure_plate')
Add-TagValues 'block' 'wooden_buttons' @('theaurorian2:weeping_willow_button')
Add-TagValues 'block' 'standing_signs' @(
    'theaurorian2:silent_wood_sign', 'theaurorian2:weeping_willow_wood_sign',
    'theaurorian2:curtain_wood_sign', 'theaurorian2:cursed_frost_wood_sign'
)
Add-TagValues 'block' 'wall_signs' @(
    'theaurorian2:silent_wood_wall_sign', 'theaurorian2:weeping_willow_wood_wall_sign',
    'theaurorian2:curtain_wood_wall_sign', 'theaurorian2:cursed_frost_wood_wall_sign'
)
Add-TagValues 'block' 'ceiling_hanging_signs' @(
    'theaurorian2:silent_wood_hanging_sign', 'theaurorian2:weeping_willow_wood_hanging_sign',
    'theaurorian2:curtain_wood_hanging_sign', 'theaurorian2:cursed_frost_wood_hanging_sign'
)
Add-TagValues 'block' 'wall_hanging_signs' @(
    'theaurorian2:silent_wood_wall_hanging_sign', 'theaurorian2:weeping_willow_wood_wall_hanging_sign',
    'theaurorian2:curtain_wood_wall_hanging_sign', 'theaurorian2:cursed_frost_wood_wall_hanging_sign'
)
Add-TagValues 'block' 'all_signs' @(
    'theaurorian2:silent_wood_sign', 'theaurorian2:silent_wood_wall_sign',
    'theaurorian2:weeping_willow_wood_sign', 'theaurorian2:weeping_willow_wood_wall_sign',
    'theaurorian2:curtain_wood_sign', 'theaurorian2:curtain_wood_wall_sign',
    'theaurorian2:cursed_frost_wood_sign', 'theaurorian2:cursed_frost_wood_wall_sign'
)
Add-TagValues 'block' 'all_hanging_signs' @(
    'theaurorian2:silent_wood_hanging_sign', 'theaurorian2:silent_wood_wall_hanging_sign',
    'theaurorian2:weeping_willow_wood_hanging_sign', 'theaurorian2:weeping_willow_wood_wall_hanging_sign',
    'theaurorian2:curtain_wood_hanging_sign', 'theaurorian2:curtain_wood_wall_hanging_sign',
    'theaurorian2:cursed_frost_wood_hanging_sign', 'theaurorian2:cursed_frost_wood_wall_hanging_sign'
)
Add-TagValues 'block' 'campfires' @('theaurorian2:silent_campfire')

Add-TagValues 'item' 'planks' @('theaurorian2:weeping_willow_planks')
Add-TagValues 'item' 'logs' @(
    'theaurorian2:weeping_willow_log', 'theaurorian2:stripped_weeping_willow_log',
    'theaurorian2:weeping_willow_wood', 'theaurorian2:stripped_weeping_willow_wood'
)
Add-TagValues 'item' 'logs_that_burn' @(
    'theaurorian2:weeping_willow_log', 'theaurorian2:stripped_weeping_willow_log',
    'theaurorian2:weeping_willow_wood', 'theaurorian2:stripped_weeping_willow_wood'
)
Add-TagValues 'item' 'wooden_doors' @('theaurorian2:weeping_willow_door')
Add-TagValues 'item' 'wooden_trapdoors' @('theaurorian2:weeping_willow_trapdoor')
Add-TagValues 'item' 'signs' @(
    'theaurorian2:silent_wood_sign', 'theaurorian2:weeping_willow_wood_sign',
    'theaurorian2:curtain_wood_sign', 'theaurorian2:cursed_frost_wood_sign'
)
Add-TagValues 'item' 'hanging_signs' @(
    'theaurorian2:silent_wood_hanging_sign', 'theaurorian2:weeping_willow_wood_hanging_sign',
    'theaurorian2:curtain_wood_hanging_sign', 'theaurorian2:cursed_frost_wood_hanging_sign'
)
Add-TagValues 'item' 'furnace_fuels' @(
    'theaurorian2:weeping_willow_log', 'theaurorian2:stripped_weeping_willow_log',
    'theaurorian2:weeping_willow_wood', 'theaurorian2:stripped_weeping_willow_wood',
    'theaurorian2:weeping_willow_planks', 'theaurorian2:weeping_willow_stairs',
    'theaurorian2:weeping_willow_slab', 'theaurorian2:weeping_willow_fence',
    'theaurorian2:weeping_willow_fence_gate', 'theaurorian2:weeping_willow_door',
    'theaurorian2:weeping_willow_trapdoor', 'theaurorian2:weeping_willow_pressure_plate',
    'theaurorian2:weeping_willow_button', 'theaurorian2:vertical_weeping_willow_slab',
    'theaurorian2:vertical_weeping_willow_stairs', 'theaurorian2:silent_wood_sign',
    'theaurorian2:silent_wood_hanging_sign', 'theaurorian2:weeping_willow_wood_sign',
    'theaurorian2:weeping_willow_wood_hanging_sign',
    'theaurorian2:curtain_wood_sign', 'theaurorian2:curtain_wood_hanging_sign',
    'theaurorian2:cursed_frost_wood_sign', 'theaurorian2:cursed_frost_wood_hanging_sign',
    'theaurorian2:vertical_curtain_wood_slab', 'theaurorian2:vertical_curtain_wood_stairs',
    'theaurorian2:vertical_cursed_frost_wood_slab', 'theaurorian2:vertical_cursed_frost_wood_stairs'
)

Write-Output "Generated resources for $($blockIds.Count) legacy structure blocks."
