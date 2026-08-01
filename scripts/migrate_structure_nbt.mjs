import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import zlib from 'node:zlib';

const LEGACY_BLOCK_MAPPINGS = new Map([
  ['silent_wood_chest', 'aurorian_chest'],
  ['moon_water', 'moon_dew'],
  ['snow_aurorian_grass_block', 'aurorian_grass_block'],
]);

class NbtReader {
  constructor(buffer) {
    this.buffer = buffer;
    this.offset = 0;
  }

  readByte() {
    return this.buffer.readInt8(this.offset++);
  }

  readUnsignedByte() {
    return this.buffer.readUInt8(this.offset++);
  }

  readShort() {
    const value = this.buffer.readInt16BE(this.offset);
    this.offset += 2;
    return value;
  }

  readUnsignedShort() {
    const value = this.buffer.readUInt16BE(this.offset);
    this.offset += 2;
    return value;
  }

  readInt() {
    const value = this.buffer.readInt32BE(this.offset);
    this.offset += 4;
    return value;
  }

  readLong() {
    const value = this.buffer.readBigInt64BE(this.offset);
    this.offset += 8;
    return value;
  }

  readFloat() {
    const value = this.buffer.readFloatBE(this.offset);
    this.offset += 4;
    return value;
  }

  readDouble() {
    const value = this.buffer.readDoubleBE(this.offset);
    this.offset += 8;
    return value;
  }

  readString() {
    const length = this.readUnsignedShort();
    const value = this.buffer.toString('utf8', this.offset, this.offset + length);
    this.offset += length;
    return value;
  }

  readPayload(type) {
    switch (type) {
      case 1:
        return this.readByte();
      case 2:
        return this.readShort();
      case 3:
        return this.readInt();
      case 4:
        return this.readLong();
      case 5:
        return this.readFloat();
      case 6:
        return this.readDouble();
      case 7: {
        const length = this.readInt();
        const value = Buffer.from(this.buffer.subarray(this.offset, this.offset + length));
        this.offset += length;
        return value;
      }
      case 8:
        return this.readString();
      case 9: {
        const itemType = this.readUnsignedByte();
        const length = this.readInt();
        return {
          itemType,
          value: Array.from({length}, () => this.readPayload(itemType)),
        };
      }
      case 10: {
        const value = {};
        while (true) {
          const childType = this.readUnsignedByte();
          if (childType === 0) {
            return value;
          }
          value[this.readString()] = {type: childType, value: this.readPayload(childType)};
        }
      }
      case 11: {
        const length = this.readInt();
        return Array.from({length}, () => this.readInt());
      }
      case 12: {
        const length = this.readInt();
        return Array.from({length}, () => this.readLong());
      }
      default:
        throw new Error(`Unknown NBT tag type ${type} at offset ${this.offset - 1}`);
    }
  }
}

class NbtWriter {
  constructor() {
    this.parts = [];
  }

  writeNumber(size, method, value) {
    const buffer = Buffer.allocUnsafe(size);
    buffer[method](value, 0);
    this.parts.push(buffer);
  }

  writeByte(value) {
    this.writeNumber(1, 'writeInt8', value);
  }

  writeUnsignedByte(value) {
    this.writeNumber(1, 'writeUInt8', value);
  }

  writeShort(value) {
    this.writeNumber(2, 'writeInt16BE', value);
  }

  writeUnsignedShort(value) {
    this.writeNumber(2, 'writeUInt16BE', value);
  }

  writeInt(value) {
    this.writeNumber(4, 'writeInt32BE', value);
  }

  writeLong(value) {
    this.writeNumber(8, 'writeBigInt64BE', value);
  }

  writeFloat(value) {
    this.writeNumber(4, 'writeFloatBE', value);
  }

  writeDouble(value) {
    this.writeNumber(8, 'writeDoubleBE', value);
  }

  writeString(value) {
    const buffer = Buffer.from(value, 'utf8');
    if (buffer.length > 65535) {
      throw new Error(`NBT string is too long: ${buffer.length} bytes`);
    }
    this.writeUnsignedShort(buffer.length);
    this.parts.push(buffer);
  }

  writePayload(type, value) {
    switch (type) {
      case 1:
        this.writeByte(value);
        break;
      case 2:
        this.writeShort(value);
        break;
      case 3:
        this.writeInt(value);
        break;
      case 4:
        this.writeLong(value);
        break;
      case 5:
        this.writeFloat(value);
        break;
      case 6:
        this.writeDouble(value);
        break;
      case 7:
        this.writeInt(value.length);
        this.parts.push(value);
        break;
      case 8:
        this.writeString(value);
        break;
      case 9:
        this.writeUnsignedByte(value.itemType);
        this.writeInt(value.value.length);
        value.value.forEach(entry => this.writePayload(value.itemType, entry));
        break;
      case 10:
        for (const [name, child] of Object.entries(value)) {
          this.writeUnsignedByte(child.type);
          this.writeString(name);
          this.writePayload(child.type, child.value);
        }
        this.writeUnsignedByte(0);
        break;
      case 11:
        this.writeInt(value.length);
        value.forEach(entry => this.writeInt(entry));
        break;
      case 12:
        this.writeInt(value.length);
        value.forEach(entry => this.writeLong(entry));
        break;
      default:
        throw new Error(`Unknown NBT tag type ${type}`);
    }
  }

  toBuffer() {
    return Buffer.concat(this.parts);
  }
}

export function readNbt(filePath) {
  const source = fs.readFileSync(filePath);
  const buffer = source[0] === 0x1f && source[1] === 0x8b ? zlib.gunzipSync(source) : source;
  const reader = new NbtReader(buffer);
  const type = reader.readUnsignedByte();
  const name = reader.readString();
  const value = reader.readPayload(type);
  if (reader.offset !== buffer.length) {
    throw new Error(`${filePath} contains ${buffer.length - reader.offset} unread bytes`);
  }
  return {type, name, value};
}

export function writeNbt(filePath, root) {
  const writer = new NbtWriter();
  writer.writeUnsignedByte(root.type);
  writer.writeString(root.name);
  writer.writePayload(root.type, root.value);
  fs.mkdirSync(path.dirname(filePath), {recursive: true});
  fs.writeFileSync(filePath, zlib.gzipSync(writer.toBuffer(), {level: 9}));
}

export function replacePrefix(type, value, oldPrefix, newPrefix) {
  if (type === 8) {
    if (!value.startsWith(oldPrefix)) return value;
    const oldId = value.slice(oldPrefix.length);
    return newPrefix + (LEGACY_BLOCK_MAPPINGS.get(oldId) ?? oldId);
  }
  if (type === 9) {
    value.value = value.value.map(entry => replacePrefix(value.itemType, entry, oldPrefix, newPrefix));
  } else if (type === 10) {
    for (const child of Object.values(value)) {
      child.value = replacePrefix(child.type, child.value, oldPrefix, newPrefix);
    }
  }
  return value;
}

if (import.meta.url === pathToFileURL(process.argv[1]).href) {
  const [inputPath, outputPath, oldPrefix = 'theaurorian:', newPrefix = 'theaurorian2:'] = process.argv.slice(2);
  if (!inputPath || !outputPath) {
    console.error('Usage: node migrate_structure_nbt.mjs <input> <output> [old-prefix] [new-prefix]');
    process.exit(2);
  }

  const root = readNbt(inputPath);
  root.value = replacePrefix(root.type, root.value, oldPrefix, newPrefix);
  writeNbt(outputPath, root);
  assert.deepStrictEqual(readNbt(outputPath), root);
  console.log(`Migrated ${inputPath} -> ${outputPath}`);
}
