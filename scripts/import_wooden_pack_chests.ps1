$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/import_wooden_pack_chests.py"
if ($LASTEXITCODE -ne 0) { throw 'Wooden Pack import failed' }
$root=Split-Path $PSScriptRoot
$paths=@(Get-ChildItem "$root/exports/wooden_pack_chests" -Recurse -Filter *.bbmodel)
$paths+=@(Get-ChildItem "$root/src/main/resources/assets/theaurorian2/models/chest" -Filter *.json)
foreach ($name in @('aurorian_chest','weeping_willow_chest','curtain_wood_chest','cursed_frost_wood_chest','filthy_wood_chest')) {
 $paths+=Get-Item "$root/src/main/resources/assets/theaurorian2/models/item/$name.json"
}
foreach ($path in $paths) { Write-Json $path.FullName (Get-Content -LiteralPath $path.FullName -Raw -Encoding UTF8 | ConvertFrom-Json) }
