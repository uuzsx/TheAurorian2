param([Parameter(Mandatory=$true)][string]$Source)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& python "$PSScriptRoot/import_rising_clock_oracle.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Rising Clock Oracle import failed.' }
$pending = Get-Content -LiteralPath "$projectRoot/build/oracle-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
foreach ($entry in $pending.GetEnumerator()) { Write-Json (Join-Path $projectRoot $entry.Key) $entry.Value }
