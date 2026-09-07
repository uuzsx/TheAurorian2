$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' "$PSScriptRoot/build_wood_stools.py"
if ($LASTEXITCODE -ne 0) { throw 'Stool generation failed' }
$root=Split-Path $PSScriptRoot
foreach ($path in Get-Content "$root/exports/wood_stools/generated-files.txt" -Encoding UTF8) {
 Write-Json $path (Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable)
}
