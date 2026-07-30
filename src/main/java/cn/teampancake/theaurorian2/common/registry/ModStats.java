package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStats {

    private static final DeferredRegister<Identifier> CUSTOM_STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, TheAurorian2.MOD_ID);

    public static final DeferredHolder<Identifier, Identifier> INTERACT_WITH_AURORIAN_FURNACE =
            CUSTOM_STATS.register("interact_with_aurorian_furnace", id -> id);
    public static final DeferredHolder<Identifier, Identifier> INTERACT_WITH_FIREPLACE =
            CUSTOM_STATS.register("interact_with_fireplace", id -> id);

    private ModStats() {
    }

    public static void register(IEventBus modEventBus) {
        CUSTOM_STATS.register(modEventBus);
    }

    public static void bootstrap() {
        Stats.CUSTOM.get(INTERACT_WITH_AURORIAN_FURNACE.get(), StatFormatter.DEFAULT);
        Stats.CUSTOM.get(INTERACT_WITH_FIREPLACE.get(), StatFormatter.DEFAULT);
    }
}
