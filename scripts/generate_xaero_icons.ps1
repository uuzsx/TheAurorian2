$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'json_utils.ps1')
$definitionRoot = Join-Path $PSScriptRoot '../src/main/resources/assets/xaerominimap/entity/icon/definition/theaurorian2'
$modelNamespace = 'cn.teampancake.theaurorian2.client.model.'

# Resolve the actual head, including its children; retain the imported horn/ear rotations.
$heads = @(
    @('aurorian_cow', 'AurorianCowModel', 0.65),
    @('aurorian_pig', 'AurorianPigModel', 0.9),
    @('aurorian_sheep', 'AurorianSheepModel', 0.9),
    @('aurorian_rabbit', 'AurorianRabbitModel', 1.2)
)
foreach ($head in $heads) {
    $field = $modelNamespace + $head[1] + ';head'
    Write-Json (Join-Path $definitionRoot ($head[0] + '.json')) ([ordered]@{
        variants = @{ default = 'model:0' }
        modelConfigs = @([ordered]@{
            baseScale = $head[2]
            modelMainPartFieldAliases = @($field)
            modelPartsFields = @($field)
            renderingFullModel = $false
            modelPartsRotationReset = $false
            layersAllowed = $false
        })
    })
}

# Render the root itself so its cubes and children are all included. Xaero already
# rotates CodModel icons sideways; retain the imported root poses without another yaw.
foreach ($fish in @('moon_fish', 'aurorian_winged_fish')) {
    Write-Json (Join-Path $definitionRoot ($fish + '.json')) ([ordered]@{
        variants = @{ default = 'model:0' }
        modelConfigs = @([ordered]@{
            baseScale = 1.2
            modelMainPartFieldAliases = @('net.minecraft.client.model.Model;root')
            modelPartsFields = @('net.minecraft.client.model.Model;root')
            renderingFullModel = $false
            modelPartsRotationReset = $false
            layersAllowed = $false
        })
    })
}

foreach ($humanoid in @('iseryn_vale', 'purification_ritual_zombie')) {
    Write-Json (Join-Path $definitionRoot ($humanoid + '.json')) @{ variants = @{ default = 'model' } }
}

# Xaero's model icon renderer requires a vanilla EntityModel. GeckoLib models use
# native game-rendered sprites instead, without an optional Java dependency or Mixin.
foreach ($mob in @('blue_tail_wolf', 'spirit', 'moonreaver_skeleton',
        'moonreaver_skeleton_swordsman', 'moonreaver_skeleton_captain', 'frostfin',
        'azure_warbler', 'training_dummy', 'spider_mother', 'spiderling',
        'spiderling_crystal_shell', 'spiderling_wall_climber', 'spider_egg')) {
    # All three skeleton types have the same head and texture; the captain's armor
    # layer only covers the torso and limbs.
    $sprite = if ($mob.StartsWith('moonreaver_skeleton')) { 'moonreaver_skeleton' } else { $mob }
    # These three models have identical rendered heads, including their teeth and UVs.
    if ($mob.StartsWith('spiderling')) { $sprite = 'spiderling' }
    Write-Json (Join-Path $definitionRoot ($mob + '.json')) @{
        variants = @{ default = ('outlined_sprite:theaurorian2/' + $sprite + '.png') }
    }
}
