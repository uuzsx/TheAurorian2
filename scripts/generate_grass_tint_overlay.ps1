$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$textures = Join-Path $root 'src/main/resources/assets/theaurorian2/textures/block'
$brightnessScale = 1.30

function Write-TintMask {
    param(
        [string]$SourcePath,
        [string]$OutputPath,
        [ValidateSet('Luminance', 'Maximum')]
        [string]$Mode
    )

    $source = [System.Drawing.Bitmap]::new($SourcePath)
    $output = [System.Drawing.Bitmap]::new(
        $source.Width,
        $source.Height,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )

    try {
        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = 0; $x -lt $source.Width; $x++) {
                $color = $source.GetPixel($x, $y)
                $gray = if ($Mode -eq 'Maximum') {
                    [Math]::Max($color.R, [Math]::Max($color.G, $color.B))
                }
                else {
                    # Preserve perceived texture contrast while compensating for Minecraft's tint multiplication.
                    $luminance = 0.2126 * $color.R + 0.7152 * $color.G + 0.0722 * $color.B
                    [Math]::Min(255, [int][Math]::Round($luminance * $brightnessScale))
                }
                $output.SetPixel(
                    $x,
                    $y,
                    [System.Drawing.Color]::FromArgb($color.A, $gray, $gray, $gray)
                )
            }
        }

        $output.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $output.Dispose()
        $source.Dispose()
    }
}

function Get-MaximumMaskTint {
    param([string]$SourcePath)

    $source = [System.Drawing.Bitmap]::new($SourcePath)
    try {
        $sumGraySquared = 0.0
        $sumGrayRed = 0.0
        $sumGrayGreen = 0.0
        $sumGrayBlue = 0.0

        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = 0; $x -lt $source.Width; $x++) {
                $color = $source.GetPixel($x, $y)
                if ($color.A -eq 0) {
                    continue
                }

                $gray = [Math]::Max($color.R, [Math]::Max($color.G, $color.B))
                $sumGraySquared += $gray * $gray
                $sumGrayRed += $gray * $color.R
                $sumGrayGreen += $gray * $color.G
                $sumGrayBlue += $gray * $color.B
            }
        }

        if ($sumGraySquared -eq 0.0) {
            throw "Cannot extract a tint from an empty side texture"
        }

        return [System.Drawing.Color]::FromArgb(
            255,
            [Math]::Min(255, [int][Math]::Round(255 * $sumGrayRed / $sumGraySquared)),
            [Math]::Min(255, [int][Math]::Round(255 * $sumGrayGreen / $sumGraySquared)),
            [Math]::Min(255, [int][Math]::Round(255 * $sumGrayBlue / $sumGraySquared))
        )
    }
    finally {
        $source.Dispose()
    }
}

Write-TintMask `
    (Join-Path $textures 'aurorian_grass_block_top.png') `
    (Join-Path $textures 'aurorian_grass_block_top_overlay.png') `
    'Luminance'
$sideSourcePath = Join-Path $textures 'aurorian_grass_block.png'
Write-TintMask `
    $sideSourcePath `
    (Join-Path $textures 'aurorian_grass_block_side_overlay.png') `
    'Maximum'

$sideTint = Get-MaximumMaskTint $sideSourcePath
Write-Output ('Extracted side tint: #{0:X2}{1:X2}{2:X2}' -f $sideTint.R, $sideTint.G, $sideTint.B)
