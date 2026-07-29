package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AstrologyTableBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianGrassRockBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianFurnaceBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.FireplaceBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianTableBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AstrologyTableBlockEntity>> ASTROLOGY_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "astrology_table",
                    () -> new BlockEntityType<>(
                            AstrologyTableBlockEntity::new,
                            Set.of(ModBlocks.ASTROLOGY_TABLE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianGrassRockBlockEntity>>
            AURORIAN_GRASS_ROCK = BLOCK_ENTITY_TYPES.register(
                    "aurorian_grass_rock",
                    () -> new BlockEntityType<>(
                            AurorianGrassRockBlockEntity::new,
                            Set.of(ModBlocks.AURORIAN_GRASS_ROCK.get())));
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianChestBlockEntity>> AURORIAN_CHEST =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_chest",
                    () -> new BlockEntityType<>(
                            AurorianChestBlockEntity::new,
                            Set.of(ModBlocks.AURORIAN_CHEST.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianTableBlockEntity>> AURORIAN_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_table",
                    () -> new BlockEntityType<>(
                            AurorianTableBlockEntity::new,
                            Set.of(
                                    ModBlocks.SILENT_WOOD_TABLE.get(),
                                    ModBlocks.CURTAIN_WOOD_TABLE.get(),
                                    ModBlocks.CURSED_FROST_WOOD_TABLE.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
