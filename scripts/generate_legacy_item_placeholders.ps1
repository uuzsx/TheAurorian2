param(
    [Parameter(Mandatory = $true)]
    [string]$LegacyItemTextureRoot,

    [Parameter(Mandatory = $true)]
    [string]$OutputRoot
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot 'json_utils.ps1')

$manifestPath = Join-Path $PSScriptRoot 'legacy_item_manifest.json'
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$validCategories = @('equipment', 'food', 'functional', 'ingredients', 'tools')
$seenIds = @{}

foreach ($item in $manifest) {
    if ($item.id -notmatch '^[a-z0-9_]+$') {
        throw "Invalid registry id '$($item.id)'"
    }
    if ($seenIds.ContainsKey($item.id)) {
        throw "Duplicate registry id '$($item.id)'"
    }
    if ($item.category -notin $validCategories) {
        throw "Invalid category '$($item.category)' for '$($item.id)'"
    }
    if ($item.model -and $item.model -notin @('generated', 'handheld')) {
        throw "Invalid model '$($item.model)' for '$($item.id)'"
    }
    $seenIds[$item.id] = $true
}

$resourceRoot = Join-Path $OutputRoot 'resources'
$javaRoot = Join-Path $OutputRoot 'java'
$modelRoot = Join-Path $resourceRoot 'assets\theaurorian2\models\item'
$itemDefinitionRoot = Join-Path $resourceRoot 'assets\theaurorian2\items'
$textureRoot = Join-Path $resourceRoot 'assets\theaurorian2\textures\item'
New-Item -ItemType Directory -Force -Path $modelRoot, $itemDefinitionRoot, $textureRoot | Out-Null

foreach ($item in $manifest) {
    $sourceName = if ($item.source) { [string]$item.source } else { "$($item.id).png" }
    $sourcePath = Join-Path $LegacyItemTextureRoot $sourceName
    if (-not (Test-Path -LiteralPath $sourcePath)) {
        throw "Missing source texture '$sourceName' for '$($item.id)'"
    }

    $targetTexture = Join-Path $textureRoot "$($item.id).png"
    Copy-Item -LiteralPath $sourcePath -Destination $targetTexture -Force
    $sourceMetadata = "$sourcePath.mcmeta"
    if (Test-Path -LiteralPath $sourceMetadata) {
        Copy-Item -LiteralPath $sourceMetadata -Destination "$targetTexture.mcmeta" -Force
    }

    $modelParent = if ($item.model -eq 'handheld') {
        'minecraft:item/handheld'
    } else {
        'minecraft:item/generated'
    }
    Write-Json (Join-Path $modelRoot "$($item.id).json") ([ordered]@{
        parent = $modelParent
        textures = [ordered]@{layer0 = "theaurorian2:item/$($item.id)"}
    })
    Write-Json (Join-Path $itemDefinitionRoot "$($item.id).json") ([ordered]@{
        model = [ordered]@{
            type = 'minecraft:model'
            model = "theaurorian2:item/$($item.id)"
        }
    })
}

$languageRoot = Join-Path $resourceRoot 'assets\theaurorian2\lang'
$zh = Get-Content -LiteralPath (Join-Path $projectRoot 'src\main\resources\assets\theaurorian2\lang\zh_cn.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$en = Get-Content -LiteralPath (Join-Path $projectRoot 'src\main\resources\assets\theaurorian2\lang\en_us.json') -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($item in $manifest) {
    $key = "item.theaurorian2.$($item.id)"
    $zh | Add-Member -Force -NotePropertyName $key -NotePropertyValue $item.zh
    $en | Add-Member -Force -NotePropertyName $key -NotePropertyValue $item.en
}
Write-Json (Join-Path $languageRoot 'zh_cn.json') $zh
Write-Json (Join-Path $languageRoot 'en_us.json') $en

$javaPackageRoot = Join-Path $javaRoot 'cn\teampancake\theaurorian2\common\registry'
New-Item -ItemType Directory -Force -Path $javaPackageRoot | Out-Null
$java = [System.Text.StringBuilder]::new()
[void]$java.AppendLine('package cn.teampancake.theaurorian2.common.registry;')
[void]$java.AppendLine()
[void]$java.AppendLine('import java.util.ArrayList;')
[void]$java.AppendLine('import java.util.EnumMap;')
[void]$java.AppendLine('import java.util.List;')
[void]$java.AppendLine('import java.util.Map;')
[void]$java.AppendLine('import java.util.function.Consumer;')
[void]$java.AppendLine('import net.minecraft.world.item.Item;')
[void]$java.AppendLine('import net.neoforged.neoforge.registries.DeferredItem;')
[void]$java.AppendLine()
[void]$java.AppendLine('public final class ModLegacyItems {')
[void]$java.AppendLine()
[void]$java.AppendLine('    private static final Map<Category, List<DeferredItem<Item>>> ITEMS_BY_CATEGORY =')
[void]$java.AppendLine('            new EnumMap<>(Category.class);')
[void]$java.AppendLine()
[void]$java.AppendLine('    static {')
[void]$java.AppendLine('        for (Category category : Category.values()) {')
[void]$java.AppendLine('            ITEMS_BY_CATEGORY.put(category, new ArrayList<>());')
[void]$java.AppendLine('        }')
[void]$java.AppendLine('    }')
[void]$java.AppendLine()

foreach ($item in $manifest) {
    $constant = $item.id.ToUpperInvariant()
    $category = $item.category.ToUpperInvariant()
    $maxStack = if ($null -ne $item.stack) {
        [int]$item.stack
    } elseif ($item.category -eq 'ingredients') {
        64
    } else {
        1
    }
    [void]$java.AppendLine("    public static final DeferredItem<Item> $constant =")
    [void]$java.AppendLine("            register(`"$($item.id)`", Category.$category, $maxStack);")
}

[void]$java.AppendLine()
[void]$java.AppendLine('    private ModLegacyItems() {')
[void]$java.AppendLine('    }')
[void]$java.AppendLine()
[void]$java.AppendLine('    private static DeferredItem<Item> register(String id, Category category, int maxStack) {')
[void]$java.AppendLine('        DeferredItem<Item> item = ModItems.ITEMS.registerItem(')
[void]$java.AppendLine('                id, properties -> new Item(maxStack == 64 ? properties : properties.stacksTo(maxStack)));')
[void]$java.AppendLine('        ITEMS_BY_CATEGORY.get(category).add(item);')
[void]$java.AppendLine('        return item;')
[void]$java.AppendLine('    }')
[void]$java.AppendLine()
foreach ($category in $validCategories) {
    $methodName = $category.Substring(0, 1).ToUpperInvariant() + $category.Substring(1)
    $enumName = $category.ToUpperInvariant()
    [void]$java.AppendLine("    public static void forEach$methodName(Consumer<Item> consumer) {")
    [void]$java.AppendLine("        ITEMS_BY_CATEGORY.get(Category.$enumName).forEach(item -> consumer.accept(item.get()));")
    [void]$java.AppendLine('    }')
    [void]$java.AppendLine()
}
[void]$java.AppendLine('    public static void bootstrap() {')
[void]$java.AppendLine('    }')
[void]$java.AppendLine()
[void]$java.AppendLine('    private enum Category {')
[void]$java.AppendLine('        EQUIPMENT,')
[void]$java.AppendLine('        FOOD,')
[void]$java.AppendLine('        FUNCTIONAL,')
[void]$java.AppendLine('        INGREDIENTS,')
[void]$java.AppendLine('        TOOLS')
[void]$java.AppendLine('    }')
[void]$java.AppendLine('}')

[System.IO.File]::WriteAllText(
    (Join-Path $javaPackageRoot 'ModLegacyItems.java'),
    $java.ToString(),
    [System.Text.UTF8Encoding]::new($false))

Write-Output "Generated $($manifest.Count) legacy item placeholders in '$OutputRoot'."
