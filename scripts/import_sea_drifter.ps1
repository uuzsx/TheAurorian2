param([Parameter(Mandatory=$true)][string]$Source)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& python "$PSScriptRoot/import_sea_drifter.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Sea drifter import failed' }
$pending = Get-Content -LiteralPath "$projectRoot/build/sea-drifter-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($entry in $pending.PSObject.Properties) {
    Write-Json (Join-Path $projectRoot $entry.Name) $entry.Value
}
