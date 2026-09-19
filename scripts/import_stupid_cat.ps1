param(
    [string]$Source = 'exports/skin_doll/final/stupid_cat.bbmodel',
    [string]$Id = 'stupid_cat',
    [string]$ChineseName = '笨蛋锚猫',
    [string]$EnglishName = 'Stupid Cat'
)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
python "$PSScriptRoot/import_stupid_cat.py" --source $Source --id $Id --zh $ChineseName --en $EnglishName
if ($LASTEXITCODE -ne 0) { throw 'Doll export failed' }
$root = Split-Path $PSScriptRoot
$resources = Get-Content "$root/build/stupid-cat-resources.json" -Raw -Encoding utf8 | ConvertFrom-Json
foreach ($entry in $resources.PSObject.Properties) {
    Write-Json (Join-Path $root $entry.Name) $entry.Value
}
