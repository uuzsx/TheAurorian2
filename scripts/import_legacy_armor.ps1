param([string]$LegacyRoot = 'D:/TheAurorian-NeoForge-1.21/TheAurorian-NeoForge-1.21')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$repo = Split-Path $PSScriptRoot
$env:PYTHONUTF8 = '1'
python "$PSScriptRoot/import_legacy_armor.py" $LegacyRoot
if ($LASTEXITCODE -ne 0) { throw 'Legacy armor validation failed.' }
$pending = Get-Content -LiteralPath "$repo/build/legacy-armor-resources.json" -Raw | ConvertFrom-Json -AsHashtable
foreach ($path in $pending.Keys) { Write-Json (Join-Path $repo $path) $pending[$path] }
