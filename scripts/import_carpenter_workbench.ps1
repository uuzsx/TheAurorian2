$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/import_carpenter_workbench.py"
if ($LASTEXITCODE -ne 0) { throw 'Carpenter workbench import failed.' }
Get-Content "$PSScriptRoot/../exports/carpenter_workbench/generated-files.txt" -Encoding UTF8 | ForEach-Object {
    Write-Json $_ (Get-Content -Raw -LiteralPath $_ -Encoding UTF8 | ConvertFrom-Json)
}
