package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.loot.AurorianFishingLootModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TheAurorian2.MOD_ID);

    static {
        SERIALIZERS.register("aurorian_fishing", () -> AurorianFishingLootModifier.CODEC);
    }

    private ModLootModifiers() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
