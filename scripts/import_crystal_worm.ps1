param([Parameter(Mandatory=$true)][string]$Source)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& python "$PSScriptRoot/import_crystal_worm.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Crystal worm import failed' }
$pending = Get-Content -LiteralPath "$projectRoot/build/crystal-worm-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($entry in $pending.PSObject.Properties) {
    Write-Json (Join-Path $projectRoot $entry.Name) $entry.Value
}
