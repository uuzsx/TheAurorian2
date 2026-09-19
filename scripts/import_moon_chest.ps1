param([string]$Source = 'E:/BaiduNetdiskDownload/1407箱子包第二卷/Crates Pack v2/Blockbench files')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
python "$PSScriptRoot/import_moon_chest.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Moon chest import failed' }
$root = Split-Path $PSScriptRoot
$pending = Get-Content -LiteralPath "$root/build/moon-chest/pending.json" -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
foreach ($entry in $pending.GetEnumerator()) { Write-Json (Join-Path $root $entry.Key) $entry.Value }
