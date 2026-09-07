param([string]$Python = 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& $Python "$PSScriptRoot/build_moon_chest.py"
if ($LASTEXITCODE -ne 0) { throw 'Moon chest export failed.' }
$destination=Join-Path (Split-Path $PSScriptRoot) 'exports/moon_chest'
$modelPath="$destination/moon_chest.bbmodel"
Write-Json $modelPath (Get-Content $modelPath -Raw -Encoding UTF8 | ConvertFrom-Json)
Compress-Archive -Path "$destination/moon_chest.bbmodel","$destination/moon_chest.png","$destination/overview.png","$destination/opening.gif","$destination/README.txt" -DestinationPath "$destination/moon_chest.zip" -Force
