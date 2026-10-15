param([Parameter(Mandatory=$true)][string]$Source,
    [string]$Python = "$env:USERPROFILE/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe")
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/json_utils.ps1"
& $Python "$PSScriptRoot/import_oracle_artificer.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Artificer spell import failed.' }
$pending = Get-Content -LiteralPath "$projectRoot/build/oracle-artificer-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
foreach ($entry in $pending.GetEnumerator()) { Write-Json (Join-Path $projectRoot $entry.Key) $entry.Value }
