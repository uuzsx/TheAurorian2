package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.placement.NotInUmbraDarkMazePlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<NotInUmbraDarkMazePlacement>>
            NOT_IN_UMBRA_DARK_MAZE = PLACEMENT_MODIFIERS.register(
                    "not_in_umbra_dark_maze", () -> () -> NotInUmbraDarkMazePlacement.CODEC);

    private ModPlacementModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        PLACEMENT_MODIFIERS.register(modEventBus);
    }
}
