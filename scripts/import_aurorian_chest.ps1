$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$root = Split-Path $PSScriptRoot
$assets = "$root/src/main/resources/assets"
$models = @{}
foreach ($variant in @('single', 'left', 'right')) {
    $source = if ($variant -eq 'single') {
        "$root/exports/aurorian_wood_chest/aurorian_wood_chest.bbmodel"
    } else {
        "$root/exports/aurorian_double_wood_chest/aurorian_double_wood_chest_$variant.bbmodel"
    }
    $model = Get-Content -LiteralPath $source -Raw -Encoding UTF8 | ConvertFrom-Json
    $models[$variant] = $model
    $groups = [ordered]@{}
    foreach ($group in $model.outliner) {
        $elements = @($model.elements | Where-Object { $_.uuid -in $group.children } | ForEach-Object {
            $faces = [ordered]@{}
            foreach ($face in $_.faces.PSObject.Properties) { $faces[$face.Name] = $face.Value.uv }
            [ordered]@{from=$_.from; to=$_.to; origin=$_.origin; rotation=$_.rotation; faces=$faces}
        })
        $groups[($group.name -split '_')[0]] = [ordered]@{origin=$group.origin; elements=$elements}
    }
    Write-Json "$assets/theaurorian2/models/chest/$variant.json" $groups
}
$itemElements = @($models['single'].elements | ForEach-Object {
    $element = [ordered]@{from=$_.from; to=$_.to}
    $axes = @('x','y','z')
    $rotations = 0
    for ($i=0; $i -lt 3; $i++) {
        if ($_.rotation[$i] -ne 0) {
            $rotations++
            $element.rotation = [ordered]@{origin=$_.origin; axis=$axes[$i]; angle=$_.rotation[$i]; rescale=$false}
        }
    }
    if ($rotations -gt 1) { throw 'Item model requires a multi-axis rotation; conversion would not preserve it.' }
    $faces = [ordered]@{}
    foreach ($face in $_.faces.PSObject.Properties) {
        $faces[$face.Name] = [ordered]@{uv=@($face.Value.uv | ForEach-Object { $_ / 4 }); texture='#chest'}
    }
    $element.faces = $faces
    $element
})
Write-Json "$assets/theaurorian2/models/item/aurorian_chest.json" ([ordered]@{
    parent='minecraft:block/block'
    textures=@{particle='theaurorian2:block/silent_tree_planks'; chest='theaurorian2:block/aurorian_chest_model'}
    display=@{
        gui=@{rotation=@(30,45,0); translation=@(0,0,0); scale=@(0.625,0.625,0.625)}
        ground=@{rotation=@(0,0,0); translation=@(0,3,0); scale=@(0.25,0.25,0.25)}
        fixed=@{rotation=@(0,180,0); translation=@(0,0,0); scale=@(0.5,0.5,0.5)}
        thirdperson_righthand=@{rotation=@(75,45,0); translation=@(0,2.5,0); scale=@(0.375,0.375,0.375)}
        firstperson_righthand=@{rotation=@(0,45,0); translation=@(0,0,0); scale=@(0.4,0.4,0.4)}
        firstperson_lefthand=@{rotation=@(0,225,0); translation=@(0,0,0); scale=@(0.4,0.4,0.4)}
    }
    elements=$itemElements
})
Write-Json "$assets/theaurorian2/items/silent_wood_chest.json" @{model=@{type='minecraft:model'; model='theaurorian2:item/aurorian_chest'}}
$chestAtlasPath = "$assets/minecraft/atlases/chests.json"
$chestAtlas = Get-Content -LiteralPath $chestAtlasPath -Raw | ConvertFrom-Json
$chestAtlas.sources = @($chestAtlas.sources | Where-Object { $_.resource -ne 'theaurorian2:entity/chest/aurorian_chest' }) + @(@{type='minecraft:single'; resource='theaurorian2:entity/chest/aurorian_chest'})
Write-Json $chestAtlasPath $chestAtlas
$blockAtlasPath = "$assets/minecraft/atlases/blocks.json"
$blockAtlas = Get-Content -LiteralPath $blockAtlasPath -Raw | ConvertFrom-Json
$blockAtlas.sources = @($blockAtlas.sources | Where-Object { $_.resource -ne 'theaurorian2:entity/chest/aurorian_chest' }) + @(@{
    type='minecraft:single'; resource='theaurorian2:entity/chest/aurorian_chest'; sprite='theaurorian2:block/aurorian_chest_model'
})
Write-Json $blockAtlasPath $blockAtlas
Copy-Item -LiteralPath "$root/exports/aurorian_double_wood_chest/aurorian_chest.png" -Destination "$assets/theaurorian2/textures/entity/chest/aurorian_chest.png"
