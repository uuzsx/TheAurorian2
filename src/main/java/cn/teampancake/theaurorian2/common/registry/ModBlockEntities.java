package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AstrologyTableBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.ArcaneMagicCircleBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianFurnaceBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.FireplaceBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianTableBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianHangingSignBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianSignBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.CrystallineSwordPedestalBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.SacrificeTableBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.SilentCampfireBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.SpiderMotherSpawnerBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianUrnBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.LongMirrorBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LongMirrorBlockEntity>> LONG_MIRROR =
            BLOCK_ENTITY_TYPES.register("long_mirror", () -> new BlockEntityType<>(
                    LongMirrorBlockEntity::new,
                    Set.of(ModBlocks.SILENT_WOOD_LONG_MIRROR.get(), ModBlocks.WEEPING_WILLOW_LONG_MIRROR.get(),
                            ModBlocks.CURTAIN_WOOD_LONG_MIRROR.get(), ModBlocks.CURSED_FROST_WOOD_LONG_MIRROR.get(),
                            ModBlocks.FILTHY_WOOD_LONG_MIRROR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AstrologyTableBlockEntity>> ASTROLOGY_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "astrology_table",
                    () -> new BlockEntityType<>(
                            AstrologyTableBlockEntity::new,
                            Set.of(ModBlocks.ASTROLOGY_TABLE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneMagicCircleBlockEntity>>
            ARCANE_MAGIC_CIRCLE = BLOCK_ENTITY_TYPES.register(
                    "arcane_magic_circle",
                    () -> new BlockEntityType<>(
                            ArcaneMagicCircleBlockEntity::new,
                            Set.of(ModBlocks.ARCANE_MAGIC_CIRCLE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PurificationAltarBlockEntity>>
            PURIFICATION_ALTAR = BLOCK_ENTITY_TYPES.register(
                    "purification_altar",
                    () -> new BlockEntityType<>(
                            PurificationAltarBlockEntity::new,
                            Set.of(ModBlocks.PURIFICATION_ALTAR.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SacrificeTableBlockEntity>> SACRIFICE_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "sacrifice_table",
                    () -> new BlockEntityType<>(
                            SacrificeTableBlockEntity::new,
                            Set.of(ModStructureBlocks.SACRIFICE_TABLE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystallineSwordPedestalBlockEntity>>
            CRYSTALLINE_SWORD_PEDESTAL = BLOCK_ENTITY_TYPES.register(
                    "crystalline_sword_pedestal",
                    () -> new BlockEntityType<>(
                            CrystallineSwordPedestalBlockEntity::new,
                            Set.of(ModStructureBlocks.CRYSTALLINE_SWORD_PEDESTAL.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianFurnaceBlockEntity>> AURORIAN_FURNACE =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_furnace",
                    () -> new BlockEntityType<>(
                            AurorianFurnaceBlockEntity::new,
                            Set.of(ModBlocks.AURORIAN_FURNACE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireplaceBlockEntity>> FIREPLACE =
            BLOCK_ENTITY_TYPES.register(
                    "fireplace",
                    () -> new BlockEntityType<>(
                            FireplaceBlockEntity::new,
                            Set.of(ModBlocks.FIREPLACE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianChestBlockEntity>> SILENT_WOOD_CHEST =
            BLOCK_ENTITY_TYPES.register(
                    "silent_wood_chest",
                    () -> new BlockEntityType<>(
                            AurorianChestBlockEntity::new,
                            Set.of(ModBlocks.SILENT_WOOD_CHEST.get(), ModBlocks.WEEPING_WILLOW_CHEST.get(),
                                    ModBlocks.CURTAIN_WOOD_CHEST.get(), ModBlocks.CURSED_FROST_WOOD_CHEST.get(),
                                    ModBlocks.FILTHY_WOOD_CHEST.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianTableBlockEntity>> AURORIAN_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_table",
                    () -> new BlockEntityType<>(
                            AurorianTableBlockEntity::new,
                            Set.of(
                                    ModBlocks.SILENT_WOOD_TABLE.get(),
                                    ModBlocks.CURTAIN_WOOD_TABLE.get(),
                                    ModBlocks.CURSED_FROST_WOOD_TABLE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianSignBlockEntity>> AURORIAN_SIGN =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_sign",
                    () -> new BlockEntityType<>(
                            AurorianSignBlockEntity::new,
                            Set.of(
                                    ModStructureBlocks.SILENT_WOOD_SIGN.get(),
                                    ModStructureBlocks.SILENT_WOOD_WALL_SIGN.get(),
                                    ModStructureBlocks.WEEPING_WILLOW_WOOD_SIGN.get(),
                                    ModStructureBlocks.WEEPING_WILLOW_WOOD_WALL_SIGN.get(),
                                    ModStructureBlocks.CURTAIN_WOOD_SIGN.get(),
                                    ModStructureBlocks.CURTAIN_WOOD_WALL_SIGN.get(),
                                    ModStructureBlocks.CURSED_FROST_WOOD_SIGN.get(),
                                    ModStructureBlocks.CURSED_FROST_WOOD_WALL_SIGN.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianHangingSignBlockEntity>>
            AURORIAN_HANGING_SIGN = BLOCK_ENTITY_TYPES.register(
                    "aurorian_hanging_sign",
                    () -> new BlockEntityType<>(
                            AurorianHangingSignBlockEntity::new,
                            Set.of(
                                    ModStructureBlocks.SILENT_WOOD_HANGING_SIGN.get(),
                                    ModStructureBlocks.SILENT_WOOD_WALL_HANGING_SIGN.get(),
                                    ModStructureBlocks.WEEPING_WILLOW_WOOD_HANGING_SIGN.get(),
                                    ModStructureBlocks.WEEPING_WILLOW_WOOD_WALL_HANGING_SIGN.get(),
                                    ModStructureBlocks.CURTAIN_WOOD_HANGING_SIGN.get(),
                                    ModStructureBlocks.CURTAIN_WOOD_WALL_HANGING_SIGN.get(),
                                    ModStructureBlocks.CURSED_FROST_WOOD_HANGING_SIGN.get(),
                                    ModStructureBlocks.CURSED_FROST_WOOD_WALL_HANGING_SIGN.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SilentCampfireBlockEntity>>
            SILENT_CAMPFIRE = BLOCK_ENTITY_TYPES.register(
                    "silent_campfire",
                    () -> new BlockEntityType<>(
                            SilentCampfireBlockEntity::new,
                            Set.of(ModStructureBlocks.SILENT_CAMPFIRE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpiderMotherSpawnerBlockEntity>>
            SPIDER_MOTHER_SPAWNER = BLOCK_ENTITY_TYPES.register(
                    "spider_mother_spawner",
                    () -> new BlockEntityType<>(
                            SpiderMotherSpawnerBlockEntity::new,
                            Set.of(ModStructureBlocks.SPIDER_MOTHER_SPAWNER.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianUrnBlockEntity>> AURORIAN_URN =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_urn",
                    () -> new BlockEntityType<>(
                            AurorianUrnBlockEntity::new,
                            Set.of(ModStructureBlocks.URN.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.addAlias(TheAurorian2.id("aurorian_chest"), TheAurorian2.id("silent_wood_chest"));
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
