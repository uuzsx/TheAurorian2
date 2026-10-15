param([Parameter(Mandatory=$true)][string]$Source)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& python "$PSScriptRoot/import_clock_raptor.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Clock Raptor import failed.' }
$pending = Get-Content -LiteralPath "$projectRoot/build/clock-raptor-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($entry in $pending.PSObject.Properties) {
    Write-Json (Join-Path $projectRoot $entry.Name) $entry.Value
}
