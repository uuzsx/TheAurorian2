$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$root = Split-Path $PSScriptRoot
$assets = "$root/src/main/resources/assets"
$data = "$root/src/main/resources/data"
$variants = @(
    @('weeping_willow', 'weeping_willow_chest', 'weeping_willow_planks', '垂柳木箱', 'Weeping Willow Chest'),
    @('curtain', 'curtain_wood_chest', 'curtain_tree_planks', '幽帘木箱', 'Curtain Wood Chest'),
    @('cursed_frost', 'cursed_frost_wood_chest', 'cursed_frost_tree_planks', '咒霜木箱', 'Cursed Frost Wood Chest'),
    @('filthy', 'filthy_wood_chest', 'filthy_tree_planks', '污秽木箱', 'Filthy Wood Chest')
)
$chestAtlasPath = "$assets/minecraft/atlases/chests.json"
$blockAtlasPath = "$assets/minecraft/atlases/blocks.json"
$chestAtlas = Get-Content $chestAtlasPath -Raw | ConvertFrom-Json
$blockAtlas = Get-Content $blockAtlasPath -Raw | ConvertFrom-Json
$zh = Get-Content "$assets/theaurorian2/lang/zh_cn.json" -Raw -Encoding UTF8 | ConvertFrom-Json
$en = Get-Content "$assets/theaurorian2/lang/en_us.json" -Raw -Encoding UTF8 | ConvertFrom-Json
$fuelPath = "$data/neoforge/data_maps/item/furnace_fuels.json"
$fuels = Get-Content $fuelPath -Raw | ConvertFrom-Json
foreach ($variant in $variants) {
    $folder, $id, $planks, $chinese, $english = $variant
    Copy-Item -LiteralPath "$root/exports/aurorian_wood_chest_variants/$folder/chest.png" -Destination "$assets/theaurorian2/textures/entity/chest/$id.png"
    # All variants inherit the exact approved single geometry and item transforms.
    Write-Json "$assets/theaurorian2/models/item/$id.json" @{
        parent='theaurorian2:item/aurorian_chest'
        textures=@{particle="theaurorian2:block/$planks"; chest="theaurorian2:block/${id}_model"}
    }
    Write-Json "$assets/theaurorian2/items/$id.json" @{model=@{type='minecraft:model'; model="theaurorian2:item/$id"}}
    Write-Json "$assets/theaurorian2/models/block/$id.json" @{
        parent='theaurorian2:block/aurorian_chest'; textures=@{particle="theaurorian2:block/$planks"}
    }
    Write-Json "$assets/theaurorian2/blockstates/$id.json" @{multipart=@(@{apply=@{model="theaurorian2:block/$id"}})}
    Write-Json "$data/theaurorian2/recipe/$id.json" @{
        type='minecraft:crafting_shaped'; category='misc'; group='theaurorian2:wooden_chests'
        pattern=@('###','# #','###'); key=@{'#'="theaurorian2:$planks"}; result=@{id="theaurorian2:$id";count=1}
    }
    Write-Json "$data/theaurorian2/advancement/recipes/decorations/$id.json" @{
        parent='minecraft:recipes/root'
        criteria=@{
            has_planks=@{trigger='minecraft:inventory_changed';conditions=@{items=@(@{items="theaurorian2:$planks"})}}
            has_the_recipe=@{trigger='minecraft:recipe_unlocked';conditions=@{recipe="theaurorian2:$id"}}
        }
        requirements=@(,@('has_planks','has_the_recipe'))
        rewards=@{recipes=@("theaurorian2:$id")}
    }
    $loot = Get-Content "$data/theaurorian2/loot_table/blocks/silent_wood_chest.json" -Raw | ConvertFrom-Json
    $loot.pools[0].entries[0].name="theaurorian2:$id"
    Write-Json "$data/theaurorian2/loot_table/blocks/$id.json" $loot
    $resource="theaurorian2:entity/chest/$id"
    $chestAtlas.sources=@($chestAtlas.sources | Where-Object {$_.resource -ne $resource}) + @(@{type='minecraft:single';resource=$resource})
    $blockAtlas.sources=@($blockAtlas.sources | Where-Object {$_.resource -ne $resource}) + @(@{type='minecraft:single';resource=$resource;sprite="theaurorian2:block/${id}_model"})
    $zh | Add-Member -Force -NotePropertyName "block.theaurorian2.$id" -NotePropertyValue $chinese
    $en | Add-Member -Force -NotePropertyName "block.theaurorian2.$id" -NotePropertyValue $english
    $fuels.values | Add-Member -Force -NotePropertyName "theaurorian2:$id" -NotePropertyValue @{burn_time=300}
}
foreach ($tag in @('c/tags/block/chests/wooden.json','c/tags/item/chests/wooden.json','minecraft/tags/block/mineable/axe.json')) {
    $path="$data/$tag"
    $content=Get-Content $path -Raw | ConvertFrom-Json
    foreach ($variant in $variants) {
        $id="theaurorian2:$($variant[1])"
        if ($id -notin $content.values) {$content.values=@($content.values)+@($id)}
    }
    Write-Json $path $content
}
$recipePath="$data/theaurorian2/recipe/silent_wood_chest.json"
$recipe=Get-Content $recipePath -Raw | ConvertFrom-Json
$recipe.key.'#'='theaurorian2:silent_tree_planks'
Write-Json $recipePath $recipe
Write-Json $chestAtlasPath $chestAtlas
Write-Json $blockAtlasPath $blockAtlas
Write-Json "$assets/theaurorian2/lang/zh_cn.json" $zh
Write-Json "$assets/theaurorian2/lang/en_us.json" $en
Write-Json $fuelPath $fuels
# Prefer exact wood recipes over vanilla's broad #minecraft:planks chest recipe.
$priorityPath="$data/neoforge/recipe_priorities.json"
$priorities=if (Test-Path $priorityPath) {Get-Content $priorityPath -Raw | ConvertFrom-Json} else {[pscustomobject]@{replace=$false;entries=[pscustomobject]@{}}}
foreach ($id in (@('silent_wood_chest') + @($variants | ForEach-Object {$_[1]}))) {
    $priorities.entries | Add-Member -Force -NotePropertyName "theaurorian2:$id" -NotePropertyValue 100
}
Write-Json $priorityPath $priorities
