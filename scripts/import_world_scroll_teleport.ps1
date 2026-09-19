param(
    [string]$Source = 'E:/BaiduNetdiskDownload/1731传送动画.zip',
    [string]$FFmpeg = 'ffmpeg'
)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
python "$PSScriptRoot/import_world_scroll_teleport.py" $Source $FFmpeg
if ($LASTEXITCODE -ne 0) { throw 'World scroll teleport import failed' }
$root = Split-Path $PSScriptRoot
$pending = Get-Content -LiteralPath "$root/build/world-scroll-teleport-resources.json" -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
foreach ($entry in $pending.GetEnumerator()) { Write-Json (Join-Path $root $entry.Key) $entry.Value }
