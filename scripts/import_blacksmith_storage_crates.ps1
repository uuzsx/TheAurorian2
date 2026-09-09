$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/import_blacksmith_storage_crates.py"
if ($LASTEXITCODE -ne 0) { throw 'Blacksmith storage crate import failed.' }
Get-Content "$PSScriptRoot/../exports/wood_storage_crates/generated-files.txt" | ForEach-Object {
    Write-Json $_ (Get-Content -Raw -LiteralPath $_ | ConvertFrom-Json -AsHashtable)
}
