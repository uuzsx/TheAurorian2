$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

function Convert-MultiplierToColor {
    param(
        [double]$Temperature,
        [double]$Downfall
    )

    # Normal Aurorian climates use white so the legacy color artwork is unchanged.
    # Other climates only multiply it gently, preserving every source hue and highlight.
    $cold = [Math]::Min(1.0, [Math]::Max(0.0, (0.80 - $Temperature) / 0.20))
    $warm = [Math]::Min(1.0, [Math]::Max(0.0, ($Temperature - 0.80) / 0.20))
    $dry = [Math]::Max(0.0, (0.38 - $Downfall) / 0.38)
    $wet = [Math]::Max(0.0, ($Downfall - 0.42) / 0.58)

    # Cooler biomes keep the source blue channel while lowering red and green.
    # This preserves the original cyan details but remains visible in blue scenery.
    $red = [Math]::Max(0.0, 1.0 - 0.568 * $cold - 0.04 * $wet)
    $green = [Math]::Max(0.0, 1.0 - 0.344 * $cold - 0.02 * $warm - 0.02 * $dry)
    $blue = [Math]::Max(0.0, 1.0 - 0.08 * $warm - 0.04 * $dry)
    return [System.Drawing.Color]::FromArgb(
        255,
        [int][Math]::Round($red * 255.0),
        [int][Math]::Round($green * 255.0),
        [int][Math]::Round($blue * 255.0)
    )
}

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$outputDirectory = Join-Path $root 'src/main/resources/assets/theaurorian2/textures/colormap'
$outputPath = Join-Path $outputDirectory 'aurorian_grass.png'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$size = 256
$image = [System.Drawing.Bitmap]::new(
    $size,
    $size,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
)

try {
    for ($y = 0; $y -lt $size; $y++) {
        for ($x = 0; $x -lt $size; $x++) {
            # Minecraft only samples the half where effective rainfall cannot exceed temperature.
            if ($y -lt $x) {
                $image.SetPixel($x, $y, [System.Drawing.Color]::White)
                continue
            }

            $temperature = 1.0 - ($x / 255.0)
            $effectiveRainfall = 1.0 - ($y / 255.0)
            $downfall = if ($temperature -gt 0.0) {
                [Math]::Min(1.0, $effectiveRainfall / $temperature)
            }
            else {
                0.0
            }

            $image.SetPixel($x, $y, (Convert-MultiplierToColor $temperature $downfall))
        }
    }

    $image.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
}
finally {
    $image.Dispose()
}
