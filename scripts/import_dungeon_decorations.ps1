param([string]$Python = 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'json_utils.ps1')
& $Python (Join-Path $PSScriptRoot 'import_dungeon_decorations.py')
if ($LASTEXITCODE -ne 0) { throw 'Dungeon decoration import failed.' }
$generated = Get-Content -LiteralPath (Join-Path $projectRoot 'build/dungeon-props/generated.staging') -Raw | ConvertFrom-Json
foreach ($entry in $generated.PSObject.Properties) { Write-Json $entry.Name $entry.Value }
$manifest = Get-Content -LiteralPath (Join-Path $projectRoot 'build/dungeon-props/manifest.staging') -Raw | ConvertFrom-Json
Write-Json (Join-Path $projectRoot 'build/dungeon-props/manifest.json') $manifest
