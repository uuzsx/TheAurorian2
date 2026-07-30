function Write-Json($path, $value) {
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    $json = $value | ConvertTo-Json -Depth 50 -Compress
    try {
        $json | ConvertFrom-Json | Out-Null
    } catch {
        throw "Generated invalid JSON for '$path': $($_.Exception.Message)"
    }
    [System.IO.File]::WriteAllText(
        $path,
        $json + [Environment]::NewLine,
        [System.Text.UTF8Encoding]::new($false))
}
