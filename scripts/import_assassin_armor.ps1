param([Parameter(Mandatory = $true)][string]$SourceRoot)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$repo = Split-Path $PSScriptRoot
$env:PYTHONUTF8 = '1'
python "$PSScriptRoot/import_assassin_armor.py" $SourceRoot
if ($LASTEXITCODE -ne 0) { throw 'Assassin source validation failed.' }
$pending = Get-Content -LiteralPath "$repo/build/assassin-armor-resources.json" -Raw | ConvertFrom-Json -AsHashtable
foreach ($path in $pending.Keys) { Write-Json (Join-Path $repo $path) $pending[$path] }
Write-Host "Imported five assassin sets / 20 armor items."
