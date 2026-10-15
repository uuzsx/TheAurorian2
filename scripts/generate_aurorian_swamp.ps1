$ErrorActionPreference='Stop'
. "$PSScriptRoot/json_utils.ps1"
$swampResources=Join-Path (Split-Path $PSScriptRoot) 'src/main/resources'
$swampData="$swampResources/data/theaurorian2"
function Read-SwampJson($path) { Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable }
function Write-SwampData($name,$value) { Write-Json "$swampData/$name.json" $value }
function Swamp-Block($name) { @{type='minecraft:block';result_state=@{Name="theaurorian2:$name"}} }
function Swamp-If($condition,$rule) { @{type='minecraft:condition';if_true=$condition;then_run=$rule} }

# Swamp-only warped-fungus silhouettes, lifted four blocks, with silent wood/leaves.
Write-SwampData 'worldgen/configured_feature/swamp_silent_tree' @{type='theaurorian2:swamp_silent_tree';config=@{}}
# Keep the same total attempts, with occasional indigo and native red/brown mushrooms.
Write-SwampData 'worldgen/configured_feature/trees_aurorian_swamp' @{type='minecraft:random_selector';config=@{
    features=@(
        @{chance=0.08;feature=@{feature='theaurorian2:indigo_mushroom_tree';placement=@()}},
        @{chance=0.04;feature=@{feature='minecraft:huge_red_mushroom';placement=@()}},
        @{chance=0.04;feature=@{feature='minecraft:huge_brown_mushroom';placement=@()}})
    default=@{feature='theaurorian2:swamp_silent_tree';placement=@()}}}
Write-SwampData 'worldgen/placed_feature/trees_aurorian_swamp' @{feature='theaurorian2:trees_aurorian_swamp';placement=@(
    @{type='minecraft:count';count=@{type='minecraft:weighted_list';distribution=@(
        @{data=2;weight=1},@{data=3;weight=1})}},@{type='minecraft:in_square'},
    @{type='minecraft:surface_water_depth_filter';max_water_depth=0},
    @{type='minecraft:heightmap';heightmap='OCEAN_FLOOR'},@{type='minecraft:biome'},
    @{type='minecraft:block_predicate_filter';predicate=@{type='minecraft:would_survive';state=@{Name='theaurorian2:silent_tree_sapling';Properties=@{stage='0'}}}})}

# The existing provider includes both plants and their original randomized glow/orientation states.
Write-SwampData 'worldgen/placed_feature/aurorian_swamp_water_surface_plants' @{
    feature='theaurorian2:aurorian_water_surface_plants';placement=@(
        @{type='minecraft:count';count=2},@{type='minecraft:in_square'},
        @{type='minecraft:heightmap';heightmap='WORLD_SURFACE_WG'},@{type='minecraft:biome'},
        @{type='minecraft:block_predicate_filter';predicate=@{type='minecraft:all_of';predicates=@(
            @{type='minecraft:matching_blocks';blocks='minecraft:air'},
            @{type='minecraft:matching_fluids';fluids='minecraft:water';offset=@(0,-1,0)})}})}
$fireflies=Read-SwampJson "$swampData/worldgen/placed_feature/patch_aurorian_firefly_bush_near_water.json"
$fireflies.placement+=@(@{type='minecraft:biome'})
Write-SwampData 'worldgen/placed_feature/patch_aurorian_swamp_fireflies' $fireflies

$biome=Read-SwampJson "$swampData/worldgen/biome/silent_wood_forest.json"
$biome.temperature=0.8;$biome.downfall=0.9
$biome.effects.water_color='#4d8094'
$biome.attributes['minecraft:visual/water_fog_color']='#244b67'
$biome.attributes['minecraft:visual/water_fog_end_distance']=@{modifier='multiply';argument=0.85}
$biome.attributes['minecraft:gameplay/increased_fire_burnout']=$true
$biome.attributes['minecraft:audio/background_music']=@{default=@{sound='minecraft:music.overworld.swamp';min_delay=12000;max_delay=24000}}
# Wetland beds stay Aurorian dirt instead of receiving sand/clay/gravel disks.
$biome.features[6]=@($biome.features[6] | Where-Object {$_ -notmatch ':disk_'})
$biome.features[9]=@('theaurorian2:trees_aurorian_swamp','theaurorian2:patch_silent_forest_flowers',
    'theaurorian2:patch_silent_forest_grass','theaurorian2:aurorian_seagrass_lakes',
    'theaurorian2:aurorian_swamp_water_surface_plants','theaurorian2:patch_aurorian_swamp_fireflies',
    'theaurorian2:patch_aurorian_twisting_vines')
$biome.features[10]=@('minecraft:freeze_top_layer')
Write-SwampData 'worldgen/biome/aurorian_swamp' $biome

$dimension=Read-SwampJson "$swampData/dimension/the_aurorian.json"
$dimension.generator.biome_source.delegate.replacements['minecraft:swamp']='theaurorian2:aurorian_swamp'
Write-SwampData 'dimension/the_aurorian' $dimension
$preset=Read-SwampJson "$swampData/worldgen/world_preset/aurorian.json"
$source=$preset.dimensions['minecraft:overworld'].generator.biome_source
if ($source.delegate.type -eq 'minecraft:multi_noise') {
    $source.delegate=@{type='theaurorian2:aurorian_climate';vanilla=@{preset='minecraft:overworld'};
        land=$source.delegate;replacements=@{'minecraft:swamp'='theaurorian2:aurorian_swamp';'minecraft:mangrove_swamp'='theaurorian2:starblight_swamp'}}
} else { $source.delegate.replacements['minecraft:swamp']='theaurorian2:aurorian_swamp' }
Write-SwampData 'worldgen/world_preset/aurorian' $preset

foreach ($settings in @('the_aurorian','the_aurorian_lake_islands')) {
    $noise=Read-SwampJson "$swampData/worldgen/noise_settings/$settings.json"
    $pond=Swamp-If @{type='minecraft:y_above';anchor=@{absolute=60};surface_depth_multiplier=0;add_stone_depth=$false} (
        Swamp-If @{type='minecraft:not';invert=@{type='minecraft:y_above';anchor=@{absolute=63};surface_depth_multiplier=0;add_stone_depth=$false}} (
        Swamp-If @{type='minecraft:noise_threshold';noise='minecraft:surface_swamp';min_threshold=0;max_threshold=[double]::MaxValue} @{
            type='minecraft:block';result_state=@{Name='minecraft:water';Properties=@{level='0'}}}))
    $dry=Swamp-If @{type='minecraft:water';offset=0;surface_depth_multiplier=0;add_stone_depth=$false} (Swamp-Block 'aurorian_grass_block')
    $floor=Swamp-If @{type='minecraft:stone_depth';offset=0;surface_type='floor';add_surface_depth=$false;secondary_depth_range=0} @{
        type='minecraft:sequence';sequence=@($pond,$dry,(Swamp-Block 'aurorian_dirt'))}
    $under=Swamp-If @{type='minecraft:stone_depth';offset=0;surface_type='floor';add_surface_depth=$true;secondary_depth_range=0} (Swamp-Block 'aurorian_dirt')
    $surface=Swamp-If @{type='minecraft:biome';biome_is=@('theaurorian2:aurorian_swamp')} (
        Swamp-If @{type='minecraft:above_preliminary_surface'} @{type='minecraft:sequence';sequence=@($floor,$under)})
    $others=@($noise.surface_rule.sequence | Where-Object {$_.if_true.biome_is -notcontains 'theaurorian2:aurorian_swamp'})
    $noise.surface_rule.sequence=@($others[0],$surface)+@($others | Select-Object -Skip 1)
    Write-SwampData "worldgen/noise_settings/$settings" $noise
}
foreach ($tagPath in @('theaurorian2/tags/worldgen/biome/is_aurorian','theaurorian2/tags/worldgen/biome/has_surface_fish','c/tags/worldgen/biome/is_swamp')) {
    $path="$swampResources/data/$tagPath.json";$tag=Read-SwampJson $path
    if ($tag.values -notcontains 'theaurorian2:aurorian_swamp') { $tag.values+=@('theaurorian2:aurorian_swamp') }
    Write-Json $path $tag
}
foreach ($locale in @('zh_cn','en_us')) {
    $path="$swampResources/assets/theaurorian2/lang/$locale.json";$lang=Read-SwampJson $path
    $lang['biome.theaurorian2.aurorian_swamp']=if ($locale -eq 'zh_cn') {'极光沼泽'} else {'Aurorian Swamp'}
    Write-Json $path $lang
}
Write-Output 'Generated Aurorian Swamp: ordinary swamp mapping, Aurorian soil, silent trees and both water-surface plants.'
