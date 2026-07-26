package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AstrologyTableBlockEntity;
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

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
