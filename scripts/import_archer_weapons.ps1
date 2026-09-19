param(
    [Parameter(Mandatory = $true)][string]$SourceRoot
)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$resources = Join-Path (Split-Path $PSScriptRoot) 'src/main/resources'
$assets = Join-Path $resources 'assets/theaurorian2'
$raw = Join-Path $SourceRoot 'raw models'
$variants = @(
    @{ source = 'diamond'; id = 'starlight_ranger'; zh = '星辉游侠'; en = 'Starlight Ranger' },
    @{ source = 'golden'; id = 'dawnlight_ranger'; zh = '曦光游侠'; en = 'Dawnlight Ranger' },
    @{ source = 'iron'; id = 'forestshade_ranger'; zh = '森影游侠'; en = 'Forestshade Ranger' },
    @{ source = 'netherite'; id = 'duskflame_ranger'; zh = '暮焰游侠'; en = 'Duskflame Ranger' }
)
$types = @(
    @{ source = 'light_bow'; id = 'light_bow'; zh = '轻弓'; en = 'Light Bow' },
    @{ source = 'heavy_bow'; id = 'heavy_bow'; zh = '重弓'; en = 'Heavy Bow' },
    @{ source = 'archer_crossbow'; id = 'crossbow'; zh = '弩'; en = 'Crossbow' }
)
function Model-Reference([string]$id) {
    return @{ type = 'minecraft:model'; model = "theaurorian2:item/$id" }
}
function Add-TagValues([string]$relativePath, [string[]]$ids) {
    $path = Join-Path $resources $relativePath
    $tag = if (Test-Path -LiteralPath $path) { Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable } else { @{ replace = $false; values = @() } }
    $tag.values = @(@($tag.values) + $ids | Select-Object -Unique)
    Write-Json $path $tag
}
$zh = Get-Content -LiteralPath "$assets/lang/zh_cn.json" -Raw | ConvertFrom-Json -AsHashtable
$en = Get-Content -LiteralPath "$assets/lang/en_us.json" -Raw | ConvertFrom-Json -AsHashtable
$bows = @()
$crossbows = @()
foreach ($variant in $variants) {
    foreach ($type in $types) {
        $id = "$($variant.id)_$($type.id)"
        $sourceId = "$($variant.source)_$($type.source)"
        $crossbow = $type.id -eq 'crossbow'
        if ($crossbow) { $crossbows += "theaurorian2:$id" } else { $bows += "theaurorian2:$id" }
        $states = @('', '_pulling0', '_pulling1', '_pulling2')
        if ($crossbow) { $states += '_charget' }
        foreach ($state in $states) {
            $model = Get-Content -LiteralPath (Join-Path $raw "$sourceId$state.json") -Raw | ConvertFrom-Json -AsHashtable
            $stateId = $id + ($state -replace '_pulling', '_pulling_' -replace '_charget', '_charged')
            foreach ($key in @($model.textures.Keys)) {
                if ($model.textures[$key] -ne $sourceId) { throw "Unexpected texture in $sourceId$state" }
                $model.textures[$key] = "theaurorian2:item/$id"
            }
            $model.textures.particle = "theaurorian2:item/$id"
            # Center the baked silhouettes; the source crossbow exceeds a 16-pixel inventory slot.
            switch ($type.id) {
                'light_bow' { $model.display.gui.translation = @(0.87414, 0.87414, -3.5) }
                'heavy_bow' { $model.display.gui.translation = @(0.33523, 0.33523, -3.5) }
                'crossbow' {
                    $model.display.gui.translation = @(-0.27691, -0.27691, -3.5)
                    $model.display.gui.scale = @(0.4, 0.4, 0.4)
                }
            }
            Write-Json "$assets/models/item/$stateId.json" $model
        }
        Copy-Item -LiteralPath (Join-Path $raw "$sourceId.png") -Destination "$assets/textures/item/$id.png"
        $pull = @{
            type = 'minecraft:range_dispatch'
            property = 'minecraft:use_duration'
            scale = 0.05
            fallback = (Model-Reference "${id}_pulling_0")
            entries = @(
                @{ threshold = 0.65; model = (Model-Reference "${id}_pulling_1") },
                @{ threshold = 0.9; model = (Model-Reference "${id}_pulling_2") }
            )
        }
        if ($crossbow) {
            $pull.property = 'minecraft:crossbow/pull'
            $pull.Remove('scale')
            $pull.entries[0].threshold = 0.58
            $pull.entries[1].threshold = 1.0
        }
        $model = @{
            type = 'minecraft:condition'; property = 'minecraft:using_item'
            on_false = (Model-Reference $id); on_true = $pull
        }
        if ($crossbow) {
            # The supplied pack uses the same loaded appearance for arrows and fireworks.
            $model = @{
                type = 'minecraft:select'; property = 'minecraft:charge_type'; fallback = $model
                cases = @(@{ when = @('arrow', 'rocket'); model = (Model-Reference "${id}_charged") })
            }
        }
        Write-Json "$assets/items/$id.json" @{ model = $model }
        $zh["item.theaurorian2.$id"] = $variant.zh + $type.zh
        $en["item.theaurorian2.$id"] = "$($variant.en) $($type.en)"
    }
}
Write-Json "$assets/lang/zh_cn.json" $zh
Write-Json "$assets/lang/en_us.json" $en
Add-TagValues 'data/minecraft/tags/item/enchantable/bow.json' $bows
Add-TagValues 'data/minecraft/tags/item/enchantable/crossbow.json' $crossbows
Add-TagValues 'data/minecraft/tags/item/enchantable/durability.json' ($bows + $crossbows)
Add-TagValues 'data/c/tags/item/tools/bow.json' $bows
Add-TagValues 'data/c/tags/item/tools/crossbow.json' $crossbows
Add-TagValues 'data/theaurorian2/tags/item/ranger_ranged_weapons.json' ($bows + $crossbows)
Write-Host 'Imported 12 ranger weapons, 52 state models and 12 original textures.'
