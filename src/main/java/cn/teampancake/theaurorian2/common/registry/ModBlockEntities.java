package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AstrologyTableBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import cn.teampancake.theaurorian2.common.block.entity.AurorianFurnaceBlockEntity;
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianFurnaceBlockEntity>> AURORIAN_FURNACE =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_furnace",
                    () -> new BlockEntityType<>(
                            AurorianFurnaceBlockEntity::new,
                            Set.of(ModBlocks.AURORIAN_FURNACE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AurorianChestBlockEntity>> AURORIAN_CHEST =
            BLOCK_ENTITY_TYPES.register(
                    "aurorian_chest",
                    () -> new BlockEntityType<>(
                            AurorianChestBlockEntity::new,
                            Set.of(ModBlocks.AURORIAN_CHEST.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
