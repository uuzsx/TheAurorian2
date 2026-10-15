$ErrorActionPreference='Stop'
. "$PSScriptRoot/json_utils.ps1"
$woodResources=Join-Path (Split-Path $PSScriptRoot) 'src/main/resources'
$woodAssets="$woodResources/assets/theaurorian2"
$woodData="$woodResources/data/theaurorian2"
$woodVariants=@(
    @{id='silent_wood';cn='谧木小树精';en='Silent Wood Ent';log='silent_tree_log';biome='silent_wood_forest';egg='allay'},
    @{id='curtain_tree';cn='幽帘小树精';en='Curtain Tree Ent';log='curtain_tree_log';biome='curtain_tree_forest';egg='creaking'},
    @{id='cursed_frost';cn='咒霜小树精';en='Cursed Frost Ent';log='cursed_frost_tree_log';biome='cursed_frost_forest';egg='stray'}
)
# The original five-bone geometry and all nine animations are shared exactly; the renderer binds existing wood pixels per face.
foreach ($wood in $woodVariants) {
    $id=$wood.id+'_ent'
    Write-Json "$woodAssets/items/${id}_spawn_egg.json" @{model=@{type='minecraft:model';model="minecraft:item/$($wood.egg)_spawn_egg"}}
    Write-Json "$woodData/tags/worldgen/biome/has_$id.json" @{replace=$false;values=@("theaurorian2:$($wood.biome)","theaurorian2:$($wood.biome)_hills")}
    Write-Json "$woodData/neoforge/biome_modifier/${id}_spawns.json" @{type='neoforge:add_spawns';biomes="#theaurorian2:has_$id";
        spawners=@{type="theaurorian2:$id";weight=1;minCount=1;maxCount=1}}
    Write-Json "$woodData/loot_table/entities/$id.json" @{type='minecraft:entity';pools=@(
        @{rolls=1;entries=@(@{type='minecraft:item';name="theaurorian2:$($wood.log)"})},
        @{rolls=1;entries=@(@{type='minecraft:item';name='minecraft:stick'});
            functions=@(@{function='minecraft:set_count';count=@{type='minecraft:uniform';min=0;max=2}})})}
}
foreach ($locale in @('zh_cn','en_us')) {
    $woodLangPath="$woodAssets/lang/$locale.json"
    $woodLang=Get-Content -LiteralPath $woodLangPath -Raw | ConvertFrom-Json -AsHashtable
    foreach ($wood in $woodVariants) {
        $woodName=if ($locale -eq 'zh_cn') {$wood.cn} else {$wood.en}
        $woodLang["entity.theaurorian2.$($wood.id)_ent"]=$woodName
        $woodLang["item.theaurorian2.$($wood.id)_ent_spawn_egg"]=if ($locale -eq 'zh_cn') { $woodName+'刷怪蛋' } else {$woodName+' Spawn Egg'}
    }
    Write-Json $woodLangPath $woodLang
}
Write-Output 'Generated three woodland ent variants: forest habitats, individual spawns, matching log loot, eggs and names.'
