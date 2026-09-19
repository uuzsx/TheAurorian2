$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    python scripts/import_astrology_table.py
    if ($LASTEXITCODE -ne 0) { throw 'Astrology table model import failed.' }
    . ./scripts/json_utils.ps1
    $resources = Get-Content build/astrology-resources.json -Raw -Encoding utf8 | ConvertFrom-Json
    foreach ($entry in $resources.PSObject.Properties) {
        Write-Json $entry.Name $entry.Value
    }
} finally {
    Pop-Location
}
