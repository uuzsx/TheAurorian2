import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {readNbt} from './migrate_structure_nbt.mjs';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const legacyRoot = 'D:/TheAurorian';
const sourceRoot = path.join(projectRoot, 'src/main');
const assetsRoot = path.join(sourceRoot, 'resources/assets/theaurorian2');
const dataRoot = path.join(sourceRoot, 'resources/data/theaurorian2');

const structureGroups = [
  ['moon_palace', 'moon_palace'],
  ['runestone_dungeon', 'runestone_dungeon'],
  ['worldtree', 'worldtree'],
  ['village', 'village/set'],
  ['ruins_altar', 'ruins/ruins_altar'],
];

const explicitMappings = new Map([
  ['silent_wood_chest', 'aurorian_chest'],
  ['moon_water', 'moon_dew'],
  ['snow_aurorian_grass_block', 'aurorian_grass_block'],
]);
const deferredAlchemyBlocks = new Set([
  'alchemy_table',
]);

function filesRecursively(root, predicate = () => true) {
  if (!fs.existsSync(root)) return [];
  const files = [];
  for (const entry of fs.readdirSync(root, {withFileTypes: true})) {
    const absolute = path.join(root, entry.name);
    if (entry.isDirectory()) files.push(...filesRecursively(absolute, predicate));
    else if (predicate(absolute)) files.push(absolute);
  }
  return files;
}

function paletteNames(root) {
  const names = new Set();
  const visitPaletteEntry = entry => {
    const name = entry?.Name;
    if (name?.type === 8) names.add(name.value);
  };
  const palette = root.value.palette;
  if (palette?.type === 9) palette.value.value.forEach(visitPaletteEntry);
  const palettes = root.value.palettes;
  if (palettes?.type === 9) {
    for (const nested of palettes.value.value) {
      if (nested?.itemType === 10 && Array.isArray(nested.value)) {
        nested.value.forEach(visitPaletteEntry);
      }
    }
  }
  return names;
}

function parsePowerShellArray(source, variable) {
  const match = source.match(new RegExp(`\\$${variable}\\s*=\\s*@\\(([\\s\\S]*?)\\r?\\n\\)`));
  if (!match) throw new Error(`Cannot find PowerShell array $${variable}`);
  return [...match[1].matchAll(/'([^']+)'/g)].map(value => value[1]);
}

function collectRegisteredBlockIds() {
  const registryFiles = [
    path.join(sourceRoot, 'java/cn/teampancake/theaurorian2/common/registry/ModBlocks.java'),
    path.join(sourceRoot, 'java/cn/teampancake/theaurorian2/common/registry/ModStructureBlocks.java'),
  ];
  const ids = new Set();
  for (const file of registryFiles) {
    const source = fs.readFileSync(file, 'utf8');
    const declarations = source.matchAll(/public static final DeferredBlock<[^;]+?;/gs);
    for (const declaration of declarations) {
      const id = declaration[0].match(/"([a-z0-9_]+)"/);
      if (id) ids.add(id[1]);
    }
    for (const woodSet of source.matchAll(/woodSet\(\s*"([a-z0-9_]+)"\s*,\s*"([a-z0-9_]+)"/g)) {
      const [, treeName, woodName] = woodSet;
      [
        `stripped_${treeName}_log`, `${treeName}_wood`, `stripped_${treeName}_wood`, `${treeName}_planks`,
        `${woodName}_stairs`, `${woodName}_slab`, `${woodName}_fence`, `${woodName}_fence_gate`,
        `${woodName}_door`, `${woodName}_trapdoor`, `${woodName}_pressure_plate`, `${woodName}_button`,
      ].forEach(id => ids.add(id));
    }
  }
  return ids;
}

function collectLegacyCatalogIds() {
  const registry = fs.readFileSync(
    path.join(sourceRoot, 'java/cn/teampancake/theaurorian2/common/registry/ModStructureBlocks.java'),
    'utf8',
  );
  const ids = [];
  const lists = registry.matchAll(
    /private static final List<String> LEGACY_[A-Z0-9_]+_IDS\s*=\s*List\.of\((.*?)\);/gs,
  );
  for (const list of lists) {
    for (const id of list[1].matchAll(/"([a-z0-9_]+)"/g)) ids.push(id[1]);
  }
  return ids;
}

function collectResourceReferences(value, references = {models: [], textures: []}, context = '') {
  if (Array.isArray(value)) {
    value.forEach(entry => collectResourceReferences(entry, references, context));
  } else if (value && typeof value === 'object') {
    for (const [key, entry] of Object.entries(value)) {
      if ((key === 'model' || key === 'parent') && typeof entry === 'string') references.models.push(entry);
      else if (context === 'textures' && typeof entry === 'string') references.textures.push(entry);
      else collectResourceReferences(entry, references, key);
    }
  }
  return references;
}

const failures = [];
const warnings = [];
const requireFile = (file, reason) => {
  if (!fs.existsSync(file)) failures.push(`${reason}: ${path.relative(projectRoot, file)}`);
};

const generator = fs.readFileSync(path.join(projectRoot, 'scripts/generate_legacy_structure_blocks.ps1'), 'utf8');
const legacyCatalogIds = collectLegacyCatalogIds();
const generatedIds = [...new Set([...parsePowerShellArray(generator, 'blockIds'), ...legacyCatalogIds])];
const hiddenIds = new Set(parsePowerShellArray(generator, 'hiddenBlockIds'));
const registeredIds = collectRegisteredBlockIds();
legacyCatalogIds.forEach(id => registeredIds.add(id));
const duplicateCatalogIds = legacyCatalogIds.filter((id, index) => legacyCatalogIds.indexOf(id) !== index);
if (duplicateCatalogIds.length) failures.push(`Duplicate legacy catalog IDs: ${[...new Set(duplicateCatalogIds)].join(', ')}`);
if (legacyCatalogIds.length !== 198) failures.push(`Expected 198 deferred legacy IDs, found ${legacyCatalogIds.length}`);

const legacyStructureRoot = path.join(legacyRoot, 'src/main/resources/data/theaurorian/structure');
const structures = [];
const customBlocks = new Map();
for (const [group, relative] of structureGroups) {
  const files = filesRecursively(path.join(legacyStructureRoot, relative), file => file.endsWith('.nbt'));
  for (const file of files) {
    const names = paletteNames(readNbt(file));
    structures.push({group, file, names});
    for (const name of names) {
      if (!name.startsWith('theaurorian:')) continue;
      const id = name.slice('theaurorian:'.length);
      if (!customBlocks.has(id)) customBlocks.set(id, new Set());
      customBlocks.get(id).add(path.relative(legacyStructureRoot, file).replaceAll('\\', '/'));
    }
  }
}

for (const [oldId, structureFiles] of customBlocks) {
  const currentId = explicitMappings.get(oldId) ?? oldId;
  if (deferredAlchemyBlocks.has(oldId)) continue;
  if (!registeredIds.has(currentId)) {
    failures.push(`Unmapped legacy block theaurorian:${oldId} in ${[...structureFiles].join(', ')}`);
  }
}

const legacyBlockstateRoots = [
  path.join(legacyRoot, 'src/generated/resources/assets/theaurorian/blockstates'),
  path.join(legacyRoot, 'src/main/resources/assets/theaurorian/blockstates'),
];
const allLegacyBlockIds = new Set(legacyBlockstateRoots.flatMap(root =>
  filesRecursively(root, file => file.endsWith('.json')).map(file => path.basename(file, '.json')),
));
for (const oldId of allLegacyBlockIds) {
  if (deferredAlchemyBlocks.has(oldId)) continue;
  const currentId = explicitMappings.get(oldId) ?? oldId;
  if (!registeredIds.has(currentId)) failures.push(`Legacy block is not registered or mapped: ${oldId}`);
}

for (const id of generatedIds) {
  if (!registeredIds.has(id)) failures.push(`Generated block is not registered: ${id}`);
  requireFile(path.join(assetsRoot, `blockstates/${id}.json`), `Missing blockstate for ${id}`);
  requireFile(path.join(dataRoot, `loot_table/blocks/${id}.json`), `Missing loot table for ${id}`);
  if (!hiddenIds.has(id)) requireFile(path.join(assetsRoot, `items/${id}.json`), `Missing item definition for ${id}`);
}

for (const locale of ['zh_cn', 'en_us']) {
  const language = JSON.parse(fs.readFileSync(path.join(assetsRoot, `lang/${locale}.json`), 'utf8').replace(/^\uFEFF/, ''));
  for (const id of generatedIds) {
    if (hiddenIds.has(id)) continue;
    if (!Object.hasOwn(language, `block.theaurorian2.${id}`)) {
      failures.push(`Missing ${locale} block name for ${id}`);
    }
  }
}

const jsonFiles = filesRecursively(assetsRoot, file => file.endsWith('.json') || file.endsWith('.mcmeta'));
for (const file of jsonFiles) {
  const source = fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '');
  if (source.includes('theaurorian:')) failures.push(`Legacy namespace remains in ${path.relative(projectRoot, file)}`);
  let value;
  try {
    value = JSON.parse(source);
  } catch (error) {
    failures.push(`Invalid JSON ${path.relative(projectRoot, file)}: ${error.message}`);
    continue;
  }
  const references = collectResourceReferences(value);
  for (const resource of references.models) {
    const modelMatch = resource.match(/^theaurorian2:(block|item)\/([a-z0-9_./-]+)$/);
    if (modelMatch) {
      requireFile(
        path.join(assetsRoot, `models/${modelMatch[1]}/${modelMatch[2]}.json`),
        `Missing referenced model ${resource} from ${path.relative(projectRoot, file)}`,
      );
    }
  }
  for (const resource of references.textures) {
    const textureMatch = resource.match(/^theaurorian2:(block|item)\/([a-z0-9_./-]+)$/);
    if (textureMatch) {
      requireFile(
        path.join(assetsRoot, `textures/${textureMatch[1]}/${textureMatch[2]}.png`),
        `Missing referenced texture ${resource} from ${path.relative(projectRoot, file)}`,
      );
    }
  }
}

const expectedCounts = new Map([
  ['moon_palace', 9], ['runestone_dungeon', 9], ['worldtree', 9], ['village', 9], ['ruins_altar', 1],
]);
for (const [group, count] of expectedCounts) {
  const actual = structures.filter(structure => structure.group === group).length;
  if (actual !== count) failures.push(`Expected ${count} ${group} structures, found ${actual}`);
}

const deferredFound = [...customBlocks.keys()].filter(id => deferredAlchemyBlocks.has(id));
const mappedFound = [...explicitMappings].filter(([id]) => customBlocks.has(id));
console.log(`Audited ${structures.length} legacy structures.`);
console.log(`Found ${customBlocks.size} unique legacy Aurorian block IDs.`);
console.log(`Validated ${generatedIds.length} generated structure block registrations.`);
console.log(`Explicit mappings: ${mappedFound.map(([from, to]) => `${from} -> ${to}`).join(', ') || 'none'}.`);
console.log(`Deferred alchemy blocks: ${deferredFound.join(', ') || 'none'}.`);
if (warnings.length) warnings.forEach(warning => console.warn(`WARN: ${warning}`));
if (failures.length) {
  failures.forEach(failure => console.error(`ERROR: ${failure}`));
  process.exitCode = 1;
} else {
  console.log('Legacy structure block audit passed.');
}
