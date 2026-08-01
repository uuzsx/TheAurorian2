import assert from 'node:assert/strict';
import path from 'node:path';
import {readNbt, writeNbt} from './migrate_structure_nbt.mjs';

const placements = {
  'aurorian_forest_memory_loop.nbt': {pos: [7, 0, 7], facing: 'south'},
  'aurorian_forest_remains.nbt': {pos: [7, 1, 7], facing: 'west'},
  'aurorian_forest_ruined_portal.nbt': {pos: [6, 4, 8], facing: 'south'},
  'aurorian_forest_shattered_forest_pillar.nbt': {pos: [7, 0, 7], facing: 'south'},
  'aurorian_forest_shattered_pillar.nbt': {pos: [7, 0, 7], facing: 'south'},
  'aurorian_forest_shattered_wreath.nbt': {pos: [7, 0, 7], facing: 'south'},
  'aurorian_forest_spring.nbt': {pos: [12, 2, 7], facing: 'west'},
};

function list(tag) {
  return tag.value.value;
}

function blockPosition(block) {
  return block.pos.value.value;
}

function samePosition(first, second) {
  return first.every((value, index) => value === second[index]);
}

function paletteName(palette, index) {
  return palette[index].Name.value;
}

function chestPaletteEntry(facing) {
  return {
    Name: {type: 8, value: 'theaurorian2:aurorian_chest'},
    Properties: {
      type: 10,
      value: {
        facing: {type: 8, value: facing},
        type: {type: 8, value: 'single'},
        waterlogged: {type: 8, value: 'false'},
      },
    },
  };
}

function emptyChestData() {
  return {
    type: 10,
    value: {
      id: {type: 8, value: 'theaurorian2:aurorian_chest'},
    },
  };
}

function findChestState(palette, facing) {
  return palette.findIndex(entry => entry.Name.value === 'theaurorian2:aurorian_chest'
    && entry.Properties?.value?.facing?.value === facing
    && entry.Properties?.value?.type?.value === 'single'
    && entry.Properties?.value?.waterlogged?.value === 'false');
}

function verifyChest(filePath, expected) {
  const root = readNbt(filePath).value;
  const palette = list(root.palette);
  const chests = list(root.blocks).filter(block => {
    return paletteName(palette, block.state.value) === 'theaurorian2:aurorian_chest';
  });
  assert.equal(chests.length, 1, `${filePath} must contain exactly one Aurorian chest`);
  assert.deepEqual(blockPosition(chests[0]), expected.pos, `${filePath} chest is at the wrong position`);
  assert.equal(chests[0].nbt?.value?.id?.value, 'theaurorian2:aurorian_chest');
  assert.equal(chests[0].nbt?.value?.LootTable, undefined, `${filePath} must not define loot yet`);
}

const directory = process.argv[2];
if (!directory) {
  console.error('Usage: node add_medium_ruin_chests.mjs <medium-ruin-directory>');
  process.exit(2);
}

for (const [fileName, placement] of Object.entries(placements)) {
  const filePath = path.join(directory, fileName);
  const root = readNbt(filePath);
  const palette = list(root.value.palette);
  const blocks = list(root.value.blocks);
  const target = blocks.find(block => samePosition(blockPosition(block), placement.pos));
  if (!target) {
    throw new Error(`${fileName} has no block entry at ${placement.pos.join(',')}`);
  }

  const oldName = paletteName(palette, target.state.value);
  if (oldName !== 'minecraft:air' && oldName !== 'theaurorian2:aurorian_chest') {
    throw new Error(`${fileName} chest position contains ${oldName}, expected air`);
  }

  let chestState = findChestState(palette, placement.facing);
  if (chestState === -1) {
    chestState = palette.length;
    palette.push(chestPaletteEntry(placement.facing));
  }
  target.state.value = chestState;
  target.nbt = emptyChestData();
  writeNbt(filePath, root);
  verifyChest(filePath, placement);
  console.log(`${fileName}: chest at ${placement.pos.join(',')} facing ${placement.facing}`);
}
