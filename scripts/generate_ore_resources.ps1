$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$assets = Join-Path $root 'src/main/resources/assets/theaurorian2'
$data = Join-Path $root 'src/main/resources/data/theaurorian2'

function Write-Json($path, $value) {
    $directory = Split-Path $path
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $value | ConvertTo-Json -Depth 40 | Set-Content -LiteralPath $path -Encoding utf8
}

$shallow = @('aurorian_coal_ore','aurorian_iron_ore','aurorian_copper_ore','aurorian_gold_ore','aurorian_lapis_ore','aurorian_redstone_ore','aurorian_diamond_ore','aurorian_emerald_ore','moonstone_ore','cerulean_ore','geode_ore')
$deep = @('erosive_aurorian_iron_ore','erosive_aurorian_copper_ore','erosive_aurorian_gold_ore','erosive_aurorian_lapis_ore','erosive_aurorian_redstone_ore','erosive_aurorian_diamond_ore','erosive_aurorian_emerald_ore','erosive_moonstone_ore','erosive_cerulean_ore','erosive_geode_ore')
$redstone = @('aurorian_redstone_ore','erosive_aurorian_redstone_ore')

foreach ($name in $shallow + $deep) {
    $variants = if ($redstone -contains $name) {
        @{ variants = @{ 'lit=false' = @{ model = "theaurorian2:block/$name" }; 'lit=true' = @{ model = "theaurorian2:block/$name" } } }
    } else { @{ variants = @{ '' = @{ model = "theaurorian2:block/$name" } } } }
    Write-Json (Join-Path $assets "blockstates/$name.json") $variants
    Write-Json (Join-Path $assets "models/block/$name.json") @{ parent='minecraft:block/cube_all'; textures=@{ all="theaurorian2:block/$name" } }
    Write-Json (Join-Path $assets "items/$name.json") @{ model=@{ type='minecraft:model'; model="theaurorian2:block/$name" } }
}

foreach ($name in @('raw_moonstone','raw_cerulean','crystal')) {
    Write-Json (Join-Path $assets "models/item/$name.json") @{ parent='minecraft:item/generated'; textures=@{ layer0="theaurorian2:item/$name" } }
    Write-Json (Join-Path $assets "items/$name.json") @{ model=@{ type='minecraft:model'; model="theaurorian2:item/$name" } }
}

$orePairs = @{
    iron=@('aurorian_iron_ore','erosive_aurorian_iron_ore'); copper=@('aurorian_copper_ore','erosive_aurorian_copper_ore')
    gold=@('aurorian_gold_ore','erosive_aurorian_gold_ore'); lapis=@('aurorian_lapis_ore','erosive_aurorian_lapis_ore')
    redstone=@('aurorian_redstone_ore','erosive_aurorian_redstone_ore'); diamond=@('aurorian_diamond_ore','erosive_aurorian_diamond_ore')
    emerald=@('aurorian_emerald_ore','erosive_aurorian_emerald_ore'); moonstone=@('moonstone_ore','erosive_moonstone_ore')
    cerulean=@('cerulean_ore','erosive_cerulean_ore'); geode=@('geode_ore','erosive_geode_ore')
}

function Target($block, $ore) {
    @{ target=@{ predicate_type='minecraft:block_match'; block="theaurorian2:$block" }; state=@{ Name="theaurorian2:$ore" } }
}
function Configured-Ore($name, $size, $discard, $pair) {
    $targets = @()
    if ($name -eq 'coal') { $targets += Target 'aurorian_stone' 'aurorian_coal_ore' }
    else { $targets += Target 'aurorian_stone' $pair[0]; $targets += Target 'aurorian_erosive' $pair[1] }
    Write-Json (Join-Path $data "worldgen/configured_feature/ore_$name.json") @{ type='minecraft:ore'; config=@{ discard_chance_on_air_exposure=$discard; size=$size; targets=$targets } }
}

Configured-Ore 'coal' 17 0 @()
foreach ($name in @('iron','gold','moonstone','cerulean')) { Configured-Ore $name 9 0 $orePairs[$name] }
Configured-Ore 'copper' 10 0 $orePairs.copper
Configured-Ore 'redstone' 8 0 $orePairs.redstone
Configured-Ore 'emerald' 3 0 $orePairs.emerald
Configured-Ore 'lapis' 7 0 $orePairs.lapis
Configured-Ore 'lapis_buried' 7 1 $orePairs.lapis
foreach ($name in @('diamond','geode')) {
    Configured-Ore "${name}_small" 4 0.5 $orePairs[$name]
    Configured-Ore "${name}_medium" 8 0.5 $orePairs[$name]
    Configured-Ore "${name}_large" 12 0.7 $orePairs[$name]
    Configured-Ore "${name}_buried" 8 1 $orePairs[$name]
}

function Height($type, $min, $max) { @{ type=$type; min_inclusive=$min; max_inclusive=$max } }
function Placed($name, $feature, $frequency, $height) {
    $first = if ($frequency -is [hashtable]) { $frequency } else { @{ type='minecraft:count'; count=$frequency } }
    Write-Json (Join-Path $data "worldgen/placed_feature/$name.json") @{ feature="theaurorian2:$feature"; placement=@($first,@{type='minecraft:in_square'},@{type='minecraft:height_range';height=$height},@{type='minecraft:biome'}) }
}
$abs = { param($y) @{absolute=[int]$y} }; $bottom = { param($y) @{above_bottom=[int]$y} }; $top = { param($y) @{below_top=[int]$y} }
Placed 'ore_coal_lower' 'ore_coal' 20 (Height 'minecraft:trapezoid' (&$abs 1) (&$abs 192))
Placed 'ore_coal_upper' 'ore_coal' 30 (Height 'minecraft:uniform' (&$abs 136) (&$top 0))
Placed 'ore_iron_upper' 'ore_iron' 90 (Height 'minecraft:trapezoid' (&$abs 80) (&$abs 384))
Placed 'ore_iron_middle' 'ore_iron' 10 (Height 'minecraft:trapezoid' (&$abs -24) (&$abs 56))
Placed 'ore_iron_small' 'ore_iron' 10 (Height 'minecraft:uniform' (&$bottom 0) (&$abs 72))
Placed 'ore_copper' 'ore_copper' 16 (Height 'minecraft:trapezoid' (&$abs -16) (&$abs 112))
Placed 'ore_gold' 'ore_gold' 4 (Height 'minecraft:trapezoid' (&$abs -64) (&$abs 32))
Placed 'ore_gold_lower' 'ore_gold' @{type='minecraft:count';count=@{type='minecraft:uniform';min_inclusive=0;max_inclusive=1}} (Height 'minecraft:uniform' (&$abs -64) (&$abs -48))
Placed 'ore_redstone' 'ore_redstone' 4 (Height 'minecraft:uniform' (&$bottom 0) (&$abs 15))
Placed 'ore_redstone_lower' 'ore_redstone' 8 (Height 'minecraft:trapezoid' (&$bottom -32) (&$bottom 32))
Placed 'ore_lapis' 'ore_lapis' 2 (Height 'minecraft:trapezoid' (&$abs -32) (&$abs 32))
Placed 'ore_lapis_buried' 'ore_lapis_buried' 4 (Height 'minecraft:uniform' (&$bottom 0) (&$abs 64))
Placed 'ore_emerald' 'ore_emerald' 100 (Height 'minecraft:trapezoid' (&$abs -16) (&$abs 480))

$diamondHeight = Height 'minecraft:trapezoid' (&$bottom -80) (&$bottom 80)
Placed 'ore_diamond' 'ore_diamond_small' 7 $diamondHeight
Placed 'ore_diamond_buried' 'ore_diamond_buried' 4 $diamondHeight
Placed 'ore_diamond_large' 'ore_diamond_large' @{type='minecraft:rarity_filter';chance=9} $diamondHeight
Placed 'ore_diamond_medium' 'ore_diamond_medium' 2 (Height 'minecraft:uniform' (&$abs -64) (&$abs -4))

foreach ($spec in @(@('moonstone',36,4,4),@('cerulean',27,3,3))) {
    $name=$spec[0]
    Placed "ore_${name}_upper" "ore_$name" $spec[1] (Height 'minecraft:trapezoid' (&$abs 80) (&$abs 384))
    Placed "ore_${name}_middle" "ore_$name" $spec[2] (Height 'minecraft:trapezoid' (&$abs -24) (&$abs 56))
    Placed "ore_${name}_small" "ore_$name" $spec[3] (Height 'minecraft:uniform' (&$bottom 0) (&$abs 72))
}
Placed 'ore_geode' 'ore_geode_small' 5 $diamondHeight
Placed 'ore_geode_buried' 'ore_geode_buried' 3 $diamondHeight
Placed 'ore_geode_large' 'ore_geode_large' @{type='minecraft:rarity_filter';chance=12} $diamondHeight
Placed 'ore_geode_medium' 'ore_geode_medium' 1 (Height 'minecraft:uniform' (&$abs -64) (&$abs -4))

$vanillaLoot = Join-Path $root 'work/data/minecraft/loot_table/blocks'
$lootMap = @{
    aurorian_coal_ore=@('coal_ore','minecraft:coal'); aurorian_iron_ore=@('iron_ore','minecraft:raw_iron'); erosive_aurorian_iron_ore=@('iron_ore','minecraft:raw_iron')
    aurorian_copper_ore=@('copper_ore','minecraft:raw_copper'); erosive_aurorian_copper_ore=@('copper_ore','minecraft:raw_copper')
    aurorian_gold_ore=@('gold_ore','minecraft:raw_gold'); erosive_aurorian_gold_ore=@('gold_ore','minecraft:raw_gold')
    aurorian_lapis_ore=@('lapis_ore','minecraft:lapis_lazuli'); erosive_aurorian_lapis_ore=@('lapis_ore','minecraft:lapis_lazuli')
    aurorian_redstone_ore=@('redstone_ore','minecraft:redstone'); erosive_aurorian_redstone_ore=@('redstone_ore','minecraft:redstone')
    aurorian_diamond_ore=@('diamond_ore','minecraft:diamond'); erosive_aurorian_diamond_ore=@('diamond_ore','minecraft:diamond')
    aurorian_emerald_ore=@('emerald_ore','minecraft:emerald'); erosive_aurorian_emerald_ore=@('emerald_ore','minecraft:emerald')
    moonstone_ore=@('iron_ore','theaurorian2:raw_moonstone'); erosive_moonstone_ore=@('iron_ore','theaurorian2:raw_moonstone')
    cerulean_ore=@('iron_ore','theaurorian2:raw_cerulean'); erosive_cerulean_ore=@('iron_ore','theaurorian2:raw_cerulean')
    geode_ore=@('diamond_ore','theaurorian2:crystal'); erosive_geode_ore=@('diamond_ore','theaurorian2:crystal')
}
foreach ($entry in $lootMap.GetEnumerator()) {
    $name=$entry.Key; $template=$entry.Value[0]; $drop=$entry.Value[1]
    $loot = Get-Content -Raw -LiteralPath (Join-Path $vanillaLoot "$template.json")
    $loot = $loot.Replace("minecraft:$template", "theaurorian2:$name")
    $originalDrop = switch ($template) { 'coal_ore' {'minecraft:coal'} 'iron_ore' {'minecraft:raw_iron'} 'copper_ore' {'minecraft:raw_copper'} 'gold_ore' {'minecraft:raw_gold'} 'lapis_ore' {'minecraft:lapis_lazuli'} 'redstone_ore' {'minecraft:redstone'} 'diamond_ore' {'minecraft:diamond'} 'emerald_ore' {'minecraft:emerald'} }
    $loot = $loot.Replace($originalDrop, $drop).Replace("minecraft:blocks/$template", "theaurorian2:blocks/$name")
    $path=Join-Path $data "loot_table/blocks/$name.json"; New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null; Set-Content -LiteralPath $path -Value $loot -Encoding utf8
}

$orePickaxeTags = ($shallow + $deep) | ForEach-Object { "theaurorian2:$_" }
Write-Json (Join-Path $root 'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json') @{replace=$false;values=@('theaurorian2:aurorian_stone','theaurorian2:aurorian_erosive') + $orePickaxeTags}
$stoneTool = @('aurorian_iron_ore','aurorian_copper_ore','aurorian_lapis_ore','moonstone_ore','cerulean_ore','erosive_aurorian_iron_ore','erosive_aurorian_copper_ore','erosive_aurorian_lapis_ore','erosive_moonstone_ore','erosive_cerulean_ore') | ForEach-Object {"theaurorian2:$_"}
$ironTool = @('aurorian_gold_ore','aurorian_redstone_ore','aurorian_diamond_ore','aurorian_emerald_ore','geode_ore','erosive_aurorian_gold_ore','erosive_aurorian_redstone_ore','erosive_aurorian_diamond_ore','erosive_aurorian_emerald_ore','erosive_geode_ore') | ForEach-Object {"theaurorian2:$_"}
Write-Json (Join-Path $root 'src/main/resources/data/minecraft/tags/block/needs_stone_tool.json') @{replace=$false;values=$stoneTool}
Write-Json (Join-Path $root 'src/main/resources/data/minecraft/tags/block/needs_iron_tool.json') @{replace=$false;values=$ironTool}

$biomePath = Join-Path $data 'worldgen/biome/aurorian_plains.json'
$biome = Get-Content -Raw -LiteralPath $biomePath | ConvertFrom-Json
$features = @($biome.features)
while ($features.Count -lt 11) { $features += ,@() }
$features[6] = @('ore_coal_upper','ore_coal_lower','ore_iron_upper','ore_iron_middle','ore_iron_small','ore_copper','ore_gold','ore_gold_lower','ore_redstone','ore_redstone_lower','ore_lapis','ore_lapis_buried','ore_diamond','ore_diamond_medium','ore_diamond_large','ore_diamond_buried','ore_emerald','ore_moonstone_upper','ore_moonstone_middle','ore_moonstone_small','ore_cerulean_upper','ore_cerulean_middle','ore_cerulean_small','ore_geode','ore_geode_medium','ore_geode_large','ore_geode_buried') | ForEach-Object {"theaurorian2:$_"}
$biome.features = $features
Write-Json $biomePath $biome
