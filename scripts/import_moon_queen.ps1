param([Parameter(Mandatory=$true)][string]$Source, [string]$Python = 'python')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& $Python "$PSScriptRoot/import_moon_queen.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Moon queen import failed' }
$pending = Get-Content -LiteralPath "$projectRoot/build/moon-queen-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($entry in $pending.PSObject.Properties) {
    Write-Json (Join-Path $projectRoot $entry.Name) $entry.Value
}
