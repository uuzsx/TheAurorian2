param(
    [string]$OutputRoot = (Join-Path $PSScriptRoot '..\src\main\resources\assets\theaurorian2\textures\effect'),
    [string]$ItemOutputRoot = (Join-Path $PSScriptRoot '..\src\main\resources\assets\theaurorian2\textures\item'),
    [string]$InkHex = '#DCEEFF'
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

New-Item -ItemType Directory -Force -Path $OutputRoot | Out-Null
New-Item -ItemType Directory -Force -Path $ItemOutputRoot | Out-Null

$script:InkHex = $InkHex
$script:DarkestHex = '#02050A'
$script:DimHex = '#52708C'
$script:MidHex = '#7895AD'
$script:HighlightHex = '#EAF5FF'

function Get-Color([string]$hex, [int]$alpha) {
    $rgb = [Convert]::ToInt32($hex.TrimStart('#'), 16)
    return [System.Drawing.Color]::FromArgb(
        $alpha,
        ($rgb -shr 16) -band 255,
        ($rgb -shr 8) -band 255,
        $rgb -band 255)
}

function Get-Ink([int]$alpha) {
    return Get-Color $script:InkHex $alpha
}

function New-Canvas([int]$size) {
    $bitmap = [System.Drawing.Bitmap]::new($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $graphics.Clear([System.Drawing.Color]::Transparent)
    return @{ Bitmap = $bitmap; Graphics = $graphics; Scale = $size / 1024.0; Center = $size / 2.0 }
}

function Get-Point([float]$cx, [float]$cy, [float]$radius, [float]$angle, [float]$scale) {
    $radians = $angle * [Math]::PI / 180.0
    return [System.Drawing.PointF]::new(
        $cx + [Math]::Cos($radians) * $radius * $scale,
        $cy + [Math]::Sin($radians) * $radius * $scale)
}

function Fill-Circle(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$scale,
        [string]$hex,
        [int]$alpha) {
    $diameter = $radius * 2 * $scale
    $brush = [System.Drawing.SolidBrush]::new((Get-Color $hex $alpha))
    $graphics.FillEllipse($brush, $cx - $radius * $scale, $cy - $radius * $scale, $diameter, $diameter)
    $brush.Dispose()
}

function Draw-RawLine(
        $graphics,
        [System.Drawing.PointF]$from,
        [System.Drawing.PointF]$to,
        [float]$scale,
        [float]$width,
        [string]$hex,
        [int]$alpha) {
    $pen = [System.Drawing.Pen]::new((Get-Color $hex $alpha), $width * $scale)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $graphics.DrawLine($pen, $from, $to)
    $pen.Dispose()
}

function Draw-Stroke(
        $graphics,
        [System.Drawing.PointF[]]$points,
        [float]$scale,
        [float]$width,
        [int]$alpha = 235) {
    $pen = [System.Drawing.Pen]::new((Get-Ink $alpha), $width * $scale)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $graphics.DrawLines($pen, $points)
    $pen.Dispose()
}

function Draw-PathStroke(
        $graphics,
        [System.Drawing.Drawing2D.GraphicsPath]$path,
        [float]$scale,
        [float]$width,
        [int]$alpha = 235) {
    $pen = [System.Drawing.Pen]::new((Get-Ink $alpha), $width * $scale)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $graphics.DrawPath($pen, $path)
    $pen.Dispose()
}

function Draw-Arc(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$start,
        [float]$sweep,
        [float]$scale,
        [float]$width,
        [int]$alpha = 235) {
    $segments = [Math]::Max(8, [Math]::Ceiling([Math]::Abs($sweep) / 3.0))
    $points = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
    for ($index = 0; $index -le $segments; $index++) {
        $angle = $start + $sweep * $index / $segments
        $points.Add((Get-Point $cx $cy $radius $angle $scale))
    }
    Draw-Stroke $graphics $points.ToArray() $scale $width $alpha
}

function Draw-Circle(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$scale,
        [float]$width,
        [int]$alpha = 235) {
    Draw-Arc $graphics $cx $cy $radius 0 360 $scale $width $alpha
}

function Draw-RoughCircle(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$roughness,
        [float]$seed,
        [float]$scale,
        [float]$width,
        [int]$alpha = 210) {
    $segments = 180
    $points = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
    for ($index = 0; $index -lt $segments; $index++) {
        $angle = $index * 360.0 / $segments
        $jitter = [Math]::Sin(($index + $seed) * 0.71) * $roughness * 0.62 +
                [Math]::Sin(($index + $seed * 1.9) * 1.37) * $roughness * 0.38
        $points.Add((Get-Point $cx $cy ($radius + $jitter) $angle $scale))
    }
    $points.Add($points[0])
    Draw-Stroke $graphics $points.ToArray() $scale $width $alpha
}

function Draw-Line(
        $graphics,
        [System.Drawing.PointF]$from,
        [System.Drawing.PointF]$to,
        [float]$scale,
        [float]$width,
        [int]$alpha = 235) {
    Draw-Stroke $graphics ([System.Drawing.PointF[]]@($from, $to)) $scale $width $alpha
}

function Draw-Polygon(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [int]$sides,
        [float]$rotation,
        [float]$scale,
        [float]$width,
        [int]$alpha = 225) {
    $points = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
    for ($index = 0; $index -lt $sides; $index++) {
        $points.Add((Get-Point $cx $cy $radius ($rotation + $index * 360.0 / $sides) $scale))
    }
    $points.Add($points[0])
    Draw-Stroke $graphics $points.ToArray() $scale $width $alpha
}

function Draw-Hexagram(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$rotation,
        [float]$scale,
        [float]$width,
        [int]$alpha = 230) {
    Draw-Polygon $graphics $cx $cy $radius 3 $rotation $scale $width $alpha
    Draw-Polygon $graphics $cx $cy $radius 3 ($rotation + 60) $scale $width $alpha
}

function Draw-LocalStroke(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$angle,
        [float]$scale,
        [float]$glyphScale,
        [float]$width,
        [float[]]$coordinates,
        [int]$alpha = 235) {
    $radians = $angle * [Math]::PI / 180.0
    $tangentX = -[Math]::Sin($radians)
    $tangentY = [Math]::Cos($radians)
    $radialX = [Math]::Cos($radians)
    $radialY = [Math]::Sin($radians)
    $points = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
    for ($index = 0; $index -lt $coordinates.Count; $index += 2) {
        $x = $coordinates[$index] * $glyphScale
        $y = $coordinates[$index + 1] * $glyphScale
        $points.Add([System.Drawing.PointF]::new(
            $cx + ($radialX * ($radius + $y) + $tangentX * $x) * $scale,
            $cy + ($radialY * ($radius + $y) + $tangentY * $x) * $scale))
    }
    Draw-Stroke $graphics $points.ToArray() $scale $width $alpha
}

function Draw-LunarRune(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$angle,
        [int]$variant,
        [float]$scale,
        [float]$glyphScale = 1.0,
        [int]$alpha = 235) {
    switch ($variant % 12) {
        0 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.5 @(-13, 13, 0, -15, 12, 11) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.4 @(-8, 3, 9, 3) $alpha
        }
        1 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.0 @(-13, 10, -4, -11, 7, -3, 12, -14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.3 @(-5, -2, 9, 12) $alpha
        }
        2 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.1 @(-11, -2, 0, -15, 11, -2, 0, 9, -11, -2) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.4 @(0, 9, 0, 16) $alpha
        }
        3 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.2 @(-10, -12, -15, -2, -9, 11, 3, 14, 11, 7) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.3 @(-4, -5, 9, -8) $alpha
        }
        4 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.5 @(-13, 11, -13, -3, -6, -13, 7, -13, 13, -4, 13, 11) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.4 @(0, -10, 0, 15) $alpha
        }
        5 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.8 @(-13, -12, -3, -3, -10, 5, 1, 13, 12, 2) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.3 @(-8, -1, 9, -7) $alpha
        }
        6 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.2 @(-14, 0, 0, -12, 14, 0, 0, 12, -14, 0) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.3 @(-5, 7, 7, -8) $alpha
        }
        7 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.2 @(0, 15, 0, -7, -11, -14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.6 @(0, -7, 11, -14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.2 @(-9, 4, 8, 4) $alpha
        }
        8 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.8 @(-12, -14, -12, 13, 12, 13, 12, -14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.2 @(-12, -5, 4, -5, 4, 5, 12, 5) $alpha
        }
        9 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.8 @(-13, 9, -4, 14, 8, 9, 12, -1, 5, -9, -5, -7, -8, 1, -2, 6, 6, 4) $alpha
        }
        10 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 2.2 @(-12, -14, 12, -14, -8, 13, 11, 13, -12, -14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.4 @(-5, 0, 5, 0) $alpha
        }
        11 {
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 3.3 @(-13, 14, -13, -8, -5, -15, 8, -11, 13, -2, 13, 14) $alpha
            Draw-LocalStroke $graphics $cx $cy $radius $angle $scale $glyphScale 1.3 @(-7, 5, 7, 5) $alpha
        }
    }
}

function Draw-RuneBand(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [int]$count,
        [float]$rotation,
        [float]$scale,
        [float]$glyphScale,
        [int]$alpha,
        [int]$sequenceOffset) {
    $jitter = @(-1.4, 0.5, 1.1, -0.7, 1.5, -0.3, 0.8, -1.0)
    for ($index = 0; $index -lt $count; $index++) {
        $angle = $rotation + $index * 360.0 / $count + $jitter[$index % $jitter.Count]
        $variant = ($index * 5 + $sequenceOffset + [Math]::Floor($index / 7)) % 12
        $size = $glyphScale * $(if (($index % 9) -eq 0) { 1.16 } elseif (($index % 4) -eq 0) { 0.88 } else { 1.0 })
        Draw-LunarRune $graphics $cx $cy $radius $angle $variant $scale $size $alpha
    }
}

function Draw-NodeFrame(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$radius,
        [float]$scale,
        [int]$variant) {
    Draw-Circle $graphics $cx $cy $radius $scale 5.0 246
    Draw-Circle $graphics $cx $cy ($radius - 8) $scale 1.8 205
    Draw-Circle $graphics $cx $cy ($radius - 22) $scale 2.2 221
    for ($index = 0; $index -lt 10; $index++) {
        Draw-LunarRune $graphics $cx $cy ($radius - 15) ($index * 36 + 4) (($index * 3 + $variant) % 12) $scale 0.24 178
    }
}

function Draw-PetalRosette(
        $graphics,
        [float]$cx,
        [float]$cy,
        [int]$petalCount,
        [float]$radius,
        [float]$rotation,
        [float]$scale,
        [int]$alpha) {
    for ($index = 0; $index -lt $petalCount; $index++) {
        $angle = $rotation + $index * 360.0 / $petalCount
        $start = Get-Point $cx $cy 8 $angle $scale
        $tip = Get-Point $cx $cy $radius ($angle + 17) $scale
        $controlA = Get-Point $cx $cy ($radius * 0.54) ($angle - 15) $scale
        $controlB = Get-Point $cx $cy ($radius * 0.90) ($angle + 2) $scale
        $controlC = Get-Point $cx $cy ($radius * 0.72) ($angle + 31) $scale
        $controlD = Get-Point $cx $cy ($radius * 0.32) ($angle + 38) $scale
        $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
        $path.StartFigure()
        $path.AddBezier($start, $controlA, $controlB, $tip)
        $path.AddBezier($tip, $controlC, $controlD, $start)
        $path.CloseFigure()
        $brush = [System.Drawing.SolidBrush]::new((Get-Ink 72))
        $graphics.FillPath($brush, $path)
        $brush.Dispose()
        Draw-PathStroke $graphics $path $scale 1.5 $alpha
        $path.Dispose()
    }
}

function Draw-FilledTangentCrescent(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$outerRadius,
        [float]$innerRadius,
        [float]$angle,
        [float]$scale,
        [int]$alpha) {
    $offset = $outerRadius - $innerRadius
    $innerCenter = Get-Point $cx $cy $offset $angle $scale
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new([System.Drawing.Drawing2D.FillMode]::Alternate)
    $outerDiameter = $outerRadius * 2 * $scale
    $innerDiameter = $innerRadius * 2 * $scale
    $path.AddEllipse(
        $cx - $outerRadius * $scale,
        $cy - $outerRadius * $scale,
        $outerDiameter,
        $outerDiameter)
    $path.AddEllipse(
        $innerCenter.X - $innerRadius * $scale,
        $innerCenter.Y - $innerRadius * $scale,
        $innerDiameter,
        $innerDiameter)
    $brush = [System.Drawing.SolidBrush]::new((Get-Ink $alpha))
    $graphics.FillPath($brush, $path)
    $brush.Dispose()
    Draw-PathStroke $graphics $path $scale 1.2 226
    $path.Dispose()
}

function Draw-SagittariusRune(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$size,
        [float]$scale,
        [int]$alpha = 240) {
    $mainFrom = [System.Drawing.PointF]::new($cx - $size * 0.42 * $scale, $cy + $size * 0.42 * $scale)
    $mainTo = [System.Drawing.PointF]::new($cx + $size * 0.38 * $scale, $cy - $size * 0.38 * $scale)
    Draw-Line $graphics $mainFrom $mainTo $scale 2.2 $alpha
    Draw-Line $graphics ([System.Drawing.PointF]::new($cx + $size * 0.08 * $scale, $cy - $size * 0.38 * $scale)) $mainTo $scale 2.2 $alpha
    Draw-Line $graphics $mainTo ([System.Drawing.PointF]::new($cx + $size * 0.38 * $scale, $cy - $size * 0.08 * $scale)) $scale 2.2 $alpha
    Draw-Line $graphics ([System.Drawing.PointF]::new($cx - $size * 0.18 * $scale, $cy - $size * 0.04 * $scale)) ([System.Drawing.PointF]::new($cx + $size * 0.04 * $scale, $cy + $size * 0.18 * $scale)) $scale 2.0 $alpha
}

function Draw-LunarAstrolabeNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    Draw-Circle $graphics $cx $cy 32 $scale 2.8 238
    Draw-Arc $graphics $cx $cy 27 -72 252 $scale 2.6 226
    Draw-Arc $graphics $cx $cy 21 112 218 $scale 1.4 174
    $stars = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
    foreach ($angle in @(-90, 30, 150)) {
        $star = Get-Point $cx $cy 23 $angle $scale
        $stars.Add($star)
        Draw-Circle $graphics $star.X $star.Y 4 $scale 1.8 224
    }
    for ($index = 0; $index -lt $stars.Count; $index++) {
        Draw-Line $graphics $stars[$index] $stars[($index + 1) % $stars.Count] $scale 1.1 142
    }
    for ($index = 0; $index -lt 6; $index++) {
        $angle = $index * 60 - 90
        Draw-Line $graphics (Get-Point $cx $cy 10 $angle $scale) (Get-Point $cx $cy 17 $angle $scale) $scale 1.2 176
    }
    Draw-Circle $graphics $cx $cy 9 $scale 2.2 234
    Fill-Circle $graphics $cx $cy 3 $scale $script:InkHex 242
}

function Draw-GearNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    Draw-Circle $graphics $cx $cy 29 $scale 3.0 236
    Draw-Circle $graphics $cx $cy 12 $scale 2.5 236
    for ($index = 0; $index -lt 12; $index++) {
        $angle = $index * 30
        Draw-Line $graphics (Get-Point $cx $cy 29 $angle $scale) (Get-Point $cx $cy 36 $angle $scale) $scale 2.6 230
    }
    foreach ($angle in @(0, 90)) {
        Draw-Line $graphics (Get-Point $cx $cy 13 $angle $scale) (Get-Point $cx $cy 29 $angle $scale) $scale 4.2 245
        Draw-Line $graphics (Get-Point $cx $cy 13 ($angle + 180) $scale) (Get-Point $cx $cy 29 ($angle + 180) $scale) $scale 4.2 245
    }
    Fill-Circle $graphics $cx $cy 5 $scale $script:InkHex 246
}

function Draw-FlowerNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    Draw-PetalRosette $graphics $cx $cy 8 36 -90 $scale 224
    Draw-Circle $graphics $cx $cy 14 $scale 2.4 238
    for ($index = 0; $index -lt 8; $index++) {
        $point = Get-Point $cx $cy 33 ($index * 45 - 90) $scale
        Draw-Circle $graphics $point.X $point.Y 3.2 $scale 1.6 215
    }
    Fill-Circle $graphics $cx $cy 5 $scale $script:InkHex 242
}

function Draw-EnergyNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    for ($index = 0; $index -lt 3; $index++) {
        $point = Get-Point $cx $cy 10 ($index * 120 - 90) $scale
        Draw-Circle $graphics $point.X $point.Y 15 $scale 2.3 236
    }
    Draw-Polygon $graphics $cx $cy 34 6 -90 $scale 1.8 195
    Draw-SagittariusRune $graphics $cx $cy 20 $scale 238
}

function Draw-SunNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    Draw-PetalRosette $graphics $cx $cy 12 36 -100 $scale 196
    Draw-Circle $graphics $cx $cy 28 $scale 2.2 216
    Draw-Arc $graphics $cx $cy 18 -35 286 $scale 3.4 242
    Draw-Arc $graphics $cx $cy 10 30 250 $scale 2.3 222
    Fill-Circle $graphics $cx $cy 4 $scale $script:InkHex 242
}

function Draw-TopNode($graphics, [float]$cx, [float]$cy, [float]$scale) {
    Draw-PetalRosette $graphics $cx $cy 6 38 -105 $scale 232
    Draw-Circle $graphics $cx $cy 17 $scale 2.2 218
    Draw-Circle $graphics $cx $cy 7 $scale 2.7 245
}

function Draw-MagicNode(
        $graphics,
        [float]$cx,
        [float]$cy,
        [int]$variant,
        [float]$scale) {
    Draw-NodeFrame $graphics $cx $cy 58 $scale $variant
    switch ($variant) {
        0 { Draw-TopNode $graphics $cx $cy $scale }
        1 { Draw-GearNode $graphics $cx $cy $scale }
        2 { Draw-FlowerNode $graphics $cx $cy $scale }
        3 { Draw-EnergyNode $graphics $cx $cy $scale }
        4 { Draw-SunNode $graphics $cx $cy $scale }
        5 { Draw-LunarAstrolabeNode $graphics $cx $cy $scale }
    }
}

function Draw-OuterLayer($canvas) {
    $g = $canvas.Graphics; $s = $canvas.Scale; $c = $canvas.Center

    Draw-Circle $g $c $c 506 $s 2.2 182
    Draw-Circle $g $c $c 497 $s 5.8 248
    Draw-RoughCircle $g $c $c 486 1.1 11 $s 1.2 142
    Draw-Circle $g $c $c 478 $s 2.0 216
    Draw-Circle $g $c $c 451 $s 3.6 239
    Draw-RoughCircle $g $c $c 440 0.9 29 $s 1.2 154
    Draw-Circle $g $c $c 421 $s 2.0 205
    Draw-Circle $g $c $c 402 $s 4.2 234

    Draw-RuneBand $g $c $c 464 38 -2.0 $s 0.74 235 3
    Draw-RuneBand $g $c $c 431 68 1.0 $s 0.36 168 8

    foreach ($highlight in @(
            @(497, 208, 55, 7.2, 92),
            @(451, 28, 42, 5.0, 86),
            @(402, 300, 36, 5.0, 80))) {
        Draw-Arc $g $c $c $highlight[0] $highlight[1] $highlight[2] $s $highlight[3] $highlight[4]
    }

    for ($index = 0; $index -lt 12; $index++) {
        $angle = $index * 30 - 90
        $point = Get-Point $c $c 392 $angle $s
        Draw-Circle $g $point.X $point.Y 7 $s 2.2 218
        Draw-LunarRune $g $c $c 392 $angle (($index * 7 + 2) % 12) $s 0.28 227
    }
}

function Draw-CircuitLayer($canvas) {
    $g = $canvas.Graphics; $s = $canvas.Scale; $c = $canvas.Center

    Draw-Circle $g $c $c 389 $s 1.8 174
    Draw-Polygon $g $c $c 357 6 -90 $s 4.0 234
    Draw-Polygon $g $c $c 344 6 -90 $s 1.6 176
    Draw-Polygon $g $c $c 305 6 -90 $s 3.0 216
    Draw-Polygon $g $c $c 286 6 -60 $s 1.5 162

    for ($index = 0; $index -lt 6; $index++) {
        $angle = -90 + $index * 60
        foreach ($offset in @(-4.3, 4.3)) {
            $points = [System.Collections.Generic.List[System.Drawing.PointF]]::new()
            $points.Add((Get-Point $c $c 238 ($angle + $offset * 0.42) $s))
            $points.Add((Get-Point $c $c 264 ($angle + $offset * 0.42) $s))
            $points.Add((Get-Point $c $c 282 ($angle + $offset) $s))
            $points.Add((Get-Point $c $c 323 ($angle + $offset) $s))
            $points.Add((Get-Point $c $c 374 ($angle + $offset * 0.52) $s))
            Draw-Stroke $g $points.ToArray() $s $(if ($offset -lt 0) { 2.6 } else { 1.5 }) $(if ($offset -lt 0) { 222 } else { 170 })
        }

        for ($bar = -3; $bar -le 3; $bar++) {
            $barRadius = 284 + $bar * 6
            $from = Get-Point $c $c $barRadius ($angle - 2.1) $s
            $to = Get-Point $c $c $barRadius ($angle + 2.1) $s
            Draw-Line $g $from $to $s 1.8 $(if (($bar % 2) -eq 0) { 218 } else { 154 })
        }

        $junction = Get-Point $c $c 373 ($angle + 30) $s
        Draw-Circle $g $junction.X $junction.Y 8 $s 2.2 218
        Draw-Circle $g $junction.X $junction.Y 3 $s 1.4 175
    }

    for ($index = 0; $index -lt 6; $index++) {
        $angleA = -90 + $index * 60 + 9
        $angleB = -90 + ($index + 1) * 60 - 9
        $points = [System.Drawing.PointF[]]@(
            (Get-Point $c $c 329 $angleA $s),
            (Get-Point $c $c 369 ($angleA + 5) $s),
            (Get-Point $c $c 369 ($angleB - 5) $s),
            (Get-Point $c $c 329 $angleB $s))
        Draw-Stroke $g $points $s 2.0 192
    }
}

function Draw-NodesLayer($canvas) {
    $g = $canvas.Graphics; $s = $canvas.Scale; $c = $canvas.Center
    for ($index = 0; $index -lt 6; $index++) {
        $angle = -90 + $index * 60
        $point = Get-Point $c $c 325 $angle $s
        Draw-MagicNode $g $point.X $point.Y $index $s
    }
}

function Draw-DiscOrnament(
        $graphics,
        [float]$cx,
        [float]$cy,
        [float]$angle,
        [float]$scale,
        [int]$variant) {
    $center = Get-Point $cx $cy 177 $angle $scale
    Draw-Polygon $graphics $center.X $center.Y 16 $(if (($variant % 2) -eq 0) { 3 } else { 4 }) ($angle - 90) $scale 1.4 148
    Draw-LunarRune $graphics $cx $cy 176 $angle (($variant * 5 + 1) % 12) $scale 0.42 174
    $arcStart = $angle - 18
    Draw-Arc $graphics $cx $cy 194 $arcStart 24 $scale 1.1 126
    Draw-Arc $graphics $cx $cy 184 ($arcStart + 5) 18 $scale 0.9 108
}

function Draw-GeometryLayer($canvas) {
    $g = $canvas.Graphics; $s = $canvas.Scale; $c = $canvas.Center

    Draw-Circle $g $c $c 254 $s 5.3 242
    Draw-RoughCircle $g $c $c 243 1.0 43 $s 1.4 168
    Draw-Circle $g $c $c 232 $s 2.0 210
    Draw-Circle $g $c $c 216 $s 1.4 156

    for ($index = 0; $index -lt 6; $index++) {
        $angle = -90 + $index * 60
        Draw-Line $g (Get-Point $c $c 111 $angle $s) (Get-Point $c $c 211 $angle $s) $s 1.4 137
        Draw-DiscOrnament $g $c $c ($angle + 30) $s $index
    }

    Draw-Hexagram $g $c $c 211 -90 $s 5.0 245
    Draw-Hexagram $g $c $c 194 -90 $s 1.8 188
    Draw-Hexagram $g $c $c 179 -60 $s 2.3 214
    Draw-Polygon $g $c $c 163 6 -90 $s 1.6 174
    Draw-Polygon $g $c $c 145 3 -90 $s 3.1 225
    Draw-Polygon $g $c $c 145 3 -30 $s 2.1 190

    for ($index = 0; $index -lt 6; $index++) {
        $angle = -90 + $index * 60
        $outer = Get-Point $c $c 211 $angle $s
        $innerA = Get-Point $c $c 130 ($angle + 18) $s
        $innerB = Get-Point $c $c 130 ($angle - 18) $s
        Draw-Line $g $outer $innerA $s 1.2 138
        Draw-Line $g $outer $innerB $s 1.2 138
    }
}

function Draw-CoreLayer($canvas) {
    $g = $canvas.Graphics; $s = $canvas.Scale; $c = $canvas.Center

    Draw-Circle $g $c $c 117 $s 5.0 250
    Draw-Circle $g $c $c 105 $s 1.8 205
    Draw-FilledTangentCrescent $g $c $c 86 62 0 $s 104
}

function Save-Layer([string]$name, [scriptblock]$draw, [int]$size = 2048) {
    $canvas = New-Canvas $size
    & $draw $canvas
    $path = Join-Path $OutputRoot $name
    $canvas.Bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Graphics.Dispose()
    $canvas.Bitmap.Dispose()
}

Save-Layer 'arcane_magic_circle_outer.png' { param($canvas) Draw-OuterLayer $canvas }
Save-Layer 'arcane_magic_circle_network.png' { param($canvas) Draw-CircuitLayer $canvas }
Save-Layer 'arcane_magic_circle_nodes.png' { param($canvas) Draw-NodesLayer $canvas }
Save-Layer 'arcane_magic_circle_inner.png' { param($canvas) Draw-GeometryLayer $canvas }
Save-Layer 'arcane_magic_circle_core.png' { param($canvas) Draw-CoreLayer $canvas }

$icon = New-Canvas 1024
Draw-OuterLayer $icon
Draw-CircuitLayer $icon
Draw-NodesLayer $icon
Draw-GeometryLayer $icon
Draw-CoreLayer $icon
$icon.Bitmap.Save((Join-Path $ItemOutputRoot 'arcane_magic_circle.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$icon.Graphics.Dispose()
$icon.Bitmap.Dispose()

Write-Output "Generated layered ancient lunar magic circle textures in $OutputRoot"
