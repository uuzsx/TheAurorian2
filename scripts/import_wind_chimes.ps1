param([string]$Source = 'E:/BaiduNetdiskDownload/1316场景风铃/1316场景风铃')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
python "$PSScriptRoot/import_wind_chimes.py" $Source
if ($LASTEXITCODE -ne 0) { throw 'Wind chimes import failed' }
$root = Split-Path $PSScriptRoot
$pending = Get-Content -LiteralPath "$root/build/wind-chimes/pending.json" -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
foreach ($entry in $pending.GetEnumerator()) { Write-Json (Join-Path $root $entry.Key) $entry.Value }
