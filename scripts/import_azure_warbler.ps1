param([string]$Source = 'C:/Users/84446/Downloads/bird.zip')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& python "$PSScriptRoot/import_azure_warbler.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Azure Warbler import failed.' }
$pending = Get-Content -LiteralPath "$projectRoot/build/azure-warbler-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($entry in $pending.PSObject.Properties) {
    Write-Json (Join-Path $projectRoot $entry.Name) $entry.Value
}
