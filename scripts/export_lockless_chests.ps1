$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
$root = Split-Path $PSScriptRoot
$destination = "$root/exports/aurorian_lockless_chests"
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$sources = @(
    'aurorian_wood_chest/aurorian_wood_chest.bbmodel',
    'aurorian_double_wood_chest/aurorian_double_wood_chest.bbmodel',
    'aurorian_double_wood_chest/aurorian_double_wood_chest_left.bbmodel',
    'aurorian_double_wood_chest/aurorian_double_wood_chest_right.bbmodel'
)
foreach ($source in $sources) {
    $model = Get-Content -LiteralPath "$root/exports/$source" -Raw -Encoding UTF8 | ConvertFrom-Json
    $removed = @($model.elements | Where-Object { $_.name -match '锁扣|钥匙孔' } | ForEach-Object { $_.uuid })
    if ($removed.Count -eq 0) { throw "No lock found in $source" }
    $model.elements = @($model.elements | Where-Object { $_.uuid -notin $removed })
    foreach ($group in $model.outliner) {
        $group.children = @($group.children | Where-Object { $_ -notin $removed })
    }
    $model.name += ' · 无锁版'
    $model.model_identifier += '_lockless'
    $name = [IO.Path]::GetFileNameWithoutExtension($source) + '_lockless.bbmodel'
    Write-Json "$destination/$name" $model
    Write-Output "$name : removed $($removed.Count) lock parts; retained $($model.elements.Count) parts"
}
Copy-Item -LiteralPath "$root/exports/aurorian_double_wood_chest/aurorian_chest.png" -Destination "$destination/aurorian_chest.png"

& C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "$PSScriptRoot/detail_lockless_chests.py"
foreach ($file in Get-ChildItem -LiteralPath $destination -Filter '*.bbmodel') {
    Write-Json $file.FullName (Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json)
}
