import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {readNbt, writeNbt} from './migrate_structure_nbt.mjs';

// The legacy jigsaw pool points part_8 back to itself instead of part_9.
// Runtime placement therefore rotates this fixed nine-part layout as one building.
const repository = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const source = process.argv[2] ??
  'D:/TheAurorian-NeoForge-1.21/TheAurorian-NeoForge-1.21/src/main/resources/data/theaurorian/structure/runestone_dungeon';
const destination = path.join(repository, 'src/main/resources/data/theaurorian2/structure/runestone_dungeon');
const blockstates = path.join(repository, 'src/main/resources/assets/theaurorian2/blockstates');

// These are exact existing registry names, not visual approximations or aliases.
// Additions to the source palette must be reviewed before this importer accepts them.
const knownBlocks = new Set([
  'aurorian_diorite', 'aurorian_diorite_wall', 'aurorian_dirt', 'aurorian_grass',
  'aurorian_grass_block', 'aurorian_stone_brick_wall', 'bright_moon_sandstone',
  'chiseled_moon_temple_bricks', 'chiseled_rune_stone', 'chiseled_rune_stone_wall',
  'curtain_tree_planks', 'indigo_mushroom_block', 'moon_glass', 'moon_glass_pane',
  'moon_temple_brick_slab', 'moon_temple_brick_stairs', 'moon_temple_brick_wall',
  'moon_temple_bricks', 'moon_temple_gate', 'moon_temple_lamp', 'moon_temple_pillar',
  'potted_petunia_plant', 'rune_stone', 'rune_stone_bars', 'rune_stone_gate',
  'rune_stone_gate_keyhole', 'rune_stone_lamp', 'rune_stone_stairs', 'rune_stone_wall',
  'silent_tree_leaves', 'silent_wood_chest', 'silent_wood_fence', 'silent_wood_ladder',
  'smooth_aurorian_peridotite', 'smooth_moon_temple_brick_slab',
  'smooth_moon_temple_brick_wall', 'smooth_moon_temple_bricks', 'smooth_rune_stone',
  'smooth_rune_stone_slab', 'smooth_rune_stone_stairs', 'smooth_rune_stone_wall',
  'tall_aurorian_grass', 'transparent_rune_stone', 'vertical_moon_temple_brick_stairs',
  'weeping_willow_leaves', 'weeping_willow_planks',
]);
const markers = new Set(['minecraft:jigsaw', 'minecraft:structure_block', 'minecraft:structure_void']);
const empty = new Set(['minecraft:air', 'minecraft:cave_air', 'minecraft:void_air']);
const foundation = new Set();
const totals = {building: 0, removedAir: 0, retainedAir: 0, removedMarkers: 0, removedGateData: 0, chests: 0};

function migratedName(name) {
  if (name.startsWith('minecraft:')) return name;
  assert(name.startsWith('theaurorian:'), `Unexpected block namespace: ${name}`);
  const id = name.slice('theaurorian:'.length);
  assert(knownBlocks.has(id), `Unreviewed legacy block: ${name}`);
  assert(fs.existsSync(path.join(blockstates, `${id}.json`)), `Missing new blockstate resource: ${id}`);
  return `theaurorian2:${id}`;
}

function position(block) {
  assert.equal(block.pos.type, 9);
  assert.equal(block.pos.value.itemType, 3);
  assert.equal(block.pos.value.value.length, 3);
  return block.pos.value.value;
}

function geometryDigest(root, migrate) {
  const hash = createHash('sha256');
  const palette = root.value.palette.value.value;
  let count = 0;
  for (const block of root.value.blocks.value.value) {
    const state = palette[block.state.value];
    const name = state.Name.value;
    if (markers.has(name) || empty.has(name)) continue;
    hash.update(JSON.stringify([position(block), migrate ? migratedName(name) : name, state.Properties ?? null]));
    count++;
  }
  return {count, digest: hash.digest('hex')};
}

function migrateBlockEntity(block, name, counts) {
  if (!block.nbt) return;
  const data = block.nbt.value;
  const id = data.id?.value;
  if (id === 'theaurorian:dungeon_stone_gate') {
    assert(['theaurorian:moon_temple_gate', 'theaurorian:rune_stone_gate',
      'theaurorian:rune_stone_gate_keyhole'].includes(name), `Gate data on ${name}`);
    // These templates contain only inactive default gate data. The new blocks
    // retain their locked block state, but do not use the legacy ticking BE.
    assert.deepEqual(Object.keys(data).sort(),
      ['DestroyCountdown', 'GateState', 'Unlock', 'UnlockInterval', 'id'].sort());
    assert.equal(data.Unlock.value, 0);
    assert.equal(data.UnlockInterval.value, 0);
    assert.equal(data.DestroyCountdown.value, 0);
    assert.deepEqual(data.GateState.value, {Name: {type: 8, value: 'minecraft:air'}});
    delete block.nbt;
    counts.removedGateData++;
  } else if (id === 'theaurorian:silent_wood_chest') {
    assert.equal(name, 'theaurorian:silent_wood_chest');
    assert.deepEqual(Object.keys(data).sort(), ['Items', 'id']);
    assert.equal(data.Items.value.value.length, 0, 'Review nonempty chest migration before importing');
    data.id.value = 'theaurorian2:silent_wood_chest';
    counts.chests++;
  } else {
    throw new Error(`Unexpected block entity ${id} on ${name}`);
  }
}

for (let part = 1; part <= 9; part++) {
  const file = `part_${part}.nbt`;
  const root = readNbt(path.join(source, file));
  assert.equal(root.type, 10);
  assert.equal(root.value.blocks.value.itemType, 10);
  assert.equal(root.value.palette.value.itemType, 10);
  assert.equal(root.value.entities.value.value.length, 0, 'Review template entities before importing');
  const offsetX = Math.floor((part - 1) / 3) * 120;
  const offsetZ = ((part - 1) % 3) * 120;
  const expectedSize = [part < 7 ? 120 : 77, 236, part % 3 ? 120 : 48];
  assert.deepEqual(root.value.size.value.value, expectedSize);
  const before = geometryDigest(root, true);
  const oldPalette = root.value.palette.value.value;
  const newPalette = [];
  const indices = new Map();
  const counts = {building: 0, removedAir: 0, retainedAir: 0, removedMarkers: 0, removedGateData: 0, chests: 0};
  root.value.blocks.value.value = root.value.blocks.value.value.filter(block => {
    const oldStateIndex = block.state.value;
    const state = oldPalette[oldStateIndex];
    const name = state.Name.value;
    const [x, y, z] = position(block);
    assert(x >= 0 && x < expectedSize[0] && y >= 0 && y < expectedSize[1] && z >= 0 && z < expectedSize[2]);
    if (markers.has(name)) {
      counts.removedMarkers++;
      return false;
    }
    // The exported four-column air strip spans the entire western edge. Keep
    // actual bricks in those columns and every air cell elsewhere unchanged.
    if (empty.has(name) && x + offsetX < 4) {
      counts.removedAir++;
      return false;
    }
    if (empty.has(name)) {
      counts.retainedAir++;
    } else {
      counts.building++;
      if (y === 0) foundation.add(`${x + offsetX},${z + offsetZ}`);
    }
    migrateBlockEntity(block, name, counts);
    if (!indices.has(oldStateIndex)) {
      const converted = structuredClone(state);
      converted.Name.value = migratedName(name);
      indices.set(oldStateIndex, newPalette.length);
      newPalette.push(converted);
    }
    block.state.value = indices.get(oldStateIndex);
    return true;
  });
  root.value.palette.value.value = newPalette;
  assert.deepEqual(geometryDigest(root, false), before, `${file}: building geometry changed`);
  const output = path.join(destination, file);
  writeNbt(output, root);
  const reopened = readNbt(output);
  assert.deepEqual(reopened, root, `${file}: NBT round-trip changed typed values`);
  assert.deepEqual(geometryDigest(reopened, false), before);
  for (const [key, value] of Object.entries(counts)) totals[key] += value;
  console.log(`${file}: ${JSON.stringify(counts)} geometry=${before.digest}`);
}

const minX = 82, minZ = 90, width = 122, depth = 124;
assert.equal(foundation.size, 6212, 'The ground footprint changed; review terrain adaptation');
const header = Buffer.alloc(24);
header.write('AURORD01', 0, 'ascii');
for (const [index, value] of [minX, minZ, width, depth].entries()) header.writeInt32BE(value, 8 + index * 4);
const bits = Buffer.alloc(Math.ceil(width * depth / 8));
for (const point of foundation) {
  const [x, z] = point.split(',').map(Number);
  assert(x >= minX && x < minX + width && z >= minZ && z < minZ + depth, `Foundation point outside header: ${point}`);
  const bit = (z - minZ) * width + x - minX;
  bits[bit >> 3] |= 1 << (bit & 7);
}
// Header: ASCII AURORD01, then four big-endian ints (minX, minZ, width, depth).
// Payload: row-major z/x occupancy, least-significant bit first in each byte.
const mask = Buffer.concat([header, bits]);
fs.writeFileSync(path.join(destination, 'foundation.dat'), mask);
assert.equal(mask.length, 1915);
console.log(`foundation.dat: ${foundation.size} columns; ${width}x${depth}; ${mask.length} bytes`);
console.log(`Total: ${JSON.stringify(totals)}`);
