param([string]$Python = 'C:/Users/84446/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/json_utils.ps1"
& $Python "$PSScriptRoot/export_wood_chests.py"
if ($LASTEXITCODE -ne 0) { throw 'Chest export failed.' }
$destination = Join-Path (Split-Path $PSScriptRoot) 'exports/aurorian_wood_chest_variants'
foreach ($file in Get-ChildItem -LiteralPath $destination -Recurse -File -Filter '*.bbmodel') {
    Write-Json $file.FullName (Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json)
}
foreach ($folder in Get-ChildItem -LiteralPath $destination -Directory) {
    Compress-Archive -Path "$($folder.FullName)/*" -DestinationPath "$destination/$($folder.Name)_chests.zip" -Force
}
$archiveInputs = @((Get-ChildItem -LiteralPath $destination -Directory).FullName) + @("$destination/overview.png", "$destination/README.txt")
Compress-Archive -Path $archiveInputs -DestinationPath "$destination/aurorian_wood_chest_variants.zip" -Force
