param(
    [string]$ModelSource='C:/Users/84446/Downloads/ent.bbmodel',
    [string]$GeometrySource='D:/极光/build/starblight-ent/geometry.raw.json',
    [string]$AnimationSource='D:/极光/build/starblight-ent/animations.raw.json'
)
$ErrorActionPreference='Stop'
. "$PSScriptRoot/json_utils.ps1"
$root=Join-Path (Split-Path $PSScriptRoot) 'src/main/resources'
$assets="$root/assets/theaurorian2"
$data="$root/data/theaurorian2"
$model=Get-Content -LiteralPath $ModelSource -Raw | ConvertFrom-Json -AsHashtable
$geometry=Get-Content -LiteralPath $GeometrySource -Raw | ConvertFrom-Json -AsHashtable
$animation=Get-Content -LiteralPath $AnimationSource -Raw | ConvertFrom-Json -AsHashtable
if ($model.meta.model_format -ne 'geckolib_model' -or $model.elements.Count -ne 5 -or $model.groups.Count -ne 5) { throw 'Unexpected ent model layout' }
if ($animation.animations.Count -ne 9) { throw 'Expected all nine authored ent animations' }
$geometry['minecraft:geometry'][0].description.identifier='geometry.starblight_ent'
Write-Json "$assets/geckolib/models/entity/starblight_ent.geo.json" $geometry
Write-Json "$assets/geckolib/animations/entity/starblight_ent.animation.json" $animation
$textureDir="$assets/textures/entity"
New-Item -ItemType Directory -Force -Path $textureDir | Out-Null
$bytes=[Convert]::FromBase64String(($model.textures[0].source -split ',',2)[1])
[System.IO.File]::WriteAllBytes("$textureDir/starblight_ent.png",$bytes)
Write-Json "$assets/items/starblight_ent_spawn_egg.json" @{model=@{type='minecraft:model';model='minecraft:item/endermite_spawn_egg'}}
foreach ($locale in @('zh_cn','en_us')) {
    $path="$assets/lang/$locale.json"
    $lang=Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable
    $lang['entity.theaurorian2.starblight_ent']=if ($locale -eq 'zh_cn') {'星蚀小树精'} else {'Starblight Ent'}
    $lang['item.theaurorian2.starblight_ent_spawn_egg']=if ($locale -eq 'zh_cn') {'星蚀小树精刷怪蛋'} else {'Starblight Ent Spawn Egg'}
    Write-Json $path $lang
}
Write-Json "$data/neoforge/biome_modifier/starblight_ent_spawns.json" @{
    type='neoforge:add_spawns';biomes='theaurorian2:starblight_swamp';
    spawners=@{type='theaurorian2:starblight_ent';weight=6;minCount=1;maxCount=1}}
Write-Json "$data/loot_table/entities/starblight_ent.json" @{type='minecraft:entity';pools=@(
    @{rolls=1;entries=@(@{type='minecraft:item';name='theaurorian2:starblight_log'})},
    @{rolls=1;entries=@(@{type='minecraft:item';name='minecraft:stick'});
        functions=@(@{function='minecraft:set_count';count=@{type='minecraft:uniform';min=0;max=2}})})}
Write-Output 'Imported Starblight Ent: original geometry/texture, 9 animations, scoped natural spawns, egg, loot and translations.'
