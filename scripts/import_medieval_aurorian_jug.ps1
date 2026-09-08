$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/import_medieval_aurorian_jug.py"
if ($LASTEXITCODE -ne 0) { throw 'Aurorian jug import failed.' }
Get-Content "$PSScriptRoot/../exports/aurorian_jug/generated-files.txt" | ForEach-Object {
    Write-Json $_ (Get-Content -Raw -LiteralPath $_ | ConvertFrom-Json -AsHashtable)
}
