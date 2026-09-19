$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
python "$PSScriptRoot/import_crates_v2_chests.py"
if ($LASTEXITCODE -ne 0) { throw 'Crates v2 chest import failed' }
$assets = "$(Split-Path $PSScriptRoot)/src/main/resources/assets/theaurorian2"
$paths = @(Get-ChildItem "$assets/models/chest" -Filter *.json)
foreach ($name in @('aurorian_chest','weeping_willow_chest','curtain_wood_chest','cursed_frost_wood_chest','filthy_wood_chest')) {
    $paths += Get-Item "$assets/models/item/$name.json"
}
foreach ($path in $paths) { Write-Json $path.FullName (Get-Content -LiteralPath $path.FullName -Raw -Encoding UTF8 | ConvertFrom-Json) }
