$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/import_medieval_bench_tables.py"
if ($LASTEXITCODE -ne 0) { throw 'Medieval bench/table import failed.' }
Get-Content "$PSScriptRoot/../exports/medieval_bench_tables/generated-files.txt" | ForEach-Object {
    Write-Json $_ (Get-Content -Raw -LiteralPath $_ | ConvertFrom-Json -AsHashtable)
}
